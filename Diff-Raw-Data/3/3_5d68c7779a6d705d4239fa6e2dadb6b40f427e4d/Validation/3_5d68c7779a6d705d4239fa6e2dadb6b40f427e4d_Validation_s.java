 package com.mutinycraft.jigsaw.RankHelper.Util;
 
 import com.mutinycraft.jigsaw.RankHelper.RankHelper;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * User: Jigsaw
  * Date: 7/6/13
  * Time: 7:55 PM
  */
 
 public class Validation {
 
     RankHelper plugin;
 
     public Validation(RankHelper p) {
         plugin = p;
     }
 
     /**
      * Checks whether the provided group is a valid group as specified in the config.yml
      *
      * @param group  to validate.
      * @param sender that issued command.
      * @return true if valid, false otherwise.
      */
     public boolean isValidGroup(String group, CommandSender sender) {
         if (plugin.getGroups().contains(group)) {
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "The rank {" + group + "} is not a valid rank.");
             return false;
         }
     }
 
     /**
      * Checks whether the provided player is online.
      *
      * @param playerToRank to check if online
      * @param sender       that issued command.
      * @return true if online, false otherwise.
      */
     public boolean isValidPlayer(String playerToRank, CommandSender sender) {
         Player player = plugin.getServer().getPlayerExact(playerToRank);
         if (player != null && player.isOnline()) {
             return true;
         } else {
            sender.sendMessage(ChatColor.RED + "The player {" + playerToRank + "} is not currently online.  Use " +
                    "/rankoffline if you are sure you want to rank this player.");
             return false;
         }
     }
 
     /**
      * Checks if the provided player is allowed to have their group changed based on permissions.
      *
      * @param playerToRank to check if able to change group.
      * @param sender       that issued command.
      * @return true if allowed to change group, false otherwise.
      */
     public boolean isAllowedGroupChange(String playerToRank, CommandSender sender) {
         Player player = plugin.getServer().getPlayerExact(playerToRank);
         if (!player.hasPermission("rankhelper.norank")) {
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "You are not allowed to change the rank of {" + playerToRank + "}.");
             return false;
         }
     }
 
     /**
      * Checks if the provided world is a valid (loaded) world.
      *
      * @param worldName of world to check.
      * @return true if valid world, false otherwise.
      */
     public boolean isValidWorld(String worldName, CommandSender sender) {
         if (plugin.getServer().getWorld(worldName) != null) {
             return true;
         }
         sender.sendMessage(ChatColor.RED + "The world name {" + worldName + "} is not a valid/loaded world.  Case " +
                 "matters with world names.  That means world is not the same as World.");
         return false;
     }
 }
