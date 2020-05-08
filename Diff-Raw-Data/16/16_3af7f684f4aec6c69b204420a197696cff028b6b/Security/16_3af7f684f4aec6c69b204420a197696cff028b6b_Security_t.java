 package controllers;
 
 import controllers.securesocial.SecureSocial;
 import securesocial.provider.SocialUser;
 
 /**
  * Security class.
  */
 public class Security extends Secure.Security {
 
     /**
      * Return true if the current user has the specified profile?
      *
      * @param profile
      * @return
      */
    public static boolean check(String profile) {
         return AbstractController.hasAdminRight();
     }
 
     /**
      * Retrun the name of the current user (if its logged).
      *
      * @return
      */
    public static String connected() {
         SocialUser user = SecureSocial.getCurrentUser();
         return user.id.id;
     }
 
     /**
      * Return true if the user is connected.
      *
      * @return
      */
    public static boolean isConnected() {
         SocialUser user = SecureSocial.getCurrentUser();
         if(user != null) {
             return true;
         }
         return false;
     }
 
 }
