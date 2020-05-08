 package com.minecarts.barrenschat.websocket;
 
 import com.minecarts.barrenschat.BarrensChat;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 
 
 public class BarrensSocketFactory {
     private HashMap<Player, BarrensSocket> socketMap = new HashMap<Player, BarrensSocket>();
     private BarrensChat plugin;
 
     public BarrensSocketFactory(BarrensChat plugin) {
         this.plugin = plugin;
     }
 
     public void set(Player player, BarrensSocket socket) {
         this.socketMap.put(player, socket);
     }
 
     public BarrensSocket get(Player player) {
         if (socketMap.containsKey(player)) {
             return this.socketMap.get(player);
         } else {
             return null;
         }
     }
 
     public void remove(Player player) {
         if (this.socketMap.containsKey(player)) {
             this.socketMap.get(player).closeSocket();
             this.socketMap.remove(player);
         }
     }
 
     public boolean contains(Player player) {
         return this.socketMap.containsKey(player);
     }
 
     public void reconnect(Player p) {
        if(!p.isOnline()){ remove(p); } //Remove the players socket becuase they disconnected
         if (this.socketMap.containsKey(p)) {
             this.socketMap.get(p).reconnect();
         }
     }
 
     public void create(Player p) {
         //Async try to create a socket for this player
         final Player player = p;
         Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
             public void run() {
                if(!player.isOnline()) return; //Don't do anything if the player isn't online
                 BarrensSocket socket = new BarrensSocket(player, plugin);
                 if (socket != null && socket.isConnected()) {
                     plugin.BarrensSocketFactory.set(player, socket);
                     player.sendMessage(ChatColor.DARK_GRAY + "DEBUG: Connection established to chat server.");
                 } else {
                     player.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.GRAY + "Unable to connect to chat server. Trying again in 10 seconds.");
                     Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                         public void run() {
                             plugin.BarrensSocketFactory.create(player);
                         }
                     }, 20 * 10);
                 }
             }
         });
     }
 
 }
