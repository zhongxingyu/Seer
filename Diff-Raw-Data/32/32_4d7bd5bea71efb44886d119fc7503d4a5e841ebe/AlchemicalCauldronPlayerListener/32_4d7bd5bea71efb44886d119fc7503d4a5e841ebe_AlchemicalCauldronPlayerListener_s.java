 package com.circuitlocution.alchemicalcauldron;
 
 import java.util.logging.Logger;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerItemEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 
 public class AlchemicalCauldronPlayerListener extends PlayerListener
 {
 	private AlchemicalCauldron plugin;
 	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");
 	
 	public AlchemicalCauldronPlayerListener(AlchemicalCauldron instance)
 	{
 		plugin = instance;
 	}
 
 	@Override
 	public void onPlayerItem(PlayerItemEvent event){
 
 		log.info("AlchemicalCauldronPlayerListener.onPlayerItem() called with event: " + event.toString());
 		if (event.isCancelled()){
 			return;
 		}
 
 		Block block = event.getBlockClicked();
 		if (block == null){
 			return;
 		}
		ItemStack item = event.getItem();
 		Player p = event.getPlayer();
 		
 		plugin.process_event(event, block, item, p);
 		
 		
 	}
 	
 }
