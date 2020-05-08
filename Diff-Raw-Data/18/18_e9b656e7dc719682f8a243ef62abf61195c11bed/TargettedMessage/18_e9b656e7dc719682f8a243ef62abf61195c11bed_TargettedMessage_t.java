 /*
  * Created on Nov 23, 2004
  */
 package uk.org.ponder.messageutil;
 
 import java.io.Serializable;
 
 /**
  * Represents a single message, for presentation to the user, targetted at a
  * particular field or part of an interface. The message has not yet been 
  * resolved onto its localised representation (generally to be performed via
  * {@link MessageLocator}, and has an associated severity level. 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class TargettedMessage implements Serializable {
   public static final String TARGET_NONE = "No specific target";
   /** A fully resolved message, suitable for appearing to the user **/
   public String message;
   /**
    * A list of possible message codes, in descending order of specificity. This
    * is the same semantics as Spring's "MessageSourceResolvable" system.
    */
   public String[] messagecodes;
   /** A list of arguments to the message renderer **/
   public Object[] args = null;
   /** A target for the message - may have a context-dependent meaning **/
   public String targetid = TARGET_NONE;
   /** Any exception which originally gave rise to the message **/
   public Exception exception;
 
   public static final int SEVERITY_INFO = 0;
   public static final int SEVERITY_ERROR = 1;
   public static final int SEVERITY_CONFIRM = 2;
   public int severity = SEVERITY_ERROR;
 
   public boolean isError() {
     return ((severity & 1) == TargettedMessage.SEVERITY_ERROR);
   }
   
   public void updateMessageCode(String messagecode) {
     messagecodes = new String[] { messagecode };
   }
 
   public void updateTarget(String targetid) {
     this.targetid = targetid == null ? TARGET_NONE : targetid;
   }
 
   public String acquireMessageCode() {
    return messagecodes == null ? null : messagecodes[messagecodes.length - 1];
   }
 
   public TargettedMessage() {
   }
 
   /** Construct a simple targetted message
    * @param messagecode The message key for resolving the message text.
    * @param targetid The target field or path of the message, if <code>null</code>
    * will be substituted with {@link #TARGET_NONE}.
    */
   public TargettedMessage(String messagecode, String targetid) {
     updateMessageCode(messagecode);
     this.targetid = targetid == null ? TARGET_NONE
         : targetid;
   }
 
   public TargettedMessage(String messagecode, Object[] args, String targetid) {
     updateMessageCode(messagecode);
     updateTarget(targetid);
     this.args = args;
   }
 
   public TargettedMessage(String messagecode) {
     updateMessageCode(messagecode);
   }
 
   public TargettedMessage(String messagecode, Exception exception) {
     updateMessageCode(messagecode);
     this.exception = exception;
   }
 
   public TargettedMessage(String messagecode, Exception exception, String targetid) {
     updateMessageCode(messagecode);
     updateTarget(targetid);
     this.exception = exception;
   }
 
   public TargettedMessage(String messagecode, Object[] args) {
     updateMessageCode(messagecode);
     this.args = args;
   }
 
   public TargettedMessage(String messagecode, Object[] args, int severity) {
     updateMessageCode(messagecode);
     this.args = args;
     this.severity = severity;
   }
 
   public TargettedMessage(String[] messagecodes, Object[] args, String targetid) {
     updateTarget(targetid);
     this.messagecodes = messagecodes;
     this.args = args;
   }
 
   /** The fullest constructor for TargettedMessage.
    * @param messagecodes Message codes to be used for resolution, in descending
    * order of priority. See {@link MessageLocator}
    * @param args Any arguments required for substitution in the resolved message.
    * @param targetid The ID or path that the message is to be targetted at.
    * @param severity The severity of the message, either {@link #SEVERITY_INFO} or
    * {@link #SEVERITY_ERROR}
    */
   public TargettedMessage(String[] messagecodes, Object[] args,
       String targetid, int severity) {
     updateTarget(targetid);
     this.messagecodes = messagecodes;
     this.args = args;
     this.severity = severity;
   }
 
   public TargettedMessage(String messagecode, Object[] args, Exception exception) {
     updateMessageCode(messagecode);
     this.args = args;
     this.exception = exception;
   }
   
   public TargettedMessage(String messagecode, Object[] args, Exception exception, String targetid) {
     updateMessageCode(messagecode);
     updateTarget(targetid);
     this.args = args;
     this.exception = exception;
   }
   
   public String resolve(MessageLocator locator) {
     return message == null? locator.getMessage(messagecodes, args) : message;       
   }
   
 }
