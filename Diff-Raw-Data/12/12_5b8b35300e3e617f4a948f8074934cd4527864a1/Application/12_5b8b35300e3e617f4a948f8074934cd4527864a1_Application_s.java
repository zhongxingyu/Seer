 package controllers;
 
 import static play.data.Form.form;
 
 import be.objectify.deadbolt.java.actions.Group;
 import be.objectify.deadbolt.java.actions.Restrict;
 import com.feth.play.module.pa.PlayAuthenticate;
 
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
 import com.feth.play.module.pa.user.AuthUser;
 import models.User;
 import play.Logger;
 import play.Play;
 import play.Routes;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import providers.MyUsernamePasswordAuthProvider;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Application extends Controller {
	public static Result index() {
        //return redirect(controllers.clustrino.routes.BasicFlow.index());
		return ok(views.html.index.render());
	}
 
     public static final String FLASH_MESSAGE_KEY = "message";
     public static final String FLASH_ERROR_KEY = "error";
     public static final String USER_ROLE = "user";
 
     public static User getLocalUser(final Http.Session session) {
         final User localUser = User.findByAuthUserIdentity(PlayAuthenticate
                 .getUser(session));
         return localUser;
     }
 
 	public static Result userExists() {
 		return badRequest("User exists.");
 	}
 
 	public static Result userUnverified() {
 		return badRequest("User not yet verified.");
 	}
 
 
     @Restrict(@Group(Application.USER_ROLE))
     public static Result restricted() {
         System.out.println("HI");
         final User localUser = getLocalUser(session());
         return ok(views.html.restricted.render(localUser));
     }
 
     @Restrict(@Group(Application.USER_ROLE))
     public static Result profile() {
         final User localUser = getLocalUser(session());
         return ok(views.html.profile.render(localUser));
     }
 
     public static Result login() {
         return ok(views.html.login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
     }
 
     public static Result doLogin() {
         com.feth.play.module.pa.controllers.Authenticate.noCache(response());
         final Form<MyUsernamePasswordAuthProvider.MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
                 .bindFromRequest();
         if (filledForm.hasErrors()) {
             // User did not fill everything properly
             return badRequest(views.html.login.render(filledForm));
         } else {
             // Everything was filled
             return UsernamePasswordAuthProvider.handleLogin(ctx());
         }
     }
 
     public static Result signup() {
         return ok(views.html.signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
     }
 
     public static Result jsRoutes() {
         return ok(
                 Routes.javascriptRouter("jsRoutes",
                         controllers.routes.javascript.Signup.forgotPassword()))
                 .as("text/javascript");
     }
 
     public static Result doSignup() {
         com.feth.play.module.pa.controllers.Authenticate.noCache(response());
         final Form<MyUsernamePasswordAuthProvider.MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
                 .bindFromRequest();
         if (filledForm.hasErrors()) {
             // User did not fill everything properly
             return badRequest(views.html.signup.render(filledForm));
         } else {
             // Everything was filled
             // do something with your part of the form before handling the user
             // signup
             return MyUsernamePasswordAuthProvider.handleSignup(ctx());
         }
     }
 
     public static String formatTimestamp(final long t) {
         return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
     }
 
 }
