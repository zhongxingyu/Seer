 package me.ellbristow.simplespawnliteback.listeners;
 
 import me.ellbristow.simplespawnliteback.SimpleSpawnLiteBack;
 import me.ellbristow.simplespawnlitecore.events.SimpleSpawnTeleportEvent;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class PlayerListener implements Listener {
     
     private SimpleSpawnLiteBack plugin;
     
     public PlayerListener(SimpleSpawnLiteBack instance) {
         plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerTeleport(SimpleSpawnTeleportEvent event) {
         if (event.isCancelled()) return;
         
         Player player = event.getPlayer();
         Location fromLoc = event.getFromLoc();
         plugin.setBackLoc(player.getName(), fromLoc);
             
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerDeath(PlayerDeathEvent event) {
 
         Player player = event.getEntity();
         Location loc = player.getLocation();
 
         plugin.setBackLoc(player.getName(), loc);
 
     }
     
 }
