 package org.jboss.pressgangccms.restserver;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
 /** A test REST interface */
@Path("/")
 public class REST
 {
	/**
	 * The HAProxy needs to get a 200 return code, or otherwise it thinks that the 
	 * website is down
	 */
	@GET
	@Path("/")
	public Response rootOK()
	{
		return Response.status(200).build();
	}
	
 	@GET
 	@Path("/{param}")
 	public Response printMessage(@PathParam("param") String msg)
 	{
 		String result = "Restful example : " + msg;
 		return Response.status(200).entity(result).build();
 	}
 }
