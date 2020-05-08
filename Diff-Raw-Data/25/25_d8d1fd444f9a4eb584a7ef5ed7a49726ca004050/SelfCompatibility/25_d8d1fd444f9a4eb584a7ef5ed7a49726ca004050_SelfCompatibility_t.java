 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.inventory.LegacyInventory;
 import com.turt2live.antishare.inventory.OASI;
 import com.turt2live.antishare.util.ASMaterialList.ASMaterial;
 
 /**
  * Compatibility class for other AntiShare versions
  */
public class SelfCompatibility{
 
 	private static enum CompatibilityType{
 		BLOCK_CONVERSION(0),
 		REGION_PLAYER_DATA_MIGRATE(5),
 		INVENTORY_313(10),
 		WORLD_CONFIGURATION_MIGRATE(15),
 		// Above is the estimated version applied in
 		// -------------------------------------------
 		// Below is the version applied in
 		@Deprecated
 		CONFIGURATION_520(20),
 		@Deprecated
 		CONFIGURATION_530(25),
 		INVENTORY_520(30),
 		BLOCK_BUG_CLEANUP_540_BETA(35),
 		INVENTORY_540_BETA(40),
 		@Deprecated
 		CONFIGURATION_540_BETA(45),
 		CONFIGURATION_540(50),
 		ITEM_MAP_540(55),
 		FILES_AND_FOLDERS_540(60),
 		INVENTORY_UPDATE_540(65);
 
 		public final int bytePosition;
 
 		private CompatibilityType(int byt){
 			bytePosition = byt;
 		}
 	}
 
 	/**
 	 * File type
 	 * 
 	 * @author turt2live
 	 */
 	public static enum FileType{
 		CONFIGURATION,
 		REGION_CONFIGURATION,
 		WORLD_CONFIGURATION,
 		FINES_REWARDS,
 		LOCALE;
 	}
 
 	private static final String COMPATIBILITY_FILE_NAME = "compat.antishare";
 
 	private static void noLongerNeedsUpdate(CompatibilityType compatType){
 		RandomAccessFile file = getFile();
 		if(file != null){
 			try{
 				file.seek(compatType.bytePosition);
 				file.writeBoolean(false);
 			}catch(IOException e){}
 		}
 	}
 
 	private static boolean needsUpdate(CompatibilityType compat){
 		RandomAccessFile file = getFile();
 		if(file != null){
 			try{
 				file.seek(compat.bytePosition);
 				return file.readBoolean();
 			}catch(IOException e){}
 			return true;
 		}
 		return true;
 	}
 
 	private static RandomAccessFile getFile(){
 		File rfile = new File(AntiShare.p.getDataFolder(), "data" + File.separator + COMPATIBILITY_FILE_NAME);
 		if(!rfile.exists()){
 			try{
 				rfile.createNewFile();
 			}catch(IOException e){}
 		}
 		try{
 			RandomAccessFile file = new RandomAccessFile(rfile, "rw");
 			return file;
 		}catch(FileNotFoundException e){}
 		return null;
 	}
 
 	static List<ASMaterial> updateItemMap(Map<String, ASMaterial> listing) throws IOException{
 		List<ASMaterial> r = new ArrayList<ASMaterial>();
 		if(!needsUpdate(CompatibilityType.ITEM_MAP_540)){
 			return r;
 		}
 		AntiShare p = AntiShare.p;
 		File items = new File(p.getDataFolder(), "items.temp");
 		ItemMap.createFile(items, p);
 		BufferedReader in = new BufferedReader(new FileReader(items));
 		BufferedWriter out = new BufferedWriter(new FileWriter(new File(p.getDataFolder(), "items.csv"), true));
 		boolean updated = false;
 		String line;
		while((line = in.readLine()) != null){
 			if(line.startsWith("#")){
 				continue;
 			}
 			ASMaterial asMaterial = ItemMap.generate(line);
 			if(asMaterial == null){
 				continue;
 			}
 			String name = asMaterial.name.trim().toLowerCase();
 			if(!listing.containsKey(name)){
 				r.add(asMaterial);
 				if(!updated){
 					out.newLine();
 					out.write("# AntiShare v" + p.getDescription().getVersion() + " (Build " + p.getBuild() + ") updates below this line");
 					updated = true;
 				}
 				out.newLine();
 				out.write(line);
 			}
 		}
 		in.close();
 		out.close();
 		items.delete();
 		noLongerNeedsUpdate(CompatibilityType.ITEM_MAP_540);
 		return r;
 	}
 
