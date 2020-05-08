 package net.worldoftomorrow.noitem;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.Map.Entry;
 
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Config {
 	
 	private final File config;
 	private final YamlConfiguration messages;
 	private final YamlConfiguration notify;
 	private final YamlConfiguration misc;
 	private final NoItem plugin;
 	
 	public Config() {
 		this.plugin = NoItem.getInstance();
 		this.messages = YamlConfiguration.loadConfiguration(plugin.getResource("messages.yml"));
 		this.notify = YamlConfiguration.loadConfiguration(plugin.getResource("notify.yml"));
 		this.misc = YamlConfiguration.loadConfiguration(plugin.getResource("misc.yml"));
 		this.config = new File(plugin.getDataFolder(), "config.yml");
 		this.load();
 	}
 	
 	private void load() {
 		try {
 			if(config.exists()) {
 				this.setUserValues();
 				this.writeConfig();
 			} else {
 				this.createConfig();
 				this.writeConfig();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void writeConfig() throws IOException {
 		// Use a short-circuit operator here so that both methods are always run
		if(!config.delete() && !config.createNewFile()) {
 			plugin.getLogger().severe("Could not create configuration file! - 002");
 		}
		PrintWriter o = new PrintWriter(config, "UTF-8");
 		o.write(messages.saveToString());
 		o.write(notify.saveToString());
 		o.write(misc.saveToString());
 		o.close();
 	}
 	
 	private void setUserValues() {
 		YamlConfiguration original = YamlConfiguration.loadConfiguration(config);
 		Map<String, Object> vals = original.getValues(true);
 		String key;
 		for(Entry<String, Object> entry : vals.entrySet()) {
 			key = entry.getKey();
 			// Update the individual sections values
 			if(key.startsWith("Messages") && messages.isSet(key)) {
 				messages.set(key, entry.getValue());
 			} else if (key.startsWith("Notify") && notify.isSet(key)) {
 				notify.set(key, entry.getValue());
 			} else if(misc.isSet(key)){
 				this.misc.set(key, entry.getValue());
 			}
 		}
 	}
 	
 	private void createConfig() throws IOException {
 		if(!config.getParentFile().exists() && !config.getParentFile().mkdir()) {
 			plugin.getLogger().severe("Could not create config directory");
 		}
 		if(!config.exists() && !config.createNewFile()) {
 			plugin.getLogger().severe("Could not create new config file! - 001");
 		}
 	}
 
 	public static Object getValue(String key) {
 		return NoItem.getInstance().getConfig().get(key);
 	}
 
 	public static boolean getBoolean(String key) {
 		return NoItem.getInstance().getConfig().getBoolean(key);
 	}
 
 	public static String getString(String key) {
 		return NoItem.getInstance().getConfig().getString(key);
 	}
 
 	public static Map<String, Object> getValues() {
 		return NoItem.getInstance().getConfig().getValues(true);
 	}
 }
