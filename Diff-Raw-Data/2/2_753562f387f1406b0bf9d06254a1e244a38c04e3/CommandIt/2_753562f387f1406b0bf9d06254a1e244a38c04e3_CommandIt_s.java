 package org.zone.commandit;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 import org.zone.commandit.config.Config;
 import org.zone.commandit.config.Messages;
 import org.zone.commandit.listener.CommandListener;
 import org.zone.commandit.listener.EventListener;
 import org.zone.commandit.thirdparty.Metrics;
 import org.zone.commandit.util.CodeLoader;
 import org.zone.commandit.util.LuaCode;
 import org.zone.commandit.util.MetricsLoader;
 import org.zone.commandit.util.PlayerState;
 import org.zone.commandit.util.Updater;
 
 public class CommandIt extends JavaPlugin {
     
 		// Listeners
 		private final EventListener listener = new EventListener(this);
 		public CommandListener commandExecutor = new CommandListener(this);
 
 		// Third-party
 		public Metrics metrics;
 
 		private Economy economy;
 		private Permission permission;
 
 		// Plugin variables
 		private Map<Location, LuaCode> cache = new HashMap<Location, LuaCode>();
 		private Map<OfflinePlayer, PlayerState> playerStates = new HashMap<OfflinePlayer, PlayerState>();
 		private Map<OfflinePlayer, LuaCode> playerCode = new HashMap<OfflinePlayer, LuaCode>();
 
 		private final CodeLoader loader = new CodeLoader(this);
 		private final Config config = new Config(this);
 		private final Messages messenger = new Messages(this);
 		private Updater updater;
 
 		// Class variables
 		private BukkitTask updateTask;
 
 		public File getUpdateFile() {
 			return new File(getServer().getUpdateFolderFile().getAbsoluteFile(),
 					super.getFile().getName());
 		}
 
 		/**
 		 * Complements Vault to finding whether a player has a given permission string
 		 * 
 		 * @param player
 		 * @param string Permission node in dotted format
 		 * @return
 		 */
 		public boolean hasPermission(CommandSender player, String string) {
 			return hasPermission(player, string, true);
 		}
 
 		/**
 		 * Complements Vault to finding whether a player has a given permission string
 		 * 
 		 * @param player
 		 * @param string Permission node in dotted format
 		 * @param notify True if nag message should be sent on failure
 		 * @return
 		 */
 		public boolean hasPermission(CommandSender player, String string,
 				boolean notify) {
 			boolean perm;
 			if (permission == null) {
 				perm = player.hasPermission(string);
 			} else {
 				perm = permission.has(player, string);
 			}
 			if (perm == false && notify) {
 				messenger.sendMessage(player, "failure.no_perms");
 			}
 			return perm;
 		}
 
 		/**
 		 * Load all required data for the plugin
 		 */
 		public void load() {
 			config.load();
 			messenger.load();
 			loader.loadFile();
 			setupPermissions();
 			setupEconomy();
 
 			updater = new Updater(this, this.getFile());
 			if (config.getBoolean("updater.auto-check") == true)
 				updater.init();
 
 			if (config.getBoolean("metrics.enable") == true)
 				MetricsLoader.factory(this);
 			else
 				getLogger().info(messenger.parseRaw("metrics.opt_out"));
 		}
 
         @Override
 		public void onDisable() {
 			if (updateTask != null)
 				updateTask.cancel();
 			loader.saveFile();
 		}
 
 		@Override
 		public void onEnable() {
 			load();
 			PluginManager pm = getServer().getPluginManager();
			getCommand("commandsigns").setExecutor(commandExecutor);
 			pm.registerEvents(listener, this);
 		}
 
 		/**
 		 * Find the resident economy system and set up Vault to use it.
 		 * @return True if successful
 		 */
 		public boolean setupEconomy() {
 			RegisteredServiceProvider<Economy> economyProvider = getServer()
 					.getServicesManager().getRegistration(Economy.class);
 			if (economyProvider != null) {
 				economy = economyProvider.getProvider();
 			}
 			return economy != null;
 		}
 
 		/**
 		 * Find the resident permission system and set up Vault to use it.
 		 * @return True if successful
 		 */
 		public boolean setupPermissions() {
 			RegisteredServiceProvider<Permission> permissionProvider = getServer()
 					.getServicesManager().getRegistration(Permission.class);
 			if (permissionProvider != null) {
 				permission = permissionProvider.getProvider();
 			}
 			return permission != null;
 		}
 		
 		/**
 		 * @return Plugin's configuration and settings handler
 		 */
 		public Config getPluginConfig() {
 			return config;
 		}
 		/**
 		 * @return Handler for loading code block data
 		 */
 		public CodeLoader getCodeLoader() {
 			return loader;
 		}
 		/**
 		 * @return Handler for sending messages and statuses to players
 		 */
 		public Messages getMessenger() {
 			return messenger;
 		}
 		/**
 		 * @return Handler for the updater system
 		 */
 		public Updater getUpdater() {
 			return updater;
 		}
 		
 		/**
 		 * @return All loaded code blocks on the server
 		 */
 		public Map<Location, LuaCode> getCodeBlocks() {
 			return cache;
 		}
 		/**
 		 * @return The states of all players on the server
 		 */
 		public Map<OfflinePlayer, PlayerState> getPlayerStates() {
 			return playerStates;
 		}
 		/**
 		 * @return The clipboards of all players on the server
 		 */
 		public Map<OfflinePlayer, LuaCode> getPlayerCode() {
 			return playerCode;
 		}
 		
 		/**
 		 * @return Vault's economy handler
 		 */
 		public Economy getEconomy() {
 			return economy;
 		}
 		/**
 		 * @return Vault's permission handler
 		 */
 		public Permission getPermissionHandler() {
 			return permission;
 		}
 }
