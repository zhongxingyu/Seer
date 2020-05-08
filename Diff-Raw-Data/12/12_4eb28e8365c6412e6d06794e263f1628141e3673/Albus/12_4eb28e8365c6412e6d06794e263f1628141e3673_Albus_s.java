 // Copyright (C) 2011 Antony Derham <admin@districtmine.net>
 
 package net.districtmine.ant59.albus;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class Albus extends JavaPlugin {
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	private final String PROP_KICKMESSAGE = "kick-message";
 	private final String PROP_WHITELIST_ADMINS = "whitelist-admins";
 	private final String PROP_DISABLE_LIST = "disable-list-command";
 	private final String PROP_MYSQL_HOST = "mysql-host";
 	private final String PROP_MYSQL_PORT = "mysql-port";
 	private final String PROP_MYSQL_USER = "mysql-user";
 	private final String PROP_MYSQL_PASS = "mysql-pass";
 	private final String PROP_MYSQL_DB = "mysql-db";
 	private final String PROP_MYSQL_USERS_TABLE = "mysql-table";
 	private final String PROP_GROUP_FIELD = "group-field";
 	private final String PROP_USERNAME_FIELD = "username-field";
 	private final String PROP_ALLOWED_GROUP_IDS = "allowed-group-ids";
 	private final String PROP_RELOAD_PERIOD = "reload-period";
 	private final String configurationFile = "albus.properties";
 
 	// Plugin
 	private String name;
 	private String version;
 	private PluginDescriptionFile pdfFile;
 
 	// Attributes
 	private final AlbusListener playerListner = new AlbusListener(this);
 	private Timer timer = new Timer(true);
 	private File albusFolder;
 	private ArrayList<String> allowed;
 	private String kickMessage;
 	private boolean m_IsWhitelistActive;
 	private boolean isActive;
 
 	// Database
 	private MySQL sql;
 
	public Albus(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 
 		albusFolder = folder;
 		kickMessage = "";
 		allowed = new ArrayList<String>();
 		m_IsWhitelistActive = true;
 		isActive = false;
 	}
 
 	class ReloaderTask extends TimerTask {
         public void run() {
         	loadWhitelistSettings();
         }
     }
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListner, Priority.Low, this);
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListner, Priority.Monitor, this);
 
 		this.pdfFile = this.getDescription();
 		this.name = this.pdfFile.getName();
 		this.version = this.pdfFile.getVersion();
 
 		// Create folders and files
 		if (!albusFolder.exists()) {
 			consoleLog("Config folder missing, creating...");
 			albusFolder.mkdir();
 			consoleLog("Folder created");
 		}
 
 		File fConfig = new File(albusFolder.getAbsolutePath() + File.separator + configurationFile);
 		if (!fConfig.exists()) {
 			consoleLog("Config is missing, creating...");
 			try {
 				fConfig.createNewFile();
 				Properties propConfig = new Properties();
 				propConfig.setProperty(PROP_KICKMESSAGE, "Sorry, you are not on the whitelist!");
 				propConfig.setProperty(PROP_WHITELIST_ADMINS, "Name1,Name2,Name3");
 				propConfig.setProperty(PROP_DISABLE_LIST, "false");
 				propConfig.setProperty(PROP_MYSQL_HOST, "localhost");
 				propConfig.setProperty(PROP_MYSQL_PORT, "3306");
 				propConfig.setProperty(PROP_MYSQL_USER, "user");
 				propConfig.setProperty(PROP_MYSQL_PASS, "pass");
 				propConfig.setProperty(PROP_MYSQL_DB, "forum");
 				propConfig.setProperty(PROP_MYSQL_USERS_TABLE, "smf_members");
 				propConfig.setProperty(PROP_GROUP_FIELD, "group_id");
 				propConfig.setProperty(PROP_USERNAME_FIELD, "name");
 				propConfig.setProperty(PROP_ALLOWED_GROUP_IDS, "1,2,3");
 				propConfig.setProperty(PROP_RELOAD_PERIOD, "300");
 				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fConfig.getAbsolutePath()));
 				propConfig.store(stream, "Automatically generated config file");
 				consoleLog("Configuration created");
 			} catch (IOException ex) {
 				consoleWarning("Configuration file creation failure");
 			}
 		}
 
 		consoleLog("Enabled!");
 	}
 
 	public void onDisable() {
 		timer.cancel();
 		timer.purge();
 		timer = null;
 		consoleLog("Goodbye!");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if (args.length < 1) {
 			return false;
 		}
 		if (args[0].compareToIgnoreCase("help") == 0) {
 			sender.sendMessage(ChatColor.YELLOW + "/albus reload  (force an update of the whitelist)");
 			sender.sendMessage(ChatColor.YELLOW	+ "/albus list  (lists everyone whitelisted)");
 			return true;
 		}
 		if (args[0].compareToIgnoreCase("reload") == 0) {
 			if (loadWhitelistSettings())
 				sender.sendMessage(ChatColor.GREEN + "Settings and whitelist reloaded");
 			else
 				sender.sendMessage(ChatColor.RED + "Could not reload...");
 			return true;
 		}
 		if (args[0].compareToIgnoreCase("list") == 0 && !isListCommandDisabled()) {
 			sender.sendMessage(ChatColor.YELLOW + "Allowed players: " + ChatColor.GRAY + getFormatedAllowList());
 			return true;
 		}
 		return false;
 	}
 
 	public boolean loadWhitelistSettings() {
 		consoleLog("Trying to load whitelist and settings...");
 		try {
 			// Load albus.properties
 			Properties propConfig = new Properties();
 			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(albusFolder.getAbsolutePath() + File.separator + configurationFile));
 			propConfig.load(stream);
 			timer.schedule(new ReloaderTask(), 0, Integer.parseInt(propConfig.getProperty(PROP_RELOAD_PERIOD))*1000);
 			kickMessage = propConfig.getProperty(PROP_KICKMESSAGE);
 			if (kickMessage == null) {
 				kickMessage = "";
 			}
 			String rawDisableListCommand = propConfig.getProperty(PROP_DISABLE_LIST);
 			if (rawDisableListCommand != null) {
 				isActive = Boolean
 						.parseBoolean(rawDisableListCommand);
 			}
 
 			// Load database configuration and connect...
 			this.sql = new MySQL(this, propConfig.getProperty(PROP_MYSQL_USER),
 					propConfig.getProperty(PROP_MYSQL_PASS),
 					propConfig.getProperty(PROP_MYSQL_HOST),
 					propConfig.getProperty(PROP_MYSQL_PORT),
 					propConfig.getProperty(PROP_MYSQL_DB));
 
 			// Load whitelist from database
 			allowed.clear();
 
 			ResultSet rs;
 			rs = sql.trySelect("SELECT " + propConfig.getProperty(PROP_USERNAME_FIELD) + " from " + propConfig.getProperty(PROP_MYSQL_USERS_TABLE) + " WHERE " + propConfig.getProperty(PROP_USERNAME_FIELD) + " IN(" + propConfig.getProperty(PROP_ALLOWED_GROUP_IDS) + ")");
 			while (rs.next() != false) {
 				String user = rs.getString(propConfig.getProperty(PROP_USERNAME_FIELD));
 				allowed.add(user);
 			}
 
 			consoleLog("Whitelist Loaded");
 		} catch (Exception ex) {
 			consoleWarning("Failed to load whitelist");
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isOnWhitelist(String playerName) {
 		for (String player : allowed) {
 			if (player.compareToIgnoreCase(playerName) == 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getKickMessage() {
 		return kickMessage;
 	}
 
 	public String getFormatedAllowList() {
 		String result = "";
 		for (String player : allowed) {
 			if (result.length() > 0) {
 				result += ", ";
 			}
 			result += player;
 		}
 		return result;
 	}
 
 	public boolean isWhitelistActive() {
 		return m_IsWhitelistActive;
 	}
 
 	public void setWhitelistActive(boolean isWhitelistActive) {
 		m_IsWhitelistActive = isWhitelistActive;
 	}
 
 	public boolean isListCommandDisabled() {
 		return isActive;
 	}
 
 	public void consoleLog(String msg) {
 		log.info("[" + name + "] v" + version + " - " + msg);
 	}
 
 	public void consoleWarning(String msg) {
 		log.warning("[" + name + "] v" + version + " - " + msg);
 	}
 
 	public void consoleError(String msg) {
 		log.severe("[" + name + "] v" + version + " - " + msg);
 	}
 }
