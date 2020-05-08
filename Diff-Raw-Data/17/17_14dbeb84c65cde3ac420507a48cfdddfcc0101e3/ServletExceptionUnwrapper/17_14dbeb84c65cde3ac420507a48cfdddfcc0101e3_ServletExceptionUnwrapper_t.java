 /*
  * Created on Apr 12, 2005
  */
 package uk.org.ponder.servletutil;
 
 import javax.servlet.ServletException;
 
 import uk.org.ponder.util.ExceptionUnwrapper;
 
 /**
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ServletExceptionUnwrapper implements ExceptionUnwrapper {
  public Throwable unwrapException(Throwable tomunch) {
     if (tomunch instanceof ServletException) {
      Throwable target = ((ServletException) tomunch).getRootCause();
       if (target != null && target != tomunch) {
         return target;
       }
     }
     return null;
   }
 
  public boolean isValid() {
    try {
      Thread.currentThread().getContextClassLoader().loadClass(
          "javax.servlet.ServletException");
    }
    catch (Exception e) {
      return false;
    }
    return true;
  }

 }
