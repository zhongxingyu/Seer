 package controllers;
 
 import com.codeborne.security.AuthenticationException;
 import com.codeborne.security.mobileid.MobileIDAuthenticator;
 import com.codeborne.security.mobileid.MobileIDSession;
 import play.Logger;
 import play.Play;
 import play.data.validation.Validation;
 import play.mvc.Catch;
 import play.mvc.Controller;
 
 import java.io.File;
 
 public class Auth extends Controller {
     static {
         // need to specify a custom truststore with SK root cert in it, otherwise https requests won't work
         Logger.info("Read certificates from " + new File("conf", "keystore.jks").getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStore", "conf/keystore.jks");
     }
 
     static MobileIDAuthenticator mid = new MobileIDAuthenticator(Play.configuration.getProperty("digidoc.url"));
 
     @Catch(Throwable.class)
     public static void handleMidFailure(AuthenticationException e) {
         Logger.error(e, e.getMessage());
         Validation.addError("phone", e.getMessage());
         Validation.keep();
         form();
     }
 
     public static void form() {
         render();
     }
 
     public static void startLogin(String phone) {
         MobileIDSession midSession = mid.startLogin(phone);
         session.put("mid-session", midSession);
 
         String challenge = midSession.challenge;
         render(challenge);
     }
 
     public static void completeLogin() {
         MobileIDSession midSession = MobileIDSession.fromString(session.get("mid-session"));
         mid.waitForLogin(midSession);
         session.put("userName", midSession.firstName + " " + midSession.lastName);
         session.put("personalCode", midSession.personalCode);
         session.remove("mid-session");
         Dashboard.index();
     }
 
     public static void logout() {
         session.remove("userName");
         session.remove("personalCode");
         Dashboard.index();
     }
 }
