 package com.minecraftserver.warn.commands;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.minecraftserver.warn.Punisher;
 import com.minecraftserver.warn.SLAPI;
 import com.minecraftserver.warn.Warner;
 
 public class Warncmd extends WarnCommandHandler {
 		 
     static SLAPI             slapi;
     static String            reason          = "You have been warned!";
     static List<String[]>    warnings_player = new Vector<String[]>();
     static YamlConfiguration config          = new YamlConfiguration();
 
     // Start args[] to String
     static String createString(String[] args, int start) {
         return createString(args, start, " ");
     }
 
     static String createString(String[] args, int start, String glue) {
         StringBuilder string = new StringBuilder();
         for (int x = start; x < args.length; x++) {
             string.append(args[x]);
             if (x != args.length - 1) {
                 string.append(glue);
             }
         }
         return string.toString();
     }
 
     // End args[] to String
 
     private static String getTimeNow() {
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - hh:mm a");
         return dateFormat.format(new Date());
     }
 
     public static boolean run(CommandSender sender, String[] args, Warner warner) {
 
     	SLAPI slapi=warner.getSLAPI();
         Player target = Bukkit.getServer().getPlayer(args[0]);
 
         if (!sender.hasPermission("warner.warn.give")) {
             sender.sendMessage(ChatColor.DARK_RED + "You have insufficient permissions to do this.");
             return false;
         }
 
         if (target != null) {
             if (target.hasPermission("warner.admin.warn.exempt")) {
                 sender.sendMessage(ChatColor.RED + "You can't warn this person!");
                 return false;
             }
             if (args.length > 1) {
                 reason = createString(args, 1);
             }
         } else {
             sender.sendMessage(ChatColor.RED + args[0] + " was not found online!");
             return false;
         }
 
         // Load existing warnings of player
         warnings_player = slapi.loadPlayerWarnings(target);
         if (warnings_player != null) {
             Long time_difference = 20L;
             if (warnings_player.size() > 0) {
                 time_difference = (System.currentTimeMillis() - Long.parseLong(warnings_player
                         .get(warnings_player.size() - 1)[3])) / 1000;
             }
             if (time_difference >= 20) {
                 String[] warning = { sender.getName(), reason, getTimeNow(),
                         System.currentTimeMillis() + "" };
                 warnings_player.add(warning);
             } else {
                 sender.sendMessage(ChatColor.RED + "You have to wait " + ChatColor.GOLD
                         + (20 - time_difference) + ChatColor.RED + " seconds until you can warn "
                         + ChatColor.GOLD + target.getName() + ChatColor.RED + " again.");
                 return false;
             }
 
             // save warnings to file
             if (!slapi.savePlayerWarnings(warnings_player, target)) {
                 sender.sendMessage(ChatColor.RED + "Error writing Playerfile!");
                 return false;
             }
 
         } else {
             sender.sendMessage(ChatColor.RED + "Error reading Playerfile!");
             return false;
         }
 
         target.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.BLUE
                 + " has warned you for:\n" + ChatColor.GOLD + reason + ChatColor.BLUE + "."
                 + ChatColor.GOLD + " [" + warnings_player.size() + "]");
 
         Player[] playerList = Bukkit.getServer().getOnlinePlayers();
         for (Player player : playerList) {
             if (player.hasPermission("warner.other.notify")) {
                 player.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.BLUE
                         + " has been warned by " + ChatColor.GOLD + sender.getName()
                         + ChatColor.BLUE + " for:\n" + ChatColor.GOLD + reason + ChatColor.BLUE
                         + "." + ChatColor.GOLD + " [" + warnings_player.size() + "]");
             }
         }
 
         Punisher.punish(target, warnings_player.size(), reason, sender, config);
         return true;
     }
 
 }
