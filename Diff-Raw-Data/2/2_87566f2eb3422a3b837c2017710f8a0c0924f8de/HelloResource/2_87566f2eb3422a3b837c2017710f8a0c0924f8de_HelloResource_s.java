 package hello.jaxrs;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 /**
  * Hello World JAX-RS Resource
  */
 @Path("/")
 public class HelloResource {
 
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayHello() {
 		return sayHello("there");
 	}
 
 	@GET
 	@Path("/{name}")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayHello(@PathParam("name") final String name) {
		return String.format("Hello %s! (%s)", name);
 	}
 }
