 package net.invisioncraft.plugins.salesmania.configuration;
 
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 5/16/12
  * Time: 7:29 PM
  */
 /*
 Copyright 2012 Byte 2 O Software LLC
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 public class Locale extends Configuration {
     private String localeName;
     private ArrayList<CommandSender> userCache;
     protected Locale(Salesmania plugin, String locale) {
         super(plugin, locale + ".yml");
         localeName = locale;
        userCache = new ArrayList<CommandSender>();
     }
 
     public String getMessage(String path) {
         if(getConfig().contains(path)) return getConfig().getString(path).
                 replace("&", String.valueOf(ChatColor.COLOR_CHAR));
         else return "Locale message not found.";
     }
 
     public ArrayList<String> getMessageList(String path) {
         ArrayList<String> messageList = new ArrayList<String>();
         for(String message : getConfig().getStringList(path)) {
             messageList.add(message.replace("&", String.valueOf(ChatColor.COLOR_CHAR)));
         }
         return messageList;
     }
 
     public String getName() {
         return localeName;
     }
 
     public void addUser(CommandSender user) {
         userCache.add(user);
     }
 
     public void removeUser(CommandSender user) {
         userCache.remove(user);
     }
 
     public CommandSender[] getUsers() {
         return userCache.toArray(new Player[0]);
     }
 
     public void broadcastMessage(String message) {
         for(CommandSender user : userCache) {
             user.sendMessage(message);
         }
     }
 
     public void broadcastMessage(String message, IgnoreList ignoreList) {
         for(CommandSender user : userCache) {
             if(ignoreList.isIgnored(user)) continue;
             user.sendMessage(message);
         }
     }
 
     public void broadcastMessage(ArrayList<String> message, IgnoreList ignoreList) {
         String[] messageArray = message.toArray(new String[0]);
         for(CommandSender user : userCache) {
             if(ignoreList.isIgnored(user)) continue;
             user.sendMessage(messageArray);
         }
     }
 
     public void broadcastMessage(ArrayList<String> message) {
         String[] messageArray = message.toArray(new String[0]);
         for(CommandSender user : userCache) {
             user.sendMessage(messageArray);
         }
     }
 }
