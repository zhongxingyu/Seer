 package ch9k.chat.events;
 
import ch9k.core.ChatApplication;
 import java.net.InetAddress;
 import ch9k.eventpool.NetworkEvent;
 
 public class ContactRequestEvent extends NetworkEvent {
     
     private String username;

    private String requester;
     
     public ContactRequestEvent(InetAddress target, String username) {
         super(target);
         this.username = username;
        this.requester = ChatApplication.getInstance().getAccount().getUsername();
     }
     
     public String getUsername() {
         return username;
     }

    public String getRequester() {
        return requester;
    }

 }
