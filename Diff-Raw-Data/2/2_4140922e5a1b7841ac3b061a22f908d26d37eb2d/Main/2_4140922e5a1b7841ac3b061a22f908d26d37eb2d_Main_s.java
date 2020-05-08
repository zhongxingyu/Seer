 package co.raawr.cah;
 
 import co.raawr.tempest.core.Core;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Main extends Core {
 
     public static void main(String[] args) throws Exception {
         Main cah = new Main();
     }
 
     public Main() throws Exception {
         initializeGame();
         //setVerbose(true);
         setVersion("Cards Against Humanity ALPHA");
         setNick("Humanity_Bot");
         setName("tempest");
         connect("frogbox.es", 6667);
        setDelay(200);
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
             joinChannel("#coldstorm");
         } catch (InterruptedException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
 
     @Override
     public void onMessage(String nick, String channel, String message) {
         parseMessage(nick, channel, message);
     }
 
     @Override
     public void onPart(String nick, String channel, String reason) {
         if (channel.equals("#cah")) {
             CAH.removePlayer(CAH.lookupPlayer(nick));
         }
     }
 
     @Override
     public void onKick(String channel, String sourceNick, String targetNick, String reason) {
         if (channel.equals("#cah")) {
             CAH.removePlayer(CAH.lookupPlayer(targetNick));
         }
     }
 
     @Override
     public void onQuit(String nick, String reason) {
         CAH.removePlayer(CAH.lookupPlayer(nick));
     }
 
     @Override
     public void onPrivateMessage(String nick, String message) {
         parseMessage(nick, getNick(), message);
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
