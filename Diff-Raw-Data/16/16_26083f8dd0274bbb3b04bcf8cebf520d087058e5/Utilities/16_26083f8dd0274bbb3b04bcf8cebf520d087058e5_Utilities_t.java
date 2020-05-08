 /*
     Copyright (C) 2013 Yizhe Shen <brrr@live.ca>
 
     This file is part of ircutil.
 
     ircutil is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ircutil is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ircutil.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ircutil;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 import org.pircbotx.*;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.NoticeEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 /**
  * A set of useful functions for an IRC bot.
  * @author Yizhe Shen
  */
 public class Utilities extends ListenerAdapter<PircBotX>{
 	
     private PircBotX bot;
     private char commandChar;
     private long startTime;
     private ArrayList<String> awayList;
     private ArrayList<String> adminList;
     private ArrayList<String> simpleList;
     private ArrayList<CloneBot> cloneList;
     Random randGen;
     
     public Utilities(PircBotX parent, char commChar){
         bot = parent;
         commandChar = commChar;
         startTime = System.currentTimeMillis();
         randGen = new Random();
         awayList = loadHostList("away.txt");
         adminList = loadHostList("admins.txt");
         simpleList = loadHostList("simple.txt");
         cloneList = new ArrayList<CloneBot>();
     }
 
     @Override
     public void onNotice (NoticeEvent<PircBotX> event){
         String msg = event.getMessage();
         
         // Check if it's a CTCP reply
         if (msg.startsWith("\u0001") && msg.endsWith("\u0001")){
             processCTCPReply(bot, event.getUser(), msg);
         }
     }
     
     @Override
     public void onMessage(MessageEvent<PircBotX> event){
         String msg = event.getMessage();
         
         // Parse the message if it is a command
         if (msg.length() > 1 && msg.charAt(0) == commandChar && msg.charAt(1) != ' ') {
             msg = msg.substring(1);
             StringTokenizer st = new StringTokenizer(msg);
             String command = st.nextToken().toLowerCase();
             String[] params = new String[st.countTokens()];
             for (int ctr = 0; ctr < params.length; ctr++){
                 params[ctr] = st.nextToken();
             }
             processCommand(event.getChannel(), event.getUser(), command, params);
         }
     }
     
     @Override
     public void onPrivateMessage (PrivateMessageEvent<PircBotX> event){
         String msg = event.getMessage();
         
         // Parse the private message
         StringTokenizer st = new StringTokenizer(msg);
         String command = st.nextToken().toLowerCase();
         String[] params = new String[st.countTokens()];
         for (int ctr = 0; ctr < params.length; ctr++){
             params[ctr] = st.nextToken();
         }
         processPM(event.getUser(), command, params, msg);
     }
     
     /**
      * Processes a CTCP reply from a user.
      * 
      * @param bot the bot the caught the reply
      * @param user the User that sent the reply
      * @param msg the CTCP message
      */
     public void processCTCPReply(PircBotX bot, User user, String msg){
         StringTokenizer st = new StringTokenizer(msg, "\u0020\u0001");
         String[] params = new String[st.countTokens()];
         for (int ctr = 0; ctr < params.length; ctr++){
             params[ctr] = st.nextToken();
         }
         
         if (params[0].equals("PING")){
             informUser(user, "Lag: " + formatPing((double) (System.currentTimeMillis() - Long.parseLong(params[1])) / 1000) + " seconds");
         }
     }
     
     /**
      * Processes messages given to the the bot through PM. These commands
      * should be accessible only by admins.
      * 
      * @param user the User that sent the command
      * @param command the command
      * @param params the command parameters
      * @param origMsg the entire original message
      */
     public void processPM(User user, String command, String[] params, String origMsg){
         // Check if the user is an admin
         if (!isAdmin(user)){
            // Do nothing to prevent potential spamming.
            //informUser(user, "You are not authorized to make this command.");
         
         // Join a specified channel
         } else if (command.equals("join")){
             if (params.length < 1) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 joinChannel(user, params[0]);
             }
             
         // Part a specified channel
         } else if (command.equals("part") || command.equals("leave")){
             if (params.length < 1) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 partChannel(user, params[0]);
             }
         
         // Op the specified user in the specified channel
         } else if (command.equals("op")){
             if (params.length < 2){
                 informUser(user, "Missing parameter(s).");
             } else {
                 opUser(user, params[0], params[1]);
             }
             
         // Deop the specified user in the specified channel
         } else if (command.equals("deop")) {
             if (params.length < 2){
                 informUser(user, "Missing parameter(s).");
             } else {
                 deOpUser(user, params[0], params[1]);
             }
             
         // Voice the specified user in the specified channel
         } else if (command.equals("voice")) {
             if (params.length < 2){
                 informUser(user, "Missing parameter(s).");
             } else {
                 voiceUser(user, params[0], params[1]);
             }
             
         // Devoice the specified user in the specified channel
         } else if (command.equals("devoice")) {
             if (params.length < 2){
                 informUser(user, "Missing parameter(s).");
             } else {
                 deVoiceUser(user, params[0], params[1]);
             }
             
         // Adds a bot admin
         } else if (command.equals("addadmin")){
             if (params.length < 1){
                 informUser(user, "Missing parameter(s).");
             } else {
                 addAdmin(user, params[0]);
             }
         
         // Removes a bot admin
         } else if (command.equals("removeadmin")) {
          // Check if we have enough parameters
             if (params.length < 1){
                 informUser(user, "Missing parameter(s).");
             } else {
                 removeAdmin(user, params[0]);
             }
             
         // Gets the bot to say a message to a specified recipient
         } else if (command.equals("say") || command.equals("echo")){
             if (params.length < 2) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 // params[0] == recipient
                 int msgLoc = origMsg.toLowerCase().indexOf(params[0]) + params[0].length() + 1;
                 bot.sendMessage(params[0], origMsg.substring(msgLoc));
             }
         
         // Gets the bot to notice a message to a specified recipient
         } else if (command.equals("notice")) {
             if (params.length < 2) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 // params[0] == recipient
                 int msgLoc = origMsg.toLowerCase().indexOf(params[0]) + params[0].length() + 1;
                 bot.sendNotice(params[0], origMsg.substring(msgLoc)); 
             }
         
         // Gets the bot to send an action to the specified recipient
         } else if (command.equals("action")) {
             if (params.length < 2) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 // params[0] == recipient
                 int msgLoc = origMsg.toLowerCase().indexOf(params[0]) + params[0].length() + 1;
                 bot.sendAction(params[0], origMsg.substring(msgLoc)); 
             }
             
         // Gets the bot to send a raw line
         } else if (command.equals("raw")){
             int msgLoc = origMsg.toLowerCase().indexOf(command) + command.length() + 1;
             bot.sendRawLine(origMsg.substring(msgLoc));
             
         // Erases all hostmasks from away.txt
         } else if (command.equals("resetaway")){
             awayList.clear();
             saveHostList("away.txt", awayList);
             informUser(user, "The away list has been emptied.");
         
         // Erases all hosts from simple.txt
         } else if (command.equals("resetsimple")) {
             simpleList.clear();
             saveHostList("simple.txt", simpleList);
             informUser(user, "The simple list has been emptied.");
         
         // Adds a clone to the specified channel
         } else if (command.equals("addclone")) {
             if (params.length < 3) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 addClone(user, params[0], params[1], params[2]);
             }
             
         // Removes the specified clone
         } else if (command.equals("removeclone")) {
             if (params.length < 1) {
                 informUser(user, "Missing parameter(s).");
             } else {
                 removeClone(user, params[0]);
             }
         
         // Removes all clones
         } else if (command.equals("removeallclones")) {
             removeAllClones(user);
         }
     }
     
     /**
      * Process a command.
      * 
      * @param channel the originating channel of the command
      * @param user the user who made the command
      * @param command the command
      * @param params the parameters after the command
      */
     public void processCommand(Channel channel, User user, String command, String[] params){    	 
         // Display current host time
         if (command.equals("time")){
             bot.sendMessage(channel, "Time: " + new Date().toString());
             
         // Display bot uptime
         } else if (command.equals("uptime")){
             showUpTime(channel);
             
         // Display channels to which the bot is connected
         } else if (command.equals("channels")){
             showChannels(channel);
             
         // Remove the user from the away list
         } else if (command.equals("back")){
             if (isUserAway(user)){
                 toggleUserAway(user);
                 informUser(user, "You are no longer marked as away.");
             } else {
                 informUser(user, "You are not marked as away!");
             }
             
         // Add the user to the away list
         } else if (command.equals("away")){
             if (isUserAway(user)){
                 informUser(user, "You are already marked as away!");
             } else {
                 toggleUserAway(user);
                 informUser(user, "You are now marked as away.");
             }
         
         // Toggles the user's simple status
         } else if (command.equals("simple")) {
             /*if (isUserSimple(user)){
                 bot.sendNotice(user, "Private messages will now be sent via msg.");
             } else {
                 bot.sendNotice(user, "Private messages will now be sent via notice.");
             }*/
             toggleUserSimple(user);
             
         // Display a list of users in a channel excluding ChanServ, the bot
         } else if (command.equals("ping")){
             pingUsers(channel);
             
         // Display lag between the bot and user
         } else if (command.equals("lag")){
             bot.sendCTCPCommand(user, "PING " + System.currentTimeMillis());
             
         // Display the results of a coin flip
         } else if (command.equals("coin")){
             flipCoin(user, channel);
             
         // Displays greetings to user in channel
         } else if (command.equals("hi")){
             bot.sendMessage(channel, "Hi " + user.getNick() + "!");
             
         // Gives the user a cup of hot chocolate
         } else if (command.equals("cocoa")){
             if (params.length < 1) {
                 bot.sendAction(channel, "hands " + user.getNick() + " a cup of hot chocolate. Cheers!");
             } else {
                 if (isUserInChannel(channel, params[0])){
                     bot.sendAction(channel, "hands " + params[0] + " a cup of hot chocolate. Cheers!");
                 } else {
                     informUser(user, params[0] + " is not in " + channel.getName() + ". :(");
                 }
             }
             
         // Stokes the fireplace
         } else if (command.equals("stoke")){
             bot.sendAction(channel, "stokes the glowing embers of the fire.");
             
         // Displays a list of commands
         } else if (command.equals("commands")){
             bot.sendMessage(channel, "Commands: channels, time, uptime, lag, cocoa, stoke, away, back, simple, ping, coin, hi, help");
             if (isAdmin(user)){
                 informUser(user, "Admin Commands: say, notice, action, raw, join, part, op, deop, voice, devoice, admin, removeadmin, clearaway, clearsimple, addclone, removeclone, removeallclones");
             }
             
         // Displays a help message
         } else if (command.equals("help")){
             bot.sendMessage(channel, user.getNick()+": Read the topic. For a list of non-game commands, type .commands.");
         }
        
        // Temp command
        /*} else if (command.equals("colors")) {            
            for (int ctr = 0; ctr < 16; ctr++) {
                for (int ctr2 = 0; ctr2 < 16; ctr2++) {
                    bot.sendMessage(channel, "\u0003" + String.format("%02d,%02d.48766. People: Sarah ophelia colley cannon is better remembered as ?", ctr, ctr2));
                    try { Thread.sleep(1000); } catch (InterruptedException e){}
                }
                try { Thread.sleep(10000); } catch (InterruptedException e){}
            }
            
        }*/
     } 
     
     /**
      * Checks if a user is in a channel.
      * @param channel the channel to check
      * @param nick the user's nick
      * @return true if the user is found in the channel
      */
     private boolean isUserInChannel(Channel channel, String nick){
         Iterator<User> it = channel.getUsers().iterator();
         while(it.hasNext()){
             if (it.next().getNick().equalsIgnoreCase(nick)){
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Checks if the user is on the away list.
      * @param user the user to check
      * @return true if on the away list
      */
     private boolean isUserAway(User user) {
         return awayList.contains(user.getHostmask());
     }
     
     /**
      * Toggles the user's away status.
      * @param user the user to toggle
      */
     private void toggleUserAway(User user) {
         if (isUserAway(user)) {
             awayList.remove(user.getHostmask());
         } else {
             awayList.add(user.getHostmask());
         }
         saveHostList("away.txt", awayList);
     }
     
     /**
      * Checks if the user is on the simple list.
      * @param user the user to check
      * @return true if on the simple list
      */
     private boolean isUserSimple(User user) {
         return simpleList.contains(user.getHostmask());
     }
     
     /**
      * Toggles the user's simple status.
      * @param user the user to toggle
      */
     private void toggleUserSimple(User user) {
         if (isUserSimple(user)) {
             simpleList.remove(user.getHostmask());
         } else {
             simpleList.add(user.getHostmask());
         }
         saveHostList("simple.txt", simpleList);
     }
     
     /**
      * Gets the bot to join the specified channel.
      * @param user the command issuer
      * @param channel the channel to join
      */
     private void joinChannel(User user, String channel) {
         if (!channel.startsWith("#")){
             bot.joinChannel("#" + channel);
         } else {
             bot.joinChannel(channel);
         }
     }
     
     /**
      * Gets the bot to part the specified channel.
      * @param user the command issuer
      * @param channel the channel to part
      */
     private void partChannel(User user, String channel) {
         if (bot.channelExists(channel)){
             bot.partChannel(bot.getChannel(channel));
         } else {
             informUser(user, bot.getNick() + " is not in " + channel + ".");
         }
     }
     
     /**
      * Gives Op to a user.
      * @param user the command issuer
      * @param channel the channel in which to give Op
      * @param nick the user to Op
      */
     private void opUser(User user, String channel, String nick) {
         // Check if the bot is in the specified channel
         if (!bot.channelExists(channel)){
             informUser(user, bot.getNick() + " is not in " + channel + ".");
         } else {
             Channel tChannel = bot.getChannel(channel);
             // Check if the bot has Ops in that channel
             if (!tChannel.isOp(bot.getUserBot())){
                 informUser(user, bot.getNick() + " is not authorized to do this in " + channel + ".");
             } else {
                 // Check if the user to Op is in the channel
                 if (!isUserInChannel(tChannel, nick)){
                     informUser(user, nick + " is not in " + channel + ".");
                 } else {
                     User tUser = bot.getUser(nick);
                     bot.op(tChannel, tUser);
                 }
             }
         }
     }
     
     /**
      * Removes Op from a user.
      * @param user the command issuer
      * @param channel the channel in which to remove Op
      * @param nick the user to deOp
      */
     private void deOpUser(User user, String channel, String nick) {
         // Check if the bot is in the specified channel
         if (!bot.channelExists(channel)){
             informUser(user, bot.getNick() + " is not in " + channel + ".");
         } else {
             Channel tChannel = bot.getChannel(channel);
             // Check if the bot has Ops in that channel
             if (!tChannel.isOp(bot.getUserBot())){
                 informUser(user, bot.getNick() + " is not authorized to do this in " + channel + ".");
             } else {
                 // Check if the user to Op is in the channel
                 if (!isUserInChannel(tChannel, nick)){
                     informUser(user, nick + " is not in " + channel + ".");
                 } else {
                     User tUser = bot.getUser(nick);
                     bot.deOp(tChannel, tUser);
                 }
             }
         }
     }
     
     /**
      * Gives voice to a user.
      * @param user the command issuer
      * @param channel the channel in which to give voice
      * @param nick the user to voice
      */
     private void voiceUser(User user, String channel, String nick) {
         // Check if the bot is in the specified channel
         if (!bot.channelExists(channel)){
             informUser(user, bot.getNick() + " is not in " + channel + ".");
         } else {
             Channel tChannel = bot.getChannel(channel);
             // Check if the bot has Ops in that channel
             if (!tChannel.isOp(bot.getUserBot())){
                 informUser(user, bot.getNick() + " is not authorized to do this in " + channel + ".");
             } else {
                 // Check if the user to Op is in the channel
                 if (!isUserInChannel(tChannel, nick)){
                     informUser(user, nick + " is not in " + channel + ".");
                 } else {
                     User tUser = bot.getUser(nick);
                     bot.voice(tChannel, tUser);
                 }
             }
         }
     }
     
     /**
      * Removes voice from a user.
      * @param user the command issuer
      * @param channel the channel in which to remove voice
      * @param nick the user to deVoice
      */
     private void deVoiceUser(User user, String channel, String nick) {
         // Check if the bot is in the specified channel
         if (!bot.channelExists(channel)){
             informUser(user, bot.getNick() + " is not in " + channel + ".");
         } else {
             Channel tChannel = bot.getChannel(channel);
             // Check if the bot has Ops in that channel
             if (!tChannel.isOp(bot.getUserBot())){
                 informUser(user, bot.getNick() + " is not authorized to do this in " + channel + ".");
             } else {
                 // Check if the user to Op is in the channel
                 if (!isUserInChannel(tChannel, nick)){
                     informUser(user, nick + " is not in " + channel + ".");
                 } else {
                     User tUser = bot.getUser(nick);
                     bot.deVoice(tChannel, tUser);
                 }
             }
         }
     }
     
     /**
      * Adds a host to the admin list.
      * @param user the command issuer
      * @param nick the user to add
      */
     private void addAdmin(User user, String nick){
         // Find if user to add is in any of the channels the bot is in
         User tUser;
         Iterator<Channel> it = bot.getChannels().iterator();
         Iterator<User> it2;
         while(it.hasNext()){
             it2 = it.next().getUsers().iterator();
             while(it2.hasNext()){
                 tUser = it2.next();
                 // If we find the user, we can add them to the admin list
                 if (tUser.getNick().equalsIgnoreCase(nick)){
                     adminList.add(tUser.getHostmask());
                     saveHostList("admins.txt", adminList);
                     return;
                 }
             }
         }
         
         // If user is not in any channel to which the bot is joined
         informUser(user, nick + " was not found!");
     }
     
     /**
      * Removes a host from the admin list.
      * @param user the command issuer
      * @param nick the user to remove
      */
     private void removeAdmin(User user, String nick){
         // Find if user to remove is in any of the channels the bot is in
         User tUser;
         Iterator<Channel> it = bot.getChannels().iterator();
         Iterator<User> it2;
         while(it.hasNext()){
             it2 = it.next().getUsers().iterator();
             while(it2.hasNext()){
                 tUser = it2.next();
                 // If we find the user, we can remove them from the admin list
                 if (tUser.getNick().equalsIgnoreCase(nick)){
                     adminList.remove(tUser.getHostmask());
                     saveHostList("admins.txt", adminList);
                     return;
                 }
             }
         }
         
         // If user is not in any channel to which the bot is joined
         informUser(user, nick + " was not found!");
     }
     
     /**
      * Determines if a user is an admin for the bot.
      * @param user the user to check
      * @return true if the user is on the admin list
      */
     private boolean isAdmin (User user){
         return adminList.contains(user.getHostmask());
     }
     
     /**
      * Saves a list of hosts to the specified file.
      * @param file the file path
      * @param hostList ArrayList of hosts
      */
     private void saveHostList(String file, ArrayList<String> hostList){
         try {
             PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
             for (int ctr = 0; ctr < hostList.size(); ctr++){
                 out.println(hostList.get(ctr));
             }
             out.close();
         } catch (IOException e){
             bot.log("Error writing to " + file + "!");
         }      
     }
     
     /**
      * Loads a list of hosts from the specified file.
      * @param file the file path
      * @return ArrayList of hosts
      */
     private ArrayList<String> loadHostList(String file){
         ArrayList<String> hostList = new ArrayList<String>();
         try {
             BufferedReader in = new BufferedReader(new FileReader(file));
             while (in.ready()) {
                 hostList.add(in.readLine());
             }
             in.close();
             return hostList;
         } catch (IOException e){
             bot.log("Creating " + file + "...");
             saveHostList(file, hostList);
             return hostList; // return empty list if unable to read file
         }      
     }
     
     /**
      * Creates a clone in the specified channel via user command.
      * @param user the user who issued the command
      * @param nick the clone's nick
      * @param channel the channel for the clone to join
      * @param server the server to which the clone is to connect
      */
     public void addClone(User user, String nick, String channel, String server) {
         try {
             CloneBot newClone = new CloneBot(nick, channel);
             newClone.connect(server);
             cloneList.add(newClone);
         } catch (Exception e) {
             System.out.println("Error: " + e);
             informUser(user, "Error: " + e);
         }
     }
     
     /**
      * Disconnects a clone via user command.
      * @param user the user who issued the command
      * @param nick the clone's nick
      */
     public void removeClone(User user, String nick) {
         try {
             CloneBot cBot;
             for (int ctr = 0; ctr < cloneList.size(); ctr++) {
                 cBot = cloneList.get(ctr);
                 if (cBot.getNick().equalsIgnoreCase(nick)) {
                     cBot.quitServer("Bad clone.");
                     cloneList.remove(cBot);
                     break;
                 }
             }
         } catch (Exception e) {
             System.out.println("Error: " + e);
             informUser(user, "Error: " + e);
         }
     }
 
     /**
      * Disconnects all clones.
      */
     public void removeAllClones(User user) {
         try {
             CloneBot cBot;
             for (int ctr = 0; ctr < cloneList.size(); ctr++){
                 cBot = cloneList.get(ctr);
                 cBot.quitServer("Bad clone.");
                 cloneList.remove(cBot);
                 ctr--;
             }
         } catch (Exception e) {
             System.out.println("Error: " + e);
             informUser(user, "Error: " + e);
         }
     }
     
     /**
      * Sends a private message to the target user.
      * @param user the target
      * @param msg the message
      */
     public void informUser(User user, String msg) {
         if (isUserSimple(user)) {
             bot.sendNotice(user, msg);
         } else {
             bot.sendMessage(user, msg);
         }
     }
     
     /**
      * Displays the channels the bot is connected to.
      * @param channel the channel to send the message
      */
     public void showChannels(Channel channel) {
         String outStr = "Channels: ";
         Iterator<Channel> it = bot.getChannels().iterator();
         while(it.hasNext()){
             outStr += it.next().getName() + ", ";
         }
         bot.sendMessage(channel, outStr.substring(0, outStr.length()-2));
     }
     
     /**
      * Displays the amount of time since activation in dd:hh:mm:ss form.
      * @param channel the channel to display the information
      */
     public void showUpTime(Channel channel) {
         long d = (System.currentTimeMillis() - startTime)/1000;
         long seconds = d % 60;
         long minutes = (d / 60) % 60;
         long hours = (d / 3600) % 24;
         long days = d / 86400;
         bot.sendMessage(channel, "Uptime: "+String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds));
     }
     
     /**
      * Displays a string that highlights all the users not on the away list.
      * @param channel the channel to display the information
      */
     public void pingUsers(Channel channel) {
         // Grab the users in the channel
         User tUser;
         String tNick;
         String outStr = "Ping: ";
         Iterator<User> it = channel.getUsers().iterator();
         while(it.hasNext()){
             tUser = it.next();
             tNick = tUser.getNick();
             if (!tNick.equalsIgnoreCase("ChanServ") && 
                     !tNick.equalsIgnoreCase(bot.getNick()) &&
                     !awayList.contains(tUser.getHostmask())){
                 outStr += tNick+", ";
             }
         }
         bot.sendMessage(channel, outStr.substring(0, outStr.length()-2));
     }
     
     /**
      * Displays the flipping of a coin.
      * @param user the command issuer
      * @param channel the channel to display the information
      */
     public void flipCoin(User user, Channel channel) {
         int n = randGen.nextInt(2);
         String outStr = formatBold(user.getNick()) + " flips a coin... and it lands on ";
         if (n == 0){
             outStr += formatBold("tails") + ".";
         } else {
             outStr += formatBold("heads") + ".";
         }
         bot.sendMessage(channel, outStr);
     }
     
     /**
      * Returns a decimal number formatted as a String to 3 decimal places.
      * @param n the number
      * @return the formatted number as a String
      */
     private String formatPing(double n) {
         return String.format("%.3f", n);
     }
     
     /**
      * Returns the original string with IRC bold tags.
      * @param str the original string
      * @return the original string sandwiched with bold tags
      */
     private String formatBold(String str) {
         return Colors.BOLD + str + Colors.BOLD;
     }
 }
