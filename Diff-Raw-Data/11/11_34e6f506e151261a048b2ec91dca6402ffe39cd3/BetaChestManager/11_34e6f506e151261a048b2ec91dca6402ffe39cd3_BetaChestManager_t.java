 package com.bettercraft.betachest;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.Plugin;
 
 import net.minecraft.server.InventoryLargeChest;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.TileEntityChest;
 
 public class BetaChestManager
 {
 	private BetaChestPlugin plugin;
 	private final HashMap<String, InventoryLargeChest> chests;
 	private final File dataFolder;
 	
 	public BetaChestManager(Plugin plugin)
 	{
 		this.plugin = (BetaChestPlugin) plugin;
		this.dataFolder = new File(plugin.getDataFolder(),"chests/");
 		this.chests = new HashMap();
 	}
 
 	public InventoryLargeChest getChest(String playerName) {
 		InventoryLargeChest chest = (InventoryLargeChest)this.chests.get(playerName.toLowerCase());
 
 		if (chest == null) {
 			chest = addChest(playerName);
 		}
 		return chest;
 	}
 
 	private InventoryLargeChest addChest(String playerName) {
 		InventoryLargeChest chest = new InventoryLargeChest("Premium Chest", new TileEntityChest(), new TileEntityChest());
 		this.chests.put(playerName.toLowerCase(), chest);
 		return chest;
 	}
 
 	public void removeChest(String playerName) {
 		this.chests.remove(playerName.toLowerCase());
 	}
 
 	public void load() {
 		this.chests.clear();
 
 		int loadedChests = 0;
 
 		this.dataFolder.mkdirs();
 		FilenameFilter filter = new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".chest");
 			}
 		};
 		for (File chestFile : this.dataFolder.listFiles(filter)) {
 			try {
 				InventoryLargeChest chest = new InventoryLargeChest("Premium Chest", new TileEntityChest(), new TileEntityChest());
 				String playerName = chestFile.getName().substring(0, chestFile.getName().length() - 6);
 
 				BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
 				int field = 0;
 				String line;
 				while ((line = in.readLine()) != null)
 				{
 					
 					if (line != "") {
 						String[] parts = line.split(":");
 						try {
 							int type = Integer.parseInt(parts[0]);
 							int amount = Integer.parseInt(parts[1]);
 							short damage = Short.parseShort(parts[2]);
 							if (type != 0)
 								chest.setItem(field, new ItemStack(type, amount, damage));
 						}
 						catch (NumberFormatException localNumberFormatException)
 						{
 						}
 						field++;
 					}
 				}
 
 				in.close();
 				this.chests.put(playerName.toLowerCase(), chest);
 
 				loadedChests++;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		plugin.log("Loaded " + loadedChests + " chests");
 	}
 	
 	// TODO: This is a temporary mess! Cleanup the save code so this functionality is integrated.
 	public boolean savePlayer(String playerName) {
 
 		this.dataFolder.mkdirs();
 		
 		//TODO: Throw an exception? Log the error?
 		if ( this.chests.get(playerName)==null) return false;
 		
 		InventoryLargeChest chest = (InventoryLargeChest)this.chests.get(playerName);
 		try
 		{
 			File chestFile = new File(this.dataFolder, playerName + ".chest");
 			if (chestFile.exists()) chestFile.delete();
 			chestFile.createNewFile();
 
 			BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
 
 			for (ItemStack stack : chest.getContents()) {
 				if (stack != null)
 					out.write(stack.id + ":" + stack.count + ":" + stack.getData() + "\r\n");
 				else {
 					out.write("0:0:0\r\n");
 				}
 			}
 			out.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 
 	public void save(Boolean isAuto) {
 		int savedChests = 0;
 
 		for (String playerName : this.chests.keySet()) {
 			if (savePlayer(playerName)) savedChests++;
 		}
 		
 		if (isAuto) {
 			plugin.log("Saved " + savedChests + " chests");
 		} else {
 			plugin.log("Auto-saved " + savedChests + " chests");
 		}
 	}
 }
