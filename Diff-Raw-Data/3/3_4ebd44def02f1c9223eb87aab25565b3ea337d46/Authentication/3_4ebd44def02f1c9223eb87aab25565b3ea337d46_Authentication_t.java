 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 
 import models.*;
 import views.html.*;
 
 public class Authentication extends Controller {
   
     // -- Authentication
     
     public static class Login {
         
         public String username;
         public String password;
         
         public String validate() {
             if(UserCredentials.authenticate(username, password) == null) {
                 return "Invalid user or password";
             }
             return null;
         }
         
     }
 
     /**
      * Login page.
      */
     public static Result login() {
         return ok(
             login.render(form(Login.class))
         );
     }
     
     /**
      * Handle login form submission.
      */
     public static Result authenticate() {
     
     
         Form<Login> loginForm = form(Login.class);
         
         loginForm = loginForm.bindFromRequest();
         
         if (loginForm.hasErrors())
         {
             return badRequest(login.render(loginForm));
         }
         else
         {
             session("username", loginForm.get().username);
             
             return redirect(
                //routes.Application.index()
                routes.Students.index()
             );
         }
     }
 
     /**
      * Logout and clean the session.
      */
     public static Result logout() {
         session().clear();
         flash("success", "You've been logged out");
         return redirect(
             routes.Application.index()
         );
     }
 }
