 package com.caindonaghey.commandbin.listeners;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.caindonaghey.commandbin.CommandBin;
 
 public class AxeListener implements Listener {
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e) {
 		Block block = e.getBlock();
 		Material type = block.getType();
 		Player player = e.getPlayer();
 		
 		if(CommandBin.woodCutter) {
 			if(player.hasPermission("CommandBin.axe")) {
 				if(player.getItemInHand().getType() == Material.IRON_AXE) {
 					if(block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS) {
 						if(type == Material.LOG) {
 							for (int i = 0; i < 30; i++) {
 								if(block.getRelative(0, i, 0).getType() == Material.LOG) {
 									block.getRelative(0, i, 0).getWorld().playEffect(block.getRelative(0, i, 0).getLocation(), Effect.SMOKE, 5);
 									block.getRelative(0, i, 0).getWorld().playEffect(block.getRelative(0, i, 0).getLocation(), Effect.MOBSPAWNER_FLAMES, 5);
									block.getRelative(0, i, 0).breakNaturally(new ItemStack(Material.LOG, 1));
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 				
 	}
 
 }
