 package com.wolvencraft.prison.mines.routines;
 
 import java.util.ConcurrentModificationException;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.constants.BlacklistState;
 import com.wolvencraft.prison.mines.util.data.RandomBlock;
 
 public class RandomTerrainRoutine {	
 	public static boolean run(Mine mine) {
		RandomBlock pattern = new RandomBlock(mine.getLocalBlocks());
 		Location one = mine.getRegion().getMinimum();
 		Location two = mine.getRegion().getMaximum();
 		World world = mine.getWorld();
 		
 		BlacklistState blState = mine.getBlacklist().getState();
 		
 		if(blState.equals(BlacklistState.DISABLED)) {
 	        for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 	            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 	                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 	                	try { 
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock = pattern.next();
 		                    original.setTypeIdAndData(newBlock.getItemTypeId(), newBlock.getData(), false);
 	                    }
                     	catch (ConcurrentModificationException cme) { Message.log(Level.SEVERE, "An error has occurred while running a generator [ConcurrentModificationException]"); continue; }
                     	catch (Exception ex) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Exception]"); continue; }
 	                	catch (IllegalAccessError iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [IllegalAccessError]"); continue; }
 	                	catch (Error iae) { Message.log(Level.SEVERE, "An error has occurred while running a generator [Error]"); continue; }
 	                }
 	            }
 	        }
 	        Message.debug("| Reset complete! BlacklistState.DISABLED");
 	        return true;
 		} else if(blState.equals(BlacklistState.BLACKLIST)) {
 			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 	            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 	                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 	                	try { 
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock = pattern.next();
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
 	        Message.debug("| Reset complete! BlacklistState.BLACKLIST");
 	        return true;
 		} else if(blState.equals(BlacklistState.WHITELIST)) {
 			for (int x = one.getBlockX(); x <= two.getBlockX(); x++) {
 	            for (int y = one.getBlockY(); y <= two.getBlockY(); y++) {
 	                for (int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
 	                	try { 
 		                	Block original = world.getBlockAt(x, y, z);
 		                    MaterialData newBlock = pattern.next();
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
 	        Message.debug("| Reset complete! BlacklistState.WHITELIST");
 	        return true;
 		} else {
 			Message.log(Level.SEVERE, "Unknown blacklist state! Aborting.");
 			Message.log(Level.SEVERE, "Abortion not legal in your state due to religious nutjobs");
 		}
 		
 		return false;
 	}
 }
