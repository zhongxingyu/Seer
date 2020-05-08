 package de.dustplanet.superwheat;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * SuperWheat for CraftBukkit/Bukkit Handles some general stuff!
  * Refer to the forum thread:
  * http://bit.ly/superwheatthread
  * Refer to the dev.bukkit.org page: http://bit.ly/superwheatpage
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to thescreem for the original SuperWheat plugin!
  */
 
 public class SuperWheat extends JavaPlugin {
 	private final SuperWheatBlockListener blockListener = new SuperWheatBlockListener(this);
 	// Wheat
 	public boolean wheatTrampling = true, wheatEnabled = true, wheatPreventWater = true, wheatPreventWaterGrown, wheatWaterDropSeeds, wheatWaterDropWheat = true;
 	public boolean wheatPreventPiston = true, wheatPreventPistonGrown, wheatPistonDropWheat = true, wheatPistonDropSeeds;
 	// NetherWart
 	public boolean netherWartEnabled = true, netherWartPreventWater = true, netherWartPreventWaterGrown, netherWartWaterDropNetherWart = true;
 	public boolean netherWartPreventPiston = true, netherWartPreventPistonGrown, netherWartPistonDropNetherWart = true;
 	// CocoaPlant
 	public boolean cocoaPlantEnabled = true, cocoaPlantPreventWater = true, cocoaPlantPreventWaterGrown, cocoaPlantWaterDropCocoaPlant = true;
 	public boolean cocoaPlantPreventPiston = true, cocoaPlantPreventPistonGrown, cocoaPlantPistonDropCocoaPlant = true;
 	// Carrot
 	public boolean carrotTrampling = true, carrotEnabled = true, carrotPreventWater = true, carrotPreventWaterGrown, carrotWaterDropCarrot = true;
 	public boolean carrotPreventPiston = true, carrotPreventPistonGrown, carrotPistonDropCarrot = true;
 	// Potato
 	public boolean potatoTrampling = true, potatoEnabled = true, potatoPreventWater = true, potatoPreventWaterGrown, potatoWaterDropPotato = true;
 	public boolean potatoPreventPiston = true, potatoPreventPistonGrown, potatoPistonDropPotato = true;
 	// SugarCane
 	public boolean sugarCaneEnabled = false, sugarCanePreventWater = false, sugarCanePreventPiston = false, sugarCaneWaterDropSugarCane = true;
 	public boolean sugarCanePistonDropSugarCane = true;
 	// Wheat delay
 	public int wheatDelayHit = 3, wheatDelayWater = 5, wheatDelayPiston = 5;
 	// NetherWart delay
 	public int netherWartDelayHit = 3, netherWartDelayWater = 5, netherWartDelayPiston = 5;
 	// CocoaPlant delay
 	public int cocoaPlantDelayHit = 3, cocoaPlantDelayWater = 5, cocoaPlantDelayPiston = 5;
 	// Carrot delay
 	public int carrotDelayHit = 3, carrotDelayWater = 5, carrotDelayPiston = 5;
 	// Potato delay
 	public int potatoDelayHit = 3, potatoDelayWater = 5, potatoDelayPiston = 5;
 	// SugarCane delay
 	public int sugarCaneDelayHit = 3, sugarCaneDelayWater = 5, sugarCaneDelayPiston = 5;
 	// Localization
 	public String message = ChatColor.GOLD + "[SuperWheat] That plant isn't fully grown yet!";
 	public boolean messageEnabled = true;
 	// Creative mode
 	public boolean dropsCreative, blockCreativeDestroying;
 	// Enabled worlds
 	public List<String> enabledWorlds = new ArrayList<String>();
 	public FileConfiguration config;
 	private File configFile;
 
 	// Start
 	public void onEnable() {
 		// Events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(blockListener, this);
 		
 		// Config
 		configFile = new File(getDataFolder(), "config.yml");
 		if(!configFile.exists()){
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
 		}
 		config = getConfig();
 		setupDefaultConfig();
 		loadConfig();
 		
 		// Command
 		getCommand("superwheat").setExecutor(new SuperWheatCommand(this));
 	}
 
 	private void setupDefaultConfig() {
 		config.options().header("For help please either refer to the\nforum thread: http://bit.ly/superwheatthread\nor the bukkit dev page: http://bit.ly/superwheatpage");
 		// Localization
 		config.addDefault("message-enabled", true);
 		config.addDefault("message", "&6[SuperWheat] That plant is not fully grown yet!");
 		// Creative mode
 		config.addDefault("creative.dropsCreative", false);
 		config.addDefault("creative.blockCreativeDestroying", false);
 		// Wheat
 		config.addDefault("wheat.enabled", true);
 		config.addDefault("wheat.trampling", true);
 		config.addDefault("wheat.delayHit", 3);
 		config.addDefault("wheat.water.delay", 5);
 		config.addDefault("wheat.water.drops.wheat", true);
 		config.addDefault("wheat.water.drops.seed", false);
 		config.addDefault("wheat.water.prevent.premature", true);
 		config.addDefault("wheat.water.prevent.mature", false);
 		config.addDefault("wheat.piston.delay", 5);
 		config.addDefault("wheat.piston.drops.wheat", true);
 		config.addDefault("wheat.piston.drops.seed", false);
 		config.addDefault("wheat.piston.prevent.premature", true);
 		config.addDefault("wheat.piston.prevent.mature", false);
 		// NetherWart
 		config.addDefault("netherWart.enabled", true);
 		config.addDefault("netherWart.delayHit", 3);
 		config.addDefault("netherWart.water.delay", 5);
 		config.addDefault("netherWart.water.drops.netherWart", true);
 		config.addDefault("netherWart.water.prevent.premature", true);
 		config.addDefault("netherWart.water.prevent.mature", false);
 		config.addDefault("netherWart.piston.delay", 5);
 		config.addDefault("netherWart.piston.drops.netherWart", true);
 		config.addDefault("netherWart.piston.prevent.premature", true);
 		config.addDefault("netherWart.piston.prevent.mature", false);
 		// CocoaPlant
 		config.addDefault("cocoaPlant.enabled", true);
 		config.addDefault("cocoaPlant.delayHit", 3);
 		config.addDefault("cocoaPlant.water.delay", 5);
 		config.addDefault("cocoaPlant.water.drops.cocoaPlant", true);
 		config.addDefault("cocoaPlant.water.prevent.premature", true);
 		config.addDefault("cocoaPlant.water.prevent.mature", false);
 		config.addDefault("cocoaPlant.piston.delay", 5);
 		config.addDefault("cocoaPlant.piston.drops.cocoaPlant", true);
 		config.addDefault("cocoaPlant.piston.prevent.premature", true);
 		config.addDefault("cocoaPlant.piston.prevent.mature", false);
 		// Carrot
 		config.addDefault("carrot.enabled", true);
 		config.addDefault("carrot.trampling", true);
 		config.addDefault("carrot.delayHit", 3);
 		config.addDefault("carrot.water.delay", 5);
 		config.addDefault("carrot.water.drops.carrot", true);
 		config.addDefault("carrot.water.prevent.premature", true);
 		config.addDefault("carrot.water.prevent.mature", false);
 		config.addDefault("carrot.piston.delay", 5);
 		config.addDefault("carrot.piston.drops.carrot", true);
 		config.addDefault("carrot.piston.prevent.premature", true);
 		config.addDefault("carrot.piston.prevent.mature", false);
 		// Potato
 		config.addDefault("potato.enabled", true);
 		config.addDefault("potato.trampling", true);
 		config.addDefault("potato.delayHit", 3);
 		config.addDefault("potato.water.delay", 5);
 		config.addDefault("potato.water.drops.potato", true);
 		config.addDefault("potato.water.prevent.premature", true);
 		config.addDefault("potato.water.prevent.mature", false);
 		config.addDefault("potato.piston.delay", 5);
 		config.addDefault("potato.piston.drops.potato", true);
 		config.addDefault("potato.piston.prevent.premature", true);
 		config.addDefault("potato.piston.prevent.mature", false);
 		// SugarCane
 		config.addDefault("sugarCane.enabled", false);
 		config.addDefault("sugarCane.delayHit", 3);
 		config.addDefault("sugarCane.water.delay", 5);
 		config.addDefault("sugarCane.water.drops.sugarCane", true);
 		config.addDefault("sugarCane.water.prevent", false);
 		config.addDefault("sugarCane.piston.delay", 5);
 		config.addDefault("sugarCane.piston.drops.sugarCane", true);
 		config.addDefault("sugarCane.piston.prevent", false);
 		// Enabled worlds
 		List<World> worlds = getServer().getWorlds();
 		List<String> worldNames = new ArrayList<String>();
 		for (World w : worlds) {
 			worldNames.add(w.getName());
 		}
 		config.addDefault("enabled_worlds", worldNames);
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 	
 	private void loadConfig() {
 		// Localization
 		message = ChatColor.translateAlternateColorCodes('\u0026', config.getString("message"));
 		messageEnabled = config.getBoolean("message-enabled");
 		// Creative mode
 		dropsCreative = config.getBoolean("creative.dropsCreative");
 		blockCreativeDestroying = config.getBoolean("creative.blockCreativeDestroying");
 		// Wheat
 		wheatEnabled = config.getBoolean("wheat.enabled");
 		wheatTrampling = config.getBoolean("wheat.trampling");
 		wheatDelayHit = config.getInt("wheat.delayHit");
 		wheatDelayWater = config.getInt("wheat.water.delay");
 		wheatWaterDropWheat = config.getBoolean("wheat.water.drops.wheat");
 		wheatWaterDropSeeds = config.getBoolean("wheat.water.drops.seed");
 		wheatPreventWater = config.getBoolean("wheat.water.prevent.premature");
 		wheatPreventWaterGrown = config.getBoolean("wheat.water.prevent.mature");
 		wheatDelayPiston = config.getInt("wheat.piston.delay");
 		wheatPistonDropWheat = config.getBoolean("wheat.piston.drops.wheat");
 		wheatPistonDropSeeds = config.getBoolean("wheat.piston.drops.seed");
 		wheatPreventPiston = config.getBoolean("wheat.piston.prevent.premature");
 		wheatPreventPistonGrown = config.getBoolean("wheat.piston.prevent.mature");
 		// NetherWart
 		netherWartEnabled = config.getBoolean("netherWart.enabled");
 		netherWartDelayHit = config.getInt("netherWart.delayHit");
 		netherWartDelayWater = config.getInt("netherWart.water.delay");
 		netherWartWaterDropNetherWart = config.getBoolean("netherWart.water.drops.netherWart");
 		netherWartPreventWater = config.getBoolean("netherWart.water.prevent.premature");
 		netherWartPreventWaterGrown = config.getBoolean("netherWart.water.prevent.mature");
 		netherWartDelayPiston = config.getInt("netherWart.piston.delay");
 		netherWartPistonDropNetherWart = config.getBoolean("netherWart.piston.drops.netherWart");
 		netherWartPreventPiston = config.getBoolean("netherWart.piston.prevent.premature");
 		netherWartPreventPistonGrown = config.getBoolean("netherWart.piston.prevent.mature");
 		// CocoaBeans
 		cocoaPlantEnabled = config.getBoolean("cocoaPlant.enabled");
 		cocoaPlantDelayHit = config.getInt("cocoaPlant.delayHit");
 		cocoaPlantDelayWater = config.getInt("cocoaPlant.water.delay");
 		cocoaPlantWaterDropCocoaPlant = config.getBoolean("cocoaPlant.water.drops.cocoaPlant");
 		cocoaPlantPreventWater = config.getBoolean("cocoaPlant.water.prevent.premature");
 		cocoaPlantPreventWaterGrown = config.getBoolean("cocoaPlant.water.prevent.mature");
 		cocoaPlantDelayPiston = config.getInt("cocoaPlant.piston.delay");
 		cocoaPlantPistonDropCocoaPlant = config.getBoolean("cocoaPlant.piston.drops.cocoaPlant");
 		cocoaPlantPreventPiston = config.getBoolean("cocoaPlant.piston.prevent.premature");
 		cocoaPlantPreventPistonGrown = config.getBoolean("cocoaPlant.piston.prevent.mature");
 		// Carrot
 		carrotEnabled = config.getBoolean("carrot.enabled");
 		carrotTrampling = config.getBoolean("carrot.trampling");
 		carrotDelayHit = config.getInt("carrot.delayHit");
 		carrotDelayWater = config.getInt("carrot.water.delay");
 		carrotWaterDropCarrot = config.getBoolean("carrot.water.drops.carrot");
 		carrotPreventWater = config.getBoolean("carrot.water.prevent.premature");
 		carrotPreventWaterGrown = config.getBoolean("carrot.water.prevent.mature");
 		carrotDelayPiston = config.getInt("carrot.piston.delay");
 		carrotPistonDropCarrot = config.getBoolean("carrot.piston.drops.carrot");
 		carrotPreventPiston = config.getBoolean("carrot.piston.prevent.premature");
 		carrotPreventPistonGrown = config.getBoolean("carrot.piston.prevent.mature");
 		// Potato
 		potatoEnabled = config.getBoolean("potato.enabled");
 		potatoTrampling = config.getBoolean("potato.trampling");
 		potatoDelayHit = config.getInt("potato.delayHit");
 		potatoDelayWater = config.getInt("potato.water.delay");
 		potatoWaterDropPotato = config.getBoolean("potato.water.drops.potato");
 		potatoPreventWater = config.getBoolean("potato.water.prevent.premature");
 		potatoPreventWaterGrown = config.getBoolean("potato.water.prevent.mature");
 		potatoDelayPiston = config.getInt("potato.piston.delay");
 		potatoPistonDropPotato = config.getBoolean("potato.piston.drops.potato");
 		potatoPreventPiston = config.getBoolean("potato.piston.prevent.premature");
 		potatoPreventPistonGrown = config.getBoolean("potato.piston.prevent.mature");
 		// SugarCane
 		sugarCaneEnabled = config.getBoolean("sugarCane.enabled");
 		sugarCaneDelayHit = config.getInt("sugarCane.delayHit");
 		sugarCaneDelayWater = config.getInt("sugarCane.water.delay");
 		sugarCaneWaterDropSugarCane = config.getBoolean("sugarCane.water.drops.sugarCane");
 		sugarCanePreventWater = config.getBoolean("sugarCane.water.prevent");
 		sugarCaneDelayPiston = config.getInt("sugarCane.piston.delay");
 		sugarCanePistonDropSugarCane = config.getBoolean("sugarCane.piston.drops.sugarCane");
 		sugarCanePreventPiston = config.getBoolean("sugarCane.piston.prevent");
 		// Enabled worlds
 		enabledWorlds = config.getStringList("enabled_worlds");
 	}
 	
 	public void loadConfigAgain() {
 		try {
 			config.load(configFile);
 			saveConfig();
 			loadConfig();
 		}
 		catch (InvalidConfigurationException e) {
 			getLogger().warning("Failed to load the config again! Please report this! (InvalidConfiguration)");
 		}
 		catch (FileNotFoundException e) {
 			getLogger().warning("Failed to load the config again! Please report this! (FileNotFound)");
 		}
 		catch (IOException e) {
 			getLogger().warning("Failed to load the config again! Please report this! (I/O)");
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
 		} catch (FileNotFoundException e) {
 			getLogger().warning("Failed to copy the default config! (FileNotFound)");
 		} catch (IOException e) {
 			getLogger().warning("Failed to copy the default config! (I/O)");
 		}
 	}
 }
