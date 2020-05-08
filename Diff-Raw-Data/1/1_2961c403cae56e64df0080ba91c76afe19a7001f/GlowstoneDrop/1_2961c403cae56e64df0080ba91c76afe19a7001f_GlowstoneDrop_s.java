 package de.dustplanet.glowstonedrop;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.Player;
 import org.mcstats.Metrics;
 import org.mcstats.Metrics.Graph;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit
  * Handles some general stuff!
  * 
  * Refer to the forum thread:
  * http://bit.ly/oW6iR1
  * 
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/rcN2QB
  * 
  * @author xGhOsTkiLLeRx
  * thanks to XxFuNxX for the original GlowstoneDrop plugin!
  */
 
 public class GlowstoneDrop extends JavaPlugin {
     private GlowstoneDropBlockListener blockListener;
     public FileConfiguration config, localization;
     private File configFile, localizationFile;
     public List<String> itemList, worldsBlock = new ArrayList<String>();
     private String[] items = { "WOOD_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLD_PICKAXE", "DIAMOND_PICKAXE" };
     private GlowstoneDropCommands executor;
 
     // Shutdown
     public void onDisable() {
 	itemList.clear();
 	worldsBlock.clear();
     }
 
     // Start
     public void onEnable() {
 	// Events
 	blockListener = new GlowstoneDropBlockListener(this);
 	PluginManager pm = getServer().getPluginManager();
 	pm.registerEvents(blockListener, this);
 
 	// Config
 	configFile = new File(getDataFolder(), "config.yml");
 	// One file and the folder not existent
 	if (!configFile.exists() && !getDataFolder().exists()) {
 	    // Break if no folder can be created!
 	    if (!getDataFolder().mkdirs()) {
 		getLogger().severe("The config folder could NOT be created, make sure it's writable!");
 		getLogger().severe("Disabling now!");
 		setEnabled(false);
 		return;
 	    }
 	}
 	
 	if (!configFile.exists()) {
 	    copy(getResource("config.yml"), configFile);
 	}
 	
 	config = getConfig();
 	loadConfig();
 
 	// Localization
 	localizationFile = new File(getDataFolder(), "localization.yml");
 	if (!localizationFile.exists()) {
 	    copy(getResource("localization.yml"), localizationFile);
 	}
 	localization = YamlConfiguration.loadConfiguration(localizationFile);
 	loadLocalization();
 
 	// Refer to GlowstoneDropCommands
 	executor = new GlowstoneDropCommands(this);
 	getCommand("glowstonedrop").setExecutor(executor);
 
 	// Stats
 	try {
 	    Metrics metrics = new Metrics(this);
 	    // Construct a graph, which can be immediately used and considered
 	    // as valid
 	    Graph graph = metrics.createGraph("Percentage of affected items");
 	    // Custom plotter for each item
 	    for (String itemName : itemList) {
 		graph.addPlotter(new Metrics.Plotter(itemName) {
 		    public int getValue() {
 			return 1;
 		    }
 		});

 	    }
 	    metrics.start();
 	} catch (IOException e) {
 	    getLogger().warning(
 		    "Failed start Metrics! Please report this! (I/O)");
 	    e.printStackTrace();
 	}
     }
 
     // Loads the config at start
     public void loadConfig() {
 	config.options().header("For help please refer to http://bit.ly/oW6iR1 or http://bit.ly/rcN2QB");
 	config.addDefault("configuration.permissions", true);
 	config.addDefault("configuration.messages", true);
 	config.addDefault("items", Arrays.asList(items));
 	itemList = config.getStringList("items");
 	List<World> worlds = getServer().getWorlds();
 	List<String> worldNames = new ArrayList<String>();
 	for (World w : worlds) {
 	    worldNames.add(w.getName().toLowerCase());
 	}
 	config.addDefault("worldsBlock", worldNames);
 	worldsBlock = config.getStringList("worldsBlock");
 	config.options().copyDefaults(true);
 	saveConfig();
     }
 
     // Loads the localization
     private void loadLocalization() {
 	localization.options().header("The underscores are used for the different lines!");
 	localization.addDefault("permission_denied", "&4You don''t have the permission to do this!");
 	localization.addDefault("set", "&2Drop in the &4%world &2worlds changed to &4%value&2!");
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
 	localization.addDefault("help_11", "&eDrops &fcan be: dust, block");
 	localization.options().copyDefaults(true);
 	saveLocalization();
     }
 
     // Saves the localization
     private void saveLocalization() {
 	try {
 	    localization.save(localizationFile);
 	} catch (IOException e) {
 	    getLogger().warning("Failed to save the localization! Please report this! (I/O)");
 	    e.printStackTrace();
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
 	} catch (InvalidConfigurationException e) {
 	    getLogger().warning("Failed to load the configs again! Please report this! (InvalidConfiguration)");
 	    e.printStackTrace();
 	}catch (IOException e) {
 	    getLogger().warning("Failed to load the configs again! Please report this! (I/O)");
 	    e.printStackTrace();
 	}
     }
 
     // Message the sender or player
     public void message(CommandSender sender, Player player, String message, String value, String world) {
 	PluginDescriptionFile pdfFile = this.getDescription();
 	message = message
 		.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
 		.replaceAll("%version", pdfFile.getVersion())
 		.replaceAll("%world", world)
 		.replaceAll("%value", value);
 	if (player != null) {
 	    player.sendMessage(message);
 	} else if (sender != null) {
 	    sender.sendMessage(message);
 	} else {
 	    getLogger().warning("Sender & player are null. Unable to send a message! Please report this!");
 	}
     }
 
     // If no config is found, copy the default one(s)!
     private void copy(InputStream in, File file) {
 	OutputStream out = null;
 	try {
 	    out = new FileOutputStream(file);
 	    byte[] buf = new byte[1024];
 	    int len;
 	    while ((len = in.read(buf)) > 0) {
 		out.write(buf, 0, len);
 	    }
 	} catch (IOException e) {
 	    getLogger().warning("Failed to copy the default config! (I/O)");
 	    e.printStackTrace();
 	} finally {
 	    try {
 		if (out != null) {
 		    out.close();
 		}
 	    } catch (IOException e) {
 		getLogger().warning("Failed to close the streams! (I/O -> Output)");
 		e.printStackTrace();
 	    }
 	    try {
 		if (in != null) {
 		    in.close();
 		}
 	    } catch (IOException e) {
 		getLogger().warning("Failed to close the streams! (I/O -> Input)");
 		e.printStackTrace();
 	    }
 	}
     }
 }
