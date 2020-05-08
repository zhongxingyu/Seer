 package com.bukkit.Milton.SignReadMore;
 
 import org.bukkit.entity.*;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.*;
 import org.bukkit.*;
 
 /**
  * Handle events for all Player related events
  * @author Milton
  */
 public class SignReadMorePlayerListener extends PlayerListener {
 
     private final SignReadMore plugin;
 
     public SignReadMorePlayerListener(SignReadMore instance) {
         plugin = instance;
     }
 
     @Override
     public void onPlayerCommand(PlayerChatEvent event) {
         Player player = event.getPlayer();
         String[] message = event.getMessage().split(" ");
         if (message[0].equalsIgnoreCase("/srm")) {
             if (message.length <= 1 || message[1].equals("")) {
                 player.sendMessage("[srm] Please specify a filename.");
                 player.sendMessage("[srm] Usage: (ADMIN)/srm file filename.txt");
                 player.sendMessage("[srm] Usage: /srm line1 A string representing the line number");
                 return;
             }
 
             if (message[1].equals("file")) {
                fileBasedCustomText(event.getMessage(), player);
             } else if (message[1].startsWith("line")) {
                 stringBasedCustomText(message[1], event.getMessage(), player);
             }
         }
     }
 
     private void fileBasedCustomText(String message, Player player) {
        plugin.currentFilename = message.substring(10);
         plugin.wandActive = true;
         plugin.mode = "F";
         player.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
         player.sendMessage("[srm] Read more wand activated.");
         player.sendMessage("[srm] Right-click on Sign or Bookshelf to associate with file.");
     }
 
     private void stringBasedCustomText(String lineRef, String commandMessage, Player player) {
         int lineNo = -1;
         try {
             lineNo = Integer.parseInt(lineRef.substring(4));
             int i = commandMessage.indexOf(lineRef);
             if (i < 0) {
                 incorrectUsage(player);
                 return;
             }
 
             String lineString = commandMessage.substring(i + lineRef.length());
 
             if (lineNo > 0 && lineNo < 9) {
                 plugin.currentFilename = "";
 
                if (!plugin.wandActive) {
                     player.sendMessage("[srm] Read more wand activated.");
                     plugin.wandActive = true;
                 }

                 plugin.mode = "S";
                 player.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
 
                 Article article = plugin.getPlayerCurrentArticle(player.getDisplayName());
                 article.setLine(--lineNo, lineString);
 
                 player.sendMessage("[srm] Line added.");
                 player.sendMessage("[srm] Repeat: /srm line2 ... to add more lines.");
                 player.sendMessage("[srm] Right-click on Sign or Bookshelf when finished.");
             } else {
                 incorrectUsage(player);
             }
 
         } catch (NumberFormatException ex) {
             incorrectUsage(player);
         }
     }
 
     private void incorrectUsage(Player player) {
         player.sendMessage("[srm] Incorrect command usage.");
         printStringUsageMessage(player);
     }
 
     private void printStringUsageMessage(Player player) {
         player.sendMessage("[srm] Usage: /srm line[1 - 8] Some text for line[1 - 8]");
         player.sendMessage("[srm] e.g /srm line1 Some text for line1");
     }
 }
