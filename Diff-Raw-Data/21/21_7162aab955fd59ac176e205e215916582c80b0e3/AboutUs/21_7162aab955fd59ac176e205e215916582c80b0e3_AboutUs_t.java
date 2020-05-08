 package controllers;
 
import play.mvc.Controller;
import play.mvc.Result;
import views.html.aboutus;
 
 public class AboutUs extends Controller {
   
   public static Result index() {	  
	  return ok(aboutus.render());
   }
   
 }
