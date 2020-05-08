 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.bukkit.Milton.SignReadMore;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author pfarisoma
  */
 class SignReadMoreCommand implements CommandExecutor {
 
     private final SignReadMore plugin;
 
     public SignReadMoreCommand(SignReadMore plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
         Player player = cs instanceof Player ? ((Player) cs) : null;
         if (player == null) {
             return false;
         }
 
         if (args.length <= 1 || args[0].equals("")) {
             player.sendMessage("[srm] Please specify a filename.");
             player.sendMessage("[srm] Usage: (ADMIN)/srm file filename.txt");
             player.sendMessage("[srm] Usage: /srm line1 A string representing the line number");
             return false;
         }
 
         String message = join(args, " ");
         if (args[0].equals("file")) {
             fileBasedCustomText(message, player);
         } else if (args[0].startsWith("line")) {
             stringBasedCustomText(args[0], message, player);
         }
         return true;
     }
 
     private void fileBasedCustomText(String message, Player player) {
        plugin.currentFilename = message.substring(5);
         plugin.playerPluginActive.put(player.getDisplayName(), true);
         plugin.mode = "F";
         player.sendMessage("[srm] Read more wand activated.");
         player.sendMessage("[srm] Right-click on Sign or Bookshelf to associate with file.");
     }
 
     private void stringBasedCustomText(String lineRef, String commandMessage, Player player) {
         int lineNo = -1;
         try {
             lineNo = Integer.parseInt(lineRef.substring(4));
             int i = commandMessage.indexOf(lineRef);
             if (i < 0) {
                 System.out.println("commandMessage: "+commandMessage);
                 incorrectUsage(player);
                 return;
             }
 
             String lineString = commandMessage.substring(i + lineRef.length() + 1);
 
             if (lineNo > 0 && lineNo <= SignReadMore.MAX_LINES) {
                 plugin.currentFilename = "";
 
                 boolean playerPluginActive = plugin.playerPluginActive.get(player.getDisplayName()) != null ? plugin.playerPluginActive.get(player.getDisplayName()) : false;
                 if (!playerPluginActive) {
                     player.sendMessage("[srm] Read more wand activated.");
                     plugin.playerPluginActive.put(player.getDisplayName(), true);
                 }
 
                 plugin.mode = "S";
 
                 Article article = plugin.getPlayerCurrentArticle(player.getDisplayName());
                 article.setLine(--lineNo, lineString);
 
                 player.sendMessage("[srm] Line added.");
                 player.sendMessage("[srm] Repeat: /srm line2 ... to add more lines.");
                 player.sendMessage("[srm] Right-click on Sign or Bookshelf when finished.");
             } else {
                 System.out.println("lineref: "+lineRef.substring(4));
                 incorrectUsage(player);
             }
 
         } catch (NumberFormatException ex) {
             ex.printStackTrace();
             incorrectUsage(player);
         }
     }
 
     private void incorrectUsage(Player player) {
         player.sendMessage("[srm] Incorrect command usage.");
         printStringUsageMessage(player);
     }
 
     private void printStringUsageMessage(Player player) {
         player.sendMessage("[srm] Usage: /srm line[1 - " + SignReadMore.MAX_LINES + "] Some text for line[1 - " + SignReadMore.MAX_LINES + "]");
         player.sendMessage("[srm] e.g /srm line1 Some text for line1");
     }
 
     public static String join(String[] array, String separator) {
         if (array.length == 0) {
             return "";
         }
         StringBuilder oBuilder = new StringBuilder(array[0]);
         for (int i = 1; i < array.length; i++) {
             oBuilder.append(separator).append(array[i]);
         }
         return oBuilder.toString();
     }
 }
