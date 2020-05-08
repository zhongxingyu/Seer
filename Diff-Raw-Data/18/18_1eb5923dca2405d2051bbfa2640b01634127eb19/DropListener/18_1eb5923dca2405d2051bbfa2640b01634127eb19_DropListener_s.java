 package net.worldoftomorrow.nala.ni.listeners;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Log;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class DropListener implements Listener {
 
	Map<String, List<ItemStack>> itemList = new HashMap<String, List<ItemStack>>();
 
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent event) {
 		Log.debug("ItemDropEvent fired.");
 		Player p = event.getPlayer();
 		ItemStack stack = event.getItemDrop().getItemStack();
 		int iid = stack.getTypeId();
 		if (Perms.NODROP.has(p, stack)) {
 			Log.debug("Player has the permission node");
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.DROP, iid);
 			StringHelper.notifyAdmin(p, EventTypes.DROP, stack);
 		} else {
 			Log.debug("Player does not have the permission node");
 		}
 	}
 
 	@SuppressWarnings("unused")
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player p = event.getEntity().getPlayer();
 		if (Perms.ONDEATH.has(p, "keep")) {
 			List<ItemStack> drops = new ArrayList<ItemStack>(event.getDrops());
			for (ItemStack drop : event.getDrops()) {
				drop = null;
			}
 			itemList.put(p.getName(), drops);
 		} else if (Perms.ONDEATH.has(p, "remove")) {
			for (ItemStack drop : event.getDrops()) {
				drop = null;
			}
 		}
 	}

 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player p = event.getPlayer();
 		if (itemList.containsKey(p.getName())) {
 			for(ItemStack i : itemList.get(p.getName())) {
 				p.getInventory().addItem(i);
 			}
 		}
 	}
 }
