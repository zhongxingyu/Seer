 package com.tehbeard.BeardStat;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.sql.SQLException;
 
 import java.util.List;
 import java.util.Scanner;
 
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permissible;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.tehbeard.BeardStat.DataProviders.IStatDataProvider;
 import com.tehbeard.BeardStat.DataProviders.MysqlStatDataProvider;
 import com.tehbeard.BeardStat.DataProviders.SQLiteStatDataProvider;
 import com.tehbeard.BeardStat.DataProviders.TransferDataProvider;
 import com.tehbeard.BeardStat.Metrics.Plotter;
 import com.tehbeard.BeardStat.commands.*;
 import com.tehbeard.BeardStat.commands.formatters.FormatFactory;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.OnlineTimeManager;
 import com.tehbeard.BeardStat.containers.PlayerStatManager;
 import com.tehbeard.BeardStat.listeners.*;
 
 
 
 /**
  * BeardStat Statistic's tracking for the gentleman server
  * @author James
  *
  */
 public class BeardStat extends JavaPlugin {
 
 
 	public static final String DEFAULT_DOMAIN = "default";
 	public static final String GLOBAL_WORLD = "__global__";
 
 	private static BeardStat self;
 	private int runner;
 	private PlayerStatManager playerStatManager;
 	private static final String PERM_PREFIX = "stat";
 	
 	/**
 	 * Return the instance of this plugin
 	 * @return
 	 */
 	public static  BeardStat self(){
 		return self;
 	}
 	/**
 	 * Returns the stat manager for use by other plugins
 	 * @return
 	 */
 	public PlayerStatManager getStatManager(){
 		return playerStatManager;
 	}
 
 
 
 	/**
 	 * Check for permission
 	 * @param player player to check
 	 * @param node permission node to check
 	 * @return
 	 */
 	public static boolean hasPermission(Permissible player,String node){
 
 		return player.hasPermission(PERM_PREFIX + "." + node);
 
 
 	}
 
 	/**
 	 * Print to console
 	 * @param line
 	 */
 	public static void printCon(String line){
 		self.getLogger().info(line);
 		//System.out.println("[BeardStat] " + line);
 	}
 
 	/**
 	 * Print to console if debug mode is active
 	 * @param line
 	 */
 	public static void printDebugCon(String line){
 
 		if(self != null && self.getConfig().getBoolean("general.debug", false)){
 			printCon("[DEBUG] " + line);
 
 		}
 
 	}
 
 	public void onDisable() {
 		//flush database to cache
 
 		printCon("Stopping auto flusher");
 		getServer().getScheduler().cancelTask(runner);
 		if(playerStatManager != null){
 			printCon("Flushing cache to database");
 			playerStatManager.saveCache();
 			playerStatManager.flush();
 			printCon("Cache flushed to database");
 		}
 		self = null;
 	}
 
 	public void onEnable() {
 
 
 
 		//start initialisation
 		self = this;
 		printCon("Starting BeardStat");
 
 
 		MetaDataCapture.readData(getResource("metadata.txt"));
 		try {
 			MetaDataCapture.readData(new FileInputStream(new File(getDataFolder(),"metadata.txt")));
 		} catch (FileNotFoundException e) {
 			BeardStat.printCon("No External metadata file detected");
 		}
 
 		try {
 			printCon("Loading default language pack");
 			LanguagePack.load(getResource("messages.lang"));
 			File extLangPack = new File(getDataFolder(),"messages.lang");
 			if(extLangPack.exists()){
 				printCon("External language pack detected! Loading...");
 				LanguagePack.overlay(new FileInputStream(extLangPack));
 			}
 		} catch (IOException e1) {
 			printCon("Faield to load language pack");
 			e1.printStackTrace();
 		}
 
 		//run config updater
 		updateConfig();
 
 
 
 		//set DB HERE
 		printCon("Connecting to database");
 		printCon("Using " + getConfig().getString("stats.database.type") + " Adpater");
 		if(getConfig().getString("stats.database.type")==null){
 			printCon("INVALID ADAPTER SELECTED");
 			getPluginLoader().disablePlugin(this);
 			return;
 		}
 
 		IStatDataProvider db =getProvider(getConfig().getConfigurationSection("stats.database"));
 
 		if(db==null){
 			printCon(" Error loading database, disabling plugin");
 			getPluginLoader().disablePlugin(this);
 			return;
 		}
 
 		//start the player manager
 		playerStatManager = new PlayerStatManager(db);
 
 
 
 		printCon("initializing composite stats");
 		//parse the composite stats
 		//loadCompositeStats();
 		//loadFormats();
 
 		printCon("Registering events and collectors");
 
 
 		//register event listeners
 		//get blacklist, then start and register each type of listener
 		List<String> worldList = getConfig().getStringList("stats.blacklist");
 		StatBlockListener sbl = new StatBlockListener(worldList,playerStatManager);
 		StatPlayerListener spl = new StatPlayerListener(worldList,playerStatManager);
 		StatEntityListener sel = new StatEntityListener(worldList,playerStatManager);
 		StatVehicleListener svl = new StatVehicleListener(worldList, playerStatManager);
 		StatCraftListener scl = new StatCraftListener(worldList,playerStatManager);
 
 		getServer().getPluginManager().registerEvents(sbl, this);
 		getServer().getPluginManager().registerEvents(spl, this);
 		getServer().getPluginManager().registerEvents(sel, this);
 		getServer().getPluginManager().registerEvents(svl, this);
 		getServer().getPluginManager().registerEvents(scl, this);
 
 
 
 		//start Database flusher.
 
 		runner = getServer().getScheduler().scheduleSyncRepeatingTask(this, new dbFlusher(), 2400L, 2400L);
 		//runner = getServer().getScheduler().scheduleSyncRepeatingTask(this, new dbFlusher(), 600L, 600L);
 
 		//load the commands.
 		printCon("Loading commands");
 		getCommand("stats").setExecutor(new StatCommand(playerStatManager));
 		getCommand("played").setExecutor(new playedCommand(playerStatManager));
 		getCommand("statpage").setExecutor(new StatPageCommand(this));
 		getCommand("laston").setExecutor(new LastOnCommand(playerStatManager));
 		getCommand("beardstatdebug").setExecutor(playerStatManager);
 		getCommand("statadmin").setExecutor(new StatAdmin(playerStatManager));
 
 		//Incase of /reload, set all logged in player names.
 		for(Player player: getServer().getOnlinePlayers()){
 
 			OnlineTimeManager.setRecord(player);
 		}
 
 
 		/** ===METRICS CODE=== **/
 		Metrics metrics;
 		try {
 			metrics = new Metrics(this);
 
 
 			metrics.addCustomData(new Plotter(getConfig().getString("stats.database.type").toLowerCase()){
 
 				@Override
 				public int getValue() {
 					return 1;
 				}
 
 			});
 
 			metrics.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		printCon("BeardStat Loaded");
 	}
 
 	/**
 	 * Update config as needed.
 	 */
 	private void updateConfig() {
 
 		//convert old world lists over to blacklist (introduced. 0.4.7 - Honey)
 		if(getConfig().contains("stats.worlds")){
 			printCon("Moving blacklist to new location");
 			getConfig().set("stats.blacklist", getConfig().getStringList("stats.worlds"));
 			getConfig().set("stats.worlds",null);
 		}
 
 
 
 		//Standard defaults updater
 		if(!new File(getDataFolder(),"config.yml").exists()){
 			printCon("Writing default config file to disk.");
 			getConfig().set("stats.configversion",null);
 			getConfig().options().copyDefaults(true);
 		}
 		if(getConfig().getInt("stats.configversion",0) < Integer.parseInt("${project.config.version}")){
 
 			printCon("Updating config to include newest configuration options");
 			getConfig().set("stats.configversion",null);
 			getConfig().options().copyDefaults(true);
 
 		}
 
 		saveConfig();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		sender.sendMessage("Command not implemented!");
 		return false;
 
 	}
 
 	public class dbFlusher implements Runnable{
 
 		public void run() {
 			if(getConfig().getBoolean("general.verbose",false)){
 				BeardStat.printCon("Flushing to database.");
 			}
 
 			playerStatManager.saveCache();
 			playerStatManager.flush();
 			if(getConfig().getBoolean("general.verbose",false)){
 				BeardStat.printCon("flush completed");
 			}
 		}
 
 	}
 
 
 
 	public static void sendNoPermissionError(CommandSender sender){
 		sendNoPermissionError(sender, LanguagePack.getMsg("error.permission"));
 	}
 
 	public static void sendNoPermissionError(CommandSender sender, String message){
 		sender.sendMessage(ChatColor.RED + message);
 	}
 
 
 
 	/**
 	 * Show nicer error messages for mysql errors
 	 * @param e
 	 */
 	public static void mysqlError(SQLException e){
 		self.getLogger().severe("=========================================");
 		self.getLogger().severe("              DATABASE ERROR             ");
 		self.getLogger().severe("=========================================");
 		self.getLogger().severe("An error occured while trying to connect to the BeardStat database");
 		self.getLogger().severe("Mysql error code: "+ e.getErrorCode());
 
 		switch(e.getErrorCode()){
 		case 1042:self.getLogger().severe("Cannot find hostname provided");break;
 		case 1044:
 		case 1045:self.getLogger().severe("Cannot connect to database, check user credentials, database exists and that user is able to log in from this remote machine");break;
 		case 1049:self.getLogger().severe("Cannot locate database, check you spelt it correctly and username has access rights.");break;
 
 		default:
 			self.getLogger().severe("Error code not found (or not supplied!), either check the error code online, or post on the dev.bukkit.org/server-mods/beardstat page");
 			self.getLogger().severe("Exception Detail:");
 			self.getLogger().severe(e.getMessage());
 			break; 
 		}
 
 		self.getLogger().severe("=========================================");
 		self.getLogger().severe("            Begin error dump             ");
 		self.getLogger().severe("=========================================");
 		e.printStackTrace();
 		self.getLogger().severe("=========================================");
 		self.getLogger().severe("             End error dump              ");
 		self.getLogger().severe("=========================================");
 
 	}
 
 
 
 	private void loadCompositeStats(){
 
 		for(String cstat : getConfig().getStringList("customstats")){
 
 			String[] i = cstat.split("\\=");
 			EntityStatBlob.addDynamicStat(i[0].trim(), i[1].trim());
 
 
 		}
 
 		for(String cstat : getConfig().getStringList("savedcustomstats")){
 
 			String[] i = cstat.split("\\=");
 			EntityStatBlob.addDynamicSavedStat(i[0].trim(), i[1].trim());
 
 
 		}
 
 
 	}
 
 	private void loadFormats(){
 		for(String format : getConfig().getStringList("customformats")){
 			String stat = format.split(":")[0];
 			String formating = format.replace(stat + ":","");
 			FormatFactory.addStringFormat(stat.split("\\.")[0], stat.split("\\.")[1], formating);
 		}
 	}
 
 	private IStatDataProvider getProvider(ConfigurationSection config){
 		IStatDataProvider db = null;
 		if(config.getString("type").equalsIgnoreCase("mysql")){
 			try {
 				db = new MysqlStatDataProvider(
 						config.getString("host"),
 						config.getInt("port",3306),
 						config.getString("database"),
 						config.getString("prefix"),
 						config.getString("username"),
 						config.getString("password")
 						);
 			} catch (SQLException e) {
 				mysqlError(e);
 				db = null;
 			}
 		}
 		if(config.getString("type").equalsIgnoreCase("sqlite")){
 			try {
 				db = new SQLiteStatDataProvider(new File(getDataFolder(),"stats.db").toString());
 			} catch (SQLException e) {
 				e.printStackTrace();
 				db =null;
 			}
 
 		}
 
 		if(config.getString("type").equalsIgnoreCase("memory")){
 			try {
 				db = new SQLiteStatDataProvider(":memory:");
 			} catch (SQLException e) {
 				e.printStackTrace();
 				db =null;
 			}
 		}
 
 
 		if(config.getString("type").equalsIgnoreCase("file")){
 			BeardStat.printCon("FILE DRIVER NO LONGER SUPPORTED, PLEASE TRANSFER TO SQLITE/MYSQL IN PREVIOUS VERSION BEFORE LOADING");
 		}
 		//TRANSFER TYPE
 
 		if(config.getString("type").equalsIgnoreCase("transfer")){
 			IStatDataProvider _old = getProvider(getConfig().getConfigurationSection("stats.transfer.old"));
 			IStatDataProvider _new = getProvider(getConfig().getConfigurationSection("stats.transfer.new"));
 			new TransferDataProvider(_old, _new);
 			db = _new;
 		}
 		return db;
 	}
 
 	public String readSQL(String type,String filename,String prefix){
 		BeardStat.printDebugCon("Loading SQL: " + filename);
 		InputStream is = getResource(filename + "." + type);
 		if(is == null){
 			is = getResource(filename + ".sql");
 		}
		String sql = new Scanner(is).useDelimiter("\\Z").next();
 
 
 		return sql.replaceAll("\\$\\{PREFIX\\}",prefix);
 
 	}
 	
 }
 
