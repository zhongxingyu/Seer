 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 @Security.Authenticated(Secured.class)
 public class Students extends Controller {
     
     public static Result index()
     {
        return ok("funziona");
     }
   
 }
