 package hudson.plugins.ircbot.v2;
 
 import org.pircbotx.Colors;
 
 /**
  * Simple support for IRC colors.
  * 
  * @author syl20bnr
  */
 public class IRCColor {
 
     private final String message;
 
     public IRCColor(String message) {
         this.message = message;
     }
 
     public String colorize(){
         String foreground = Colors.DARK_GRAY;
         if(this.message.contains("Starting ")){
             if (this.message.contains("STILL FAILING")){
                 foreground = Colors.BROWN;
             }
             else if (this.message.contains("FAILURE")){
                 foreground = Colors.BOLD + Colors.YELLOW;
             }
             else{
                 foreground = Colors.DARK_GREEN;
             }
         }
         else if(this.message.contains("FIXED")){
           foreground = Colors.BOLD + Colors.UNDERLINE + Colors.WHITE;
         }
         else if(this.message.contains("SUCCESS")){
            foreground = Colors.BOLD + Colors.GREEN;
         }
         else if(this.message.contains("FAILURE")){
           foreground = Colors.BOLD + Colors.UNDERLINE + Colors.WHITE;
         }
         else if(this.message.contains("STILL FAILING")){
            foreground = Colors.BOLD + Colors.RED;
         }
         return foreground + this.message;
     }
 
 }
