 package controllers;
 
 import models.Contact;
 import models.Producer;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.application.index;
import play.data.Form;
 
 public class Application extends Controller {
 
 	public static Result index() {
		return ok(index.render(new models.Producer(123, new Contact(null, null, null, null))));
 	}
 	
 	public static Result settings() {
 		return ok(views.html.application.settings.render("settings"));
 	}
 
 	public static Result login() {
		Form<Producer> userForm = form(Producer.class);
		return ok(views.html.application.login.render(userForm));
 	}
 	
 	public static Result authenticate() {
 		return TODO;
 	}
 
 	public static Result logout() {
 		return TODO;
 	}
 	
 	public static Result saveContacts() {
 		return TODO;
 	}
 	
 	public static Result search(String term) {
 		return TODO;
 	}
 	
 	public static Result help() {
 		return TODO;
 	}
 
 }
