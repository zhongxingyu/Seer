 package com.wolvencraft.prison.mines.routines;
 
 import java.util.ConcurrentModificationException;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.RandomBlock;
 
 public class CustomTerrainRoutine {	
 	public static boolean run(Mine mine) {
 		RandomBlock pattern = new RandomBlock(mine.getBlocks());
 		Location one = mine.getRegion().getMinimum();
 		Location two = mine.getRegion().getMaximum();
 		World world = mine.getWorld();
 		
     	if(mine.getBlacklist().getEnabled()) {        		
     		if(mine.getBlacklist().getWhitelist()) {
     			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 		            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 		                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 		                	try { 
 			                	Block original = world.getBlockAt(x, y, z);
			                	MaterialData newBlock;
				                if((Math.abs(one.getBlockX() - x) < 3 || Math.abs(two.getBlockX() - x) < 3) ||
				                		(Math.abs(one.getBlockY() - y) < 3 || Math.abs(two.getBlockY() - y) < 3) ||
					    	        	(Math.abs(one.getBlockZ() - z) < 3 || Math.abs(two.getBlockZ() - z) < 3)) { newBlock = mine.getMostCommonBlock().getBlock(); }
				                else { newBlock = pattern.next(); }
 			                    if(mine.getBlacklist().getBlocks().contains(original.getState().getData()))
 			                    	original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false);
 		                    }
 	                    	catch (ConcurrentModificationException cme) { Message.log(Level.SEVERE, "An error has occurred while running a generator [ConcurrentModificationException]"); continue; }
 	                    	catch (Exception ex) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Exception]"); continue; }
 		                	catch (IllegalAccessError iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [IllegalAccessError]"); continue; }
 		                	catch (Error iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Error]"); continue; }
 		                }
 		            }
 		         }
     	        Message.debug("| Reset complete! BL, WL");
     			return true;
     		}
     		else {
     			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 		            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 		                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 		                	try { 
 			                	Block original = world.getBlockAt(x, y, z);
			                	MaterialData newBlock;
			                    if((Math.abs(one.getBlockX() - x) < 3 || Math.abs(two.getBlockX() - x) < 3) ||
				    	        	(Math.abs(one.getBlockY() - y) < 3 || Math.abs(two.getBlockY() - y) < 3) ||
				    	        	(Math.abs(one.getBlockZ() - z) < 3 || Math.abs(two.getBlockZ() - z) < 3)) { newBlock = mine.getMostCommonBlock().getBlock(); }
			                    else { newBlock = pattern.next(); }
 			                    if(!mine.getBlacklist().getBlocks().contains(original.getState().getData()))
 			                    	original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false);
 		                	}
 	                    	catch (ConcurrentModificationException cme) { Message.log(Level.SEVERE, "An error has occurred while running a generator [ConcurrentModificationException]"); continue; }
 	                    	catch (Exception ex) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Exception]"); continue; }
 		                	catch (IllegalAccessError iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [IllegalAccessError]"); continue; }
 		                	catch (Error iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Error]"); continue; }
 		                }
 		            }
 		        }
     	        Message.debug("| Reset complete! BL, No WL");
     			return true;
     		}
     	}
     	else {
 	        for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 	            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 	                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 	                	
 	                	try {
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock;
 		                    if((Math.abs(one.getBlockX() - x) < 3 || Math.abs(two.getBlockX() - x) < 3) ||
 			    	        		(Math.abs(one.getBlockY() - y) < 3 || Math.abs(two.getBlockY() - y) < 3) ||
 			    	        			(Math.abs(one.getBlockZ() - z) < 3 || Math.abs(two.getBlockZ() - z) < 3)) { newBlock = mine.getMostCommonBlock().getBlock(); }
 		                    else { newBlock = pattern.next(); }
 		                    original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false);
 	                    }
                     	catch (ConcurrentModificationException cme) { Message.log(Level.SEVERE, "An error has occurred while running a generator [ConcurrentModificationException]"); continue; }
                     	catch (Exception ex) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Exception]"); continue; }
 	                	catch (IllegalAccessError iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [IllegalAccessError]"); continue; }
 	                	catch (Error iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Error]"); continue; }
 	                }
 	            }
 	        }
 	        Message.debug("| Reset complete! No BL, No WL");
 	        return true;
     	}
 	}
 }
