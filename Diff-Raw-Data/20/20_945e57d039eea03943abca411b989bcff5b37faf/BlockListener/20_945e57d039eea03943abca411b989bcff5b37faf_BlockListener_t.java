 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Log;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockListener implements Listener {
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Log.debug("BlockBreakEvent fired");
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		if (Perms.NOBREAK.has(p, b)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.BREAK, b.getTypeId());
 			StringHelper.notifyAdmin(p, b);
 			return;
 		}
 
 		ItemStack inhand = p.getItemInHand();
 		if (inhand != null && Perms.NOUSE.has(p, inhand)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, inhand.getTypeId());
 			StringHelper.notifyAdmin(p, EventTypes.USE, inhand);
 			return;
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Log.debug("BlockPlaceEvent fired");
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
		if (Perms.NOPLACE.has(p, b)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.PLACE, b.getTypeId());
 			StringHelper.notifyAdmin(p, b);
 			return;
 		}
 		
 		ItemStack inhand = event.getItemInHand();
 		if (inhand != null && Perms.NOUSE.has(p, inhand)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, inhand.getTypeId());
 			StringHelper.notifyAdmin(p, EventTypes.USE, inhand);
 			return;
 		}
 	}
 }
