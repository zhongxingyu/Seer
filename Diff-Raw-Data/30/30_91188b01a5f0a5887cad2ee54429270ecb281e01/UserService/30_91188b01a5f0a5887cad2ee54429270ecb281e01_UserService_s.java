 package RESTSystemServices;
 
 import java.util.ArrayList;
 
 import systemHibernateEntities.User;
 import systemHibernateManagers.UserManager;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.PathParam;
 
 
 
 @Path("/userService")
 public class UserService {
 	
 	@GET
 	@Path("/userName/{username}")
 	@Produces("application/json")
 	public User getUsersFromName(@PathParam("username")String username){
 		System.out.println("Get Request recieved");
 		UserManager userManager = new UserManager();
 		ArrayList<User> holder = userManager.getUsers(username);
 		System.out.println("User array list populated");

		return holder.get(0);    
 	} 
 }
