 package nl.giantit.minecraft.GiantShop.core.Items;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.Material;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  *
  * @author Giant
  */
 public class Items {
 	
 	private GiantShop plugin;
 	private YamlConfiguration config;
	private double itemVersion = 0.5;
 	private HashMap<ItemID, String> itemsByID = new HashMap<ItemID, String>();
 	private HashMap<String, ItemID> itemsByName = new HashMap<String, ItemID>();
 	private HashMap<ItemID, List<String>> itemTypes = new HashMap<ItemID, List<String>>();
 	private HashMap<String, ItemID> itemsAliases = new HashMap<String, ItemID>();
 	
 	private List<Integer> getTypeList(int id) {
 		List<Integer> types = new ArrayList<Integer>();
 		for(Map.Entry<ItemID, List<String>> entry : itemTypes.entrySet()) {
 			ItemID key = entry.getKey();
 			if(key.getId() == id)
 				types.add(key.getType());
 		}
 		
 		return types;
 	}
 	
 	public Items(GiantShop plugin) {
 		this.plugin = plugin;
 		File configFile = new File(plugin.getDir(), "items.yml");
 		if(!configFile.exists()) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] Extracting new items.yml file...");
 			plugin.extract("items.yml");
 		}
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		double v = config.getDouble("version");
 		if(v < this.itemVersion) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] Your items.yml has ran out of date. Updating now!");
 			File oconfigFile = new File(plugin.getDir(), "items.yml." + v + ".bak");
 			configFile.renameTo(oconfigFile);
 			plugin.extract("items.yml");
 			config = YamlConfiguration.loadConfiguration(configFile);
 		}
 		
 		loadNames();
 		loadTypes();
 		loadAliases();
 	}
 	
 	public final void loadNames() {
 		Set<String> list = config.getConfigurationSection("names").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] There are no item names specified in the items.yml file?!");
 			return;
 		}
 		
 		for(String item : list) {
 			int id;
 			try {
 				id = Integer.valueOf(item.substring(4));
 			} catch(NumberFormatException x) {
 				plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] Invalid key detected in items.yml (names." + item + ")");
 				continue;
 			}
 			
 			
 			String name = config.getString("names." + item);
 			String namePrep = name.toLowerCase().replaceAll(" ", "_");
 			ItemID key = new ItemID(id, null);
 			itemsByID.put(key, name);
 			itemsByName.put(namePrep, key);
 		}
 	}
 	
 	public final void loadTypes() {
 		Set<String> list = config.getConfigurationSection("types").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] There are no item types specified in the items.yml file?!");
 			return;
 		}
 		
 		for(String item : list) {
 			int itemID;
 			
 			try {
 				itemID = Integer.valueOf(item.substring(4));
 			} catch(NumberFormatException x) {
 				plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] Invalid key detected in items.yml (types." + item + ")");
 				continue;
 			}
 			
 			Set<String> types = config.getConfigurationSection("types." + item).getKeys(false);
 			for(String type : types) {
 				int typeID;
 				try {
 					typeID = Integer.valueOf(type.substring(4));
 				} catch(NumberFormatException x) {
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] Invalid key detected in items.yml (types." + item + "." + type + ")");
 					continue;
 				}
 				
 				List<String> typeNames = config.getStringList("types." + item + "." + type);
 				if(typeNames.size() > 0) {
 					ItemID el = new ItemID(itemID, typeID);
 					String name = typeNames.get(0).toLowerCase().replaceAll(" ", "_");
 					this.itemTypes.put(el, typeNames);
 					this.itemsByID.put(el, typeNames.get(0));
 					this.itemsByName.put(name, el);
 					
 					for(int i = 1; i < typeNames.size(); i++) {
 						itemsAliases.put(typeNames.get(i).toLowerCase().replaceAll(" ", "_"), el);
 					}
 				}else{
 					plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] Given type does not have any names. (types." + item + "." + type + ")");
 				}
 			}
 		}
 	}
 	
 	public final void loadAliases() {
 		Set<String> list = config.getConfigurationSection("aliases").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] There are no item aliases specified in the items.yml file?!");
 		}
 		
 		for(String item : list) {
 			String itemIDString = config.getString("aliases." + item);
 			int itemID;
 			Integer dataType;
 			try{
 				if(itemIDString.matches("[0-9]+/[0-9]+")) {
 					String[] splitted = itemIDString.split("/");
 					itemID = Integer.parseInt(splitted[0]);
 					dataType = Integer.parseInt(splitted[0]);
 				}else{
 					itemID = Integer.parseInt(itemIDString);
 					dataType = null;
 				}
 				
 				ItemID id;
 				if(this.isValidItem(itemID, dataType)) {
 					id = new ItemID(itemID, dataType, item);
 				}else
 					id = new ItemID(itemID, null, item);
 				
 				String name = item.toLowerCase().replaceAll(" ", "_");
 				itemsAliases.put(name, id);
 			}catch(NumberFormatException e) {
 				plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] Invalid item id (" + itemIDString + ") detected for alias " + item + " in items.yml");
 				continue;
 			}
 		}
 	}
 	
 	public ItemID getItemIDByName(String item) {
 		String name = item.toLowerCase().replaceAll(" ", "_");
 		if(this.itemsByName.containsKey(name))
 			return this.itemsByName.get(name);
 		
 		if(this.itemsAliases.containsKey(name))
 			return this.itemsAliases.get(name);
 		
 		return null;
 	}
 	
 	public String getItemNameByID(int itemID) {
 		return this.getItemNameByID(itemID, null);
 	}
 	
 	public String getItemNameByID(int itemID, Integer itemType) {
 		ItemID key = new ItemID(itemID, itemType);
 		
 		for(Map.Entry<ItemID, String> entry : itemsByID.entrySet()) {
 			ItemID item = entry.getKey();
 			if(item.equals(key))
 				return entry.getValue();
 		}
 		
 		return null;
 	}
 	
 	public boolean isValidItem(int itemID) {
 		return isValidItem(itemID, null);
 	}
 	
 	public boolean isValidItem(int itemID, Integer itemType) {
 		if(itemID < 0 || (itemType != null && itemType < 0))
 			return false;
 		
 		Material mat = Material.getMaterial(itemID);
 		if(mat != null) {
 			if(itemType == null)
 				return true;
 			
 			List<Integer> types = this.getTypeList(itemID);
 			if(types.contains(itemType))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public Integer getMaxStackSize(int id) {
 		return Material.getMaterial(id).getMaxStackSize();
 	}
 }
