 package net.crimsoncactus.radioshark.client;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class MessageBubble extends VerticalPanel {
     private Label authorLabel;
     boolean me;
     String author;
     
     public MessageBubble(String author, String message, boolean isMe) {
         addStyleName("messagebubble");
         this.author = author;
         me = isMe;
         
         if (isMe) {
             addStyleName("fromme");
         } else {
             addStyleName("fromother");
         }
         
         if (author  != null) {
             authorLabel = new Label(author.toString());
             authorLabel.addStyleName("author");
             add(authorLabel);
         }
 
         appendMessage(message);
         
 
     }
     
     public String getAuthor() {
         return author;
     }
     
     public void appendMessage(String message) {
         Label html = new Label(message);
         html.addStyleName("message");
         add(html);
 
     }
 }
