 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import models.Role;
 import models.User;
 import play.Logger;
import play.Logger.ALogger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.login;
 import flexjson.JSONSerializer;
 
 public class LoginApplication extends Controller {

	static ALogger logger = play.Logger.of(LoginApplication.class);
     // -- Authentication
     
     public static class Login {
         
         public String email;
         public String password;
         
         public String validate() {
        	
        	logger.debug("email: " + email);
        	logger.debug("password: " + password);
        	
             if(User.authenticate(email, password) == null) {
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
         if(loginForm.hasErrors()) {
             return badRequest(login.render(loginForm));
         } else {
         	String email = loginForm.get().email;
         	
             User user = User.findByEmail(loginForm.get().email);
 
             session("email", user.email);
             
             JSONSerializer s = new JSONSerializer();
             
             
             Set<String> roles = new HashSet<String>();
             for(Role role : user.roles){
             	roles.add(role.code);
             }
 
             session("roles", s.serialize(roles));
             
             session("userId", user.id.toString());
             
             return redirect(
                 routes.Application.index()
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
             routes.LoginApplication.login()
         );
     }
   
     // -- Javascript routing
     
     /*public static Result javascriptRoutes() {
         response().setContentType("text/javascript");
         return ok(
             Routes.javascriptRouter("jsRoutes",
             
                 // Routes for Projects
                 controllers.routes.javascript.Projects.add(), 
                 controllers.routes.javascript.Projects.delete(), 
                 controllers.routes.javascript.Projects.rename(),
                 controllers.routes.javascript.Projects.addGroup(), 
                 controllers.routes.javascript.Projects.deleteGroup(), 
                 controllers.routes.javascript.Projects.renameGroup(),
                 controllers.routes.javascript.Projects.addUser(), 
                 controllers.routes.javascript.Projects.removeUser(), 
                 
                 // Routes for Tasks
                 controllers.routes.javascript.Tasks.addFolder(), 
                 controllers.routes.javascript.Tasks.renameFolder(), 
                 controllers.routes.javascript.Tasks.deleteFolder(), 
                 controllers.routes.javascript.Tasks.index(),
                 controllers.routes.javascript.Tasks.add(), 
                 controllers.routes.javascript.Tasks.update(), 
                 controllers.routes.javascript.Tasks.delete()
                 
             )
         );
     }*/
 
 }