 	/**
 	 * Cleans the 5.3.0 file structure
 	 */
 	public static void cleanup530FileStructure(){
 		if(!needsUpdate(CompatibilityType.FILES_AND_FOLDERS_540)){
 			return;
 		}
 		AntiShare p = AntiShare.p;
 
 		File[] files = new File[] {
 				new File(p.getDataFolder(), "features.yml"),
 				new File(p.getDataFolder(), "items.yml"),
 				new File(p.getDataFolder(), "messages.yml"),
 				new File(p.getDataFolder(), "notifications.yml"),
 				new File(p.getDataFolder(), "signs.yml"),
 				new File(p.getDataFolder(), "fines.yml")
 		};
 
 		File backupFolder = new File(p.getDataFolder(), "5_3_0_Backup");
 
 		for(File f : files){
 			if(f.exists()){
 				if(!backupFolder.exists()){
 					backupFolder.mkdirs();
 				}
 				f.renameTo(new File(backupFolder, f.getName()));
 			}
 		}
 
 		noLongerNeedsUpdate(CompatibilityType.FILES_AND_FOLDERS_540);
 	}
 
 	/**
 	 * Migrates the world configurations to their own folder
 	 */
 	public static void migrateWorldConfigurations(){
 		if(!needsUpdate(CompatibilityType.WORLD_CONFIGURATION_MIGRATE)){
 			return;
 		}
 		File directory = AntiShare.p.getDataFolder();
 		File newDir = new File(directory, "world_configurations");
 		int files = 0;
 		if(directory.listFiles() != null){
			for(File file : directory.listFiles(new FileFilter() {
 				@Override
 				public boolean accept(File arg0){
 					if(arg0.getName().endsWith("_config.yml")){
 						return true;
 					}
 					return false;
 				}
 			})){
 				files++;
 				file.renameTo(new File(newDir, file.getName()));
 				file.delete();
 			}
 			if(files > 0){
 				AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("world-config-migrate", String.valueOf(files)));
 			}
 		}
 		noLongerNeedsUpdate(CompatibilityType.WORLD_CONFIGURATION_MIGRATE);
 	}
 
 	/**
 	 * Convert 4.4.0 to 4.4.1+ system
 	 */
 	public static void convertBlocks(){
 		if(!needsUpdate(CompatibilityType.BLOCK_CONVERSION)){
 			return;
 		}
 		int converted = 0;
 		AntiShare plugin = AntiShare.p;
 		File dir = new File(plugin.getDataFolder(), "data");
 		dir.mkdirs();
 		File nDir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		nDir.mkdirs();
 		File oldBlockFile = new File(dir, "blocks.yml");
 		if(!oldBlockFile.exists()){
 			return;
 		}
 		EnhancedConfiguration blocks = new EnhancedConfiguration(oldBlockFile, plugin);
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
 			AntiShare.p.getBlockManager().addBlock(gm, block);
 			converted++;
 		}
 		oldBlockFile.delete();
 		if(converted > 0){
 			AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("blocks-converted", String.valueOf(converted)));
 		}
 		noLongerNeedsUpdate(CompatibilityType.BLOCK_CONVERSION);
 	}
 
 	/**
 	 * Migrates player data from region_players to data/region_players
 	 */
 	public static void migratePlayerData(){
 		if(!needsUpdate(CompatibilityType.REGION_PLAYER_DATA_MIGRATE)){
 			return;
 		}
 		AntiShare plugin = AntiShare.p;
 		File newSaveFolder = new File(plugin.getDataFolder(), "data" + File.separator + "region_players");
 		File oldSaveFolder = new File(plugin.getDataFolder(), "region_players");
 		newSaveFolder.mkdirs();
 		if(oldSaveFolder.exists()){
 			File[] files = oldSaveFolder.listFiles();
 			if(files != null && files.length > 0){
 				for(File file : files){
 					file.renameTo(new File(newSaveFolder, file.getName()));
 				}
 				AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("region-info-migrate", String.valueOf(files.length)));
 			}
 			oldSaveFolder.delete();
 		}
 		noLongerNeedsUpdate(CompatibilityType.REGION_PLAYER_DATA_MIGRATE);
 	}
 
 	/**
 	 * Converts 3.1.3 inventories to 3.2.0+ style
 	 */
 	public static void convert313Inventories(){
 		if(!needsUpdate(CompatibilityType.INVENTORY_313)){
 			return;
 		}
 		AntiShare plugin = AntiShare.p;
 		File[] files = new File(plugin.getDataFolder(), "inventories").listFiles();
 		if(files != null){
 			for(File file : files){
 				EnhancedConfiguration inventory = new EnhancedConfiguration(file, plugin);
 				inventory.load();
 				String fname = file.getName();
 				GameMode gamemode;
 				if(fname.replace("_CREATIVE_", "").length() != fname.length()){
 					gamemode = GameMode.CREATIVE;
 				}else if(fname.replace("_SURVIVAL_", "").length() != fname.length()){
 					gamemode = GameMode.SURVIVAL;
 				}else{
 					continue;
 				}
 				fname = fname.replace("_" + gamemode.name() + "_", "ANTI=SHARE"); // Unique character
 				String[] nameparts = fname.split("\\.")[0].split("ANTI=SHARE");
 				if(nameparts.length < 2){
 					continue;
 				}
 				String playerName = nameparts[0];
 				World world = plugin.getServer().getWorld(nameparts[1]);
 				if(world == null){
 					continue;
 				}
 				List<OASI> list = OASI.generateInventory(playerName, InventoryType.PLAYER);
 				if(list.size() > 0){
 					continue;
 				}
 				OASI newi = new OASI(InventoryType.PLAYER, playerName, world, gamemode);
 				for(String key : inventory.getKeys(false)){
 					ItemStack item = inventory.getItemStack(key);
 					int slot = -1;
 					try{
 						slot = Integer.parseInt(key);
 					}catch(NumberFormatException e){
 						continue;
 					}
 					if(slot < 0){
 						continue;
 					}
 					if(item != null && item.getType() != Material.AIR){
 						newi.set(slot, item);
 					}
 				}
 				newi.save();
 			}
 			for(File file : files){
 				file.delete();
 			}
 			if(files.length > 0){
 				AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("inventories-313-converted", String.valueOf(files.length)));
 			}
 			noLongerNeedsUpdate(CompatibilityType.INVENTORY_313);
 		}
 	}
 
 	/**
 	 * Removes/Archives old inventories
 	 */
 	public static void cleanupOldInventories(){
 		AntiShare plugin = AntiShare.p;
 		if(plugin.settings().inventoryCleanupSettings.enabled){
 			File timeFile = new File(plugin.getDataFolder(), "data" + File.separator + "lastCleanup");
 			if(timeFile.exists()){
 				try{
 					BufferedReader in = new BufferedReader(new FileReader(timeFile));
 					String line = in.readLine();
 					int lastMS = Integer.parseInt(line);
 					int hours = 3600000 * 6;
 					if(System.currentTimeMillis() - lastMS < hours){
 						return; // Don't clean
 					}
 					in.close();
 				}catch(IOException e){}catch(NumberFormatException e){}
 			}
 			try{
 				BufferedWriter out = new BufferedWriter(new FileWriter(timeFile, false));
 				out.write(String.valueOf(System.currentTimeMillis()));
 				out.close();
 			}catch(IOException e){}
 			long time = plugin.settings().inventoryCleanupSettings.after;
 			boolean delete = !plugin.settings().inventoryCleanupSettings.archive;
 			File archiveLocation = new File(plugin.getDataFolder(), "archive" + File.separator + "inventories" + File.separator + "players");
 			if(!delete && !archiveLocation.exists()){
 				archiveLocation.mkdirs();
 			}
 			File[] files = new File(plugin.getDataFolder(), "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName()).listFiles();
 			int cleaned = 0;
 			if(files != null){
 				for(File file : files){
 					String player = file.getName().split("\\.")[0];
 					OfflinePlayer p = plugin.getServer().getOfflinePlayer(player);
 					long diff = System.currentTimeMillis() - p.getLastPlayed();
 					long days = diff / (24 * 60 * 60 * 1000);
 					if(days >= time){
 						if(delete){
 							file.delete();
 						}else{
 							file.renameTo(new File(archiveLocation, file.getName()));
 						}
 						cleaned++;
 					}
 				}
 			}
 			if(cleaned > 0){
 				AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("inventories-archived", String.valueOf(cleaned)));
 			}
 		}
 	}
 
 	/**
 	 * Cleans YAML files
 	 */
 	public static void cleanupYAML(){
 		if(!needsUpdate(CompatibilityType.CONFIGURATION_540)){
 			return;
 		}
 		int cleaned = 0;
 		AntiShare plugin = AntiShare.p;
 		Map<String, FileType> files = new HashMap<String, FileType>();
 		files.put("config.yml", FileType.CONFIGURATION);
 		files.put("data" + File.separator + "regions", FileType.REGION_CONFIGURATION);
 		files.put("world_configurations", FileType.WORLD_CONFIGURATION);
 		files.put("fines.yml", FileType.FINES_REWARDS);
 		files.put("locale.yml", FileType.LOCALE);
 		File config = new File(plugin.getDataFolder(), "config.yml");
 		if(config.exists()){
 			File backup = new File(plugin.getDataFolder(), "config-backup.yml");
 			try{
 				BufferedReader reader = new BufferedReader(new FileReader(config));
 				BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
 				String line;
				while((line = reader.readLine()) != null){
 					writer.write(line);
 					writer.newLine();
 				}
 				reader.close();
 				writer.close();
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		for(String name : files.keySet()){
 			File file = new File(plugin.getDataFolder(), name);
 			if(file.isDirectory()){
 				cleaned += cleanFolder(file, files.get(name));
 			}else{
 				cleanFile(file, files.get(name));
 				cleaned++;
 			}
 		}
 		if(cleaned > 0){
 			AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("files-cleaned", String.valueOf(cleaned)));
 		}
 		noLongerNeedsUpdate(CompatibilityType.CONFIGURATION_540);
 	}
 
 	/**
 	 * Relocates 5.2.0 inventories
 	 */
 	public static void cleanup520Inventories(){
 		if(!needsUpdate(CompatibilityType.INVENTORY_520)){
 			return;
 		}
 		File data = AntiShare.p.getDataFolder();
 		File inventoryFile = new File(data, "inventories");
 		File newFolder = new File(data, "data" + File.separator + "inventories");
 		if(inventoryFile.exists()){
 			inventoryFile.renameTo(newFolder);
 		}
 		noLongerNeedsUpdate(CompatibilityType.INVENTORY_520);
 	}
 
 	/**
 	 * Relocates 5.3.0 inventories
 	 */
 	public static void cleanup530Inventories(){
 		if(!needsUpdate(CompatibilityType.INVENTORY_540_BETA)){
 			return;
 		}
 		File data = AntiShare.p.getDataFolder();
 		File inventoryFile = new File(data, "inventories");
 		File newFolder = new File(data, "data" + File.separator + "inventories");
 		if(inventoryFile.exists()){
 			File[] files = inventoryFile.listFiles();
 			ASUtils.wipeFolder(newFolder, null);
 			if(files != null){
 				for(File file : files){
 					file.renameTo(new File(newFolder, file.getName()));
 				}
 			}
 			inventoryFile.delete();
 		}
 		noLongerNeedsUpdate(CompatibilityType.INVENTORY_540_BETA);
 	}
 
 	/**
 	 * Removes dead block files introduced in 5.3.0. "The 0kb bug"
 	 */
 	public static void cleanup520blocks(){
 		if(!needsUpdate(CompatibilityType.BLOCK_BUG_CLEANUP_540_BETA)){
 			return;
 		}
 		long deleted = 0;
 		AntiShare plugin = AntiShare.p;
 		File entitiesDir = new File(plugin.getDataFolder(), "data" + File.separator + "entities");
 		File blocksDir = new File(plugin.getDataFolder(), "data" + File.separator + "blocks");
 		if(entitiesDir.listFiles() != null){
 			for(File file : entitiesDir.listFiles()){
 				if(file.isFile()){
 					if(file.length() < 100){ // 100 bytes
 						file.delete();
 						deleted++;
 					}
 				}
 			}
 		}
 		if(blocksDir.listFiles() != null){
 			for(File file : blocksDir.listFiles()){
 				if(file.isFile()){
 					if(file.length() <= 0){
 						file.delete();
 						deleted++;
 					}
 				}
 			}
 		}
 		if(deleted > 0){
 			AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("bugged-remove", String.valueOf(deleted)));
 		}
 		noLongerNeedsUpdate(CompatibilityType.BLOCK_BUG_CLEANUP_540_BETA);
 	}
 
 	/**
 	 * Converts inventories to 5.4.0 format (JSON)
 	 */
 	public static void convertTo540Inventories(){
 		if(!needsUpdate(CompatibilityType.INVENTORY_UPDATE_540)){
 			return;
 		}
 		int resolved = 0;
 		for(InventoryType type : InventoryType.values()){
 			File[] files = new File(ASInventory.DATA_FOLDER, type.getRelativeFolderName()).listFiles();
 			if(files != null){
 				for(File file : files){
 					if(file.getName().toLowerCase().endsWith(".yml")){
 						List<ASInventory> list = LegacyInventory.load(file.getName().split("\\.")[0], type);
 						for(ASInventory inv : list){
 							inv.save();
 						}
 						file.delete();
 						resolved++;
 					}
 				}
 			}
 		}
 		if(resolved > 0){
 			AntiShare.p.getLogger().info(AntiShare.p.getMessages().getMessage("inventories-to-540-converted", String.valueOf(resolved)));
 		}
 		noLongerNeedsUpdate(CompatibilityType.INVENTORY_UPDATE_540);
 	}
 
 	private static int cleanFolder(File folder, FileType type){
 		int cleaned = 0;
 		if(folder.listFiles() != null){
 			for(File file : folder.listFiles()){
 				if(file.getName().endsWith(".yml")){
 					cleanFile(file, type);
 					cleaned++;
 				}
 			}
 		}
 		return cleaned;
 	}
 
 	/**
 	 * Cleans a file
 	 * 
 	 * @param file the file
 	 * @param type the file type
 	 */
 	public static void cleanFile(File file, FileType type){
 		AntiShare plugin = AntiShare.p;
 		File temp = new File(plugin.getDataFolder(), "temp");
 		temp.mkdirs();
 		EnhancedConfiguration local = new EnhancedConfiguration(new File(temp, "temp1"), plugin);
 		switch (type){
 		case CONFIGURATION:
 			local.loadDefaults(plugin.getResource("config.yml"));
 			break;
 		case REGION_CONFIGURATION:
 			local.loadDefaults(plugin.getResource("region.yml"));
 			break;
 		case WORLD_CONFIGURATION:
 			local.loadDefaults(plugin.getResource("world.yml"));
 			break;
 		case FINES_REWARDS:
 			local.loadDefaults(plugin.getResource("fines.yml"));
 			break;
 		case LOCALE:
 			local.loadDefaults(plugin.getResource("locale.yml"));
 			break;
 		}
 		local.saveDefaults();
 		EnhancedConfiguration actual = new EnhancedConfiguration(file, plugin);
 		actual.load();
 		for(String key : actual.getKeys(true)){
 			if(local.get(key) == null){
 				actual.set(key, null);
 			}
 		}
 		actual.save();
 		ASUtils.wipeFolder(temp, null);
 		temp.delete();
 	}
 
 }
