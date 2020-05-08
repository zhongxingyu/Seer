 package me.spike.prank;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /***
  * Implements the "toolswitch" prank. Whenever the user tries to use their
  * equipped item, it moves into the first empty slot of their inventory.
  * @author Andrew
  *
  */
 public class ToolPrank extends PlayerListener {
 	private final ConfigurationManager config = ConfigurationManager.getConfigurationManager();
	
 	/**
 	 * Whenever the user clicks a mouse button (interacts), switch their
 	 * equipped item into the first empty slot.
 	 */
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		boolean beingPranked = config.checkPrank(p.getName(), "toolswitch");
				
 		if (beingPranked) {
 			Inventory i = p.getInventory();
 			ItemStack itemUsed = event.getItem();
			
 			if (itemUsed != null) {
 				int emptySlot = i.firstEmpty();
				
				// Don't do anything if the user has a full inventory
				if (emptySlot > 0) {
					int slotUsed = i.first(itemUsed);
 
					i.clear(slotUsed);
 					i.setItem(emptySlot, itemUsed);
 				}
			}
 		}
		
 		super.onPlayerInteract(event);
 	}
 }
