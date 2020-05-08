 package de.dustplanet.superwheat;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
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
 
 	public Logger log = Logger.getLogger("Minecraft");
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
 	// Wheat delay
 	public int wheatDelayHit = 3, wheatDelayWater = 5, wheatDelayPiston = 5;
 	// NetherWart delay
 	public int netherWartDelayHit = 3, netherWartDelayWater = 5, netherWartDelayPiston = 5;
 	// CocoaPlant delay
 	public int cocoaPlantDelayHit = 3, cocoaPlantDelayWater = 5, cocoaPlantDelayPiston = 5;
 	// Localization
 	public String message = "6[SuperWheat] That plant isn't fully grown yet!";
 	// Creative mode
 	public boolean dropsCreative, blockCreativeDestroying;
 	public FileConfiguration config;
 	private File configFile;
 
 	// Shutdown
 	public void onDisable() {
 		// Message
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is disabled!");
 	}
 
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
 		config = this.getConfig();
 		loadConfig();
 
 		// Message
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");
 		
 	}
 
 	private void loadConfig() {
 		config.options().header("For help please either refer to the\nforum thread: http://bit.ly/superwheatthread\nor the bukkit dev page: http://bit.ly/superwheatpage");
 		// Localization
		config.addDefault("message", "6[SuperWheat] That plant isn't fully grown yet!");
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
 		config.options().copyDefaults(true);
 		saveConfig();
 		// Localization
 		message = config.getString("message");
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
 }
