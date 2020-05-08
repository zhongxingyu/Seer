 package controllers;
 
 import play.mvc.Result;
import views.html.login;
 
 public class Login extends RuPinController {
 
     public static Result blank() {
         return ok(login.render());
     }
 }
