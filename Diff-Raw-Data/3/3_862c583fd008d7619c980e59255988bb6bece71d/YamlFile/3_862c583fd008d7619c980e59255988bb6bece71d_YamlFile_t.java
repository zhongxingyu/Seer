 package net.worldoftomorrow.noitem.lists;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import net.worldoftomorrow.noitem.NoItem;
 
 public abstract class YamlFile {
 
 	private final File file;
 	private final YamlConfiguration resource;
 	private final YamlConfiguration yamlfile;
 	private final NoItem plugin;
 
 	public YamlFile(String dir, String name) {
 		this.plugin = NoItem.getInstance();
 		this.file = new File(plugin.getDataFolder() + File.separator + dir, name);
 		this.resource = YamlConfiguration.loadConfiguration(NoItem.getInstance().getResource(name));
 		this.yamlfile = load();
 	}
 
 	private YamlConfiguration load() {
 		try {
 			// If the file does not exist, create it. Return null if it fails
			if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
				plugin.getLogger().severe("Could not create parent directory.");
			}
 			if (!file.exists() && !file.createNewFile()) {
 				plugin.getLogger().severe("Could not create config file: " + file.getName());
 				return null;
 			} else {
 				// Get the config file from the plugin folder
 				// This will be assigned to yamlfile;
 				YamlConfiguration confile = YamlConfiguration.loadConfiguration(file);
 				// Get the values of the resource file
 				Map<String, Object> vals = resource.getValues(true);
 				String key;
 				// If the destination file does not have the entry
 				// That the resource file has, add it. Otherwise
 				// Leave its value the same
 				for(Entry<String, Object> entry : vals.entrySet()) {
 					key = entry.getKey();
 					if(!confile.isSet(key)) {
 						confile.set(key, entry.getValue());
 					}
 				}
 				// Write the config file to the disk
 				PrintWriter o = new PrintWriter(file, "UTF-8");
 				o.write(confile.saveToString());
 				o.close();
 				return confile;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public YamlConfiguration getConfig() {
 		return this.yamlfile;
 	}
 	
 	public YamlConfiguration getDefaultConfig() {
 		return this.resource;
 	}
 	
 	/**
 	 * Get an object from the config file
 	 * The boolean is whether to return the default
 	 * value if the real value is not set; defaults
 	 * to true.
 	 * @param key
 	 * @param def
 	 * @return Object from the config
 	 */
 	public Object getObject(String key, boolean def) {
 		if(!this.getConfig().isSet(key) && def) {
 			return this.getDefaultConfig().get(key);
 		} else {
 			return this.getConfig().get(key);
 		}
 	}
 	
 	public Object getObject(String key) {
 		return getObject(key, true);
 	}
 }
