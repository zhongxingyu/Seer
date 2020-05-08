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
 import java.util.ArrayList;
 import java.util.List;
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
 
 	private AntiShare plugin;
 	private CopyOnWriteArrayList<Block> creative_blocks = new CopyOnWriteArrayList<Block>();
 	private CopyOnWriteArrayList<Block> survival_blocks = new CopyOnWriteArrayList<Block>();
 	private CopyOnWriteArrayList<Block> adventure_blocks = new CopyOnWriteArrayList<Block>();
 	private TrackerList tracked_creative;
 	private TrackerList tracked_survival;
 	private TrackerList tracked_adventure;
 	private CopyOnWriteArrayList<ASMaterial> recentlyRemoved = new CopyOnWriteArrayList<ASMaterial>();
 
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
 	 */
 	public void save(){
 		// Load lists
 		List<Block> creative = new ArrayList<Block>();
 		List<Block> survival = new ArrayList<Block>();
 		List<Block> adventure = new ArrayList<Block>();
 		creative.addAll(creative_blocks);
 		survival.addAll(survival_blocks);
 		adventure.addAll(adventure_blocks);
 
 		// Load file
 		File dir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		if(dir.exists()){
 			dir.delete(); // To remove old blocks
 		}
 		dir.mkdirs();
 
 		// Loops and save
 		for(Block block : creative){
 			saveBlock(dir, block, "CREATIVE");
 		}
 		for(Block block : survival){
 			saveBlock(dir, block, "SURVIVAL");
 		}
 		for(Block block : adventure){
 			saveBlock(dir, block, "ADVENTURE");
 		}
 	}
 
 	private static void saveBlock(File dir, Block block, String gamemode){
 		File file = new File(dir, block.getChunk().getX() + "." + block.getChunk().getZ() + "." + block.getWorld().getName() + ".yml");
		if(file.exists()){
			file.delete();
		}
 		EnhancedConfiguration blocks = new EnhancedConfiguration(file, AntiShare.getInstance());
 		blocks.load();
 		blocks.set(block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName(), gamemode);
 		blocks.save();
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
 	}
 
 	/**
 	 * Reloads the manager
 	 */
 	public void reload(){
 		save();
 		creative_blocks.clear();
 		survival_blocks.clear();
 		adventure_blocks.clear();
 		load();
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
