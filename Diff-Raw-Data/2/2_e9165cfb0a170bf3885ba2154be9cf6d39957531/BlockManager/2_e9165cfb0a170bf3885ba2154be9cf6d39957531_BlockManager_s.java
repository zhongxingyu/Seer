 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.storage;
 
 import java.io.File;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.tekkitcompat.ScheduleLayer;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.util.events.TrackerList;
 
 /**
  * Block Manager - Handles creative/survival blocks
  * 
  * @author turt2live
  */
 public class BlockManager {
 
 	/**
 	 * AntiShare material - used for simplicity
 	 * 
 	 * @author turt2live
 	 */
 	private class ASMaterial {
 		public Location location;
 		public GameMode gamemode;
 	}
 
 	enum ListComplete{
 		CREATIVE(0), ADVENTURE(1), SURVIVAL(2);
 
 		public final int arrayIndex;
 
 		private ListComplete(int arrayIndex){
 			this.arrayIndex = arrayIndex;
 		}
 	}
 
 	private AntiShare plugin;
 	private CopyOnWriteArrayList<Block> creative_blocks = new CopyOnWriteArrayList<Block>();
 	private CopyOnWriteArrayList<Block> survival_blocks = new CopyOnWriteArrayList<Block>();
 	private CopyOnWriteArrayList<Block> adventure_blocks = new CopyOnWriteArrayList<Block>();
 	private TrackerList tracked_creative;
 	private TrackerList tracked_survival;
 	private TrackerList tracked_adventure;
 	private CopyOnWriteArrayList<ASMaterial> recentlyRemoved = new CopyOnWriteArrayList<ASMaterial>();
 	private ConcurrentMap<String, EnhancedConfiguration> saveFiles = new ConcurrentHashMap<String, EnhancedConfiguration>();
 	private boolean[] completedSaves;
 	private final int maxLists = ListComplete.values().length;
 	private boolean doneLastSave = false;
 	private BlockSaver saveCreative, saveSurvival, saveAdventure;
 
 	/**
 	 * Creates a new block manager, also loads the block lists
 	 */
 	public BlockManager(){
 		this.plugin = AntiShare.getInstance();
 
 		// Load blocks
 		load();
 	}
 
 	/**
 	 * Saves everything to disk
 	 * 
 	 * @param clear set to true to prepare for a reload
 	 * @param load set to true to load everything after saving (reload)
 	 */
 	public void save(boolean clear, boolean load){
 		// Load file
 		File dir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		if(dir.exists()){
 			dir.delete(); // To remove old blocks
 		}
 		dir.mkdirs();
 		completedSaves = new boolean[maxLists];
 		for(int i = 0; i < maxLists; i++){
 			completedSaves[i] = false;
 		}
 		doneLastSave = false;
 
 		// Create savers
 		saveCreative = new BlockSaver(creative_blocks, GameMode.CREATIVE, dir, ListComplete.CREATIVE);
 		saveSurvival = new BlockSaver(survival_blocks, GameMode.SURVIVAL, dir, ListComplete.SURVIVAL);
 
 		saveCreative.setClear(clear);
 		saveSurvival.setClear(clear);
 		saveCreative.setLoad(load);
 		saveSurvival.setLoad(load);
 
 		// Treat adventure on it's own
 		if(ServerHas.adventureMode()){
 			saveAdventure = new BlockSaver(adventure_blocks, GameMode.ADVENTURE, dir, ListComplete.ADVENTURE);
 			saveAdventure.setClear(clear);
 			saveAdventure.setLoad(load);
 		}else{
 			saveAdventure = null;
 		}
 
 		// Schedule saves
 
 		/*
 		 * Because of how the scheduler works, we have to use the java Thread class.
 		 */
 
 		Thread creative = new Thread(saveCreative);
 		Thread survival = new Thread(saveSurvival);
 
 		// Set names, in case there is a bug
 		creative.setName("ANTISHARE-Save Creative");
 		survival.setName("ANTISHARE-Save Survival");
 
 		// Run
 		creative.start();
 		survival.start();
 
 		// Treat adventure on it's own
 		if(saveAdventure != null){
 			Thread adventure = new Thread(saveAdventure);
 			adventure.setName("ANTISHARE-Save Adventure");
 			adventure.start();
 		}
 
 		// BlockSaver handles telling BlockManager that it is done
 	}
 
 	EnhancedConfiguration getFile(File dir, String fname){
 		EnhancedConfiguration blocks = null;
 		if(!saveFiles.containsKey(fname)){
 			File file = new File(dir, fname);
 			blocks = new EnhancedConfiguration(file, AntiShare.getInstance());
 			saveFiles.put(fname, blocks);
 		}else{
 			blocks = saveFiles.get(fname);
 		}
 		return blocks;
 	}
 
 	void markSaveAsDone(ListComplete list, BlockSaver save){
 		completedSaves[list.arrayIndex] = true;
 		for(int i = 0; i < maxLists; i++){
 			if(!completedSaves[i]){
 				return;
 			}
 		}
 		if(!plugin.getConfig().getBoolean("other.more-quiet-shutdown")){
 			plugin.getLogger().info("[Block Manager] Saving files...");
 		}
 		for(String key : saveFiles.keySet()){
 			saveFiles.get(key).save();
 		}
 		saveFiles.clear();
 		if(save.getClear()){
 			creative_blocks.clear();
 			survival_blocks.clear();
 			adventure_blocks.clear();
 		}
 		if(save.getLoad()){
 			load();
 		}
 		doneLastSave = true;
 	}
 
 	/**
 	 * Determines if the last save is done
 	 * 
 	 * @return true if done
 	 */
 	public boolean isSaveDone(){
 		return doneLastSave;
 	}
 
 	/**
 	 * Gets the percentage of the save completed
 	 * 
 	 * @return the percent of the save completed (as a whole number, eg: 10)
 	 */
 	public int percentSaveDone(){
 		double percentCreative = saveCreative.getPercent();
 		double percentAdventure = saveAdventure != null ? saveAdventure.getPercent() : 0;
 		double percentSurvival = saveSurvival.getPercent();
 		Double avg = (percentCreative + percentAdventure + percentSurvival) / (saveAdventure != null ? 3 : 2);
 		return avg.intValue();
 	}
 
 	/**
 	 * Loads from disk
 	 */
 	public void load(){
 		// Setup lists
 		tracked_creative = new TrackerList("config.yml", "block-tracking.tracked-creative-blocks", plugin.getConfig().getString("block-tracking.tracked-creative-blocks").split(","));
 		tracked_survival = new TrackerList("config.yml", "block-tracking.tracked-survival-blocks", plugin.getConfig().getString("block-tracking.tracked-survival-blocks").split(","));
 		tracked_adventure = new TrackerList("config.yml", "block-tracking.tracked-adventure-blocks", plugin.getConfig().getString("block-tracking.tracked-adventure-blocks").split(","));
 
 		// Setup cache
 		File dir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		dir.mkdirs();
 		if(dir.listFiles() != null){
 			for(File file : dir.listFiles()){
 				if(!file.getName().endsWith(".yml")){
 					continue;
 				}
 				EnhancedConfiguration blocks = new EnhancedConfiguration(file, plugin);
 				blocks.load();
 				for(String key : blocks.getKeys(false)){
 					String[] keyParts = key.split(";");
 					Location location = new Location(Bukkit.getWorld(keyParts[3]), Double.parseDouble(keyParts[0]), Double.parseDouble(keyParts[1]), Double.parseDouble(keyParts[2]));
 					if(Bukkit.getWorld(keyParts[3]) == null || location == null || location.getWorld() == null){
 						continue;
 					}
 					Block block = location.getBlock();
 					if(block == null){
 						location.getChunk().load();
 						block = location.getBlock();
 					}
 					GameMode gm = GameMode.valueOf(blocks.getString(key));
 					addBlock(gm, block);
 				}
 			}
 		}
 
 		// Tell console what we loaded
 		if(creative_blocks.size() > 0){
 			plugin.getLogger().info("Creative Blocks Loaded: " + creative_blocks.size());
 		}
 		if(survival_blocks.size() > 0){
			plugin.getLogger().info("Survival Blocks Loaded: " + creative_blocks.size());
 		}
 		if(adventure_blocks.size() > 0){
 			plugin.getLogger().info("Adventure Blocks Loaded: " + adventure_blocks.size());
 		}
 	}
 
 	/**
 	 * Reloads the manager
 	 */
 	public void reload(){
 		save(true, true);
 	}
 
 	/**
 	 * Adds a block to the database
 	 * 
 	 * @param type the block type
 	 * @param block the block
 	 */
 	public void addBlock(GameMode type, Block block){
 		switch (type){
 		case CREATIVE:
 			if(!tracked_creative.isTracked(block)){
 				break;
 			}
 			creative_blocks.add(block);
 			break;
 		case SURVIVAL:
 			if(!tracked_survival.isTracked(block)){
 				break;
 			}
 			survival_blocks.add(block);
 			break;
 		default:
 			if(ServerHas.adventureMode()){
 				if(!tracked_adventure.isTracked(block)){
 					break;
 				}
 				adventure_blocks.add(block);
 			}
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
 			ASMaterial material = new ASMaterial();
 			material.gamemode = type;
 			material.location = block.getLocation();
 			recentlyRemoved.add(material);
 			switch (type){
 			case CREATIVE:
 				creative_blocks.remove(block);
 				break;
 			case SURVIVAL:
 				survival_blocks.remove(block);
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					adventure_blocks.remove(block);
 				}
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
 
 		if(type == null){
 			return;
 		}
 
 		// Remove old block
 		removeBlock(oldBlock);
 
 		// Start a thread to wait until the block changes
 		final Material oldType = oldBlock.getType();
 		ScheduleLayer.runTaskAsynchronously(plugin, new Runnable(){
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
 						AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
 						e.printStackTrace();
 					}
 				}
 
 				// Warn if not updated
 				if(!updated){
 					plugin.log("Move block took longer than " + (delay * maxRuns) + " milliseconds.", Level.SEVERE);
 				}
 			}
 		});
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
 		}else if(adventure_blocks.contains(block)){
 			if(ServerHas.adventureMode()){
 				return GameMode.ADVENTURE;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the Game Mode of a recently broken block at a location
 	 * 
 	 * @param location the location
 	 * @return the Game Mode (or null if not applicable)
 	 */
 	public GameMode getRecentBreak(Location location){
 		for(ASMaterial material : recentlyRemoved){
 			Location l = material.location;
 			if(Math.floor(l.getX()) == Math.floor(location.getX())
 					&& Math.floor(l.getY()) == Math.floor(location.getY())
 					&& Math.floor(l.getZ()) == Math.floor(location.getZ())
 					&& l.getWorld().getName().equalsIgnoreCase(location.getWorld().getName())){
 				return material.gamemode;
 			}
 		}
 		return null;
 	}
 
 }
