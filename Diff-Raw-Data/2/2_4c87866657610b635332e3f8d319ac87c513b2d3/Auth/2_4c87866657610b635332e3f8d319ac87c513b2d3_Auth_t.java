 package controllers.security;
 
 import controllers.Dashboard;
 import controllers.Users;
 import controllers.abstracts.UtilController;
 import controllers.helper.AuthHelper;
 import models.user.Profile;
 import models.user.User;
 import org.apache.commons.lang3.ArrayUtils;
 import play.Logger;
 import play.db.jpa.Model;
 import play.i18n.Messages;
 import play.mvc.Before;
 import play.mvc.Util;
 import play.mvc.With;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 @With(AuthHelper.class)
 public class Auth extends UtilController {
 
     private static final String CURRENT_USER = "currentUser";
 
     @Before(priority = 0)
     public static void checkUser() {
 
         Logger.debug("Check if user is logged");
 
         if (!isLogged()) {
             Logger.debug("User is not logged");
             return;
         }
 
         Long id;
 
         try {
             id = Long.valueOf(session.get(CURRENT_USER));
         } catch (NumberFormatException e) {
             Logger.error("Invalid session id : %s", session.get(CURRENT_USER));
             logoutUser();
             return;
         }
 
         User user = User.findById(id);
 
         if (null == user) {
             Logger.error("User don't exists with session id : %s ", id);
             logoutUser();
         }
 
         Logger.debug("Logged user : %s", user);
         renderArgs.put(CURRENT_USER, user);
     }
 
     @Before(priority = 1)
     public static void checkAccess() {
 
         Logger.debug("Check access");
 
         // check public access first
 
         Logger.debug("Check public access");
         boolean isPublic = false;
 
         PublicAccess publicAccess = getActionAnnotation(PublicAccess.class);
         if (null != publicAccess) {
             isPublic = true;
             checkAccess(publicAccess);
         }
 
         publicAccess = getControllerInheritedAnnotation(PublicAccess.class);
         if (null != publicAccess) {
             isPublic = true;
             checkAccess(publicAccess);
         }
 
         if (isPublic) {
             Logger.debug("Access allowed for both guest and logged users");
             return;
         }
 
         // check logged access next
 
         Logger.debug("Check logged access");
         boolean isAccessDefined = false;
 
         if (!isLogged()) {
             Logger.debug("Access denied for guest users");
             Users.login();
         }
 
         Logger.debug("Check profile access");
 
         LoggedAccess loggedAccess = getActionAnnotation(LoggedAccess.class);
         if (loggedAccess != null) {
             isAccessDefined = true;
             checkAccess(loggedAccess);
         }
 
         loggedAccess = getControllerInheritedAnnotation(LoggedAccess.class);
         if (loggedAccess != null) {
             isAccessDefined = true;
             checkAccess(loggedAccess);
         }
 
         // no access defined on the action
         if (!isAccessDefined) {
             denyAccess();
         }
     }
 
     @Util
     public static void checkAccess(PublicAccess publicAccess) {
 
         if (publicAccess.only() && isLogged()) {
             Logger.debug("Public access allowed only, redirect to dashboard");
             Dashboard.index();
         }
     }
 
     @Util
     public static void checkAccess(LoggedAccess loggedAccess) {
 
         Profile profile = getCurrentUser().profile;
 
         Logger.debug("Check access for current user with profile %s", profile);
 
         if (ArrayUtils.isEmpty(loggedAccess.value())) {
             Logger.debug("Required logged user only");
             return;
         }
 
         for (Profile requiredProfile : loggedAccess.value()) {
 
             Logger.debug("Required profile : " + requiredProfile);
 
             if (profile.equals(requiredProfile)) {
                 Logger.debug("User is allowed");
                 return;
             }
         }
 
         denyAccess();
     }
 
     @Util
     public static void denyAccess() {
 
         Logger.debug("Access denied");
 
        String profile = (isLogged() ? getCurrentUser().profile.getLabel() : Messages.get("profile.guest"));
         flashError("auth.logged.denied_for_profile", profile);
 
         Dashboard.index();
     }
 
     @Util
     public static User getCurrentUser() {
         return renderArgs.get(Auth.CURRENT_USER, User.class);
     }
 
     @Util
     public static boolean isLogged() {
         return session.contains(CURRENT_USER);
     }
 
     @Util
     public static void logoutUser() {
 
         Logger.debug("Logout current user : %s", getCurrentUser());
 
         session.remove(CURRENT_USER);
         renderArgs.put(CURRENT_USER, null);
     }
 
     @Util
     public static void loginUser(User user) {
         session.put(CURRENT_USER, user.id);
     }
 }
