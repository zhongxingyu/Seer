 package cz.muni.fi.pv243.sportleaguesystem.rest;
 
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import cz.muni.fi.pv243.sportleaguesystem.entities.User;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.UserService;
 
 @Path("/users")
 @RequestScoped
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 public class UserRESTService {
 
 	@Inject
 	private UserService userService;
 	
 	@GET
 	public List<User> listAllUsers(@QueryParam("name") @DefaultValue("") String name) {
 		List<User> users = null;
 		if (!"".equals(name)) {
 			users = userService.findByName(name);
 		} else {
 			users = userService.getAll();
 		}
 		return users;
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	public User lookupUserById(@PathParam("id") Long id) {
 		User user = userService.getById(id);
 		if (user == null) {
 			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
 					.entity("User with id " + id + " wasn't found")
					.type(MediaType.APPLICATION_JSON)
 					.build());
 		}
 		return user;
 	}
 }
