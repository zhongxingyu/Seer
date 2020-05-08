 package bashoid;
 
 import bash.Bash;
 import java.util.ArrayList;
 import org.jibble.pircbot.*;
 
 
 public class Bashoid extends PircBot {
     
     public Bashoid() {
         setName("bashoid");
         setAutoNickChange(true);
         setMessageDelay(0);
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         if ( Bash.isBashMessage(message) )
            sendBash(channel, sender);
     }
 
     @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         if ( recipientNick.equals( getNick() ) ) {
             joinChannel(channel);
             sendMessage(channel, Colors.BOLD + "sorry...");
         }
     }
 
     protected void sendListOfMessages(String target, ArrayList<String> output) {
         for ( String line : output )
             sendMessage(target, line);
     }
 
    protected void sendListOfNotices(String target, ArrayList<String> output) {
        for ( String line : output )
            sendNotice(target, line);
    }

    private void sendBash(String channel, String sender) {
        Bash b = new Bash();
        if ( b.errorOccured() )
            sendListOfNotices(sender, b.getOutput() );
        else
            sendListOfMessages(channel, b.getOutput() );
    }

 }
