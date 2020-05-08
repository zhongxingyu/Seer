 package controllers;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.InvalidValue;
 import models.*;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 
 import javax.persistence.PersistenceException;
 
 
 public class Application extends Controller {
 
     public static Result login() {
         return ok(login.render(form(Login.class)));
     }
 
     public static Result logout() {
         session().clear();
         flash("success", "You've been logged out");
         return redirect(routes.Application.login());
     }
 
     public static Result authenticate() {
 
         Form<Login> loginForm = form(Login.class).bindFromRequest();
         if (loginForm.hasErrors()) {
             return badRequest(login.render(loginForm));
         } else {
             session("email", loginForm.get().email);
             return redirect(routes.Dashboard.index());
         }
     }
     
     public static Result registerForward(){
     	return ok(register.render(form(User.class)));
     }
 
     public static Result registerAction(){
         Form<User> userForm = form(User.class).bindFromRequest();
         if (userForm.hasErrors()) {
             return badRequest(register.render(userForm));
         } else {
             // make form complies with all the validations at the "database" level
             InvalidValue invalid = Ebean.validate(userForm.get());
             if(!invalid.isError()){ // database constraints are valid
                 try{
                     userForm.get().save();
                     flash("success", Messages.get("registration.pending.approval", userForm.get().email));
                     // TODO: send notification to the user by email. If email fails, return form with error
                     return redirect(routes.Application.login());
                 } catch (PersistenceException pe){
                     // unfortunately, we still need to catch
                     // exceptions when writing to database
                     //TODO: how to handle persistence exceptions properly?
                     // Found error when filling in a duplicate email
                     ValidationError ve = new ValidationError("", pe.getMessage(), null);
                     userForm.reject(ve);
                     return badRequest(register.render(userForm));
                 }
             } else {
                 // failed database validations
                 //TODO: PT?
                 userForm.reject(invalid.getMessage());
                 return badRequest(register.render(userForm));
             }
         }
     }
 }
