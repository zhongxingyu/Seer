 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class blockListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	public blockListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void pistonExtend(BlockPistonExtendEvent e) {
 		
 		if((e.getBlock().getTypeId() == 29) && (e.getBlock().getData() == 1) && (plugin.getConfig().getBoolean("fixPistonBug"))) {
 			
 			if((plugin.pistonBugBlock == null) && (e.getDirection().toString().equals("UP")) && (e.getBlock().getLocation().add(0.0D, 2.0D, 0.0D).getBlock().getTypeId() == 8)) {
 				
 				plugin.pistonBugBlock = e.getBlocks().get(0);
 				plugin.pistonBugId = e.getBlocks().get(0).getTypeId();
 				plugin.pistonBugData = e.getBlocks().get(0).getData();
 				
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
 					public void run() {
 						
 						Block b = blockListener.this.plugin.pistonBugBlock.getLocation().add(0.0D, 1.0D, 0.0D).getBlock();
 						
 						if((b.getTypeId() == blockListener.this.plugin.pistonBugId) && (b.getData() != blockListener.this.plugin.pistonBugData)) {
 							
 							StringBuilder bugPlayer = new StringBuilder();
 							List<Player> pll = blockListener.this.plugin.getServer().getWorld(b.getLocation().getWorld().getName()).getPlayers();
 							
 							for(Player p : pll) {
 								
 								if(p.getLocation().distance(b.getLocation()) >= 15)
 									continue;
 								if(bugPlayer.length() > 0) bugPlayer.append(", ");
 								bugPlayer.append(p.getDisplayName());
 									
 								}
 							
 							Player[] pl = blockListener.this.plugin.getServer().getOnlinePlayers();
 							
 							for(int i = 0 ; i < pl.length ; i++) {
 								
 								if(!blockListener.this.plugin.perm.has(pl[i], "rur.seePistonBugInfo"))
 									continue;
 								pl[i].sendMessage(blockListener.this.plugin.messages.getString("pistonbug.adminInfo1") + b.getLocation().getWorld().getName() + " | " + b.getLocation().getX() + ", " + b.getLocation().getY() + ", " + b.getLocation().getZ());
 				                pl[i].sendMessage(blockListener.this.plugin.messages.getString("pistonbug.adminInfo2") + bugPlayer.toString());
 								
 							}
 							
 							b.setData(blockListener.this.plugin.pistonBugData);
 								
 							}
 						
 						blockListener.this.plugin.pistonBugBlock =null;
 							
 						}
 						
 					
 				}, 1L);
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void appleDrop(LeavesDecayEvent e) {
 		
 		if(e.getBlock().getTypeId() == 18) {
 			
 			Random rand = new Random();
 			float num = rand.nextFloat()*100.0F;
 			
 			if(num <= plugin.config.getInt("appledropchance")) {
				e.getBlock().getDrops().add(new ItemStack(Material.APPLE, 1));
 			}
 			
 		}
 		
 	}
 	
 }
