 package com.timvisee.safecreeper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import java.io.OutputStream;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.timvisee.safecreeper.api.SCApiController;
 import com.timvisee.safecreeper.command.CommandHandler;
 import com.timvisee.safecreeper.entity.SCLivingEntityReviveManager;
 import com.timvisee.safecreeper.handler.SCConfigHandler;
 import com.timvisee.safecreeper.handler.plugin.SCCorruptionHandler;
 import com.timvisee.safecreeper.handler.plugin.SCFactionsHandler;
 import com.timvisee.safecreeper.handler.plugin.SCMobArenaHandler;
 import com.timvisee.safecreeper.handler.plugin.SCPVPArenaHandler;
 import com.timvisee.safecreeper.handler.plugin.SCTVNLibHandler;
 import com.timvisee.safecreeper.handler.plugin.SCWorldGuardHandler;
 import com.timvisee.safecreeper.listener.*;
 import com.timvisee.safecreeper.manager.*;
 import com.timvisee.safecreeper.task.SCDestructionRepairRepairTask;
 import com.timvisee.safecreeper.task.SCDestructionRepairSaveDataTask;
 import com.timvisee.safecreeper.task.SCUpdateCheckerTask;
 import com.timvisee.safecreeper.util.SCFileUpdater;
 import com.timvisee.safecreeper.util.SCUpdateChecker;
 
 public class SafeCreeper extends JavaPlugin {
 	
 	// Safe Creeper static instance
 	public static SafeCreeper instance;
 	
 	// Logger
 	private SCLogger log;
 	
 	// Listeners
 	private final SCBlockListener blockListener = new SCBlockListener();
 	private final SCEntityListener entityListener = new SCEntityListener();
 	private final SCPlayerListener playerListener = new SCPlayerListener();
 	private final SCPluginListener pluginListener = new SCPluginListener();
 	private final SCHangingListener hangingListener = new SCHangingListener();
 	private final SCTVNLibListener tvnlListener = new SCTVNLibListener();
 	private final SCWeatherListener weatherListener = new SCWeatherListener();
 	private final SCWorldListener worldListener = new SCWorldListener();
 	
 	// Config file and folder paths
 	private File globalConfigFile = new File("plugins/SafeCreeper/global.yml");
 	private File worldConfigsFolder = new File("plugins/SafeCreeper/worlds");
 	
 	// Managers
 	private SCApiController apiManager;
 	private SCTVNLibHandler tvnlManager;
 	private SCPermissionsManager pm;
 	private SCConfigHandler cm = null;
 	private SCDestructionRepairManager drm;
 	private SCLivingEntityReviveManager lerm;
 	private SCCorruptionHandler corManager;
 	private SCMobArenaHandler mam;
 	private SCPVPArenaHandler pam;
 	private SCFactionsHandler fm;
 	private SCWorldGuardHandler wgm;
 	
 	// Update Checker
 	private SCUpdateChecker uc = null;
 	
 	// Debug Mode
 	boolean debug = false;
 	
 	// Variable to disable the other explosions for a little, little while (otherwise some explosions are going to be looped)
 	public boolean disableOtherExplosions = false;
 	
 	/**
 	 * Constructor
 	 */
 	public SafeCreeper() {
 		// Define the Safe Creeper static instance variable
 		instance = this;
 	}
 	
 	/**
 	 * On enable method, called when plugin is being enabled
 	 */
 	public void onEnable() {
 		// Store the time Safe Creeper is starting on so the starting duration can be calculated
 		long t = System.currentTimeMillis();
 		
 		// Get Bukkit's plugin manager
 		PluginManager pm = getServer().getPluginManager();
 		
 		// Set up the file paths
 		globalConfigFile = new File(getConfig().getString("GlobalConfigFilePath", globalConfigFile.getPath()));
 		worldConfigsFolder = new File(getConfig().getString("WorldConfigsFolderPath", worldConfigsFolder.getPath()));
 
 		// Set up the Safe Creeper logger
 		setUpSCLogger();
 		
 		// Set up the API manager
 		setUpApiManager();
 		
		// Verify all the Safe Creeper files
		verifyExternalFiles(true);
		
 		// Setup the config manager before all other managers, to make the file updater work
 	    setUpConfigManager();
 		
 		// Initialize the update checker
 		setUpUpdateChecker();
 		
 		// Remove all (old) update files
 		getUpdateChecker().removeUpdateFiles();
 		
 		// Check if any update exists
 		if(getConfig().getBoolean("updateChecker.enabled", true)) {
 			if(uc.isNewVersionAvailable()) {
 				final String newVer = uc.getNewestVersion();
 				System.out.println("[SafeCreeper] New Safe Creeper version available: v" + newVer);
 				
 				// Auto install updates if enabled
 				if(getConfig().getBoolean("updateChecker.autoInstallUpdates", true) || getUpdateChecker().isImportantUpdateAvailable()) {
 					if(!uc.isNewVersionCompatibleWithCurrentBukkit()) {
 						System.out.println("[SafeCreeper] The newest Safe Creeper version is not compatible with the current Bukkit version!");
 						System.out.println("[SafeCreeper] Please update to Bukkit " + uc.getRequiredBukkitVersion() + " or higher!");
 					} else {
 						// Check if already update installed
 						if(getUpdateChecker().isUpdateDownloaded())
 							System.out.println("[SafeCreeper] Safe Creeper update installed, server reload required!");
 						else {
 							// Download the update and show some status messages
 							System.out.println("[SafeCreeper] Automaticly installing SafeCreeper update...");
 							getUpdateChecker().downloadUpdate();
 							System.out.println("[SafeCreeper] Safe Creeper update installed, reload required!");
 						}
 					}
 				} else {
 					// Auto installing updates not enabled, show a status message
 					System.out.println("[SafeCreeper] Use '/sc installupdate' to automaticly install the new update!");
 				}
 			}
 		}
 		
 		// Schedule update checker task
 		FileConfiguration config = getConfig();
 		if(config.getBoolean("tasks.updateChecker.enabled", true)) {
 			int taskInterval = (int) config.getDouble("tasks.updateChecker.interval", 3600) * 20;
 			
 			// Schedule the update checker task
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new SCUpdateCheckerTask(getConfig(), getUpdateChecker()), taskInterval, taskInterval);
 		} else {
 			// Show an warning in the console
 			getSCLogger().info("Scheduled task 'updateChecker' disabled in the config file!");
 		}
 		
 		// Set up remaining managers
 		setUpTVNLibManager();
 	    setUpPermissionsManager();
 	    setUpDestructionRepairManager();
 	    setUpLivingEntityReviveManager();
 	    setUpMobArenaManager();
 	    setUpPVPArenaManager();
 	    setUpFactionsManager();
 	    setUpWorldGuardManager();
 	    setUpCorruptionManager();
 		
 		// Load destruction repair data
 		getDestructionRepairManager().load();
 		
 		// Register event listeners
 		pm.registerEvents(this.blockListener, this);
 		pm.registerEvents(this.entityListener, this);
 		pm.registerEvents(this.hangingListener, this);
 		pm.registerEvents(this.playerListener, this);
 		pm.registerEvents(this.pluginListener, this);
 		pm.registerEvents(this.weatherListener, this);
 		pm.registerEvents(this.worldListener, this);
 		
 		// Register the TVNLibListener if the TVNLib listener plugin is installed
 		if(getTVNLibManager().isEnabled())
 			pm.registerEvents(this.tvnlListener, this);
 		
 		/* // Test - Beginning of custom mob abilities!
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					List<Player> onlinePlayers = Arrays.asList(getServer().getOnlinePlayers());
 					if(onlinePlayers.size() > 0) {
 						for(Player p : onlinePlayers) {
 							//Player p = onlinePlayers.get(0);
 							for(LivingEntity e : p.getWorld().getLivingEntities()) {
 								if(e instanceof Creature) {
 									/*Creature c = (Creature) e;
 									c.setTarget(p);* /
 									
 									//c.launchProjectile();
 									
 									if(getLivingEntityManager().isSCLivingEntity(e)) {
 										SCLivingEntity scle = getLivingEntityManager().getLivingEntity(e);
 										
 										if(scle.getLivingEntity().getLocation().distance(p.getLocation()) > 15)
 											continue;
 										
 										scle.shootProjectile(p);
 									}
 								}
 							}
 						}
 					}
 				}
 			}, 20, 20);*/
 		
 		// Task to repair blocks from the destruction repair manager// Schedule update checker task
 		if(config.getBoolean("tasks.destructionRepairRepair.enabled", true)) {
 			int taskInterval = (int) config.getDouble("tasks.destructionRepairRepair.interval", 1) * 20;
 			
 			// Schedule the task
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new SCDestructionRepairRepairTask(getDestructionRepairManager()), taskInterval, taskInterval);
 		} else {
 			// Show an warning in the console
 			getSCLogger().info("Scheduled task 'destructionRepairRepair' disabled in the config file!");
 		}
 		
 		// Task to save the destruction repair data
 		if(config.getBoolean("tasks.destructionRepairSave.enabled", true)) {
 			int taskInterval = (int) config.getDouble("tasks.destructionRepairSave.interval", 300) * 20;
 			
 			// Schedule the task
 			boolean showMsg = config.getBoolean("tasks.destructionRepairSave.showSaveStatus", true);
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new SCDestructionRepairSaveDataTask(getDestructionRepairManager(), showMsg), taskInterval, taskInterval);
 		} else {
 			// Show an warning in the console
 			getSCLogger().info("Scheduled task 'destructionRepairSave' disabled in the config file!");
 		}
 		
 		// Plugin sucesfuly enabled, show console message
 		PluginDescriptionFile pdfFile = getDescription();
 		
 		// Calculate the load duration
 		long duration = System.currentTimeMillis() - t;
 		
 		// Show a status message
 		getSCLogger().info("Safe Creeper v" + pdfFile.getVersion() + " enabled, took " + String.valueOf(duration) + " ms!");
 	}
 	
 	/**
 	 * On disable method, called when plugin is being disabled
 	 */
 	public void onDisable() {
 		// Save the destruction repair data
 		getDestructionRepairManager().save();
 		
 		// Cancel all running Safe Creeper tasks
 		stopTasks();
 		
 		// Unhook all plugins hooked into Safe Creeper and remove/unregister their sessions
 		if(getApiManager().getApiSessionsCount() > 0) {
 			getSCLogger().info("Unhooking all hooked plugins...");
 			getApiManager().unregisterAllApiSessions();
 		}
 		
 		// If any update was downloaded, install the update
 		if(getUpdateChecker().isUpdateDownloaded())
 			getUpdateChecker().installUpdate();
 		
 		// Remove all update files
 		getUpdateChecker().removeUpdateFiles();
 		
 		// Plugin disabled, show console message
 		getSCLogger().info("Safe Creeper Disabled");
 	}
 	
 	/**
 	 * Stop all scheduled Safe Creeper tasks
 	 */
 	public void stopTasks() {
 		getSCLogger().info("Cancelling all Safe Creeper tasks...");
 		SafeCreeper.instance.getServer().getScheduler().cancelTasks(SafeCreeper.instance);
 		getSCLogger().info("All Safe Creeper tasks cancelled!");
 	}
     
 	/**
 	 * Fetch the Safe Creeper version from the plugin.yml file
 	 * @return Fetch the Safe Creeper version from the plugin.yml file
 	 */
 	public String getVersion() {
 		return getDescription().getVersion();
 	}
 	
 	/**
 	 * Set up the update checker
 	 */
 	public void setUpUpdateChecker() {
 		this.uc = new SCUpdateChecker();
 	}
 	
 	/**
 	 * Get the update checker instance
 	 * @return Update checker instance
 	 */
 	public SCUpdateChecker getUpdateChecker() {
 		return this.uc;
 	}
 	
 	/**
 	 * Set up the Safe Creeper logger
 	 */
 	public void setUpSCLogger() {
 		this.log = new SCLogger(getLogger());
 	}
 	
 	/**
 	 * Get the Safe Creeper logger instance
 	 * @return Safe Creeper logger instance
 	 */
 	public SCLogger getSCLogger() {
 		return this.log;
 	}
 	
 	/**
 	 * Get the plugin listener
 	 * @return SCPluginListener instance
 	 */
 	public SCPluginListener getPluginListener() {
 		return this.pluginListener;
 	}
 	
 	/**
 	 * Set up the API Manager
 	 */
 	public void setUpApiManager() {
 		// Construct the API Manager
 		this.apiManager = new SCApiController(false);
 		
 		// Show a status message
 		getSCLogger().info("Safe Creeper API started!");
 		
 		// Enable the API if it should be enabled
 		if(getConfig().getBoolean("api.enabled", true))
 			this.apiManager.setEnabled(true);
 		else
 			getSCLogger().info("Not enabling Safe Creeper API, disabled in config file!");
 	}
 	
 	/**
 	 * Get the API Manager instance
 	 * @return API Manager instance
 	 */
 	public SCApiController getApiManager() {
 		return this.apiManager;
 	}
 	
 	/**
 	 * Set up the TVNLib manager
 	 */
 	public void setUpTVNLibManager() {
 		// Setup TVNLib Manager
 		this.tvnlManager = new SCTVNLibHandler(getSCLogger());
 		this.tvnlManager.setUp();
 	}
 	
 	/**
 	 * Get the TVNLib manager instance
 	 * @return TVNLib manager instance
 	 */
 	public SCTVNLibHandler getTVNLibManager() {
 		return this.tvnlManager;
 	}
 	
 	/**
 	 * Set up the config manager
 	 */
 	public void setUpConfigManager() {
 		this.cm = new SCConfigHandler(globalConfigFile, worldConfigsFolder);
 	}
 	
 	/**
 	 * Get the config manager instance
 	 * @return
 	 */
 	public SCConfigHandler getConfigManager() {
 		return this.cm;
 	}
 	
 	/**
 	 * Setup the permissions manager
 	 */
 	public void setUpPermissionsManager() {
 		// Setup the permissions manager
 		this.pm = new SCPermissionsManager(this.getServer(), this, getSCLogger());
 		this.pm.setup();
 	}
 	
 	/**
 	 * Get the permissions manager
 	 * @return permissions manager
 	 */
 	public SCPermissionsManager getPermissionsManager() {
 		return this.pm;
 	}
 	
 	/**
 	 * Setup the destruction repair manager
 	 */
 	public void setUpDestructionRepairManager() {
 		// Setup the  destruction repair manager
 		this.drm = new SCDestructionRepairManager();
 	}
 	
 	/**
 	 * Get the destruction repair manager
 	 * @return destruction repair manager
 	 */
 	public SCDestructionRepairManager getDestructionRepairManager() {
 		return this.drm;
 	}
 
 	/**
 	 * Set up the World Guard handler
 	 */
 	public void setUpWorldGuardManager() {
 		this.wgm = new SCWorldGuardHandler(getSCLogger());
 		this.wgm.setUp();
 	}
 	
 	/**
 	 * Get the World Guard plugin instance
 	 * @return
 	 */
     public SCWorldGuardHandler getWorldGuardManager() {
         return this.wgm;
     }
     
     /**
      * Set up the Mob Arena Manager
      */
     public void setUpMobArenaManager() {
     	// Set up the mob arena manager
     	this.mam = new SCMobArenaHandler(getSCLogger());
     	this.mam.setUp();
     }
     
     /**
      * Get the MobArena manager
      * @return MobArena manager
      */
     public SCMobArenaHandler getMobArenaManager() {
     	return this.mam;
     }
    
     /**
      * Set up the PVP Arena manager
      */
     public void setUpPVPArenaManager() {
     	// Set up the PVP Arena manager
     	this.pam = new SCPVPArenaHandler(getSCLogger());
     	this.pam.setUp();
     }
     
     /**
      * Get the PVP Arena manager instance
      * @return PVP Arena manager instnace
      */
     public SCPVPArenaHandler getPVPArenaManager() {
     	return this.pam;
     }
    
     /**
      * Set up the Factions manager
      */
     public void setUpFactionsManager() {
     	this.fm = new SCFactionsHandler(getSCLogger());
     	this.fm.setUp();
     }
     
     /**
      * Get the Factions manager
      * @return Factions manager instance
      */
     public SCFactionsHandler getFactionsManager() {
     	return this.fm;
     }
     
     /**
      * Set up the Corruption manager
      */
     public void setUpCorruptionManager() {
     	this.corManager = new SCCorruptionHandler(getSCLogger());
     	this.corManager.setUp();
     }
     
     /**
      * Get the Corruption handler
      * @return Corruption manager
      */
     public SCCorruptionHandler getCorruptionManager() {
     	return this.corManager;
     }
     
     /**
      * Set up the living 
      */
     public void setUpLivingEntityReviveManager() {
     	this.lerm = new SCLivingEntityReviveManager();
     }
     
     /**
      * Get the living entity revive manager instance
      * @return Living entity revive manager instance
      */
     public SCLivingEntityReviveManager getLivingEntityReviveManager() {
     	return this.lerm;
     }
 	
     /**
      * Verify all the external Safe Creeper files, automatically fix invalid files if enabled
      * @param autoFix True to automatically fix and update invalid files
      * @return True when every file was fine, false if something was wrong
      */
     public boolean verifyExternalFiles(boolean autoFix) {
     	// Get the Safe Creeper data folder
     	File scDir = this.getDataFolder();
     	
     	// Keep track if everything was right
     	boolean invalid = false;
     	
     	// Make sure the Safe Creeper directory exists
     	if(!scDir.exists() || !scDir.isDirectory()) {
     		invalid = true;
     		
     		// Automatically fix this issue if enabled
     		if(autoFix) {
     			getSCLogger().info("Creating Safe Creeper directory...");
     			scDir.mkdirs();
     		}
     	}
     	
     	// Make sure the main config file exists
 		File cfgFile = new File(scDir, "config.yml");
 		if(!cfgFile.exists() || !cfgFile.isFile()) {
 			invalid = true;
 			
 			// Automatically fix the file
 			if(autoFix) {
 				getSCLogger().info("Creating new config file...");
 				copyFile(getResource("res/config.yml"), cfgFile);
 			}
 		}
 		
 		// Verify the config file version
 		if(SCFileUpdater.updateConfig())
 			invalid = true;
 		
 		// Make sure the global file exists
 		if(!globalConfigFile.exists() || !globalConfigFile.isFile()) {
 			invalid = true;
 			
 			// Automatically fix the file
 			if(autoFix) {
 				getSCLogger().info("Creating new global file...");
 				copyFile(getResource("res/global.yml"), globalConfigFile);
 			}
 		}
 		
 		// Verify the config file version
 		if(SCFileUpdater.updateGlobalConfig())
 			invalid = true;
 		
 		// Make sure the worlds folder exists
 		if(!worldConfigsFolder.exists() || !worldConfigsFolder.isDirectory()) {
 			invalid = true;
 			
 			// Automatically fix the directory
 			if(autoFix) {
 				getSCLogger().info("Generating new worlds directory...");
 				worldConfigsFolder.mkdirs();
 				copyFile(getResource("res/worlds/world_example.yml"), new File(worldConfigsFolder, "world_example.yml"));
 				copyFile(getResource("res/worlds/world_example2.yml"), new File(worldConfigsFolder, "world_example2.yml"));
 			}
 		}
     	
 		// Verify all the world config files
 		if(SCFileUpdater.updateAllWorldsConfig())
 			invalid = true;
     	
 		// Return the result
 		return (!invalid);
     }
     
     /**
      * Check if the config file exists
      * @throws Exception
      */
 	public void checkConigFilesExist() throws Exception {
 		if(!getDataFolder().exists()) {
 			getSCLogger().info("Creating new Safe Creeper folder");
 			getDataFolder().mkdirs();
 		}
 		File configFile = new File(getDataFolder(), "config.yml");
 		if(!configFile.exists()) {
 			getSCLogger().info("Generating new config file");
 			copyFile(getResource("res/config.yml"), configFile);
 		}
 		if(!globalConfigFile.exists()) {
 			getSCLogger().info("Generating new global file");
 			copyFile(getResource("res/global.yml"), globalConfigFile);
 		}
 		if(!worldConfigsFolder.exists()) {
 			getSCLogger().info("Generating new 'worlds' folder");
 			worldConfigsFolder.mkdirs();
 			copyFile(getResource("res/worlds/world_example.yml"), new File(worldConfigsFolder, "world_example.yml"));
 			copyFile(getResource("res/worlds/world_example2.yml"), new File(worldConfigsFolder, "world_example2.yml"));
 		}
 	}
 	
 	/**
 	 * Copy a file
 	 * @param in Input stream (file)
 	 * @param file File to copy the file to
 	 */
 	private void copyFile(InputStream in, File file) {
 	    try {
 	        OutputStream out = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0){
 	            out.write(buf,0,len);
 	        }
 	        out.close();
 	        in.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 
 	/**
 	 * On command method, called when a command ran on the server
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// Run the command trough the command handler
 		CommandHandler ch = new CommandHandler();
 		return ch.onCommand(sender, cmd, commandLabel, args);
 	}
 }
