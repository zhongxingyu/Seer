 package eu.icecraft.iceauth;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 //import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class IceAuth extends JavaPlugin {
 
 	public String logPrefix = "[IceAuth] ";
 	public Logger log = Logger.getLogger("Minecraft");
 	public mysqlCore manageMySQL;
 	public sqlCore manageSQLite;
 
 	public Boolean MySQL = false;
 	public String dbHost = null;
 	public String dbUser = null;
 	public String dbPass = null;
 	public String dbDatabase = null;
 	public String tableName;
 
	private ArrayList<Player> playersLoggedIn = new ArrayList<Player>();
	private ArrayList<Player> notRegistered = new ArrayList<Player>();
 	//private ArrayList<Player> notLoggedIn = new ArrayList<Player>();
 	//private Map<Player, Location> notLoggedIn = new HashMap<Player, Location>();
	private Map<Player, NLIData> notLoggedIn = new HashMap<Player, NLIData>();
 	private boolean useSpout;
 	//private Permissions perm;
 	//private boolean UseOP;
 	private Thread thread;
 	private String userField;
 	private String passField;
 	private MessageDigest md5;
 
 
 	@Override
 	public void onDisable() {
 
 		try {
 			thread.interrupt();
 			thread.join();
 		} catch (InterruptedException ex) {
 			ex.printStackTrace();
 		}
 
 		System.out.println(this.getDescription().getName() + " " + this.getDescription().getVersion() + " was disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 
 		PluginManager pm = getServer().getPluginManager();
 
 		// TODO: Permissions
 		/*
 		Plugin perms = pm.getPlugin("Permissions");
 
 		if (perms != null) {
 			if (!pm.isPluginEnabled(perms)) {
 				pm.enablePlugin(perms);
 			}
 			perm = (Permissions) perms;
 		} else {
 			UseOP  = true;
 		}
 		 */
 
 		Plugin spLoaded = pm.getPlugin("Spout");
 
 		if (spLoaded != null && pm.isPluginEnabled(spLoaded)) {
 			System.out.println("[IceAuth] Found Spout, using inventory events.");
 			useSpout = true;
 		} else {
 			System.out.println("[IceAuth] WARNING! Spout not found, inventories are unprotected!");
 		}
 
 		if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
 		File confFile = new File(this.getDataFolder(), "config.yml");
 		Configuration conf = new Configuration(confFile);
 		if(!confFile.exists()) {
 
 			conf.setProperty("mysql.use", false);
 			conf.setProperty("mysql.dbHost", "localhost");
 			conf.setProperty("mysql.dbUser", "root");
 			conf.setProperty("mysql.dbPass", "");
 			conf.setProperty("mysql.database", "minecraft");
 			conf.setProperty("mysql.tableName", "auth");
 			conf.setProperty("mysql.userField", "username");
 			conf.setProperty("mysql.passField", "password");
 			conf.save();
 
 		}
 		conf.load();
 
 		this.MySQL = conf.getBoolean("mysql.use", false);
 		this.dbHost = conf.getString("mysql.dbHost");
 		this.dbUser = conf.getString("mysql.dbUser");
 		this.dbPass = conf.getString("mysql.dbPass");
 		this.dbDatabase = conf.getString("mysql.database");
 		this.tableName = conf.getString("mysql.tableName");
 		this.userField = conf.getString("mysql.userField");
 		this.passField = conf.getString("mysql.passField");
 
 		if (this.MySQL) {
 			if (this.dbHost.equals(null)) { this.MySQL = false; this.log.severe(this.logPrefix + "MySQL is on, but host is not defined, defaulting to SQLite"); }
 			if (this.dbUser.equals(null)) { this.MySQL = false; this.log.severe(this.logPrefix + "MySQL is on, but username is not defined, defaulting to SQLite"); }
 			if (this.dbPass.equals(null)) { this.MySQL = false; this.log.severe(this.logPrefix + "MySQL is on, but password is not defined, defaulting to SQLite"); }
 			if (this.dbDatabase.equals(null)) { this.MySQL = false; this.log.severe(this.logPrefix + "MySQL is on, but database is not defined, defaulting to SQLite"); }
 		}
 
 		if (this.MySQL) {
 
 			this.manageMySQL = new mysqlCore(this.log, this.logPrefix, this.dbHost, this.dbDatabase, this.dbUser, this.dbPass);
 
 			this.log.info(this.logPrefix + "MySQL Initializing");
 
 			this.manageMySQL.initialize();
 
 			try {
 				if (this.manageMySQL.checkConnection()) {
 					this.log.info(this.logPrefix + "MySQL connection successful");
 					if (!this.manageMySQL.checkTable(tableName)) {
 						this.MySQL = false;
 					}
 				} else {
 					this.log.severe(this.logPrefix + "MySQL connection failed. Defaulting to SQLite");
 					this.MySQL = false;
 					this.tableName = "auth";
 					this.userField = "username";
 					this.passField = "password";
 				}
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			this.log.info(this.logPrefix + "SQLite Initializing");
 
 			this.manageSQLite = new sqlCore(this.log, this.logPrefix, "IceAuth", this.getDataFolder().getPath());
 
 			this.tableName = "auth";
 			this.userField = "username";
 			this.passField = "password";
 			
 			this.manageSQLite.initialize();
 
 			if (!this.manageSQLite.checkTable(tableName)) {
 
 				this.manageSQLite.createTable("CREATE TABLE auth (id INT AUTO_INCREMENT PRIMARY_KEY, username VARCHAR(30), password VARCHAR(50));");
 
 			}
 
 		}
 
 		try {
 			this.md5 = MessageDigest.getInstance("MD5");
 		} catch(NoSuchAlgorithmException ex) {
 			ex.printStackTrace();
 		}
 
 		IceAuthPlayerListener playerListener = new IceAuthPlayerListener(this);
 		IceAuthBlockListener blockListener = new IceAuthBlockListener(this);
 		IceAuthEntityListener entityListener = new IceAuthEntityListener(this);
 		if(useSpout) {
 			IceAuthSpoutListener spoutListener = new IceAuthSpoutListener(this);
 			pm.registerEvent(Event.Type.CUSTOM_EVENT, spoutListener, Priority.Highest, this);
 		}
 
 		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Highest, this);
 		//pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this); pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.High, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.High, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Highest, this);
 
 		thread = new Thread(new PlayerThread(this));
 		thread.start();
 
 		System.out.println("IceAuth v1.0 has been enabled. Forked thread: "+thread.getName());
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if(commandLabel.equalsIgnoreCase("register")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 
 			Player player = (Player) sender;
 			if(!checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "Already registered.");
 				return false;
 			}
 
 
 			if(args.length != 1) {
 				player.sendMessage("Usage: /register <password>");
 				return false;
 			}
 
 
 			String password = args[0];
 
 			if(!register(player.getName(), password)) {
 				player.sendMessage(ChatColor.RED + "Something failed?");
 				return false;
 			}
 
 
 			player.sendMessage(ChatColor.GREEN + "Registered successfully! Use /login <password>");
 
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("login") || commandLabel.equalsIgnoreCase("l")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 
 			if(args.length != 1) {
 				player.sendMessage("Usage: /login <password>");
 				return false;
 			}
 
 			String playername = player.getName();
 			String password = args[0];
 
 			if(checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "You need to register first!");
 				return false;
 			}
 
 			if(checkAuth(player)) {
 				player.sendMessage(ChatColor.RED + "Already logged in!");
 				return false;
 			}
 
 			if(checkLogin(playername, password)) {
 				player.sendMessage(ChatColor.GREEN + "Logged in successfully");
 				delPlayerNotLoggedIn(player);
 				addAuthPlayer(player);
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED + "Wrong password!");
 				return false;
 			}
 
 		}
 
 
 		if(commandLabel.equalsIgnoreCase("changepassword")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 
 			Player player = (Player) sender;
 
 			if(checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "You aren't registered!");
 				return false;
 			}
 			if(!checkAuth(player)) {
 				player.sendMessage(ChatColor.RED + "You aren't logged in!");
 				return false;
 			}
 			if(args.length != 2) {
 				player.sendMessage("Usage: /changepassword <oldpassword> <newpassword>");
 				return false;
 			}
 
 			changePassword(args[0], args[1], player);
 			return true;
 
 		}
 
 		return false;
 
 	}
 
 	public void addAuthPlayer(Player player) {
		playersLoggedIn.add(player);	
 	}
 
 	public boolean checkAuth(Player player) {
 		return playersLoggedIn.contains(player);
 	}
 
 	public boolean checkUnReg(Player player) {
 		return notRegistered.contains(player);
 	}
 
 	public void removePlayerCache(Player player) {
 		playersLoggedIn.remove(player);
 	}
 
 	public void addPlayerNotLoggedIn(Player player, Location loc, Boolean registered) {
 		NLIData nli = new NLIData(loc, (int) (System.currentTimeMillis() / 1000L));
		notLoggedIn.put(player, nli);
 		
		if(!registered) notRegistered.add(player);
 	}
 
 	public void delPlayerNotLoggedIn(Player player) {
 		notLoggedIn.remove(player);	
 		if(notRegistered.contains(player)) notRegistered.remove(player);
 	}
 
 	public void msgPlayerLogin(Player player) {
 		if(checkUnReg(player)) {
 			player.sendMessage(ChatColor.RED + "Use /register <password> to register!");
 		} else {
 			player.sendMessage(ChatColor.RED + "Use /login <password> to log in!");
 		}
 	}
 
 	public String getMD5(String message) {
 		byte[] digest;
 		md5.reset();
 		md5.update(message.getBytes());
 		digest = md5.digest();
 
 		return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
 				digest));
 	}
 
 	public boolean isRegistered(String name) {
 
 		ResultSet result = null;
 
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM "+tableName+" WHERE " + userField + " = ?");
 			regQ.setString(1, name);
 			result = regQ.executeQuery();
 			while(result.next()) {
 				if(result.getInt("c") > 0) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return false;
 	}
 
 	public boolean checkLogin(String name, String password) { // fails at sqlite
 
 		ResultSet result = null;
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 		try {
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM "+tableName+" WHERE " + userField + " = ? && "+passField+" = ?");
 			regQ.setString(1, name);
 			regQ.setString(2, getMD5(password));
 			result = regQ.executeQuery();
 			while(result.next()) {
 				if(result.getInt("c") > 0) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 
 		return false;
 	}
 
 	public boolean register(String name, String password) {
 
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 		try {
 			PreparedStatement regQ = connection.prepareStatement("INSERT INTO "+tableName+" ("+userField+", "+passField+") VALUES(?,?)");
 			regQ.setString(1, name);
 			regQ.setString(2, getMD5(password));
 			regQ.executeUpdate();
 
 			return true;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public boolean changePassword(String oldpass, String password, Player player) {
 
 		if(checkLogin(player.getName(), oldpass)) {
 
 			Connection connection = null;
 
 			if (this.MySQL) {
 				try {
 					connection = this.manageMySQL.getConnection();
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			} else {
 				connection = this.manageSQLite.getConnection();
 			}
 			try {
 
 				PreparedStatement regQ = connection.prepareStatement("UPDATE "+tableName+" SET " + passField + " = ? WHERE " + userField + " = ?");
 				regQ.setString(1, getMD5(password));
 				regQ.setString(2, player.getName());
 				regQ.executeUpdate();
 
 				player.sendMessage(ChatColor.GREEN + "Password updated sucessfully!");
 
 				return true;
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 		} else {
 			player.sendMessage(ChatColor.RED + "Wrong password!");
 			return true;
 		}
 
 		return false;
 	}
 
 	public void tpPlayers(boolean msgLogin) {
 
		Set<Player> ks = notLoggedIn.keySet();
		for (Player player : ks) {
 
			NLIData nli = notLoggedIn.get(player);
 			Location pos = nli.getLoc();
 			
 			if((int) (System.currentTimeMillis() / 1000L) - nli.getLoggedSecs() > 30) {
				player.kickPlayer("Took to long to log in");
 				continue;
 			}
 			
 			player.teleport(pos);
 
 			if(msgLogin) {
 				msgPlayerLogin(player);
 			}
 
 		}
 
 	}
 
 	public class NLIData {
 		private int loggedSecs;
 		private Location loc;
 
 		public NLIData(Location loc, int loggedSecs) {
 			this.loc = loc;
 			this.loggedSecs = loggedSecs;
 		}
 		
 		public Location getLoc() {
 			return this.loc;
 		}
 		
 		public int getLoggedSecs() {
 			return this.loggedSecs;
 		}
 	}
 	
 }
