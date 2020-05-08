 //package .web.utils;
 
 import javax.servlet.http.HttpSession;
 
 /**
  * Utility class for generating flash messages.
  */
 public final class FlashMessage {
    public static void createAlertMessage(HttpSession session, String messageKey) {
        session.setAttribute("alertMessage", messageKey);
    }

     public static void createInfoMessage(HttpSession session, String messageKey) {
         session.setAttribute("infoMessage", messageKey);
     }

     public static void createErrorMessage(HttpSession session, String messageKey) {
         session.setAttribute("errorMessage", messageKey);
     }

    public static void createSuccessMessage(HttpSession session, String messageKey) {
        session.setAttribute("successMessage", messageKey);
    }

    public static void createAlertMessage(HttpSession session, String messageKey, Object... args) {
        session.setAttribute("alertMessage", messageKey);
        session.setAttribute("alertMessageArgs", args);
    }

    public static void createInfoMessage(HttpSession session, String messageKey, Object... args) {
         session.setAttribute("infoMessage", messageKey);
         session.setAttribute("infoMessageArgs", args);
     }

    public static void createErrorMessage(HttpSession session, String messageKey, Object... args) {
         session.setAttribute("errorMessage", messageKey);
         session.setAttribute("errorMessageArgs", args);
     }

    public static void createSuccessMessage(HttpSession session, String messageKey, Object... args) {
        session.setAttribute("successMessage", messageKey);
        session.setAttribute("successMessageArgs", args);
    }
 }
