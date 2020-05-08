 /*
  * GuestUnlock - a bukkit plugin
  * Copyright (C) 2013 Mylleranton
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
 
 package se.myllers.guestunlock;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
 	/**
 	 * The Main logger used to contact the console
 	 */
 	private static Logger		log;
 
 	/**
 	 * The Main config used to contact the config.yml
 	 * <p />
 	 * Initialized in onEnable()
 	 */
 	public static FileConfiguration	config;
 
 	/**
 	 * The Main PluginManager used to handle other plugins
 	 * <p />
 	 * Initialized in onEnable()
 	 */
 	public static PluginManager		pm;
 
 	/**
 	 * The current version of GU
 	 */
 	private static String			version;
 	
 	public static Permission 		perms;
 	
 	public static boolean hookedIntoVault = false;
 
 	/**
 	 * Called when the plugin is disabled
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
 	 */
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTask(RepeatingTask.threadId);
 		INFO("Cancelled repeating task, shutting down..");
 		pm = null;
 		config = null;
 		Listener.enableChat = false;
 		Listener.pluginVersion = null;
 		UpdateCheck.newestVersion = null;
 		UpdateCheck.version = null;
 		RepeatingTask.threadId = 0;
 		INFO("Nulled all static variables");
 	}
 
 	/**
 	 * Called when the plugin is enabled
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {
 		log = this.getLogger();
 		
 		config = getConfig();
 		DEBUG("Getting config");
 
 		DEBUG("Creating FileHandler now");
 		new FileHandler(this);
 
 		DEBUG("Registering events and getting PluginManager");
 		pm = getServer().getPluginManager();
 		pm.registerEvents(new Listener(), this);
 
 		DEBUG("Getting commands and setting Executor");
 		final CommandEx commandEx = new CommandEx();
 		getCommand("guestunlock").setExecutor(commandEx);
 		getCommand("gupassword").setExecutor(commandEx);
 	
		if(config.getBoolean("PermissionSystem.Vault.Enable")) {
 			if(hookIntoVault()) {
 				hookedIntoVault = true;
 				DEBUG("Sucessfully hooked into Vault!");
 			}
 		} 
 		else {
 			hookedIntoVault = false;
 			DEBUG("Did not hook into Vault");
 		}
 
 		DEBUG("Starting RepeatingTask");
 		new RepeatingTask(this);
 		version = getDescription().getVersion();
 
 		if (config.getBoolean("Admin.CheckForUpdate")) {
 			DEBUG("Checking for a new version, current version: " + Main.version);
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					new UpdateCheck(Main.version);
 				}
 			}, 120);
 		}
 
 		Listener.pluginVersion = getDescription().getVersion();
 
 		if (config.getBoolean("Admin.CheckForPass")) {
 			Listener.enableChat = true;
 		}
 	}
 
 	/**
 	 * Prints msg as Logging Level.INFO to the console
 	 * 
 	 * @param msg
 	 *            - Message to print
 	 */
 	public static final void INFO(final String msg) {
 		log.info(msg);
 	}
 
 	/**
 	 * Prints msg as Logging Level.WARNING to the console
 	 * 
 	 * @param msg
 	 *            - Message to print
 	 */
 	public static final void WARNING(final String msg) {
 		log.warning(msg);
 	}
 
 	/**
 	 * Prints msg as Logging Level.SEVERE to the console
 	 * 
 	 * @param msg
 	 *            - Message to print
 	 */
 	public static final void SEVERE(final String msg) {
 		log.severe(msg);
 	}
 
 	/**
 	 * Prints msg as Logging Level.INFO to the console, but contacts in DEBUG.
 	 * <p />
 	 * NOTE: This message will only be visible if Debug = true in the config
 	 * 
 	 * @param msg
 	 *            - Message to print
 	 */
 	public static final void DEBUG(final String msg) {
 		if (config.getBoolean("Admin.Debug")) {
 			log.info(" DEBUG: " + msg);
 		}
 	}
 	
 	private final boolean hookIntoVault() {
 	        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
 	        perms = rsp.getProvider();
 	        return perms != null;
 	}
 }
