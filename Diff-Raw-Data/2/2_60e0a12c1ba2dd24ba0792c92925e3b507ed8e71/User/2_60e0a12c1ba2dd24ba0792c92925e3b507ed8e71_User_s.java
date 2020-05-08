 /*
  * Copyright (C) 2013 Lord_Ralex
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.lordralex.ralexbot.api.users;
 
 import com.lordralex.ralexbot.api.Utilities;
 import com.lordralex.ralexbot.api.channels.Channel;
 import com.lordralex.ralexbot.api.sender.Sender;
 import com.lordralex.ralexbot.permissions.Permissible;
 import com.lordralex.ralexbot.permissions.Permission;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.pircbotx.hooks.WaitForQueue;
 import org.pircbotx.hooks.events.WhoisEvent;
 
 /**
  *
  * @author Joshua
  */
 public class User extends Utilities implements Sender, Permissible {
 
     protected static final Map<String, User> userList = new ConcurrentHashMap<>();
     protected final org.pircbotx.User pircbotxUser;
 
     public User(String nick) {
         pircbotxUser = bot.getUser(nick);
     }
 
     public User(org.pircbotx.User u) {
         pircbotxUser = u;
     }
 
     public User(org.pircbotx.UserSnapshot snap) {
         pircbotxUser = snap;
     }
 
     public static User getUser(String username) {
         User user = userList.get(username);
         if (user == null) {
             user = new User(username);
             userList.put(username, user);
         }
         return user;
     }
 
     @Override
     public void sendMessage(String message) {
         pircbotxUser.sendMessage(message);
     }
 
     @Override
     public void sendNotice(String message) {
         bot.sendNotice(pircbotxUser, message);
     }
 
     @Override
     public void sendMessage(String[] messages) {
         for (String message : messages) {
             sendMessage(message);
         }
     }
 
     @Override
     public void sendNotice(String[] messages) {
         for (String message : messages) {
             sendNotice(message);
         }
     }
 
     public boolean hasVoice(String channel) {
         return pircbotxUser.getChannelsVoiceIn().contains(bot.getChannel(channel));
     }
 
     public boolean hasOP(String channel) {
         return pircbotxUser.getChannelsOpIn().contains(bot.getChannel(channel));
     }
 
     public String isVerified() {
         String name = null;
         try (WaitForQueue queue = new WaitForQueue(pircbotxUser.getBot())) {
             WhoisEvent evt;
             try {
                 pircbotxUser.getBot().sendRawLineNow("whois " + pircbotxUser.getNick());
                 while (true) {
                     evt = queue.waitFor(WhoisEvent.class);
                     if (evt.getNick().equals(this.pircbotxUser.getNick())) {
                         name = evt.getRegisteredAs();
                         break;
                     }
                 }
             } catch (InterruptedException ex) {
                 Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
                 name = null;
             }
             if (name != null && name.isEmpty()) {
                 name = null;
             }
         }
         return name;
     }
 
     public String[] getChannels() {
        Channel[] chanArray = pircbotxUser.getChannels().toArray(new Channel[0]);
         String[] channelList = new String[chanArray.length];
         for (int i = 0; i < channelList.length; i++) {
             channelList[i] = chanArray[i].getName();
         }
         return channelList;
     }
 
     public String getIP() {
         return pircbotxUser.getHostmask();
     }
 
     public String getNick() {
         return pircbotxUser.getNick();
     }
 
     @Override
     public boolean hasPermission(Permission perm) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addPermission(Permission perm) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addPermission(Permission perm, boolean val) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void removePermission(Permission perm) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Map<Permission, Boolean> getPermissions() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
