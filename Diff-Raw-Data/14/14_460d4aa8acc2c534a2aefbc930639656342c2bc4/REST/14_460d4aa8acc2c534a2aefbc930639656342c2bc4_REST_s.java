 package org.jboss.pressgangccms.restserver;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
 /** A test REST interface */
@Path("/message")
 public class REST
 {
 	@GET
 	@Path("/{param}")
 	public Response printMessage(@PathParam("param") String msg)
 	{
 		String result = "Restful example : " + msg;
 		return Response.status(200).entity(result).build();
 	}
 }
