 package controllers;
 
 import models.Profile;
 import models.User;
 import play.Logger;
 import play.Play;
 import play.libs.WS;
 import play.libs.WS.WSRequest;
 import play.mvc.Router;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 /**
  * Checks user login pass
  * 
  * @author anthonjp
  *
  */
 public class Security extends controllers.Secure.Security {
 
 	
 	static boolean authenticate(String username, String password) {
 		
 		//return User.connect(username, password) != null;
 		
 		// authenticate with rollcall
 		String rollcallUrl = Play.configuration.getProperty("sail.rollcall.url");
 		
 		WSRequest req = WS.url(rollcallUrl+"/login.json");
 		req.setParameter("session[login]", username);
 		req.setParameter("session[password]", password);
 		WS.HttpResponse sessionRes = req.post();
 		
 		JsonElement sessionJson = sessionRes.getJson();
 		
 		if (sessionRes.getStatus() == 201) { // session created successfully
 			JsonObject session = sessionJson.getAsJsonObject().getAsJsonObject("session");
 			JsonObject account = session.getAsJsonObject("account");
 			
 			String token = session.get("token").getAsString();
 			String encryptedPassword = account.get("encrypted_password").getAsString();
 			
 			// now check if we already have this user in the local database
 			if (User.findUserByUsername(username) == null) { // user doesn't yet exist
 				new User(username, encryptedPassword).save();
 				Logger.info("User '"+username+"' created and authenticated.");
 				return true;
 			} else { // user exists
 				Logger.info("User '"+username+"' successfuly authenticated.");
 				return true;
 			}
 			
 		} else { // some error during session creation
 			Logger.warn("Authentication for '"+username+"' failed because: "+sessionJson.toString());
 			flash.put("error", "Rollcall authentication failed: "+sessionJson.toString());
 			return false;
 		}
 	}
 	
 	static void onAuthenticated() {
 		
 //		  String username = Security.connected();	    
 //		    User user = User.findUserByUsername(username);
 //		    Profile profile = Profile.findProfileByUser(user);
 //
 //		    if( profile == null ) {
 //		    	profile = new Profile(user, null, null).save();
 //		    }
 //
 //		    //profile.isComplete = false;
 //
 //		    session.put("profile.id", profile.id);
 //
 //		    if( profile.isComplete ) {
 //		    	Application.index();
 //		    } else {
 //		    	Profiles.form();
 //		    }
 		
 	    String username = Security.connected();	    
 	    User user = User.findUserByUsername(username);
 	    Profile profile = Profile.findProfileByUser(user);
 	    
 	    if( profile == null ) {
 	    	profile = new Profile(user, null, null).save();
 	    }
 	    
 	    session.put("profile.id", profile.id);
 		
 	    // figure out what kind of user this is based on data in Rollcall
 	    String rollcallUrl = Play.configuration.getProperty("sail.rollcall.url");
 		WS.HttpResponse userRes = WS.url(rollcallUrl+"/users/"+username+".json").get();
 		
 		String userKind;
 		
 		if (userRes.getStatus() == 200) {
 			userKind = userRes.getJson().getAsJsonObject().getAsJsonObject("user").get("kind").getAsString();
 			
 			if (userKind.equals("Instructor")) {
 				// redirect to the teacher dashboard
 				redirect(Router.reverse("TeacherDashboard.show").url);
 			} else if (userKind.equals("Student")) {
 			    if( profile.isComplete == true ) {
 			    	Application.index();
 			    } else {
 			    	Profiles.form();
 			    }
 			} else if (userKind.equals("Admin")) {
 			    if( profile.isComplete ) {
 			    	Application.index();
 			    } else {
 			    	Profiles.form();
 			    }
 			} else {
 				throw new RuntimeException("This kind of user cannot log in to this service: '"+userKind+"'");
 			}
 			
 		} else if (userRes.getStatus() == 404) {
 			throw new RuntimeException("'"+username+"' is not a user (maybe a group account?)");
 		} else {
 			throw new RuntimeException("Couldn't retrieve User data for '"+username+"' from "+rollcallUrl+". Response status: "+userRes.getStatus());
 		}
 	   
 	}
 }
