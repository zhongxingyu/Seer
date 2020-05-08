 package de.hydrox.bukkit.DroxPerms;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.hydrox.bukkit.DroxPerms.data.Config;
 import de.hydrox.bukkit.DroxPerms.data.IDataProvider;
 import de.hydrox.bukkit.DroxPerms.data.flatfile.FlatFilePermissions;
 
 /**
  * Base Class of DroxPerms
  * 
  * @author Matthias Söhnholz
  */
 public class DroxPerms extends JavaPlugin {
 	protected Config config;
 	protected IDataProvider dataProvider;
 
 	private DroxPlayerListener playerListener = new DroxPlayerListener(this);
     private DroxGroupCommands groupCommandExecutor = new DroxGroupCommands(this);
     private DroxPlayerCommands playerCommandExecutor = new DroxPlayerCommands(this);
     private DroxTestCommands testCommandExecutor = new DroxTestCommands();
 	private HashMap<Player, PermissionAttachment> permissions = new HashMap<Player, PermissionAttachment>();
 	private DroxPermsAPI API = null;
 
 	private Logger logger = Logger.getLogger("Minecraft");
 
 	public void onDisable() {
 		long time = System.currentTimeMillis();
 		logger.info("[DroxPerms] shutting down");
 		// Unregister everyone
 		logger.info("[DroxPerms] unregister Players");
 		for (Player p : getServer().getOnlinePlayers()) {
 			unregisterPlayer(p);
 		}
 
 		// Safe data
 		logger.info("[DroxPerms] safe configs");
 		dataProvider.save();
 		logger.info("[DroxPerms] Plugin unloaded in " + (System.currentTimeMillis() - time) + "ms.");
 	}
 
 	public void onEnable() {
 		long time = System.currentTimeMillis();
 		logger.info("[DroxPerms] Activating Plugin.");
 		logger = getServer().getLogger();
 		config = new Config(this);
 		logger.info("[DroxPerms] Loading DataProvider");
 		if (Config.getDataProvider().equals(FlatFilePermissions.NODE)) {
 			dataProvider = new FlatFilePermissions(this);
 		}
 		
 		API = new DroxPermsAPI(this);
 
 		// Commands
 		logger.info("[DroxPerms] Setting CommandExecutors");
 		getCommand("changegroup").setExecutor(groupCommandExecutor);
 		getCommand("changeplayer").setExecutor(playerCommandExecutor);
 		getCommand("testdroxperms").setExecutor(testCommandExecutor);
 
 		// Events
 		logger.info("[DroxPerms] Registering Events");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_KICK, playerListener, Priority.Normal, this);
 
 		// Register everyone online right now
 		logger.info("[DroxPerms] Register online players");
 		for (Player p : getServer().getOnlinePlayers()) {
 			registerPlayer(p);
 		}
 		logger.info("[DroxPerms] Plugin activated in " + (System.currentTimeMillis() - time) + "ms.");
 	}
 	
 	public DroxPermsAPI getAPI() {
 		return API;
 	}
 
 	protected void registerPlayer(Player player) {
 		PermissionAttachment attachment = player.addAttachment(this);
 		permissions.put(player, attachment);
 		calculateAttachment(player);
 	}
 
 	protected void unregisterPlayer(Player player) {
 		player.removeAttachment(permissions.get(player));
 		permissions.remove(player);
 	}
 
 	protected void refreshPermissions() {
 		getConfiguration().save();
 		for (Player player : permissions.keySet()) {
 			refreshPlayer(player);
 		}
 	}
 
 	protected void refreshPlayer(Player player) {
 		if (player == null) {
 			return;
 		}
 		PermissionAttachment attachment = permissions.get(player);
 		for (String key : attachment.getPermissions().keySet()) {
 			attachment.unsetPermission(key);
 		}
 
 		calculateAttachment(player);
 	}
 
 	private void calculateAttachment(Player player) {
 		PermissionAttachment attachment = permissions.get(player);
 
		for (String entry : dataProvider.getPlayerPermissions(player.getName(), player.getWorld().getName())) {
 			if (entry.startsWith("-")) {
 				attachment.setPermission(entry, false);
 				logger.fine("[DroxPerms] Setting " + entry + " to false for player " + player.getName());
 			} else {
 				attachment.setPermission(entry, true);
 				logger.fine("[DroxPerms] Setting " + entry + " to true for player " + player.getName());
 			}
 		}
 
 		player.recalculatePermissions();
 	}
 }
