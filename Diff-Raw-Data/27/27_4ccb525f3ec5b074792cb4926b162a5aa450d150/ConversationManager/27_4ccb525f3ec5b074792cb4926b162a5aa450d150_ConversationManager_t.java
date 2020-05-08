 package ch9k.chat;
 
 import java.util.Map;
 
 /**
  *
  * @author Jens Panneel
  */
 public class ConversationManager {
     private Map<Contact, Conversation> conversations;
 
     /**
      * Start a conversation with the given contact
      * @param contact The contact to start a new conversation with.
      * @return conversation The started conversation
      */
     public Conversation startConversation(Contact contact) {
         // TODO pass true current active plugins
         Conversation conversation = new Conversation(contact, true, null);
        // TODO what if there is already a conversation with this contact?
         conversations.put(contact, conversation);
         return conversation;
     }
 
     /**
      * Close the conversation with the given contact
      * @param contact The contact you want to stop chatting with
      */
     public void closeConversation(Contact contact) {
         conversations.get(contact).close();
     }
 
     /**
      * Get the conversation you have with the given contact
      * @param contact
      * @return conversation
      */
     public Conversation getConversation(Contact contact) {
         return conversations.get(contact);
     }
     
 }
