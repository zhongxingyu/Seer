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
 package com.turt2live.antishare.inventory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 
 /**
  * AntiShare Inventory
  * 
  * @author turt2live
  */
// TODO: Schedule for rewrite
 public class ASInventory implements Cloneable {
 
 	/**
 	 * An enum to represent inventory types
 	 * 
 	 * @author turt2live
 	 */
 	public static enum InventoryType{
 		PLAYER("players"),
 		REGION("regions"),
 		TEMPORARY("temporary"),
 		ENDER("ender");
 
 		private String relativeFolderName;
 
 		private InventoryType(String relativeFolderName){
 			this.relativeFolderName = relativeFolderName;
 		}
 
 		/**
 		 * Gets the relative folder name
 		 * 
 		 * @return the folder
 		 */
 		public String getRelativeFolderName(){
 			return relativeFolderName;
 		}
 	}
 
 	/**
 	 * Generates an AntiShare Inventory from a player
 	 * 
 	 * @param player the player
 	 * @param type the inventory type to generate
 	 * @return the inventory
 	 */
 	public static ASInventory generate(Player player, InventoryType type){
 		ASInventory inventory = new ASInventory(type, player.getName(), player.getWorld(), player.getGameMode());
 		if(type == InventoryType.ENDER){
 			ItemStack[] contents = player.getEnderChest().getContents();
 			int slot = 0;
 			for(ItemStack item : contents){
 				inventory.set(slot, item);
 				slot++;
 			}
 		}else{
 			ItemStack[] contents = player.getInventory().getContents();
 			int slot = 0;
 			for(ItemStack item : contents){
 				inventory.set(slot, item);
 				slot++;
 			}
 			contents = player.getInventory().getArmorContents();
 			slot = 100;
 			for(ItemStack item : contents){
 				inventory.set(slot, item);
 				slot++;
 			}
 		}
 		return inventory;
 	}
 
 	/**
 	 * Generates an inventory list
 	 * 
 	 * @param name inventory name
 	 * @param type the Inventory Type
 	 * @return the inventories
 	 */
 	public static List<ASInventory> generateInventory(String name, InventoryType type){
 		// Setup
 		List<ASInventory> inventories = new ArrayList<ASInventory>();
 
 		// Configuration check
 		if(!AntiShare.p.settings().features.inventories){
 			return inventories;
 		}
 		// Setup
 		File dir = new File(AntiShare.p.getDataFolder(), "data" + File.separator + "inventories" + File.separator + type.getRelativeFolderName());
 		File saveFile = new File(dir, name + ".yml");
 		if(!saveFile.exists()){
 			return inventories;
 		}
 		EnhancedConfiguration file = new EnhancedConfiguration(saveFile, AntiShare.p);
 		file.load();
 
 		// Load data
 		// Structure: yml:world.gamemode.slot.properties
 		List<String> removeWorlds = new ArrayList<String>();
 		for(String world : file.getKeys(false)){
 			for(String gamemode : file.getConfigurationSection(world).getKeys(false)){
 				World bukkitWorld = Bukkit.getWorld(world);
 				if(bukkitWorld == null){
 					AntiShare.p.getLogger().severe(AntiShare.p.getMessages().getMessage("world-missing", world, type.name(), name));
 					if(AntiShare.p.settings().inventoryCleanupSettings.removeOldWorlds){
 						AntiShare.p.getLogger().severe("=== " + AntiShare.p.getMessages().getMessage("remove-world-1") + " ===");
 						AntiShare.p.getLogger().severe(AntiShare.p.getMessages().getMessage("remove-world-2"));
 						removeWorlds.add(world);
 					}
 					continue;
 				}
 				if(gamemode.equalsIgnoreCase("adventure") || gamemode.equalsIgnoreCase("creative") || gamemode.equalsIgnoreCase("survival")){
 					ASInventory inventory = new ASInventory(type, name, bukkitWorld, GameMode.valueOf(gamemode));
 					for(String stringSlot : file.getConfigurationSection(world + "." + gamemode).getKeys(false)){
 						Integer slot = Integer.valueOf(stringSlot);
 						ItemStack item = file.getItemStack(world + "." + gamemode + "." + stringSlot);
 						inventory.set(slot, item);
 					}
 					inventories.add(inventory);
 				}
 			}
 		}
 		// Remove old worlds
 		if(AntiShare.p.settings().inventoryCleanupSettings.removeOldWorlds){ // Safe-guard check
 			for(String world : removeWorlds){
 				file.set(world, null);
 			}
 			file.save();
 		}
 
 		// return
 		return inventories;
 	}
 
 	private final HashMap<Integer, ItemStack> inventory = new HashMap<Integer, ItemStack>();
 	private final AntiShare plugin;
 	private InventoryType type = InventoryType.PLAYER;
 	private String inventoryName = "UNKNOWN";
 	private World world;
 	private GameMode gamemode;
 
 	/**
 	 * Creates a new AntiShare Inventory
 	 * 
 	 * @param type the type
 	 * @param inventoryName the name
 	 * @param world the world
 	 * @param gamemode the gamemode
 	 */
 	public ASInventory(InventoryType type, String inventoryName, World world, GameMode gamemode){
 		plugin = AntiShare.p;
 		this.type = type;
 		this.inventoryName = inventoryName;
 		this.world = world;
 		this.gamemode = gamemode;
 	}
 
 	/**
 	 * Determines if the inventory is empty
 	 * 
 	 * @return true if empty, false otherwise
 	 */
 	public boolean isEmpty(){
 		for(Integer slot : inventory.keySet()){
 			ItemStack stack = inventory.get(slot);
 			if(stack != null && stack.getType() != Material.AIR){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Sets a slot to an item
 	 * 
 	 * @param slot the slot
 	 * @param item the item
 	 */
 	public void set(int slot, ItemStack item){
 		if(item == null){
 			item = new ItemStack(Material.AIR, 1);
 		}
 		inventory.put(slot, item);
 	}
 
 	/**
 	 * Sets the player's inventory to this inventory
 	 * 
 	 * @param player the player
 	 */
 	@SuppressWarnings ("deprecation")
 	public void setTo(Player player){
 		Inventory playerInventory;
 		if(type == InventoryType.ENDER){
 			playerInventory = player.getEnderChest();
 		}else{
 			playerInventory = player.getInventory();
 			ItemStack air = new ItemStack(Material.AIR);
 			ItemStack[] armor = {air, air, air, air};
 			((PlayerInventory) playerInventory).setArmorContents(armor);
 		}
 		playerInventory.clear();
 		for(Integer slot : inventory.keySet()){
 			ItemStack item = inventory.get(slot);
 			if(item == null){
 				inventory.put(slot, new ItemStack(Material.AIR, 1));
 				item = new ItemStack(Material.AIR, 1);
 			}
 			if(slot < 100){
 				playerInventory.setItem(slot, item);
 			}else{
 				if(playerInventory instanceof PlayerInventory){
 					switch (slot){
 					case 100:
 						((PlayerInventory) playerInventory).setBoots(item);
 						break;
 					case 101:
 						((PlayerInventory) playerInventory).setLeggings(item);
 						break;
 					case 102:
 						((PlayerInventory) playerInventory).setChestplate(item);
 						break;
 					case 103:
 						((PlayerInventory) playerInventory).setHelmet(item);
 						break;
 					}
 				}
 			}
 		}
 		player.updateInventory();
 	}
 
 	/**
 	 * Saves the inventory to disk
 	 */
 	public void save(){
 		// Configuration check
 		if(!plugin.settings().features.inventories){
 			return;
 		}
 		// Setup
 		File directory = new File(plugin.getDataFolder(), "data" + File.separator + "inventories" + File.separator + type.getRelativeFolderName());
 		File saveFile = new File(directory, inventoryName + ".yml");
 		EnhancedConfiguration yamlFile = new EnhancedConfiguration(saveFile, plugin);
 		yamlFile.load();
 		yamlFile.set(world.getName() + "." + gamemode.name(), null);
 
 		// Save data
 		// Structure: yml:world.gamemode.slot.properties
 		for(Integer slot : inventory.keySet()){
 			// Don't save AIR
 			ItemStack item = inventory.get(slot);
 			if(item == null || item.getType() == Material.AIR){
 				continue;
 			}
 
 			// Save item
 			yamlFile.set(world.getName() + "." + gamemode.name() + "." + String.valueOf(slot), item);
 		}
 		yamlFile.save();
 	}
 
 	/**
 	 * Gets the world of this inventory
 	 * 
 	 * @return the world
 	 */
 	public World getWorld(){
 		return world;
 	}
 
 	/**
 	 * Gets the game mode of this inventory
 	 * 
 	 * @return the game mode
 	 */
 	public GameMode getGameMode(){
 		return gamemode;
 	}
 
 	/**
 	 * Changes the type of this inventory
 	 * 
 	 * @param type the new type
 	 */
 	public void setType(InventoryType type){
 		this.type = type;
 	}
 
 	/**
 	 * Gets the inventory type
 	 * 
 	 * @return the type
 	 */
 	public InventoryType getType(){
 		return type;
 	}
 
 	@Override
 	public ASInventory clone(){
 		ASInventory newInventory = new ASInventory(this.type, this.inventoryName, this.world, this.gamemode);
 		for(int slot : this.inventory.keySet()){
 			newInventory.set(slot, this.inventory.get(slot));
 		}
 		return newInventory;
 	}
 
 	/**
 	 * Sets the gamemode of the inventory
 	 * 
 	 * @param gamemode the game mode
 	 */
 	public void setGamemode(GameMode gamemode){
 		this.gamemode = gamemode;
 	}
 
 	/**
 	 * Set the world this inventory is bound to
 	 * 
 	 * @param world the world
 	 */
 	public void setWorld(World world){
 		this.world = world;
 	}
 
 	/**
 	 * Gets the size of this inventory
 	 * 
 	 * @return inventory size
 	 */
 	public int getSize(){
 		switch (this.type){
 		case ENDER:
 			return 27;
 		case PLAYER:
 		case REGION:
 		case TEMPORARY:
 		default:
 			return 36;
 		}
 	}
 
 	void populateOtherInventory(Inventory inventory){
 		inventory.clear();
 		for(Integer slot : this.inventory.keySet()){
 			inventory.setItem(slot, this.inventory.get(slot));
 		}
 	}
 
 	void populateSelf(Inventory inventory){
 		for(int i = 0; i < inventory.getSize(); i++){
 			ItemStack item = inventory.getItem(i);
 			if(item == null || item.getType() == Material.AIR){
 				continue;
 			}
 			set(i, item);
 		}
 		inventory.clear();
 	}
 
 	/**
 	 * Gets the name of this inventory
 	 * 
 	 * @return the inventory name
 	 */
 	public String getName(){
 		return inventoryName;
 	}
 
 }
