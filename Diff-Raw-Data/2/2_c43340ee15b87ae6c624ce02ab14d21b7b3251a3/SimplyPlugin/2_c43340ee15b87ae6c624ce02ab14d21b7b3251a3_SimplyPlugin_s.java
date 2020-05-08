 package net.crystalyx.bukkit.simplyperms;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.crystalyx.bukkit.simplyperms.io.ConfigFile;
 import net.crystalyx.bukkit.simplyperms.io.ConfigSQL;
 import net.crystalyx.bukkit.simplyperms.io.PermsConfig;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimplyPlugin extends JavaPlugin {
 
 	protected PermsConfig config;
 	private SimplyPlayer playerListener = new SimplyPlayer(this);
 	private SimplyCommands commandExecutor = new SimplyCommands(this);
 	private HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();
 
 	private File configFile;
 	private YamlConfiguration YamlConfig;
 
 	// -- Basic stuff
 	@Override
 	public void onEnable() {
 		// Disable PermissionsBukkit to avoid bugs
 		if (getServer().getPluginManager().isPluginEnabled("PermissionsBukkit")) {
 			for (Permission perm : getServer().getPluginManager().getPlugin("PermissionsBukkit").getDescription().getPermissions()) {
 				getServer().getPluginManager().removePermission(perm);
 			}
 			getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("PermissionsBukkit"));
 		}
 
 		// Take care of configuration
 		configFile = new File(getDataFolder(), "config.yml");
 		if (!configFile.exists()) {
 			saveDefaultConfig();
 		}
 		reloadConfig();
 
 		// Register stuff
 		getCommand("permissions").setExecutor(commandExecutor);
 		getServer().getPluginManager().registerEvents(playerListener, this);
 		registerEvents();
 
 		// Register everyone online right now
 		for (Player p : getServer().getOnlinePlayers()) {
 			registerPlayer(p);
 		}
 
 		// How are you gentlemen
 		getLogger().info("Enabled successfully, " + getServer().getOnlinePlayers().length + " players registered");
 	}
 
 	@Override
 	public FileConfiguration getConfig() {
 		return YamlConfig;
 	}
 
 	@Override
 	public void reloadConfig() {
 		YamlConfig = new YamlConfiguration();
 		YamlConfig.options().pathSeparator('/');
 		try {
 			YamlConfig.load(configFile);
 		} catch (Exception e) {
 			getLogger().severe("Unable to load configuration!");
 		}
 
 		// Init DB
 		initDatabase();
 	}
 
 	@Override
 	public void onDisable() {
 		// Unregister everyone
 		for (Player p : getServer().getOnlinePlayers()) {
 			unregisterPlayer(p);
 		}
 
 		// Good day to you! I said good day!
 		getLogger().info("Disabled successfully, " + getServer().getOnlinePlayers().length + " players unregistered");
 	}
 
 	private void initDatabase() {
 		if (!getConfig().getString("db/type", "file").equals("file")) {
 			ConfigSQL configsql = new ConfigSQL(this);
 			if (configsql.checkDatabase()) {
 				config = configsql;
 			} else {
 				getLogger().info("Fail to connect to database !");
 				config = new ConfigFile(this);
 			}
 		}
 		else {
 			config = new ConfigFile(this);
 		}
 	}
 
 	public SimplyAPI getAPI() {
 		return new SimplyAPI(this);
 	}
 
 	public List<String> getKeys(YamlConfiguration config, String node) {
 		if (config.isConfigurationSection(node)) {
 			return new ArrayList<String>(config.getConfigurationSection(node).getKeys(false));
 		}
 		else {
 			return new ArrayList<String>();
 		}
 	}
 
 	// -- Plugin stuff
 
 	protected void registerPlayer(Player player) {
 		if (permissions.containsKey(player.getName())) {
 			debug("Registering " + player.getName() + ": was already registered");
 			unregisterPlayer(player);
 		}
 		PermissionAttachment attachment = player.addAttachment(this);
 		permissions.put(player.getName(), attachment);
 		calculateAttachment(player);
 	}
 
 	protected void unregisterPlayer(Player player) {
 		if (permissions.containsKey(player.getName())) {
 			try {
 				player.removeAttachment(permissions.get(player.getName()));
 			}
 			catch (IllegalArgumentException ex) {
 				debug("Unregistering " + player.getName() + ": player did not have attachment");
 			}
 			permissions.remove(player.getName());
 		} else {
 			debug("Unregistering " + player.getName() + ": was not registered");
 		}
 	}
 
 	protected void refreshConfig() {
 		try {
 			getConfig().save(configFile);
 			reloadConfig();
 		} catch (IOException e) {
 			getLogger().warning("Failed to write changed config.yml: " + e.getMessage());
 		}
 	}
 
 	protected void refreshPlayerPermissions(String player) {
 		refreshConfig();
		PermissionAttachment attachment = permissions.get(player);
 		for (String key : attachment.getPermissions().keySet()) {
 			attachment.unsetPermission(key);
 		}
 		calculateAttachment(getServer().getPlayer(player));
 	}
 
 	protected void refreshPermissions() {
 		refreshConfig();
 		for (String player : permissions.keySet()) {
 			refreshPlayerPermissions(player);
 		}
 	}
 
 	public ConfigurationSection getNode(String node) {
 		for (String entry : getConfig().getKeys(true)) {
 			if (node.equalsIgnoreCase(entry) && getConfig().isConfigurationSection(entry)) {
 				return getConfig().getConfigurationSection(entry);
 			}
 		}
 		return null;
 	}
 
 	public void debug(String message) {
 		if (config.getDebug()) {
 			getLogger().info("Debug: " + message);
 		}
 	}
 
 	protected void calculateAttachment(Player player) {
 		if (player == null) {
 			return;
 		}
 		PermissionAttachment attachment = permissions.get(player.getName());
 		if (attachment == null) {
 			debug("Calculating permissions on " + player.getName() + ": attachment was null");
 			return;
 		}
 
 		for (String key : attachment.getPermissions().keySet()) {
 			attachment.unsetPermission(key);
 		}
 
 		for (Map.Entry<String, Boolean> entry : calculatePlayerPermissions(player.getName().toLowerCase(), player.getWorld().getName()).entrySet()) {
 			attachment.setPermission(entry.getKey(), entry.getValue());
 		}
 
 		player.recalculatePermissions();
 	}
 
 	// -- Private stuff
 
 	private Map<String, Boolean> calculatePlayerPermissions(String player, String world) {
 		String default_group = config.getDefaultGroup();
 		if (!config.isPlayerInDB(player)) {
 			return calculateGroupPermissions(default_group, world);
 		}
 
 		Map<String, Boolean> perms = config.getPlayerPermissions(player);
 		List<String> groups = config.getPlayerGroups(player);
 		if (groups.isEmpty()) groups.add(default_group);
 		// No containskey; world overrides non-world
 		perms.putAll(config.getPlayerPermissions(player, world));
 
 		for (String group : groups) {
 			for (Map.Entry<String, Boolean> entry : calculateGroupPermissions(group, world).entrySet()) {
 				if (!perms.containsKey(entry.getKey())) { // User overrides group
 					perms.put(entry.getKey(), entry.getValue());
 				}
 			}
 		}
 
 		return perms;
 	}
 
 	private Map<String, Boolean> calculateGroupPermissions(String group, String world) {
 		if (getNode("groups/" + group) == null) {
 			return new HashMap<String, Boolean>();
 		}
 
 		Map<String, Boolean> perms = config.getGroupPermissions(group);
 		// No containskey; world overrides non-world
 		perms.putAll(config.getGroupPermissions(group, world));
 
 		for (String parent : config.getGroupInheritance(group)) {
 			for (Map.Entry<String, Boolean> entry : calculateGroupPermissions(parent, world).entrySet()) {
 				if (!perms.containsKey(entry.getKey())) { // Children override permissions
 					perms.put(entry.getKey(), entry.getValue());
 				}
 			}
 		}
 
 		return perms;
 	}
 
 	private void registerEvents() {
 		String path = getDescription().getMain().substring(0, getDescription().getMain().lastIndexOf('.'));
 		for (String prevent : SimplyPrevents.preventions) {
 			try {
 				getServer().getPluginManager().registerEvents((SimplyPrevents) Class.forName(path + ".preventions." + prevent).getDeclaredConstructor(SimplyPlugin.class).newInstance(this), this);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
