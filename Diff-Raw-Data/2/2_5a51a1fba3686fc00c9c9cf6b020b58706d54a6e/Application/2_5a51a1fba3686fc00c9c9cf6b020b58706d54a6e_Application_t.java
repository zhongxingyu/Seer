 package controllers;
 
 import org.joda.time.DateTime;
 
 import models.User;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 import play.mvc.Http.Session;
 import providers.MyUsernamePasswordAuthProvider;
 import providers.MyUsernamePasswordAuthProvider.MyLogin;
 import providers.MyUsernamePasswordAuthProvider.MySignup;
 
 import views.html.*;
 import be.objectify.deadbolt.java.actions.Group;
 import be.objectify.deadbolt.java.actions.Restrict;
 
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
 import com.feth.play.module.pa.user.AuthUser;
 
 public class Application extends Controller {
 
   public static final String FLASH_MESSAGE_KEY = "message";
   public static final String FLASH_ERROR_KEY = "error";
   public static final String USER_ROLE = "user";
   
   public static Result index() {
     String msg     = "Hello, nicocale";
     String dateStr = new DateTime().toString();
     return ok(index.render(msg, dateStr));
   }
 
   public static User getLocalUser(final Session session) {
     final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
     final User localUser = User.findByAuthUserIdentity(currentAuthUser);
     return localUser;
   }
 
   public static Result login() {
    String msg = "Login";
     return ok(login.render(msg));
   }
 
   public static Result oAuth(final String provider) {
     Http.Request req = play.mvc.Http.Context.current().request();
 
     if (req.queryString().containsKey("denied")) {
       return oAuthDenied(provider);
     }
 
     return com.feth.play.module.pa.controllers.Authenticate.authenticate(provider);
   }
 
   public static Result oAuthDenied(final String providerKey) {
     com.feth.play.module.pa.controllers.Authenticate.noCache(response());
     flash(FLASH_ERROR_KEY,
           "You need to accept the OAuth connection in order to use this website!");
     return redirect(routes.Application.index());
   }
 
   public static Result signup() {
     return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
   }
 
   public static Result doSignup() {
     com.feth.play.module.pa.controllers.Authenticate.noCache(response());
     final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
       .bindFromRequest();
     if (filledForm.hasErrors()) {
       // User did not fill everything properly
       return badRequest(signup.render(filledForm));
     } else {
       // Everything was filled
       // do something with your part of the form before handling the user
       // signup
       return UsernamePasswordAuthProvider.handleSignup(ctx());
     }
   }
 }
