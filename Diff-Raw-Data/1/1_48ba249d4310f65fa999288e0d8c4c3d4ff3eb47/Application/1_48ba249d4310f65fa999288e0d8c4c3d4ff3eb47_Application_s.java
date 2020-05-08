 package controllers;
 
 import org.joda.time.DateTime;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 import com.feth.play.module.pa.PlayAuthenticate;
 
 public class Application extends Controller {
 
   public static final String FLASH_MESSAGE_KEY = "message";
   public static final String FLASH_ERROR_KEY = "error";
 
   public static Result index() {
     String msg     = "Hello, nicocale";
     String dateStr = new DateTime().toString();
     return ok(index.render(msg, dateStr));
   }
 
   public static Result oAuth(final String provider) {
     Http.Request req = play.mvc.Http.Context.current().request();
    System.out.println(req.queryString());
 
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
 
 }
