 package com.hawkfalcon.deathswap.game;
 
 import com.hawkfalcon.deathswap.DeathSwap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Join {
 
     public DeathSwap plugin;
 
     public Join(DeathSwap ds) {
         this.plugin = ds;
     }
 
     public void join(Player player) {
         if (player.hasPermission("deathswap.join")) {
             String name = player.getName();
             if (!plugin.game.contains(name) && !plugin.lobby.contains(name)) {
                 plugin.utility.message("You joined the game!", player);
                plugin.utility.broadcastLobby(name + " joined the game!");
                 plugin.lobby.add(name);
                 plugin.utility.teleport(player, 0);
                 plugin.utility.checkForStart();
             } else {
                 plugin.utility.message("You are already in a game!", player);
             }
         } else {
             player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
         }
     }
 }
