 /*
  * Copyright (C) 2011  Joshua Reetz
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * ClayGen block listener
  * @author Tux2
  */
 public class ClayGenListener extends PluginListener {
     public static final int CLAY = 82;
 	private final ClayGen plugin;
     public static final int GRAVEL = 13;
     boolean debug = false;
 
     public ClayGenListener(final ClayGen plugin) {
         this.plugin = plugin;
     }
     public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {
     	if(blockPlaced.getType() == GRAVEL) {
     		plugin.gravelPlaced(blockPlaced);
     	}
     	return false;
     }
     
     public boolean onFlow(Block blockFrom, Block blockTo) {
    	Block newtoblock = blockTo.getWorld().getBlockAt(blockTo.getX(), blockTo.getY(), blockTo.getZ());
     	plugin.convertBlocks(blockFrom, newtoblock);
     	return false;
     }
     
     public boolean onBlockPhysics (Block block, boolean placed) {
     	if(plugin.debug) {
     		System.out.println("Physics event here on block id: " + block.getType());
     	}
     	if(block.getType() == CLAY) {
     		if(plugin.waterenabled && plugin.lavaenabled) {
         		if(!plugin.hasBlockNextTo(block, 8, 9, 10, 11)) {
         			plugin.clayWaterRemoved(block);
         		}
     		}else if(plugin.waterenabled) {
     			if(!plugin.hasBlockNextTo(block, 8, 9)) {
         			plugin.clayWaterRemoved(block);
         		}
     		}else if(plugin.lavaenabled) {
     			if(!plugin.hasBlockNextTo(block, 10, 11)) {
         			plugin.clayWaterRemoved(block);
         		}
     		}
     	}
     	return false;
     }
     
     public boolean onBlockBreak(Player player, Block block)  {
     	if(block.getType() == CLAY) {
     		int claycount = 0;
     		if(plugin.customdrops) {
     			if(plugin.clayblocks.containsKey(plugin.compileBlockString(block))) {
         			ClayDelay theblock = plugin.clayblocks.get(plugin.compileBlockString(block));
         			claycount = plugin.getNumberOfDrops(theblock);
         			plugin.clayblocks.remove(plugin.compileBlockString(block));
         			plugin.saveClayBlocks();
         		}else {
             		//claycount = block.getData();
         		}
     		}else {
     			claycount = plugin.defaultclaydrop;
     		}
     		
     		//If the clay count is zero, this must be a world generated block.
     		//Set it to the default amount of clay.
     		if(claycount == 0) {
     			claycount = plugin.defaultclaydrop;
     		}
     		//drop the items
     		for(int i = 0; i < claycount; i++) {
     			//TODO: make sure this doesn't drop the clay in the exact position. have it a little random.
        		block.getWorld().dropItem(block.getX(), block.getY(), block.getZ(), 337, 1);
     		}
     		//turn the block to air, disabling further drops.
     		block.setType(0);
     		block.update();
     		return true;
     	}
     	return false;
     }
 
     //put all Block related code here
 }
