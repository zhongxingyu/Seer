 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.commands.BundleCommand;
 import io.github.alshain01.flags.commands.FlagCommand;
 import io.github.alshain01.flags.commands.SectorCommand;
 
 import io.github.alshain01.flags.sector.SectorListener;
 import io.github.alshain01.flags.sector.Sector;
 import io.github.alshain01.flags.sector.SectorManager;
 import io.github.alshain01.flags.update.UpdateListener;
 import io.github.alshain01.flags.update.UpdateScheduler;
 import io.github.alshain01.flags.data.*;
 import io.github.alshain01.flags.events.PlayerChangedAreaEvent;
 import io.github.alshain01.flags.importer.GPFImport;
 import io.github.alshain01.flags.metrics.MetricsManager;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredListener;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Flags
  * 
  * @author Alshain01
  */
 public class Flags extends JavaPlugin {
     private boolean sqlData = false;
 
     // Made static to access from enumerations and lower hefty method calls
     protected static CustomYML messageStore;
     private static DataStore dataStore;
     private static Economy economy = null;
     private static Logger logger;
     private static boolean debugOn = false;
 
     // Made static for use by API
     protected static System currentSystem = System.WORLD;
     private static Registrar flagRegistrar = new Registrar();
     private static SectorManager sectors;
     private static boolean borderPatrol = false;
 
 
 	/**
 	 * Called when this plug-in is enabled
 	 */
 	@Override
 	public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         ConfigurationSerialization.registerClass(Flag.class);
 
         // Set up the plugin's configuration file
         saveDefaultConfig();
         final ConfigurationSection pluginConfig = getConfig().getConfigurationSection("Flags");
 
         // Initialize the static variables
         debugOn = pluginConfig.getBoolean("Debug");
         logger  = this.getLogger();
         (messageStore = new CustomYML(this, "message.yml")).saveDefaultConfig();
         currentSystem = System.find(pm, pluginConfig.getList("AreaPlugins"));
         dataStore = DataStoreType.getByUrl(this, pluginConfig.getString("Database.Url"));
         economy = setupEconomy();
         sqlData = dataStore instanceof SQLDataStore;
 
         //TODO Consider removing support for this
         // Check for older database and import as necessary.
         if (currentSystem == System.GRIEF_PREVENTION
                 && !pm.isPluginEnabled("GriefPreventionFlags")) {
             GPFImport.importGPF();
         }
 
         // New installation
         if (!dataStore.create(this)) {
             logger.severe("Failed to create database schema. Shutting down Flags.");
             pm.disablePlugin(this);
             return;
         }
         dataStore.update(this);
 
         // Load Mr. Clean
         MrClean.enable(this, pluginConfig.getBoolean("MrClean"));
 
         // Configure the updater
         ConfigurationSection updateConfig = pluginConfig.getConfigurationSection("Update");
 		if (updateConfig.getBoolean("Check")) {
             UpdateScheduler updater = new UpdateScheduler(this, getFile(), updateConfig);
            updater.runTaskAsynchronously(this);
			updater.runTaskTimerAsynchronously(this, 36000, 1728000);
             pm.registerEvents(new UpdateListener(updater), this);
 		}
 
 		// Load Border Patrol
         ConfigurationSection bpConfig = pluginConfig.getConfigurationSection("BorderPatrol");
 		if (bpConfig.getBoolean("Enable")) {
             borderPatrol = true;
 			BorderPatrol bp = new BorderPatrol(bpConfig.getInt("EventDivisor"), bpConfig.getInt("TimeDivisor"));
 			pm.registerEvents(bp, this);
 		}
 
         // Load Sectors
         if(currentSystem == System.FLAGS) {
             ConfigurationSerialization.registerClass(Sector.class);
             ConfigurationSection sectorConfig = pluginConfig.getConfigurationSection("Sector");
             sectors = new SectorManager(new CustomYML(this, "sector.yml"), sectorConfig.getInt("DefaultDepth"));
             pm.registerEvents(new SectorListener(Material.getMaterial(sectorConfig.getString("Tool"))), this);
             getCommand("sector").setExecutor(new SectorCommand());
         }
 
         // Set Command Executors
         getCommand("flag").setExecutor(new FlagCommand());
         getCommand("bundle").setExecutor(new BundleCommand());
 
  		// Schedule tasks to perform after server is running
 		new onServerEnabledTask(this, pluginConfig.getBoolean("Metrics.Enabled")).runTask(this);
 		logger.info("Flags Has Been Enabled.");
 	}
 
     /**
      * Called when this plug-in is disabled
      */
     @Override
     public void onDisable() {
         if(currentSystem == System.FLAGS) {
             sectors.write(new CustomYML(this, "sector.yml"));
         }
 
         if(sqlData) { ((SQLDataStore)dataStore).close(); }
 
         // Static cleanup
         messageStore = null;
         dataStore = null;
         economy = null;
         logger = null;
         flagRegistrar = null;
         currentSystem = null;
         sectors = null;
         this.getLogger().info("Flags Has Been Disabled.");
     }
 
 	/**
 	 * Executes the given command, returning its success
 	 * 
 	 * @param sender
 	 *            Source of the command
 	 * @param cmd
 	 *            Command which was executed
 	 * @param label
 	 *            Alias of the command which was used
 	 * @param args
 	 *            Passed command arguments
 	 * @return true if a valid command, otherwise false
 	 * 
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
         if(!cmd.toString().equalsIgnoreCase("flags")) { return false; }
         // Handle administration command
 
         if(args.length < 1) {
             return false;
         }
 
         if(args[0].equalsIgnoreCase("reload")) {
             this.reload();
         }
 
         if(args[0].equalsIgnoreCase("import")) {
             if(sqlData) {
                 ((SQLDataStore)dataStore).importDB();
                 return true;
             }
             sender.sendMessage(Message.SQLDatabaseError.get());
             return true;
         }
 
         /*
         if(args[0].equalsIgnoreCase("export")) {
             if(sqlData) {
                 ((SQLDataStore)dataStore).exportDB();
                 return true;
             }
             sender.sendMessage(Message.SQLDatabaseError.get());
             return true;
         }
         */
         return false;
 	}
 
     /*
      * Reloads YAML files
      */
     private void reload() {
         this.reloadConfig();
         debugOn = getConfig().getBoolean("Flags.Debug");
         messageStore.reload();
         dataStore.reload();
         logger.info("Flag Database Reloaded");
     }
 
     /**
      * Checks if the provided string represents a version number that is equal
      * to or lower than the current Bukkit API version.
      *
      * String should be formatted with 3 numbers: x.y.z
      *
      * @return true if the version provided is compatible
      */
     public static boolean checkAPI(String version) {
         try {
             final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
             final float apiVersion = Float.valueOf(bukkitVersion.substring(0, 3));
             final float CompareVersion = Float.valueOf(version.substring(0, 3));
             final int apiBuild = Integer.valueOf(bukkitVersion.substring(4, 5));
             final int CompareBuild = Integer.valueOf(version.substring(4, 5));
 
             return (apiVersion > CompareVersion
                     || apiVersion == CompareVersion	&& apiBuild >= CompareBuild);
         } catch (NumberFormatException ex) {
             warn("The API version could not be determined. Some compatible flags may be disabled.");
             return false;
         }
     }
 
     /**
      * Sends a debug message through the Flags logger.
      *
      * @param message
      *            The message
      */
     public static void debug(String message) {
         if(debugOn) {
             logger.info("[DEBUG] " + message);
         }
     }
 
     /**
      * Sends a warning message through the Flags logger.
      *
      * @param message
      *            The message
      */
     public static void warn(String message) { logger.warning(message); }
 
     /**
      * Sends a info message through the Flags logger.
      *
      * @param message
      *            The message
      */
     public static void log(String message) { logger.info(message); }
 
     /**
      * Sends a severe message through the Flags logger.
      *
      * @param message
      *            The message
      */
     public static void severe(String message) { logger.severe(message); }
 
     /**
      * Gets the status of the border patrol event listener. (i.e
      * PlayerChangedAreaEvent)
      *
      * @return The status of the border patrol listener
      */
     public static boolean getBorderPatrolEnabled() { return borderPatrol; }
 
     /**
      * Gets the DataStore used by Flags. In most cases, plugins should not
      * attempt to access this directly.
      *
      * @return The dataStore object in Flags.
      */
     public static DataStore getDataStore() { return dataStore; }
 
     /**
      * Gets the vault economy for this instance of Flags.
      *
      * @return The vault economy.
      */
     public static Economy getEconomy() { return economy; }
 
     /**
      * Gets the registrar for this instance of Flags.
      *
      * @return The flag registrar.
      */
     public static Registrar getRegistrar() { return flagRegistrar; }
 
     /**
      * Gets the sector manager for this instance of Flags.
      *
      * @return The flag registrar. Null if disabled.
      */
     public static SectorManager getSectorManager() { return sectors; }
 
 
 	/*
 	 * Tasks that must be run only after the entire sever has loaded.
 	 * Runs on first server tick.
 	 */
 	private class onServerEnabledTask extends BukkitRunnable {
         private onServerEnabledTask(JavaPlugin plugin, boolean mcStats) {
             this.plugin = plugin;
             this.mcStats = mcStats;
         }
 
         private final JavaPlugin plugin;
         private final boolean mcStats;
 
 		@Override
 		public void run() {
             // Tell Bukkit about the existing bundles
             Bundle.registerPermissions();
 
 			// Check the handlers to see if anything is registered for Border Patrol
 			final RegisteredListener[] listeners = PlayerChangedAreaEvent.getHandlerList().getRegisteredListeners();
 			if (borderPatrol && (listeners == null || listeners.length == 0)) {
                 borderPatrol = false;
                 PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Flags"));
 				logger.info("No plugins have registered for Flags' Border Patrol listener. Unregistering PlayerMoveEvent.");
 			}
 
             if (mcStats && !debugOn && Flags.checkAPI("1.3.2")) {
                 MetricsManager.StartMetrics(plugin);
             }
 		}
 	}
 
 	/*
 	 * Register with the Vault economy plugin.
 	 * 
 	 * @return True if the economy was successfully configured.
 	 */
 	private static Economy setupEconomy() {
 		if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
             final RegisteredServiceProvider<Economy> economyProvider = Bukkit
 				    .getServer().getServicesManager()
 				    .getRegistration(net.milkbowl.vault.economy.Economy.class);
 		    if (economyProvider != null) {
 			    return economyProvider.getProvider();
 		    }
         }
 		return null;
 	}
 }
