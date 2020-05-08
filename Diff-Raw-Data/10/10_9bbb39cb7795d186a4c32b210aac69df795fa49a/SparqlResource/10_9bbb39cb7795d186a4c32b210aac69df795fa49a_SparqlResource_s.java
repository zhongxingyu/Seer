 package pl.psnc.dl.wf4ever.sparql;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.Constants;
 import pl.psnc.dl.wf4ever.connection.SemanticMetadataServiceFactory;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 import pl.psnc.dl.wf4ever.dlibra.UserProfile;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataService;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
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
 
 	static {
 		RDFFormat.register(new RDFFormat("XML", "application/xml", Charset
 				.forName("UTF-8"), "xml", false, false));
 		RDFFormat.register(new RDFFormat("JSON", "application/json", Charset
 				.forName("UTF-8"), "json", false, false));
 	}
 
 
 	@GET
	public Response executeSparql(@QueryParam("content")
 	String query)
 		throws DigitalLibraryException, NotFoundException,
 		ClassNotFoundException, IOException, NamingException, SQLException
 	{
 		UserProfile user = (UserProfile) request.getAttribute(Constants.USER);
 		SemanticMetadataService sms = SemanticMetadataServiceFactory
 				.getService(user);
 		String contentType = request.getContentType() != null ? request
 				.getContentType() : "application/rdf+xml";
 		RDFFormat rdfFormat = RDFFormat.forMIMEType(contentType,
 			RDFFormat.RDFXML);
 		ContentDisposition cd = ContentDisposition
 				.type(rdfFormat.getDefaultMIMEType())
 				.fileName("result" + rdfFormat.getDefaultFileExtension())
 				.build();
 
 		try {
 			InputStream is = sms.executeSparql(query, rdfFormat);
 			return Response.ok(is).header("Content-disposition", cd).build();
			//TODO react to bad query syntax
 		}
 		finally {
 			sms.close();
 		}
 
 	}
 }
