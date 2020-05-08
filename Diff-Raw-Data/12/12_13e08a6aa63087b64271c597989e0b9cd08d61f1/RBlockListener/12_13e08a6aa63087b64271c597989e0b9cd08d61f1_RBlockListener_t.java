 package net.croxis.plugins.research;
 
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class RBlockListener extends BlockListener{
 	public void onBlockBreak(BlockBreakEvent event){
		if(TechManager.players.get(event.getPlayer()).cantBreak.contains(event.getBlock().getTypeId()) && !event.getPlayer().hasPermission("research.override"))
 			event.setCancelled(true);
 	}
 	
 	public void onBlockPlace(BlockPlaceEvent event){
		if(TechManager.players.get(event.getPlayer()).cantPlace.contains(event.getBlock().getTypeId()) && !event.getPlayer().hasPermission("research.override"))
 			event.setCancelled(true);
 	}
 }
