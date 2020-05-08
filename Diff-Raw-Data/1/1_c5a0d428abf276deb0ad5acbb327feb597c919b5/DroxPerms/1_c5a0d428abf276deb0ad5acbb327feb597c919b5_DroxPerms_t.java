 package de.hydrox.bukkit.DroxPerms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import org.bukkit.World;
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
 	private HashMap<Player, HashMap<String,PermissionAttachment>> permissions = new HashMap<Player, HashMap<String,PermissionAttachment>>();
 	private DroxPermsAPI API = null;
 
 	private Runnable commiter;
 	private ScheduledThreadPoolExecutor scheduler;
 
 	public Logger logger = Logger.getLogger("Minecraft");
 
 	public void onDisable() {
 		long time = System.currentTimeMillis();
 		logger.info("[DroxPerms] shutting down");
 		// Unregister everyone
 		logger.info("[DroxPerms] unregister Players");
 		for (Player p : getServer().getOnlinePlayers()) {
 			unregisterPlayer(p);
 		}
		disableScheduler();
 
 		// Safe data
 		logger.info("[DroxPerms] safe configs");
 		dataProvider.save();
 		logger.info("[DroxPerms] Plugin unloaded in " + (System.currentTimeMillis() - time) + "ms.");
 	}
 
 	public void onEnable() {
 		long time = System.currentTimeMillis();
 		logger.info("[DroxPerms] Activating Plugin.");
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
 		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
 
 		// Register everyone online right now
 		logger.info("[DroxPerms] Register online players");
 		for (Player p : getServer().getOnlinePlayers()) {
 			registerPlayer(p);
 		}
 
 		enableScheduler();
 
 		logger.info("[DroxPerms] Plugin activated in " + (System.currentTimeMillis() - time) + "ms.");
 	}
 	
 	public DroxPermsAPI getAPI() {
 		return API;
 	}
 
 	protected void registerPlayer(Player player) {
 		registerPlayer(player, player.getWorld());
 	}
 
 	protected void registerPlayer(Player player, World world) {
 		HashMap<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();
 
 		PermissionAttachment attachment = player.addAttachment(this);
 		attachments.put("subgroups", attachment);
 		attachment = player.addAttachment(this);
 		attachments.put("group", attachment);
 		attachment = player.addAttachment(this);
 		attachments.put("global", attachment);
 		attachment = player.addAttachment(this);
 		attachments.put("world", attachment);
 
 		permissions.put(player, attachments);
 		calculateAttachment(player, world);
 	}
 
 	protected void unregisterPlayer(Player player) {
 		HashMap<String, PermissionAttachment> attachments = permissions.get(player);
 		if (attachments != null)
 		for (PermissionAttachment attachment : attachments.values()) {
 			player.removeAttachment(attachment);
 		}
 		permissions.remove(player);
 	}
 
 	protected void refreshPermissions() {
 		getConfiguration().save();
 		for (Player player : permissions.keySet()) {
 			refreshPlayer(player);
 		}
 	}
 
 	protected void refreshPlayer(Player player) {
 		refreshPlayer(player, player.getWorld());
 	}
 
 	protected void refreshPlayer(Player player, World world) {
 		if (player == null) {
 			return;
 		}
 		HashMap<String, PermissionAttachment> attachments = permissions.get(player);
 		for (PermissionAttachment attachment : attachments.values()) {
 			for (String key : attachment.getPermissions().keySet()) {
 				attachment.unsetPermission(key);
 			}
 		}
 
 		calculateAttachment(player, world);
 	}
 
 	private void calculateAttachment(Player player, World world) {
 		HashMap<String, PermissionAttachment> attachments = permissions
 				.get(player);
 
 		PermissionAttachment attachment = attachments.get("group");
 		HashMap<String, ArrayList<String>> playerPermissions = dataProvider
 				.getPlayerPermissions(player.getName(), world.getName());
 		ArrayList<String> perms = playerPermissions.get("group");
 		if (perms != null)
 			for (String entry : playerPermissions.get("group")) {
 				if (entry.startsWith("-")) {
 					entry = entry.substring(1);
 					attachment.setPermission(entry, false);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to false for player " + player.getName());
 				} else {
 					attachment.setPermission(entry, true);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to true for player " + player.getName());
 				}
 			}
 		player.recalculatePermissions();
 
 		attachment = attachments.get("subgroups");
 		perms = playerPermissions.get("subgroups");
 		if (perms != null)
 			for (String entry : perms) {
 				if (entry.startsWith("-")) {
 					entry = entry.substring(1);
 					attachment.setPermission(entry, false);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to false for player " + player.getName());
 				} else {
 					attachment.setPermission(entry, true);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to true for player " + player.getName());
 				}
 			}
 		player.recalculatePermissions();
 
 		attachment = attachments.get("global");
 		perms = playerPermissions.get("global");
 		if (perms != null)
 			for (String entry : perms) {
 				if (entry.startsWith("-")) {
 					entry = entry.substring(1);
 					attachment.setPermission(entry, false);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to false for player " + player.getName());
 				} else {
 					attachment.setPermission(entry, true);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to true for player " + player.getName());
 				}
 			}
 		player.recalculatePermissions();
 
 		attachment = attachments.get("world");
 		perms = playerPermissions.get("world");
 		if (perms != null)
 			for (String entry : perms) {
 				if (entry.startsWith("-")) {
 					entry = entry.substring(1);
 					attachment.setPermission(entry, false);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to false for player " + player.getName());
 				} else {
 					attachment.setPermission(entry, true);
 					logger.fine("[DroxPerms] Setting " + entry
 							+ " to true for player " + player.getName());
 				}
 			}
 		player.recalculatePermissions();
 	}
 
 	private void enableScheduler() {
 		disableScheduler();
 		commiter = new DroxSaveThread(this);
 		scheduler = new ScheduledThreadPoolExecutor(1);
 		int minutes = Config.getSaveInterval();
 		scheduler.scheduleAtFixedRate(commiter, minutes, minutes, TimeUnit.MINUTES);
 		logger.info("[DroxPerms] Saving changes every " + minutes + " minutes!");
 	}
 
 	private void disableScheduler() {
 		if (scheduler != null) {
 			try {
 				scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
 				scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
 				scheduler.shutdown();
 			} catch (Exception e) {
 			}
 			scheduler = null;
 			logger.info("[DroxPerms] Deactivated Save-Thread.");
 		}
 	}
 }
