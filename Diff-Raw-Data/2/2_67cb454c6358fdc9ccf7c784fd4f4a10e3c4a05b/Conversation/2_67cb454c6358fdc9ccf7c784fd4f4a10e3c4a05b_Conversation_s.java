 package ch9k.chat;
 
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.chat.event.NewChatMessageEvent;
 import ch9k.chat.gui.ConversationWindow;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.awt.EventQueue;
 import java.util.Date;
 import javax.swing.DefaultListModel;
 import javax.swing.ListModel;
 
 /**
  * Represents a conversation between two users.
  * @author Jens Panneel
  */ 
 public class Conversation implements EventListener {
     private Contact contact;
     private boolean closed;
     private boolean initiatedByMe;
     private Date startTime = new Date();
     private ConversationSubject subject;
     private DefaultListModel messages = new DefaultListModel();
     private ConversationWindow window;
 
     /**
      * Constructor
      * @param contact The contact you are chatting with.
      * @param initiatedByMe Is this conversation started by me?
      */
     public Conversation(Contact contact, boolean initiatedByMe) {
         this.contact = contact;
         this.initiatedByMe = initiatedByMe;
         this.closed = false;
         
         EventPool.getAppPool().addListener(this, new ConversationEventFilter(this));
 
         // create a new window
         window = new ConversationWindow(Conversation.this);
         EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 window.init();
             }
         });
     }
     
     @Override
     public void handleEvent(Event event) {
         if(event instanceof NewChatMessageEvent){
             NewChatMessageEvent newChatMessageEvent = (NewChatMessageEvent) event;
             addMessage(newChatMessageEvent.getChatMessage());
         }
     }
 
     /**
      * Get the last messages.
      * Most recent message will be last in line.
      * When there arent n messages the size of the returned array will be
      * reduced to the number of messages.
      * @param n The number of messages to return.
      * @return The resulting messages.
      */
     public ChatMessage[] getMessages(int n) {
         int size = messages.getSize();
        n = n < size ? size : n;
         ChatMessage[] array = new ChatMessage[n];
 
         for(int i = 0; i < n; i++) {
             array[i] = (ChatMessage)messages.getElementAt(size-n+i);
         }
         
         return array;
     }
 
     /**
      * Close this conversation
      * @param closeWindow Close the window or not?
      */
     public void close(boolean closeWindow) {
         closed = true;
 
         if(closeWindow) {
             EventPool pool = EventPool.getAppPool();
             pool.removeListener(this);
 
             if(window.isVisible()) {
                 window.dispose();
             }
         }
     }
 
     /**
      * Check whether or not this conversation is started by the current user.
      * @return initiatedByMe
      */
     public boolean isInitiatedByMe() {
         return initiatedByMe;
     }
 
     /**
      * Get the chatting contact on the other end of the line.
      * @return contact
      */
     public Contact getContact() {
         return contact;
     }
 
     /**
      * Get the current subject of this conversation.
      * @return subject
      */
     public ConversationSubject getSubject() {
         return subject;
     }
 
     /**
      * Set the subject for this conversation.
      * @param subject
      */
     public void setSubject(ConversationSubject subject) {
         this.subject = subject;
     }
 
     /**
      * Get the date/time on witch this conversation was started
      * @return starttime
      */
     public Date getStartTime() {
         return startTime;
     }
 
     /**
      * Adds a message to this Conversation.
      * @param chatMessage
      */
     private void addMessage(ChatMessage chatMessage) {
         messages.addElement(chatMessage);
     }
 
     /**
      * Get the Messages ListModel
      * @return
      */
     public ListModel getMessageList() {
         return messages;
     }
 
     /**
      * Get the conversation window
      * @return window
      */
     public ConversationWindow getWindow() {
         return window;
     }
 
     /**
      * Check if window is closed
      * @return closed
      */
     public boolean isClosed() {
         return closed;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Conversation other = (Conversation) obj;
         if (this.contact != other.contact && (this.contact == null || !this.contact.equals(other.contact))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (this.contact != null ? this.contact.hashCode() : 0);
         return hash;
     }
 }
