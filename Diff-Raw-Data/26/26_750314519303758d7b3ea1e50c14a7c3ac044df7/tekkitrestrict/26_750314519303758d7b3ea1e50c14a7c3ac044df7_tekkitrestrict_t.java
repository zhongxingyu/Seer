 package com.github.dreadslicer.tekkitrestrict;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import net.minecraft.server.RedPowerLogic;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.ChunkUnloader;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Dupes;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Global;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Hacks;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.LWC;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Listeners;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.MetricValues;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.SafeZones;
 import com.github.dreadslicer.tekkitrestrict.TRConfigCache.Threads;
 import com.github.dreadslicer.tekkitrestrict.commands.TRCommandAlc;
 import com.github.dreadslicer.tekkitrestrict.commands.TRCommandTPIC;
 import com.github.dreadslicer.tekkitrestrict.commands.TRCommandTR;
 import com.github.dreadslicer.tekkitrestrict.lib.TRFileConfiguration;
 import com.github.dreadslicer.tekkitrestrict.lib.YamlConfiguration;
 import com.github.dreadslicer.tekkitrestrict.listeners.Assigner;
 
 public class tekkitrestrict extends JavaPlugin {
 	public enum ConfigFile {
 		General, Advanced, ModModifications, DisableClick, DisableItems, Hack, LimitedCreative, Logging, TPreformance, MicroPermissions;
 	}
 	
 	public static Logger log;
 	public static TRFileConfiguration config;
 	public static boolean EEEnabled = false;
 	/**
 	 * Indicates if tekkitrestrict is disabling. Threads use this to check if they should stop.
 	 */
 	public static boolean disable = false;
 	public static final double version = 1.15;
 	public static Object perm = null;
 	public static TRSQLDB db;
 	private static tekkitrestrict instance;
 	public static ExecutorService basfo = Executors.newCachedThreadPool();
 	
 	private static TRThread ttt = null;
 	public static List<YamlConfiguration> configList = new LinkedList<YamlConfiguration>();
 
 	@Override
 	public void onLoad() {
 		instance = this;
 		log = this.getLogger();
 		
 		loadSqlite(); //pre-load the sqlite
 		initSqlite();
 
 		this.saveDefaultConfig();
 
 		config = this.getConfigx(); //Load the configuration files
 		loadConfigCache();
 		
 		try {
 			int ticks = (int) Math.round(config.getDouble("RPTimerMin", 0.2) * 20);
 			RedPowerLogic.minInterval = ticks; // set minimum interval for logic timers...
 		} catch (Exception e) {
 			log.warning("Setting the RedPower Timer failed.");
 		}
 
 		// ///////////
 		
 		
 		if (config.getBoolean("UseLogFilter", true)){
 			Enumeration<String> cc = LogManager.getLogManager().getLoggerNames();
 			TRLogFilter filter = new TRLogFilter();
 			while(cc.hasMoreElements()) {
 				Logger.getLogger(cc.nextElement()).setFilter(filter); 
 			}
 			log.info("Log filter Placed!");
 		}
 		
 		// ///////////
 
 		Log.init();
 		log.info("SQLite loaded!");
 	}
 
 	@Override
 	public void onEnable() {
 		
 		ttt = new TRThread();
 		Assigner.assign(this); //Register the required listeners
 		
 		new TRLogger();
 		TRSafeZone.init();
 		TRLimitFly.init();
 		TRLimitBlock.init();
 		TRNoDupe_BagCache.init();
 
 		getCommand("tekkitrestrict").setExecutor(new TRCommandTR());
 		getCommand("openalc").setExecutor(new TRCommandAlc());
 		getCommand("tpic").setExecutor(new TRCommandTPIC());
 
 		// determine if EE2 is enabled by using pluginmanager
 		PluginManager pm = this.getServer().getPluginManager();
 		if (pm.isPluginEnabled("mod_EE"))
 			tekkitrestrict.EEEnabled = true;
 		else
 			tekkitrestrict.EEEnabled = false;
 
 		try {
 			if (pm.isPluginEnabled("PermissionsEx")) {
 				perm = ru.tehkode.permissions.bukkit.PermissionsEx.getPermissionManager();
 				log.info("PEX is enabled!");
 			}
 		} catch (Exception ex) {
 			// Was not able to load permissionsEx
 		}
 
 		// Initiate noItem, Time-thread and our event listener
 		try {
 			reload(); // load em up!
 			ttt.init(); //Start up all threads
 
 			initHeartBeat();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 		try {
 			Metrics metrics = new Metrics(this);
 			Metrics.Graph g = metrics.createGraph("TekkitRestrict Stats (Since last server restarts)");
 			/*
 			 * g.addPlotter(new Metrics.Plotter("Total Safezones") {
 			 * 
 			 * @Override public int getValue() { return TRSafeZone.zones.size();
 			 * } });
 			 */
 			g.addPlotter(new Metrics.Plotter("Hack attempts") {
 				@Override
 				public int getValue() {
 					try {
 						return TRNoHack.hacks;
 					} catch(Exception e){
 						return 0;
 					}
 				}
 			});
 			g.addPlotter(new Metrics.Plotter("Recipe blocks") {
 				@Override
 				public int getValue() {
 					try{
 						int size = 0;
 						List<String> ssr = tekkitrestrict.config.getStringList("RecipeBlock");
 						for (int i = 0; i < ssr.size(); i++) {
 							List<TRCacheItem> iss = TRCacheItem.processItemString(
 									"", ssr.get(i), -1);
 							size += iss.size();
 						}
 						ssr = tekkitrestrict.config.getStringList("RecipeFurnaceBlock");
 						for (int i = 0; i < ssr.size(); i++) {
 							List<TRCacheItem> iss = TRCacheItem.processItemString(
 									"", ssr.get(i), -1);
 							size += iss.size();
 						}
 						return size;
 					}
 					catch(Exception e){
 						return 0;
 					}
 				}
 			});
 			g.addPlotter(new Metrics.Plotter("Dupe attempts") {
 				@Override
 				public int getValue() {
 					try {
 						return MetricValues.dupeAttempts;
 					} catch(Exception ex){
 						return 0;
 					}
 				}
 			});
 			g.addPlotter(new Metrics.Plotter("Disabled items") {
 				@Override
 				public int getValue() {
 					try {
 						return TRNoItem.getTotalLen();
 					} catch(Exception ex){
 						return 0;
 					}
 				}
 			});
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 		
 		if (!("" + version).equals(getDescription().getVersion())){
 			Log.Debug("Version Mismatch!");
 		}
 		log.info("TekkitRestrict v " + getDescription().getVersion()+ " Enabled!");
 		
 		/*
 		 * log.info("T: "+config.get("UseChunkUnloader").toString());
 		 * log.info("T1: "+config.get("FlyLimitDailyMinutes").toString());
 		 * log.info("T2: "+config.get("RPTimerMin").toString());
 		 */
 		// TRThrottler.init();
 	}
 
 	@Override
 	public void onDisable() {
 		disable = true;
 		
 		ttt.disableItemThread.interrupt();
 		ttt.entityRemoveThread.interrupt();
 		ttt.gemArmorThread.interrupt();
 		ttt.worldScrubThread.interrupt();
 		ttt.saveThread.interrupt();
 		
 		try { Thread.sleep(4000); } catch (InterruptedException e) {} //Sleep for 4 seconds to allow the savethread to save.
 		//try {
 		//	TRThread.originalEUEnd(); (Currently does nothing)
 		//} catch (Exception ex) {
 		//}
 
 		TRLogFilter.disable();
		FileLog.closeAll();
 		Log.deinit();
 		log.info("TekkitRestrict v " + getDescription().getVersion()+ " disabled!");
 	}
 
 	public static tekkitrestrict getInstance() {
 		return instance;
 	}
 
 	/*
 	 * public static boolean hasPermission(Player p, String perm,boolean list){
 	 * if(list){ //this type of element is in a list... if(perm != null &&
 	 * pm.isPluginEnabled("PermissionsEx")){ //return
 	 * ((ru.tehkode.permissions.PermissionManager
 	 * )tekkitrestrict.perm).getUser(p).getAllPermissions(); String[] perms =
 	 * getAllPlayerPerms(p,perm); }else if(pm.isPluginEnabled("Vault")){
 	 * RegisteredServiceProvider<net.milkbowl.vault.permission.Permission>
 	 * permissionProvider = tekkitrestrict.getInstance().getServer().
 	 * getServicesManager
 	 * ().getRegistration(net.milkbowl.vault.permission.Permission.class);
 	 * //permission. }else{/* //use superperms Set<PermissionAttachmentInfo>
 	 * pail = p.getEffectivePermissions(); Iterator<PermissionAttachmentInfo> cc
 	 * = pail.iterator(); LinkedList<String> listr = new LinkedList<String>();
 	 * while(cc.hasNext()){ PermissionAttachmentInfo cr = cc.next();
 	 * listr.add(cr.getPermission());
 	 * //tekkitrestrict.log.info("playerperms+ "+cr.getPermission()); } return
 	 * false; } } else{ return hasPermission(p,perm); } }
 	 */
 	
 	public static void loadConfigCache(){
 		Hacks.broadcast = config.getStringList(ConfigFile.Hack, "HackBroadcasts");
 		Hacks.broadcastFormat = config.getString(ConfigFile.Hack, "HackBroadcastString", "{PLAYER} tried to {TYPE}-hack!"); //TODO add colors
 		Hacks.kick = config.getStringList(ConfigFile.Hack, "HackKick");
 		
 		Hacks.fly = config.getBoolean(ConfigFile.Hack, "HackFlyEnabled", false);
 		Hacks.flyTolerance = config.getInt(ConfigFile.Hack, "HackFlyTolerance", 60);
 		Hacks.flyMinHeight = config.getInt(ConfigFile.Hack, "HackFlyMinHeight", 3);
 		
 		Hacks.forcefield = config.getBoolean(ConfigFile.Hack, "HackForcefieldEnabled", true);
 		Hacks.ffTolerance = config.getInt(ConfigFile.Hack, "HackForcefieldTolerance", 15);
 		Hacks.ffVangle = config.getDouble(ConfigFile.Hack, "HackForcefieldAngle", 40);
 		
 		Hacks.speed = config.getBoolean(ConfigFile.Hack, "HackSpeedEnabled", false);
 		Hacks.speedTolerance = config.getInt(ConfigFile.Hack, "HackMoveSpeedTolerance", 30);
 		Hacks.speedMaxSpeed = config.getDouble(ConfigFile.Hack, "HackMoveSpeedMax", 2.5);
 		
 		Dupes.broadcast = config.getStringList(ConfigFile.Hack, "Dupes.Broadcast");
 		Dupes.broadcastFormat = config.getString(ConfigFile.Hack, "Dupes.BroadcastString", "{PLAYER} tried to dupe using {TYPE}!"); //TODO add colors
 		Dupes.kick = config.getStringList(ConfigFile.Hack, "Dupes.Kick");
 		Dupes.alcBag = config.getBoolean(ConfigFile.Hack, "Dupes.PreventAlchemyBagDupe", true);
 		Dupes.rmFurnace = config.getBoolean(ConfigFile.Hack, "Dupes.PreventRMFurnaceDupe", true);
 		Dupes.tankcart = config.getBoolean(ConfigFile.Hack, "Dupes.PreventTankCartDupe", true);
 		Dupes.tankcartGlitch = config.getBoolean(ConfigFile.Hack, "Dupes.PreventTankCartGlitch", true);
 		Dupes.transmute = config.getBoolean(ConfigFile.Hack, "Dupes.PreventTransmuteDupe", true);
 		Dupes.pedestal = config.getBoolean(ConfigFile.Hack, "Dupes.PedestalEmcGen", true);
 		
 		Global.debug = config.getBoolean("ShowDebugMessages", false);
 		Global.kickFromConsole = config.getBoolean("KickFromConsole", false);
 		Listeners.UseBlockLimit = config.getBoolean("UseItemLimiter", true);
 		Listeners.BlockCreativeContainer = config.getBoolean("LimitedCreativeNoContainer", true);
 		
 		TRConfigCache.LogFilter.replaceList = config.getStringList("LogFilter");
 		TRConfigCache.LogFilter.logConsole = config.getBoolean("LogConsole", true);
 		TRConfigCache.LogFilter.logLocation = config.getString("LogLocation", "log");
 		
 		Threads.gemArmorSpeed = config.getInt("GemArmorDThread");
 		Threads.inventorySpeed = config.getInt("InventoryThread");
 		Threads.saveSpeed = config.getInt("AutoSaveThreadSpeed");
 		Threads.SSEntityRemoverSpeed = config.getInt("SSEntityRemoverThread");
 		Threads.worldCleanerSpeed = config.getInt("WorldCleanerThread");
 		
 		Threads.GAMovement = config.getBoolean("AllowGemArmorDefensive", true);
 		Threads.GAOffensive = config.getBoolean("AllowGemArmorOffensive", false);
 		
 		Threads.SSDisableEntities = config.getBoolean("SSDisableEntities", false);
 		Threads.SSDechargeEE = config.getBoolean("SSDechargeEE", true);
 		Threads.SSDisableArcane = config.getBoolean("SSDisableRingOfArcana");
 		
 		Threads.RMDB = config.getBoolean("RemoveDisabledItemBlocks", false);
 		Threads.UseRPTimer = config.getBoolean("UseAutoRPTimer", false);
 		Threads.ChangeDisabledItemsIntoId = config.getInt("ChangeDisabledItemsIntoId", 3);
 		Threads.RPTickTime = (int) Math.round(config.getDouble("RPTimerMin", 0.2) * 20);
 		
 		List<String> lwcprevent = config.getStringList("LWCPreventNearLocked");
 		if (lwcprevent == null) LWC.blocked = Collections.synchronizedList(new LinkedList<String>());
 		else LWC.blocked = Collections.synchronizedList(lwcprevent);
 		
 		SafeZones.allowNormalUser = config.getBoolean("SSAllowNormalUserToHaveSafeZones", true);
 		SafeZones.SSPlugins = config.getStringList("SSEnabledPlugins");
 		SafeZones.SSDisableFly = config.getBoolean("SSDisableFlying", false);
 		
 		ChunkUnloader.enabled = config.getBoolean("UseChunkUnloader", false);
 		ChunkUnloader.maxChunks = config.getInt("MaxChunks", 3000);
 		ChunkUnloader.maxRadii = config.getInt("MaxRadii", 256);
 	}
 
 	private static void initHeartBeat() {
 		instance.getServer().getScheduler().scheduleAsyncRepeatingTask(instance, new Runnable() {
 			@Override
 			public void run() {
 				TRLimitBlock.expireLimiters();
 			}
 		}, 60L, 32L);
 	}
 
 	private void loadSqlite() {
 		db = new TRSQLDB(null, "tr", "Data", this.getDataFolder().toString());
 		db.open();
 	}
 
 	private void initSqlite() {
 		//determine if Data.db is older.
 		Double ver = new Double(this.getDescription().getVersion());
 		ResultSet prev = null;
 		List<List<String>> srvals = null,limvals=null;
 		try{
 			//Version select
 			ResultSet rs = db.query("SELECT version FROM tr_dbversion");
 			prev = rs;
 			if(rs.next()){
 				Double verX = rs.getDouble("version");
 				if(verX == 1.10 || verX == 1.00){
 					//nothing changed...
 				}
 			}
 			rs.close();
 		}
 		catch(Exception e){
 			if(prev != null)
 				try {prev.close();} catch (SQLException e1) {}
 			//PRE-1.00 version, remove/purge and replace.
 			tekkitrestrict.log.info("Transfering last Data.db info to new Database prototype");
 			//remove all relevant information from both databases.
 			//tr_saferegion =	id name mode data world
 			//tr_limiter = 		id player blockdata
 			try {
 				srvals = this.getTableVals("tr_saferegion");
 			} catch(SQLException ex){
 				log.warning("Minor exception occured when trying to load the safezones from the database.");
 			}
 			try {
 				limvals = this.getTableVals("tr_limiter");
 			} catch(SQLException ex){
 				log.warning("Minor exception occured when trying to load the limiter from the database.");
 			}
 			
 			if (srvals != null && limvals != null)
 				tekkitrestrict.log.info("DB - Copied "+(srvals.size() + limvals.size())+" rows");
 			
 			try{db.query("DROP TABLE `tr_saferegion`;");} catch(Exception ex){}
 			try{db.query("DROP TABLE `tr_limiter`;");} catch(Exception ex){}
 			
 			
 			try {
 				db.query("CREATE TABLE IF NOT EXISTS 'tr_dbversion' (version NUMERIC);");
 				db.query("INSERT INTO 'tr_dbversion' (version) VALUES("+ver+");");
 			} catch (Exception E) {
 			}
 		}
 		
 		
 		try {
 			db.query("CREATE TABLE IF NOT EXISTS 'tr_saferegion' ( "
 					+ "'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
 					+ "'name' TEXT," + "'mode' INT," + "'data' TEXT,"
 					+ "'world' TEXT); ");
 			if(srvals != null){
 				for(List<String> sr:srvals){
 					String tadd = "";
 					for(String l:sr) tadd+=","+l;
 					//tadd = tadd.replace("null", "''");
 					if(tadd.startsWith(",")) tadd=tadd.substring(1, tadd.length());
 					//tekkitrestrict.log.info("INSERT INTO 'tr_saferegion' VALUES("+tadd+");");
 					db.query("INSERT INTO 'tr_saferegion' VALUES("+tadd+");");
 				}
 			}
 		} catch (Exception E) {
 		}
 		try {
 			db.query("CREATE TABLE IF NOT EXISTS 'tr_limiter' ( "
 					+ "'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
 					+ "'player' TEXT," + "'blockdata' TEXT);");
 			if(limvals != null){
 				for(List<String> sr:limvals){
 					String tadd = "";
 					for(String l:sr) tadd+=","+l;
 					//tadd = tadd.replace("null", "''");
 					if(tadd.startsWith(",")) tadd=tadd.substring(1, tadd.length());
 					//tekkitrestrict.log.info("INSERT INTO 'tr_saferegion' VALUES("+tadd+");");
 					db.query("INSERT INTO 'tr_limiter' VALUES("+tadd+");");
 				}
 			}
 		} catch (Exception E) {
 		}
 	}
 
 	private List<List<String>> getTableVals(String table) throws SQLException{
 		/*ResultSet rs1 = db.query("SELECT COUNT(*) FROM `"+table+"`");
 		int c = 0;
 		if(rs1 != null){
 			if(rs1.next()){
 				c=rs1.getInt(1);
 			}
 		}
 		rs1.close();*/
 		ResultSet rs = db.query("SELECT * FROM `"+table+"`");
 		List<List<String>> ls = new LinkedList<List<String>>();
 		if (rs == null) return ls;
 		while(rs.next()) {
 			List<String> j = new LinkedList<String>();
 			
 			for(int i=1;i<=100;i++){
 				//tekkitrestrict.log.info("++ "+rs.getNString(i));
 				try{j.add(rs.getString(i));}catch(Exception e){}
 			}
 			//tekkitrestrict.log.info("t: "+j.size());
 			ls.add(j);
 		}
 		rs.close();
 		return ls;
 	}
 	
 	public void reload() {
 		this.reloadConfig();		
 		config = this.getConfigx();
 		loadConfigCache();
 		TRNoItem.clear(); //TRNI
 		TRCacheItem.reload();
 		TRNoItem.reload(); //TRNI2
 		TRThread.reload(); // branches out
 		TRListener.reload();
 		TRLimitBlock.reload();
 		TRLogger.reload();
 		TRRecipeBlock.reload();
 		TRNoHack.reload();
 		TRLimitFly.reload();
 		TREMCSet.reload();
 		log.info("TekkitRestrict Reloaded!");
 	}
 
 	public static String antisqlinject(String ins) {
 		ins = ins.replaceAll("--", "");
 		ins = ins.replaceAll("`", "");
 		ins = ins.replaceAll("'", "");
 		ins = ins.replaceAll("\"", "");
 		return ins;
 	}
 
 	private TRFileConfiguration getConfigx() {
 		if (configList.size() == 0) {
 			reloadConfig();
 		}
 		return (new TRFileConfiguration());
 	}
 
 	@Override
 	public void reloadConfig() {
 		configList.clear();
 		configList.add(reloadc("General.config.yml"));
 		configList.add(reloadc("Advanced.config.yml"));
 		configList.add(reloadc("ModModifications.config.yml"));
 		configList.add(reloadc("DisableClick.config.yml"));
 		configList.add(reloadc("DisableItems.config.yml"));
 		configList.add(reloadc("Hack.config.yml"));
 		configList.add(reloadc("LimitedCreative.config.yml"));
 		configList.add(reloadc("Logging.config.yml"));
 		configList.add(reloadc("TPerformance.config.yml"));
 		configList.add(reloadc("MicroPermissions.config.yml"));
 	}
 
 	private YamlConfiguration reloadc(String loc) {
 		File cf = new File("plugins/tekkitrestrict/" + loc);
 		// tekkitrestrict.log.info(cf.getAbsolutePath());
 		YamlConfiguration conf = YamlConfiguration.loadConfiguration(cf);
 		// newConfig.loadFromString(s)
 		InputStream defConfigStream = getResource(loc);
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			conf.setDefaults(defConfig);
 			try {
 				defConfigStream.close();
 			} catch (IOException e) {
 				tekkitrestrict.log.warning("Exception while trying to reload the config!");
 				e.printStackTrace();
 			}
 		}
 		return conf;
 	}
 
 	@Override
 	public void saveDefaultConfig() {
 		Level ll = log.getLevel();
 		log.setLevel(Level.SEVERE);
 		try {
 			saveResource("General.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("Advanced.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("ModModifications.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("DisableClick.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("DisableItems.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("Hack.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("LimitedCreative.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("Logging.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("TPerformance.config.yml", false);
 		} catch (Exception e) {
 		}
 		try {
 			saveResource("MicroPermissions.config.yml", false);
 		} catch (Exception e) {
 		}
 		log.setLevel(ll);
 	}
 }
