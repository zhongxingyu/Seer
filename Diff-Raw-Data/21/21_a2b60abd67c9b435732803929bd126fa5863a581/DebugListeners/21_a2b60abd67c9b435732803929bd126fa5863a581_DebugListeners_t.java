 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.Log;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 
 public class DebugListeners implements Listener {
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInvClick(InventoryClickEvent event) {
     	int clicked = event.getRawSlot();
     	String item = event.getCurrentItem() == null ? "empty" : "" + event.getView().getItem(clicked).getType().name();
         Log.debug("Inventory Click Event: ");
         Log.debug("    Item: " + item);
         Log.debug("    Raw Slot: " + event.getRawSlot());
         Log.debug("    View: " + event.getView().getType());
         Log.debug("    Type: " + event.getInventory().getType());
         Log.debug("    Slot: " + event.getSlotType());
         Log.debug("    Cancelled: " + event.isCancelled());
     }
 
     /*
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Log.debug(event.getEventName() + ":");
         Log.debug("    Action: " + event.getAction().name());
         Log.debug("    Cancelled: " + event.isCancelled());
         if (event.getItem() != null) {
             Log.debug("    In Hand ID: " + event.getItem().getTypeId());
             Log.debug("    In Hand DV: " + event.getItem().getDurability());
             Log.debug("    In Hand Name: " + event.getItem().getType().name());
         }
     }
     */
 }
