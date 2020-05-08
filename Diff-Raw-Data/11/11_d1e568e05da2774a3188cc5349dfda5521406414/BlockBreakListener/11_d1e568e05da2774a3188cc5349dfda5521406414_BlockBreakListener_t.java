 package com.wolvencraft.prison.mines.events;
 
 import java.util.List;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.DisplaySign;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.Protection;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class BlockBreakListener implements Listener
 {
 	public BlockBreakListener(PrisonMine plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 	
 	@EventHandler
 	public void onBlockbreak(BlockBreakEvent event) {
 		if(event.isCancelled()) return;
 		Message.debug("BlockBreakEvent caught");
 		
 		Player player = event.getPlayer();
 
 		List<Mine> mines = PrisonMine.getMines();
 		
 		Block b = event.getBlock();
 		String errorString = PrisonMine.getLanguage().PROTECTION_BREAK;
 		errorString.replaceAll("<BLOCK>", b.getType().name().toLowerCase().replace("_", " "));
 		
 		for(Mine mine : mines) {
 			Message.debug("Checking mine " + mine.getName());
 			
			if(mine.getRegion().isLocationInRegion(b.getLocation())) mine.recountBlocks();
 			if(!mine.getProtectionRegion().isLocationInRegion(b.getLocation())) continue;
 			
 			Message.debug("Location is in the mine protection region");
 			
 			if(!player.hasPermission("prison.mine.protection.break." + mine.getName()) && !player.hasPermission("prison.mine.protection.break") && !player.hasPermission("prison.mine.bypass.break")) {
 				Message.debug("Player " + event.getPlayer().getName() + " does not have permission to break blocks in the mine");
 				Message.sendError(player, errorString);
 				event.setCancelled(true);
 				return;
 			}
 				
 			if(!mine.getProtection().contains(Protection.BLOCK_BREAK)) {
 				Message.debug("Mine has no block breaking protection enabled");
 				continue;
 			}
 				
 			Message.debug("Mine has a block breaking protection enabled");
 			if(mine.getBreakBlacklist().getEnabled()) {
 				Message.debug("Block breaking blacklist detected");
 				boolean found = false;
 				for(MaterialData block : mine.getBreakBlacklist().getBlocks()) {
 					if(block.getItemType().equals(b.getType())) {
 						found = true;
 						break;
 					}
 				}
 				
 				if((mine.getBreakBlacklist().getWhitelist() && !found) || (!mine.getBreakBlacklist().getWhitelist() && found)) {
 					Message.debug("Player " + player.getName() + " broke a black/whitelisted block in the mine!");
 					Message.sendError(player, errorString);
 					event.setCancelled(true);
 					return;
 				}
 			}
 			else {
 				Message.debug("No block breaking blacklist detected");
 				Message.sendError(player, errorString);
 				event.setCancelled(true);
 			}
 		}
 		Message.debug("Broken block was not in the mine region");
 		signCheck(event);
 		return;
 	}
 	
 	public void signCheck(BlockBreakEvent event) {
 		if(event.isCancelled()) return;
         BlockState b = event.getBlock().getState();
         if(b instanceof Sign) {
         	Message.debug("Checking for defined signs...");
         	DisplaySign sign = DisplaySign.get((Sign) b);
         	if(sign == null) return;
         	
         	if(sign.delete()) Message.sendSuccess(event.getPlayer(), "Sign successfully removed");
         	else {
         		Message.sendError(event.getPlayer(), "Error removing sign!");
         		event.setCancelled(true);
         	}
         	return;
         }
         else return;
 	}
 }
