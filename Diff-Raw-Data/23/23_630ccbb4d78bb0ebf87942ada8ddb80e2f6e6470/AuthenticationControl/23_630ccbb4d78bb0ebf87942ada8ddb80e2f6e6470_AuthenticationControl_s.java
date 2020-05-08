 package elfville.server.controller;
 
 import elfville.protocol.Response.Status;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.Response;
 import elfville.protocol.SignUpRequest;
 import elfville.protocol.utils.Converter;
 import elfville.server.CurrentUserProfile;
 import elfville.server.model.*;
 
 /* 
  * Controls sign in, sign up
  */
 public class AuthenticationControl extends Controller {
 
 	public static Response signIn(SignInRequest r,
 			CurrentUserProfile currentUser) { 
 		
 		Response resp= new Response(Status.FAILURE);
 		User user = database.userDB.findByUsername(r.getUsername());
 		
 		if (user == null) {
 			return resp;
 		}
 		
 		//TODO: need to check if user is already logged in.  flag in database?
 		
 		//TODO:need pepper/salt here
 		if(!r.getPassword().equals(user.getPassword())){
 			return resp;
 		}
 		
 		//TODO: check last login against value stored in database
 		//if(user.getLastLogin().before(r.)
 		
 		currentUser.setSharedKey(r.getSharedKey());
 		currentUser.setNonce(r.getNonce());
 		resp= new Response(Status.SUCCESS);
 		currentUser.setCurrentUserId(user.getModelID());
 		return resp;
 	}
 
 	public static Response signUp(SignUpRequest r,
 			CurrentUserProfile currentUser) {
 		
		Response resp= new Response(Status.FAILURE);
 		User user = database.userDB.findByUsername(r.getUsername());
		
		if (user == null) {
			return resp;
 		}
 		
		//check to see if user already exists
		
		currentUser.setSharedKey(r.getSharedKey());
		currentUser.setNonce(r.getNonce());
		currentUser.setCurrentUserId(user.getModelID());
		
 		
 		Elf elf = new Elf(r.getUsername(), r.description);
 		elf.save();
 		user = new User(elf, r.getUsername());
 		// user.setPassword("lolskates"); //TODO: password
 		
 		// sign the user in
 		currentUser.setCurrentUserId(user.getModelID());
 		
 		user.save();
		resp= new Response(Status.SUCCESS);
 		return resp;
 	}
 
 }
