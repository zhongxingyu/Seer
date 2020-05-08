 package controllers;
 
 import play.mvc.Result;
import views.html.*;
 
 public class Login extends RuPinController {
 
     public static Result blank() {
         return ok(login.render());
     }
 }
