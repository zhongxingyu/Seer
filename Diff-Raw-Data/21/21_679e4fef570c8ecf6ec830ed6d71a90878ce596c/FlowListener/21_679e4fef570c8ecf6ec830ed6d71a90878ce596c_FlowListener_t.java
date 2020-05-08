 package com.evilplatypus.bukkit.flow;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockListener;
 
 /**
  * FlowControl for Bukkit
  *
  * @author Jauneau Vincent
  */
 public abstract class FlowListener extends BlockListener {
     
     @Override
     public void onBlockFlow(BlockFromToEvent event) {
     	if (event.isCancelled()) return;
 
     	Block block = event.getBlock();
     	if (isLiquid(block) && isPowered(block)) {
 			stopFlow(block);
 			event.setCancelled(true);
 
     	}
     }
     
     protected abstract boolean isLiquid(Block block);
 
     private boolean isPowered(Block block) {
     	BlockFace[] faces = {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};
     	
     	for (int i = 0; i < faces.length; i++) {
     		Block b = block.getFace(faces[i]);
    		if (b.isBlockPowered()) {
     			return true;
     		}
     	}
     	return false;
     }
        
     private void stopFlow(Block block) {
     	BlockFace[] faces = {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};
     	for (int i = 0; i < faces.length; i++) {
     		Block b = block.getFace(faces[i]);
        	if (isLiquid(b) && b.getData() > 0) {
     			b.setType(Material.AIR);
     		}
     	}
     }
 
 }
