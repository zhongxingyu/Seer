 package org.jvnet.mock_javamail;
 
 import javax.mail.Message;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author dev@sokol-web.de <Kamill Sokol>
  */
 public class MailboxBuilder {
 
     private String address;
     private String name;
     private boolean subscribed;
     private boolean exists;
     private List<Message> messages = new ArrayList<>();
 
     public MailboxBuilder(String address) {
         this.address = address;
     }
 
     public MailboxBuilder inbox() {
         this.name = "INBOX";
         return this;
     }
 
     public MailboxBuilder mailbox(String name) {
         this.name = name;
         return this;
     }
 
     public MailboxBuilder subscribed(boolean subscribed) {
         this.subscribed = subscribed;
         return this;
     }
 
     public MailboxBuilder exists(boolean exists) {
         this.exists = exists;
         return this;
     }
 
     public MailboxBuilder addMessage(Message message) {
         this.messages.add(message);
         return this;
     }
 
     public Mailbox build() {
         try {
             Mailbox mailbox = new Mailbox(new InternetAddress(address), (name == null) ? "INBOX" : name);
             mailbox.setSubscribed(subscribed);
             mailbox.setExists(exists);
             mailbox.addAll(messages);
 
             MailboxHolder.addFixture(mailbox);

             return mailbox;
         } catch (AddressException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 }
