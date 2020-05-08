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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import se.myller.GuestUnlock.Commands.CMDguestunlock;
 import se.myller.GuestUnlock.Commands.CMDgupassword;
 import se.myller.GuestUnlock.Commands.CMDgutest;
 import se.myller.GuestUnlock.Permission.PermGroupManager;
 import se.myller.GuestUnlock.Permission.PermPermissionsEx;
 import se.myller.GuestUnlock.Permission.PermbPermissions;
 
 
 
 /**
  * GuestUnlock for Bukkit
  * @author Myller
  */
 
 public class Main extends JavaPlugin {
 	
 		// Our variables:
 		public static Main plugin;
 		public Logger logger;
 		public FileConfiguration config;
 
 		public static int threadID = 0;
 		public static long messageInterval;
 
 		
 		public PluginManager pluginManager;
 		public GroupManager groupMan;
 		public PermGroupManager groupManager;
 		public PermbPermissions bPermissions;
 		public PermPermissionsEx permissionsEx;
     	public CMDguestunlock guestUnlock;
     	public CMDgupassword guPassword;
     	public CMDgutest guTest;
     	public UpdateCheck updateCheck;
     	
     	public boolean passwordCheck = false;
     	public boolean hasNewVersion = false;
     	private boolean isDebugEnabled;
     	public boolean isNewConfigAvailable = false;
    	private int newestConfigVersion = 5;
 	
     	public Main() {
     		guestUnlock = new CMDguestunlock(this);
     		guPassword = new CMDgupassword(this);
     		guTest = new CMDgutest(this);
     		updateCheck = new UpdateCheck(this);
     	}
 	/*
 	 * 
 	 * On plugin enable
 	 * 
 	 */
 	@Override
 	public void onEnable() {
 
 		// Get the logger
 		logger = Logger.getLogger("Minecraft.GuestUnlock");
 		
 		// Get the config
 		config = getConfig();
 		
 		// Debug
 		isDebugEnabled = config.getBoolean("Admin.Debug");
 		
 		//Check The Password
 		if (isDebugEnabled) {
 			checkPassword();
 		}
 		// Get the plugin.yml
 		PluginDescriptionFile pdfFile = this.getDescription();
 		
 		// Msg to the console
 		log("==================================", false, Level.INFO);
 		log("=====   Loading", true, Level.INFO);
 		log("=====   Checking password", true, Level.INFO);
 		if (passwordCheck == false) {
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("=====   Password is NOT OK!", true, Level.WARNING);
 			log("-------------------------------------------------", true, Level.INFO);
 			log("=====   Password MUST be a STRING!", true, Level.WARNING);
 			log("=====   Password set to default value: 'GuestUnlock'", true, Level.WARNING);
 			log("-------------------------------------------------", true, Level.INFO);
 		} else {
 			log("=====   Password OK", true, Level.INFO);
 		}
 		log("=====   Lets see if you want auto-group moving", true, Level.INFO);
 		
 		// Register our events
 		pluginManager = getServer().getPluginManager();
 		pluginManager.registerEvents(new JoinListener(this), this);
 		
 		// Make our config + directory
 		MakeDefaultConfig();
 		
 		// Start the timer
 		RepeatMessages();
 
 		// Register our commands
 		CommandExcecutor commandEx = new CommandExcecutor(this);
 		getCommand("guestunlock").setExecutor(commandEx);
 		getCommand("gupassword").setExecutor(commandEx);
 		getCommand("gutest").setExecutor(commandEx);
 		
 		// Check config for permissions support
 		if (config.getBoolean("Permissions.PermissionsEx.Enable") || config.getBoolean("Permissions.GroupManager.Enable") || config.getBoolean("Permissions.bPermissions.Enable")) {
 			log("=====   You wanted auto-group moving support!", true, Level.INFO);
 			
 			// Implement PEX
 			if (config.getBoolean("Permissions.PermissionsEx.Enable")) {
 				log("=====   Ill try to find PermissionsEx [PEX]!", true, Level.INFO);
 				permissionsEx = new PermPermissionsEx(this);
 				permissionsEx.getPex();
 			}
 			
 			// Implement GM
 			else if (config.getBoolean("Permissions.GroupManager.Enable")) {
 				log("=====   Ill try to find GroupManager [GM]!", true, Level.INFO);
 				groupManager = new PermGroupManager(this);
 				groupManager.getGM();
 				}
 			
 			// Implement bP
 			else if (config.getBoolean("Permissions.bPermissions.Enable")) {
 				log("=====   Ill try to find bPermissions [bP]!", true, Level.INFO);
 				bPermissions = new PermbPermissions(this);
 				bPermissions.getBP();
 				}
 		} else {
 			log("=====   You did not want auto-group moving enabled!", true, Level.INFO);
 		}
 		log("version " + pdfFile.getVersion() + " by Myller is now Enabled!", false, Level.INFO);
 		log("==================================", false, Level.INFO);
 		
 		// Check for updates
 		updateCheck.run();
 		
 		// Check config-version
 		checkConfigVersion();
 	}
 	/*
 	 * 
 	 * On plugin disable
 	 *
 	 */
 	@Override
 	public void onDisable() {
 		// Get the logger
 		logger = Logger.getLogger("Minecraft");
 		
 		// Get the plugin.yml
 		PluginDescriptionFile pdfFile = this.getDescription();
 		
 		// Stop the running task
 		Bukkit.getServer().getScheduler().cancelTask(threadID);
 		
 		// Msg:s to the log
 		log("stopped running task!", true, Level.INFO);
 		log("version " + pdfFile.getVersion() + " by Myller is now Disabled!", false, Level.INFO);
 		
 	}
 	public void checkPassword() {
 		config.getString("Admin.Password").toString();
 		if (config.isString("Admin.Password")) {
 			passwordCheck = true;
 			return;
 		} else {
 			config.set("Admin.Password", "GuestUnlock");
 			this.saveConfig();
 			passwordCheck = false;
 			return;
 		}
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
 	/*
 	 * 
 	 * Make our config + directory
 	 * 
 	 */
 	public void MakeDefaultConfig() {
 		try {			
 			File configFile = new File("plugins/GuestUnlock/config.yml");
 			if(!configFile.exists()) {
 				new File("plugins/GuestUnlock").mkdir();
 				this.saveDefaultConfig();
 				log("=====   Configuration file and directory created!", false, Level.INFO);
 			} else {
 				log("=====   Configuration file loaded!", true, Level.INFO);
 			}
 		} catch(Exception e) {	
 			log("=====   Configuration file failed to load!", false, Level.SEVERE);
 			log("=====   Disabling Plugin.", false, Level.SEVERE);
 			getServer().getPluginManager().disablePlugin(this);
 		}
 	}
 	/*
 	 * 
 	 * Start the task
 	 * 
 	 */
 	public void RepeatMessages() {
 		messageInterval = config.getInt("Guest.RepeatingMessage.Interval") * 20;
 		threadID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					SendGuestMessage();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}, 0, messageInterval);
 	}
 	/*
 	 * 
 	 * The running task
 	 * 
 	 */
 	public void SendGuestMessage() throws IOException {
 		Player[] players = getServer().getOnlinePlayers();
 		for (Player player: players) {
 			if (player.hasPermission("GuestUnlock.guest") && !player.hasPermission("GuestUnlock.moderator")) {
 					if (config.getBoolean("Guest.RepeatingMessage.UseJoinMessage") == true) { 
 						player.sendMessage(ChatColor.GREEN + config.getString("Guest.Join.Message"));
 					} else {
 						player.sendMessage(ChatColor.GREEN + config.getString("Guest.RepeatMessage.RepeatMessage"));
 					}
 			}
 		}
 	}
 	public void checkConfigVersion() {
 		int currentConfigVersion = config.getInt("Configuration-Version");
 		if (currentConfigVersion < newestConfigVersion) {
 			log("There is a new config-version available, you are currently using v" + currentConfigVersion + ", the latest is v" + newestConfigVersion, false, Level.INFO);
 			isNewConfigAvailable = true;
 		}
 	}
 }
 
 
 
 	
 	
 
 
