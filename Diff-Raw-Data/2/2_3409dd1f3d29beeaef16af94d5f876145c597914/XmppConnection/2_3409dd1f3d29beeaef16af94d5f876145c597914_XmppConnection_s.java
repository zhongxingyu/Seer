 /*
  * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.con.xmpp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.SASLAuthentication;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.muc.DiscussionHistory;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 import erki.api.util.Log;
 import erki.xpeter.Bot;
 import erki.xpeter.con.Connection;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.Message;
 import erki.xpeter.util.Delay;
 
 /**
  * Establishes a connection to an XMPP server. If the connection is lost it tries to reconnect from
  * time to time. Messages that are to be sent to the server are buffered in case the connection is
  * lost and sent if the connection is re-established.
  * 
  * @author Edgar Kalkowski
  */
 public class XmppConnection implements Connection {
     
     private static final String PASS_FILE = "config" + File.separator + "passwd.xmpp";
     
     /*
      * Queue messages if the connection is not ready at the moment. The queue is also used as a lock
      * to synchronize access to itself.
      */
     private Queue<Message> sendQueue = new LinkedList<Message>();
     
     private String host, channel, nick, loginName, password;
     
     private int port;
     
     private Bot bot;
     
     private XMPPConnection con;
     
     private ParticipantStatusListener statusListener;
     
     private WatchDog dog;
     
     /**
      * Create a new XmppConnection to an XMPP server.
      * 
      * @param host
      *        The hostname of the XMPP server to connect to.
      * @param port
      *        The port to connect to.
      * @param channel
      *        The channel to join once connected.
      * @param nick
      *        The nickname to use when joining the channel.
      */
     public XmppConnection(Bot bot, String host, int port, String channel, String nick) {
         this.bot = bot;
         this.host = host;
         this.port = port;
         this.channel = channel;
         this.nick = nick;
         dog = new WatchDog(this);
         dog.start();
         
         // There seems to be a bug in there so better disable it.
         SASLAuthentication.unsupportSASLMechanism("DIGEST-MD5");
     }
     
     @Override
     public void run() {
         boolean pause = false;
         
         while (true) {
             ConnectionListener connectionListener = null;
             
             try {
                 Log.info("Connecting to " + channel + "@" + host + ":" + port + ".");
                 ConnectionConfiguration config = new ConnectionConfiguration(host, port);
                 con = new XMPPConnection(config);
                 con.connect();
                 
                 if (con.isConnected()) {
                     Log.info("Connection established. Logging in.");
                     load();
                     con.login(loginName, password, "Daheim");
                     // Get the password out of memory asap.
                     loginName = null;
                     password = null;
                     System.gc();
                     
                     MultiUserChat chat = new MultiUserChat(con, channel);
                     PacketListener packetListener = new PacketListener(this, bot, dog);
                     // chat.addInvitationRejectionListener(new InvitationRejectionListener());
                     chat.addMessageListener(packetListener);
                     // chat.addParticipantListener(packetListener);
                     statusListener = new ParticipantStatusListener(this, bot, dog);
                     chat.addParticipantStatusListener(statusListener);
                     // chat.addPresenceInterceptor(new PresenceInterceptor());
                     // chat.addSubjectUpdatedListener(new SubjectUpdatedListener());
                     chat.addUserStatusListener(new UserStatusListener(this));
                     // MultiUserChat.addInvitationListener(con, new InvitationListener());
                     connectionListener = new ConnectionListener(this);
                     con.addConnectionListener(connectionListener);
                     
                     // We don’t want the bot to react on old stuff when he joins.
                     DiscussionHistory history = new DiscussionHistory();
                     history.setMaxStanzas(0);
                     
                     Log.info("Logged in. Joining chat.");
                     chat.join(nick, null, history, SmackConfiguration.getPacketReplyTimeout());
                     pause = false;
                     
                     // Wait for messages to send.
                     while (con.isConnected()) {
                         
                         synchronized (sendQueue) {
                             
                             while (!sendQueue.isEmpty()) {
                                 Message msg = sendQueue.poll();
                                 Log.info("Sending " + msg + " to the server.");
                                 chat.sendMessage(msg.getText());
                             }
                             
                             try {
                                 sendQueue.wait();
                             } catch (InterruptedException e) {
                             }
                         }
                     }
                 }
                 
             } catch (XMPPException e) {
                 Log.error(e);
             } catch (Throwable e) {
                 // See that _everything_ goes to the log.
                 Log.error(e);
             } finally {
                 
                 if (con != null) {
                     
                     if (connectionListener != null) {
                         con.removeConnectionListener(connectionListener);
                     }
                     
                     con.disconnect();
                 }
                 
                 if (pause || !pause) {
                     Log.info("Lost connection to XMPP server. Trying to reconnect in 5 min.");
                     
                     try {
                         Thread.sleep(300000);
                     } catch (InterruptedException e) {
                     }
                     
                 } else {
                     pause = true;
                     Log.info("Lost connection to XMPP server. Trying to reconnect.");
                 }
             }
         }
     }
     
     /**
      * Used by {@link UserStatusListener} to trigger a reconnect if re-joining the chat room after a
      * kick resulted in some XMPP error (see there for more info).
      */
     public void reconnect() {
         
         synchronized (sendQueue) {
             
            if (con != null) {
                 con.disconnect();
             }
             
             sendQueue.notify();
         }
     }
     
     private void load() {
         
         try {
             Log.debug("Trying to load password from " + PASS_FILE + ".");
             BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(
                     PASS_FILE), "UTF-8"));
             String line;
             
             while ((line = fileIn.readLine()) != null) {
                 
                 if (line.toUpperCase().startsWith("USER=")) {
                     loginName = line.substring("USER=".length());
                 } else if (line.toUpperCase().startsWith("PASSWORD=")) {
                     password = line.substring("PASSWORD=".length());
                 }
             }
             
             Log.debug("Password file found and successfully parsed.");
         } catch (UnsupportedEncodingException e) {
             throw new Error(e);
         } catch (FileNotFoundException e) {
             Log.warning("Password file " + PASS_FILE + " could not be found.");
             Log.info("Trying to continue but this is likely not to work!");
             loginName = nick;
             password = "default";
         } catch (IOException e) {
             throw new Error(e);
         }
     }
     
     @Override
     public void send(final Message msg) {
         
         if (msg instanceof DelayedMessage) {
             
             new Delay((DelayedMessage) msg) {
                 
                 @Override
                 public void delayedAction() {
                     
                     synchronized (sendQueue) {
                         sendQueue.offer(msg);
                         sendQueue.notify();
                     }
                 }
                 
             }.start();
             
         } else {
             
             synchronized (sendQueue) {
                 sendQueue.offer(msg);
                 sendQueue.notify();
             }
         }
     }
     
     @Override
     public String getNick() {
         return nick;
     }
     
     @Override
     public String getShortId() {
         return channel.substring(0, channel.lastIndexOf('@'));
     }
     
     @Override
     public String toString() {
         return "Connection(xmpp://" + channel + "@" + host + ":" + port + ")";
     }
     
     @Override
     public Collection<String> getUserList() {
         return statusListener.getUserList();
     }
 }
