 package net.anei.cadpage;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import android.content.Context;
 
 /**
  * Buffers SMS messages so they can be included in problem reports and 
  * developer emails
  */
 public class SmsMsgLogBuffer {
   
   private static final String LOG_FILENAME = "log.msg.buffer";
   
   private Context context;
   
   private LinkedList<SmsMmsMessage> msgQueue = new LinkedList<SmsMmsMessage>();
   
   /**
    * private constructor
    */
   @SuppressWarnings("unchecked")
   private SmsMsgLogBuffer(Context context) {
     this.context = context;
     ObjectInputStream is = null;
     try {
       is = new ObjectInputStream(
         context.openFileInput(LOG_FILENAME));
       msgQueue = (LinkedList<SmsMmsMessage>)is.readObject();
     } catch (FileNotFoundException ex) {
       msgQueue = new LinkedList<SmsMmsMessage>();
     } catch (Exception ex) {
       Log.e(ex);
     } finally {
       if (is != null) try {is.close();} catch (IOException ex) {}
     }
   }
   
   /**
    * Check for duplicate MMS message notification
    * @param msg message to be checked
    * @return true if we have already processed this MMS notification
    */
   public boolean checkDuplicateNotice(SmsMmsMessage msg) {
     String contentLoc = msg.getContentLoc();
     if (contentLoc != null) {
       for (SmsMmsMessage msg2 : msgQueue) {
         if (contentLoc.equals(msg2.getContentLoc())) return true;
       }
     }
     return false;
   }
 
   /**
    * Add new message to message log buffer
    * @param msg message to be added
    * @returns true if message was added, false if message should
    * be suppressed because it duplicates a message already in the log
    * buffer
    */
   public synchronized boolean add(SmsMmsMessage msg) {
     
     // If we are suppressing duplicate messages, see if the new message
     // duplicates one already in the queue.  If queued message wasn't
     // identified as some kind of CAD message, don't check it
     if (msg.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_SMS && ManagePreferences.suppressDupMsg()) {
       for (SmsMmsMessage msg2 : msgQueue) {
        if (msg.equals(msg2)) return false;
       }
     }
     
     // Add new message to beginning of queue
     msgQueue.addFirst(msg);
     
     // Remove messages from end of queue to satisfy log limit
     int limit = ManagePreferences.logLimit();
     while (msgQueue.size() > limit) msgQueue.removeLast();
     
     persistQueue();
     return true;
   }
   
   /**
    * Add new message to message log buffer in response to application crash
    * This method is used to log a SmsMmsMessage that is believed to have been
    * responsible for a pending application crash.  It simply logs the message
    * persists the message queue and exits
    * @param msg message to be logged
    */
   public synchronized void addCrashMsg(SmsMmsMessage msg) {
     msgQueue.addFirst(msg);
     persistQueue();
   }
 
   /**
    * Write message queue to permanent storage
    */
   private void persistQueue() {
     // Persist entire list to file
     ObjectOutputStream os = null;
     try {
       os = new ObjectOutputStream(
         context.openFileOutput(LOG_FILENAME, Context.MODE_PRIVATE));
       os.writeObject(msgQueue);
     } catch (IOException ex) {
       Log.e(ex);
     } finally {
       if (os != null) try {os.close();} catch (IOException ex) {}
     }
   }
 
   /**
    * Update a message that has already been queued in the log buffer.  Uses for
    * MMS messages when the text content has been retrieved
    * @param message Message to b logged.
    */
   public synchronized void update(SmsMmsMessage message) {
     ListIterator<SmsMmsMessage> iter = msgQueue.listIterator();
     while (iter.hasNext()) {
       SmsMmsMessage msg = iter.next();
       if (msg.equals(message)) {
         iter.set(message);
         break;
       }
     }
   }
   
   /**
    * @return last message added to queue
    */
   public synchronized SmsMmsMessage getLastMessage() {
     
     // Queue is stored in reverse order, so get the first message
     if (msgQueue.size() == 0) return null;
     return msgQueue.getFirst();
   }
 
   /**
    * Append message information to support message under construction
    * @param sb String builder holding message being constructed
    */
   public synchronized void addMessageInfo(Context context, StringBuilder sb) {
     for (SmsMmsMessage msg : msgQueue) {
       msg.addMessageInfo(context, sb);
     }
   }
   
   private static SmsMsgLogBuffer instance = null;
   
   public static void setup(Context context) {
     instance = new SmsMsgLogBuffer(context); 
   }
   
   public static SmsMsgLogBuffer getInstance() {
     return instance;
   }
 }
