 package eu.dm2e.ws;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyReader;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.ws.api.SerializablePojo;
 import eu.dm2e.ws.api.json.OmnomJsonSerializer;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.GStatement;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 
 //@Singleton
 @Provider
 @Produces({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 		// , MediaType.TEXT_HTML
 		, DM2E_MediaType.TEXT_PLAIN
 		, MediaType.APPLICATION_JSON
 })
 @Consumes({
 		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
 		DM2E_MediaType.APPLICATION_RDF_XML,
 		DM2E_MediaType.APPLICATION_X_TURTLE,
 		DM2E_MediaType.TEXT_RDF_N3,
 		DM2E_MediaType.TEXT_TURTLE
 		// , MediaType.TEXT_HTML
 		, DM2E_MediaType.TEXT_PLAIN
 		, MediaType.APPLICATION_JSON
 })
 public class SerializablePojoProvider implements MessageBodyWriter<SerializablePojo>,
 		MessageBodyReader<SerializablePojo> {
 
 	Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	@Override
 	public boolean isWriteable(Class<?> type,
 			Type genericType,
 			Annotation[] annotations,
 			MediaType mediaType) {
 		// return true;
 		// return false;
 		return (SerializablePojo.class.isAssignableFrom(type)
 		&& DM2E_MediaType.expectsMetadataResponse(mediaType));
 	}
 
 	@Override
 	public long getSize(SerializablePojo t,
 			Class<?> type,
 			Type genericType,
 			Annotation[] annotations,
 			MediaType mediaType) {
 		// This is deprecated
 		return -1;
 	}
 
 	@Override
 	public void writeTo(SerializablePojo t,
 			Class<?> type,
 			Type genericType,
 			Annotation[] annotations,
 			MediaType mediaType,
 			MultivaluedMap<String, Object> httpHeaders,
 			OutputStream entityStream)
 			throws IOException, WebApplicationException {
 		if (DM2E_MediaType.isJsonMediaType(mediaType)) {
 			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to JSON: {}", t.toJson());
 			entityStream.write(t.toJson().getBytes("UTF-8"));
 		} else if (DM2E_MediaType.isRdfMediaType(mediaType)) {
 			// TODO FIXME this is duplicated in GrafeoMessageBodyWriter
 			GrafeoImpl g = (GrafeoImpl) t.getGrafeo();
 			final String jenaLng = DM2E_MediaType.getJenaLanguageForMediaType(mediaType);
 			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to RDF ({}), as terse turtle: {}",
 					jenaLng, g.getTerseTurtle());
 			g.getModel().write(entityStream, jenaLng);
 		}
 		log.debug("Finished writing to entityStream");
 	}
 
 	@Override
 	public boolean isReadable(Class<?> type,
 			Type genericType,
 			Annotation[] annotations,
 			MediaType mediaType) {
 		log.debug("I AM THE PROVIDER");
 		return SerializablePojo.class.isAssignableFrom(type);
 	}
 
 	// FIXME ugly ugly ugly
 	@Override
 	public SerializablePojo readFrom(Class<SerializablePojo> type,
 			Type genericType,
 			Annotation[] annotations,
 			MediaType mediaType,
 			MultivaluedMap<String, String> httpHeaders,
 			InputStream entityStream) throws IOException, WebApplicationException {
 
 
 		log.debug("MEDIA_TYPE " + mediaType);
 		log.debug("GENERIC TYPE " + genericType);
 		Class<?> pojoClass = (Class<?>) genericType;
 		SerializablePojo pojo;
 		if (DM2E_MediaType.isJsonMediaType(mediaType)) {
 			java.util.Scanner scanner = new java.util.Scanner(entityStream);
 			java.util.Scanner s = scanner.useDelimiter("\\A");
 			String str = s.hasNext() ? s.next() : null;
 			scanner.close();
 			pojo = (SerializablePojo) OmnomJsonSerializer.deserializeFromJSON(str, pojoClass);
 		} else if (DM2E_MediaType.isRdfMediaType(mediaType)) {
 			GrafeoImpl g = new GrafeoImpl(entityStream);
 			// TODO find top blank node etc.
 			String rdfType = pojoClass.getAnnotation(RDFClass.class).value();
 			Set<GStatement> set = g.listResourceStatements(null, "rdf:type", rdfType);
 			if (set.isEmpty()) {
 				throw new RuntimeException();
 			}
 			GResource res = set.iterator().next().getSubject();
 			pojo = (SerializablePojo) g.getObjectMapper().getObject(pojoClass, res);
 		} else {
			throw new RuntimeException("Can't deserialize this: " + mediaType);
 		}
 		return pojo;
 	}
 }
