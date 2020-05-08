 package com.turt2live.antishare;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.SQL.SQLManager;
 import com.turt2live.antishare.debug.Debugger;
 import com.turt2live.antishare.listener.ASListener;
 import com.turt2live.antishare.log.ASLog;
 import com.turt2live.antishare.permissions.PermissionsHandler;
 import com.turt2live.antishare.regions.RegionHandler;
 import com.turt2live.antishare.storage.VirtualStorage;
 
 public class AntiShare extends PluginWrapper {
 
 	/* TODO: Wait until an API solution is available
 	 *  - TNT Creative Explosions
 	 *  	- Refs in plugin.yml, config.yml, world.yml, PermissionsMenu.java, and OtherEditor.java have been removed
 	 * - Throw items in region 
 	 * 		- Refs in plugin.yml, config.yml, world.yml, OtherEditor.java, and MessageEditor.java have been removed
 	 *  
 	 *  TODO: Test code when BUKKIT issues fixed:
 	 *  ** For self ref: https://bukkit.atlassian.net/browse/BUKKIT-####
 	 *  - Creative/Survival Block Tracking (BUKKIT-1215/1214, BUKKIT-1211)
 	 *  - Inventory mirror creative-glitch (BUKKIT-1211)
 	 *  
 	 *  TODO: Add these features
 	 *  - AntiShare log file analyzer
 	 *  - Bug catching (sanity checks)
 	 *  - Isolated creative / survival tracked blocks
 	 *  
 	 *  TODO: Test/verify these potential bugs
 	 *  - Minecart (storage/furnace) may be accessible (explicit support?)
 	 *  - MobArena potential issue (see bug report)
 	 *  
 	 *  TODO: Fix these known bugs:
 	 *  - /as rl resets people's inventories while in regions
 	 *  - Metadata related (fix?)
 	 *  - TNT Related (waiting on PR to be accepted)
 	 *  - Using a projectile to hit mobs is considered hitting a player
	 *  
	 *  TODO: Add permissions to config.yml
	 *  - Fire charge, fire, tnt, bucket  
 	 */
 
 	// TODO: SET TO FALSE BEFORE RELEASE
 	public static boolean DEBUG_MODE = true;
 
 	private Configuration config;
 	public ASLog log;
 	private SQLManager sql;
 	public VirtualStorage storage;
 	private RegionHandler regions;
 	private Conflicts conflicts;
 	private Debugger debugger;
 	private TimedSave timedSave;
 	private PermissionsHandler perms;
 
 	@Override
 	public void onEnable(){
 		log = new ASLog(this, getLogger());
 		log.logTechnical("Starting up...");
 		config = new Configuration(this);
 		config.create();
 		config.reload();
 		if(getConfig().getBoolean("settings.debug-override")){
 			DEBUG_MODE = true;
 		}
 		new File(getDataFolder(), "inventories").mkdirs(); // Setup folders
 		cleanInventoryFolder();
 		getServer().getPluginManager().registerEvents(new ASListener(this), this);
 		MultiWorld.detectWorlds(this);
 		storage = new VirtualStorage(this);
 		log.info("Converting pre-3.0.0 creative blocks...");
 		int converted = storage.convertCreativeBlocks();
 		log.info("Converted " + converted + " blocks!");
 		if(getConfig().getBoolean("SQL.use")){
 			sql = new SQLManager(this);
 			if(sql.attemptConnectFromConfig()){
 				sql.checkValues();
 			}
 		}
 		regions = new RegionHandler(this);
 		debugger = new Debugger(this);
 		if(DEBUG_MODE){
 			getServer().getPluginManager().registerEvents(debugger, this);
 		}
 		conflicts = new Conflicts(this);
 		perms = new PermissionsHandler(this);
 		if(!DEBUG_MODE){
 			UsageStatistics.send(this);
 		}
 		getCommand("as").setExecutor(new CommandHandler(this));
 		getCommand("gm").setExecutor(new GameModeCommand(this));
 		if(getConfig().getInt("settings.save-interval") > 0){
 			int saveTime = (getConfig().getInt("settings.save-interval") * 60) * 20;
 			timedSave = new TimedSave(this, saveTime);
 		}
 		log.info("Enabled! (turt2live)");
 	}
 
 	@Override
 	public void onDisable(){
 		log.logTechnical("Shutting down...");
 		if(timedSave != null){
 			timedSave.cancel();
 		}
 		log.info("Saving virtual storage to disk/SQL");
 		storage.saveToDisk();
 		regions.saveStatusToDisk();
 		if(sql != null){
 			sql.disconnect();
 		}
 		log.info("Disabled! (turt2live)");
 	}
 
 	public Configuration config(){
 		return config;
 	}
 
 	public SQLManager getSQLManager(){
 		return sql;
 	}
 
 	public RegionHandler getRegionHandler(){
 		return regions;
 	}
 
 	public Conflicts getConflicts(){
 		return conflicts;
 	}
 
 	public Debugger getDebugger(){
 		return debugger;
 	}
 
 	public PermissionsHandler getPermissions(){
 		return perms;
 	}
 
 	public void cleanInventoryFolder(){
 		File sdir = new File(getDataFolder(), "inventories");
 		String world = Bukkit.getWorlds().get(0).getName();
 		if(sdir.exists()){
 			for(File f : sdir.listFiles()){
 				if(f.getName().endsWith("CREATIVE.yml")
 						|| f.getName().endsWith("SURVIVAL.yml")){
 					File newName = new File(f.getParent(), f.getName().replace("SURVIVAL", "SURVIVAL_" + world).replace("CREATIVE", "CREATIVE_" + world));
 					f.renameTo(newName);
 				}
 			}
 		}
 	}
 }
