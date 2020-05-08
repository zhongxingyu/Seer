 package io.github.alshain01.flags.sector;
 
 import io.github.alshain01.flags.Flags;
 import io.github.alshain01.flags.Message;
 import io.github.alshain01.flags.events.SectorCreateEvent;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 public class SectorListener implements Listener {
     final Material tool;
     final Map<UUID, Location> createQueue = new HashMap<UUID, Location>();
 
     public SectorListener(Material tool) {
         this.tool = tool;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     private void onPlayerInteractEvent(PlayerInteractEvent e) {
         Player player = e.getPlayer();
        if(e.getItem() == null || e.getItem().getType() != tool || !player.hasPermission("flags.sector.create")) { return; }
         Location corner1 = e.getClickedBlock().getLocation();
 
         if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
             //Process the first corner
             player.sendMessage(Message.SectorStarted.get());
             createQueue.put(player.getUniqueId(), corner1);
             e.setCancelled(true);
         }
 
         if(e.getAction() == Action.RIGHT_CLICK_BLOCK && createQueue.containsKey(player.getUniqueId())) {
             // Process the second corner and final creation
             SectorManager sectors = Flags.getSectorManager();
             Location corner2 = createQueue.get(player.getUniqueId());
 
             if(sectors.isOverlap(corner1, corner2)) {
                 UUID parent = sectors.isContained(corner1, corner2);
                 if(parent == null || sectors.get(parent).getParentID() != null) {
                     // Sector is only partially inside another or is inside another subdivison
                     player.sendMessage(Message.SectorOverlapError.get());
                     return;
                 }
                 // Create Subdivision
                 player.sendMessage(Message.SubsectorCreated.get());
                 Bukkit.getPluginManager().callEvent(new SectorCreateEvent(sectors.add(corner1, corner2, parent)));
             } else {
                 // Create Parent Sector
                 player.sendMessage(Message.SectorCreated.get());
                 Bukkit.getPluginManager().callEvent(new SectorCreateEvent(sectors.add(corner1, corner2)));
             }
             createQueue.remove(player.getUniqueId());
             e.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     private void onPlayerPlayerItemHeld(PlayerItemHeldEvent e) {
         Player player = e.getPlayer();
         ItemStack prevSlot = player.getInventory().getItem(e.getPreviousSlot());
         ItemStack newSlot = player.getInventory().getItem(e.getNewSlot());
         if(prevSlot != null && prevSlot.getType() == tool
                 && (newSlot == null || newSlot.getType() != tool)
                 && createQueue.containsKey(player.getUniqueId())) {
                     createQueue.remove(player.getUniqueId());
                     player.sendMessage(Message.CancelCreateSector.get());
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     private void onPlayerQuit(PlayerQuitEvent e) {
         createQueue.remove(e.getPlayer().getUniqueId());
     }
 }
