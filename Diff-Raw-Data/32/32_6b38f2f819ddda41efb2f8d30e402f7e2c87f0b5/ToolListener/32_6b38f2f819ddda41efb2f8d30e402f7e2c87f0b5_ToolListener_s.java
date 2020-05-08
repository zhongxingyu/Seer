 package com.wolvencraft.prison.mines.events;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.MineFlag;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class ToolListener implements Listener {
 	
 	public ToolListener(PrisonMine plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if(event.isCancelled()) return;
 		
 		Block b = event.getBlock();
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(b.getLocation())) continue;
 			
 			if(mine.hasFlag(MineFlag.NoToolDamage)) {
 				Player player = event.getPlayer();
 				if(!player.hasPermission("prison.mine.flags.notooldamage." + mine.getId()) && !player.hasPermission("prison.mine.flags.notooldamage")) { continue; }
 
 				Message.debug("NoToolDamage flag is in effect");
				event.setCancelled(true);
				b.breakNaturally();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockDamage(BlockDamageEvent event) {
 			if(event.isCancelled()) return;
 			
 			Block b = event.getBlock();
 			
 			for(Mine mine : PrisonMine.getLocalMines()) {
 				if(!mine.getRegion().isLocationInRegion(b.getLocation())) continue;
 				
 				if(mine.hasFlag(MineFlag.SuperTools)) {
 					Player player = event.getPlayer();
 					if(!player.hasPermission("prison.mine.flags.supertools." + mine.getId()) && !player.hasPermission("prison.mine.flags.supertools")) { continue; }
 					Message.debug("SuperTools flag is in effect");
 					b.breakNaturally();
 				}
 			}
 	}
 }
