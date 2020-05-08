 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.SaveablePlayer.PlayerRank;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.*;
 
 /**
  * When a player logs onto the server.
  * @author UndeadScythes
  */
 public class PlayerJoin implements Listener {
     @EventHandler
     public void onEvent(final PlayerJoinEvent event) {
         final String playerName = event.getPlayer().getName();
         SaveablePlayer player;
         if(UDSPlugin.getPlayers().containsKey(playerName)) {
             player = UDSPlugin.getPlayers().get(playerName);
             player.wrapPlayer(event.getPlayer());
             UDSPlugin.getOnlinePlayers().put(playerName, player);
         } else {
             player = new SaveablePlayer(event.getPlayer());
             UDSPlugin.getPlayers().put(playerName, player);
             UDSPlugin.getOnlinePlayers().put(playerName, player);
             if(player.getName().equals(Config.serverOwner)) {
                 player.setRank(PlayerRank.OWNER);
                 player.sendMessage(ChatColor.GOLD + "Welcome to your new server, I hope everything goes well.");
             } else {
                 Bukkit.broadcastMessage(Color.BROADCAST + "A new player, free gifts for everyone!");
                 final ItemStack gift = new ItemStack(Config.welcomeGift);
                 for(SaveablePlayer onlinePlayer : UDSPlugin.getOnlinePlayers().values()) {
                     onlinePlayer.giveAndDrop(gift);
                 }
             }
             player.quietTeleport(UDSPlugin.getWarps().get("spawn"));
         }
         if(UDSPlugin.serverInLockdown && !player.hasLockdownPass()) {
             player.kickPlayer("The server is currently in lockdown please check back later.");
         } else {
             player.sendMessage(Color.MESSAGE + Config.welcome);
             if(player.getRank().equals(PlayerRank.DEFAULT)) {
                 player.sendMessage(Color.MESSAGE + "Kill monsters or trade with players to earn " + Config.buildCost + " credits then type /acceptrules in chat.");
             } else if(player.getRank().compareTo(PlayerRank.MOD) >= 0) {
                player.sendMessage(Config.welcomeAdmin);
             }
             event.setJoinMessage(Color.BROADCAST + player.getNick() + (player.isInClan() ? " of " + player.getClan().getName() : "") + " has joined.");
         }
     }
 }
