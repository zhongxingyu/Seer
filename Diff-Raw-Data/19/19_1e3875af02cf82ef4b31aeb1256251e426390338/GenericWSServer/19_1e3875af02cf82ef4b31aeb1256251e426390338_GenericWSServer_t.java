 package org.bioinfo.gcsa.ws;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ResourceBundle;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
import org.bioinfo.commons.Config;
 import org.bioinfo.commons.log.Logger;
 
 
 @Path("/")
 @Produces("text/plain")
 public class GenericWSServer {
 
 	
 	protected UriInfo uriInfo;
 	protected Logger logger;
 	protected ResourceBundle properties;
	protected Config config;
 	MultivaluedMap<String, String> params;
 	
 	public GenericWSServer(@Context UriInfo uriInfo) throws IOException {
 		this.uriInfo = uriInfo;
 		this.params = this.uriInfo.getQueryParameters();
 		
 		logger = new Logger();
 		logger.setLevel(Logger.INFO_LEVEL);
 		
 		properties = ResourceBundle.getBundle("org.bioinfo.gcs.ws.application");
		config = new Config(properties);
 		
 		File dqsDir = new File(properties.getString("DQS.PATH"));
 		if(dqsDir.exists()){
 			File accountsDir = new File(properties.getString("ACCOUNTS.PATH"));
 			if(!accountsDir.exists()){
 				accountsDir.mkdir();
 			}
 		}
 	}
 	
 	@GET
 	@Path("/echo/{message}")
 	public Response echoGet(@PathParam("message") String message) {
 		return createOkResponse(message);
 	}
 	
 	
 	protected Response createErrorResponse(Object o) {
 		String objMsg = o.toString();
 		if(objMsg.startsWith("ERROR:")) {
 			return Response.ok("" + o).header("Access-Control-Allow-Origin", "*").build();			
 		}else {
 			return Response.ok("ERROR: " + o).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 	
 	protected Response createOkResponse(Object o){		
 		return Response.ok(o).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	protected Response createOkResponse(Object o1, MediaType o2){
 		return Response.ok(o1, o2).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	protected Response createOkResponse(Object o1, MediaType o2, String fileName){
 		return Response.ok(o1, o2).header("content-disposition","attachment; filename ="+fileName).header("Access-Control-Allow-Origin", "*").build();
 	}
 }
