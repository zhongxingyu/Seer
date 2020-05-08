 package co.raawr.cah;
 
 import co.raawr.tempest.core.Core;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Main extends Core {
 
     public static void main(String[] args) {
         Main cah = new Main();
     }
 
     public Main() {
         initializeGame();
         //setVerbose(true);
         setVersion("Cards Against Humanity ALPHA");
         setNick("Humanity_Bot");
         setName("tempest");
         connect("frogbox.es", 6667);
     }
 
     private void initializeGame() {
         Handler.init(this);
         CAH.init(this);
         CAH.addCards();
     }
 
     @Override
     public void onConnection() {
         try {
             identify("cahbot7543");
             Thread.sleep(1000);
             joinChannel("#cah");
             joinChannel("#2");
         } catch (InterruptedException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
 
     @Override
     public void onMessage(String nick, String channel, String message) {
         parseMessage(nick, channel, message);
     }
 
     @Override
     public void onPrivateMessage(String nick, String message) {
        parseMessage(nick, nick, message);
     }
 
     private void parseMessage(String nick, String channel, String message) {
 
         if (channel.equals(getNick())) {
             // Handle PM here
             Handler.handlePM(nick, message);
         } else {
             // Handle command
             Handler.handleMessage(nick, channel, message);
         }
 
     }
 }
