 package edu.wustl.cab2b.common.exception;
 
 /**
  * @author gautam_shetty
  */
 public class RuntimeException extends java.lang.RuntimeException implements Cab2bExceptionInterface {
 
     private static final long serialVersionUID = 6463085010521715714L;
 
     protected String errorCode;
 
     public RuntimeException() {
    	// TODO Auto-generated constructor stub
     }
 
     public RuntimeException(Throwable throwable) {
         super(throwable);
     }
 
     public RuntimeException(String message) {
         super(message);
     }
 
     public RuntimeException(String message, String errorCode) {
         super(message);
         this.errorCode = errorCode;
     }
 
     public RuntimeException(String message, Throwable throwable) {
         super(message, throwable);
     }
 
     public RuntimeException(String message, Throwable throwable, String errorCode) {
         super(message, throwable);
         this.errorCode = errorCode;
     }
 
     /**
      * @return Returns the errorCode.
      */
     public String getErrorCode() {
         return errorCode;
     }
 
     /**
      * @param errorCode The errorCode to set.
      */
     public void setErrorCode(String errorCode) {
         this.errorCode = errorCode;
     }
 }
