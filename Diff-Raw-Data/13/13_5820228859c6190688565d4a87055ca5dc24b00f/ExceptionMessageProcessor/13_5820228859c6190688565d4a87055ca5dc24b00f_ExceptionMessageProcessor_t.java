 /*
  * Created on 4 Apr 2008
  */
 package uk.org.ponder.rsf.state.support;
 
 import uk.org.ponder.errorutil.CoreMessages;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.rsf.state.MessageProcessor;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /** A default implementation which processes those TargettedMessages which were 
  * constructed to represent exceptions arising from the model, accepting a 
  * message delimiter code (by default "msg=") in the exception text, delimiting a 
  * section to be resolved as a message code. 
  * 
  * <p/> For those messages which are absent this delimiter in exception text, they by
  * default are mapped onto the standard framework code {@link CoreMessages#GENERAL_ACTION_ERROR}.
  *  
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class ExceptionMessageProcessor implements MessageProcessor {
   
   private String messageDelimiter;
 
   public void setMessageDelimiter(String messageDelimiter) {
     this.messageDelimiter = messageDelimiter;
   }
 
   public String processMessage(TargettedMessage message, String target) {
     if (message.messagecodes.length == 1 
         && message.messagecodes[0].equals(CoreMessages.RAW_EXCEPTION_PLACEHOLDER) 
         && message.exception != null) {
       String emess = UniversalRuntimeException.unwrapException(message.exception).getMessage();
      if (emess == null) {
        emess = "";
      }
       int delimpos = emess.indexOf(messageDelimiter);
       if (delimpos == -1) {
         handleRawException(message);
       }
       else {
         String key = emess.substring(delimpos + messageDelimiter.length());
         message.updateMessageCode(key);
       }
     }
     return null;
   }
 
   protected void handleRawException(TargettedMessage message) {
     message.updateMessageCode(CoreMessages.GENERAL_ACTION_ERROR);
   }
 
 }
