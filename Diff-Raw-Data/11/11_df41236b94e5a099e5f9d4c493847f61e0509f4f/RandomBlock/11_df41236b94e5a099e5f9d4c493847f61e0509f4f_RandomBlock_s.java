 package com.wolvencraft.prison.mines.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.wolvencraft.prison.mines.mine.MineBlock;
 
 import org.bukkit.material.MaterialData;
 
 public class RandomBlock {
  
     List<MineBlock> weightedBlocks;
     
     /**
      * Creates a new RandomBlock instance with the composition provided
      * @param blocks Mine composition
      */
     public RandomBlock(List<MineBlock> blocks) {
     	weightedBlocks = new ArrayList<MineBlock>();
         double tally = 0;
         Message.debug("| Loading blocks from mine data");
         for (MineBlock block : blocks) {
             tally += block.getChance();
             weightedBlocks.add(new MineBlock(block.getBlock(), tally));
             Message.debug("| Block added: " + block.getBlock().getItemTypeId() + " (" + block.getChance() + ")");
         }
         Message.debug("| RandomBlock initialized successfully");
     }
     
     /**
      * Returns a random block according to the mine's composition
      * @return Random block
      */
    public MaterialData next()
    {
     	double r = new Random().nextDouble();
     	for (MineBlock block : weightedBlocks) {
     	    if (r <= block.getChance()) {
     	        return block.getBlock();
     	    }
     	}
    	//At this point, we've got a problem folks.
     	return null;
     }
 
 }
