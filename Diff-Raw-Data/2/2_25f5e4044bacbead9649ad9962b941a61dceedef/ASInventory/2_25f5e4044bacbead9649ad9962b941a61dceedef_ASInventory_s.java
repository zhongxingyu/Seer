 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.inventory;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.MaterialData;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.storage.SQL;
 
 /**
  * AntiShare Inventory
  * 
  * @author turt2live
  */
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
 
 		if(AntiShare.getInstance().useSQL()){
 			// SQL load
 
 			// Setup
 			for(World world : Bukkit.getWorlds()){
 				for(GameMode gamemode : GameMode.values()){
 					try{
 						ResultSet items = AntiShare.getInstance().getSQL().get("SELECT * FROM `" + SQL.INVENTORIES_TABLE + "` WHERE `name`='" + name + "' AND `type`='" + type.name() + "' AND `gamemode`='" + gamemode.name() + "' AND `world`='" + world.getName() + "'");
 						ASInventory inventory = new ASInventory(type, name, world, gamemode);
 
 						// Get items
 						if(items != null){
 							while (items.next()){
 								int slot = items.getInt("slot");
 
 								// Item properties
 								int id = items.getInt("itemID");
 								String durability = items.getString("itemDurability");
 								int amount = items.getInt("itemAmount");
 								byte data = Byte.parseByte(items.getString("itemData"));
 
 								// Create item
 								ItemStack item = new ItemStack(id);
 								item.setAmount(amount);
 								MaterialData itemData = item.getData();
 								itemData.setData(data);
 								item.setData(itemData);
 								item.setDurability(Short.parseShort(durability));
 								String enchants[] = items.getString("itemEnchant").split(" ");
 								if(items.getString("itemEnchant").length() > 0){
 									for(String enchant : enchants){
 										String parts[] = enchant.split("\\|");
 										String enchantID = parts[0];
 										int level = Integer.parseInt(parts[1]);
 										Enchantment e = Enchantment.getById(Integer.parseInt(enchantID));
 										item.addEnchantment(e, level);
 									}
 								}
 
 								// Set
 								inventory.set(slot, item);
 							}
 						}
 
 						// Save item to map
 						inventories.add(inventory);
 					}catch(SQLException e){
 						AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
 						e.printStackTrace();
 					}
 				}
 			}
 		}else{
 			// Flat-File (YAML) load
 
 			// Setup
 			File dir = new File(AntiShare.getInstance().getDataFolder(), "inventories" + File.separator + type.getRelativeFolderName());
 			dir.mkdirs();
 			File saveFile = new File(dir, name + ".yml");
 			if(!saveFile.exists()){
 				return inventories;
 			}
 			EnhancedConfiguration file = new EnhancedConfiguration(saveFile, AntiShare.getInstance());
 			file.load();
 
 			// Load data
 			// Structure: yml:world.gamemode.slot.properties
 			for(String world : file.getKeys(false)){
 				for(String gamemode : file.getConfigurationSection(world).getKeys(false)){
 					World worldV = Bukkit.getWorld(world);
 					if(worldV == null){
 						AntiShare.getInstance().log("World '" + world + "' does not exist (Inventory: " + type.name() + ", " + name + ".yml) AntiShare is ignoring this world.", Level.SEVERE);
 						continue;
 					}
 					ASInventory inventory = new ASInventory(type, name, worldV, GameMode.valueOf(gamemode));
 					for(String strSlot : file.getConfigurationSection(world + "." + gamemode).getKeys(false)){
 						Integer slot = Integer.valueOf(strSlot);
 						inventory.set(slot, file.getItemStack(world + "." + gamemode + "." + strSlot));
 					}
 					inventories.add(inventory);
 				}
 			}
 		}
 
 		// return
 		return inventories;
 	}
 
 	private HashMap<Integer, ItemStack> inventory = new HashMap<Integer, ItemStack>();
 	private AntiShare plugin;
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
 		plugin = AntiShare.getInstance();
 		this.type = type;
 		this.inventoryName = inventoryName;
 		this.world = world;
 		this.gamemode = gamemode;
 	}
 
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
 		Inventory pInventory;
 		if(type == InventoryType.ENDER){
 			pInventory = player.getEnderChest();
 		}else{
 			pInventory = player.getInventory();
 			ItemStack air = new ItemStack(Material.AIR);
 			ItemStack[] armor = {air, air, air, air};
 			((PlayerInventory) pInventory).setArmorContents(armor);
 		}
 		pInventory.clear();
 		for(Integer slot : inventory.keySet()){
 			ItemStack item = inventory.get(slot);
 			if(item == null){
 				inventory.put(slot, new ItemStack(Material.AIR, 1));
 				item = new ItemStack(Material.AIR, 1);
 			}
 			if(slot < 100){
 				pInventory.setItem(slot, item);
 			}else{
 				if(pInventory instanceof PlayerInventory){
 					switch (slot){
 					case 100:
 						((PlayerInventory) pInventory).setBoots(item);
 						break;
 					case 101:
 						((PlayerInventory) pInventory).setLeggings(item);
 						break;
 					case 102:
 						((PlayerInventory) pInventory).setChestplate(item);
 						break;
 					case 103:
 						((PlayerInventory) pInventory).setHelmet(item);
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
 		if(plugin.useSQL()){
 			// SQL save
 
 			// Loop
 			for(Integer slot : inventory.keySet()){
				plugin.getSQL().update("DELETE FROM `" + SQL.INVENTORIES_TABLE + "` WHERE `type`='" + type.name() + "' AND `name`='" + inventoryName + "' AND `gamemode`='" + gamemode.name() + "' AND `world`='" + world.getName() + "' AND `slot`='" + slot + "' LIMIT 1");
 
 				// Don't save AIR
 				ItemStack item = inventory.get(slot);
 				if(item == null || item.getType() == Material.AIR){
 					continue;
 				}
 
 				// Setup
 				int itemID = item.getTypeId();
 				String itemName = item.getType().name();
 				int itemDurability = item.getDurability();
 				int itemAmount = item.getAmount();
 				int itemData = item.getData().getData();
 
 				// Setup enchants
 				String enchant = "";
 				Set<Enchantment> enchantsSet = item.getEnchantments().keySet();
 				Map<Enchantment, Integer> enchantsMap = item.getEnchantments();
 				for(Enchantment e : enchantsSet){
 					enchant = enchant + e.getId() + "|" + enchantsMap.get(e) + " ";
 				}
 				if(enchant.length() > 0){
 					enchant = enchant.substring(0, enchant.length() - 1);
 				}
 
 				// Save
 				plugin.getSQL().update("INSERT INTO `" + SQL.INVENTORIES_TABLE + "` (`type`, `name`, `gamemode`, `world`, `slot`, `itemID`, `itemName`, `itemDurability`, `itemAmount`, `itemData`, `itemEnchant`) VALUES ('" + type.name() + "', '" + inventoryName + "', '" + gamemode.name() + "', '" + world.getName() + "', '" + slot + "', '" + itemID + "', '" + itemName + "', '" + itemDurability + "', '" + itemAmount + "', '" + itemData + "', '" + enchant + "')");
 			}
 		}else{
 			// Flat-File (YAML) save
 
 			// Setup
 			File dir = new File(plugin.getDataFolder(), "inventories" + File.separator + type.getRelativeFolderName());
 			dir.mkdirs();
 			File saveFile = new File(dir, inventoryName + ".yml");
 			EnhancedConfiguration file = new EnhancedConfiguration(saveFile, plugin);
 			file.load();
 
 			// Save data
 			// Structure: yml:world.gamemode.slot.properties
 			for(Integer slot : inventory.keySet()){
 				if(inventory.get(slot) == null){
 					continue;
 				}
 
 				// Don't save AIR
 				ItemStack item = inventory.get(slot);
 				if(item.getType() == Material.AIR){
 					file.set(world.getName() + "." + gamemode.name() + "." + String.valueOf(slot), null);
 					continue;
 				}
 
 				// Save item
 				file.set(world.getName() + "." + gamemode.name() + "." + String.valueOf(slot), item);
 			}
 			file.save();
 		}
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
 		ASInventory newI = new ASInventory(this.type, this.inventoryName, this.world, this.gamemode);
 		for(int slot : this.inventory.keySet()){
 			newI.set(slot, this.inventory.get(slot));
 		}
 		return newI;
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
 }
