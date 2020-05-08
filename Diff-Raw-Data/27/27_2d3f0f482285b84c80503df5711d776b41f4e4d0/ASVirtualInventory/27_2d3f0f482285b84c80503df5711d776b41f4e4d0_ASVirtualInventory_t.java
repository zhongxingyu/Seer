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
 
 public class ASVirtualInventory {
 
 	private AntiShare plugin;
 	private Player player;
 	private World world;
 	private HashMap<Integer, ItemStack> survival = new HashMap<Integer, ItemStack>();
 	private HashMap<Integer, ItemStack> creative = new HashMap<Integer, ItemStack>();
 
 	public ASVirtualInventory(Player player, World world, AntiShare plugin){
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
 				EnhancedConfiguration config = new EnhancedConfiguration(saveFile, plugin);
 				config.load();
 				Integer i = 0;
 				Integer size = player.getInventory().getSize();
 				for(i = 0; i < size; i++){
 					ItemStack item = new ItemStack(0, 0);
 					if(config.getItemStack(i.toString()) != null){
 						item = config.getItemStack(i.toString());
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
 
 	private void saveInventory(){
 		saveInventory(player.getGameMode());
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
 		saveInventory();
 		saveToDisk(GameMode.CREATIVE, getCreativeInventory());
 		saveToDisk(GameMode.SURVIVAL, getSurvivalInventory());
 	}
 
 	private void saveToDisk(GameMode gamemode, HashMap<Integer, ItemStack> inventoryMap){
 		wipe();
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
 			if(!saveFile.exists()){
 				saveFile.createNewFile();
 			}
 			EnhancedConfiguration config = new EnhancedConfiguration(saveFile, plugin);
 			config.load();
			for(Integer slot : inventoryMap.keySet()){
				config.set(String.valueOf(slot), inventoryMap.get(slot));
 			}
 			config.save();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	private void wipe(){
 		boolean skip = false;
 		if(plugin.getConfig().getBoolean("SQL.use") && plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				SQLManager sql = plugin.getSQLManager();
 				sql.deleteQuery("DELETE FROM AntiShare_Inventory WHERE username='" + player.getName() + "' AND gamemode='" + player.getGameMode().toString() + "' AND world='" + world.getName() + "'");
 			}
 		}
 		if(skip){
 			return;
 		}
 		File sdir = new File(plugin.getDataFolder(), "inventories");
 		sdir.mkdirs();
 		File saveFile = new File(sdir, player.getName() + "_" + player.getGameMode().toString() + "_" + world.getName() + ".yml");
 		if(saveFile.exists()){
 			saveFile.delete();
 			try{
 				saveFile.createNewFile();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
