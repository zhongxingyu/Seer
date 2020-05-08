 package de.jpaw.enums;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import de.jpaw.util.ApplicationException;

 
 /**
  * The EnumException class.
  * 
  * @author Michael Bischoff
  * @version $Revision$
  * 
  *          Extends the generic ApplicationException class in order to provide error details which are
  *          specific to creating an Enum instance from a token or Ordinal.
  */
 
 public class EnumException extends ApplicationException {
     private static final long serialVersionUID = 6578705245543364726L;
     private static final Logger logger = LoggerFactory.getLogger(EnumException.class);
     
     private static final int OFFSET = PARAMETER_ERROR * CLASSIFICATION_FACTOR + 18000; // offset for all codes in this class
     private static boolean textsInitialized = false;
     private String badParameter;
     
     static public final int INVALID_NUM                  = OFFSET + 1;
     static public final int INVALID_TOKEN                = OFFSET + 2;
     
     /**
      * Method lazyInitialization.
      * 
      * Upload textual descriptions only once they're needed for this type of exception class.
      * The idea is that in working environments, we will never need them ;-).
      * There is a small chance of duplicate initialization, because the access to the flag textsInitialized is not
      * synchronized, but duplicate upload does not hurt (is idempotent)
      */
     static private void lazyInitialization() {
         synchronized (codeToDescription) {
             textsInitialized = true;
             codeToDescription.put(INVALID_NUM                  , "Invalid Enum ordinal");
             codeToDescription.put(INVALID_TOKEN                , "Invalid Enum token");
         }
     }
     
     private final String getSpecificDescription() {
         return badParameter == null ? "?" : "Token <" + badParameter + ">";
     }
     
     private final void constructorSubroutine(String parameter) {
         this.badParameter = parameter;
         if (!textsInitialized)
             lazyInitialization();
         // for the logger call, do NOT use toString, because that can be overridden, and we're called from a constructor here
         logger.error("Error " + getErrorCode() + " (" + getStandardDescription() + ") for " + getSpecificDescription());
     }
     
     public EnumException(int errorCode, String parameter) {
         super(errorCode, null);
         constructorSubroutine(parameter);
     }
     
     public EnumException(int errorCode) {
         super(errorCode, null);
         constructorSubroutine(null);
     }
     
     @Override
     public String toString() {
         return getSpecificDescription() + ": " + super.toString();
     }
 }
