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
 package com.turt2live.antishare.manager;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.inventory.LinkedInventory;
 import com.turt2live.antishare.inventory.TemporaryASInventory;
import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.PermissionNodes;
 
 /**
  * Manages inventories within AntiShare
  * 
  * @author turt2live
  */
 public class InventoryManager {
 
 	private final ConcurrentHashMap<String, ASInventory> creative = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> survival = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> adventure = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderCreative = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderSurvival = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderAdventure = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, TemporaryASInventory> temporary = new ConcurrentHashMap<String, TemporaryASInventory>();
 	private final List<LinkedInventory> linkedInventories = new ArrayList<LinkedInventory>();
 	private AntiShare plugin = AntiShare.p;
 
 	/**
 	 * Creates a new Inventory Manager
 	 */
 	public InventoryManager(){
 		// Prepare linked inventories
 		EnhancedConfiguration linksYaml = new EnhancedConfiguration(new File(plugin.getDataFolder(), "linked-inventories.yml"), plugin);
 		linksYaml.loadDefaults(plugin.getResource("linked-inventories.yml"));
 		if(!linksYaml.fileExists() || !linksYaml.checkDefaults()){
 			linksYaml.saveDefaults();
 		}
 		linksYaml.load();
 	}
 
 	/**
 	 * Loads a specific player from file
 	 * 
 	 * @param player the player
 	 */
 	public void loadPlayer(Player player){
 		// Check archive first
 		File expected = new File(plugin.getDataFolder(), "data" + File.separator + "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player.getName() + ".yml");
 		File archive = new File(plugin.getDataFolder(), "data" + File.separator + "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player.getName() + ".yml");
 		if(!expected.exists()){
 			// Check archive
 			if(archive.exists()){
 				// Move file
 				archive.renameTo(expected);
 			}
 		}
 
 		// Standard inventories
 		List<ASInventory> list = ASInventory.generateInventory(player.getName(), InventoryType.PLAYER);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				World world = inventory.getWorld();
 				GameMode gamemode = inventory.getGameMode();
 				switch (gamemode){
 				case CREATIVE:
 					creative.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case SURVIVAL:
 					survival.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case ADVENTURE:
 					adventure.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 
 		// Ender inventories
 		list = ASInventory.generateInventory(player.getName(), InventoryType.ENDER);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				World world = inventory.getWorld();
 				GameMode gamemode = inventory.getGameMode();
 				switch (gamemode){
 				case CREATIVE:
 					enderCreative.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case SURVIVAL:
 					enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case ADVENTURE:
 					enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 
 		// Temporary inventories
 		list = ASInventory.generateInventory(player.getName(), InventoryType.TEMPORARY);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				TemporaryASInventory spec = new TemporaryASInventory(ASInventory.generate(player, InventoryType.PLAYER), inventory);
 				temporary.put(player.getName(), spec);
 			}
 		}
 	}
 
 	/**
 	 * Cleans up player data
 	 * 
 	 * @param player the player
 	 */
 	public void releasePlayer(final Player player){
 		if(player == null){
 			return;
 		}
 		if(isInTemporary(player)){
 			removeFromTemporary(player);
 		}
 		savePlayer(player);
 		String name = player.getName(), world = player.getWorld().getName();
 		creative.remove(name + "." + world);
 		survival.remove(name + "." + world);
 		adventure.remove(name + "." + world);
 		enderCreative.remove(name + "." + world);
 		enderSurvival.remove(name + "." + world);
 		enderAdventure.remove(name + "." + world);
 	}
 
 	public void inject(ASInventory inventory){
 		// player.world = key
 		String key = inventory.getName() + "." + inventory.getWorld().getName();
 		switch (inventory.getType()){
 		case PLAYER:
 			switch (inventory.getGameMode()){
 			case CREATIVE:
 				creative.put(key, inventory);
 				break;
 			case SURVIVAL:
 				survival.put(key, inventory);
 				break;
 			case ADVENTURE:
 				adventure.put(key, inventory);
 				break;
 			default:
 				break;
 			}
 			break;
 		case ENDER:
 			switch (inventory.getGameMode()){
 			case CREATIVE:
 				enderCreative.put(key, inventory);
 				break;
 			case SURVIVAL:
 				enderSurvival.put(key, inventory);
 				break;
 			case ADVENTURE:
 				enderAdventure.put(key, inventory);
 				break;
 			default:
 				break;
 			}
 			break;
 		default:
 			return;
 		}
 	}
 
 	/**
 	 * Refreshes inventories of a player
 	 * 
 	 * @param player the player
 	 * @param alreadySaved set to true to bypass saving
 	 */
 	public void refreshInventories(Player player, boolean alreadySaved){
 		if(!player.hasPermission(PermissionNodes.NO_SWAP)){
 			return;
 		}
 		// Save
 		ASInventory premerge = ASInventory.generate(player, InventoryType.PLAYER);
 		ASInventory enderPremerge = ASInventory.generate(player, InventoryType.ENDER);
 		switch (player.getGameMode()){
 		case CREATIVE:
 			if(!alreadySaved){
 				saveCreativeInventory(player, player.getWorld());
 			}
 			premerge = getCreativeInventory(player, player.getWorld());
 			if(!alreadySaved){
 				saveEnderCreativeInventory(player, player.getWorld());
 			}
 			enderPremerge = getEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			if(!alreadySaved){
 				saveSurvivalInventory(player, player.getWorld());
 			}
 			premerge = getSurvivalInventory(player, player.getWorld());
 			if(!alreadySaved){
 				saveEnderSurvivalInventory(player, player.getWorld());
 			}
 			enderPremerge = getEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			if(!alreadySaved){
 				saveAdventureInventory(player, player.getWorld());
 			}
 			premerge = getAdventureInventory(player, player.getWorld());
 			if(!alreadySaved){
 				saveEnderAdventureInventory(player, player.getWorld());
 			}
 			enderPremerge = getEnderAdventureInventory(player, player.getWorld());
 			break;
 		default:
 			break;
 		}
 
 		// Merge all inventories if needed
 		ASInventory merge = premerge.clone();
 		ASInventory enderMerge = enderPremerge.clone();
 		for(World world : Bukkit.getWorlds()){
 			merge.setGamemode(GameMode.CREATIVE);
 			enderMerge.setGamemode(GameMode.CREATIVE);
 			creative.put(player.getName() + "." + world.getName(), merge);
 			enderCreative.put(player.getName() + "." + world.getName(), enderMerge);
 			merge.setGamemode(GameMode.SURVIVAL);
 			enderMerge.setGamemode(GameMode.SURVIVAL);
 			survival.put(player.getName() + "." + world.getName(), merge);
 			enderSurvival.put(player.getName() + "." + world.getName(), enderMerge);
 			merge.setGamemode(GameMode.ADVENTURE);
 			enderMerge.setGamemode(GameMode.ADVENTURE);
 			adventure.put(player.getName() + "." + world.getName(), merge);
 			enderAdventure.put(player.getName() + "." + world.getName(), enderMerge);
 		}
 	}
 
 	/**
 	 * Sets a player to a temporary inventory, such as a region inventory
 	 * 
 	 * @param player the player
 	 * @param inventory the temporary inventory
 	 */
 	@SuppressWarnings ("deprecation")
 	public void setToTemporary(Player player, ASInventory inventory){
 		// Save current inventory
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld());
 			saveEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld());
 			saveEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, player.getWorld());
 			saveEnderAdventureInventory(player, player.getWorld());
 			break;
 		default:
 			break;
 		}
 
 		// Set to temp
 		TemporaryASInventory spec = new TemporaryASInventory(ASInventory.generate(player, InventoryType.PLAYER), inventory);
 		temporary.put(player.getName(), spec);
 		if(inventory == null){
 			player.getInventory().clear();
 			player.updateInventory();
 		}else{
 			inventory.setTo(player);
 		}
 	}
 
 	/**
 	 * Removes the player from their temporary inventory, discarding it
 	 * 
 	 * @param player the player
 	 */
 	public void removeFromTemporary(Player player){
 		TemporaryASInventory inventory = temporary.get(player.getName());
 		if(inventory != null){
 			inventory.getLastInventory().setTo(player);
 			temporary.remove(player.getName());
 		}
 	}
 
 	/**
 	 * Determines if a player is in a temporary inventory
 	 * 
 	 * @param player the player
 	 * @return true if they are in temporary
 	 */
 	public boolean isInTemporary(Player player){
 		return temporary.containsKey(player.getName());
 	}
 
 	/**
 	 * Saves a player's creative inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveCreativeInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.CREATIVE);
 		creative.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's creative ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveEnderCreativeInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.CREATIVE);
 		enderCreative.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's survival inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveSurvivalInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.SURVIVAL);
 		survival.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's survival ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveEnderSurvivalInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.SURVIVAL);
 		enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's adventure inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveAdventureInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.ADVENTURE);
 		adventure.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's adventure ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveEnderAdventureInventory(Player player, World world){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		inventory.setWorld(world);
 		inventory.setGamemode(GameMode.ADVENTURE);
 		enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Gets a player's creative inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getCreativeInventory(Player player, World world){
 		ASInventory inventory = creative.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			creative.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's creative ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getEnderCreativeInventory(Player player, World world){
 		ASInventory inventory = enderCreative.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderCreative.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's survival inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getSurvivalInventory(Player player, World world){
 		ASInventory inventory = survival.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			survival.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's survival ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getEnderSurvivalInventory(Player player, World world){
 		ASInventory inventory = enderSurvival.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's adventure inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getAdventureInventory(Player player, World world){
 		ASInventory inventory = adventure.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			adventure.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's adventure ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @return the inventory
 	 */
 	public ASInventory getEnderAdventureInventory(Player player, World world){
 		ASInventory inventory = enderAdventure.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Loads the inventory manager
 	 */
 	public void load(){
 		// Clear
 		creative.clear();
 		survival.clear();
 		adventure.clear();
 		enderCreative.clear();
 		enderSurvival.clear();
 		enderAdventure.clear();
 		temporary.clear();
 		// Loads regions
 		for(Region region : plugin.getRegionManager().getAllRegions()){
 			String UID = region.getID();
 			List<ASInventory> inventory = ASInventory.generateInventory(UID, InventoryType.REGION);
 			if(inventory != null){
 				if(inventory.size() >= 1){
 					region.setInventory(inventory.get(0));
 				}else{
 					region.setInventory(null);
 				}
 			}
 		}
 
 		// Load all links
 		EnhancedConfiguration links = new EnhancedConfiguration(new File(plugin.getDataFolder(), "linked-inventories.yml"), plugin);
 		links.load();
 		Set<String> worlds = links.getKeys(false);
 		this.linkedInventories.clear();
 		if(worlds != null){
 			for(String w : worlds){
 				List<String> affectedWorlds = new ArrayList<String>();
 				affectedWorlds.add(w);
 				String otherWorlds = links.getString(w + ".linked");
 				String[] otherWorldsArray = otherWorlds.split(",");
 				for(String ow : otherWorldsArray){
 					affectedWorlds.add(ow.trim());
 				}
 				GameMode gamemode = ASUtils.getGameMode(links.getString(w + ".gamemode"));
 				if(gamemode != null && affectedWorlds.size() > 0){
 					String[] allWorlds;
 					allWorlds = affectedWorlds.toArray(new String[affectedWorlds.size()]);
 					LinkedInventory link = new LinkedInventory(gamemode, allWorlds);
 					this.linkedInventories.add(link);
 				}else{
 					plugin.getLogger().warning(plugin.getMessages().getMessage("bad-file", "linked-inventories.yml"));
 				}
 			}
 		}
 
 		// Status
 		if(this.linkedInventories.size() > 0){
 			plugin.getLogger().info(plugin.getMessages().getMessage("linked-inventories-loaded", String.valueOf(this.linkedInventories.size())));
 		}
 	}
 
 	/**
 	 * Saves the inventory manager
 	 */
 	public void save(){
 		// Save players
 		if(Bukkit.getOnlinePlayers() != null){
 			for(Player player : Bukkit.getOnlinePlayers()){
 				releasePlayer(player);
 			}
 		}
 
 		// Save inventories
 		for(String key : creative.keySet()){
 			creative.get(key).save();
 		}
 		for(String key : survival.keySet()){
 			survival.get(key).save();
 		}
 		for(String key : adventure.keySet()){
 			adventure.get(key).save();
 		}
 		for(String key : enderCreative.keySet()){
 			enderCreative.get(key).save();
 		}
 		for(String key : enderSurvival.keySet()){
 			enderSurvival.get(key).save();
 		}
 		for(String key : enderAdventure.keySet()){
 			enderAdventure.get(key).save();
 		}
 		for(Region region : plugin.getRegionManager().getAllRegions()){
 			if(region.getInventory() != null){
 				region.getInventory().save();
 			}
 		}
 	}
 
 	/**
 	 * Gets the number of loaded inventories
 	 * 
 	 * @return the number of loaded inventories
 	 */
 	public int getLoaded(){
 		return creative.size() + survival.size() + temporary.size() + adventure.size() + enderCreative.size() + enderSurvival.size() + enderAdventure.size();
 	}
 
 	/**
 	 * Fixes up world inventories by merging the specified world with the others
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void fixInventory(Player player, World world){
 		ASInventory creativeInventory, survivalInventory, adventureInventory, enderCreativeInventory, enderSurvivalInventory, enderAdventureInventory;
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, world);
 			saveEnderCreativeInventory(player, world);
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, world);
 			saveEnderSurvivalInventory(player, world);
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, world);
 			saveEnderAdventureInventory(player, world);
 			break;
 		default:
 			break;
 		}
 		creativeInventory = getCreativeInventory(player, world);
 		survivalInventory = getSurvivalInventory(player, world);
 		adventureInventory = getAdventureInventory(player, world);
 		enderCreativeInventory = getEnderCreativeInventory(player, world);
 		enderSurvivalInventory = getEnderSurvivalInventory(player, world);
 		enderAdventureInventory = getEnderAdventureInventory(player, world);
 		for(World w : Bukkit.getWorlds()){
 			String p = player.getName() + "." + w.getName();
 			creative.put(p, creativeInventory);
 			survival.put(p, survivalInventory);
 			adventure.put(p, adventureInventory);
 			enderCreative.put(p, enderCreativeInventory);
 			enderSurvival.put(p, enderSurvivalInventory);
 			enderAdventure.put(p, enderAdventureInventory);
 		}
 	}
 
 	/**
 	 * Checks the player for linked worlds. This must be called AFTER saving
 	 * 
 	 * @param player the player
 	 * @param to the 'going to' world
 	 * @param from the 'coming from' world
 	 */
 	public void checkLinks(Player player, World to, World from){
 		GameMode gamemode = player.getGameMode();
 		for(LinkedInventory link : linkedInventories){
 			if(link.isGameModeAffected(gamemode)){
 				if(link.isWorldAffected(from)){
 					String[] allWorlds = link.getAffectedWorlds();
 					ASInventory inventory = null;
 					ASInventory enderInventory = null;
 					switch (gamemode){
 					case SURVIVAL:
 						inventory = getSurvivalInventory(player, from);
 						enderInventory = getEnderSurvivalInventory(player, from);
 						break;
 					case CREATIVE:
 						inventory = getCreativeInventory(player, from);
 						enderInventory = getEnderCreativeInventory(player, from);
 						break;
 					case ADVENTURE:
 						inventory = getAdventureInventory(player, from);
 						enderInventory = getEnderAdventureInventory(player, from);
 					default:
 						break;
 					}
 					for(String world : allWorlds){
 						String p = player.getName() + "." + world;
 						if(world.equalsIgnoreCase(from.getName())){
 							continue;
 						}
 						switch (gamemode){
 						case CREATIVE:
 							creative.put(p, inventory);
 							enderCreative.put(p, enderInventory);
 							break;
 						case SURVIVAL:
 							survival.put(p, inventory);
 							enderSurvival.put(p, enderInventory);
 							break;
 						case ADVENTURE:
 							adventure.put(p, inventory);
 							enderAdventure.put(p, enderInventory);
 							break;
 						default:
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Saves all of a player's inventories
 	 * 
 	 * @param player the player to save
 	 */
 	public void savePlayer(Player player){
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld());
 			saveEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld());
 			saveEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, player.getWorld());
 			saveEnderAdventureInventory(player, player.getWorld());
 			break;
 		default:
 			break;
 		}
 		refreshInventories(player, true);
 		for(World w : plugin.getServer().getWorlds()){
 			String name = player.getName();
 			String world = w.getName();
 			if(creative.get(name + "." + world) != null){
 				creative.get(name + "." + world).save();
 			}
 			if(survival.get(name + "." + world) != null){
 				survival.get(name + "." + world).save();
 			}
 			if(adventure.get(name + "." + world) != null){
 				adventure.get(name + "." + world).save();
 			}
 			if(enderCreative.get(name + "." + world) != null){
 				enderCreative.get(name + "." + world).save();
 			}
 			if(enderSurvival.get(name + "." + world) != null){
 				enderSurvival.get(name + "." + world).save();
 			}
 			if(enderAdventure.get(name + "." + world) != null){
 				enderAdventure.get(name + "." + world).save();
 			}
 		}
 	}
 
 	/**
 	 * Reloads the inventory manager
 	 */
 	public void reload(){
 		save();
 		load();
 	}
 }
