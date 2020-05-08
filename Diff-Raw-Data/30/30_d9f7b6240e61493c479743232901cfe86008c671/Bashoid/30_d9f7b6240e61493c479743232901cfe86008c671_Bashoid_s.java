 package bashoid;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import org.jibble.pircbot.*;
 import utils.Config;
 import utils.FloodChecker;
 
 
 public class Bashoid extends PircBot implements AddonListener {
 
     private ArrayList<Addon> addons = new ArrayList<Addon>();
 
 
     public Bashoid() {
         setName( getNickFromConfig("bashoid") );
         setAutoNickChange(true);
         setMessageDelay(0);
         trySetUTFEncoding();
         registerAddons();
     }
 
     private String getNickFromConfig(String defaultNick) {
         Config config = new Config();
         return config.getValue("nickname", defaultNick);
     }
 
     private void trySetUTFEncoding() {
         try {
             setEncoding("UTF-8");
         } catch (UnsupportedEncodingException e) {
             System.err.println("Cannot set UTF-8 encoding.");
         }
     }
 
     private void registerAddons() {
         Addon.setAddonListener(this);
 
         addons.add( new bash.Bash() );
         addons.add( new pepa.Pepa() );
         addons.add( new rss.RSS() );
         // addons.add( new period.PeriodicMessage() );
         addons.add( new stopwatch.Stopwatch() );
         addons.add( new title.Title() );
         addons.add( new topic.Topic() );
         addons.add( new translator.Translator() );
         addons.add( new youtube.Youtube() );
     }
 
     @Override
     protected void onAction(String sender, String login, String hostname, String target, String action) {
         onMessage(target, sender, login, hostname, action);
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         onMessage( new Message(channel, sender, login, hostname, message) );
     }
 
     protected void onMessage(Message message) {
        if ( !FloodChecker.canBeServed(message.hostname) ) {
            sendNotice(message.author, "Prekrocen maximalni pocet pozadavku za minutu: " + FloodChecker.maxServesPerMinute() );
        } else {

            boolean hasReacted = false;

            for (Addon a : addons) {
                if ( a.shouldReact(message.text) ) {
                    sendAddonOutput(a, message);
                    hasReacted = true;
                }
             }

            if (hasReacted)
                FloodChecker.logServed(message.hostname);

         }
 
     }
 
     @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         if ( recipientNick.equals( getNick() ) ) {
             joinChannel(channel);
             sendMessage(channel, kickerNick + ": polib si");
         }
     }
 
     @Override
     public void sendAddonMessage(String target, String msg) {
         sendMessage(target, msg);
     }
 
     @Override
     public void sendAddonMessageToChannels(String msg) {
         String[] channels = getChannels();
         for (String channel : channels)
             sendMessage(channel, msg );
     }
 
     private void sendAddonOutput(Addon addon, Message message) {
         List<String> reaction = addon.generateReaction(message.text, message.author);
         if ( addon.errorOccurred() ) {
             displayAddonFailureIfIsSet( addon.getError() );
             sendListOfNotices(message.author, reaction);
         } else {
             sendListOfMessages(message.channel, reaction);
         }
     }
 
     protected void sendListOfMessages(String target, List<String> output) {
         for ( String line : output )
             sendMessage(target, line);
     }
 
     protected void sendListOfNotices(String target, List<String> output) {
         for ( String line : output )
             sendNotice(target, line);
     }
 
     protected void displayAddonFailureIfIsSet(Exception e) {
         if (e != null)
             System.err.println( e.getMessage() );
     }
 
     @Override
     public String getBotNickname() {
         return getNick();
     }
 
 }
