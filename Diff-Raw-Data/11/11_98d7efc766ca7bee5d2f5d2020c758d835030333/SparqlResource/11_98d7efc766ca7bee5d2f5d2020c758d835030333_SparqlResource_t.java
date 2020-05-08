 package pl.psnc.dl.wf4ever.sparql;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 import pl.psnc.dl.wf4ever.sms.QueryResult;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 
 import com.sun.jersey.core.header.ContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 
 /**
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path(("sparql/"))
 public class SparqlResource
 {
 
 	@Context
 	HttpServletRequest request;
 
 	@Context
 	UriInfo uriInfo;
 
 
 	@GET
 	@Produces({ "application/sparql-results+xml", "application/xml", "text/xml"})
 	public Response executeSparqlGetXml(@QueryParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, SemanticMetadataService.SPARQL_XML);
 	}
 
 
 	@GET
 	@Produces({ "application/sparql-results+json", "application/json"})
 	public Response executeSparqlGetJson(@QueryParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, SemanticMetadataService.SPARQL_JSON);
 	}
 
 
 	@GET
 	@Produces("application/rdf+xml")
 	public Response executeSparqlGetRdfXml(@QueryParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, RDFFormat.RDFXML);
 	}
 
 
 	@GET
 	@Produces({ "application/x-turtle", "text/turtle"})
 	public Response executeSparqlGetTurtle(@QueryParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, RDFFormat.TURTLE);
 	}
 
 
 	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({ "application/sparql-results+xml", "application/xml", "text/xml"})
 	public Response executeSparqlPostXml(@FormDataParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, SemanticMetadataService.SPARQL_XML);
 	}
 
 
 	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({ "application/sparql-results+json", "application/json"})
 	public Response executeSparqlPostJson(@FormDataParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, SemanticMetadataService.SPARQL_JSON);
 	}
 
 
 	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces("application/rdf+xml")
 	public Response executeSparqlPostRdfXml(@FormDataParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, RDFFormat.RDFXML);
 	}
 
 
 	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})
 	@Produces({ "application/x-turtle", "text/turtle"})
 	public Response executeSparqlPostTurtle(@FormDataParam("query")
 	String query)
 		throws ClassNotFoundException, DigitalLibraryException, NotFoundException, IOException, NamingException,
 		SQLException
 	{
 		return executeSparql(query, RDFFormat.TURTLE);
 	}
 
 
 	private Response executeSparql(String query, RDFFormat inFormat)
 		throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
 		SQLException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		SemanticMetadataService sms = SemanticMetadataServiceFactory.getService(user);
 
 		try {
 			QueryResult queryResult = sms.executeSparql(query, inFormat);
 			RDFFormat outFormat = queryResult.getFormat();
 			ContentDisposition cd = ContentDisposition.type(outFormat.getDefaultMIMEType())
 					.fileName("result." + outFormat.getDefaultFileExtension()).build();
 			return Response.ok(queryResult.getInputStream()).header("Content-disposition", cd).build();
 		}
 		catch (NullPointerException | IllegalArgumentException e) {
 			return Response.status(Status.BAD_REQUEST).type("text/plain").entity(e.getMessage()).build();
 		}
 		finally {
 			sms.close();
 		}
 
 	}
 }
