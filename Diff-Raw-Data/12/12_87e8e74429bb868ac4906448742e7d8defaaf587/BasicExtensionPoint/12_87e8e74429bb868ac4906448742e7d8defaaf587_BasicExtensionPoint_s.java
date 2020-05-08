 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package extension.secure;
 
 import annotations.For;
 import controllers.secure.providers.BasicSecurityProvider;
 
 /**
  *
  * @author Julien Durillon
  */
 @For(BasicSecurityProvider.class)
 public class BasicExtensionPoint extends SecurityExtensionPoint {
 
     public static boolean authenticate(String username, String password) {
        return true;
     }
 
     public static void onAuthenticated() {
     }
 
 }
