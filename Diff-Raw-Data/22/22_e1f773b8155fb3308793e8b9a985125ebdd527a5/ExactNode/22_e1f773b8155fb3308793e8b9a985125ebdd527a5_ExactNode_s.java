 package com.github.coaster3000.exactnode;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.coaster3000.exactnode.listeners.BlockListener;
 import com.github.coaster3000.exactnode.listeners.ItemListener;
 import com.github.coaster3000.exactnode.listeners.PlayerListener;
 
 public class ExactNode extends JavaPlugin {
 
 	protected PlayerInformer informer;
 	protected List<ExactNodeListener> listeners = new ArrayList<ExactNodeListener>();
 	@SuppressWarnings("unchecked")
 	protected final static Class<? extends ExactNodeListener>[] LISTENERS = new Class[] { ItemListener.class, BlockListener.class, PlayerListener.class };
 	protected FileConfiguration config;
 	protected File configFile;
 
 	@Override
 	public void onLoad() {
 		configFile = new File(this.getDataFolder(), "config.yml");
 	}
 
 	@Override
 	public void onEnable() {
 		this.config = this.getConfig();
 
 		if (!config.isConfigurationSection("messages")) {
 			this.getLogger().info("Deploying default config");
 			this.initializeConfiguration(config);
 		}
 
 		this.saveConfig();
 		this.informer = new PlayerInformer();
 		PlayerInformer.NO_PERM = config.getString("messages.permissions.denied", "&4You do not have permission");
 		PlayerInformer.EMPTY = config.getString("messages.permissions.empty", "for that!");
 		PlayerInformer.FULL = config.getString("messages.permissios.full", "&e%1$s %2$s");

 		registerListeners();
 		getLogger().info("Registered Listeners");
 	}
 
 	@Override
 	public void onDisable() {
 		this.listeners.clear();
 		this.config = null;
 
 	}
 
 	@Override
 	public void saveConfig() {
 		try {
 			this.config.save(configFile);
 		} catch (IOException e) {
 			this.getLogger().severe("Failed to save configuration file: " + e.getMessage());
 		}
 	}
 
 	protected void initializeConfiguration(FileConfiguration config) {
 		// Flags
 		config.set("checks.metadata", true);
 		config.set("settings.use.material-names", true);
 		config.set("settings.notify-player", true);
 		config.set("settings.use.custom-wildcard-node", false);
 		config.set("settings.use.op-perms", true);
 
 		//Methods for inventory permissions.
 		config.set("methods.item-permissions", "click");
 		config.set("methods.item-fullscan", false);
 
 		//Checks
 		config.set("checks.items.pickup", true);
 		config.set("checks.items.drop", true);
 		config.set("checks.items.have", true);
 		config.set("checks.items.use", true);
 
 		// Messages
		PlayerInformer.NO_PERM = config.getString("messages.permissions.denied", "&4You do not have permission");
		PlayerInformer.EMPTY = config.getString("messages.permissions.empty", "for that!");
		PlayerInformer.FULL = config.getString("messages.permissios.full", "&e%1$s %2$s");

 		// Predefined messages
 	}
 
 	@Override
 	public FileConfiguration getConfig() {
 		if (this.config == null) {
 			this.reloadConfig();
 		}
 
 		return this.config;
 	}
 
 	protected void registerListeners() {
 		for (Class<? extends ExactNodeListener> listenerClass : LISTENERS) {
 			try {
 				Constructor<? extends ExactNodeListener> constructor = listenerClass.getConstructor(Plugin.class, ConfigurationSection.class);
 				ExactNodeListener listener = (ExactNodeListener) constructor.newInstance(this, this.getConfig());
 				this.listeners.add(listener);
 			} catch (Throwable e) {
 				this.getLogger().warning("Failed to initialize \"" + listenerClass.getName() + "\" listener");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void reloadConfig() {
 		this.config = new YamlConfiguration();
 		config.options().pathSeparator('.');
 
 		try {
 			config.load(configFile);
 		} catch (FileNotFoundException e) {
 			this.getLogger().severe("Configuration file not found - deploying default one");
 			InputStream defConfigStream = getResource("config.yml");
 			if (defConfigStream != null) {
 				try {
 					this.config.load(defConfigStream);
 				} catch (Exception de) {
 					this.getLogger().severe("Default config file is broken. Please tell this to ExactNode author.");
 				}
 			}
 		} catch (Exception e) {
 			this.getLogger().severe("Failed to load configuration file: " + e.getMessage());
 		}
 
 		InputStream defConfigStream = getResource("config.yml");
 		if (defConfigStream != null) {
 			this.config.setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
 		}
 
 	}
 }
