 package org.infobip.mpayments.help.repo.rest;
 
 import java.net.URI;
 
 import javax.ejb.Local;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 @Local
 @Path("/")
 public interface RestHelpRepo {
 
 	@GET
 	@Path("/test")
 	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
 	public Response test(@Context HttpServletRequest request);
 
 	@GET
 	@Path("/sayHello")
 	@Produces(MediaType.TEXT_HTML)
 	public String sayHtmlHello();
 
 	@GET
 	@Path("/document")
 	@Produces(MediaType.TEXT_HTML)
 	public String getDocument(@QueryParam("language") String language, @QueryParam("test") String test);
 
 	@GET
 	@Path("/testJSON")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getJSON();
 
 	@GET
 	@Path("/getOneLevelJSON/{nodePath:.*}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getOneLevelJSON(@PathParam("nodePath") String nodePath);
 
 	@GET
	@Path("/getLinksJSON/{parent:.*}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getLinksJSON(@PathParam("parent") String parent);
 
 	@GET
	@Path("/getLinksJSON/{parent:.*}/children")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getChildrenLinksJSON(@PathParam("parent") String parent);
 }
