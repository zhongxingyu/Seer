 package net.anei.cadpage;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.anei.cadpage.parsers.MsgParser;
 
 import android.content.Context;
 
 /**
  * This class is responsible for processing incoming SMS text messages so they
  * can be merged back into individual CAD page messages 
  *
  */
 public class SmsMsgAccumulator {
   
   private List<MessageList> msgQueue = new LinkedList<MessageList>();
   
   private SmsMmsMessage nextMsg = null;
   private long startTimeoutId = 0;
   private long cancelTimeoutId = 0;
   
   private Context context;
   
 
   /**
    * Constructor - checks for save file and if found restores message status from it
    * @param context
    */
   private SmsMsgAccumulator(Context context) {
     this.context = context;
   }
   
   /**
    * Add new text message to message accumulator
    * @param newMsg message to be added
    * @return true if message was accepted, which means receiver should abort
    * the message broadcast unless configured to not do so.
    */
   public boolean addMsg(SmsMmsMessage newMsg) {
     
     // Reset timeout request
     startTimeoutId = 0;
     cancelTimeoutId = 0;
     
     // First step is to see if this msg matches any of the currently 
     // accumulating messages
     MessageList curList = null;
     for (MessageList list : msgQueue) {
       if (list.matches(newMsg)) {
         curList = list;
         break;
       }
     }
     
     // Did we find a match?
     if (curList != null) {
       
       // Yes we did
       // add the new message to this message list
       curList.addMsg(newMsg);
       
       // If we have a complete message list, set up as the next return messsage
       // remove if from the processing queue
       // and cancel any pending timeout for this list
       // If the queue is empty, release the KeepAliveService lock
       if (curList.isComplete()) {
         nextMsg = curList.getMessage();
         msgQueue.remove(curList);
         cancelTimeoutId = curList.getTimeoutId();
         if (msgQueue.isEmpty()) KeepAliveService.unregister(context, this);
       }
       
       // In any case, this message has been accepted
       return true;
     }
     
     // See if the current parser will handle this message
     boolean isPage = newMsg.isPageMsg();
     
     // If not a cad page (or general alert), we're done with this
     if (! isPage) return false;
     
     // If the message contains counters indicating that there is more to follow
     // of if configuration settings expect more than one message as a minimum
     // or if the location parser determines that more data is expected, Create
     // a new message list to build an accumulated CAD page, and start the timeout
     // timer for this message list.
     if (isSplitCad(newMsg)) {
       if (msgQueue.isEmpty()) {
         KeepAliveService.register(context, this, R.drawable.ic_stat_notify,
                                   R.string.notification_msg_acc_title,
                                   R.string.notification_msg_acc_text);
       }
       MessageList list = new MessageList(newMsg);
       msgQueue.add(list);
       startTimeoutId = list.getTimeoutId();
       return true;
     }
     
     // Otherwise simply pass this message into the output queue
     nextMsg = newMsg;
     return true;
   }
   
   /**
    * Determine if an incoming message is the first of a possible sequence of
    * text messages generated from a single CAD page
    * @param msg message to be checked
    * @return true if this is a possible split message CAD page
    */
   private boolean isSplitCad(SmsMmsMessage msg) {
 
     // If this message has a message count, life is easy
     // return false if the count is 1 and true if it is > 1
     int msgCount = msg.getMsgCount();
     if (msgCount == 1) return false;
     if (msgCount > 1) return true;
     
     // If there is a configured minimum message count > 1, return true
     if (ManagePreferences.splitMinMsg() > 1) return true;
     
     // If the message parser decided more data was needed, return true;
     if (msg.getInfo().isExpectMore()) return true;
     
     // If this was parsed as a general alert call, and the accumulate messages
     // in reverse order was set, return true.  If messages are coming in in 
     // reverse order and we get the last part first, the we won't know if
     // this is a real CAD page until the first part is received, so we have to
     // treat everything as a potential CAD page.
     if (msg.getInfo().getCall().equals("GENERAL ALERT") &&
         ManagePreferences.revMsgOrder()) return true;
     
     // Otherwise the answer is no
     return false;
   }
 
   /**
    * Should be called when a timeout with the id of id expires
    * @param id timeout ID
    */
   public void timeout(long id) {
     
     // Reset timeout request
     startTimeoutId = 0;
     cancelTimeoutId = 0;
     
     // If nothing in queue, nothing to worry about
     if (msgQueue.isEmpty()) return;
     
     // First step is to see if this msg matches any of the currently 
     // accumulating messages
     for (MessageList list : msgQueue) {
       if (list.getTimeoutId() == id) {
         nextMsg = list.getMessage();
         msgQueue.remove(list);
         break;
       }
     }
     
     // If we removed everything, cancel the keep alive service
     if (msgQueue.isEmpty()) KeepAliveService.unregister(context, this);
   }
   
   /**
    * @return next available message if there is one, null otherwise
    */
   public SmsMmsMessage nextMsg() {
     SmsMmsMessage result = nextMsg;
     nextMsg = null;
     return result;
   }
   
   /**
    * This should be called after a call to addMsg.  If it returns a
    * non-zero timeout ID, a timeout timer should be started with this ID.
    * @return timeout ID
    */
   public long startTimeoutId() {
     return startTimeoutId;
   }
   
   /**
    * This should be called after a call to addMsg.  If it returns a
    * non-zero timeout ID, an existing timeout timer should be with this ID
    * should be canceled
    * @return timeout ID
    */
   public long cancelTimeoutId() {
     return cancelTimeoutId;
   }
   
   /**
    * private list containing some number of text messages being compiled into
    * a single CAD message
    */
   private static class MessageList {
     
     private int count;  // Expected message count
     private List<SmsMmsMessage> list = new ArrayList<SmsMmsMessage>();
     private SmsMmsMessage firstMessage;
     private SmsMmsMessage lastMessage;
     
     private transient SmsMmsMessage result = null;
 
     /**
      * Construct new message list based on incoming message
      * @param newMsg incoming message
      */
     public MessageList(SmsMmsMessage newMsg) {
       firstMessage = newMsg;
       count = newMsg.getMsgCount();
       if (count > 0) {
         for (int j = 0; j<count; j++) list.add(null);
       }
       addMsg(newMsg);
     }
     
     /**
      * Determine if new message belongs in this message list
      * @param newMsg new message
      * @return true if new message should be added to this list
      */
     public boolean matches(SmsMmsMessage newMsg) {
       
       // If req msg count is differen't don't even consider it
       if (newMsg.getMsgCount() != count) return false;
       
       // This message must have been sent within 30 seconds of last message
       if (newMsg.getSentTime() - lastMessage.getSentTime() > 30000) return false;
       
       // There is an option to not check the sender address. But we have to check
       // something or we will accumulate messages from anyone, whether or not
       // they pass any sender filters in place.  What we will do is accept
       // this as a match if either the first or last 4 characters are the same
       String newAddr = newMsg.getFromAddress();
       String lastAddr = lastMessage.getFromAddress();
       if (! ManagePreferences.splitChkSender()) {
         if (newAddr.length() >= 4 && lastAddr.length() >= 4) {
           if (newAddr.substring(0,4).equals(lastAddr.substring(0,4))) return true;
           if (newAddr.substring(newAddr.length()-4).equals(lastAddr.substring(lastAddr.length()-4))) return true;
         }
       }
       
       // We generally expect a complete sender address match
       if (newAddr.equals(lastAddr)) return true;
       
       // if they don't match, we will check the last 3 characters of both addresses
       // if they form sequential numbers, we'll take that
       int len = newAddr.length();
       if (len >= 3 && lastAddr.length() == len) {
         try {
           int newSeq = Integer.parseInt(newAddr.substring(len-3));
           int lastSeq = Integer.parseInt(lastAddr.substring(len-3));
           if (newSeq == lastSeq + 1) return true;
         } catch (NumberFormatException ex) {}
       }
       return false;
     }
 
     /**
      * Add new message to message list
      * @param newMsg new message to be added
      */
     public void addMsg(SmsMmsMessage newMsg) {
       result = null;
       lastMessage = newMsg;
       if (count > 0) {
         int ndx = newMsg.getMsgIndex();
        list.set(ndx-1, newMsg);
       } else {
         if (ManagePreferences.revMsgOrder()) {
           list.add(0, newMsg);
         } else {
           list.add(newMsg);
         }
       }
     }
     
     /**
      * @return true if message list forms a complete message
      */
     public boolean isComplete() {
       
       // If we are looking for a specific number of messages, see if they
       // are all present
       if (count > 0) {
         for (SmsMmsMessage msg : list) {
           if (msg == null) return false;
         }
         return true;
       }
       
       // If there is a minimum count that we have not reached, return false
       int minCount = ManagePreferences.splitMinMsg();
       if (list.size() < minCount) return false;
       
       // Otherwise, invoke the current location parser and see if it thinks
       // we have a complete message.  If we have an impossible parse failure
       // return complete so we can get rid of this message
       SmsMmsMessage msg = getMessage();
       if (msg == null) return true;
       
       boolean isPage = msg.isPageMsg(MsgParser.PARSE_FLG_SKIP_FILTER);
       if (isPage && !msg.getInfo().isExpectMore()) return true;
       
       // If not, return false;
       return false;
     }
 
     /**
      * @return timeout associated with this message list
      */
     public long getTimeoutId() {
       return firstMessage.getTimestamp();
     }
     
     /**
      * Convert list into a combined message
      * @return
      */
     public SmsMmsMessage getMessage() {
       if (result == null) result = new SmsMmsMessage(firstMessage, list);
       
       // We have to call isPageMsg() to do the initial message parsing,
       // and we have to force processing because the component text messages
       // have already been blocked from getting to the regular messaging app.
       // If the parse still fails despite the force flag (very bady news)
       // make this message disappear
       if (!result.isPageMsg(SmsMmsMessage.PARSE_FLG_FORCE)) result = null;
       return result;
     }
   }
   
   private static SmsMsgAccumulator instance = null;
   
   public static void setup(Context context) {
     instance = new SmsMsgAccumulator(context);
   }
   
   public static SmsMsgAccumulator instance() {
     return instance;
   }
 }
