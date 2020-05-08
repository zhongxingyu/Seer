 package actions;
 
 import play.Logger;
 import play.mvc.Action;
 import play.mvc.Http;
 import play.mvc.Result;
 
 import java.util.*;
 
 import org.osgeo.mapguide.*;
 
 public class MgCheckSessionAction extends Action<MgCheckSessionAction> {
 
     public static final String MAPGUIDE_SESSION_ID_KEY = "mapguide.sessionid";
 
     public static final String AUTHORIZATION = "authorization";
     public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
     public static final String REALM = "Basic realm=\"mapguide4j\"";
 
     public Result call(Http.Context ctx) throws Throwable {
 
         //NOTE: We're currently following the same rules as the mapagent
         //
         //We expect either a SESSION or credentials in the authentication header
         //Otherwise throw back a 401 with WWW-Authenticate
         //
         //The mgServerError() method will also throw back a 401 with WWW-Authenticate if the
         //matching exceptions are caught during processing
 
         Logger.debug(ctx.request().method() + ": " + ctx.request().uri());
         Logger.debug("Checking MapGuide Session");
 
         String sessionId = null;
         if (ctx.request().method().equals("GET")) {
             Map<String, String[]> params = ctx.request().queryString();
             if (params.get("SESSION") != null)
                 sessionId = params.get("SESSION")[0];
         }
         else if (ctx.request().method().equals("POST")) {
             Map<String, String[]> params = ctx.request().body().asFormUrlEncoded();
             if (params != null && params.get("SESSION") != null)
                 sessionId = params.get("SESSION")[0];
         }
 
         if (sessionId == null)
         {
             Logger.debug("No MapGuide Session ID found. Checking username/password");
 
             String authHeader = ctx.request().getHeader(AUTHORIZATION);
             if (authHeader == null) {
                 Logger.debug("No HTTP authentication header found");
                 //HACK: We don't want to trip the qunit test runner with interactive dialogs
                 String fromTestHarness = ctx.request().getHeader("x-mapguide4j-test-harness");
                 if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                     ctx.response().setHeader(WWW_AUTHENTICATE, REALM);
                 return unauthorized("You must enter a valid login ID and password to access this site");
             }
 
             if (authHeader.length() <= 6) {
                 //HACK: We don't want to trip the qunit test runner with interactive dialogs
                 String fromTestHarness = ctx.request().getHeader("x-mapguide4j-test-harness");
                 if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                     ctx.response().setHeader(WWW_AUTHENTICATE, REALM);
                 return unauthorized("You must enter a valid login ID and password to access this site");
             }
 
             String auth = authHeader.substring(6);
             //Logger.debug("Authentication header: " + auth);
             byte[] decodedAuth = javax.xml.bind.DatatypeConverter.parseBase64Binary(auth);
             String decodedStr = new String(decodedAuth, "UTF-8");
             //Logger.debug("Decoded string: " + decodedStr);
             String[] credString = decodedStr.split(":");
             //Logger.debug("Decoded array: " + credString.length);
             if (credString == null || (credString.length != 1 && credString.length != 2)) {
                 String fromTestHarness = ctx.request().getHeader("x-mapguide4j-test-harness");
                 if (fromTestHarness == null || !fromTestHarness.toUpperCase().equals("TRUE"))
                     ctx.response().setHeader(WWW_AUTHENTICATE, REALM);
                 return unauthorized("You must enter a valid login ID and password to access this site");
             }
         }
         return delegate.call(ctx);
     }
 }
