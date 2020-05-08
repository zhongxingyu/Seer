 package rest;
 
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 import business.UserBC;
 
 @Path("/user")
 public class UserRS {
 	@Inject 
 	UserBC bc;
 	
 	@GET
	@Path("/addPoints/{email}/{points}")
 	public int addPoints(@PathParam("email") String email, @PathParam("points") int points) {
 		return bc.addPoints(email, points);
 	}		
 }
