 package ch9k.chat.events;
 
 import java.net.InetAddress;
 import ch9k.eventpool.NetworkEvent;
 
 public class ContactRequestEvent extends NetworkEvent {
     
     private String username;
     
     public ContactRequestEvent(InetAddress target, String username) {
         super(target);
         this.username = username;
     }
     
     public String getUsername() {
         return username;
     }
    
 }
