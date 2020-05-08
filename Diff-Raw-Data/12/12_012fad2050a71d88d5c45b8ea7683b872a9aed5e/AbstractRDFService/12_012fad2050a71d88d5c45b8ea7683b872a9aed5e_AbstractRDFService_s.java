 package eu.dm2e.ws.services.data;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.*;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 @Produces({ MediaType.TEXT_PLAIN, "application/rdf+xml",
 		"application/x-turtle", "text/turtle", "text/rdf+n3" })
 @Consumes({ MediaType.TEXT_PLAIN, "application/rdf+xml",
 		"application/x-turtle", "text/turtle", "text/rdf+n3",
 		MediaType.MULTIPART_FORM_DATA })
 public abstract class AbstractRDFService {
 
 	public static final String PLAIN = MediaType.TEXT_PLAIN;
 	public static final String XML = "application/rdf+xml";
 	public static final String TTL_A = "application/x-turtle";
 	public static final String TTL_T = "text/turtle";
 	public static final String N3 = "text/rdf+n3";
 
 	List<Variant> supportedVariants;
 	Map<MediaType, String> mediaType2Language = new HashMap<MediaType, String>();
 	@Context
 	Request request;
 	@Context
 	protected UriInfo uriInfo;
 	@Context 
 	protected HttpHeaders headers;
 
 	protected String getRequestUriString() {
 		String str =  uriInfo.getRequestUri().toString();
 		// remove query string
 		str = str.replaceFirst("\\?.*$", "");
 		return str;
 	}
 
 	protected AbstractRDFService() {
 		this.supportedVariants = Variant
 				.mediaTypes(MediaType.valueOf(PLAIN), MediaType.valueOf(XML),
 						MediaType.valueOf(TTL_A), MediaType.valueOf(TTL_T),
 						MediaType.valueOf(N3)).add().build();
 		mediaType2Language.put(MediaType.valueOf(PLAIN), "N-TRIPLE");
 		mediaType2Language.put(MediaType.valueOf(XML), "RDF/XML");
 		mediaType2Language.put(MediaType.valueOf(TTL_A), "TURTLE");
 		mediaType2Language.put(MediaType.valueOf(TTL_T), "TURTLE");
 		mediaType2Language.put(MediaType.valueOf(N3), "N3");
 	}
 
 	protected Response getResponse(Model model) {
 		Variant selectedVariant = request.selectVariant(supportedVariants);
 		assert selectedVariant != null;
 
 		return Response.ok(
 				new RDFOutput(model, selectedVariant.getMediaType()),
 				selectedVariant.getMediaType()).build();
 
 	}
 
 	protected Response getResponse(Grafeo grafeo) {
 		return getResponse(((GrafeoImpl) grafeo).getModel());
 
 	}
 
 	protected StreamingOutput getResponseEntity(Model model) {
 		Variant selectedVariant = request.selectVariant(supportedVariants);
 		assert selectedVariant != null;
 		return new RDFOutput(model, selectedVariant.getMediaType());
 
 	}
 
 	protected StreamingOutput getResponseEntity(Grafeo grafeo) {
 		return getResponseEntity(((GrafeoImpl) grafeo).getModel());
 
 	}
 
 	protected class RDFOutput implements StreamingOutput {
 		Logger log = Logger.getLogger(getClass().getName());
 		Model model;
 		MediaType mediaType;
 
 		public RDFOutput(Model model, MediaType mediaType) {
 			this.model = model;
 			this.mediaType = mediaType;
 		}
 
 		@Override
 		public void write(OutputStream output) throws IOException,
 				WebApplicationException {
 			log.fine("Mediatype: " + this.mediaType);
 			model.write(output, mediaType2Language.get(this.mediaType));
 		}
 	}
 }
