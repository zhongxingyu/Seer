 package in.haeg.csbot;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.schwering.irc.lib.IRCConnection;
 import org.schwering.irc.lib.IRCEventListener;
 import org.schwering.irc.lib.IRCModeParser;
 import org.schwering.irc.lib.IRCUser;
 import org.schwering.irc.lib.IRCUtil;
 
 public class Main {
     private static IRCConnection        m_Conn;
     private static Logger               m_Log;
     private static Map<String, Integer> m_Karma;
 
     public static void main(String[] args) {
         m_Log = Logger.getLogger(Main.class.getName());
         m_Log.setLevel(Level.FINEST);
 
         m_Karma = new HashMap<String, Integer>();
 
         m_Conn = new IRCConnection(Constants.HOST, Constants.PORT, Constants.PASSWORD, Constants.NICK_NAME, Constants.USER_NAME, Constants.REAL_NAME);
         m_Conn.setColors(false);
         m_Conn.setPong(true);
         try {
             m_Conn.connect();
             m_Conn.doJoin(Constants.CHANNEL);
             m_Conn.doPrivmsg("#cs-york", "Hi, I'm a fairly basic karma bot. Tell Haegin if you want me to be able to do more than just count karma");
 
             m_Conn.addIRCEventListener(new IRCEventListener() {
 
                 @Override public void unknown(String a_prefix, String a_command, String a_middle, String a_trailing) {
                     m_Log.log(Level.INFO, "Received unknown message: " + a_prefix + ", " + a_command + ", " + a_middle + ", " + a_trailing);
                 }
 
                 @Override public void onTopic(String a_chan, IRCUser a_user, String a_topic) {
                 }
 
                 @Override public void onReply(int a_num, String a_value, String a_msg) {
                     System.out.println("REPLY: " + a_value + " =][= " + a_msg);
 
                     // If the reply is a list of names then we want to process them and add them to the list of karma names
                     if (a_num == IRCUtil.RPL_NAMREPLY) {
                         for (String name : a_msg.split("[ \t]+")) {
                             if (!m_Karma.containsKey(name)) {
                                 m_Karma.put(name, 0);
                             }
                         }
                     }
                 }
 
                 @Override public void onRegistered() {
                 }
 
                 @Override public void onQuit(IRCUser a_user, String a_msg) {
                     if (m_Karma.containsKey(a_user.getNick())) {
                         m_Karma.remove(a_user.getNick());
                     }
                    if (a_user.getNick().equals("prettygreat")) {
                        m_Conn.doPrivmsg("#cs-york", "...dary");
                    }
                 }
 
                 @Override public void onPrivmsg(String a_target, IRCUser a_user, String a_msg) {
                     m_Log.log(Level.FINE, "Got privmsg from " + a_target);
                     System.out.println("PRIVMSG: " + a_msg);
                     if (a_target.equals(Constants.CHANNEL) && a_msg.contains("++")) {
                         String karmacipient = a_msg.substring(0, a_msg.lastIndexOf("++")).trim(); // get rid of any spaces between '++' and the preceeding word
                         karmacipient = karmacipient.replaceAll("[#:,.!\"$%&*()?+/]", ""); // Clear out characters that aren't allowed in nicknames
                         if (karmacipient.contains(" ")) { // get the last word before the '++'
                             karmacipient = karmacipient.substring(karmacipient.lastIndexOf(" ") + 1).trim();
                         }
                         if (m_Karma.containsKey(karmacipient)) {
                             m_Karma.put(karmacipient, ((int) m_Karma.get(karmacipient)) + 1);
                             m_Conn.doPrivmsg(Constants.CHANNEL, karmacipient + " karma = " + m_Karma.get(karmacipient));
                         }
                     } else if (a_target.equals(Constants.CHANNEL) && a_msg.contains("--")) {
                         String karmacipient = a_msg.substring(0, a_msg.lastIndexOf("--")).trim(); // get rid of any spaces between '--' and the preceeding word
                         karmacipient = karmacipient.replaceAll("[#:,.!\"$%&*()?+/]", ""); // Clear out characters that aren't allowed in nicknames
                         if (karmacipient.contains(" ")) { // get the last word before the '--'
                             karmacipient = a_msg.substring(karmacipient.lastIndexOf(" ") + 1).trim();
                         }
                         if (m_Karma.containsKey(karmacipient)) {
                             m_Karma.put(karmacipient, ((int) m_Karma.get(karmacipient)) - 1);
                             m_Conn.doPrivmsg(Constants.CHANNEL, karmacipient + " karma = " + m_Karma.get(karmacipient));
                         }
                     }
 
                     // Actions
                     if ((a_target.equals(Constants.NICK_NAME) || a_target.equals(Constants.CHANNEL)) && a_msg.startsWith("!")) {
                         if (a_msg.startsWith("!karma")) {
                             String karmacipient = a_msg.split("[ \t]")[1];
                             if (m_Karma.containsKey(karmacipient)) {
                                 String target;
                                 if (a_target.equals(Constants.NICK_NAME)) {
                                     target = a_user.getNick();
                                 } else {
                                     target = Constants.CHANNEL;
                                 }
                                 m_Conn.doPrivmsg(target, karmacipient + " karma = " + m_Karma.get(karmacipient));
                             }
                         }
                     }
                 }
 
                 @Override public void onPing(String a_ping) {
                 }
 
                 @Override public void onPart(String a_chan, IRCUser a_user, String a_msg) {
                     if (m_Karma.containsKey(a_user.getNick())) {
                         m_Karma.remove(a_user.getNick());
                     }
                     if (a_user.getNick().equals("prettygreat") && a_chan.equals("#cs-york")) {
                         m_Conn.doPrivmsg("#cs-york", "...dary");
                     }
                 }
 
                 @Override public void onNotice(String a_target, IRCUser a_user, String a_msg) {
                     System.out.println("NOTICE: " + a_msg);
                 }
 
                 @Override public void onNick(IRCUser a_user, String a_newNick) {
                     int value = 0;
                     if (m_Karma.containsKey(a_user.getNick())) {
                         value = m_Karma.remove(a_user.getNick());
                     }
                     m_Karma.put(a_newNick, value);
                 }
 
                 @Override public void onMode(IRCUser a_user, String a_passiveNick, String a_mode) {
                 }
 
                 @Override public void onMode(String a_chan, IRCUser a_user, IRCModeParser a_modeParser) {
                 }
 
                 @Override public void onKick(String a_chan, IRCUser a_user, String a_passiveNick, String a_msg) {
                     if (m_Karma.containsKey(a_passiveNick)) {
                         m_Karma.remove(a_passiveNick);
                     }
                 }
 
                 @Override public void onJoin(String a_chan, IRCUser a_user) {
                     System.out.println("JOIN: " + a_user.getNick());
                     if (!m_Karma.containsKey(a_user.getNick())) {
                         m_Karma.put(a_user.getNick(), 0);
                     }
                 }
 
                 @Override public void onInvite(String a_chan, IRCUser a_user, String a_passiveNick) {
                     System.out.println("INVITE: " + a_user.getNick());
                     if (!m_Karma.containsKey(a_user.getNick())) {
                         m_Karma.put(a_user.getNick(), 0);
                     }
                 }
 
                 @Override public void onError(int a_num, String a_msg) {
                     System.out.println("ERROR: " + a_msg);
                 }
 
                 @Override public void onError(String a_msg) {
                     System.out.println("ERROR: " + a_msg);
                 }
 
                 @Override public void onDisconnected() {
                     System.out.println("DISCO");
                 }
             });
 
         } catch (IOException ex) {
             m_Log.log(Level.WARNING, "Couldn't connect to server " + Constants.HOST);
         }
     }
 
 }
