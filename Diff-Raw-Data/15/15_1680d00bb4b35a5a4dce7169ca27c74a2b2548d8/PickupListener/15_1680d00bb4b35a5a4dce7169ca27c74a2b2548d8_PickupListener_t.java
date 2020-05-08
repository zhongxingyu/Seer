 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PickupListener implements Listener {
 	
 	private static Log log = new Log();
 
 	@EventHandler
 	public void onPickup(PlayerPickupItemEvent event) {
 
 		Player p = event.getPlayer();
 		ItemStack stack = new ItemStack(event.getItem().getItemStack());
 		int iid = event.getItem().getItemStack().getTypeId();
 		
		if(Tools.isTool(iid) || Armor.isArmor(iid)){
 			stack.setDurability((short) 0);
 		}
 		
 		log.debug("PlayerPickupItemEvent fired. ".concat(Integer.toString(iid)));
 		if(Perms.NOPICKUP.has(p, stack)){
 			event.setCancelled(true);
 			event.getItem().setPickupDelay(200);
 			StringHelper.notifyPlayer(p, EventTypes.PICKUP, iid);
 			StringHelper.notifyAdmin(p, EventTypes.PICKUP, stack);
 		} else {
 			log.debug("Item can be picked up");
 		}
 	}
 }
