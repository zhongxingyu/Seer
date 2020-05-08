 package com.turt2live.antishare.storage;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.SQL.SQLManager;
 
 public class VirtualInventory {
 
 	private AntiShare plugin;
 	private Player player;
 	private World world;
 	private HashMap<Integer, ItemStack> survival = new HashMap<Integer, ItemStack>();
 	private HashMap<Integer, ItemStack> creative = new HashMap<Integer, ItemStack>();
 	private HashMap<Integer, ItemStack> temporary = new HashMap<Integer, ItemStack>();
 	private HashMap<Integer, ItemStack> previous = new HashMap<Integer, ItemStack>();
 	private boolean isInTemp = false;
 
 	public VirtualInventory(Player player, World world, AntiShare plugin){
 		this.player = player;
 		this.world = world;
 		this.plugin = plugin;
 		load();
 	}
 
 	public HashMap<Integer, ItemStack> getCreativeInventory(){
 		return creative;
 	}
 
 	public HashMap<Integer, ItemStack> getSurvivalInventory(){
 		return survival;
 	}
 
 	public void load(){
 		survival = load(GameMode.SURVIVAL);
 		creative = load(GameMode.CREATIVE);
 	}
 
 	public void makeMatch(){
 		// Match each item
 		HashMap<Integer, ItemStack> current = getCurrentInventory();
 		HashMap<Integer, ItemStack> proper = getInventory(player.getGameMode());
 		for(Integer slot : current.keySet()){
 			if(current.get(slot) != null && proper.get(slot) != null){
 				if(!current.get(slot).equals(proper.get(slot))){
 					switchInventories((player.getGameMode().equals(GameMode.SURVIVAL) ? GameMode.CREATIVE : GameMode.SURVIVAL), player.getGameMode());
 					return;
 				}
 			}
 		}
 	}
 
 	public void setTemporaryInventory(HashMap<Integer, ItemStack> inventory){
 		temporary = inventory;
 	}
 
 	@SuppressWarnings ("deprecation")
 	public void loadToTemporary(){
 
 		saveInventory(player.getGameMode());
 		previous = getInventory(player.getGameMode());
 		isInTemp = true;
 		HashMap<Integer, ItemStack> inventory = temporary;
 		player.getInventory().clear();
 		for(Integer slot : inventory.keySet()){
 			player.getInventory().setItem(slot, inventory.get(slot));
 		}
 		player.updateInventory();
 	}
 
 	@SuppressWarnings ("deprecation")
 	public void unloadFromTemporary(){
 		isInTemp = false;
 		HashMap<Integer, ItemStack> inventory = previous;
 		player.getInventory().clear();
 		for(Integer slot : inventory.keySet()){
 			player.getInventory().setItem(slot, inventory.get(slot));
 		}
 		player.updateInventory();
 	}
 
 	public boolean isTemp(){
 		return isInTemp;
 	}
 
 	public HashMap<Integer, ItemStack> getInventory(GameMode gamemode){
 		if(gamemode.equals(GameMode.CREATIVE)){
 			return getCreativeInventory();
 		}else if(gamemode.equals(GameMode.SURVIVAL)){
 			return getSurvivalInventory();
 		}
 		return null;
 	}
 
 	public HashMap<Integer, ItemStack> getCurrentInventory(){
 		HashMap<Integer, ItemStack> inventory = new HashMap<Integer, ItemStack>();
 		for(int slot = 0; slot < player.getInventory().getSize(); slot++){
 			ItemStack item = player.getInventory().getItem(slot);
 			inventory.put(slot, item);
 		}
 		return inventory;
 	}
 
 	public void switchInventories(GameMode from, GameMode to){
 		if(!isInTemp){
 			saveInventory(from);
 		}
 		loadInventory(to);
 	}
 
 	@SuppressWarnings ("deprecation")
 	public void loadInventory(GameMode gamemode){
 		HashMap<Integer, ItemStack> inventory = null;
 		if(gamemode.equals(GameMode.CREATIVE)){
 			inventory = getCreativeInventory();
 		}else if(gamemode.equals(GameMode.SURVIVAL)){
 			inventory = getSurvivalInventory();
 		}
 		player.getInventory().clear();
 		for(Integer slot : inventory.keySet()){
 			player.getInventory().setItem(slot, inventory.get(slot));
 		}
 		player.updateInventory();
 	}
 
 	private HashMap<Integer, ItemStack> load(GameMode gamemode){
 		boolean skip = false;
 		HashMap<Integer, ItemStack> inventoryMap = new HashMap<Integer, ItemStack>();
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				ResultSet inventory = sql.getQuery("SELECT * FROM AntiShare_Inventory WHERE username='" + player.getName() + "' AND gamemode='" + gamemode.toString() + "' AND world='" + world.getName() + "'");
 				if(inventory != null){
 					try{
 						while (inventory.next()){
 							int slot = inventory.getInt("slot");
 							int id = inventory.getInt("itemID");
 							String durability = inventory.getString("itemDurability");
 							int amount = inventory.getInt("itemAmount");
 							byte data = Byte.parseByte(inventory.getString("itemData"));
 							String enchants[] = inventory.getString("itemEnchant").split(" ");
 							ItemStack item = new ItemStack(id);
 							item.setAmount(amount);
 							MaterialData itemData = item.getData();
 							itemData.setData(data);
 							item.setData(itemData);
 							item.setDurability(Short.parseShort(durability));
 							if(inventory.getString("itemEnchant").length() > 0){
 								for(String enchant : enchants){
 									String parts[] = enchant.split("\\|");
 									String enchantID = parts[0];
 									int level = Integer.parseInt(parts[1]);
 									Enchantment e = Enchantment.getById(Integer.parseInt(enchantID));
 									item.addEnchantment(e, level);
 								}
 							}
 							inventoryMap.put(slot, item);
 						}
 						skip = true;
 					}catch(SQLException e){
 						AntiShare.log.severe("[" + plugin.getDescription().getFullName() + "] Cannot handle inventory: " + e.getMessage());
 					}
 				}else{
 					skip = true;
 				}
 			}
 		}
 		if(!skip){
 			inventoryMap.clear();
 			try{
 				File sdir = new File(plugin.getDataFolder(), "inventories");
 				sdir.mkdirs();
 				File saveFile = new File(sdir, player.getName() + "_" + gamemode.toString() + "_" + world.getName() + ".yml");
 				if(!saveFile.exists()){
 					saveFile.createNewFile();
 				}
 				EnhancedConfiguration config = new EnhancedConfiguration("inventories/" + saveFile.getName(), plugin);
 				if(!config.load()){
 					AntiShare.log.severe("[AntiShare] CANNOT LOAD INVENTORY FILE: " + saveFile.getName());
 				}
 				Integer i = 0;
 				Integer size = player.getInventory().getSize();
 				for(i = 0; i < size; i++){
 					if(config.getItemStack(String.valueOf(i)) != null){
 						ItemStack item = config.getItemStack(i.toString());
 						inventoryMap.put(i, item);
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return inventoryMap;
 	}
 
 	public void reload(){
 		saveInventoryToDisk();
 		load();
 	}
 
 	public void saveInventory(GameMode gamemode){
 		HashMap<Integer, ItemStack> newInventory = new HashMap<Integer, ItemStack>();
 		for(int slot = 0; slot < player.getInventory().getSize(); slot++){
 			ItemStack item = player.getInventory().getItem(slot);
 			newInventory.put(slot, item);
 		}
 		if(gamemode.equals(GameMode.CREATIVE)){
 			creative = newInventory;
 		}else if(gamemode.equals(GameMode.SURVIVAL)){
 			survival = newInventory;
 		}
 	}
 
 	public void saveInventoryToDisk(){
 		saveInventory(player.getGameMode());
 		saveToDisk(GameMode.CREATIVE, getCreativeInventory());
 		saveToDisk(GameMode.SURVIVAL, getSurvivalInventory());
 	}
 
 	private void saveToDisk(GameMode gamemode, HashMap<Integer, ItemStack> inventoryMap){
 		wipe(gamemode);
 		boolean skip = false;
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				for(Integer slot : inventoryMap.keySet()){
 					ItemStack item = inventoryMap.get(slot);
 					String id = item.getTypeId() + "";
 					String name = item.getType().name();
 					String durability = item.getDurability() + "";
 					String amount = item.getAmount() + "";
 					String data = item.getData().getData() + "";
 					String enchant = "";
 					Set<Enchantment> enchantsSet = item.getEnchantments().keySet();
 					Map<Enchantment, Integer> enchantsMap = item.getEnchantments();
 					for(Enchantment e : enchantsSet){
 						enchant = enchant + e.getId() + "|" + enchantsMap.get(e) + " ";
 					}
 					if(enchant.length() > 0){
 						enchant = enchant.substring(0, enchant.length() - 1);
 					}
 					sql.insertQuery("INSERT INTO AntiShare_Inventory (username, gamemode, slot, itemID, itemName, itemDurability, itemAmount, itemData, itemEnchant, world) " +
 							"VALUES ('" + player.getName() + "', '" + gamemode.toString() + "', '" + slot + "', '" + id + "', '" + name + "', '" + durability + "', '" + amount + "', '" + data + "', '" + enchant + "', '" + world.getName() + "')");
 				}
 				skip = true;
 			}
 		}
 		if(skip){
 			return;
 		}
 		try{
 			File sdir = new File(plugin.getDataFolder(), "inventories");
 			sdir.mkdirs();
 			File saveFile = new File(sdir, player.getName() + "_" + gamemode.toString() + "_" + world.getName() + ".yml");
 			EnhancedConfiguration config = new EnhancedConfiguration(saveFile, plugin);
 			if(!config.load()){
 				AntiShare.log.severe("[AntiShare] CANNOT LOAD INVENTORY FILE: " + saveFile.getName());
 			}
 			for(Integer slot : inventoryMap.keySet()){
 				config.set(String.valueOf(slot), inventoryMap.get(slot));
 				if(!config.save()){
 					AntiShare.log.severe("[AntiShare] CANNOT SAVE INVENTORY FILE: " + saveFile.getName());
 				}
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	private void wipe(GameMode gamemode){
 		boolean skip = false;
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				sql.deleteQuery("DELETE FROM AntiShare_Inventory WHERE username='" + player.getName() + "' AND gamemode='" + gamemode.toString() + "' AND world='" + world.getName() + "'");
 				skip = true;
 			}
 		}
 		if(skip){
 			return;
 		}
 		File sdir = new File(plugin.getDataFolder(), "inventories");
 		sdir.mkdirs();
 		File saveFile = new File(sdir, player.getName() + "_" + gamemode.toString() + "_" + world.getName() + ".yml");
 		if(saveFile.exists()){
 			saveFile.delete();
 			try{
 				saveFile.createNewFile();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static HashMap<Integer, ItemStack> getInventoryFromPlayer(Player player){
 		HashMap<Integer, ItemStack> newInventory = new HashMap<Integer, ItemStack>();
 		for(int slot = 0; slot < player.getInventory().getSize(); slot++){
 			ItemStack item = player.getInventory().getItem(slot);
 			newInventory.put(slot, item);
 		}
 		return newInventory;
 	}
 
 	public static void saveInventoryToDisk(File file, HashMap<Integer, ItemStack> inventory, AntiShare plugin){
 		if(inventory == null || file == null || plugin == null){
 			return;
 		}
 		if(inventory.size() <= 0){
 			return;
 		}
 		boolean flatfile = true;
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				for(Integer slot : inventory.keySet()){
 					ItemStack item = inventory.get(slot);
 					String id = item.getTypeId() + "";
 					String name = item.getType().name();
 					String durability = item.getDurability() + "";
 					String amount = item.getAmount() + "";
 					String data = item.getData().getData() + "";
 					String enchant = "";
 					Set<Enchantment> enchantsSet = item.getEnchantments().keySet();
 					Map<Enchantment, Integer> enchantsMap = item.getEnchantments();
 					for(Enchantment e : enchantsSet){
 						enchant = enchant + e.getId() + "|" + enchantsMap.get(e) + " ";
 					}
 					if(enchant.length() > 0){
 						enchant = enchant.substring(0, enchant.length() - 1);
 					}
 					sql.insertQuery("INSERT INTO AntiShare_MiscInventory (uniqueID, slot, itemID, itemName, itemDurability, itemAmount, itemData, itemEnchant) " +
 							"VALUES ('" + file.getName() + "', '" + slot + "', '" + id + "', '" + name + "', '" + durability + "', '" + amount + "', '" + data + "', '" + enchant + "')");
 				}
 				flatfile = false;
 			}
 		}
 		if(flatfile){
 			if(file.exists()){
 				file.delete();
 				try{
 					file.createNewFile();
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			try{
 				File sdir = file.getParentFile();
 				sdir.mkdirs();
 				File saveFile = file;
 				EnhancedConfiguration config = new EnhancedConfiguration(saveFile, plugin);
 				if(!config.load()){
 					AntiShare.log.severe("[AntiShare] CANNOT LOAD INVENTORY FILE: " + saveFile.getName());
 				}
 				for(Integer slot : inventory.keySet()){
 					config.set(String.valueOf(slot), inventory.get(slot));
 					if(!config.save()){
 						AntiShare.log.severe("[AntiShare] CANNOT SAVE INVENTORY FILE: " + saveFile.getName());
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static HashMap<Integer, ItemStack> getInventoryFromDisk(File file, AntiShare plugin){
 		if(file == null || plugin == null){
 			return null;
 		}
 		HashMap<Integer, ItemStack> inventoryMap = new HashMap<Integer, ItemStack>();
 		boolean flatfile = true;
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				ResultSet inventory = sql.getQuery("SELECT * FROM AntiShare_MiscInventory WHERE uniqueID='" + file.getName() + "'");
 				try{
 					if(inventory != null){
 						while (inventory.next()){
 							int slot = inventory.getInt("slot");
 							int id = inventory.getInt("itemID");
 							String durability = inventory.getString("itemDurability");
 							int amount = inventory.getInt("itemAmount");
 							byte data = Byte.parseByte(inventory.getString("itemData"));
 							String enchants[] = inventory.getString("itemEnchant").split(" ");
 							ItemStack item = new ItemStack(id);
 							item.setAmount(amount);
 							MaterialData itemData = item.getData();
 							itemData.setData(data);
 							item.setData(itemData);
 							item.setDurability(Short.parseShort(durability));
 							if(inventory.getString("itemEnchant").length() > 0){
 								for(String enchant : enchants){
 									String parts[] = enchant.split("\\|");
 									String enchantID = parts[0];
 									int level = Integer.parseInt(parts[1]);
 									Enchantment e = Enchantment.getById(Integer.parseInt(enchantID));
 									item.addEnchantment(e, level);
 								}
 							}
 							inventoryMap.put(slot, item);
 						}
 					}
 					flatfile = false;
 				}catch(SQLException e){
 					AntiShare.log.severe("[" + plugin.getDescription().getFullName() + "] Cannot handle misc inventory: " + e.getMessage());
 				}
 			}
 		}
 		if(flatfile){
 			if(file.exists()){
 				file.delete();
 				try{
 					file.createNewFile();
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			try{
 				File sdir = new File(plugin.getDataFolder(), "inventories");
 				sdir.mkdirs();
 				File saveFile = file;
 				saveFile.getParentFile().mkdirs();
 				if(!saveFile.exists()){
 					saveFile.createNewFile();
 				}
 				EnhancedConfiguration config = new EnhancedConfiguration(saveFile, plugin);
 				if(!config.load()){
 					AntiShare.log.severe("[AntiShare] CANNOT LOAD INVENTORY FILE: " + saveFile.getName());
 				}
				Set<String> keys = config.getKeys(false);
 				for(String key : keys){
 					inventoryMap.put(Integer.valueOf(key), config.getItemStack(key));
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return inventoryMap;
 	}
 }
