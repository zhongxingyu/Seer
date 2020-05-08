 package eu.nerdz.api;
 
/**
 * User: Alessandro Suglia - alessandro.suglia@gmail.com
 * Date: 27/07/13
 * Time: 9.58
 */
 import eu.nerdz.api.impl.reverse.ReverseMessenger;
 

 public class NewTrial {
 
     public static void main(String[] args) {
 
         try {
 
             Messenger messenger = new ReverseMessenger("AlexZ", "");
             ConversationHandler conversationHandler = messenger.getConversationHandler();
 
             for (Conversation conversation : conversationHandler.getConversations()) {
 
                 System.out.println(conversation.toString() + "\n");
                 for(Message message : conversationHandler.getMessagesFromConversation(conversation))
                     System.out.println(message);
                 System.out.println();
 
             }
 
             messenger.sendMessage("AlexZ", "prova");
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 
 
 }
