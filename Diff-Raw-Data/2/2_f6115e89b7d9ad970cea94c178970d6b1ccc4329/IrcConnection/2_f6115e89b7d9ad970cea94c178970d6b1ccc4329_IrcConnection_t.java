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
 
 package erki.xpeter.con.irc;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import erki.api.util.Log;
 import erki.xpeter.Bot;
 import erki.xpeter.con.Connection;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.Message;
 import erki.xpeter.msg.NickChangeMessage;
 import erki.xpeter.msg.RawMessage;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.msg.UserJoinedMessage;
 import erki.xpeter.msg.UserLeftMessage;
 import erki.xpeter.util.Delay;
 
 /**
  * This class uses the PircBot api to enable the bot to join IRC servers.
  * 
  * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
  */
 public class IrcConnection extends PircBot implements Connection {
     
     private Bot bot;
     
     private String host, channel;
     
     private int port;
     
     private Queue<Message> sendQueue = new LinkedList<Message>();
     
     private boolean reconnect = false;
     
     private LinkedList<String> userList = new LinkedList<String>();
     
     public IrcConnection(Bot bot, String host, int port, String channel, String nick) {
         this.bot = bot;
         this.host = host;
         this.channel = channel;
         this.port = port;
         setName(nick);
     }
     
     @Override
     public String getShortId() {
         return channel;
     }
     
     @Override
     public Collection<String> getUserList() {
         LinkedList<String> list = new LinkedList<String>();
         list.addAll(userList);
         return list;
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
     public void run() {
         boolean pause = false;
         
         while (true) {
             
             try {
                 Log.info("Trying to connect to " + host + ":" + port + ".");
                 connect(host, port);
                 Log.info("Connection established. Joining " + channel + ".");
                 joinChannel(channel);
                 Log.info("Channel joined. Waiting for messages.");
                reconnect = false;
                pause = false;
                 
                 while (!reconnect) {
                     
                     synchronized (sendQueue) {
                         
                         while (!sendQueue.isEmpty()) {
                             Message msg = sendQueue.poll();
                             Log.debug("Sending " + msg + " to the server.");
                             
                             if (msg instanceof RawMessage) {
                                 sendRawLine(msg.getText());
                             } else {
                                 
                                 if (msg.getText().contains("\n")) {
                                     
                                     for (String line : msg.getText().split("\n")) {
                                         sendMessage(channel, line);
                                     }
                                     
                                 } else {
                                     sendMessage(channel, msg.getText());
                                 }
                             }
                         }
                     }
                 }
                 
             } catch (NickAlreadyInUseException e) {
                 Log.error(e);
             } catch (IOException e) {
                 Log.error(e);
             } catch (IrcException e) {
                 Log.error(e);
             } catch (Throwable e) {
                 // See that _everything_ goes to the log.
                 Log.error(e);
             } finally {
                 disconnect();
                 
                 if (pause) {
                     Log.info("Lost connection to IRC server. Trying to reconnect in 5 min.");
                     
                     try {
                         Thread.sleep(300000);
                     } catch (InterruptedException e) {
                     }
                     
                 } else {
                     pause = true;
                     Log.info("Lost connection to IRC server. Trying to reconnect.");
                 }
             }
         }
     }
     
     @Override
     protected void onJoin(String channel, String sender, String login, String hostname) {
         super.onJoin(channel, sender, login, hostname);
         userList.add(sender);
         bot.process(new UserJoinedMessage(sender, this));
         Log.debug(sender + " has joined the chat.");
     }
     
     @Override
     protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
         super.onNickChange(oldNick, login, hostname, newNick);
         userList.remove(oldNick);
         userList.add(newNick);
         Log.debug(oldNick + " is now known as " + newNick + ".");
         bot.process(new NickChangeMessage(oldNick, newNick, this));
     }
     
     @Override
     protected void onPart(String channel, String sender, String login, String hostname) {
         super.onPart(channel, sender, login, hostname);
         userList.remove(sender);
         bot.process(new UserLeftMessage(sender, "", this));
         Log.debug(sender + " has left.");
     }
     
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname,
             String message) {
         super.onMessage(channel, sender, login, hostname, message);
         Log.info("Received from server: " + message + ".");
         bot.process(new TextMessage(sender, message, this));
     }
     
     @Override
     protected void onUserList(String channel, User[] users) {
         super.onUserList(channel, users);
         
         for (User user : users) {
             userList.add(user.getNick());
         }
     }
     
     @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin,
             String kickerHostname, String recipientNick, String reason) {
         super.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
         quitServer();
     }
     
     @Override
     protected void onDisconnect() {
         super.onDisconnect();
         
         synchronized (sendQueue) {
             reconnect = true;
             sendQueue.notify();
         }
     }
 }
