 package de.devsurf.echo.frameworks.rs.examples.welcome;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
import de.devsurf.echo.frameworks.rs.api.Publishable.AbstractEndpoint;
import de.devsurf.echo.frameworks.rs.api.User;
import de.devsurf.echo.frameworks.rs.system.api.CapabilitiesManager;
 
 @Path("welcome")
 public class WelcomeEndpoint extends AbstractEndpoint {
 
 	@Context
 	private CapabilitiesManager capabilities;
 
 	@Override
 	public String description() {
 		return "example welcome resource";
 	}
 	
 	@Override
 	@GET
 	@Consumes(MediaType.WILDCARD)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response get() {
 		return Response.ok("{ \"message\" : \"Welcome to "
 				+ getClass().getSimpleName()
 				+ ".\", \"uri\" : \""
 				+ (uriInfo == null ? "no uri informations" : uriInfo
 						.getAbsolutePath()) + "\"}").build();
 	}
 
 	@Path("enabled")
 	@GET
 	public String world() {
 		return Boolean.toString(capabilities.can(new User() {
 		}).use(this.getClass()));
 	}
 }
