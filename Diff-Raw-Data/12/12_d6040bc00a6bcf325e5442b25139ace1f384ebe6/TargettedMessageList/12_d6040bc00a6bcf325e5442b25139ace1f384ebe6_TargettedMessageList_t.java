 /*
  * Created on Aug 4, 2005
  */
 package uk.org.ponder.messageutil;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.stringutil.StringList;
 
 /**
  * A typesafe container of {@link TargettedMessage} objects. It maintains the
  * hidden state of a field <code>nestedpath</code> which corresponds to the EL
  * location for which the data alterations currently being processed are based
  * at. TargettedMessage objects received to this list via the
  * {@link #addMessage(TargettedMessage)} methods will have their
  * <code>targetid</code> field, if set, interpreted as an EL relative to this
  * hidden path and rebased.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class TargettedMessageList implements Serializable {
  /** A nestedpath ending with this String will consume the following path
   * segments
   */
  public static final String BACKUP_PATH = "..";
   // NB - this class is ALMOST identical to the Spring "Errors" interface,
   // which I was put off since the only implementation there is "BindException"
   // which couples to all sorts of other greasy stuff, including the dreaded
   // BeanWrapper. However, it may be worth going back to Errors and Validator
   // at least, which are fairly clean in of themselves.
   private ArrayList errors = new ArrayList();
 
   /** Returns the number of messages held in this list * */
   public int size() {
     return errors.size();
   }
 
   /**
    * Appends a new TargettedMessage to this list. The <code>targetid</code>
    * field will be interpreted as an EL path adjusted to take into account of
    * the current value of <code>nestedPath</code>
    */
   public void addMessage(TargettedMessage message) {
     if (nestedpath.length() != 0
         && !message.targetid.equals(TargettedMessage.TARGET_NONE)) {
      if (nestedpath.endsWith(BACKUP_PATH)) {
        message.targetid = nestedpath.substring(0, nestedpath.length() - BACKUP_PATH.length());
      }
      else {
        message.targetid = nestedpath + message.targetid;
      }
     }
     errors.add(message);
   }
 
   /**
    * Appends multiple messages to this list, through multiple calls to
    * {@link #addMessage(TargettedMessage)}.
    */
 
   public void addMessages(TargettedMessageList list) {
     for (int i = 0; i < list.size(); ++i) {
       addMessage(list.messageAt(i));
     }
   }
 
   /**
    * Does the current state of the TML represent an error? This is determined by
    * checking for any individual message which is at the
    * {@link TargettedMessage#SEVERITY_ERROR} severity level.
    */
   public boolean isError() {
     for (int i = 0; i < size(); ++i) {
       TargettedMessage mess = messageAt(i);
       if ((mess.severity & 1) == TargettedMessage.SEVERITY_ERROR)
         return true;
     }
     return false;
   }
 
   private StringList pathstack;
 
   private String nestedpath = "";
 
   /** Push the supplied path segment onto the hidden path base * */
   public void pushNestedPath(String extrapath) {
     if (extrapath == null) {
       extrapath = "";
     }
     if (extrapath.length() > 0 && !extrapath.endsWith(".")) {
       extrapath += '.';
     }
     if (pathstack == null) {
       pathstack = new StringList();
     }
     pathstack.add(nestedpath);
     this.nestedpath = nestedpath + extrapath;
   }
 
   /** Pops one path segment off the hidden path base * */
 
   public void popNestedPath() {
     int top = pathstack.size() - 1;
     nestedpath = pathstack.stringAt(top);
     pathstack.remove(top);
   }
 
   /**
    * Access a message by index.
    * 
    * @param i The index of the message to access.
    * @return The TargettedMessage at index position <code>i</code>
    */
   public TargettedMessage messageAt(int i) {
     return (TargettedMessage) errors.get(i);
   }
 
   /**
    * Update a message held at a particular index.
    * 
    * @param i The index of the message to update
    * @param message The new message to be held at index <code>i</code>
    */
   public void setMessageAt(int i, TargettedMessage message) {
     errors.set(i, message);
   }
 
   /** Clears all messages from this list * */
 
   public void clear() {
     errors.clear();
   }
 
   /**
    * Pack this list into a String for debugging purposes.
    */
   public String pack() {
     CharWrap togo = new CharWrap();
     for (int i = 0; i < size(); ++i) {
       TargettedMessage mess = messageAt(i);
       togo.append("Target: " + mess.targetid + " message "
           + mess.acquireMessageCode() + "\n");
     }
     return togo.toString();
   }
 
   /**
    * Renders the entire contents of this message list into a list of localised
    * messages, using the supplied <code>MessageLocator</code>
    */
   public StringList render(MessageLocator locator) {
     StringList togo = new StringList();
     for (int i = 0; i < size(); ++i) {
       TargettedMessage message = messageAt(i);
       togo.add(locator.getMessage(message.messagecodes, message.args));
     }
     return togo;
   }
 
 }
