 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import models.*;
 import views.html.*;
 
 @Security.Authenticated(Secured.class)
 public class Students extends Controller {
     
     public static Result index()
     {
        UserCredentials uc = UserCredentials.find.where().eq("userName",request().username()).findUnique();
        
        return ok(students.render(uc));
     }
   
 }
