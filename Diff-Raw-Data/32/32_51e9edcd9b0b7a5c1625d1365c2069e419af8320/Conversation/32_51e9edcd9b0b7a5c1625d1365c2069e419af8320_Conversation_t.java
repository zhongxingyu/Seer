 package ch9k.chat;
 
 import ch9k.chat.events.CloseConversationEvent;
 import ch9k.chat.events.ConversationEventFilter;
 import ch9k.chat.events.NewChatMessageEvent;
 import ch9k.chat.gui.ConversationWindow;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.awt.EventQueue;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.swing.AbstractListModel;
 
 /**
  * Represents a conversation between two users.
  * @author Jens Panneel
  */
 public class Conversation extends AbstractListModel implements EventListener {
     private Contact contact;
     private boolean initiatedByMe;
     private Date startTime = new Date();
     private ConversationSubject subject;
     private List<ChatMessage> messages = new ArrayList<ChatMessage>();
     private ConversationWindow window;
 
     /**
      * Constructor
      * @param contact The contact you are chatting with.
      * @param initiatedByMe Is this conversation started by me?
      */
     public Conversation(Contact contact, boolean initiatedByMe) {
         this.contact = contact;
         this.initiatedByMe = initiatedByMe;
         
         EventPool.getAppPool().addListener(this, new ConversationEventFilter(this));
 
         // create a new window
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                 window = new ConversationWindow(Conversation.this);
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
      * Close this conversation
      */
     public void close() {
         EventPool pool = EventPool.getAppPool();
         pool.raiseEvent(new CloseConversationEvent(contact));
         pool.removeListener(this);
 
         window.markAsClosed(true);
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
         int size = messages.size();
         if(size == 0 || !chatMessage.equals(messages.get(size-1))){
             messages.add(chatMessage);
             fireIntervalAdded(contact, size, size);
         }
     }
 
     /**
      * Get the n last messages as Strings.
      * Most recent message will be last in line.
      * When there arent n messages the size of the returned array will be reduced to the number of messages.
      * @param n The number of messages to return
      * @return String[]
      */
     public String[] getMessages(int n) {
         if(n > messages.size()) {
             n = messages.size();
         }
         String[] result = new String[n];
         int size = messages.size();
         for(int i = size - n; i < size; i++){
             result[i - size + n] = messages.get(i).getText();
         }
         return result;
     }
 
 
 
     @Override
     public int getSize() {
         return messages.size();
     }
 
     @Override
     public Object getElementAt(int index) {
         return messages.get(index);
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
