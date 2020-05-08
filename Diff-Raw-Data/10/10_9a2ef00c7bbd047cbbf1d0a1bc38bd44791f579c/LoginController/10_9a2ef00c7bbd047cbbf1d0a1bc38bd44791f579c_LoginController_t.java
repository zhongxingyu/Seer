 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 
 public class LoginController extends Controller {
	
	private static String errorLogin = null;
   
     public static Result index() {
        return ok(login.render("Вхід до системи...", LoginController.errorLogin));
    }
    
    public static Result login() {
    	// TODO implement login method with cookies
    	return redirect(routes.LoginController.index());
     }
   
 }
