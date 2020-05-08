 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.validation.Constraints.*;
 import play.data.*;
 
 import static play.data.Form.*;
 
 import views.html.*;
 
 import models.*;
 
 public class Application extends Controller {
   
     public static Result index() {
         return ok(index.render("Home"));
     }
     
     public static class Login {
         
         public String email;
         public String password;
         
         public String validate() {
             if(User.authenticate(email, password) == null) {
                 return "Invalid user or password";
             }
             return null;
         }
         
     }
 
     public static class Register {
         @Required
         public String email;
         @Required
         public String name;
         @Required
         public String password;
     }
 
     /**
      * Login page.
      */
     public static Result login() {
 
         // return ok(
         //     login.render(form(Login.class))
         // );
         return new Results.Todo();
     }
 
     public static Result registration() {
         return ok(
             views.html.register.render(form(Register.class))
         );
     }
 
     public static Result register() {
         Form<Register> registrationForm = form(Register.class);
         Form<Register> filledForm = registrationForm.bindFromRequest();
         if(filledForm.hasErrors()) {
             return badRequest(register.render(filledForm));
         } else {
             User newUser = new User(filledForm.get().email, 
                                     filledForm.get().name, 
                                     filledForm.get().password);
             newUser.save();
             session("email", filledForm.get().email);
             return redirect( routes.Application.index() );
         }        
     }
     
     /**
      * Handle login form submission.
      */
     public static Result authenticate() {
        // Form<Login> loginForm = form(Login.class).bindFromRequest();
        // if(loginForm.hasErrors()) {
        //     return badRequest(login.render(loginForm));
        // } else {
        //     session("email", loginForm.get().email);
        //     return redirect(
        //         routes.Application.index()
        //     );
        // }
        return new Results.Todo();
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
     
     public static Result about() {
         return ok(about.render("About"));
     }
 
     public static Result contact() {
         return ok(contact.render("Contact"));
     }
   
 }
