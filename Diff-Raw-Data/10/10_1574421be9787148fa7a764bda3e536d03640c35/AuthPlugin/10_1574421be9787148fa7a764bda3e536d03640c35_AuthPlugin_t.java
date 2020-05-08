 package fr.skyost.auth;
 
 import java.io.IOException;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import fr.skyost.auth.listeners.CommandsExecutor;
 import fr.skyost.auth.listeners.EventsListener;
 import fr.skyost.auth.tasks.SkyauthTasks;
 import fr.skyost.auth.utils.Metrics;
 import fr.skyost.auth.utils.Updater;
 
 public class AuthPlugin extends JavaPlugin {
 	
 	public static ConfigFile config;
 	public static MessagesFile messages;
 	
 	private static Statement stat;
 	private static boolean useMySQL;
 	
 	public static final HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();
 	public static final HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>();
 	public static final HashMap<String, String> sessions = new HashMap<String, String>();
 	public static final HashMap<String, Integer> tried = new HashMap<String, Integer>();
 	
 	@Override
 	public final void onEnable() {
 		try {
 			init();
 			startMetrics();
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	@Override
 	public final void onDisable() {
 		try {
 			reload();
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private final void init() throws Exception {
 		config = new ConfigFile(this);
 		config.init();
 		messages = new MessagesFile(this);
 		messages.init();
 		MySQLFile mysql = new MySQLFile(this);
 		mysql.init();
 		useMySQL = mysql.MySQL_Use;
 		Bukkit.getPluginManager().registerEvents(new EventsListener(), this);
 		if(config.CheckForUpdates) {
 			new Updater(this, 65625, this.getFile(), Updater.UpdateType.DEFAULT, true);
 		}
 		if(config.SessionLength <= 0) {
 			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Skyauth] SessionLength must be positive !");
 		}
 		if(config.ForgiveDelay <= 0) {
 			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Skyauth] ForgiveDelay must be positive !");
 		}
 		if(config.ReloadDelay <= 0) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Skyauth] ReloadDelay must be positive !");
		}
		if(mysql.MySQL_Use) {
			stat = DriverManager.getConnection("jdbc:mysql://" + mysql.MySQL_Host + ":" + mysql.MySQL_Port + "/" + mysql.MySQL_Database, mysql.MySQL_Username, mysql.MySQL_Password).createStatement();
 		}
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SkyauthTasks(), 0, config.ReloadDelay * 20);
 		CommandExecutor executor = new CommandsExecutor();
 		this.getCommand("login").setExecutor(executor);
 		this.getCommand("register").setExecutor(executor);
 		this.getCommand("change").setExecutor(executor);
 		this.getCommand("reload-skyauth").setExecutor(executor);
 	}
 	
 	private final void startMetrics() throws IOException {
 		Metrics metrics = new Metrics(this);
     	metrics.createGraph("EncryptGraph").addPlotter(new Metrics.Plotter("Encrypting password") {	
     			
     		@Override
     		public int getValue() {	
     			return 1;
     		}
     			
     		@Override
     		public String getColumnName() {
     			if(config.EncryptPassword) {
     				return "Yes";
     			}
     			else {
     				return "No";
     			}
     		}
     			
     	});
     	metrics.start();
 	}
 	
 	public static void reload() throws Exception {
 		if(useMySQL()) {
 			final ArrayList<String> arrayData = new ArrayList<String>();
 			ResultSet rs = stat.executeQuery("SELECT User, Password, Code FROM Skyauth_Data");
 			while(rs.next()) {
 				arrayData.add(0, rs.getString("Password"));
 				arrayData.add(1, rs.getString("Code"));
 				data.put(rs.getString("User"), arrayData);
 			}
 			stat.executeUpdate("TRUNCATE TABLE Skyauth_Data");
 			for(Entry<String, ArrayList<String>> entry : data.entrySet()) {
 				stat.executeUpdate("INSERT INTO Skyauth_Data(User, Password, Code) VALUES('" + entry.getKey() + "', '" + entry.getValue().get(0) + "', '" + entry.getValue().get(1) + "')");
 			}
 		}
 		else {
 			AuthPlugin.data.putAll(AuthPlugin.config.Data);
 			AuthPlugin.config.Data.putAll(AuthPlugin.data);
 			AuthPlugin.config.save();
 		}
 	}
 	
 	public static void reload(CommandSender sender) throws Exception {
 		sender.sendMessage(ChatColor.GOLD + "Reloading...");
 		if(useMySQL()) {
 			final ArrayList<String> arrayData = new ArrayList<String>();
 			ResultSet rs = stat.executeQuery("SELECT User, Password, Code FROM Skyauth_Data");
 			while(rs.next()) {
 				arrayData.add(0, rs.getString("Password"));
 				arrayData.add(1, rs.getString("Code"));
 				data.put(rs.getString("User"), arrayData);
 			}
 			stat.executeUpdate("TRUNCATE TABLE Skyauth_Data");
 			for(Entry<String, ArrayList<String>> entry : data.entrySet()) {
 				stat.executeUpdate("INSERT INTO Skyauth_Data(User, Password, Code) VALUES('" + entry.getKey() + "', '" + entry.getValue().get(0) + "', '" + entry.getValue().get(1) + "')");
 			}
 		}
 		else {
 			AuthPlugin.data.putAll(AuthPlugin.config.Data);
 			AuthPlugin.config.Data.putAll(AuthPlugin.data);
 			AuthPlugin.config.save();
 		}
 		sender.sendMessage(ChatColor.GREEN + "Done !");
 	}
 	
 	public static final boolean useMySQL() {
 		return useMySQL;
 	}
 	
 	public static final boolean isLogged(final Player player) {
 		if(sessions.get(player.getName()) != null) {
 			return true;
 		}
 		return false;
 	}
 }
