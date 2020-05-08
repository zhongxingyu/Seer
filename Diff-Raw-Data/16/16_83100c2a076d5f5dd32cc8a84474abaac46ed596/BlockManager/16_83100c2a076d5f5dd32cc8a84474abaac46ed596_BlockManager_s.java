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
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 
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
 		CREATIVE_BLOCKS(0), ADVENTURE_BLOCKS(1), SURVIVAL_BLOCKS(2),
 		CREATIVE_ENTITIES(3), SURVIVAL_ENTITIES(4), ADVENTURE_ENTITIES(5);
 
 		public final int arrayIndex;
 
 		private ListComplete(int arrayIndex){
 			this.arrayIndex = arrayIndex;
 		}
 	}
 
 	private AntiShare plugin;
 	private CopyOnWriteArrayList<String> creative_blocks = new CopyOnWriteArrayList<String>();
 	private CopyOnWriteArrayList<String> survival_blocks = new CopyOnWriteArrayList<String>();
 	private CopyOnWriteArrayList<String> adventure_blocks = new CopyOnWriteArrayList<String>();
 	private CopyOnWriteArrayList<String> creative_entities = new CopyOnWriteArrayList<String>();
 	private CopyOnWriteArrayList<String> survival_entities = new CopyOnWriteArrayList<String>();
 	private CopyOnWriteArrayList<String> adventure_entities = new CopyOnWriteArrayList<String>();
 	private TrackerList tracked_creative;
 	private TrackerList tracked_survival;
 	private TrackerList tracked_adventure;
 	private CopyOnWriteArrayList<ASMaterial> recentlyRemoved = new CopyOnWriteArrayList<ASMaterial>();
 	private ConcurrentMap<String, EnhancedConfiguration> saveFiles = new ConcurrentHashMap<String, EnhancedConfiguration>();
 	private boolean[] completedSaves;
 	private final int maxLists = ListComplete.values().length;
 	private boolean doneLastSave = false;
 	private ObjectSaver saveCreativeBlocks, saveSurvivalBlocks, saveAdventureBlocks, saveCreativeEntities, saveSurvivalEntities, saveAdventureEntities;
 	private final File entitiesDir;
 	private final File blocksDir;
 
 	/**
 	 * Creates a new block manager, also loads the block lists
 	 */
 	public BlockManager(){
 		this.plugin = AntiShare.getInstance();
 
 		// Setup files
 		entitiesDir = new File(plugin.getDataFolder(), "data" + File.separator + "entities");
 		blocksDir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		blocksDir.mkdirs();
 		entitiesDir.mkdirs();
 
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
 		// Load files
 		if(blocksDir.exists()){
 			blocksDir.delete(); // To remove old blocks
 		}
 		blocksDir.mkdirs();
 		if(entitiesDir.exists()){
 			entitiesDir.delete(); // To remove old entities
 		}
 		entitiesDir.mkdirs();
 		completedSaves = new boolean[maxLists];
 		for(int i = 0; i < maxLists; i++){
 			completedSaves[i] = false;
 		}
 		doneLastSave = false;
 
 		// Create savers
 		saveCreativeBlocks = new ObjectSaver(creative_blocks, GameMode.CREATIVE, blocksDir, ListComplete.CREATIVE_BLOCKS, true);
 		saveSurvivalBlocks = new ObjectSaver(survival_blocks, GameMode.SURVIVAL, blocksDir, ListComplete.SURVIVAL_BLOCKS, true);
		saveCreativeEntities = new ObjectSaver(creative_entities, GameMode.CREATIVE, blocksDir, ListComplete.CREATIVE_ENTITIES, false);
		saveSurvivalEntities = new ObjectSaver(survival_entities, GameMode.SURVIVAL, blocksDir, ListComplete.SURVIVAL_ENTITIES, false);
 
 		saveCreativeBlocks.setClear(clear);
 		saveSurvivalBlocks.setClear(clear);
 		saveCreativeBlocks.setLoad(load);
 		saveSurvivalBlocks.setLoad(load);
 		saveCreativeEntities.setClear(clear);
 		saveSurvivalEntities.setClear(clear);
 		saveCreativeEntities.setLoad(load);
 		saveSurvivalEntities.setLoad(load);
 
 		// Treat adventure on it's own
 		if(ServerHas.adventureMode()){
 			saveAdventureBlocks = new ObjectSaver(adventure_blocks, GameMode.ADVENTURE, blocksDir, ListComplete.ADVENTURE_BLOCKS, true);
 			saveAdventureBlocks.setClear(clear);
 			saveAdventureBlocks.setLoad(load);
 			saveAdventureEntities = new ObjectSaver(adventure_entities, GameMode.ADVENTURE, entitiesDir, ListComplete.ADVENTURE_ENTITIES, false);
 			saveAdventureEntities.setClear(clear);
 			saveAdventureEntities.setLoad(load);
 		}else{
 			saveAdventureBlocks = null;
 			saveAdventureEntities = null;
 		}
 
 		// Schedule saves
 
 		/*
 		 * Because of how the scheduler works, we have to use the java Thread class.
 		 */
 
 		Thread creativeBlocksThread = new Thread(saveCreativeBlocks);
 		Thread survivalBlocksThread = new Thread(saveSurvivalBlocks);
 		Thread creativeEntitiesThread = new Thread(saveCreativeEntities);
 		Thread survivalEntitiesThread = new Thread(saveSurvivalEntities);
 
 		// Set names, in case there is a bug
 		creativeBlocksThread.setName("ANTISHARE-Save Creative Blocks");
 		survivalBlocksThread.setName("ANTISHARE-Save Survival Blocks");
 		creativeEntitiesThread.setName("ANTISHARE-Save Creative Entities");
 		survivalEntitiesThread.setName("ANTISHARE-Save Survival Entities");
 
 		// Run
 		creativeBlocksThread.start();
 		survivalBlocksThread.start();
 		creativeEntitiesThread.start();
 		survivalEntitiesThread.start();
 
 		// Treat adventure on it's own
 		ObjectSaver nullSaver = new NullObjectSaver();
 		nullSaver.setClear(clear);
 		nullSaver.setLoad(load);
 		if(saveAdventureBlocks != null){
 			Thread adventureBlocksThread = new Thread(saveAdventureBlocks);
 			adventureBlocksThread.setName("ANTISHARE-Save Adventure Blocks");
 			adventureBlocksThread.start();
 		}else{
 			markSaveAsDone(ListComplete.ADVENTURE_BLOCKS, nullSaver);
 		}
 		if(saveAdventureEntities != null){
 			Thread adventureEntitiesThread = new Thread(saveAdventureEntities);
 			adventureEntitiesThread.setName("ANTISHARE-Save Adventure Entities");
 			adventureEntitiesThread.start();
 		}else{
 			markSaveAsDone(ListComplete.ADVENTURE_ENTITIES, nullSaver);
 		}
 
 		// BlockSaver handles telling BlockManager that it is done
 	}
 
 	EnhancedConfiguration getFile(File dir, String fname){
 		EnhancedConfiguration ymlFile = null;
 		if(!saveFiles.containsKey(fname)){
 			File file = new File(dir, fname);
 			ymlFile = new EnhancedConfiguration(file, AntiShare.getInstance());
 			saveFiles.put(fname, ymlFile);
 		}else{
 			ymlFile = saveFiles.get(fname);
 		}
 		return ymlFile;
 	}
 
 	void markSaveAsDone(ListComplete list, ObjectSaver save){
 		completedSaves[list.arrayIndex] = true;
 		for(int i = 0; i < maxLists; i++){
 			if(!completedSaves[i]){
 				return;
 			}
 		}
 		if(doneLastSave == true){
 			return;
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
 			creative_entities.clear();
 			survival_entities.clear();
 			adventure_entities.clear();
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
 		return doneLastSave || (
 				creative_blocks.size() <= 0 &&
 						survival_blocks.size() <= 0 &&
 						adventure_blocks.size() <= 0 &&
 						creative_entities.size() <= 0 &&
 						survival_entities.size() <= 0 &&
 				adventure_entities.size() <= 0);
 	}
 
 	/**
 	 * Gets the percentage of the save completed
 	 * 
 	 * @return the percent of the save completed (as a whole number, eg: 10)
 	 */
 	public int percentSaveDone(){
 		if(isSaveDone()){
 			return 100;
 		}
 		double percentCreative = saveCreativeBlocks.getPercent() + saveCreativeEntities.getPercent();
 		double percentSurvival = saveSurvivalBlocks.getPercent() + saveSurvivalEntities.getPercent();
 		double percentAdventure = (saveAdventureBlocks != null ? saveAdventureBlocks.getPercent() : 0)
 				+ (saveAdventureEntities != null ? saveAdventureEntities.getPercent() : 0);
 		double divisible = 6 - (saveAdventureBlocks == null ? 1 : 0) - (saveAdventureEntities == null ? 1 : 0);
 		Double avg = (percentCreative + percentAdventure + percentSurvival) / divisible;
 		return avg.intValue();
 	}
 
 	/**
 	 * Loads a directory
 	 * 
 	 * @param directory the directory
 	 * @param worldname the world name (or null to not load a specific world)
 	 * @param isBlock true for a block file
 	 */
 	private void load(File directory, String worldname, boolean isBlock){
 		if(directory.listFiles() != null){
 			for(File file : directory.listFiles()){
 				if(!file.getName().endsWith(".yml")){
 					continue;
 				}
 				String[] fparts = file.getName().split("\\.");
 				if(fparts.length < 3){
 					plugin.getLogger().warning("INVALID " + (isBlock ? "BLOCK" : "ENTITY") + " FILE: " + file.getName());
 					continue;
 				}
 				String w = fparts[2];
 				if(Bukkit.getWorld(w) == null){
 					continue;
 				}
 				if(worldname != null && !w.equalsIgnoreCase(worldname)){
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
 					EntityType etype = null;
 					if(keyParts.length > 4){
 						try{
 							etype = EntityType.fromName(keyParts[4]);
 						}catch(Exception e){ // Prevents messy consoles
 							etype = null;
 						}
 					}
 					GameMode gm = GameMode.valueOf(blocks.getString(key));
 					if(isBlock){
 						Block block = location.getBlock();
 						if(block == null){
 							location.getChunk().load();
 							block = location.getBlock();
 						}
 						addBlock(gm, block);
 					}else{
 						if(etype == null){
 							plugin.getLogger().warning("INVALID " + (isBlock ? "BLOCK" : "ENTITY") + " KEY IN FILE ('" + file.getName() + "'): " + key);
 							continue;
 						}
 						addEntity(gm, location, etype);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Loads blocks for a specific world
 	 * 
 	 * @param world the world to load
 	 */
 	public void loadWorld(String world){
 		int pc = creative_blocks.size(), ps = survival_blocks.size(), pa = adventure_blocks.size(), pce = creative_entities.size(), pse = survival_entities.size(), pae = adventure_entities.size();
 
 		// Load
 		load(blocksDir, world, true);
 		load(entitiesDir, world, false);
 
 		// Tell console what we loaded
 		if(creative_blocks.size() - pc > 0){
 			plugin.getLogger().info("Creative Blocks Loaded (for world '" + world + "'): " + (creative_blocks.size() - pc));
 		}
 		if(survival_blocks.size() - ps > 0){
 			plugin.getLogger().info("Survival Blocks Loaded (for world '" + world + "'): " + (survival_blocks.size() - ps));
 		}
 		if(adventure_blocks.size() - pa > 0){
 			plugin.getLogger().info("Adventure Blocks Loaded (for world '" + world + "'): " + (adventure_blocks.size() - pa));
 		}
 		if(creative_entities.size() - pce > 0){
 			plugin.getLogger().info("Creative Entities Loaded (for world '" + world + "'): " + (creative_entities.size() - pce));
 		}
 		if(survival_entities.size() - pse > 0){
 			plugin.getLogger().info("Survival Entities Loaded (for world '" + world + "'): " + (survival_entities.size() - pse));
 		}
 		if(adventure_entities.size() - pae > 0){
 			plugin.getLogger().info("Adventure Entities Loaded (for world '" + world + "'): " + (adventure_entities.size() - pae));
 		}
 	}
 
 	/**
 	 * Loads from disk
 	 */
 	public void load(){
 		// Setup lists
 		tracked_creative = new TrackerList("config.yml", "block-tracking.tracked-creative-blocks", plugin.getConfig().getString("block-tracking.tracked-creative-blocks").split(","));
 		tracked_survival = new TrackerList("config.yml", "block-tracking.tracked-survival-blocks", plugin.getConfig().getString("block-tracking.tracked-survival-blocks").split(","));
 		tracked_adventure = new TrackerList("config.yml", "block-tracking.tracked-adventure-blocks", plugin.getConfig().getString("block-tracking.tracked-adventure-blocks").split(","));
 
 		// Load
 		load(blocksDir, null, true);
 		load(entitiesDir, null, false);
 
 		// Tell console what we loaded
 		if(creative_blocks.size() > 0){
 			plugin.getLogger().info("Creative Blocks Loaded: " + creative_blocks.size());
 		}
 		if(survival_blocks.size() > 0){
 			plugin.getLogger().info("Survival Blocks Loaded: " + survival_blocks.size());
 		}
 		if(adventure_blocks.size() > 0){
 			plugin.getLogger().info("Adventure Blocks Loaded: " + adventure_blocks.size());
 		}
 		if(creative_entities.size() > 0){
 			plugin.getLogger().info("Creative Entities Loaded: " + creative_entities.size());
 		}
 		if(survival_entities.size() > 0){
 			plugin.getLogger().info("Survival Entities Loaded: " + survival_entities.size());
 		}
 		if(adventure_entities.size() > 0){
 			plugin.getLogger().info("Adventure Entities Loaded: " + adventure_entities.size());
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
 			creative_blocks.add(blockToString(block));
 			break;
 		case SURVIVAL:
 			if(!tracked_survival.isTracked(block)){
 				break;
 			}
 			survival_blocks.add(blockToString(block));
 			break;
 		default:
 			if(ServerHas.adventureMode()){
 				if(!tracked_adventure.isTracked(block)){
 					break;
 				}
 				adventure_blocks.add(blockToString(block));
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Adds an entity to the database
 	 * 
 	 * @param type the entity type
 	 * @param entity the entity
 	 */
 	public void addEntity(GameMode type, Entity entity){
 		switch (type){
 		case CREATIVE:
 			if(!tracked_creative.isTracked(entity)){
 				break;
 			}
 			creative_entities.add(entityToString(entity));
 			break;
 		case SURVIVAL:
 			if(!tracked_survival.isTracked(entity)){
 				break;
 			}
 			survival_entities.add(entityToString(entity));
 			break;
 		default:
 			if(ServerHas.adventureMode()){
 				if(!tracked_adventure.isTracked(entity)){
 					break;
 				}
 				adventure_entities.add(entityToString(entity));
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Adds an entity to the database
 	 * 
 	 * @param type the entity gamemode
 	 * @param entityType the entity type
 	 * @param entity the entity
 	 */
 	public void addEntity(GameMode type, Location entity, EntityType entityType){
 		switch (type){
 		case CREATIVE:
 			if(!tracked_creative.isTracked(entityType)){
 				break;
 			}
 			creative_entities.add(entityToString(entity, entityType));
 			break;
 		case SURVIVAL:
 			if(!tracked_survival.isTracked(entityType)){
 				break;
 			}
 			survival_entities.add(entityToString(entity, entityType));
 			break;
 		default:
 			if(ServerHas.adventureMode()){
 				if(!tracked_adventure.isTracked(entityType)){
 					break;
 				}
 				adventure_entities.add(entityToString(entity, entityType));
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Removes an entity from the database
 	 * 
 	 * @param entity the entity
 	 */
 	public void removeEntity(Entity entity){
 		GameMode type = getType(entity);
 		if(type != null){
 			ASMaterial material = new ASMaterial();
 			material.gamemode = type;
 			material.location = entity.getLocation();
 			recentlyRemoved.add(material);
 			switch (type){
 			case CREATIVE:
 				creative_entities.remove(entityToString(entity));
 				break;
 			case SURVIVAL:
 				survival_entities.remove(entityToString(entity));
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					adventure_entities.remove(entityToString(entity));
 				}
 				break;
 			}
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
 				creative_blocks.remove(blockToString(block));
 				break;
 			case SURVIVAL:
 				survival_blocks.remove(blockToString(block));
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					adventure_blocks.remove(blockToString(block));
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
 		if(creative_blocks.contains(blockToString(block))){
 			return GameMode.CREATIVE;
 		}else if(survival_blocks.contains(blockToString(block))){
 			return GameMode.SURVIVAL;
 		}else if(adventure_blocks.contains(blockToString(block))){
 			if(ServerHas.adventureMode()){
 				return GameMode.ADVENTURE;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the gamemode associated with a entity
 	 * 
 	 * @param entity the entity
 	 * @return the gamemode, or null if no assignment
 	 */
 	public GameMode getType(Entity entity){
 		if(creative_entities.contains(entityToString(entity))){
 			return GameMode.CREATIVE;
 		}else if(survival_entities.contains(entityToString(entity))){
 			return GameMode.SURVIVAL;
 		}else if(adventure_entities.contains(entityToString(entity))){
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
 
 	String blockToString(Block block){
 		return block.getChunk().getX() + ";" + block.getChunk().getZ() + ";" + block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
 	}
 
 	String entityToString(Entity entity){
 		Location location = entity.getLocation();
 		return location.getChunk().getX() + ";" + location.getChunk().getZ() + ";" + location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + entity.getType().name();
 	}
 
 	String entityToString(Location entity, EntityType type){
 		return entity.getChunk().getX() + ";" + entity.getChunk().getZ() + ";" + entity.getWorld().getName() + ";" + entity.getX() + ";" + entity.getY() + ";" + entity.getZ() + ";" + type.name();
 	}
 
 }
