 package ai.reaver;
 
 import ai.scribble.License;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import java.io.Serializable;
 
 /**
  * This class needs to be {@link Serializable} because it needs to be sent across EJB & Hazelcast nodes
 *
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jan 1, 2010
  * Time: 4:29:55 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 final public class ReturnImpl<T> implements Return<T>, Serializable {
 
     final static private String logMsgBeginning = "SORRY! I ENCOUNTERED AN EXCEPTION! HOWEVER, THE APPLICATION SHOULD REMAIN INTACT. SEE BELOW FOR MORE DETAILS.\n\n";
     static private long ERROR_SEQ_NUMBER = 1;
    final Injector loggerModule = Guice.createInjector(new LoggerModule());
    final LoggerClient logger = loggerModule.getInstance(LoggerClientFactory.class).getInstance(this.getClass().getName());
     private int returnStatus = 1;
     private T returnValue = null;
     private Throwable returnError = null;
     private String returnMsg = null;
 
     public ReturnImpl(final int returnStatus, final T returnValue, final Throwable returnError, final String returnMsg) {
         this.returnStatus = returnStatus;
         this.returnValue = returnValue;
         this.returnError = returnError;
         this.returnMsg = returnMsg;
     }
 
     public ReturnImpl(final T returnValue, final String returnMsg) {
         this.returnStatus = 0;
         this.returnValue = returnValue;
         this.returnMsg = returnMsg;
     }
 
     public ReturnImpl(final Throwable returnError, final String returnMsg, final boolean shouldLog) {
         if (shouldLog) {
             logger.log(logMsgBeginning + returnMsg + "\n", returnError);
         }
         this.returnStatus = 1;
         this.returnError = returnError;
         this.returnMsg = returnMsg;
     }
 
     /**
      * Returns 0 if no error occurred, or a unique error code specific only to this error. This code can be given to
      * the user and can be grepped or resolved in any manner you require, later.
      * <p/>
      * The current return implementation had to go by int and expects the system will not exeed integer range. To get
      * the exact error code, check if the return status is greater than zero and if so, get the code separately.
      *
      * @return int corresponding to the return status
      */
     @Override
     final public int returnStatus() {
         return returnStatus == 0 ? returnStatus : (int) ERROR_SEQ_NUMBER++;
     }
 
     /**
      * Returns a unique error code for this error, if any, or throws IllegalAccessError if otherwise.
      *
      * @return Unique ERROR_SEQ_NUMBER
      */
     @Override
     final public long returnErrorSeqCode() {
         if (returnStatus == 0) {
             throw new IllegalAccessError("SORRY! YOU CANNOT OBTAIN AN ERROR CODE WHEN THERE IS NO ERROR. PLEASE AVOID CALLING THIS METHOD IF NO ERROR IS FOUND.");
         }
         return ERROR_SEQ_NUMBER;
     }
 
     @Override
     final public T returnValue() {
         return returnValue;
     }
 
     @Override
     final public T returnValueBadly() {
         if (returnStatus() != 0) {
             throw new RuntimeException(returnError);
         }
         return returnValue;
     }
 
     @Override
     final public Throwable returnError() {
         return returnError;
     }
 
     @Override
     public String returnMsg() {
         return returnMsg;
     }
 
     @Override
     public boolean valid() {
         return returnStatus() == 0;
     }
 
     @Override
     public String toString() {
         return "Return{" +
                 "returnStatus=" + returnStatus +
                 ", returnValue=" + (returnValue != null ? returnValue.toString() : "null") +
                 ", returnError=" + (returnError != null ? returnError.toString() : "null") +
                 ", returnMsg='" + (returnMsg != null ? returnMsg : "") + '\'' +
                 '}';
     }
 }
