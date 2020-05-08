 package controllers;
 
 import play.data.validation.Required;
 import play.libs.OpenID;
 import play.libs.OpenID.UserInfo;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Authentication extends Controller {
 	
 	@Before(unless={"login", "authenticate"})
 	public static void checkAuthenticated() {
 	    if(!session.contains("user")) {
 	        login();
 	    }
 	}
 	     
 	public static void login() {
 		if(!session.contains("user")) {
 			render();
 		}else{
 			GlobalTimeline.timeline();
 		}
 	}
 	
 	public static void logout(){
 		session.clear();
 		redirect("/");
 	}
 	
 	public static void authenticate(String user) {
 		if(OpenID.isAuthenticationResponse()) {
 	        UserInfo verifiedUser = OpenID.getVerifiedID();
 	        if(verifiedUser == null) {
 	            flash.error("Oops. Authentication has failed");
 	            login();
 	        } 
 	        session.put("user", verifiedUser.id);
             redirect("/");
 	    } else {
 	        if(!OpenID.id(user).verify()) { // will redirect the user
 	            flash.error("Cannot verify your OpenID");
 	            login();
 	        } 
 	    }
 	}
 }
