 package vooga.rts.networking.example;
 
 import vooga.rts.networking.server.Message;
 
 
 public class ChatMessage extends Message {
 
     /**
      * 
      */
     private static final long serialVersionUID = 765150064081796600L;
     private String myMessage;
 
     public ChatMessage (int timeSent, String message) {
        super(timeSent);
         myMessage = message;
     }
     
     public ChatMessage (String message) {
         super();
         myMessage = message;
     }
 
     public String getMessage () {
         return myMessage;
     }
 }
