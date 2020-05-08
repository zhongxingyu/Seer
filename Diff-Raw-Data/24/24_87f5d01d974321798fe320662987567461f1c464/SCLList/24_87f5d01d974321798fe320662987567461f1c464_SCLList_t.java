 package com.webkonsept.bukkit.simplechestlock;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 public class SCLList implements Runnable {
 	SCL plugin;
 	HashMap<Location,SCLItem> list = new HashMap<Location,SCLItem>();
 	int key = 0;
 	
 	public SCLList(SCL instance) {
 		plugin = instance;
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
 	public String getOwner(Block block){
 		if (block == null) return null;
 		if (list.containsKey(block.getLocation())){
 			return list.get(block.getLocation()).getOwner();
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
 			if (plugin.lockpair && plugin.canDoubleLock(block)){
 				lockedItems = this.addNeighboring(block,player);
 			}
 			else {
 				list.put(block.getLocation(),new SCLItem(player,block));
 				lockedItems = 1;
 			}
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
 			if (plugin.lockpair && plugin.canDoubleLock(block)){
 				lockedItems = this.addNeighboring(block, player,combo);
 			}
 			else {
 				list.put(block.getLocation(),new SCLItem(player,block,combo));
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
 				return unlockedItems;
 			}
 			else {
 				return 0;
 			}
 		}
 		return 0;
 	}
 	protected HashSet<Block> getNeighbours (Block block) {
 		HashSet<Block> neighbours = new HashSet<Block>();
 		neighbours.add(block);
 		neighbours.add(block.getRelative(BlockFace.NORTH));
 		neighbours.add(block.getRelative(BlockFace.SOUTH));
 		neighbours.add(block.getRelative(BlockFace.EAST));
 		neighbours.add(block.getRelative(BlockFace.WEST));
 		// For doors
 		//neighbours.add(block.getRelative(BlockFace.UP));
 		//neighbours.add(block.getRelative(BlockFace.DOWN));
 		
 		
 		HashSet<Block> additionalNeighbours = new HashSet<Block>();
 		for (Block neighbour : neighbours){
 			Block above = neighbour.getRelative(BlockFace.UP);
 			additionalNeighbours.add(above);
 			
 			Block below = neighbour.getRelative(BlockFace.DOWN);
 			additionalNeighbours.add(below);
 			
 		}
 		neighbours.addAll(additionalNeighbours);
 		
 		
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
 	private Integer addNeighboring (Block block,Player owner,DyeColor[] combo) {
 		Integer additionalItemsLocked = 0;
		
 		for (Block currentNeighbour : this.getNeighbours(block)){
 			if (currentNeighbour.getType().equals(block.getType())){
				list.put(currentNeighbour.getLocation(), new SCLItem(owner,currentNeighbour,combo));
 				additionalItemsLocked++;
 			}
 		}
 		return additionalItemsLocked;
 	}
 	private Integer addNeighboring (Block block,Player owner) {
 		Integer additionalItemsLocked = 0;
		plugin.out(block.getLocation().toString());
 		for (Block currentNeighbour : this.getNeighbours(block)){
 			if (currentNeighbour.getType().equals(block.getType())){
 				if (list.containsKey(currentNeighbour.getLocation())){
 					plugin.babble("Uhm, this "+currentNeighbour.getType().toString().toLowerCase()+" is already locked.");
 				}
 				else {
					plugin.babble("Locking "+currentNeighbour.getType().toString().toLowerCase()+" at "+currentNeighbour.getLocation().toString());
					list.put(currentNeighbour.getLocation(), new SCLItem(owner,currentNeighbour));
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
 	public void run() { // So saving to the default filename is easily scheduled
 		this.save("Chests.txt");
 	}
 }
