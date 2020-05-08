 package com.ACStache.ArenaGodPlus;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class AGPSetter
 {
     private static HashMap<String,HashSet<Boolean>> godMode = new HashMap<String,HashSet<Boolean>>();
     
     public static void setGod(Player player)
     {
         String playerName = player.getName();
         if(godMode.get(playerName) == null)
             godMode.put(playerName, new HashSet<Boolean>());
         
         HashSet<Boolean> godSet = godMode.get(playerName);
         
         if(godSet.isEmpty())
         {
             if(!inRestrictedRegion(player))
             {
                 godSet.add(true);
                 player.sendMessage(ChatColor.AQUA + "AGP: God mode enabled");
                 if(AGPConfig.getPersistence())
                     AGPConfig.addPersGod(player);
             }
             else
             {
                player.sendMessage(ChatColor.RED + "AGP: God mode not allowed in Restricted Zones or excluded Worlds");
             }
         }
         else if(godSet.contains(true))
         {
             godSet.remove(true);
             godSet.add(false);
             if(AGPConfig.getPersistence())
                 AGPConfig.removePersGod(player);
             
             if(!inRestrictedRegion(player))
                     player.sendMessage(ChatColor.AQUA + "AGP: God mode disabled");
             else
                player.sendMessage(ChatColor.RED + "AGP: God mode not allowed in Restricted Zones or excluded Worlds");
         }
         else
         {
             if(!inRestrictedRegion(player))
             {
                 godSet.remove(false);
                 godSet.add(true);
                 player.sendMessage(ChatColor.AQUA + "AGP: God mode enabled");
                 if(AGPConfig.getPersistence())
                     AGPConfig.addPersGod(player);
             }
             else
             {
                player.sendMessage(ChatColor.RED + "AGP: God mode not allowed in Restricted Zones or excluded Worlds");
             }
         }
     }
     
     public static boolean isGod(Player player)
     {
         if(godMode.containsKey(player.getName()))
             return godMode.get(player.getName()).contains(true);
         else
             return false;
     }
     
     public static void addGod(Player player)
     {
         String playerName = player.getName();
         if(godMode.get(playerName) == null)
         {
             godMode.put(playerName, new HashSet<Boolean>());
             if(AGPConfig.getPersistence() && AGPConfig.getPersGod(player))
                 godMode.get(playerName).add(true);
             else
                 godMode.get(playerName).add(false);
         }
         else if(godMode.get(playerName).isEmpty())
         {
             if(AGPConfig.getPersistence() && AGPConfig.getPersGod(player))
                 godMode.get(playerName).add(true);
             else
                 godMode.get(playerName).add(false);
         }
     }
     
     private static boolean inRestrictedRegion(Player player)
     {
         if(AGPArenaChecker.isPlayerInMAArena(player))
             return true;
         if(AGPArenaChecker.isPlayerInPVPArena(player))
             return true;
         if(AGPArenaChecker.isPlayerInWarRegion(player))
             return true;
         if(AGPArenaChecker.isPlayerInMobDungeon(player))
             return true;
         if(AGPConfig.getExcludedWorlds().contains(player.getWorld()))
             return true;
         return false;
     }
 }
