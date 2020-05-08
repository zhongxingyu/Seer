 package ch9k.chat;
 
 import ch9k.chat.event.CloseConversationEvent;
 import ch9k.chat.event.ContactOfflineEvent;
 import ch9k.chat.event.NewChatMessageEvent;
 import ch9k.chat.event.NewConversationEvent;
 import ch9k.core.I18n;
 import ch9k.core.event.AccountLogoffEvent;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.log4j.Logger;
 
 /**
  * Manages the active conversations
  * @author Jens Panneel
  */
 public class ConversationManager implements Iterable<Conversation> {
     private Map<Contact, Conversation> conversations;
 
     public ConversationManager() {
         conversations = new HashMap<Contact, Conversation>();
 
         EventPool.getAppPool().addListener(new ContactOfflineListener(),
                 new EventFilter(ContactOfflineEvent.class));
         EventPool.getAppPool().addListener(new CloseConversationListener(),
                 new EventFilter(CloseConversationEvent.class));
         EventPool.getAppPool().addListener(new NewConversationListener(),
                 new EventFilter(NewConversationEvent.class));
 
         EventPool.getAppPool().addListener(new EventListener() {
             public void handleEvent(Event event) {
                 clear();
             }
         }, new EventFilter(AccountLogoffEvent.class));
 
     }
 
     /**
      * Start a conversation with the given contact
      * @param contact The contact to start a new conversation with.
      * @param initiatedByMe Whether the local user started the conversation
      * @return conversation The started conversation
      */
     public Conversation startConversation(Contact contact, boolean initiatedByMe) {
         if(!conversations.containsKey(contact)) {
             Conversation conversation = new Conversation(contact, initiatedByMe);
             conversations.put(contact, conversation);
             return conversation;
         } else {
             return conversations.get(contact);
         }
     }
 
     /**
      * Close the conversation with the given contact
      * @param conversation The conversation to close
      * @param forceClose If the window should be really closed
      */
     public void closeConversation(Conversation conversation, boolean forceClose) {
         Logger.getLogger(ConversationManager.class).info(
                 "Closing conversation with " + 
                 conversation.getContact().getUsername());
 
         // send a system event
        NewChatMessageEvent systemEvent = new NewChatMessageEvent(
                conversation, new ChatMessage("info", I18n.get("ch9k.chat",
                    "contact_closed_conversation"), true));
        EventPool.getAppPool().raiseEvent(systemEvent);
         
         // close the conversation
         conversation.close(forceClose);
     }
 
     public void clear() {
         // using an iterator, so we can remove them immediately
         Iterator<Conversation> it = conversations.values().iterator();
         while(it.hasNext()) {
             closeConversation(it.next(), true);
             it.remove();
         }
     }
 
     /**
      * Get the conversation you have with the given contact
      * @param contact
      * @return conversation
      */
     public Conversation getConversation(Contact contact) {
         return conversations.get(contact);
     }
 
     @Override
     public Iterator<Conversation> iterator() {
         return conversations.values().iterator();
     }
 
     private class ContactOfflineListener implements EventListener {
         public void handleEvent(Event ev) {
             ContactOfflineEvent event = (ContactOfflineEvent)ev;
             Conversation conversation = conversations.get(event.getContact());
             
             if(conversation != null) {
                 CloseConversationEvent closeEvent = new CloseConversationEvent(conversation);
                 closeEvent.setSource(event.getContact().getIp());
                 EventPool.getAppPool().raiseEvent(closeEvent);
             }
         }
     }
 
     private class NewConversationListener implements EventListener {
         public void handleEvent(Event ev) {
             // a new conversation has been started
             NewConversationEvent newConversationEvent = (NewConversationEvent)ev;
             Contact contact = newConversationEvent.getContact();
 
             // verify that this request comes from a real contact
             if(contact != null && contact.isOnline()) {
                 startConversation(newConversationEvent.getContact(),
                     !newConversationEvent.isExternal());
             }
         }
     }
 
     public class CloseConversationListener implements EventListener {
         public void handleEvent(Event ev) {
             // a conversation has been closed
             CloseConversationEvent closeConversationEvent = (CloseConversationEvent)ev;
             Conversation conversation = closeConversationEvent.getConversation();
 
             if(conversation != null) {
                 closeConversation(conversation, !closeConversationEvent.isExternal());
 
                 // remove the conversation from the manager-list
                 conversations.remove(conversation.getContact());
             }
         }
     }
 }
