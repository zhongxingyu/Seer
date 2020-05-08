 package me.meta1203.plugins.gpextras;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.sk89q.worldedit.LocalSession;
 
 public class ExtrasListener implements Listener {
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if (event.getClickedBlock() != null) {
 				if (event.getItem() != null) {
 					if (Gpextras.itemBan.containsKey(event.getItem().getTypeId()) && 
 							Gpextras.itemBan.containsValue(event.getItem().getDurability())) {
 						if (Gpextras.gp.dataStore.getClaimAt(event.getClickedBlock().getLocation(), true, null).allowBuild(event.getPlayer()) == null) {
 							event.setCancelled(true);
 						}
 					}
 				}
 			}
 		}
 	}
 	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		if (Gpextras.wep != null) {
			System.out.println("Masking player...");
			LocalSession ls = Gpextras.wep.getSession(event.getPlayer());
			ls.setMask(new GriefPreventionMask(event.getPlayer()));
		}
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (Gpextras.wep != null) {
			System.out.println("Masking player...");
 			LocalSession ls = Gpextras.wep.getSession(event.getPlayer());
 			ls.setMask(new GriefPreventionMask(event.getPlayer()));
 		}
 	}
 }
