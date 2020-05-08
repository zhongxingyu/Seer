 package eu.dm2e.ws.services;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import eu.dm2e.ws.Config;
import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.api.ParameterPojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.commons.validator.routines.UrlValidator;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * TODO @GET /{id}/param/{param} 303 -> /{id}
  *
  */
 @Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, "application/rdf+xml",
 		"application/x-turtle", "text/turtle", "text/rdf+n3" })
 @Consumes({ MediaType.TEXT_PLAIN, "application/rdf+xml",
 		"application/x-turtle", "text/turtle", "text/rdf+n3",
 		MediaType.MULTIPART_FORM_DATA })
 public abstract class AbstractRDFService {
 
 	protected Logger log = Logger.getLogger(getClass().getName());
 	
 	public static final String PLAIN = MediaType.TEXT_PLAIN;
 	public static final String XML = "application/rdf+xml";
 	public static final String TTL_A = "application/x-turtle";
 	public static final String TTL_T = "text/turtle";
 	public static final String N3 = "text/rdf+n3";
	public static final String RDF_TRIPLES = "application/rdf-triples";
 	protected static String[] allowedSchemes = { "http", "https", "file", "ftp" };
 	protected static final UrlValidator urlValidator = new UrlValidator(allowedSchemes,
 		UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS
 	);
     protected WebservicePojo webservicePojo = new WebservicePojo();
 
 	protected List<Variant> supportedVariants;
 	Map<MediaType, String> mediaType2Language = new HashMap<MediaType, String>();
 	@Context
 	Request request;
 	@Context
 	protected UriInfo uriInfo;
 	@Context 
 	protected HttpHeaders headers;
 	
 	protected Response throwServiceError(String msg, int status) {
 		return Response.status(status).entity(msg).build();
 	}
 	protected Response throwServiceError(String msg) {
 		return throwServiceError(msg, 400);
 	}
 	protected Response throwServiceError(Exception e) {
 		return throwServiceError(e.toString() + "\n" + ExceptionUtils.getStackTrace(e), 400);
 	}
	protected Response throwServiceError(ErrorMsg err) {
		return throwServiceError(err.getMessage());
	}
	protected Response throwServiceError(String badString, ErrorMsg err) {
		return throwServiceError(badString + ": " + err.getMessage());
	}
 	
 	protected URI getUriForString(String uriStr) throws URISyntaxException {
 		if (null == uriStr)
 			throw new URISyntaxException("", "Must provide 'uri' query parameter.");
 		
 		// this might throw a URISyntaxException as well
 		URI uri = new URI(uriStr);
 		
 		// stricter validation than just throwing an URISyntaxException (Sesame is picky about URLs)
 		if (notValid(uriStr))
 			throw new URISyntaxException(uriStr, "'uri' parameter is not a valid URI.");
 		
 		return uri;
 		
 	}
 
 	protected URI getRequestUriWithoutQuery() {
 		UriBuilder ub =  uriInfo.getRequestUriBuilder();
 		ub.replaceQuery("");
 		return ub.build();
 	}
 	
 //	protected GrafeoImpl getServiceDescriptionGrafeo() throws IOException  {
 ////        InputStream descriptionStream  = Thread.currentThread().getContextClassLoader().getResourceAsStream("xslt-service-description.ttl");
 ////		System.out.println(getServiceDescriptionResourceName());
 ////		InputStream descriptionStream = ClassLoader.getSystemResource(getServiceDescriptionResourceName()).openStream();
 //        InputStream descriptionStream  = this.getClass().getResourceAsStream("service-description.ttl");
 //		if (null == descriptionStream) {
 //			throw new FileNotFoundException();
 //		}
 //        GrafeoImpl g = new GrafeoImpl(descriptionStream, "TURTLE");
 //        // rename top blank node if any
 //        GResource blank = g.findTopBlank();
 //        String uri = getRequestUriWithoutQuery().toString();
 //        if (blank!=null) blank.rename(uri);
 //		return g;
 //	}
 	
 
 
     /**
      * Default implementation of the webservice description.
      * Implementing subclasses should provide further information
      * by calling the setters of the returned description pojo.
      *
      * @return The webservice description
      */
     public  WebservicePojo getWebServicePojo() {
         if (webservicePojo.getId()==null)    {
             String base = Config.config.getString("dm2e.ws.base_uri");
             String path = this.getClass().getAnnotation(Path.class).value();
             if (base.endsWith("/") && path.startsWith("/")) base = base.substring(0,base.length()-1);
             webservicePojo.setId(base + path);
         }
         return webservicePojo;
     }
 
     /**
      *
      * Implementation of the default behaviour, which is a 303 redirect
      * from the base URI to /describe, where the webservice description is returned.
      *
      * @param uriInfo
      * @return
      */
     @GET
     public Response getBase(@Context UriInfo uriInfo)  {
         URI uri = appendPath(uriInfo.getRequestUri(),"describe");
         return Response.seeOther(uri).build();
 
     }
     @GET
     @Path("{id}/nested/{nestedId}")
     public Response getConfigAssignment(
     		@Context UriInfo uriInfo,
      		@PathParam("id") String id,
      		@PathParam("nestedId") String nestedId
     		) {
         log.info("Nested resource " + nestedId + " of service requested: " + uriInfo.getRequestUri());
 //        Grafeo g = new GrafeoImpl();
         // @TODO should proabably use getRequestUriWithoutQuery().toString() here
 //        g.readFromEndpoint(NS.ENDPOINT, uriInfo.getRequestUri().toString());
 //        return getResponse(g);
         return Response.seeOther(getRequestUriWithoutQuery()).build();
     }
 
     /**
      * The serialization of the webservice description is returned.
      *
      * @param uriInfo
      * @return
      */
     @GET
 	@Path("/describe")
 	public Response getDescription(@Context UriInfo uriInfo)  {
         WebservicePojo wsDesc = this.getWebServicePojo();
         Grafeo g = new GrafeoImpl();
         g.getObjectMapper().addObject(wsDesc);
         return Response.ok().entity(getResponseEntity(g)).build();
 	}
     
     @GET
     @Path("/param/{paramId}")
     public Response getParamDescription() {
     	String baseURIstr = getRequestUriWithoutQuery().toString();
     	baseURIstr = baseURIstr.replaceAll("/param/[^/]+$", "");
     	URI baseURI;
 		try {
 			baseURI = new URI(baseURIstr);
 		} catch (URISyntaxException e) {
 //			throw(e);
 			return throwServiceError(e);
 		}
     	return Response.seeOther(baseURI).build();
     }
 	
 	
 	@PUT
 	@Path("validate")
 	public Response validateConfigRequest(String configUriStr) {
 		try {
 			validateServiceInput(configUriStr);
 		} catch (Exception e) {
 			return throwServiceError(e);
 		}
 		return Response.noContent().build();
 	}
 	
 	protected Grafeo getGrafeoForUriWithContentNegotiation(String uriStr) throws IOException, URISyntaxException {
 		return getGrafeoForUriWithContentNegotiation(getUriForString(uriStr));
 	}
 	protected Grafeo getGrafeoForUriWithContentNegotiation(URI uri) throws IOException {
 		URL url = new URL(uri.toString());
 		InputStream in = null;
 		HttpURLConnection con = (HttpURLConnection) url.openConnection();
 		con.setRequestMethod("GET");
 		con.setRequestProperty("Accept", "text/turtle");
 		con.connect();
 		in = con.getInputStream();
 		GrafeoImpl tempG = new GrafeoImpl();
 		tempG.getModel().read(in, null, "TURTLE");
 		if (tempG.getModel().isEmpty()) {
 			return null;
 		}
 		return tempG;
 	}
 
 	protected AbstractRDFService() {
 		this.supportedVariants = Variant
 				.mediaTypes(
 						MediaType.valueOf(PLAIN),
 						MediaType.valueOf(XML),
 						MediaType.valueOf(TTL_A),
 						MediaType.valueOf(TTL_T),
						MediaType.valueOf(N3),
						MediaType.valueOf(RDF_TRIPLES)
 						).add().build();
 		mediaType2Language.put(MediaType.valueOf(PLAIN), "N-TRIPLE");
 		mediaType2Language.put(MediaType.valueOf(XML), "RDF/XML");
 		mediaType2Language.put(MediaType.valueOf(TTL_A), "TURTLE");
 		mediaType2Language.put(MediaType.valueOf(TTL_T), "TURTLE");
 		mediaType2Language.put(MediaType.valueOf(N3), "N3");
		mediaType2Language.put(MediaType.valueOf(RDF_TRIPLES), "N-TRIPLE");
 	}
 
 	protected Response getResponse(Model model) {
 		Variant selectedVariant = request.selectVariant(supportedVariants);
 		assert selectedVariant != null;
         if (uriInfo.getQueryParameters().containsKey("debug")) {
             return Response.ok(
                     new HTMLOutput(model),
                     MediaType.TEXT_HTML_TYPE).build();
         }
 
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
         if (uriInfo.getQueryParameters().containsKey("debug")) {
             return new HTMLOutput(model);
         }
 		return new RDFOutput(model, selectedVariant.getMediaType());
 
 	}
 
 	protected StreamingOutput getResponseEntity(Grafeo grafeo) {
 		return getResponseEntity(((GrafeoImpl) grafeo).getModel());
 
 	}
 
 	protected static boolean notValid(String uri) {
 		return !urlValidator.isValid(uri);
 	}
 	
 	protected String createUniqueStr() {
 		return new Date().getTime() + "_" + UUID.randomUUID().toString();
 	}
 	
 
 	protected void validateServiceInput(String configUriStr) throws Exception {
 		Grafeo inputGrafeo = new GrafeoImpl();
 		inputGrafeo.load(configUriStr);
 		if (inputGrafeo.isEmpty()) {
 			throw new Exception("config model is empty.");
 		}
 		WebservicePojo wsDesc = this.getWebServicePojo();
 		for (ParameterPojo param : wsDesc.getInputParams()) {
 			if (param.getIsRequired()) {
 				if (! inputGrafeo.containsStatementPattern("?s", "omnom:forParam", param.getId())) {
 					log.severe(configUriStr + " does not contain '?s omnom:forParam " + param.getId());
 					throw new RuntimeException(configUriStr + " does not contain '?s omnom:forParam " + param.getId());
 				}
 			}
 		}
 
 	}
 
     protected URI appendPath(URI uri, String path) {
         String query = uri.getQuery();
         String u = uri.toString();
         log.info("Query: " + query);
         if (query!=null) {
             log.info("URI: " + u);
             u = u.replace("?" + query,"");
         }
         if (!u.endsWith("/") && !path.startsWith("/")) u = u + "/";
         u = u + path;
         if (query!=null) {
             u = u + "?" + query;
         }
         log.info("Result: " + u);
         try {
             return new URI(u);
         } catch (URISyntaxException e) {
             throw new RuntimeException("An exception occurred: " + e, e);
         }
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
 			log.fine("Media type: " + this.mediaType);
 			model.write(output, mediaType2Language.get(this.mediaType));
 		}
 	}
 
 	protected WebserviceConfigPojo resolveWebServiceConfigPojo(String configURI) {
 		// TODO Auto-generated method stub
 		Grafeo g = new GrafeoImpl();
 		g.readFromEndpoint(Config.getString("dm2e.ws.sparql_endpoint_statements"), configURI);
 		try {
 			return g.getObjectMapper().getObject(WebserviceConfigPojo.class, configURI);
 		} catch (Exception e) {
 			log.warning(e.toString());
 		}
 		return null;
 	}
 
 
     protected class HTMLOutput implements StreamingOutput {
         Logger log = Logger.getLogger(getClass().getName());
         Model model;
 
         public HTMLOutput(Model model) {
             this.model = model;
 
         }
 
         @Override
         public void write(OutputStream output) throws IOException,
                 WebApplicationException {
             PrintWriter pw = new PrintWriter(output);
             pw.write("<html><body><table>");
             StmtIterator it = model.listStatements();
             while (it.hasNext()) {
                 Statement st = it.nextStatement();
                 pw.write("<tr><td>");
                     pw.write("<a href=\"");
                         pw.write(st.getSubject().getURI());
                         pw.write("?debug\">");
                         pw.write(st.getSubject().getURI());
                     pw.write("</a>");
                 pw.write("</td><td>");
                 pw.write(st.getPredicate().getURI());
                 pw.write("</td><td>");
                 pw.write(st.getObject().toString());
                 pw.write("</td></tr>");
 
             }
             pw.write("</table></body></html>");
             pw.close();
             output.flush();
         }
     }
 }
