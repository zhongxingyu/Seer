 package controllers;
 
 import java.util.List;
 
 import models.User;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Application extends Controller {
 
 	public static void index() {
		Application.showCalendars(Security.connected());
 	}
 
 	public static void showCalendars(String nickname) {
 		User user = User.find("byNickname", nickname).first();
 		List calendars = user.calendars;
 		render(user, calendars);
 	}
 
 }
