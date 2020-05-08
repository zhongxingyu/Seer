 package de.minestar.core.listener;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import de.minestar.core.manager.PlayerManager;
 
 public class ConnectionListener implements Listener {
 
     private PlayerManager playerManager;
 
     public ConnectionListener(PlayerManager playerManager) {
         this.playerManager = playerManager;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerJoin(PlayerJoinEvent event) {
        this.playerManager.getPlayer(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerPreLogin(PlayerPreLoginEvent event) {
         this.playerManager.getPlayer(event.getName());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerQuit(PlayerQuitEvent event) {
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerKick(PlayerKickEvent event) {
         // EVENT IS CANCELLED? => RETURN
         if (event.isCancelled())
             return;
 
         this.onPlayerDisconnect(event.getPlayer());
     }
 
     private void onPlayerDisconnect(Player player) {
         this.playerManager.removePlayer(player);
     }
 
 }
