 package com.ugleh.instntly;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 public class InsTNTlyListener implements Listener{
 	private static final InsTNTly instance = new InsTNTly();
 	
 	public static final InsTNTly getPlugin() {
 		return instance;
 		}
 	
 	 
 	@EventHandler
 	public void BlockRedstone(BlockRedstoneEvent event){
 		if(event.getNewCurrent() >= 1){ //If the current of the block, be it a lever or redstone wire is ON
 			Block blk = event.getBlock(); //Set the events block to the variable 'blk', because we are going to be using this alot.
 			ArrayList<Block> tntBlocks = new ArrayList<Block>(); //There may be more than 1 TNT block connected to a redstone wire.
 			Boolean tntExists = false; //I would of checked if tntBlk was null, but that creates a NPE, so I made a Boolean variable.
 			if(blk.getRelative(BlockFace.NORTH).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.NORTH));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.EAST).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.EAST));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.SOUTH).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.SOUTH));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.WEST).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.WEST));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getType().equals(Material.TNT)){
 				tntBlocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST));
 				tntExists = true;
 			}else if(blk.getRelative(BlockFace.DOWN).getType().equals(Material.TNT)){
				tntBlocks.add(blk.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST));
 				tntExists = true;
 			}
 			if(tntExists){ //If tntExists is set to true, then there is a TNT Block in the area.
 				for(Block tntBlk : tntBlocks){
 					tntBlk.setType(Material.AIR); //Sets the tntBlk to air so it does not trigger 2 explosions
 					tntBlk.getWorld().createExplosion(tntBlk.getLocation(), 5); //Create an explosion, same area, same radius.	
 				}
 			}
 			}
 			
 		}
 		
 }
 
 
