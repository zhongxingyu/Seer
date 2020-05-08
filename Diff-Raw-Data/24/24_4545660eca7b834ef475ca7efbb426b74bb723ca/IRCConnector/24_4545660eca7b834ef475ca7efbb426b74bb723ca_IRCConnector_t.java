 /*
  * roomstore - an irc journaller using cassandra.
  *
  * Copyright 2011-2012 MeBigFatGuy.com
  * Copyright 2011-2012 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.roomstore;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class IRCConnector {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(IRCConnector.class);
     private CassandraWriter writer;
     private CasBot casBot;
     private String server;
     private String[] channels;
 
     public IRCConnector(String nickName, String ircServer, String[] ircChannels) {
         casBot = new CasBot(nickName);
         server = ircServer;
         channels = ircChannels;
     }
 
     public void setWriter(CassandraWriter cassandraWriter) {
         writer = cassandraWriter;
     }
 
     public void startRecording() throws IrcException, IOException {
         boolean started = false;
         while (!started) {
             try {
                 casBot.connect(server);
                 for (String channel : channels) {
                     if (!channel.startsWith("#")) {
                         channel = '#' + channel;
                     }
                     casBot.joinChannel(channel);
                     started = true;
                 }
             } catch (NickAlreadyInUseException naiue) {
                 String name = casBot.getName();
                 name += String.valueOf((int) (Math.random() * 10));
                 casBot.rename(name);
             }
         }
     }
 
     private class CasBot extends PircBot {
         public CasBot(String nick) {
             setName(nick);
             setLogin(nick);
             setFinger(nick);
             setVersion(nick);
         }
 
         public void rename(String nick) {
             super.setName(nick);
         }
 
         public void onMessage(String channel, String sender, String login, String hostname, String message) {
             try {
                 String[] msgParts = message.split("\\s+");
                 if (msgParts.length >= 2) {
                     if ("~".equals(msgParts[0])) {
                         if ("help".equalsIgnoreCase(msgParts[1])) {
                             sendMessage(sender, "roomstore - https://github.com/mebigfatguy/roomstore");
                             sendMessage(sender, "~ help                  -- this message");
                             sendMessage(sender, "~ seen user             -- show last time user said something if available");
                             sendMessage(sender, "~ today                 -- see messages from today");
                             sendMessage(sender, "~ date MM/yy/dddd       -- see messages from date");
                         } else if ((msgParts.length >= 3) && "seen".equalsIgnoreCase(msgParts[1])) {
                             String user = msgParts[2].trim();
                             Message msg = writer.getLastMessage(channel,  user);
                             if (msg != null) {
                                 sendMessage(sender,  user + " last seen " + DateFormat.getInstance().format(msg.getTime()) + " saying: " + msg.getMessage());
                             }
                         } else if ("today".equalsIgnoreCase(msgParts[1])) {
                             Calendar dayCal = Calendar.getInstance();
                             dayCal.set(Calendar.HOUR_OF_DAY, 0);
                             dayCal.set(Calendar.MINUTE, 0);
                             dayCal.set(Calendar.SECOND, 0);
                             dayCal.set(Calendar.MILLISECOND, 0);
                             
                             List<Message> msgs = writer.getMessages(channel, dayCal.getTime());
                             for (Message m : msgs) {
                                 sendMessage(sender,  m.getSender() + ": " + DateFormat.getInstance().format(m.getTime()) + ": " + m.getMessage());
                             }
                         } else if ((msgParts.length >= 3) && "date".equalsIgnoreCase(msgParts[1])) {
                             SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                             Date day = sdf.parse(msgParts[2]);
                             
                             List<Message> msgs = writer.getMessages(channel, day);
                             for (Message m : msgs) {
                                 sendMessage(sender,  m.getSender() + ": " + DateFormat.getInstance().format(m.getTime()) + ": " + m.getMessage());
                             }
                         }
                         return;
                     }
                 }
                 writer.addMessage(channel, sender, hostname, message);
 
             } catch (Exception e) {
                LOGGER.error(String.format("Failed processing message on channel %s for user %s - %s", channel, sender, message));
             }
         }
 
         @Override
         protected void onDisconnect() {
             Thread t = new Thread(new Runnable() {
                 public void run() {
                     long sleepTime = 2000;
                     while (!Thread.interrupted()) {
                         try {
                             Thread.sleep(sleepTime);
                             sleepTime *= 1.5;
                             if (sleepTime > 60000) {
                                 sleepTime = 60000;
                             }
 
                             casBot.connect(server);
                             for (String channel : channels) {
                                 if (!channel.startsWith("#")) {
                                     channel = '#' + channel;
                                 }
                                 casBot.joinChannel(channel);
                             }
                             return;
                         } catch (Exception e) {
                         }
                     }
                 }
             });
             t.start();
         }
 
     }
 }
