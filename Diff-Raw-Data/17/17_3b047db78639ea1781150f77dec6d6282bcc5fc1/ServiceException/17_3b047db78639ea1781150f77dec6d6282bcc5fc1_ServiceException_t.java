 package fm.audiobox.core.exceptions;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 
 
 
 /**
  * ServiceException is thrown whenever a connection exception occurs while
  * performing requests to AudioBox.fm services.
  *
  * <p>
  * Exception is specified through the error codes.
  * </p>
  */
 public class ServiceException extends AudioBoxException {
 
   /** Socket exception error code */
   public static final int SOCKET_ERROR = 1;
 
   /** Client exception error code */
   public static final int CLIENT_ERROR = 2;
 
   /** Timeout exception error code */
   public static final int TIMEOUT_ERROR = 3;
   
   private static final long serialVersionUID = 1L;
 
   
   public ServiceException() {
     super();
   }
 
   public ServiceException(String message, Throwable cause) {
     super(message, cause);
   }
   
   public ServiceException(int errorCode, String message){
     super(message);
     this.errorCode = errorCode;
   }
   
  public ServiceException(int errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
  
 
   public ServiceException(String message) {
     super(message);
   }
 
   public ServiceException(Throwable cause) {
     super(cause);
     
     if ( cause instanceof ClientProtocolException ){
       this.errorCode = CLIENT_ERROR;
     } else if ( cause instanceof IOException ){
       this.errorCode = SOCKET_ERROR;
     }
     
   }
   
   public void fireGlobally() {
     this.configuration.getDefaultServiceExceptionHandler().handle( this );
   }
   
 }
