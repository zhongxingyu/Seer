 package alshain01.Flags;
 
 import java.util.List;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import alshain01.Flags.Director.LandSystem;
 import alshain01.Flags.Updater.UpdateResult;
 import alshain01.Flags.commands.Command;
 import alshain01.Flags.data.CustomYML;
 import alshain01.Flags.data.DataStore;
 import alshain01.Flags.data.YamlDataStore;
 import alshain01.Flags.importer.GPFImport;
 import alshain01.Flags.metrics.MetricsManager;
 
 /**
  * Flags
  * 
  * @author Alshain01
  */
 public class Flags extends JavaPlugin{
 	public static Flags instance;
 	public DataStore dataStore;
 	protected CustomYML messageStore;
 	public Economy economy = null;
 	
 	private Registrar flagRegistrar = new Registrar();
 	private final Boolean DEBUG = true;
 	protected LandSystem currentSystem = LandSystem.NONE;
 	protected static Updater updater = null;
 	
 	/**
 	 * Called when this plug-in is enabled
 	 */
 	@Override
 	public void onEnable(){
 		instance = this;
 		
 		// Create the configuration file if it doesn't exist
 		this.saveDefaultConfig();
 		
 		if(this.getConfig().getBoolean("Flags.Update.Check")) {
 			String key = this.getConfig().getString("Flags.Update.ServerModsAPIKey");
 			if(this.getConfig().getBoolean("Flags.Update.Download")) {
 				updater = new Updater(this, 65024, this.getFile(), Updater.UpdateType.DEFAULT, key, true);
 			} else {
 				updater = new Updater(this, 65024, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, key, false);
 			}
 			if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
 				Bukkit.getServer().getConsoleSender().sendMessage("[Flags] " + ChatColor.DARK_RED + "An update is available for Flags. Please visit http://dev.bukkit.org/bukkit-plugins/flags/ to update.");
 			}
 		}
 		this.getServer().getPluginManager().registerEvents(new UpdateListener(), instance);
         
 		// Create the specific implementation of DataStore
 		// TODO: Add sub-interface for SQL
 		dataStore = new YamlDataStore(this);
 		messageStore = new CustomYML(this, "message.yml");
 		messageStore.saveDefaultConfig();
 		
 		if (!dataStore.exists(this)) {
 			// New installation
 			if (!dataStore.create(this)) {
 				this.getLogger().warning("Failed to create database schema. Shutting down Flags.");
 				this.getServer().getPluginManager().disablePlugin(this);
 				return;
 			}
 		}
 		
 		// Update the data to current as needed.
 		dataStore.update(this);
 		
 		// Find the first available land management system
 		currentSystem = findSystem(getServer().getPluginManager());
 		if (currentSystem == LandSystem.NONE) {
 			getLogger().info("No system detected. Only world flags will be available.");
 		} else {
 			getLogger().info(currentSystem.getDisplayName() + " detected. Enabling integrated support.");
 		}
 		
 		// Check for older database and import as necessary.
 		if(currentSystem == LandSystem.GRIEF_PREVENTION && !getServer().getPluginManager().isPluginEnabled("GriefPreventionFlags")) {
 			GPFImport.importGPF();
 		}
 		
 		// Enable Vault support
 		if(setupEconomy()) {
 			//this.getServer().getPluginManager().registerEvents(new EconomyListener(), instance);
 		}
 		
 		// Load Mr. Clean
 		Director.enableMrClean(this.getServer().getPluginManager());
 		
 		// Load Border Patrol
 		if (this.getConfig().getBoolean("Flags.BorderPatrol.Enable")) {
 			this.getServer().getPluginManager().registerEvents(new BorderPatrol(), instance);
 		}
 		
		if(!DEBUG) {
 			MetricsManager.StartMetrics();
 		}
 		
 		this.getLogger().info("Flags Has Been Enabled.");
 	}
 	
 	/**
 	 * Called when this plug-in is disabled 
 	 */
 	@Override
 	public void onDisable(){
 		//if(dataStore instanceof SQLDataStore) { ((SQLDataStore)dataStore).close(); }
 		getLogger().info("Flags Has Been Disabled.");
 	}
 	
 	/**
 	 * Gets the registrar for this instance of Flags.
 	 * 
 	 * @return The flag registrar.
 	 */
 	public Registrar getRegistrar() {
 		return flagRegistrar;
 	}
     
 	/**
 	 * Executes the given command, returning its success 
 	 * 
 	 * @param sender Source of the command
 	 * @param cmd    Command which was executed
 	 * @param label  Alias of the command which was used
 	 * @param args   Passed command arguments 
 	 * @return		 true if a valid command, otherwise false
 	 * 
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("flag")) {
 			return Command.onFlagCommand(sender, args);
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("bundle")) {
 			return Command.onBundleCommand(sender, args);
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns true if the provided string represents a
 	 * version number that is equal to or lower than the
 	 * current Bukkit API version.
 	 * 
 	 * String should be formatted with 3 numbers: x.y.z
 	 * 
 	 * @return True if the version provided is compatible
 	 */	
 	public boolean checkAPI(String version) {
 		float APIVersion = Float.valueOf(Bukkit.getServer().getBukkitVersion().substring(0, 3));
 		float CompareVersion = Float.valueOf(version.substring(0, 3));
 		int APIBuild = Integer.valueOf(Bukkit.getServer().getBukkitVersion().substring(4, 5));
 		int CompareBuild = Integer.valueOf(version.substring(4, 5));
 		
 		if (APIVersion > CompareVersion || (APIVersion == CompareVersion && APIBuild >= CompareBuild)) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sends a debug message through the Flags logger if the plug-in is a development build.
 	 * @param message The debug message
 	 */
 	public final void Debug(String message) {
 		if (DEBUG) {
 			this.getLogger().info("DEBUG: " + message);
 		}
 	}
 	
 	/*
 	 * Register with the Vault economy plugin.
 	 * 
 	 * @return True if the economy was successfully configured. 
 	 */
     private boolean setupEconomy()
     {
     	if (!Flags.instance.getServer().getPluginManager().isPluginEnabled("Vault")) { return false; }
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
         		.getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
         	economy = economyProvider.getProvider();
         }
 
         return (economy != null);
     }
 	
 	
 	/*
 	 * Acquires the land management plugin.
 	 */
 	private LandSystem findSystem(PluginManager pm) {
 		List<?> pluginList = getConfig().getList("Flags.AreaPlugins");
 
 		for(Object o : pluginList) {
 			if (pm.isPluginEnabled((String)o)) {
 				return LandSystem.getByName((String)o);
 			}
 		}
 		return LandSystem.NONE;				
 	}
 	
 	private class UpdateListener implements Listener {
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerJoin(PlayerJoinEvent e) {
 			if(e.getPlayer().hasPermission("flags.admin.notifyupdate") && updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
 					e.getPlayer().sendMessage(ChatColor.DARK_RED + "An update is available for Flags. Please visit http://dev.bukkit.org/bukkit-plugins/flags/ to update.");
 			}
 		}
 	}
 }
