 package controllers;
 
 import java.util.List;
 
 import models.User;
import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Application extends Controller {
	
	/**
	 * this method creates a user object of the currently logged in user and
	 * makes it accessible for all views and tag views.
	 */
	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			User account = User.find("byNickname", Security.connected()).first();
			renderArgs.put("account", account);
		}
	}
	   
 	public static void index() {
 		Application.showCalendars(Security.connected());
 	}
 
 	public static void showCalendars(String nickname) {
 		User user = User.find("byNickname", nickname).first();
 		List calendars = user.calendars;
 		render(user, calendars);
 	}
 
 }
