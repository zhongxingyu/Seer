 package com.desklampstudios.edab;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 @Path("account")
 @Consumes("application/json")
 @Produces("application/json")
 public class AccountResource {	
 
 	@GET @Path("/currentUser")
 	public Response getCurrentUserData(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
 		// NotLoggedIn or InvalidSession sent to client
 		String currentUserId = AccountService.checkLogin(req, resp);
 
 
 		// now we must actually get the user info
 		User user = ofy().load().type(User.class).id(currentUserId).get();
		
		assert user != null;
 
 		if (user.accountState != null && user.accountState == User.AccountState.NEEDS_APPROVAL) {
 			throw new eDABException.NeedsApprovalException("");
 		}
 
 		// serialize into JSON
 
 		ObjectMapper mapper = new ObjectMapper();
 		String json = null;
 		try {
 			json = mapper.writeValueAsString(user);
 		} catch (JsonProcessingException e) {
			throw new eDABException.InternalServerException("Error converting into JSON: " + e.toString());
 		}
 
 		return Response.ok(Utils.JsonPad + json).build();
 	}
 
 	@PUT @Path("/currentUser")
 	public Response setCurrentUserData(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException {
 		// NotLoggedIn or InvalidSession sent to client
 		String currentUserId = AccountService.checkLogin(req, resp);
 		
 		// get provided user object
 		InputStream inputStream = req.getInputStream();
 		ObjectMapper mapper = new ObjectMapper();
 		User providedUser = mapper.readValue(inputStream, User.class);
 		
 		// load user info from db
 		User user = ofy().load().type(User.class).id(currentUserId).get();
 		
 		// Make sure we're not changing fields we shouldn't be
 		// Also ensures you can't change other people's
 		try {
 			assert user.id == providedUser.id;
 			assert user.name == providedUser.name;
 			assert user.real_name == providedUser.real_name;
 			assert user.fcps_id == providedUser.fcps_id;
 		} catch (AssertionError ex) {
 			throw new eDABException.NotAuthorizedException("Illegal field change");
 		}
 		
 		// Save user
 		ofy().save().entity(providedUser).now();
 		
 		return Response.ok().build();
 	}
 
 }
