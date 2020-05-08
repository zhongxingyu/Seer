 package controllers;
 
import play.*;
import play.mvc.*;

import views.html.*;
 
 public class AboutUs extends Controller {
   
   public static Result index() {	  
	  return ok(views.html.aboutus.render(""));
   }
   
 }
