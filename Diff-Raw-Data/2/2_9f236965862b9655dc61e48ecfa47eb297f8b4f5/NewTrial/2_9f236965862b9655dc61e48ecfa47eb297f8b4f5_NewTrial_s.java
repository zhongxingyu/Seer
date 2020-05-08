 package eu.nerdz.api;
 
 import eu.nerdz.api.impl.reverse.ReverseMessenger;
 
 public class NewTrial {
 
     public static void main(String[] args) {
 
         try {
 
            Messenger messenger = new ReverseMessenger("", "22.alex");
             ConversationHandler conversationHandler = messenger.getConversationHandler();
 
             for (Conversation conversation : conversationHandler.getConversations()) {
 
                 System.out.println(conversation.toString() + "\n");
                 for(Message message : conversationHandler.getMessagesFromConversation(conversation))
                     System.out.println(message);
                 System.out.println();
 
             }
 
             messenger.sendMessage("Gesù", "test1");
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 
 
 }
