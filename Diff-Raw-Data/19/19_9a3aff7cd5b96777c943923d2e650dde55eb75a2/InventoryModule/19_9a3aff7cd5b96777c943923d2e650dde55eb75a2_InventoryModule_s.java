 package com.selfequalsthis.grubsplugin.modules;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Set;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.selfequalsthis.grubsplugin.AbstractGrubsModule;
 
 public class InventoryModule extends AbstractGrubsModule {
 	
 	private HashMap<String,ItemStack[]> itemKitPresets = new HashMap<String,ItemStack[]>();
 	private HashMap<String,ItemStack[]> itemKitArmorPresets = new HashMap<String,ItemStack[]>();
 	private String itemKitMainDirectory = "plugins/ItemKitPresets";
 	private File ItemKitPresetFile = new File(itemKitMainDirectory + File.separator + "presets.dat");
 	private Properties itemKitProperties = new Properties(); 
 	
 	public InventoryModule() {
 		this.logPrefix = "[InventoryModule]: ";
 		
 		this.commandNames.add("kitget");
		this.commandNames.add("kitget");
		this.commandNames.add("kitget");
		this.commandNames.add("kitget");
		this.commandNames.add("kitget");
 	}
 	
 	@Override
 	public void enable(JavaPlugin plugin) {
 		super.enable(plugin);
 		
 		this.log.info(logPrefix + "Initializing Item Kit functionality.");
 		File mainDir = new File(itemKitMainDirectory);
 		if (!mainDir.exists()) {
 			this.log.info(logPrefix + "Item Kit save directory doesn't exist. Creating.");
 			mainDir.mkdir();
 		}
 		
 		if(!ItemKitPresetFile.exists()){
 			this.log.info(logPrefix + "Item Kit preset file doesn't exist. Creating.");
 			try {
 				ItemKitPresetFile.createNewFile();
 			} 
 			catch (IOException ex) {
 				this.log.info(logPrefix + "Error creating Item Kit preset file!");
 				ex.printStackTrace();
 			}
 		}
 		
 		this.log.info(logPrefix + "Loading Item Kit presets.");
 		loadItemKits();
 		this.log.info(logPrefix + "Loaded " + itemKitPresets.size() + " presets.");
 	}
 
 	@Override
 	public void disable() {
 		this.log.info(logPrefix + "Saving Item Kit presets.");
 		saveItemKits();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		
 		String cmdName = command.getName();
 		Player executingPlayer = (Player) sender;
 		
 		if (!executingPlayer.isOp()) {
 			return false;
 		}
 		
 		if (cmdName.equalsIgnoreCase("kitset")) {
 			if (args.length > 0) {
 				String argName = args[0];
 				if (itemKitPresets.containsKey(argName)) {
 					executingPlayer.sendMessage(ChatColor.RED + "[Items] Kit '" + argName + "' already exists.");
 					return true;
 				}
 				else {
 					itemKitPresets.put(argName, executingPlayer.getInventory().getContents());
 					itemKitArmorPresets.put(argName, executingPlayer.getInventory().getArmorContents());
 					executingPlayer.sendMessage(ChatColor.GREEN + "[Items] Kit '" + argName + "' saved.");
 					saveItemKits();
 					return true;
 				}
 			}
 			else {
 				executingPlayer.sendMessage(ChatColor.RED + "[Items] Missing command argument.");
 				return false;
 			}
 		}
 		else if (cmdName.equalsIgnoreCase("kitget")) {
 			if (args.length > 0) {
 				String argName = args[0];
 				if (itemKitPresets.containsKey(argName)) {
 					executingPlayer.getInventory().clear();
 					executingPlayer.getInventory().setContents(itemKitPresets.get(argName));
 					executingPlayer.getInventory().setArmorContents(itemKitArmorPresets.get(argName));
 					executingPlayer.sendMessage(ChatColor.GREEN + "[Items] Inventory updated.");
 					return true;
 				}
 				else {
 					executingPlayer.sendMessage(ChatColor.RED + "[Items] No kit named '" + argName + "' found.");
 					return true;
 				}
 			}
 			else {
 				executingPlayer.sendMessage(ChatColor.RED + "[Items] Missing command argument.");
 				return false;
 			}
 		}
 		else if (cmdName.equalsIgnoreCase("kitdel")) {
 			if (args.length > 0) {
 				String argName = args[0];
 				if (itemKitPresets.containsKey(argName)) {
 					itemKitPresets.remove(argName);
 					itemKitArmorPresets.remove(argName);
 					executingPlayer.sendMessage(ChatColor.GREEN + "[Items] Kit '" + argName + "' deleted.");
 					saveItemKits();
 					return true;
 				}
 				else {
 					executingPlayer.sendMessage(ChatColor.RED + "[Items] No kit named '" + argName + "' found.");
 					return true;
 				}
 			}
 			else {
 				executingPlayer.sendMessage(ChatColor.RED + "[Items] Missing command argument.");
 				return false;
 			}
 		}
 		else if (cmdName.equalsIgnoreCase("kitlist")) {
 			boolean useSeparator = false;
 			String msgIdentifier = "[Items] ";
 			String list = "";
 			Set<String> keys = itemKitPresets.keySet();
 
 			if (keys.size() > 0) {
 				for (String s : keys) {
 					if ( (msgIdentifier.length() + list.length() + 2 + s.length()) > 60) {
 						executingPlayer.sendMessage(ChatColor.GOLD + msgIdentifier + list);
 						list = "";
 						useSeparator = false;
 					}
 					
 					if (useSeparator) {
 						list += ", ";
 					}
 					else {
 						useSeparator = true;
 					}
 					
 					
 					list += s;	
 				}
 
 				executingPlayer.sendMessage(ChatColor.GOLD + msgIdentifier + list);
 			}
 			else {
 				executingPlayer.sendMessage(ChatColor.RED + msgIdentifier + "No kits in list.");
 			}
 			return true;
 		}
 		else if (cmdName.equalsIgnoreCase("clearinv")) {
 			executingPlayer.getInventory().clear();
 			executingPlayer.getInventory().setArmorContents(new ItemStack[4]);
 			executingPlayer.sendMessage(ChatColor.GREEN + "[Items] Inventory cleared.");
 			return true;
 		}
 		
 		return false;
 	}
 
 	private void loadItemKits() {
 		try {
 			FileInputStream in = new FileInputStream(ItemKitPresetFile);
 			itemKitProperties.load(in);
 			in.close();
 			
 			for (Object key : itemKitProperties.keySet()) {
 				String realKey = (String) key;
 				String rawValue = itemKitProperties.getProperty(realKey);
 				
 				ItemStack[] kitItems = new ItemStack[36];
 				ItemStack[] armorItems = new ItemStack[4];
 
 				String[] parts = rawValue.split("\\^");
 
 				// set up inventory part
 				if (parts[0].length() > 0) {
 					String[] invParts = parts[0].split(",");
 					for (int i=0; i < invParts.length; ++i) {
 						String[] curInvTuple = invParts[i].split(":");
 						
 						// if spot was empty, it won't be listed
 						int index = Integer.parseInt(curInvTuple[0]);
 						int materialId = Integer.parseInt(curInvTuple[1]);
 						int amt = Integer.parseInt(curInvTuple[2]);
 						
 						kitItems[index] = new ItemStack(materialId, amt);
 					}
 				}
 				
 				if (parts[1].length() > 0) {					
 					String[] armorParts = parts[1].split(",");
 					for (int i=0; i < armorParts.length; ++i) {
 						String[] curArmorTuple = armorParts[i].split(":");
 						
 						int index = Integer.parseInt(curArmorTuple[0]);
 						int materialId = Integer.parseInt(curArmorTuple[1]);
 						int amt = Integer.parseInt(curArmorTuple[2]);
 						
 						armorItems[index] = new ItemStack(materialId, amt);
 					}
 				}
 				
 				itemKitPresets.put(realKey, kitItems);
 				itemKitArmorPresets.put(realKey, armorItems);
 			}
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private void saveItemKits() {
 		String settingStr = "";
 		boolean useSeparator = false;
 				
 		itemKitProperties.clear();
 		
 		for (String s : itemKitPresets.keySet()) {
 			ItemStack[] curStackList = itemKitPresets.get(s);
 			
 			settingStr = "";
 			useSeparator = false;
 			
 			for (int i=0; i < curStackList.length; ++i) {
 				ItemStack curStackItem = curStackList[i];
 
 				if (curStackItem == null) {
 					continue;
 				}
 				
 				if (useSeparator) {
 					settingStr += ",";
 				}
 				else {
 					useSeparator = true;
 				}
 				
 				settingStr += "" + i + ":" + curStackItem.getTypeId() + ":" + curStackItem.getAmount();
 			}
 			
 			settingStr += "^";
 			useSeparator = false;
 			
 			ItemStack[] curArmorStackList = itemKitArmorPresets.get(s);
 			
 			for (int i=0; i < curArmorStackList.length; ++ i) {
 				ItemStack curArmorStackItem = curArmorStackList[i];
 				
 				if (curArmorStackItem == null) {
 					continue;
 				}
 				
 				if (useSeparator) {
 					settingStr += ",";
 				}
 				else {
 					useSeparator = true;
 				}
 				
 				settingStr += "" + i + ":" + curArmorStackItem.getTypeId() + ":" + curArmorStackItem.getAmount();
 			}
 			
 			itemKitProperties.put(s, settingStr);
 		}
 		
 		this.log.info(logPrefix + "Writing Item Kit presets file.");
 		try {
 			FileOutputStream out = new FileOutputStream(ItemKitPresetFile);
 			itemKitProperties.store(out, "Do NOT edit this file manually!");
 			out.flush();
 			out.close();
 		}
 		catch (IOException ex) {
 			this.log.info(logPrefix + "Error writing Item Kit presets file!");
 			ex.printStackTrace();
 		}
 	}
 }
