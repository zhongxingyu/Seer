 /*
  * NAME: era.foss.util.log.Errors
  */
 
 package era.foss.util.log;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * Static helper methods to convert error and diagnostic types to {@link IStatus} and to show errors to the user.
  * 
  * @author poldi
  */
 public class Log {
     
 
     /**
      * Converts ecore diagnostic result to a {@link IStatus} multi status object.
      * 
      * @param diagnostics errors or warnings
      * @param plugin the plug-in logging the error
      * @param severity {@link IStatus} severity
      * @return status or null on error
      * @since Jul 31, 2009
      */
     public static IStatus convert( Diagnostic diagnostics, String plugin, int severity ) {
         try {
             return new ResourceStatus(
                 severity,
                 plugin,
                 new URI( diagnostics.getLocation() ),
                 diagnostics.getLine(),
                 diagnostics.getColumn(),
                 diagnostics.getMessage(),
                 new Exception() );
         } catch( URISyntaxException e ) {
             return null;
         }
     }
 
     /**
      * Shows a modal error dialog to the user if running the GUI else lgs the errors to the console.
      * 
      * @param status The error to show
      * @since Jul 31, 2009
      */
     public static void show( IStatus status ) {
         StatusManager.getManager().handle( status, StatusManager.BLOCK );
     }
 
     /**
      * Log errors to the error log.
      * 
      * @param status the errors to log
      * @since Jul 31, 2009
      */
     public static void log( IStatus status ) {
         StatusManager.getManager().handle( status, StatusManager.LOG );
     }
 
 } // Error
