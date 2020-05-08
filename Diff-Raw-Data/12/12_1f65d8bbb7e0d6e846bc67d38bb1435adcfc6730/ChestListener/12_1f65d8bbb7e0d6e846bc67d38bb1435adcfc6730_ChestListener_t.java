 package com.black921.AntiChestBug;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class ChestListener implements Listener {
 
 	public ChestListener(AntiChestBug plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void ChestInteract(PlayerInteractEvent e) {
 
 		Player ply = e.getPlayer();
		Material type = null;
 
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			Block block = e.getClickedBlock();
			if(block.getType() == Material.CHEST || block.getType() == Material.SIGN){type = block.getType();}
			if (type != null) {
 				for (Block b : ply.getLineOfSight(null, 10)) {
 					if (b.getType() != Material.AIR) {
						if (b.getType() == type) {
 							return;
 						}
 						break;
 						
 					}
 				}
				if(type == Material.CHEST){ply.sendMessage("You can't open a chest through a block.");}
				if(type == Material.SIGN){ply.sendMessage("You can't use a sign through a block.");}
 				e.setCancelled(true);
 			}
 		}
 	}
 }
