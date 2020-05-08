 package com.wolvencraft.prison.mines.generation;
 
import java.util.ConcurrentModificationException;

 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.RandomBlock;
 
 public class RandomGenerator implements BaseGenerator {
 	public final String NAME;
 	public final String DESCRIPTION;
 	
 	public RandomGenerator() {
 		NAME = "RANDOM";
 		DESCRIPTION = "Resets the contents of the mine randomly according to the persentage set by the mine configuration";
 	}
 	
 	public boolean run(Mine mine) {
 		RandomBlock pattern = new RandomBlock(mine.getBlocks());
 		Location one = mine.getRegion().getMinimum();
 		Location two = mine.getRegion().getMaximum();
 		World world = mine.getWorld();
 		
     	if(mine.getBlacklist().getEnabled()) {        		
     		if(mine.getBlacklist().getWhitelist()) {
     			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 		            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 		                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock = pattern.next();
		                    if(mine.getBlacklist().getBlocks().contains(original.getState().getData())) {
		                    	try { original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false); }
		                    	catch (ConcurrentModificationException cme) { Message.debug("ConcurrentModificationException thrown"); continue; }
		                    	catch (Exception ex) { Message.debug("Unknown exceptiont thrown"); continue; }
		                    }
 		                }
 		            }
 		        }
     	        Message.debug("Reset complete! BL, WL");
     			return true;
     		}
     		else {
     			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 		            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 		                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock = pattern.next();
		                    if(!mine.getBlacklist().getBlocks().contains(original.getState().getData())) {
		                    	try { original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false); }
		                    	catch (ConcurrentModificationException cme) { Message.debug("ConcurrentModificationException thrown"); continue; }
		                    	catch (Exception ex) { Message.debug("Unknown exceptiont thrown"); continue; }
		                    }
 		                }
 		            }
 		        }
     	        Message.debug("Reset complete! BL, No WL");
     			return true;
     		}
     	}
     	else {
 	        for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 	            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 	                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 	                	Block original = world.getBlockAt(x, y, z);
 	                    MaterialData newBlock = pattern.next();
	                    try { original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false); }
                    	catch (ConcurrentModificationException cme) { Message.debug("ConcurrentModificationException thrown"); continue; }
                    	catch (Exception ex) { Message.debug("Unknown exceptiont thrown"); continue; }
 	                }
 	            }
 	        }
 	        Message.debug("Reset complete! No BL, No WL");
 	        return true;
     	}
 	}
 
 	@Override
 	public boolean init(Mine mine) { return true; }
 
 	@Override
 	public boolean remove(Mine mine) { return true; }
 
 	@Override
 	public String getName() { return NAME; }
 
 	@Override
 	public String getDescription() { return DESCRIPTION; }
 }
