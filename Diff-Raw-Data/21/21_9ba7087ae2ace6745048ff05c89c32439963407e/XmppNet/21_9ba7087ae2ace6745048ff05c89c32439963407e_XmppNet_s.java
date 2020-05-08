 /* 
  * Leks13
  * GPL v3
  */
 package ru.leks13.jabbertimer;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jivesoftware.smack.*;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.PacketExtension;
 import org.jivesoftware.smack.packet.Presence;
 
 public final class XmppNet {
 
     static ConnectionConfiguration connConfig;
     static XMPPConnection connection;
     public static String admin;
 
     public XmppNet(String status, String nick, String domain, String server, String password, String resource, int port) throws XMPPException, IOException {
 
         connConfig = new ConnectionConfiguration(server, port, domain);
         connection = new XMPPConnection(connConfig);
 
         try {
             int priority = 1;
             SASLAuthentication.supportSASLMechanism("PLAIN", 0);
             connection.connect();
             connection.login(nick, password, resource);
             Presence presence = new Presence(Presence.Type.available);
             presence.setStatus(status);
             presence.setPriority(priority);
             connection.sendPacket(presence);
 
         } catch (XMPPException ex) {
             System.out.println("Unable to connect");
         }
         getResultConnection();
     }
 
     public void whoIsAdmin(String adminB) {
         admin = adminB;
         System.out.println("Admin is " + admin);
     }
 
     public String getResultConnection() throws IOException {
         String result = null;
         if (connection.isConnected() == true && connection.isAuthenticated() == true) {
             result = "Connect to " + connection.getHost() + " \n " + "JID -  " + connection.getUser();
             System.out.println(result);
         }
         if (connection.isConnected() == true && connection.isAuthenticated() == false) {
             result = "Not connect to " + connection.getHost() + "!";
             System.out.println(result);
         }
         if (connection.isConnected() == false) {
             result = "Unable to connect";
             System.exit(0);
             System.out.println(result);
         }
         return result;
 
     }
 
     public static String getXmppRoster() {
         String msg = "";
         Roster roster = connection.getRoster();
         roster.reload();
         Collection<RosterEntry> entries = roster.getEntries();
         Iterator it = entries.iterator();
         while (it.hasNext()) {
             msg = msg + "\n" + it.next().toString();
         }
 
         return msg;
     }
 
     public static void disconnect() throws IOException {
         connection.disconnect();
         if (connection.isConnected() == false) {
             System.out.println("Offline");
         }
         System.exit(0);
     }
 
     public static void sendMessage(String to_jid, String msg) throws XMPPException {
         if (connection.isConnected() == false) {
             throw new XMPPException("Not connected");
         }
 
         ChatManager chatmanager = connection.getChatManager();
         Chat newChat = chatmanager.createChat(to_jid, new MessageListener() {
             @Override
             public void processMessage(Chat chat, Message message) {
             }
         });
         newChat.sendMessage(msg);
 
     }
 
     public static void parseMessagePacket(Message message) throws XMPPException, IOException {
         try {
             String jid = message.getFrom();
             String msg = message.getBody();
             boolean ans;
             ans = UserCommand.doUserCommand(msg, jid, admin);
 
             if (ans == false) {
                 sendMessage(jid, "!help - list of commands");
             }
         } catch (NumberFormatException | ClassNotFoundException | SQLException | ParseException ex) {
             Logger.getLogger(XmppNet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void incomeChat() throws InterruptedException {
         PacketFilter filterMessage = new AndFilter(new PacketTypeFilter(Message.class));
         PacketCollector myMessageCollector = connection.createPacketCollector(filterMessage);
 
         PacketListener messageListener = new PacketListener() {
             @Override
             public void processPacket(Packet ppacket) {
                 Message packet = (Message) ppacket;
                 try {
                     try {
                         parseMessagePacket((Message) ppacket);
                     } catch (IOException ex) {
                         Logger.getLogger(XmppNet.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } catch (XMPPException ex) {
                     Logger.getLogger(XmppNet.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 packet.removeExtension((PacketExtension) packet);
 
             }
         };
         connection.addPacketListener(messageListener, filterMessage);
     }
 }
