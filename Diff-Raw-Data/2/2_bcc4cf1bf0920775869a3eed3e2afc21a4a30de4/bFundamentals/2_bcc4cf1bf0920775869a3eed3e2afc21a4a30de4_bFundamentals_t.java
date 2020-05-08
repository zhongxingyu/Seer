 /**
  * bFundamentals 1.2-SNAPSHOT
  * Copyright (C) 2013  CodingBadgers <plugins@mcbadgercraft.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.codingbadgers.bFundamentals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 import uk.codingbadgers.bFundamentals.module.ModuleLoader;
 import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
 import uk.codingbadgers.bFundamentals.player.FundamentalPlayerArray;
 import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager;
 import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager.DatabaseType;
 import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
 
 public class bFundamentals extends JavaPlugin implements Listener {
 	
 	private static Logger m_log = null;
 	private static bFundamentals m_instance = null;
 	private static BukkitDatabase m_database = null;
 	
 	private static Permission m_permissions = null;
 	private static Chat m_chat = null;
 	private static Economy m_economy = null;
 	
 	private static ModuleLoader m_moduleLoader = null;
 	private static ConfigManager m_configuration = null;
 	
 	public static FundamentalPlayerArray Players = new FundamentalPlayerArray(); 
 	
 	/**
 	 * Called on loading. This is called before onEnable.
 	 * Store the instance here, to do it as early as possible.
 	 */
 	@Override
 	public void onLoad() {
 		setInstance(this);
 		m_log = getLogger();
 		log(Level.INFO, "bFundamentals Loading");
 	}
 	
 	/**
 	 * Called when the plugin is being enabled
 	 * Load the configuration and all modules
 	 * Register the command listener
 	 */
 	@Override
 	public void onEnable() {
 		
 		// load the configuration into the configuration manager
 		try {
 			setConfigManager(new BukkitConfigurationManager());
 			m_configuration.loadConfiguration(new File(getDataFolder(), "config.yml"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		// load the modules in
 		m_moduleLoader = new ModuleLoader();
 		m_moduleLoader.load();
 		m_moduleLoader.enable();
 		
 		// check if any of the modules need updating
 		if (m_configuration.isAutoUpdateEnabled()) {
 			m_moduleLoader.update();
 		}
 		
 		// Register this as a listener
 		this.getServer().getPluginManager().registerEvents(this, this);
 		
 		getCommand("bFundamentals").setExecutor(new CommandHandler());
 		
 		bFundamentals.log(Level.INFO, "bFundamentals Loaded.");
 	}
 
 	public static void setInstance(bFundamentals plugin) {
 		if (m_instance != null) {
 			throw new RuntimeException("Plugin instance already set, cannot redeclare");
 		}
 		m_instance = plugin;
 	}
 	
 	public void setConfigManager(ConfigManager manager) {
 		if (m_configuration != null) {
 			throw new RuntimeException("Configuration manager already set, cannot redeclare");
 		}
 		m_configuration = manager;
 	}
 
 	/**
 	 * Called when the plugin is being disabled
 	 * Here we disable the module and thus all modules
 	 */
 	@Override
 	public void onDisable() {
 		bFundamentals.log(Level.INFO, "bFundamentals Disabled.");
 		m_moduleLoader.disable();
 		m_database.freeDatabase();
 
 		// Clear instances
 		m_instance = null;
 		m_configuration = null;
 	}
 	
 	/**
 	 * Handle commands in the modules or plugin.
 	 * @return True if the command was handled, False otherwise
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if (label.equalsIgnoreCase("modules")) {
 			handleModulesCommand(sender);
 			return true;
 		}
 		
 		return false;
 	}
 	
 
 	/**
 	 * Disable a specific module
 	 * 
 	 * @param module the module to disable
 	 */
 	public void disableModule(Module module) {
 		Validate.notNull(module, "Moudule cannot be null");
 		
 		m_moduleLoader.unload(module);
 	}
 
 	/**
 	 * Reloads a specific module
 	 * 
 	 * @param module the module to reload
 	 */
 	public void reloadModule(Module module) {
 		Validate.notNull(module, "Moudule cannot be null");
 		
 		m_moduleLoader.unload(module);
 		m_moduleLoader.load(module.getFile());
 		m_moduleLoader.getModule(module.getName()).onEnable();
 	}
 
 	/**
 	 * Get the bFundamentals plugin instance.
 	 * @return the instance
 	 */
 	public static bFundamentals getInstance() {
 		return m_instance;
 	}
 	
 	/**
 	 * Static access to log as bFundamentals
 	 * 
 	 * @param level the log level
 	 * @param msg the message to log
 	 */
 	public static void log(Level level, String msg) {
 		Validate.notNull(level, "Log level cannot be null");
 		Validate.notNull(msg, "Message cannot be null");
 		
 		m_log.log(level, msg);
 	}
 
 	/**
 	 * Static access to log as bFundamentals
 	 * 
 	 * @param level the log level
 	 * @param msg the message to log
 	 * @param e the exception to log
 	 */
 	public static void log(Level level, String msg, Throwable e) {
 		Validate.notNull(level, "Log level cannot be null");
 		Validate.notNull(msg, "Message cannot be null");
 		Validate.notNull(e, "The exception to log cannot be null");
 		
 		m_log.log(level, msg, e);
 	}
 	
 	/**
 	 * Get the configuration manager
 	 * 
 	 * @return the configuration manager for bFundamentals
 	 */
 	public static ConfigManager getConfigurationManager() {
 		return m_configuration;
 	}
 	
 	/**
 	 * Access to the bukkit database
 	 * 
 	 * @return the bukkit database for bFundamentals
 	 */
 	public static BukkitDatabase getBukkitDatabase() {
 		if (m_database == null) {
 			DatabaseSettings settings = m_configuration.getDatabaseSettings();
			m_database = bDatabaseManager.createDatabase(settings.name, m_instance, settings.type);
 			if (settings.type == DatabaseType.SQL) {
 				m_database.login(settings.host, settings.user, settings.password, settings.port);
 			}
 		}
 		return m_database;
 	}
 	
 	/**
 	 * Static access to vaults permission manager
 	 * 
 	 * @return the vault permission manager
 	 * @see Permission
 	 */
 	public static Permission getPermissions() {
 		if (m_permissions == null) {
 			RegisteredServiceProvider<Permission> permissionProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		    if (permissionProvider != null) {
 		    	m_permissions = permissionProvider.getProvider();
 		    }
 		}
 	    return m_permissions;
 	}
 	
 	/**
 	 * Static access to vaults chat manager
 	 * 
 	 * @return the vault chat manager
 	 * @see Chat
 	 */
 	public static Chat getChat() {
 		if (m_chat == null) {
 			RegisteredServiceProvider<Chat> chatProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
 		    if (chatProvider != null) {
 		    	m_chat = chatProvider.getProvider();
 		    }
 		}
 	    return m_chat;
 	}
 	
 	/**
 	 * Static access to vaults economy manager
 	 * 
 	 * @return the vault economy manager
 	 * @see Economy
 	 */
 	public static Economy getEconomy() {
 		if (m_economy == null) {
 			RegisteredServiceProvider<Economy> economyProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		    if (economyProvider != null) {
 		    	m_economy = economyProvider.getProvider();
 		    }
 		}
 	    return m_economy;
 	}
 	
 	/**
 	 * Gets the module loader
 	 * 
 	 * @return the module loader for all bFundamentals modules
 	 */
 	public static ModuleLoader getModuleLoader(){	
 		return m_moduleLoader;
 	}
 
 	private void handleModulesCommand(CommandSender sender) {
 		List<Module> modules = m_moduleLoader.getModules();
 		String moduleString = ChatColor.GREEN + "Modules(" + modules.size() + "): ";
 		boolean first = true;
 		
 		for (Module module : modules) {
 			moduleString += (first ? "" : ", ") + module.getName();
 			first = false;
 		}
 		
 		sender.sendMessage(moduleString);
 	}
 
 	/**
 	 * Handle a player join event
 	 *
 	 * @param event The player join event
 	 */
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		FundamentalPlayer newPlayer = new FundamentalPlayer(event.getPlayer());
 		bFundamentals.Players.add(newPlayer);
 	}
 	
 	/**
 	 * Handle a player join event
 	 *
 	 * @param event The player join event
 	 */
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		bFundamentals.Players.removePlayer(event.getPlayer());
 	}
 		
 	/**
 	 * Get a list of online players for a given rank or group
 	 * 
 	 * @param rank	The rank to get the list of online players from
 	 * @return An array list of online players within a given rank or group
 	 */
 	public ArrayList<Player> getPlayersOfRank(String rank) {
 		
 		PermissionManager pexmanager = null;
 		
 		try {
 			pexmanager = PermissionsEx.getPermissionManager();
 		} catch (Exception ex) {
 			// If pex does not exist on the server, just return now, we don't want errors
 			return null;
 		}
 		
 		PermissionGroup group = pexmanager.getGroup(rank);
 		
 		// If the group doesn't exist just leave.
 		if (group == null) {
 			return null;
 		}
 				
 		// 
 		ArrayList<Player> players = new ArrayList<Player>();
 		for (PermissionUser user : group.getUsers()) {
 			Player player = Bukkit.getPlayer(user.getName());
 			if (player != null) {
 				players.add(player);
 			}
 		}
 		
 		return players;		
 	}
 	
 }
