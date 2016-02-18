/**
 * 
 */
package fr.toutatice.ecm.es.customizer.nx.writer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonESDocumentWriter;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.ComplexProperty;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.es.customizer.registry.ESCustomizersServiceRegistry;
import fr.toutatice.ecm.es.customizer.writers.ICustomJsonESWriter;

/**
 * @author david
 *
 */
@Provider
@Produces({ JsonESDocumentWriter.MIME_TYPE })
public class JsonESDocumentWriterCustomizer extends JsonESDocumentWriter {

    /**
     * Registry of customizers.
     */
    private static ESCustomizersServiceRegistry registry;
    
    /**
     * @return registry of customizers.
     */
    public static ESCustomizersServiceRegistry getESCustomizersServiceRegistry() {
        if (registry == null) {
            registry = Framework.getService(ESCustomizersServiceRegistry.class);
        }
        return registry;
    }

	/**
	 * Default (i.e. not custom) writing doc.
	 * 
	 * @param jg
	 * @param doc
	 * @param schemas
	 * @param contextParameters
	 * @throws IOException
	 */
	public void writeNativeESDocument(JsonGenerator jg, DocumentModel doc,
			String[] schemas, Map<String, String> contextParameters)
			throws IOException {
		super.writeDoc(jg, doc, schemas, contextParameters, headers);
	}

	/**
	 * Override default behavior to add custom data.
	 */
	@Override
	public void writeDoc(JsonGenerator jg, DocumentModel doc, String[] schemas,
			Map<String, String> contextParameters, HttpHeaders headers)
			throws IOException {
		jg.writeStartObject();
		super.writeSystemProperties(jg, doc);
		super.writeSchemas(jg, doc, schemas);
		super.writeContextParameters(jg, doc, contextParameters);
		writeData(jg, doc, schemas, contextParameters);
		jg.writeEndObject();
		jg.flush();
	}

    /**
	 * Writes custom metadata.
	 * 
	 * @param jg
	 * @param doc
	 * @param schemas
	 * @param contextParameters
	 * @throws IOException
	 */
	protected void writeData(JsonGenerator jg, DocumentModel doc,
			String[] schemas, Map<String, String> contextParameters)
			throws IOException {
		for (ICustomJsonESWriter customJsonESWriter : getESCustomizersServiceRegistry()
				.getCustomJsonESWriters()) {
			customJsonESWriter.setJsonESWriter(this);
			customJsonESWriter.writeData(jg, doc, schemas, contextParameters);
		}
	}

}
