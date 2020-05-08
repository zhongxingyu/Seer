 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.ambientseasons.listener;
 
 import me.ambientseasons.AmbientSeasons;
 
 import org.bukkit.Material;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 /**
  * 
  * @author Matt
  */
 public class BlockPlaceListener extends BlockListener {
 	private AmbientSeasons plugin;
 
 	public BlockPlaceListener(AmbientSeasons plugin) {
 		this.plugin = plugin;
		AmbientSeasons.log.info("Block Listener enabled.");
 	}
 
 	@Override
 	public void onBlockPlace(BlockPlaceEvent event)
         {
             if (event.getBlock().getType() == Material.CROPS)
             {
                 //What we do is register a Runnable to grow the crops.
                 //This way we can accurately speed up (or slow down) crop growth
                 //without growing them all at once.
                plugin.log.info("Biome: " + event.getBlock().getBiome().toString());
                 plugin.wheatMod.CreateWheatGrowthScheduler(event);
             }  
 	}
             
 	@Override
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (event.getBlock().getType() == Material.CROPS) {
 			// Remove it from the list if it's on there.
 			//plugin.WheatBlockLocations.remove(event.getBlock().getLocation());
 		}
 	}
 
 }
