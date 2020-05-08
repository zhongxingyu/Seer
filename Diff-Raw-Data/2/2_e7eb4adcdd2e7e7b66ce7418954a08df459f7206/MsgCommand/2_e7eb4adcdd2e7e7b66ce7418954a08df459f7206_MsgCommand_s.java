 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.rretzbach.bobchat.irc;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author rretzbach
  */
 public class MsgCommand extends IrcCommand {
     private String target;
     private String message;
 
     public MsgCommand() {
         keyword = "msg";
        pattern = Pattern.compile("\\A\\Q" + keyword + "\\E (\\S+) (\\S+)\\z");
     }
 
     @Override
     public boolean canHandle(String input) {
         if (input.matches(pattern.pattern())) {
             return true;
         }
         return false;
     }
 
     @Override
     public void handle(Conversation conversation, String input) {
         Matcher matcher = pattern.matcher(input);
         if (!matcher.find()) {
             return;
         }
         
         target = matcher.group(1);
         message= matcher.group(2);
         
         Network network = conversation.getNetwork();
         
         network.sendAnyMessage(target, message);
     }
     
 }
