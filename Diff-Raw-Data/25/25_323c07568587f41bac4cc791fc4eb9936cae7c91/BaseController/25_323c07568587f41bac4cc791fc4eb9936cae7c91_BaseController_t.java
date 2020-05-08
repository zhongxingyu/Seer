 package controllers;
 
import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class BaseController extends Controller {
 
 	@Before
	public static void setUserConnected() throws Throwable {
		User user = Security.connectedUser();
		if (user == null && Security.connected() != null) {
			Secure.logout();
		}
		renderArgs.put("user", user);
 	}
 
 }
