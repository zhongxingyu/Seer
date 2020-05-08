 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
   
     public static Result index() {
         return ok(index.render());
     }
     
     public static Result user() {
         return ok(user.render());
     }
     
     public static Result signIn() {
         return ok(signIn.render());
     }
 
     public static Result about(){
     	return ok(about.render());
     }
     
   
 }
