 /*
  * Copyright (c) 2012 cedeel.
  * All rights reserved.
  * 
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package be.darnell.mc.FuzzyMessenger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @author cedeel
  */
 public final class PrivateMessaging {
 
     private ChatColor introColor = ChatColor.DARK_GRAY;
     private ChatColor snoopColor = ChatColor.DARK_GREEN;
     private ChatColor msgColor = ChatColor.ITALIC;
     private ChatColor warnColor = ChatColor.RED;
     private static FuzzyMessenger plugin;
     private HashMap<String, String> pairs; // 2 names
     private List<String> snoopers;
 
     public PrivateMessaging(final FuzzyMessenger instance) {
         plugin = instance;
         pairs = new HashMap<String, String>();
         snoopers = new ArrayList<String>();
     }
 
     public boolean sendMessage(CommandSender sender, String recipient, String message) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
 
             if (!plugin.mm.isMuted(player) && player.hasPermission("fuzzymessenger.pm.send")) {
                 String destination;
                 if (!recipient.equalsIgnoreCase("console")) {
                     // Player may be null, so we use a try-catch block
                     try {
                         Player receiver = plugin.getServer().getPlayer(recipient);
                         destination = receiver.getName();
                         fireMessage(message, player, receiver);
                     } catch (NullPointerException e) {
                         sender.sendMessage(warnColor + "Player " + recipient + " not found.");
                         return false;
                     }
 
                 } else { // If recipient is console
                     System.out.println("«" + player.getDisplayName() + ": " + message);
                     sender.sendMessage(introColor + "»CONSOLE: " + msgColor + message);
                     logMessage(sender.getName(), "CONSOLE", message);
                     destination = "console";
                 }
                 pairs.put(player.getName(), destination);
                 pairs.put(destination, player.getName());
                 return true;
 
             } else {
                 player.sendMessage(warnColor + "You're not allowed to send private messages.");
                 return false;
             }
         } else if (sender instanceof ConsoleCommandSender) {
             try {
                 Player receiver = plugin.getServer().getPlayer(recipient);
                 System.out.println("»" + receiver.getDisplayName() + ": " + message);
                 receiver.sendMessage(introColor + "«CONSOLE: " + msgColor + message);
                 logMessage("CONSOLE", receiver.getName(), message);
                 pairs.put(receiver.getName(), "console");
                 pairs.put("console", receiver.getName());
                 return true;
             } catch (NullPointerException e) {
                 System.out.println("Recipient not found.");
             }
         }
         return false;
     }
 
     /**
      * Reply to an incoming private message.
      *
      * @param sender  The sender who is replying.
      * @param message The message contents.
      * @return Whether the operation was successful.
      */
     public boolean replyMessage(CommandSender sender, String message) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             String recipient;
             if (!plugin.mm.isMuted(player) && player.hasPermission("fuzzymessenger.pm.send")) {
                 try {
                     recipient = pairs.get(player.getName()); // This is what might generate a NullPointerException.
                     if (recipient.equalsIgnoreCase("console")) {
                         System.out.println("«" + player.getDisplayName() + ": " + message);
                         sender.sendMessage(introColor + "»CONSOLE: " + msgColor + message);
                         logMessage(player.getName(), "CONSOLE", message);
                         return true;
                     }
                 } catch (NullPointerException e) {
                     sender.sendMessage(warnColor + "You cannot reply before sending or receiving a private message.");
                     return false;
                 }
                 try {
                     Player receiver = plugin.getServer().getPlayer(recipient); // This is another point where a NPE can be generated.
                     fireMessage(message, player, receiver);
                     return true;
                 } catch (NullPointerException e) {
                     sender.sendMessage(warnColor + "The player to whom you are replying could not be found.");
                     return false;
                 }
             } else {
                 player.sendMessage(introColor + "You're not allowed to send private messages.");
                 return false;
             }
         } // Sender is player
         else if (sender instanceof ConsoleCommandSender) {
             try {
                 Player receiver = plugin.getServer().getPlayer(pairs.get("console"));
                 System.out.println("»" + receiver.getDisplayName() + ": " + message);
                 receiver.sendMessage(introColor + "«CONSOLE: " + msgColor + message);
                 logMessage("CONSOLE", receiver.getName(), message);
                 pairs.put(receiver.getName(), "console");
                 pairs.put("console", receiver.getName());
             } catch (NullPointerException e) {
                 System.out.println("Player not found.");
             }
         } // Sender is console
         return false;
     } // replyMessage()
 
     /**
      * Add a player to the snooper list.
      *
      * @param p The player to be added.
      */
     public void addSnooper(Player p) {
         snoopers.add(p.getName().toLowerCase());
     }
 
     /**
      * Know whether a player is in the snooper list.
      *
      * @param p The player in question.
      * @return True if the player is snooping private messages.
      */
     public boolean isSnooper(Player p) {
         return snoopers.contains(p.getName().toLowerCase());
     }
 
     /**
      * Remove a player from snooping private messages.
      *
      * @param p The player to be removed.
      */
     public void removeSnooper(Player p) {
         snoopers.remove(p.getName().toLowerCase());
     }
 
     public List<String> getSnoopers() {
         return snoopers;
     }
 
     /**
      * Do the actual work of delivering a private message.
      *
      * @param message  The message contents.
      * @param sender   The sender of the message.
      * @param receiver The receiver of the message.
      */
     private void fireMessage(String message, Player sender, Player receiver) {
         sender.sendMessage(introColor + "»" + receiver.getDisplayName() + ": " + msgColor + message);
         receiver.sendMessage(introColor + "«" + sender.getDisplayName() + ": " + msgColor + message);
 
         for (String snooper : snoopers) {
             Player p = Bukkit.getPlayer(snooper);
             if ((p != sender) && (p != receiver)) {
                p.sendMessage(snoopColor + sender.getDisplayName() + "» " + receiver.getDisplayName() + msgColor + message);
             }
         }
 
         logMessage(sender.getName(), receiver.getName(), message);
     }
 
     private void logMessage(String sender, String receiver, String message) {
         StringBuilder toLog = new StringBuilder(32);
         toLog
                 .append(sender)
                 .append(" » ")
                 .append(receiver)
                 .append(": ")
                 .append(message);
 
         FuzzyMessenger.logMessage(toLog.toString());
     }
 
     /**
      * Construct a message string from an array of strings.
      * @param message The array of strings to be concatenated.
      * @param offset The first index in the array where the message appears.
      * @return A string containing the message.
      */
     public static String constructMessage(String[] message, int offset) {
         String m = "";
         for (int i = offset; i < message.length; i++)
             m = m + " " + message[i];
         return m.trim();
     }
 } //PrivateMessaging
