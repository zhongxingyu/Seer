 package net.croxis.plugins.research;
 
 import org.bukkit.GameMode;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
import java.util.HashSet;
 
 public class RBlockListener extends BlockListener{
 	public void onBlockBreak(BlockBreakEvent event){
 		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
 			return;
 		if (event.getPlayer().hasPermission("research")){
			if(TechManager.players.get(event.getPlayer()).cantBreak.contains(event.getBlock().getTypeId()))
 				event.setCancelled(true);
 		}
 	}
 	
 	public void onBlockPlace(BlockPlaceEvent event){
 		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
 			return;
 		if (event.getPlayer().hasPermission("research")){
 			if(TechManager.players.get(event.getPlayer()).cantPlace.contains(event.getBlock().getTypeId()))
 				event.setCancelled(true);
 		}
 	}
 }
