 import botto.xmpp.annotations.Receive;
 import org.xmpp.packet.Message;
 
 /**
  * A simple bot that replies to a number incrementing it by one.
  */
 public class CountUpBot {
 
     /**
      * Reply to a number, increased by one
      * @param the message being received
      */
     @Receive
     public Message countUp(Message receive) {
         Message message = receive.createCopy();
         message.setFrom(receive.getTo());
         message.setTo(receive.getFrom());
 
         try {
             String body = receive.getBody();
             int number = Integer.parseInt(body);
             message.setBody(number + " + 1 = " + (number + 1));
         }
         catch(Exception ex) {
             message.setBody("What's that?");
         }
 
         return message;
     }
 }
