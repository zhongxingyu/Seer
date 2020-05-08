 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.Log;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class DebugListeners implements Listener {
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInvClick(InventoryClickEvent event) {
         Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
         Log.debug("Inventory Click Event:");
         Log.debug("    HeldSlot: " + p.getInventory().getHeldItemSlot());
         Log.debug("    FirstEmpty: " + p.getInventory().firstEmpty());
         Log.debug("    Type: " + event.getInventory().getType());
         Log.debug("    Slot: " + event.getSlotType());
         Log.debug("    Cancelled: " + event.isCancelled());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Log.debug(event.getEventName() + ":");
         Log.debug("    Action: " + event.getAction().name());
         Log.debug("    Cancelled: " + event.isCancelled());
     }
 }
