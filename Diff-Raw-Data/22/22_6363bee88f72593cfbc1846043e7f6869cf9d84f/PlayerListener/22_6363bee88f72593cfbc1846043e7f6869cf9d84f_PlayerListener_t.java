 package com.minecarts.bouncer.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.entity.Player;
 import com.minecarts.bouncer.Bouncer;
 import java.text.MessageFormat;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener{
     private Bouncer plugin;
     public PlayerListener(Bouncer plugin){
         this.plugin = plugin;
     }
     
     @Override
     public void onPlayerPreLogin(PlayerPreLoginEvent e){
         String reason = plugin.dbHelper.isIdentiferBanned(e.getAddress().toString());
         if(reason != null){
             e.setResult(PlayerPreLoginEvent.Result.KICK_BANNED);
             e.setKickMessage("You have been banned: " + reason);
         }
     }
     @Override
     public void onPlayerLogin(PlayerLoginEvent e){
         String reason = plugin.dbHelper.isIdentiferBanned(e.getPlayer().getName());
         if(reason != null){
             e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
             e.setKickMessage("You have been banned: " + reason);
             return;
         }
         //Check to see if they're a sub
         boolean isSubscriber = false;
         if(isSubscriber){
             //Check the player count
             Player[] online = Bukkit.getServer().getOnlinePlayers();
             if(online.length >= Bukkit.getServer().getMaxPlayers()){
                 //Kick the newest player
                 online[online.length - 1].kickPlayer(plugin.fullMessage);
                 e.setResult(Result.ALLOWED);
             }
         } else {
             //Check to see if the server is full and display a better message
             if(e.getResult() == Result.KICK_FULL){
                 e.setKickMessage(plugin.fullMessage);
             }
         }
     }
     @Override
     public void onPlayerJoin(PlayerJoinEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String message = plugin.dbHelper.getJoinMessage(playerName);
         if(message != null){
             e.setJoinMessage(MessageFormat.format("{0}" + message,ChatColor.GRAY,playerDisplayName));
         } else if(plugin.dbHelper.getKey("joinCount", playerName) == null){
         //} else if(e.getPlayer().getLocation().getPitch() == 0 && e.getPlayer().getLocation().getYaw() == 0){
             e.setJoinMessage(ChatColor.WHITE + playerDisplayName + " has joined the server for the first time!");
         } else {
             e.setJoinMessage(ChatColor.GRAY + playerDisplayName + " has joined the server.");
         }
     }
     @Override
     public void onPlayerQuit(PlayerQuitEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String message = plugin.dbHelper.getQuitMessage(playerName);
         if(message != null){
            e.setQuitMessage(MessageFormat.format("{0}" + message,ChatColor.GRAY,playerDisplayName));
         } else {
             e.setQuitMessage(ChatColor.GRAY + playerDisplayName + " has left the server.");
         }
     }
 }
