 package controllers;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Poll;
 import models.PollInstance;
 import models.SimpleUserAuthBinding;
 import models.User;
 import api.entities.PollInstanceJSON;
 import api.entities.PollJSON;
 import api.helpers.GsonHelper;
 import api.requests.AuthenticateUserRequest;
 import api.requests.CreateUserRequest;
 import api.requests.UpdateUserRequest;
 import api.responses.AuthenticateUserResponse;
 import api.responses.CreateUserResponse;
 import api.responses.EmptyResponse;
import api.responses.ReadUserDetailsResponse;
 import api.responses.ReadUserResponse;
 import api.responses.UpdateUserResponse;
 
 /**
  * Class that manages the responses in the API for Users.
  * @author OpenARMS Service Team
  *
  */
 public class UserController extends APIController {
 	/**
 	 * Method that authenticates the user.
 	 * It generates new secret for the user.
 	 * Used only when user is logging in.
 	 */
 	public static void authenticate() {
 		try {
 			// Takes the UserJSON from the http body
 			AuthenticateUserRequest req = GsonHelper.fromJson(request.body, AuthenticateUserRequest.class);
 			User user = User.find("byEmail", req.user.email).first();
 			if (user == null) {
 				throw new NotFoundException("No user with this email, found on the system.");
 			} else if (!(user.userAuth instanceof SimpleUserAuthBinding)) {
 				throw new NotFoundException("This user has no support for the choosen backend.");
 			} else {
 				user = SimpleAuthBackend.authenticate(req);
 				if (user != null) {
 				    //Creates the UserJSON Response.
 					AuthenticateUserResponse response = new AuthenticateUserResponse(user.toJson());
 					renderJSON(response.toJson());
 				} else {
 					throw new UnauthorizedException();
 				}
 			}
 		} catch (Exception e) {
 			//renderText(e.getMessage());
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that authorizes the user.
 	 * It keeps user logged in the system.
 	 * Used every time user sends any request.
 	 */
 	public static boolean authorize() {
 		User user = AuthBackend.getCurrentUser();
 		return (user != null);
 	}
 	
 	/**
 	 * Method that saves a new User in the DataBase.
 	 */
 	public static void create() {
 		try {	
 	    	// Takes the UserJSON and creates a new User object with this UserJSON.
 	        CreateUserRequest req = GsonHelper.fromJson(request.body, CreateUserRequest.class);
 	        User user = User.fromJson(req.user);
 	     	user.userAuth.save();
 	        user.save();
 	        user.userAuth.user = user;
 	        user.userAuth.save();
 	        
 	        // if (user.userAuth instanceof SimpleUserAuthBinding)
 	        // 	((SimpleUserAuthBinding)user.userAuth).save();
 	        //user.save();
 	           
 	        //Creates the UserJSON Response.
 	        CreateUserResponse response2 = new CreateUserResponse(user.toJson());
 	    	String jsonResponse = GsonHelper.toJson(response2);
 	    	System.out.println(jsonResponse);
 	    	renderJSON(jsonResponse);
 		} catch (Exception e) {
 			e.printStackTrace();
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that gets a User from the DataBase.
 	 */
 	public static void retrieve () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User user = User.find("byID", userid).first();
 	
 			if (user == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || user.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Creates the UserJSON Response.
 			ReadUserResponse response = new ReadUserResponse(user.toJson());
 			String jsonResponse = GsonHelper.toJson(response);
 			renderJSON(jsonResponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that edits a User already existing in the DB.
 	 */
 	public static void edit () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User originalUser = User.find("byID", userid).first();
 			
 			if (originalUser == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || originalUser.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Takes the edited UserJSON and creates a new User object with this UserJSON.
 			UpdateUserRequest req = GsonHelper.fromJson(request.body, UpdateUserRequest.class);
             User editedUser = User.fromJson(req.user);
             
             //Changes the old fields for the new ones.
             if (editedUser.name != null)
             	originalUser.name = editedUser.name;
             if (editedUser.email != null)
             	originalUser.email = editedUser.email;
             if (editedUser.secret != null)
             	originalUser.secret = editedUser.secret;
             if (editedUser.userAuth != null) {
             	// Compares originalAuth with editedAuth
             	if (editedUser.userAuth.getClass().toString().equals(originalUser.userAuth.getClass().toString())) {
             		// Check authentication method
             		if (originalUser.userAuth instanceof SimpleUserAuthBinding) {
             			Long idAuth = ((SimpleUserAuthBinding)originalUser.userAuth).id;
             			SimpleUserAuthBinding originalAuth = (SimpleUserAuthBinding)SimpleUserAuthBinding.find("byID", idAuth).fetch().get(0);
             			originalAuth.password = ((SimpleUserAuthBinding)editedUser.userAuth).password;
             			originalAuth.save();
             		}
             	}
             }
             originalUser.save();
             
             
             //Creates the PollJSON Response.
             UpdateUserResponse response = new UpdateUserResponse(originalUser.toJson());
         	String jsonResponse = GsonHelper.toJson(response);
         	renderJSON(jsonResponse);
             
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that deletes a User existing in the DataBase.
 	 */
 	public static void delete () {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User user = User.find("byID", userid).first();
 			
 			if (user == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || user.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Deletes the Authentication from the DataBase.
 			if (user.userAuth instanceof SimpleUserAuthBinding) {
     			Long idAuth = ((SimpleUserAuthBinding)user.userAuth).id;
     			SimpleUserAuthBinding auth = (SimpleUserAuthBinding)SimpleUserAuthBinding.find("byID", idAuth).fetch().get(0);
     			auth.delete();
     		}
 			//Deletes the User from the DataBase and creates an empty UserJSON for the response.
 			user.delete();
 
 			renderJSON(new EmptyResponse().toJson());
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that gets a User from the DataBase.
 	 */
 	public static void details() {
 		try {
 			String userid = params.get("id");
 	
 			//Takes the User from the DataBase.
 			User user = User.find("byID", userid).first();
 	
 			if (user == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || user.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			List<Poll> polllist = Poll.find("byAdmin.id", u.id).fetch();
 			List<PollJSON> polljsonlist = new LinkedList<PollJSON>();
 			
 			for(Poll p: polllist) {
 				PollJSON tmpp = p.toJson();
 				tmpp.pollinstances = new LinkedList<PollInstanceJSON>();
 				for(PollInstance pi: p.instances) {
 					tmpp.pollinstances.add(pi.toJson());
 				}
 				polljsonlist.add(tmpp);
 			}
 			
 			
 			//Creates the UserJSON Response.
 			ReadUserDetailsResponse response = new ReadUserDetailsResponse(user.toJson(), polljsonlist);
 			String jsonResponse = GsonHelper.toJson(response);
 			renderJSON(jsonResponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 }
