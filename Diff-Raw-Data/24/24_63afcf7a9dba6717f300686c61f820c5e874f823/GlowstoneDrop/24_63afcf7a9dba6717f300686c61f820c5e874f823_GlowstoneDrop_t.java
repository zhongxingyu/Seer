 package de.xghostkillerx.glowstonedrop;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.Player;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit Handles some general stuff!
  * Refer to the forum thread:
  * http://bit.ly/oW6iR1
  * Refer to the dev.bukkit.org page: http://bit.ly/rcN2QB
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to XxFuNxX for the original GlowstoneDrop plugin!
  */
 
 public class GlowstoneDrop extends JavaPlugin {
 
 	public final Logger log = Logger.getLogger("Minecraft");
 	private final GlowstoneDropBlockListener blockListener = new GlowstoneDropBlockListener(this);
 	public FileConfiguration config;
 	public FileConfiguration localization;
 	public File configFile;
 	public File localizationFile;
 	public List<String> itemList = new ArrayList<String>();
 	private String[] items = {"WOOD_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLD_PICKAXE", "DIAMOND_PICKAXE"};
 	private GlowstoneDropCommands executor;
 
 	// Shutdown
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion()	+ " has been disabled!");
 	}
 
 	// Start
 	public void onEnable() {
 		// Events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(blockListener, this);
 
 		// Config
 		configFile = new File(getDataFolder(), "config.yml");
 		if (!configFile.exists()) {
 			configFile.getParentFile().mkdirs();
 			copy(getResource("config.yml"), configFile);
 		}
 		config = this.getConfig();
 		loadConfig();
 
 		// Localization
 		localizationFile = new File(getDataFolder(), "localization.yml");
 		if(!localizationFile.exists()){
 			localizationFile.getParentFile().mkdirs();
 			copy(getResource("localization.yml"), localizationFile);
 		}
 		// Try to load
 		try {
 			localization = YamlConfiguration.loadConfiguration(localizationFile);
 			loadLocalization();
 		}
 		// If it failed, tell it
 		catch (Exception e) {
 			log.warning("GlowstoneDrop failed to load the localization!");
 		}
		
		// Refer to GlowstoneDropCommands
		executor = new GlowstoneDropCommands(this);
		getCommand("glowstonedrop").setExecutor(executor);
 
 		// Message
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");
 
 		// Stats
 		try {
 			Metrics metrics = new Metrics();
 			// Custom plotter for each item
 			for (int i = 0; i < itemList.size(); i++) {
 				final String itemName = itemList.get(i);
 				metrics.addCustomData(this, new Metrics.Plotter() {
 					@Override
 					public String getColumnName() {
 						return itemName;
 					}
 
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 			metrics.beginMeasuringPlugin(this);
 		}
 		catch (IOException e) {}
 	}
 
 	// Loads the config at start
 	public void loadConfig() {
 		config.options().header("For help please refer to http://bit.ly/oW6iR1 or http://bit.ly/rcN2QB");
 		config.addDefault("configuration.permissions", true);
 		config.addDefault("configuration.messages", true);
 		config.addDefault("worlds.normal", "block");
 		config.addDefault("worlds.nether", "dust");
 		config.addDefault("worlds.end", "block");
 		config.addDefault("items", Arrays.asList(items));
 		itemList = config.getStringList("items");
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	// Loads the localization
 	public void loadLocalization() {
 		localization.options().header("The underscores are used for the different lines!");
 		localization.addDefault("permission_denied", "&4You don''t have the permission to do this!");
 		localization.addDefault("set", "&2Drop in the &4%world &2worlds changed to &4%value&2!");
 		localization.addDefault("set_all", "&2Drop for all worlds changed to &4%value&2!");
 		localization.addDefault("reload", "&2GlowstoneDrop &4%version &2reloaded!");
 		localization.addDefault("enable_messages", "&2GlowstoneDrop &4messages &2enabled!");
 		localization.addDefault("disable_messages", "&2GlowstoneDrop &4messages &2disabled!");
 		localization.addDefault("enable_permissions_1", "&2GlowstoneDrop &4permissions &2enabled! Only OPs");
 		localization.addDefault("enable_permissions_2", "&2and players with the permission can use the plugin!");
 		localization.addDefault("disable_permissions_1", "&2GlowstoneDrop &4permissions &4disabled!");
 		localization.addDefault("disable_permissions_2", "&2All players can use the plugin!");
 		localization.addDefault("help_1", "&2Welcome to the GlowstoneDrop version &4%version &2help!");
 		localization.addDefault("help_2", "To see the help type &4/glowstonedrop help &f or &4/glowdrop help");
 		localization.addDefault("help_3", "To reload use &4/glowstonedrop reload &f or &4/glowdrop reload");
 		localization.addDefault("help_4", "To change the drops use &4/glowstonedrop set <world> <drop>");
 		localization.addDefault("help_5", "or &4/glowdrop set <world> <drop>");
 		localization.addDefault("help_6", "To enable something use &4/glowstonedrop enable &e<value>");
 		localization.addDefault("help_7", "or &4/glowdrop enable &e<value>");
 		localization.addDefault("help_8", "To disable something use &4/glowstonedrop disable &e<value>");
 		localization.addDefault("help_9", "or &4/glowdrop disable &e<value>");
 		localization.addDefault("help_10", "&eValues &fcan be: permissions, messages");
 		localization.addDefault("help_11", "&eWorlds &fcan be: normal, end, nether (or all)");
 		localization.addDefault("help_12", "&eDrops &fcan be: dust, block");
 		localization.options().copyDefaults(true);
 		saveLocalization();
 
 	}
 
 	// Saves the localization
 	public void saveLocalization() {
 		try {
 			localization.save(localizationFile);
		}
		catch (IOException e) {
 			log.warning("GlowstoneDrop failed to save the localization! Please report this!");
 		}
 	}
 
 	// Reloads the config via command /glowstonedrop reload or /glowdrop reload
 	public void loadConfigsAgain() {
 		try {
 			config.load(configFile);
 			saveConfig();
 			localization.load(localizationFile);
 			saveLocalization();
 			itemList = config.getStringList("items");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// Message the sender or player
 	public void message(CommandSender sender, Player player, String message, String value, String world) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		message = message
 				.replaceAll("&([0-9a-fk])", "\u00A7$1")
 				.replaceAll("%version", pdfFile.getVersion())
 				.replaceAll("%world", world)
 				.replaceAll("%value", value);
 		if (player != null) {
 			player.sendMessage(message);
 		}
 		else if (sender != null) {
 			sender.sendMessage(message);
 		}
 	}
 
 	// If no config is found, copy the default one!
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len=in.read(buf)) > 0) {
 				out.write(buf,0,len);
 			}
 			out.close();
 			in.close();
		}
		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
