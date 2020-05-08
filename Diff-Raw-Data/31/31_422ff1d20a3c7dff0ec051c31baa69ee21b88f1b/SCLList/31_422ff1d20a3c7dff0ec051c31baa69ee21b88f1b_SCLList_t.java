 package com.webkonsept.bukkit.simplechestlock.locks;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.UUID;
 
 import org.bukkit.Chunk;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.webkonsept.bukkit.simplechestlock.SCL;
 
 public class SCLList implements Runnable {
 	SCL plugin;
 	public HashMap<Location,SCLItem> list = new HashMap<Location,SCLItem>();
 	int key = 0;
 	HashSet<SCLItem> deferredItems = new HashSet<SCLItem>();
 	
 	public SCLList(SCL instance) {
 		plugin = instance;
 	}
 	public void retryDeferred(String worldName){
 		List<SCLItem> successful = new ArrayList<SCLItem>();
 		for (SCLItem item : deferredItems){
 			if (item.worldName.equals(worldName)){
 				try {
 					item.retryLocation(plugin);
 				} catch (ParseException e) {
 					plugin.crap("Failed to retry item loading, parse error during location retry: "+e.getMessage());
 				}
 				successful.add(item);
 			}
 		}
 		for (SCLItem resolved : successful){
 			deferredItems.remove(resolved);
 		}
 	}
 	public void load (String filename) {
 		File lockedItemFile = new File (plugin.getDataFolder(),filename);
 		plugin.babble("Reading locks from "+lockedItemFile.getAbsolutePath());
 		list.clear();
 		if (!lockedItemFile.exists()){
 			plugin.getDataFolder().mkdir();
 			try {
 				plugin.babble("Attempting to create "+lockedItemFile.getAbsolutePath());
 				lockedItemFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				plugin.crap("FAILED TO CREATE FILE FOR LOCKS ("+filename+"): "+e.getMessage());
 			}
 		}
 		
 		int lineNumber = 0;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(lockedItemFile));
 			String line = "";
 			
 			while (line != null){
 				line = in.readLine();
 				lineNumber++;
 				if (line != null && !line.matches("^\\s*#")){
 					try {
 						SCLItem item = new SCLItem(plugin,line);
 						list.put(item.getLocation(),item);
 					} catch(ParseException e){
 						plugin.crap("Failed to parse line "+lineNumber+" from locks file: "+e.getMessage());
 					}
 				}
 				else {
 					plugin.babble("Done reading protected locations!");
 				}
 			}
 			in.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
 		} catch (IOException e) {
 			e.printStackTrace();
 			plugin.crap("Okay, crap, IOException while reading "+filename+": "+e.getMessage());
 		}
 	}
 	public void save(String filename) {
 		File lockedItemsFile = new File (plugin.getDataFolder(),filename);
 		if (!lockedItemsFile.exists()){
 			plugin.getDataFolder().mkdir();
 			plugin.out("Attempting to create "+lockedItemsFile.getAbsolutePath());
 			try {
 				lockedItemsFile.createNewFile();
 			} catch (IOException e) {
 				plugin.crap("FAILED TO CREATE FILE FOR LOCKS CALLED "+filename);
 				e.printStackTrace();
 			}
 		}
 		
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(lockedItemsFile));
 			for (SCLItem item : list.values()){
 				String line = item.toString();
 				plugin.babble("Saved: "+line);
 				out.write(line);
 				out.newLine();
 			}
 			out.flush();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
 		} catch (IOException e) {
 			e.printStackTrace();
 			plugin.crap("Okay, crap, IOExeption while writing "+filename+": "+e.getMessage());
 		}
 	}
 	public void suck(){
 		HashSet<UUID> sucked = new HashSet<UUID>();
 		for (SCLItem item : list.values() ){
 			if (!item.isLocationDeferred()){
 				Block block = item.getLocation().getBlock();
 				
 				Chunk chunk = block.getChunk();
 				if (chunk.isLoaded() && plugin.canSuck.contains(block.getType())){
 					if (block.getState() instanceof ContainerBlock){
 						ContainerBlock container = (ContainerBlock)block.getState();
 						Inventory inventory = container.getInventory();
 						if (inventory.firstEmpty() != -1){ // No sense in sucking at all if it doesn't have space to begin with!
 							ArrayList<Entity> entityList = new ArrayList<Entity>();
 							for (Chunk inChunk : getNearbyChunks(block,plugin.suckRange)){
 								for (Entity entity : inChunk.getEntities()){
 									if (entity instanceof Item && entity.getLocation().distance(block.getLocation()) <= plugin.suckRange){
 										entityList.add(entity);
 									}
 								}
 							}
 							for (Entity entity : entityList){
 								ItemStack original = ((Item)entity).getItemStack();
 								ItemStack itemFound = original.clone();
 								if ( inventory.firstEmpty() != -1 && !sucked.contains(entity.getUniqueId()) ){ 
 									inventory.addItem(itemFound);
 									entity.remove();
 									sucked.add(entity.getUniqueId());
 								}
 								else {
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	public HashSet<Chunk> getNearbyChunks(Block origin,int range){
 		HashSet<Chunk> chunks = new HashSet<Chunk>();
 		for (Block block : getNearbyBlocks(origin,range)){
 			chunks.add(block.getChunk());
 		}
 		return chunks;
 	}
 	public List<Block> getNearbyBlocks(Block origin,int range){
 		ArrayList<Block> blocks = new ArrayList<Block>();
 		Location cornerOne = origin.getLocation().add(range,range,range);
 		Location cornerTwo = origin.getLocation().subtract(range,range,range);
 		World world = origin.getWorld();
 		for (int x = cornerTwo.getBlockX(); x<=cornerOne.getBlockX();x++){
 			for (int y = cornerTwo.getBlockY(); y<=cornerOne.getBlockY();y++){
 				for (int z = cornerTwo.getBlockZ(); z<=cornerOne.getBlockZ();z++){
 					blocks.add(world.getBlockAt(x,y,z));
 				}
 			}
 		}
 		return blocks;
 	}
 	public String getOwner(Block block){
 		if (block == null) return null;
 		if (list.containsKey(block.getLocation())){
 			return list.get(block.getLocation()).getOwner();
 		}
 		else {
 			return null;
 		}
 	}
 	public SCLItem getItem(Block block){
 	    if (block == null) return null;
 	    if (list.containsKey(block.getLocation())){
 	        return list.get(block.getLocation());
 	    }
 	    else {
 	        return null;
 	    }
 	}
 	public boolean isLocked(Block block){
 		if (block == null) return false;
 		if (list == null) return false;
 		if (list.containsKey(block.getLocation())){
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	public boolean isComboLocked(Block block){
 		if (isLocked(block)){
 			plugin.babble("> Locked");
 			 
 			SCLItem item = list.get(block.getLocation());
 			if (item != null){
 				if (item.isComboLocked()){
 					plugin.babble("> Combo");
 					return true;
 				}
 				else {
 					plugin.babble("> Not a combo");
 					return false;
 				}
 			}
 			else {
 				plugin.babble("> Locked item is null?!");
 				return false;
 			}
 		}
 		else {
 			plugin.babble("> Not locked!");
 			return false;
 		}
 	}
 	public Integer lock(Player player,Block block){
 		if (player == null || block == null || list == null) return 0;
 		if (plugin.canLock(block)){
 			int lockedItems = 0;
             String lockAs = player.getName();
             if (plugin.locksAs.containsKey(lockAs)){
                 lockAs = plugin.locksAs.get(lockAs);
             }
             
 			if (plugin.lockpair && plugin.canDoubleLock(block)){
 				lockedItems = this.addNeighboring(block,lockAs);
 			}
 			else {
 				SCLItem newItem = new SCLItem(lockAs,block);
				newItem.setTrusted(plugin.trustHandler.getTrusteesCopy(lockAs));
 				list.put(block.getLocation(),newItem);
 				lockedItems = 1;
 			}
 			save("Chests.txt");
 			return lockedItems;
 		}
 		else {
 			return 0;
 		}
 	}
 	public Integer lock(Player player,Block block,DyeColor[] combo){
 		if (player == null || block == null || list == null || combo.length != 3) return 0;
 		if (plugin.canLock(block)){
 			int lockedItems = 0;
             String lockAs = player.getName();
             if (plugin.locksAs.containsKey(lockAs)){
                 lockAs = plugin.locksAs.get(lockAs);
             }
 			if (plugin.lockpair && plugin.canDoubleLock(block)){
 				lockedItems = this.addNeighboring(block,lockAs,combo);
 			}
 			else {
 				SCLItem newItem = new SCLItem(lockAs,block,combo);
				newItem.setTrusted(plugin.trustHandler.getTrusteesCopy(lockAs));
 				list.put(block.getLocation(),newItem);
 				lockedItems = 1;
 			}
 			return lockedItems;
 		}
 		else {
 			return 0;
 		}
 	}
 	public String getComboString(Block block) {
 		if (list.containsKey(block.getLocation())){
 			SCLItem item = list.get(block.getLocation());
 			return item.getComboString();
 		}
 		else {
 			return " ..NOT LOCKED.. ";
 		}
 	}
 	public Integer unlock(Block block){
 		if (block == null) return 0;
 		if (this.isLocked(block)){
 			if (list != null){
 				
 				Integer unlockedItems = 0;
 				if (plugin.lockpair && plugin.canDoubleLock(block)){
 					unlockedItems = this.removeNeighboring(block);
 				}
 				else {
 					list.remove(block.getLocation());
 					unlockedItems = 1;
 				}
 				save("Chests.txt");
 				return unlockedItems;
 			}
 			else {
 				return 0;
 			}
 		}
 		return 0;
 	}
 	public HashSet<Block> getNeighbours (Block block) {
 		HashSet<Block> neighbours = new HashSet<Block>();
 		neighbours.add(block);
 		neighbours.add(block.getRelative(BlockFace.NORTH));
 		neighbours.add(block.getRelative(BlockFace.SOUTH));
 		neighbours.add(block.getRelative(BlockFace.EAST));
 		neighbours.add(block.getRelative(BlockFace.WEST));
 		
 		// For doors
 		if (plugin.lockIncludeVertical.contains(block.getType())){
 			plugin.babble(block.getType().toString()+" is vertically locked.");
 			HashSet<Block> additionalNeighbours = new HashSet<Block>();
 			for (Block neighbour : neighbours){
 				Block above = neighbour.getRelative(BlockFace.UP);
 				additionalNeighbours.add(above);
 				
 				Block below = neighbour.getRelative(BlockFace.DOWN);
 				additionalNeighbours.add(below);
 				
 			}
 			neighbours.addAll(additionalNeighbours);
 		}
 		else {
 			plugin.babble(block.getType().toString()+" is NOT vertically locked.");
 		}
 		
 		return neighbours;
 	}
 	private Integer removeNeighboring (Block block) {
 		String playerName = getOwner(block);
 		Integer additionalUnlocked = 0;
 		for (Block currentNeighbour : this.getNeighbours(block)){
 			if (currentNeighbour.getType().equals(block.getType())){
 				String owner = this.getOwner(currentNeighbour);
 				if (owner != null && owner.equals(playerName)){
 					list.remove(currentNeighbour.getLocation());
 					additionalUnlocked++;
 				}
 			}
 		}
 		return additionalUnlocked;
 	}
 	private Integer addNeighboring (Block block,String ownerName,DyeColor[] combo) {
 		Integer additionalItemsLocked = 0;
 		for (Block currentNeighbour : this.getNeighbours(block)){
 			if (currentNeighbour.getType().equals(block.getType())){
 			    SCLItem newItem = new SCLItem(ownerName,currentNeighbour,combo);
			    newItem.setTrusted(plugin.trustHandler.getTrusteesCopy(ownerName));
 				list.put(currentNeighbour.getLocation(),newItem);
 				plugin.babble("ComboLocked a block by association");
 				additionalItemsLocked++;
 			}
 		}
 		return additionalItemsLocked;
 	}
 	private Integer addNeighboring (Block block,String ownerName) {
 		Integer additionalItemsLocked = 0;
 
 		for (Block currentNeighbour : this.getNeighbours(block)){
 			if (currentNeighbour.getType().equals(block.getType())){
 				if (list.containsKey(currentNeighbour.getLocation())){
 					plugin.babble("Uhm, this "+currentNeighbour.getType().toString().toLowerCase()+" is already locked.");
 				}
 				else {
 					plugin.babble("Locking "+currentNeighbour.getType().toString().toLowerCase()+" at "+currentNeighbour.getLocation().toString());
 					SCLItem newItem = new SCLItem(ownerName,currentNeighbour);
	                newItem.setTrusted(plugin.trustHandler.getTrusteesCopy(ownerName));
 					list.put(currentNeighbour.getLocation(), newItem);
 					additionalItemsLocked++;
 				}
 			}
 			else {
 				plugin.babble("Not locking "+currentNeighbour.getType().toString().toLowerCase()+"!  Current type is "+block.getType().toString().toLowerCase());
 			}
 		}
 		return additionalItemsLocked;
 	}
 	@Override
 	public void run() {
 		if (plugin.lockedChestsSuck){
 			this.suck();
 		}
 	}
 }
