 package com.turt2live.antishare.storage;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.ErrorLog;
 
 /**
  * Block Manager - Handles creative/survival blocks
  * 
  * @author turt2live
  */
 public class BlockManager {
 
 	/**
 	 * AntiShare block - used for simplicity
 	 * 
 	 * @author turt2live
 	 */
 	private class ASBlock {
 		public Location location;
 		public Material expectedType;
 	}
 
 	private AntiShare plugin;
 	private List<Block> creative_blocks = new ArrayList<Block>();
 	private List<Block> survival_blocks = new ArrayList<Block>();
 	private ConcurrentHashMap<Block, ASBlock> expected_creative = new ConcurrentHashMap<Block, ASBlock>();
 	private ConcurrentHashMap<Block, ASBlock> expected_survival = new ConcurrentHashMap<Block, ASBlock>();
 	private TrackerList tracked_creative;
 	private TrackerList tracked_survival;
 
 	/**
 	 * Creates a new block manager, also loads the block lists
 	 */
 	public BlockManager(){
 		this.plugin = AntiShare.getInstance();
 
 		// Load blocks
 		load();
 
 		// Schedule a sanity check
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 			@Override
 			public void run(){
 				// Remove air blocks
 				List<Block> remove = new ArrayList<Block>();
 				for(Block block : creative_blocks){
 					if(block.getType() == Material.AIR){
 						remove.add(block);
 					}
 				}
 				for(Block block : remove){
 					creative_blocks.remove(block);
 					expected_creative.remove(block);
 				}
 				remove = new ArrayList<Block>();
 				for(Block block : survival_blocks){
 					if(block.getType() == Material.AIR){
 						remove.add(block);
 					}
 				}
 				for(Block block : remove){
 					survival_blocks.remove(block);
 					expected_survival.remove(block);
 				}
 
 				// Check lists
 				ConcurrentHashMap<Block, ASBlock> creative = new ConcurrentHashMap<Block, ASBlock>();
 				ConcurrentHashMap<Block, ASBlock> survival = new ConcurrentHashMap<Block, ASBlock>();
 				creative.putAll(expected_creative);
 				survival.putAll(expected_survival);
 				for(ASBlock block : creative.values()){
 					Block atLocation = block.location.getBlock();
 					String location = "(" + block.location.getBlockX() + ", " + block.location.getBlockY() + ", " + block.location.getBlockZ() + ", " + block.location.getWorld().getName() + ")";
 					if(atLocation.getType() != block.expectedType){
 						plugin.getMessenger().log("Creative block at location " + location + " is not " + block.expectedType.name() + " (found " + atLocation.getType().name() + ")", Level.WARNING, LogType.BLOCK);
 						block.expectedType = atLocation.getType();
 					}
 				}
 				for(ASBlock block : survival.values()){
 					Block atLocation = block.location.getBlock();
 					String location = "(" + block.location.getBlockX() + ", " + block.location.getBlockY() + ", " + block.location.getBlockZ() + ", " + block.location.getWorld().getName() + ")";
 					if(atLocation.getType() != block.expectedType){
 						plugin.getMessenger().log("Survival block at location " + location + " is not " + block.expectedType.name() + " (found " + atLocation.getType().name() + ")", Level.WARNING, LogType.BLOCK);
 						block.expectedType = atLocation.getType();
 					}
 				}
 			}
 		}, 0L, (20 * 60 * 10)); // 10 minutes
 	}
 
 	/**
 	 * Saves everything to disk
 	 */
 	public void save(){
 		// Load lists
 		List<Block> creative = new ArrayList<Block>();
 		List<Block> survival = new ArrayList<Block>();
 		creative.addAll(creative_blocks);
 		survival.addAll(survival_blocks);
 
 		// Load file
 		File dir = new File(plugin.getDataFolder(), "data");
 		dir.mkdirs();
 		File file = new File(dir, "blocks.yml");
 		if(file.exists()){
 			file.delete();
 		}
 		EnhancedConfiguration blocks = new EnhancedConfiguration(file, plugin);
 		blocks.load();
 
 		// Loops and save
 		for(Block block : creative){
 			String path = block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName();
 			blocks.set(path, "CREATIVE");
 		}
 		for(Block block : survival){
 			String path = block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName();
 			blocks.set(path, "SURVIVAL");
 		}
 		blocks.save();
 	}
 
 	/**
 	 * Loads from disk
 	 */
 	public void load(){
 		// Setup lists
 		tracked_creative = new TrackerList("config.yml", "block-tracking.tracked-creative-blocks", plugin.getConfig().getString("block-tracking.tracked-creative-blocks").split(","));
 		tracked_survival = new TrackerList("config.yml", "block-tracking.tracked-survival-blocks", plugin.getConfig().getString("block-tracking.tracked-survival-blocks").split(","));
 
 		// Setup cache
 		File dir = new File(plugin.getDataFolder(), "data");
 		dir.mkdirs();
 		EnhancedConfiguration blocks = new EnhancedConfiguration(new File(dir, "blocks.yml"), plugin);
 		blocks.load();
 		for(String key : blocks.getKeys(false)){
 			String[] keyParts = key.split(";");
 			Location location = new Location(Bukkit.getWorld(keyParts[3]), Double.parseDouble(keyParts[0]), Double.parseDouble(keyParts[1]), Double.parseDouble(keyParts[2]));
 			Block block = location.getBlock();
 			if(block == null){
 				location.getChunk().load();
 				block = location.getBlock();
 			}
 			GameMode gm = GameMode.valueOf(blocks.getString(key));
 			addBlock(gm, block);
 		}
 	}
 
 	/**
 	 * Reloads the manager
 	 */
 	public void reload(){
 		save();
 		creative_blocks.clear();
 		survival_blocks.clear();
 		expected_creative.clear();
 		expected_survival.clear();
 		load();
 	}
 
 	/**
 	 * Adds a block to the database
 	 * 
 	 * @param type the block type
 	 * @param block the block
 	 */
 	public void addBlock(GameMode type, Block block){
 		ASBlock asblock = new ASBlock();
 		asblock.location = block.getLocation();
 		asblock.expectedType = block.getType();
 		switch (type){
 		case CREATIVE:
 			if(!tracked_creative.isTracked(block)){
 				break;
 			}
 			creative_blocks.add(block);
 			expected_creative.put(block, asblock);
 			break;
 		case SURVIVAL:
 			if(!tracked_survival.isTracked(block)){
 				break;
 			}
 			survival_blocks.add(block);
 			expected_survival.put(block, asblock);
 			break;
 		}
 	}
 
 	/**
 	 * Removes a block from the database
 	 * 
 	 * @param block the block
 	 */
 	public void removeBlock(Block block){
 		GameMode type = getType(block);
 		if(type != null){
 			switch (type){
 			case CREATIVE:
 				creative_blocks.remove(block);
 				expected_creative.remove(block);
 				break;
 			case SURVIVAL:
 				survival_blocks.remove(block);
 				expected_survival.remove(block);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Moves a block in the system. This auto-detects type
 	 * 
 	 * @param oldLocation the old location
 	 * @param newLocation the new location
 	 */
 	public void moveBlock(Location oldLocation, final Location newLocation){
 		final GameMode type = getType(oldLocation.getBlock());
 		Block oldBlock = oldLocation.getBlock();
 
 		// Remove old block
 		removeBlock(oldBlock);
 
 		// Start a thread to wait until the block changes
 		final Material oldType = oldBlock.getType();
 		new Thread(new Runnable(){
 			@Override
 			public void run(){
 				// Setup vars
 				int runs = 0;
 				int maxRuns = 10;
 				long delay = 100;
 				boolean updated = false;
 
 				// Loop
 				while (runs <= maxRuns && !updated){
 					// Check block
 					Block newBlock = newLocation.getBlock();
 					if(newBlock.getType() == oldType){
 						addBlock(type, newBlock);
 						updated = true;
 					}
 
 					// Count and wait
 					runs++;
 					try{
 						Thread.sleep(delay);
 					}catch(InterruptedException e){
 						AntiShare.getInstance().getMessenger().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE, LogType.ERROR);
 						AntiShare.getInstance().getMessenger().log("Please see " + ErrorLog.print(e) + " for the full error.", Level.SEVERE, LogType.ERROR);
 					}
 				}
 
 				// Warn if not updated
 				if(!updated){
 					plugin.getMessenger().log("Move block took longer than " + (delay * maxRuns) + " milliseconds.", Level.SEVERE, LogType.BYPASS);
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Gets the gamemode associated with a block
 	 * 
 	 * @param block the block
 	 * @return the gamemode, or null if no assignment
 	 */
 	public GameMode getType(Block block){
 		if(creative_blocks.contains(block)){
 			return GameMode.CREATIVE;
 		}else if(survival_blocks.contains(block)){
 			return GameMode.SURVIVAL;
 		}
 		return null;
 	}
 }
