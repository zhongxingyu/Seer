 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.listeners.BlockEventListener;
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.Config;
 import com.araeosia.ArcherGames.listeners.PlayerEventListener;
 import com.araeosia.ArcherGames.listeners.EntityEventListener;
 import com.araeosia.ArcherGames.listeners.VotifierListener;
 import com.araeosia.ArcherGames.utils.Database;
 import com.araeosia.ArcherGames.utils.Economy;
 import com.araeosia.ArcherGames.utils.IRCBot;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class ArcherGames extends JavaPlugin {
 
 	public boolean debug = false;
 	public Config config;
 	public ScheduledTasks scheduler;
 	public Logger log;
 	public List<String> voteSites;
 	public Location startPosition;
 	public HashMap<String, ArrayList<ItemStack>> kits = new HashMap<String, ArrayList<ItemStack>>();
 	public static ArrayList<Archer> players = new ArrayList<Archer>();
 	public HashMap<String, String> strings = new HashMap<String, String>();
 	public ServerWide serverwide;
 	public HashMap<String, Boolean> configToggles = new HashMap<String, Boolean>();
 	public Connection conn;
 	public Database db;
 	public IRCBot IRCBot;
 	public Economy econ;
 
 	/**
 	 *
 	 */
 	@Override
 	public void onEnable() {
 		econ = new Economy(this);
 		startPosition = getServer().getWorlds().get(0).getSpawnLocation();
 		log = this.getLogger();
 		scheduler = new ScheduledTasks(this);
 		serverwide = new ServerWide(this);
 		IRCBot = new IRCBot(this);
 		config = new Config(this);
 		config.loadConfiguration();
 		db = new Database(this);
 		// Events
 		this.getServer().getPluginManager().registerEvents(new EntityEventListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new VotifierListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new BlockEventListener(this), this);
 		// Commands
 		this.getServer().getPluginManager().registerEvents(new CommandHandler(this), this);
 		this.getCommand("kit").setExecutor(new CommandHandler(this));
 		this.getCommand("listkits").setExecutor(new CommandHandler(this));
 		this.getCommand("vote").setExecutor(new CommandHandler(this));
 		this.getCommand("money").setExecutor(new CommandHandler(this));
 		this.getCommand("stats").setExecutor(new CommandHandler(this));
 		this.getCommand("archergames").setExecutor(new CommandHandler(this));
 		this.getCommand("chunk").setExecutor(new CommandHandler(this));
		//this.getCommand("pay").setExecutor(new CommandHandler(this));
 		this.getCommand("time").setExecutor(new CommandHandler(this));
 		this.getCommand("timer").setExecutor(new CommandHandler(this));
 
 		log.info("ArcherGames is enabled!");
 		if (debug) {
 			log.info("Debug mode is enabled!");
 		}
 		log.info("Connecting to IRC server...");
 		try {
 			IRCBot.setupBot();
 		} catch (Exception e) {
 			if(debug) e.printStackTrace();
 		}
 		log.info("Starting automated loop of games...");
 		scheduler.everySecondCheck();
 	}
 
 	/**
 	 *
 	 */
 	@Override
 	public void onDisable() {
 		this.getServer().getScheduler().cancelTask(scheduler.schedulerTaskID); // Kill the loop.
 		log.info("ArcherGames is disabled.");
 
 	}
 
 	public void dbConnect() {
 		try {
 			if (conn == null || conn.isValid(1) || conn.isClosed()) {
 				java.util.Properties conProperties = new java.util.Properties();
 				conProperties.put("user", this.getConfig().getString("ArcherGames.mysql.username"));
 				conProperties.put("password", this.getConfig().getString("ArcherGames.mysql.password"));
 				conProperties.put("autoReconnect", "true");
 				conProperties.put("maxReconnects", "3");
 				if(debug){
 					log.info(conProperties.toString());
 				}
 				String uri = "jdbc:mysql://" + this.getConfig().getString("ArcherGames.mysql.hostname") + ":" + this.getConfig().getString("ArcherGames.mysql.port") + "/" + this.getConfig().getString("ArcherGames.mysql.database");
 				if(debug){
 					log.info(uri);
 				}
 				conn = DriverManager.getConnection(uri, conProperties);
 			}
 		} catch (SQLException ex) {
 			log.log(Level.SEVERE, "Unable to connect to database!");
 		}
 	}
 }
