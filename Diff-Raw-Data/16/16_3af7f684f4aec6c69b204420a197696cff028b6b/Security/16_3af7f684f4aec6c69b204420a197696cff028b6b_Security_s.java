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
    static boolean check(String profile) {
         return AbstractController.hasAdminRight();
     }
 
     /**
      * Retrun the name of the current user (if its logged).
      *
      * @return
      */
    static String connected() {
         SocialUser user = SecureSocial.getCurrentUser();
         return user.id.id;
     }
 
     /**
      * Return true if the user is connected.
      *
      * @return
      */
    static boolean isConnected() {
         SocialUser user = SecureSocial.getCurrentUser();
         if(user != null) {
             return true;
         }
         return false;
     }
 
 }
