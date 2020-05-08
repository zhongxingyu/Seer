 package com.yurijware.bukkit.selfclosingdoors;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.block.BlockInteractEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class SCDBlockListener extends BlockListener {
 	
 	public SCDBlockListener(){	
 	}
 	
 	@Override
 	public void onBlockInteract(BlockInteractEvent event){
 		final Block block = event.getBlock();
 		if(block.getType() != Material.WOODEN_DOOR) return;
		if(block.getData() > 0) return;
 		int sec = SelfClosingDoors.plugin.getClosingTime();
 		BukkitScheduler scheduler = SelfClosingDoors.plugin.getServer().getScheduler();
 		scheduler.scheduleSyncDelayedTask(SelfClosingDoors.plugin, new Runnable(){
 			@Override
 			public void run() {
 				Block upperblock = block.getFace(BlockFace.UP);
				if(block.getData() == 4) block.setData((byte) 0);
				if(upperblock.getType() == Material.WOODEN_DOOR &&
						upperblock.getData() == 12) upperblock.setData((byte) 8);
 			}
 		}, sec * 20);
 	}
 	
 }
