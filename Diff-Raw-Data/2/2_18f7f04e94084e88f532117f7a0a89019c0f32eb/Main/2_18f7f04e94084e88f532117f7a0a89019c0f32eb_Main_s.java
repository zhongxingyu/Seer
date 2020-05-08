 package net.robbytu.banjoserver.framework;
 
 import net.robbytu.banjoserver.framework.bungee.PluginMessengerListener;
 import net.robbytu.banjoserver.framework.utils.ServerUpdater;
 
 import net.robbytu.banjoserver.framework.utils.TaskWorker;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 public class Main extends JavaPlugin {
 	public static Connection conn;
 	public static Main plugin;
 	
 	private ServerUpdater serverUpdater;
 	
 	@Override
 	public void onEnable() {
 		// Update plugin
 		plugin = this;
 		
 		// Check db configuration
 		if(!getConfig().contains("db.host") || !getConfig().contains("db.port") || !getConfig().contains("db.username") || !getConfig().contains("db.password") || !getConfig().contains("db.database")) {
 			// Create a default configuration file
 			getConfig().set("db.host", "127.0.0.1");
 			getConfig().set("db.port", 3306);
 			getConfig().set("db.username", "minecraft");
 			getConfig().set("db.password", "");
 			getConfig().set("db.database", "minecraft");
 			
 			// Save configuration
 			this.saveConfig();
 			
 			// Log and exit
 			getLogger().warning("Framework has not been enabled. Please check your configuration - a default one has been written.");
 			return;
 		}
 		
 		// Fetch db configuration
 		String host = getConfig().getString("db.host");
 		int port = getConfig().getInt("db.port");
 		String user = getConfig().getString("db.username");
 		String pass = getConfig().getString("db.password");
 		String database = getConfig().getString("db.database");
 
 		// Set up a connection
 		try{
 			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", user, pass);
 		}
 		catch (Exception e) {
 			getLogger().warning("Framework has not been enabled. Please check your configuration.");
 			e.printStackTrace();
 		}
 
 		// Set up serverUpdater
 		this.serverUpdater = new ServerUpdater();
 		this.serverUpdater.setOnline(1);
 		
 		getServer().getPluginManager().registerEvents(this.serverUpdater, this);
 
         // Set up TaskWorker
         new TaskWorker();
 
         // Register for Plugin messages
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BSBungge", new PluginMessengerListener());
         Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BSFramework");
 
 		// Everything went OK
 		getLogger().info("Framework has been enabled.");
 	}
 	
 	@Override
 	public void onDisable() {
 		// Update our status to offline
 		this.serverUpdater.setOnline(0);
 		
 		getLogger().info("Framework has been disabled.");
 	}
 }
