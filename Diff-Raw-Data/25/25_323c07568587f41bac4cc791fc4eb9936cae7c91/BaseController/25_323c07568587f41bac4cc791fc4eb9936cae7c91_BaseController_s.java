 package controllers;
 
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class BaseController extends Controller {
 
 	@Before
	public static void setUserConnected() {
		renderArgs.put("user", Security.connectedUser());
 	}
 
 }
