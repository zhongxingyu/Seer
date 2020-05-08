 package net.betterverse.unclaimed;
 
 import net.betterverse.unclaimed.util.UnclaimedRegistry;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 
 
 public class Listener implements org.bukkit.event.Listener {
 
     private Unclaimed instance;
 
     public Listener(Unclaimed instance) {
         this.instance = instance;
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         event.setCancelled(checkProtection(event.getPlayer(), event.getBlock().getLocation()));
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         event.setCancelled(checkProtection(event.getPlayer(), event.getBlock().getLocation()));
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerBucket(PlayerBucketEmptyEvent event) {
         event.setCancelled(checkProtection(event.getPlayer(), event.getBlockClicked().getLocation()));
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerBucket(PlayerBucketFillEvent event) {
         event.setCancelled(checkProtection(event.getPlayer(), event.getBlockClicked().getLocation()));
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
         if (e.getRemover() instanceof Player) {
             e.setCancelled(checkProtection((Player)e.getRemover(), e.getEntity().getLocation()));
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onHangingPlace(HangingPlaceEvent e) {
         e.setCancelled(checkProtection(e.getPlayer(), e.getEntity().getLocation()));
     }
 
     public boolean checkProtection(Player player, Location location) {
         /*ProtectionInfo protectionInfo = CheckProtection.isProtected(player.getLocation());
         if (!protectionInfo.isProtected() && !player.hasPermission("unclaimed.build")) {
             player.sendMessage(instance.getDescription().getPrefix() +" "+ instance.getConfiguration().getBuildMessage());
             return true;
         }
         return false;*/
         
        return UnclaimedRegistry.isProtected(location);
     }
 }
