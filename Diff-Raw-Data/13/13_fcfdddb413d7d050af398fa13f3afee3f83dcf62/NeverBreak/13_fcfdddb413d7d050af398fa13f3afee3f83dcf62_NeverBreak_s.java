 package com.github.thebiologist13;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NeverBreak extends JavaPlugin {
 	
 	//YAML variable
 	private FileConfiguration config;
 	
 	//YAML file variable
 	private File configFile;
 	
 	//Toggling command executor variable
 	private ToggleCommand tc;
 	
 	//Set durability command executor variable
 	private DurabilityCommand dc;
 	
 	//Logger
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public void onEnable() {
 		
 		//Config
 		config = this.getCustomConfig();
 		
 		//Listeners
 		getServer().getPluginManager().registerEvents(new BreakListener(this), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
 		getServer().getPluginManager().registerEvents(new FishListener(this), this);
 		getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
 		getServer().getPluginManager().registerEvents(new BowFireListener(this), this);
 		getServer().getPluginManager().registerEvents(new FireStartListener(this), this);
 		getServer().getPluginManager().registerEvents(new BlockDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new ShearListener(this), this);
 		getServer().getPluginManager().registerEvents(new FarmListener(this), this);
 		
 		//Toggle command setup
 		tc = new ToggleCommand(this);
 		getCommand("neverbreak").setExecutor(tc);
 		
 		//Durability command setup
 		dc = new DurabilityCommand(this);
 		getCommand("setdurability").setExecutor(dc);
 		
 		//Enable message
 		log.info("NeverBreak by thebiologist13 has been enabled!");
 	}
 	
 	public void onDisable() {
 		//Disable message
 		log.info("NeverBreak by thebiologist13 has been disabled!");
 	}
 	
 	//Config stuff
 	//Credit goes to respective owners.
 	public void reloadCustomConfig() {
 		if (configFile == null) {
 		    configFile = new File(getDataFolder(), "config.yml");
 		    
 		    if(!configFile.exists()){
 		        configFile.getParentFile().mkdirs();
 		        copy(getResource("config.yml"), configFile);
 		    }
 		}
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		 
 		// Look for defaults in the jar
 		InputStream defConfigStream = this.getResource("config.yml");
 		if (defConfigStream != null) {
 		    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 		    config.setDefaults(defConfig);
 		}
 	}
 	
 	public FileConfiguration getCustomConfig() {
 		if (config == null) {
 	        reloadCustomConfig();
 	    }
 	    return config;
 	}
 	
 	public void saveCustomConfig() {
 		if (config == null || configFile == null) {
 		    return;
 		}
 		try {
 			config.save(configFile);
 		} catch (IOException ex) {
 			log.severe("Could not save config to " + configFile.getPath());
 		}
 	}
 	
 	public void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			log.severe("Could not copy config from jar!");
 			e.printStackTrace();
 		}
 	}
 	
 	public void resetDurability(ItemStack stack, Player p) {
 		List<?> items = config.getList("items");
 		//Loop for all items from config
 		for(Object o : items) {
 			//Make sure that it is specifying data IDs 
 			if(o instanceof Integer) {
 				//If item in hand matches one from config
 				if(stack.getTypeId() == (Integer) o) {
 					//If a mode has been set for the player
 					if(ToggleCommand.mode.containsKey(p)) {
 						//If that mode is true
 						if(ToggleCommand.mode.get(p) == true) {
 							//Set the item to -128 durability
 							stack.setDurability((short) -128);
 						//If that mode is false, proceed as normal 
 						} else {
 							//Unless it was set to REALLY unused, then make the durability 0 again
 							if(stack.getDurability() < 0 ) {
 								stack.setDurability((short) 0);
 							}
 						}
 					} 
 				}
 			//Continue if not a data ID
 			} else {
 				continue;
 			}
 		}
 	}
 }
