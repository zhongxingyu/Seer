 //package .web.utils;
 
 import javax.servlet.http.HttpSession;
 
 /**
  * Utility class for generating flash messages.
  */
 public final class FlashMessage {
     public static void createInfoMessage(HttpSession session, String messageKey) {
         session.setAttribute("infoMessage", messageKey);
     }
     public static void createErrorMessage(HttpSession session, String messageKey) {
         session.setAttribute("errorMessage", messageKey);
     }
    
    public static void createInfoMessage(HttpSession session, String messageKey, String...args) {
         session.setAttribute("infoMessage", messageKey);
         session.setAttribute("infoMessageArgs", args);
     }
    public static void createErrorMessage(HttpSession session, String messageKey, String...args) {
         session.setAttribute("errorMessage", messageKey);
         session.setAttribute("errorMessageArgs", args);
     }
 }
