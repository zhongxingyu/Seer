 package edgruberman.bukkit.fixchunkholes;
 
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener {
     
     private Main plugin;
     
     public PlayerListener(Main plugin) {
         this.plugin = plugin;
     }
     
     @Override
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (event.isCancelled()) return;
         
         this.plugin.refreshChunk(event.getTo().getBlock());
     }
 }
