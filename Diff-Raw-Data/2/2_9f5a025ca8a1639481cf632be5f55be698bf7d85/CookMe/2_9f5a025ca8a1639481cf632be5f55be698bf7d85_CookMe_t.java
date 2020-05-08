 package de.xghostkillerx.cookme;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.*;
 import org.bukkit.entity.Player;
 
 import de.xghostkillerx.cookme.Metrics.Graph;
 
 /**
  * CookeMe for CraftBukkit/Bukkit
  * Handles some general stuff!
  * 
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin for his awesome code snippet!
  * 
  */
 
 public class CookMe extends JavaPlugin {
 
 	public static final Logger log = Logger.getLogger("Minecraft");
 	private final CookMePlayerListener playerListener = new CookMePlayerListener(this);
 	public static FileConfiguration config;
 	public FileConfiguration localization;
 	public File configFile;
 	public File localizationFile;
 	public List<String> itemList = new ArrayList<String>();
 	private String[] rawFood = {"RAW_BEEF", "RAW_CHICKEN", "RAW_FISH", "PORK", "ROTTEN_FLESH"};
 	private CookMeCommands executor;
 
 	// Shutdown
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion()	+ " has been disabled!");
 	}
 
 	// Start
 	public void onEnable() {
 		// Events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(playerListener, this);
 
 		// Config
 		configFile = new File(getDataFolder(), "config.yml");
 		if(!configFile.exists()){
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
 		// if it failed, tell it
 		catch (Exception e) {
 			log.warning("CookMe failed to load the localization!");
 		}
 
 		// Refer to CookMeCommands
 		executor = new CookMeCommands(this);
 		getCommand("cookme").setExecutor(executor);
 
 		// Message
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");
 
 		// Stats
 		try {
 			Metrics metrics = new Metrics();
 			// Construct a graph, which can be immediately used and considered as valid
 			Graph graph = metrics.createGraph(this, Graph.Type.Pie, "Percentage of affected items");
 			// Custom plotter for each item
 			for (int i = 0; i < itemList.size(); i++) {
 				final String itemName = itemList.get(i);
 				graph.addPlotter(new Metrics.Plotter(itemName) {
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
 		config.options().header("For help please refer to http://bit.ly/cookmebukkitdev or http://bit.ly/cookmebukkit");
 		config.addDefault("configuration.permissions", true);
 		config.addDefault("configuration.messages", true);
 		config.addDefault("configuration.noBlocks", true);
 		config.addDefault("configuration.duration.min", 15);
 		config.addDefault("configuration.duration.max", 30);
 		config.addDefault("configuration.cooldown", 30);
 		config.addDefault("effects.damage", true);
 		config.addDefault("effects.death", true);
 		config.addDefault("effects.venom", true);
 		config.addDefault("effects.hungervenom", true);
 		config.addDefault("effects.hungerdecrease", true);
 		config.addDefault("effects.confusion", true);
 		config.addDefault("effects.blindness", true);
 		config.addDefault("effects.weakness", true);
 		config.addDefault("effects.venom", true);
 		config.addDefault("effects.slowness", true);
 		config.addDefault("effects.slowness_blocks", true);
 		config.addDefault("effects.instant_damage", true);
 		config.addDefault("effects.refusing", true);
 		config.addDefault("food", Arrays.asList(rawFood));
 		itemList = config.getStringList("food");
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	// Loads the localization
 	public void loadLocalization() {
 		localization.options().header("The underscores are used for the different lines!");
 		localization.addDefault("damage", "&4You got some random damage! Eat some cooked food!");
 		localization.addDefault("hungervenom", "&4Your foodbar is a random time venomed! Eat some cooked food!");
 		localization.addDefault("death", "&4The raw food killed you! :(");
 		localization.addDefault("venom", "&4You are for a random time venomed! Eat some cooked food!");
 		localization.addDefault("hungerdecrease", "&4Your food level went down! Eat some cooked food!");
 		localization.addDefault("confusion", "&4You are for a random time confused! Eat some cooked food!");
 		localization.addDefault("blindness", "&4You are for a random time blind! Eat some cooked food!");
 		localization.addDefault("weakness", "&4You are for a random time weak! Eat some cooked food!");
 		localization.addDefault("slowness", "&4You are for a random time slower! Eat some cooked food!");
 		localization.addDefault("slowness_blocks", "&4You mine for a random time slower! Eat some cooked food!");
 		localization.addDefault("instant_damage", "&4You got some magic damage! Eat some cooked food!");
 		localization.addDefault("refusing", "&4You decided to save your life and didn''t eat this food!");
 		localization.addDefault("permission_denied", "&4You don''t have the permission to do this!");
 		localization.addDefault("enable_effect", "&2Effect &4%effect &2enabled!");
 		localization.addDefault("enable_all", "&4All &2effects enabled!");
 		localization.addDefault("enable_messages", "&2CookMe &4messages &2enabled!");
 		localization.addDefault("enable_permissions_1", "&2CookMe &4permissions &2enabled! Only OPs");
 		localization.addDefault("enable_permissions_2", "&2and players with the permission can use the plugin!");
 		localization.addDefault("disable_effect", "&2Effect &4%effect &2disabled!");
 		localization.addDefault("disable_all", "&4All &2effects disabled!");
 		localization.addDefault("disable_messages", "&2CookMe &4messages &2disabled!");
 		localization.addDefault("disable_permissions_1", "&2CookMe &4permissions &4disabled!");
 		localization.addDefault("disable_permissions_2", "&2All players can use the plugin!");
 		localization.addDefault("reload", "&2CookMe &4%version &2reloaded!");
 		localization.addDefault("changed_cooldown", "&2The cooldown time has been changed to &e%value!");
 		localization.addDefault("changed_duration_max", "&2The maximum duration time has been changed to &e%value!");
 		localization.addDefault("changed_duration_min", "&2The minimum duration time has been changed to &e%value!");
 		localization.addDefault("help_1", "&2Welcome to the CookMe version &4%version &2help");
 		localization.addDefault("help_2", "To see the help type &4/cookme help");
 		localization.addDefault("help_3", "To reload use &4/cookme reload");
 		localization.addDefault("help_4", "To change the cooldown or duration values, type");
 		localization.addDefault("help_5", "&4/cookme set cooldown <value> &for &4/cookme set duration min <value>");
 		localization.addDefault("help_6", "&4/cookme set duration max <value>");
 		localization.addDefault("help_7", "To disable something use &4/cookme disable &e<value>");
 		localization.addDefault("help_8", "&eValues: &fpermissions, messages, damage, death, venom,");
 		localization.addDefault("help_9", "hungervenom, hungerdecrease, confusion, blindness, weakness");
		localization.addDefault("help_10", "slowness, slowness_blocks, instant_damage, refusing");
 		localization.options().copyDefaults(true);
 		saveLocalization();
 	}
 
 	// Saves the localization
 	public void saveLocalization() {
 		try {
 			localization.save(localizationFile);
 		}
 		catch (IOException e) {
 			log.warning("CookMe failed to save the localization! Please report this!");
 		}
 	}
 
 	// Reloads the configs via command /cookme reload
 	public void loadConfigsAgain() {
 		try {
 			config.load(configFile);
 			saveConfig();
 			localization.load(localizationFile);
 			saveLocalization();
 			itemList = config.getStringList("food");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// If no config is found, copy the default one!
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len=in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Message the sender or player
 	public void message(CommandSender sender, Player player, String message, String value) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		message = message
 				.replaceAll("&([0-9a-fk])", "\u00A7$1")
 				.replaceAll("%version", pdfFile.getVersion())
 				.replaceAll("%effect", value)
 				.replaceAll("%value", value);
 		if (player != null) {
 			player.sendMessage(message);
 		}
 		else if (sender != null) {
 			sender.sendMessage(message);
 		}
 	}
 }
