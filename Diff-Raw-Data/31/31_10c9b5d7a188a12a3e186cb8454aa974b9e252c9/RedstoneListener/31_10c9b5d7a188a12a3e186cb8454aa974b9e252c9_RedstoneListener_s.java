 package me.heccubus.PowerGrid;
 
 import java.util.logging.Logger;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 /**
  * RedstoneListener for PowerGrid
  * 
  * @author Craig Coffey (aka. Heccubus)
  *
  */
 public class RedstoneListener extends BlockListener {
 	Logger log = Logger.getLogger("Minecraft");
 	
 	// Used to limit the depth of the recursion to avoid crushing 
 	// the server if common blocks are chosen accidentally.
 	private int maxDepth = -1;
 	private int currentDepth = 0;
 
 	// Grid blocks use for powered and unpowered states
 	private int GridOffBlockID = -1;
 	private int GridOnBlockID = -1;
 	
 	public static PowerGrid plugin;
 
 	public RedstoneListener(PowerGrid instance) {
 	    plugin = instance;
 	}
 
 	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
 		Block gridPowerBlock = event.getBlock();
 		int gridPowerBlockID = gridPowerBlock.getTypeId();		
 		Block gridBaseBlock = gridPowerBlock.getRelative(BlockFace.DOWN);
 		int gridBaseBlockID = gridBaseBlock.getTypeId();
 		int targetPowerBlockID = -1;
 		int targetBaseBlockID = -1;
 		currentDepth = 0;
 		
 		// Load config settings
 		maxDepth = PowerGrid.CONFIG.getInt("MaxGridSize",1000);
 		GridOffBlockID = PowerGrid.CONFIG.getInt("GridOffBlockID",22);
 		GridOnBlockID = PowerGrid.CONFIG.getInt("GridOnBlockID",41);
 
 		// If the torch is un-powered, then set the target type to AIR, otherwise set it to a powered torch
 		if (gridPowerBlockID == 75) { targetPowerBlockID = 0; } else { targetPowerBlockID = 76; }

 		// Toggle the base power grid block
 		if (gridBaseBlockID == GridOffBlockID) { targetBaseBlockID = GridOnBlockID; }
 		if (gridBaseBlockID == GridOnBlockID) { targetBaseBlockID = GridOffBlockID; }

 		// If the base block is of the correct type, and the power block is too... then start the recursion.
 		if ((gridBaseBlockID == GridOffBlockID && gridPowerBlockID == 76) || (gridBaseBlockID == GridOnBlockID && gridPowerBlockID == 75)) {
 			toggleTorch(gridBaseBlock, targetBaseBlockID, targetPowerBlockID);
 		}
 	}
 
 	private void toggleTorch(Block tb, int gbbID, int gpbID) {
 		Block targetBaseBlock = tb;
 		int targetBaseBlockID = targetBaseBlock.getTypeId();
 		Block tempBlock = null;
 		int tempBlockID = -1;
 		BlockFace bFaces[] = BlockFace.values();
 		BlockFace bFace = null;
 
 		// If the base block is not already of the proper type
 		if (targetBaseBlockID != gbbID) {
 			
 			// Set it to be of the proper type
 			targetBaseBlock.setTypeId(gbbID);
 
 			// Check the blocks around the base block, N,S,E,W and UP.
 			for (int i=0; i<=4; i++) {
 				bFace = bFaces[i];
 				tempBlock = targetBaseBlock.getRelative(bFace);
 				tempBlockID = tempBlock.getTypeId();
 
 				// If the current block is a powered or un-powered redstone torch, or air...
				if (tempBlockID == 0 || tempBlockID == 75 || tempBlockID == 76) {
 					
 					// If the base block is not near a redstone wire, set the target block to the proper type.
					if (!nearRedstoneWire(targetBaseBlock)) { tempBlock.setTypeId(gpbID); }
 
 					// Update the temp block ID variable now that it may have changed.
 					tempBlockID = tempBlock.getTypeId();
 
 					// If the temp block is a powered torch...
 					if (tempBlockID == 76) {
 						// Make sure it is placed on the proper face of the base block
 						BlockState state = tempBlock.getState();
 						((org.bukkit.material.RedstoneTorch)state.getData()).setFacingDirection(bFace);
 						state.update();
 					}
 				}
 				
 			}
 			currentDepth++;
 		}
 		
 		for (int i=0; i<=5; i++) {
 			bFace = bFaces[i];
 			tempBlock = targetBaseBlock.getRelative(bFace);
 			tempBlockID = tempBlock.getTypeId();
 			
 			if ((tempBlockID == GridOffBlockID || tempBlockID == GridOnBlockID) && currentDepth <= maxDepth) {
 				if (tempBlockID != gbbID) {
 					toggleTorch(tempBlock, gbbID, gpbID); 
 				}
 			}
 		}
 	}
 
	private boolean nearRedstoneWire(Block b) {
		Block tempBlock = null;
		int tempBlockID = -1;
		BlockFace bFaces[] = BlockFace.values();
		BlockFace bFace = null;
 
		for (int i=0; i<=3; i++) {
			bFace = bFaces[i];
			tempBlock = b.getRelative(bFace);
			tempBlockID = tempBlock.getTypeId();
			
			if (tempBlockID == 55) { return true; }
		}
 		return false;
 	}
 }
