 package controllers;
 
 import models.User;
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
   
   public static Result index() {
      User user = new User("this@gmail.com","password","name");

      return ok(index.render(user));
 
   }
   
 }
