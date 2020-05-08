 /*
  * GuestUnlock - a bukkit plugin
  * Copyright (C) 2012 Mylleranton
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package se.myller.GuestUnlock;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import se.myller.GuestUnlock.Checks.ConfigCheck;
 import se.myller.GuestUnlock.Checks.ConfigUpdateCheck;
 import se.myller.GuestUnlock.Checks.PasswordCheck;
 import se.myller.GuestUnlock.Checks.PluginUpdateCheck;
 import se.myller.GuestUnlock.Commands.GuestUnlock;
 import se.myller.GuestUnlock.Commands.GuPassword;
 import se.myller.GuestUnlock.Commands.GuTest;
 import se.myller.GuestUnlock.Listeners.JoinListener;
 import se.myller.GuestUnlock.Permission.PermGroupManager;
 import se.myller.GuestUnlock.Permission.PermPermissionsEx;
 import se.myller.GuestUnlock.Permission.PermbPermissions;
 
 /**
  * GuestUnlock for Bukkit
  * 
  * @author Myller
  */
 
 public class Main extends JavaPlugin {
 
 	// Our variables:
 	public static Main plugin;
 	public Logger logger;
 	public FileConfiguration config;
 
 	// Class references
 	public PluginManager pluginManager;
 	public GroupManager groupMan;
 	public PermGroupManager groupManager;
 	public PermbPermissions bPermissions;
 	public PermPermissionsEx permissionsEx;
 	public GuestUnlock guestUnlock;
 	public GuPassword guPassword;
 	public GuTest guTest;
 	public PluginUpdateCheck updateCheck;
 	public DataHandler dataHandler;
 	public ConfigUpdateCheck configUpdateCheck;
 	public RunningTask runningTask;
 	public PasswordCheck passwordCheck;
 	public ConfigCheck configCheck;
 
 	public boolean isPasswordOK = false;
 	public boolean hasNewVersion = false;
 	public boolean isDebugEnabled;
 	public boolean isNewConfigAvailable = false;
 
 	public Main() {
 		guestUnlock = new GuestUnlock(this);
 		guPassword = new GuPassword(this);
 		guTest = new GuTest(this);
 		updateCheck = new PluginUpdateCheck(this);
 		dataHandler = new DataHandler(this);
 		configUpdateCheck = new ConfigUpdateCheck(this);
 		runningTask = new RunningTask(this);
 	}
 
 	/*
 	 * 
 	 * On plugin enable
 	 */
 	@Override
 	public void onEnable() {
 
 		// Get the logger
 		logger = Logger.getLogger("Minecraft.GuestUnlock");
 
 		// Get the config
 		config = this.getConfig();
 
 		// Debug
 		isDebugEnabled = config.getBoolean("Admin.Debug");
 
 		// Get the plugin.yml
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		// Msg to the console
 		log("==================================", false, Level.INFO);
 		log("Loading", true, Level.INFO);
 		log("Checking password", true, Level.INFO);
 		// Check The Password
 		if (isDebugEnabled) {
		    new PasswordCheck(this).checkPassword();
 		}
 		log("Lets see if you want auto-group moving", true, Level.INFO);
 		log("Registering events", true, Level.INFO);
 
 		// Register our events
 		pluginManager = getServer().getPluginManager();
 		pluginManager.registerEvents(new JoinListener(this), this);
 
 		// Make our config + directory
 		if (dataHandler.createDataDirectory() && dataHandler.createConfigFile()) {
 			passwordCheck = new PasswordCheck(this);
 		}
 		else if (dataHandler.createConfigFile()) {
 		    passwordCheck = new PasswordCheck(this);
 		}
 		
 		
 		// Start the timer
 		runningTask.StartTimer();
 
 		// Register our commands
 		CommandExcecutor commandEx = new CommandExcecutor(this);
 		getCommand("guestunlock").setExecutor(commandEx);
 		getCommand("gupassword").setExecutor(commandEx);
 		getCommand("gutest").setExecutor(commandEx);
 
 		// Check config for permissions support
 		if (config.getBoolean("Permissions.PermissionsEx.Enable")
 				|| config.getBoolean("Permissions.GroupManager.Enable")
 				|| config.getBoolean("Permissions.bPermissions.Enable")) {
 			log("You wanted auto-group moving support!", true, Level.INFO);
 
 			// Implement PEX
 			if (config.getBoolean("Permissions.PermissionsEx.Enable")) {
 				log("Ill try to find PermissionsEx [PEX]!", true, Level.INFO);
 				permissionsEx = new PermPermissionsEx(this);
 				permissionsEx.getPex();
 			}
 
 			// Implement GM
 			else if (config.getBoolean("Permissions.GroupManager.Enable")) {
 				log("Ill try to find GroupManager [GM]!", true, Level.INFO);
 				groupManager = new PermGroupManager(this);
 				groupManager.getGM();
 			}
 
 			// Implement bP
 			else if (config.getBoolean("Permissions.bPermissions.Enable")) {
 				log("Ill try to find bPermissions [bP]!", true, Level.INFO);
 				bPermissions = new PermbPermissions(this);
 				bPermissions.getBP();
 			}
 		} else {
 			log("You did not want auto-group moving enabled!", true, Level.INFO);
 		}
 		log("version " + pdfFile.getVersion() + " by Myller is now Enabled!",
 				false, Level.INFO);
 
 		// Check for updates
 		updateCheck.run();
 
 		// Check config-version
 		configUpdateCheck.checkConfigVersion();
 		log("==================================", false, Level.INFO);
 	}
 
 	/*
 	 * 
 	 * On plugin disable
 	 */
 	@Override
 	public void onDisable() {
 		// Get the logger
 		logger = Logger.getLogger("Minecraft.GuestUnlock");
 
 		// Get the plugin.yml
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		// Stop the running task
 		Bukkit.getServer().getScheduler().cancelTask(RunningTask.threadID);
 
 		// Msg:s to the log
 		log("stopped running task!", true, Level.INFO);
 		log("version " + pdfFile.getVersion() + " by Myller is now Disabled!",
 				false, Level.INFO);
 
 	}
 
 	public void log(String message, boolean debug, Level level) {
 		String prefix = "[GuestUnlock] ";
 		if (!debug) {
 			logger.log(level, prefix + message);
 		} else if (debug && isDebugEnabled) {
 			logger.log(level, prefix + message);
 		} else if (!isDebugEnabled && debug) {
 			return;
 		} else {
 			logger.log(level, prefix + message);
 		}
 	}
 }
