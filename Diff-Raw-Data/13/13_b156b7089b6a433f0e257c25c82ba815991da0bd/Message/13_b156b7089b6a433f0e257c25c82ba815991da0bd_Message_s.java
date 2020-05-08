 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lighting;
 
 /**
  * 
  * @author StompingBrokenGlass <stompingbrokenglass@gmail.com>
  * @since 2013-06-13
  */
 public class Message {
     
     private String rawMessage ;
     private boolean privateMessage ;
     private String context;
     private String sender;
     private String channel;
     private String nickname;
     
     public Message (String message, String nickname ){
         this.nickname = nickname;
         this.parseMessage(message);
     }
     
    public void parseMessage (String message){
         
          //:stomp!~stomp@localhost.WAG160N PRIVMSG Lighting :test
                 
         //:stomp!~stomp@localhost.WAG160N PRIVMSG #test :HI
         
         this.rawMessage = message;
         
         int firstSpace = message.indexOf(" ");
         int secondSpace = message.indexOf(" ", firstSpace+1);
         int thirdSpace = message.indexOf(" ", secondSpace+1);
         
         this.sender = message.substring(1,message.indexOf('!'));
         
         this.channel = message.substring(secondSpace+1,thirdSpace);
         
         this.context = message.substring(thirdSpace+2);
         
         if (channel.equalsIgnoreCase(this.nickname)){
             // Private message
             this.privateMessage = true;
         }else{
             this.privateMessage = false;
         }
         
         
     }
     
     public boolean isPM () {
         return privateMessage;
     }
     
     public String getContext () {
         return context;
     }
     
     public String getSender () {
         return sender;
     }
     
     public String getChannel () {
         return channel;
     }
 }
