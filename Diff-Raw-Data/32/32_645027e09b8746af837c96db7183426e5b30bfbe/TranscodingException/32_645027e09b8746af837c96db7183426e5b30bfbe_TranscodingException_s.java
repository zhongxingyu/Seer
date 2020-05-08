 package dk.statsbiblioteket.broadcasttranscoder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 2/5/13
  * Time: 3:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TranscodingException extends Exception {
 
     public TranscodingException() {
     }
 
     public TranscodingException(String message) {
         super(message);
     }
 
     public TranscodingException(String message, Throwable cause) {
         super(message, cause);
     }
 
     public TranscodingException(Throwable cause) {
         super(cause);
     }
 
    public TranscodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
 }
