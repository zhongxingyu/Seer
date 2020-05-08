 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 
 public class LoginController extends Controller {
   
     public static Result index() {
        return ok(index.render("Вхід до системи..."));
     }
   
 }
