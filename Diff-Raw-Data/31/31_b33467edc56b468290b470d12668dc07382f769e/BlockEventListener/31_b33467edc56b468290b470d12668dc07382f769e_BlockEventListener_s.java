 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 
 public class BlockEventListener implements Listener {
 
 	public ArcherGames plugin;
 
 	public BlockEventListener(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent event){
		if(ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus==2){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 	@EventHandler
 	public void onBlockBurn(final BlockBurnEvent event){
		if(ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus==2){
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent event){
		if(ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus==2){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 	@EventHandler
 	public void onBlockIgnite(final BlockIgniteEvent event){
		if(ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus==2){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 	@EventHandler
 	public void onInventoryOpen(final InventoryOpenEvent event){
		if(ScheduledTasks.gameStatus==1 || ScheduledTasks.gameStatus==2){
 			event.setCancelled(true);
 		}
 	}
 }
