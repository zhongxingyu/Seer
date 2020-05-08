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
 	private HashMap<Player, PermissionAttachment> permissions = new HashMap<Player, PermissionAttachment>();
 
 	private Logger logger;
 
 	public void onDisable() {
 		// Unregister everyone
 		for (Player p : getServer().getOnlinePlayers()) {
 			unregisterPlayer(p);
 		}
 
 		// Safe data
 		dataProvider.save();
 	}
 
 	public void onEnable() {
 		logger = getServer().getLogger();
 		config = new Config(this);
 		if (Config.getDataProvider().equals(FlatFilePermissions.NODE)) {
 			dataProvider = new FlatFilePermissions(this);
 		}
 
         // Commands
         getCommand("changegroup").setExecutor(groupCommandExecutor);
         getCommand("changeplayer").setExecutor(playerCommandExecutor);
 
 		// Events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_KICK, playerListener, Priority.Normal, this);
 
 		// Register everyone online right now
 		for (Player p : getServer().getOnlinePlayers()) {
 			registerPlayer(p);
 		}
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
 		PermissionAttachment attachment = permissions.get(player);
 		for (String key : attachment.getPermissions().keySet()) {
 			attachment.unsetPermission(key);
 		}
 
 		calculateAttachment(player);
 	}
 
 	private void calculateAttachment(Player player) {
 		PermissionAttachment attachment = permissions.get(player);
 
 		for (String entry : dataProvider.getPlayerPermissions(player.getName().toLowerCase(), player.getWorld().getName())) {
 			if (entry.startsWith("-")) {
 				attachment.setPermission(entry, false);
 				logger.info("[DroxPerms] Setting " + entry + " to false for player " + player.getName());
 			} else {
 				attachment.setPermission(entry, true);
 				logger.info("[DroxPerms] Setting " + entry + " to true for player " + player.getName());
 			}
 		}
 
 		player.recalculatePermissions();
 	}
 }
