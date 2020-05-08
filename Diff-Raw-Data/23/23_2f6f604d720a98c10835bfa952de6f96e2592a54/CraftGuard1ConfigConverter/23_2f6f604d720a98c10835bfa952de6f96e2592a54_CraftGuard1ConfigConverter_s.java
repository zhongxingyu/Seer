 package fr.frozentux.craftguard2.config.compat;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 
 /**
  * Class that converts configuration file from CraftGuard 1.x format to CraftGuard 2.x format
  * @author FrozenTux
  *
  */
 public class CraftGuard1ConfigConverter {
 	
 	private CraftGuardPlugin plugin;
 	
 	private FileConfiguration config, list;
 	private File configFile, listFile;
 	
 	/**
 	 * Class that converts configuration file from CraftGuard 1.x format to CraftGuard 2.x format
 	 * @param config		FileConfiguration instance of the plugin's config file (containing the old data structure)
 	 * @param configFile	File pointing at the configuration file
 	 * @param list			FileConfiguration instance of the craftguard's list file
 	 * @param listFile		File pointing at the list file
 	 * @param plugin		Plugin using this converter
 	 */
 	public CraftGuard1ConfigConverter(CraftGuardPlugin plugin){
 		this.listFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "lists.yml");
 		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
 		this.config = new YamlConfiguration();
 		this.list = new YamlConfiguration();
 		try {
 			list.load(listFile);
 			config.load(configFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * Converts the configuration file
 	 */
 	public void convert(){
 		
 		Iterator<String> listIt = config.getConfigurationSection("craftguard").getKeys(false).iterator();
 		int listCount = 0;
 		
 		while(listIt.hasNext()){
 			String name = listIt.next();
			String permission = config.getString("craftguard." + name + ".permision");
 			String parent = config.getString("craftguard." + name + ".inheritance");
 			List<String> rawList = config.getStringList("craftguard." + name + ".granted");
 			
 			list.set(name + ".list", rawList);
 			if(permission != null)list.set(name + ".permission", permission);
 			if(parent != null)list.set(name + ".parent", parent);
 			listCount++;
 		}
 		
 		Iterator<String> configIt = config.getConfigurationSection("config").getKeys(false).iterator();
 		HashMap<String, Object> fields = new HashMap<String, Object>();
 		
 		while(configIt.hasNext()){
 			String key = configIt.next();
 			fields.put(key, config.get(key));
 		}
 		
 		configFile.delete();
 		try {
 			config.load(configFile);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		
 		configIt = fields.keySet().iterator();
 		
 		while(configIt.hasNext()){
 			String key = configIt.next();
 			config.set(key, fields.get(key));
 		}
 		
 		try {
 			config.save(configFile);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			list.save(listFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		plugin.getCraftGuardLogger().info("Succesfully converted " + listCount + "lists into lists.yml !");
 	}
 }
