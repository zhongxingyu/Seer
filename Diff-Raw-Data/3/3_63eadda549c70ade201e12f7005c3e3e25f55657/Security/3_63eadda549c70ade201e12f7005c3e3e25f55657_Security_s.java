 package controllers;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 
 import models.account.Account;
 import models.account.User;
 import models.tm.TMUser;
 import play.Play;
 import play.mvc.Util;
 import util.Logger;
 
 /**
  * Custom implementation of the {@link Security} class of Play's security module.
  * This is where the authentication code sits.
  */
 public class Security extends Secure.Security {
 
     static boolean authenticate(String username, String password) {
 
         // fetch the account from the URL, i.e. client.oxiras.com
         Account account = getAccountFromHost();
         if(account == null) {
             error("Trying to login to unknown account");
         }
 
         User a = User.find("byEmailAndActiveAndAccount", username, true, account).first();
         boolean success = a != null && a.connect(password);
         if (success) {
             Logger.info(Logger.LogType.AUTHENTICATION, false, "Authenticated user '%s'", username);
         } else {
             Logger.warn(Logger.LogType.AUTHENTICATION, false, "Unsuccessful authentication attempt by user '%s'", username);
         }
         return success;
     }
 
     private static Account getAccountFromHost() {
         String host = request.host.substring(0, request.host.indexOf(":"));
         String applicationHost = Play.configuration.getProperty("tm.hostname");
         if (host.endsWith(applicationHost)) {
             String account = host.substring(0, host.indexOf(applicationHost));
             if (account.length() > 0) {
                 account = account.substring(0, account.length() - 1); // remove the dot
                 // TODO caching
                 Account a = Account.find("from Account a where a.subdomain = ?", account).<Account>first();
                 if(a == null) {
                     Logger.error(Logger.LogType.AUTHENTICATION, false, "Trying to login into unknown account %s", account);
                 }
                 return a;
             }
         }
         return null;
     }
 
     static void onAuthenticated() {
         Account account = getAccountFromHost();
         TMUser u = TMUser.find("from TMUser u where u.user.email = ? and u.account.id = ?", Security.connected(), account.getId()).first();
 
         // set the session expiration timestamp and compute the active project
         u.sessionExpirationTime = getSessionExpirationTimestamp();
 
         if (u.activeProject == null) {
             u.initializeActiveProject();
         }
 
         u.save();
         // bind the basic session variables
         // we really want to keep the user session as thin as possible as it is sent at each request
         session.put("account", u.user.account.getId());
         if (u.activeProject != null) {
             session.put("activeProject", u.activeProject.getId());
         }
     }
 
     @Util
     public static Timestamp getSessionExpirationTimestamp() {
         // TODO this should be configurable in the Account / Project settings
         Calendar c = Calendar.getInstance();
         c.add(Calendar.HOUR, 1);
         return new Timestamp(c.getTime().getTime());
     }
 
 }
