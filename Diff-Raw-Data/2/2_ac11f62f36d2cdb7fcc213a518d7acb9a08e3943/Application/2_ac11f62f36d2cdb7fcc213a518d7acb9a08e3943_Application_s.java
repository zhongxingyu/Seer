 package controllers;
 
 import models.User;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
 
     static Form<User> userForm = form(User.class);
 
     // -- Authentication
 
     public static class Login {
 
         public String email;
         public String password;
 
         public String validate() {
             if (User.authenticate(email, password) == null) {
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
         Form<Login> loginForm = form(Login.class).bindFromRequest();
         if (loginForm.hasErrors()) {
             return badRequest(login.render(loginForm));
         } else {
             session("email", loginForm.get().email);
             return redirect(
                     routes.Employees.employees()
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
                 routes.Application.login()
         );
     }
 
     public static Result register() {
         //TODO validation
         Form<User> filledForm = userForm.bindFromRequest();
         if (filledForm.hasErrors()) {
             return badRequest(
                     views.html.register.render(filledForm)
             );
         } else {
             User.create(filledForm.get());
             return redirect(routes.Application.login());
         }
     }
  
 }
