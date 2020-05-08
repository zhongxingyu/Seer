 package team.GunsPlus;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import me.lyneira.MachinaRedstoneBridge.MachinaRedstoneBridge;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 import com.miykeal.showCaseStandalone.ShowCaseStandalone;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import team.ApiPlus.API.PluginPlus;
 import team.ApiPlus.API.Type.BlockType;
 import team.ApiPlus.API.Type.ItemType;
 import team.ApiPlus.Manager.Loadout.Loadout;
 import team.ApiPlus.Manager.Loadout.LoadoutManager;
 import team.ApiPlus.Util.Task;
 import team.GunsPlus.API.GunsPlusAPI;
 import team.GunsPlus.Block.Tripod;
 import team.GunsPlus.Block.TripodData;
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Item.GunItem;
 import team.GunsPlus.Manager.ConfigLoader;
 import team.GunsPlus.Manager.TripodDataHandler;
 import team.GunsPlus.Util.Metrics;
 import team.GunsPlus.Util.Metrics.Graph;
 import team.GunsPlus.Util.Util;
 import team.GunsPlus.Util.VersionChecker;
 
 public class GunsPlus extends PluginPlus {
 	public static String PRE = "[Guns+]";
 	
 	public static GunsPlus plugin;
 	public static LWC lwc;
 	public static WorldGuardPlugin wg;
 	public static MachinaRedstoneBridge mrb;
 	public static ShowCaseStandalone showcase;
 	public static boolean useFurnaceAPI = false;
 	
 	private static GunsPlusAPI api;
 	
 	public static Logger log = Bukkit.getLogger();
 	
 	public static List<GunsPlusPlayer> GunsPlusPlayers = new ArrayList<GunsPlusPlayer>();
 	
 	public static boolean warnings = true;
 	public static boolean debug = false;
 	public static boolean notifications = true;
 	public static boolean autoreload = true;
 	public static boolean hudenabled = false;
 	public static boolean useperms = true;
 	public static boolean toolholding = true;
 	public static int hudX = 20;
 	public static int hudY = 20;
 	public static String hudBackground = null;
 	public static KeyType zoomKey = new KeyType("right", true);
 	public static KeyType fireKey = new KeyType("left", false);
 	public static KeyType reloadKey = new KeyType("R", false);
 
 	public static List<Gun> allGuns = new ArrayList<Gun>();
 	public static List<Ammo> allAmmo = new ArrayList<Ammo>();
 	public static List<Addition> allAdditions = new ArrayList<Addition>();
 	public static List<TripodData> allTripodBlocks = Collections.synchronizedList(new ArrayList<TripodData>());
 	public static Tripod tripod;
 	
 	public static Map<String, Class<? extends BlockType>> customBlockTypes = new HashMap<String, Class<? extends BlockType>>();
 	public static Map<String, Class<? extends ItemType>> customItemTypes = new HashMap<String, Class<? extends ItemType>>();
 
 	
 	
 	static{
 		customBlockTypes.put("Tripod", Tripod.class);
 		customItemTypes.put("Gun", GunItem.class);
 		customItemTypes.put("Addition", Addition.class);
 		customItemTypes.put("Ammo", Ammo.class);
 	}
 
 	
 
 	@Override
 	public void onDisable() {
 		if(Tripod.tripodenabled = true) {
 			for(TripodData td : allTripodBlocks) {
 				td.destroy();
 			}
 			TripodDataHandler.saveAll();
 		}
 		plugin.resetFields();
 		log.log(Level.INFO, PRE + " version " + getDescription().getVersion()
 				+ " is now disabled.");
 	}
 	
 	@Override
 	public void onEnable() {
 		plugin = this;
 		ConfigLoader.config();
 		new VersionChecker(this,"http://dev.bukkit.org/server-mods/guns/files.rss");
 		this.registerBlockTypes(customBlockTypes);
 		this.registerItemTypes(customItemTypes);
 		hook();
 		init();
		registerPluginPlus();
		Util.printCustomIDs();
 		new FireBinding(this, GunsPlus.fireKey);
 		new ReloadBinding(this, GunsPlus.reloadKey);
 		new ZoomBinding(this, GunsPlus.zoomKey);
 		Bukkit.getPluginManager().registerEvents(new GunsPlusListener(this), this);
 		getCommand("guns+").setExecutor(new CommandEx(this));
 		api = new GunsPlusAPI(this);
 		metricStart();
 		log.log(Level.INFO, PRE + " version " + getDescription().getVersion()+ " is now enabled.");
 	}
 
 	private void init() {
 		ConfigLoader.loadGeneral();
 		if(Tripod.tripodenabled){
 			tripod = new Tripod(this, Tripod.tripodTexture);
 		}
 		ConfigLoader.loadAdditions();
 		ConfigLoader.loadAmmo();
 		ConfigLoader.loadGuns();
 		ConfigLoader.loadRecipes();
 		if(Tripod.tripodenabled) {
 			initTripod();
 			updateTripods();
 		}
 		if(hudenabled)
 			updateHUD();
 	}
 	
 	private void initTripod(){
 		TripodDataHandler.setNextId(ConfigLoader.dataDB.getKeys(false).size());
 		TripodDataHandler.allowLoading();//uhh ugly hack ;)
 		TripodDataHandler.loadAll();
 		TripodDataHandler.denyLoading(); 
 	}
 	
 	private void hook(){
 		Plugin spout = getServer().getPluginManager().getPlugin("Spout");
 		Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
 		Plugin furnaceAPI = getServer().getPluginManager().getPlugin("FurnaceAPI");
 		Plugin worldguard = getServer().getPluginManager().getPlugin("WorldGuard");
 		Plugin machina = getServer().getPluginManager().getPlugin("MachinaRedstoneBridge");
 		Plugin show = getServer().getPluginManager().getPlugin("Showcase");
 		if(spout != null) {
 		    log.log(Level.INFO, PRE+" Plugged into Spout!");
 		}else{
 			//disable this, because it would do nothing without spout
 			log.log(Level.INFO, PRE+" disableing because Spout is missing!");
 			this.setEnabled(false);
 		}
 		if(lwcPlugin != null) {
 			lwc = ((LWCPlugin) lwcPlugin).getLWC();
 		    log.log(Level.INFO, PRE+" Plugged into LWC!");
 		}
 		if(furnaceAPI != null) {
 			useFurnaceAPI = true;	
 			log.log(Level.INFO, PRE+" Plugged into FurnaceAPI!");
 		}
 		if(worldguard != null) {
 			wg = (WorldGuardPlugin) worldguard;
 			log.log(Level.INFO, PRE+" Plugged into WorldGuard!");
 		}
 		if(machina != null) {
 			mrb = (MachinaRedstoneBridge) machina;
 			log.log(Level.INFO, PRE+" Plugged into MachinaRedstoneBridge!");
 		}
 		if(show != null) {
 			showcase = (ShowCaseStandalone) show;
 			log.log(Level.INFO, PRE+" Plugged into Showcase!");
 		}
 	}
 	
 	private void updateHUD(){
 		Task update = new Task(this){
 			public void run(){
 				for(GunsPlusPlayer gp:GunsPlus.GunsPlusPlayers){
 					gp.getHUD().update(gp.getPlayer());
 				}
 			}
 		};
 		update.startTaskRepeating(5, false);
 	}
 	
 	private void updateTripods(){
 		if(Tripod.tripodenabled == false) return;
 		Task update = new Task(this){
 			public void run(){
 				for(TripodData td: GunsPlus.allTripodBlocks){
 					td.update();
 					TripodDataHandler.save(td);
 				}
 			}
 		};
 		update.startTaskRepeating(10, false);
 		Task save = new Task(this){
 			public void run(){
 				try {
 					ConfigLoader.dataDB.save(ConfigLoader.dataFile);
 					ConfigLoader.dataDB.load(ConfigLoader.dataFile);
 				} catch (Exception e) {
 					Util.debug(e);
 				}
 			}
 		};
 		save.startTaskRepeating(200, false);
 	}
 
 	private void resetFields() {
 		GunsPlus.allGuns.clear();
 		GunsPlus.allAmmo.clear();
 		GunsPlus.allAdditions.clear();
 		GunsPlus.allTripodBlocks.clear();
 	}
 	
 	public void reload(){
 		try{
 			ConfigLoader.config();
 			resetFields();
 			init();
 		}catch(Exception e){
 			Util.debug(e);
 			Util.warn("An error occured during reloading: "+ e.getMessage());
 		}
 	}
 	
 	public GunsPlusAPI getAPI() {
 		return api;
 	}
 
 	@Override
 	public boolean loadConfig(FileConfiguration con) {
 		try {
 			return ConfigLoader.modify(con);
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public void metricStart() {
 		try {
 			Metrics met = new Metrics(this);
 			Graph g = met.createGraph("Weapon Loadouts Used");
 			List<Loadout> list = LoadoutManager.getInstance().getLoadouts(this);
 			if(list == null || list.isEmpty()) {
 				g.addPlotter(new Metrics.Plotter("None") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			} else for(Loadout l : list) {
 				g.addPlotter(new Metrics.Plotter(l.getName()) {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 			met.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
