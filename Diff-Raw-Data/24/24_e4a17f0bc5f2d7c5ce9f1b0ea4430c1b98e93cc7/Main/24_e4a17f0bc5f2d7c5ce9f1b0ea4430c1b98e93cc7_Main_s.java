 package net.robbytu.banjoserver.framework;
 
 import net.robbytu.banjoserver.framework.events.ServerUpdater;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 public class Main extends JavaPlugin {
 	public static Connection conn;
 	
 	private ServerUpdater serverUpdater;
 	
 	@Override
 	public void onEnable() {
 		// Fetch db configuration
 		String host = getConfig().getString("db.host");
 		int port = getConfig().getInt("db.port");
 		String user = getConfig().getString("db.username");
 		String pass = getConfig().getString("db.password");
 		String database = getConfig().getString("db.database");
		
		// Check db configuration
		if(host.isEmpty() || user.isEmpty() || pass.isEmpty() || database.isEmpty()) {
			getLogger().warning("Framework has not been enabled. Please check your configuration.");
			return;
		}
 
 		// Set up a connection
 		try{
 			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
 		}
 		catch (Exception e) {
 			getLogger().warning("Framework has not been enabled. Please check your configuration.");
 			e.printStackTrace();
 		}
 
 		// Set up serverUpdater
 		this.serverUpdater = new ServerUpdater();
 		this.serverUpdater.setOnline(1);
 		
 		getServer().getPluginManager().registerEvents(this.serverUpdater, this);
 		
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
