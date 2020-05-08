 /**
  * 
  */
 package net.java.dev.cejug.classifieds.exception;
 
 /**
  * @author rodrigo
  */
 public class ObjectInUseException extends Exception {
 
     /**
      * ObjectInUseException.java.
      */
     private static final long serialVersionUID = -4523245787125785648L;
 
     /**
      * Constructor.
      */
     public ObjectInUseException() {
 
        super();
     }
 
     /**
      * Constructor.
      * @param message
      */
     public ObjectInUseException(String message) {
 
         super(message);
 
     }
 
     /**
      * Constructor.
      * @param cause
      */
     public ObjectInUseException(Throwable cause) {
 
         super(cause);
 
     }
 
     /**
      * Constructor.
      * @param message
      * @param cause
      */
     public ObjectInUseException(String message, Throwable cause) {
 
         super(message, cause);
 
     }
 
 }
