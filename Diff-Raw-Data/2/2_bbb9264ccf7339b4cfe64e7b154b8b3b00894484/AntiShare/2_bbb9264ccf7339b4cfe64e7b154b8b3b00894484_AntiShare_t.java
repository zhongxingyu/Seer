 package com.turt2live.antishare;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.SQL.SQLManager;
 import com.turt2live.antishare.api.ASAPI;
 import com.turt2live.antishare.debug.Bug;
 import com.turt2live.antishare.debug.Debugger;
 import com.turt2live.antishare.listener.ASListener;
 import com.turt2live.antishare.log.ASLog;
 import com.turt2live.antishare.permissions.PermissionsHandler;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.regions.RegionHandler;
 import com.turt2live.antishare.storage.ItemMap;
 import com.turt2live.antishare.storage.VirtualInventory;
 import com.turt2live.antishare.storage.VirtualStorage;
 
 public class AntiShare extends PluginWrapper {
 
 	/* TODO: Wait until an API solution is available
 	 *  - TNT Creative Explosions
 	 *  
 	 *  TODO: Add these features:
 	 *  - Graphical configuration
 	 *  - Graphical permissions setup
 	 *  - Help GUI
 	 *  - TNT Creative Explosions
 	 *  
 	 *  Not officially a "todo", just a potential optimization:
 	 *  - Creative/Survival Block Tracking (BUKKIT-1215/1214, BUKKIT-1211) [Hackish code]
 	 *  - Inventory mirror creative-glitch (BUKKIT-1211) [Hackish code]
 	 *  
 	 *  Updates:
 	 *  - BUKKIT-1211 has been fixed by deltahat in bleeding, waiting on BUKKIT-1215/1214
 	 *    to be implemented before dropping MetadataHack.java. BUKKIT-1211's fix has yet
 	 *    (as of right now) to be pushed to the (Craft)Bukkit repo(s).
 	 *  
 	 *  NOTES:
 	 *  - Leaky:
 	 *  	https://bukkit.atlassian.net/browse/BUKKIT-####
 	 */
 
 	/*
 	 * Note To Self:
 	 * The plugin.yml file has already been updated to 
 	 * 3.2.0-PRE RELEASE, don't change the version number >.<
 	 */
 
 	// TODO: SET TO FALSE BEFORE RELEASE
	public static boolean DEBUG_MODE = false;
 
 	private Configuration config;
 	public ASLog log;
 	private SQLManager sql;
 	public VirtualStorage storage;
 	private RegionHandler regions;
 	private Conflicts conflicts;
 	private Debugger debugger;
 	private PermissionsHandler perms;
 	public ASAPI api;
 	public ItemMap itemMap;
 
 	@Override
 	public void onEnable(){
 		try{
 			api = new ASAPI();
 			debugger = new Debugger();
 			MultiWorld.detectWorlds(this);
 			if(DEBUG_MODE){
 				getServer().getPluginManager().registerEvents(debugger, this);
 			}
 			log = new ASLog(this, getLogger());
 			log.logTechnical("Starting up...");
 			if(getConfig().getBoolean("SQL.use")){
 				sql = new SQLManager(this);
 				if(sql.attemptConnectFromConfig()){
 					sql.checkValues();
 				}
 			}
 			config = new Configuration(this);
 			config.create();
 			config.reload();
 			itemMap = new ItemMap(this);
 			conflicts = new Conflicts(this);
 			perms = new PermissionsHandler(this);
 			if(getConfig().getBoolean("settings.debug-override")){
 				DEBUG_MODE = true;
 			}
 			new File(getDataFolder(), "inventories").mkdirs(); // Setup folders
 			cleanInventoryFolder();
 			getServer().getPluginManager().registerEvents(new ASListener(this), this);
 			storage = new VirtualStorage(this);
 			log.info("Converting pre-3.0.0 creative blocks...");
 			int converted = storage.convertCreativeBlocks();
 			log.info("Converted " + converted + " blocks!");
 			regions = new RegionHandler(this);
 			if(!DEBUG_MODE){
 				UsageStatistics.send(this);
 			}
 			getCommand("as").setExecutor(new CommandHandler(this));
 			getCommand("gm").setExecutor(new GameModeCommand(this));
 			if(getConfig().getInt("settings.save-interval") > 0){
 				int saveTime = (getConfig().getInt("settings.save-interval") * 60) * 20;
 				new TimedSave(this, saveTime);
 			}
 			new UpdateChecker(this);
 			// Check player regions
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 				@Override
 				public void run(){
 					for(Player player : Bukkit.getOnlinePlayers()){
 						if(regions.isRegion(player.getLocation())){
 							ASRegion region = regions.getRegion(player.getLocation());
 							region.alertEntry(player, regions);
 						}else{
 							VirtualInventory inventory = storage.getInventoryManager(player, player.getWorld());
 							inventory.makeMatch();
 						}
 					}
 				}
 			});
 			log.info("Enabled! (turt2live)");
 		}catch(Exception e){
 			Bug bug = new Bug(e, e.getMessage(), this.getClass(), null);
 			Debugger.sendBug(bug);
 		}
 	}
 
 	@Override
 	public void onDisable(){
 		try{
 			log.logTechnical("Shutting down...");
 			getServer().getScheduler().cancelTasks(this);
 			log.info("Saving virtual storage to disk/SQL");
 			regions.saveStatusToDisk();
 			storage.saveToDisk();
 			if(sql != null){
 				sql.disconnect();
 			}
 			log.info("Disabled! (turt2live)");
 			log.save();
 		}catch(Exception e){
 			Bug bug = new Bug(e, e.getMessage(), this.getClass(), null);
 			Debugger.sendBug(bug);
 		}
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
 
 	public boolean isBlocked(Player player, String permission, World world){
 		if(getPermissions().has(player, permission, world)){
 			return false;
 		}
 		if(config().onlyIfCreative(player)){
 			if(player.getGameMode().equals(GameMode.CREATIVE)){
 				return true;
 			}else{
 				return false;
 			}
 		}else{
 			return true;
 		}
 	}
 }
