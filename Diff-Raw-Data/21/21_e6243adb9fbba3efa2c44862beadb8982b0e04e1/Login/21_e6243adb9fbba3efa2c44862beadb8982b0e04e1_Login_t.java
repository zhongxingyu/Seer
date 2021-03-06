 package controllers;
 
 import java.net.URLEncoder;
 
 import models.Blacklist;
 import models.User;
 import play.Logger;
 import play.libs.OAuth;
 import play.libs.OAuth.ServiceInfo;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http.Header;
import play.mvc.Http.Request;
 import play.mvc.Router;
 import play.mvc.With;
 
 import com.google.appengine.api.NamespaceManager;
 import com.google.appengine.api.utils.SystemProperty;
 import com.google.common.base.Joiner;
 import common.request.Headers;
 
 import dropbox.DropboxOAuthServiceInfoFactory;
 import dropbox.client.DropboxClient;
 import dropbox.client.DropboxClientFactory;
 import dropbox.gson.DbxAccount;
 
 /**
  * Make given controller or controller methods require login.
  * Usage:
  * @With(Login.class)
  * 
  * Based on {@link Secure}.
  *
  * @author mustpax
  */
 @With(ErrorReporter.class)
 public class Login extends Controller {
     private static final String REDIRECT_URL = "url";
 
     private static class SessionKeys {
         static final String TOKEN = "token";
         static final String SECRET = "secret";
         static final String UID = "uid";
        static final String IP = "ip";
     }
     
     @Before(priority=10)
     static void https() {
         if (request.headers.containsKey(Headers.FORWARDED_FOR)) {
             request.remoteAddress = Headers.first(request, Headers.FORWARDED_FOR);
         }
 
         if (request.headers.containsKey(Headers.FORWARDED_PROTO)) {
             Header h = request.headers.get(Headers.FORWARDED_PROTO);
             request.secure = "https".equals(h.value());
         }
     }
 
     @Before
     static void log() {
         Joiner joiner = Joiner.on(":").useForNull("");
         Logger.info(joiner.join(request.remoteAddress,
                                 request.method,
                                 request.secure ? "ssl" : "http",
                                 SystemProperty.applicationVersion.get(),
                                 SystemProperty.environment.get(),
                                 session.get(SessionKeys.UID),
                                 request.url));
     }
 
     @Before(unless={"login", "auth", "logout", "offline", "authCallback", "alt"}, priority=1000)
     static void checkAccess() throws Throwable {
         if (!isLoggedIn()) {
             if ("GET".equals(request.method)) {
                 flash.put(REDIRECT_URL, request.url);
             }
 
             login();
         }
     }
 
     @Before(priority=1)
     static void setNamespace() {
         if (NamespaceManager.get() == null) {
             Logger.info("Updating namespace.");
             String namespace = System.getenv("NAMESPACE");
             if (namespace != null && ! namespace.isEmpty()) {
                 Logger.info("Namespace: %s", namespace);
                 NamespaceManager.set(namespace);
             }
         }
     }
     
     public static void alt() {
         boolean alt = true;
         flash.keep(REDIRECT_URL);
         render("Login/login.html", alt);
     }
     
     public static void login() {
         if (isLoggedIn()) {
             Logger.info("User visited login url, but already logged in.");
             redirectToOriginalURL();
         } else {
             flash.keep(REDIRECT_URL);
             render();
         }
     }
 
     public static boolean isLoggedIn() {
         return getLoggedInUser() != null;
     }
 
     public static void authCallback() throws Exception {
         String token = session.get(SessionKeys.TOKEN);
         String secret = session.get(SessionKeys.SECRET);
         ServiceInfo serviceInfo = DropboxOAuthServiceInfoFactory.create();
         OAuth.Response oauthResponse = OAuth.service(serviceInfo).retrieveAccessToken(token, secret);
         if (oauthResponse.error == null) {
             Logger.info("Succesfully authenticated with Dropbox.");
             User u = upsertUser(oauthResponse.token, oauthResponse.secret);
             session.put(SessionKeys.UID, u.id);
            session.put(SessionKeys.IP, request.remoteAddress);
             session.remove(SessionKeys.TOKEN, SessionKeys.SECRET);
             redirectToOriginalURL();
         } else {
             Logger.error("Error connecting to Dropbox: " + oauthResponse.error);
             session.remove(SessionKeys.TOKEN, SessionKeys.SECRET);
             forbidden("Could not authenticate with Dropbox.");
         }
     }
 
     public static void auth() throws Exception {
         flash.keep(REDIRECT_URL);
         ServiceInfo serviceInfo = DropboxOAuthServiceInfoFactory.create();
         OAuth oauth = OAuth.service(serviceInfo);
         OAuth.Response oauthResponse = oauth.retrieveRequestToken();
         if (oauthResponse.error == null) {
             Logger.info("Redirecting to Dropbox for auth.");
             session.put(SessionKeys.TOKEN, oauthResponse.token);
             session.put(SessionKeys.SECRET, oauthResponse.secret);
             redirect(oauth.redirectUrl(oauthResponse.token) +
                     "&oauth_callback=" +
                     URLEncoder.encode(request.getBase() + "/auth-cb", "UTF-8"));
         } else {
             Logger.error("Error connecting to Dropbox: " + oauthResponse.error);
             error("Error connecting to Dropbox.");
         }
     }
     
     /**
      * Ensure that the Dropbox user authenticated with the given oauth credentials
      * is in the datastore.
      */
     private static User upsertUser(String token, String secret) {
         DropboxClient client = DropboxClientFactory.create(token, secret);
         DbxAccount account = client.getAccount();
         return User.upsert(account, token, secret);
     }
     
     /**
      * @return the currently logged in user, null if no logged in user
      */
     public static User getLoggedInUser() {
         String uid = session.get(SessionKeys.UID);
         if (uid == null) {
             Logger.info("User not logged in: no uid in session.");
             return null;
         }
 
        String ip = session.get(SessionKeys.IP);
        if (ip == null) {
            Logger.info("Session IP address marker missing.");
            session.remove(SessionKeys.UID);
            return null;
        }

        if (! ip.equals(request.remoteAddress)) {
            Logger.warn("Session does not match expected IP. Saved: %s Actual: %s",
                        ip, request.remoteAddress);
            return null;
        }

         try {
             User user = User.findById(Long.valueOf(uid));

             if (user == null) {
                 Logger.warn("Session has uid, but user missing from datastore. Uid: %s", uid);
             }
             return user;
         } catch (NumberFormatException e) {
             Logger.error(e, "Invalidly formatted or missing uid: %s", uid);
             return null;
         }
     }
 
     public static void logout() {
         session.clear();
         login();
     }
     
     static void redirectToOriginalURL() {
         String url = flash.get(REDIRECT_URL);
         if(url == null) {
             Application.index();
         }
         redirect(url);
     }
 }
