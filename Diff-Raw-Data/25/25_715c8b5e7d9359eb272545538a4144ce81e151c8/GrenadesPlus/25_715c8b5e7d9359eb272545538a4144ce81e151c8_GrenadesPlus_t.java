 package team.GrenadesPlus;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import team.ApiPlus.API.PluginPlus;
 import team.ApiPlus.API.Type.BlockType;
 import team.ApiPlus.API.Type.ItemType;
 import team.ApiPlus.Manager.Loadout.Loadout;
 import team.ApiPlus.Manager.Loadout.LoadoutManager;
 import team.GrenadesPlus.API.GrenadesPlusAPI;
 import team.GrenadesPlus.Block.Placeable;
 import team.GrenadesPlus.Controls.KeyType;
 import team.GrenadesPlus.Controls.ThrowBinding;
 import team.GrenadesPlus.Item.Detonator;
 import team.GrenadesPlus.Item.Throwable;
 import team.GrenadesPlus.Manager.ConfigLoader;
 import team.GrenadesPlus.Manager.DBManager;
 import team.GrenadesPlus.Trigger.TriggerListener;
 import team.GrenadesPlus.Util.Metrics;
 import team.GrenadesPlus.Util.Metrics.Graph;
 import team.GrenadesPlus.Util.Util;
 import team.GunsPlus.GunsPlus;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class GrenadesPlus extends PluginPlus{
 	public static final String PRE = "[Grenades+] ";
 	public static final Logger log = Bukkit.getLogger();
 	
 	public static GrenadesPlus plugin;
 	
 	public static GunsPlus gunsplus;
 	public static LWC lwc;
 	public static WorldGuardPlugin wg;
 	public static boolean useFurnaceAPI = false;
 	
 	private GrenadesPlusAPI api;
 	
 	public static boolean warnings = true;
 	public static boolean debug = false;
 	public static boolean notifications = true;
 	public static boolean autoreload = true;
 	public static KeyType throwType = new KeyType("G", true);
 	
 	public static Map<String,  Class<? extends ItemType>> customItemTypes = new HashMap<String, Class<? extends ItemType>>();
 	public static Map<String,  Class<? extends BlockType>> customBlockTypes = new HashMap<String, Class<? extends BlockType>>();
 	static{
 		customItemTypes.put("Throwable", Throwable.class);
 		customItemTypes.put("Detonator", Detonator.class);
 		customBlockTypes.put("Placeable", Placeable.class);
 	}
 	
 	public static List<Throwable> allThrowables = new ArrayList<Throwable>();
 	public static List<Placeable> allPlaceables = new ArrayList<Placeable>();
 	public static Map<Placeable, HashMap<BlockFace, Placeable>> wallDesignPlaceables = new HashMap<Placeable, HashMap<BlockFace, Placeable>>();
 	public static Detonator detonator;
 	public static Set<GrenadesPlusPlayer> GrenadesPlusPlayers = new HashSet<GrenadesPlusPlayer>();
 	
 	@Override
 	public void onDisable() {
 		for(Block b : new HashSet<Block>(DBManager.blocks.keySet())){
 			try {
 				DBManager.updateBlock(DBManager.blocks.get(b), true);
 			} catch (SQLException e) {
 				Util.warn(e.getMessage());
 				Util.debug(e);
 			}
 		}
 		log.log(Level.INFO, PRE + " version " + getDescription().getVersion()
 				+ " is now disabled.");
 	}
 	
 	@Override
 	public void onEnable() {
 		plugin = this;
 		ConfigLoader.config();
 //		new VersionChecker(this, ""); TODO: Add rss url
 		this.registerBlockTypes(customBlockTypes);
 		this.registerItemTypes(customItemTypes);
		hook();
 		init();
		registerPluginPlus();
		Util.printIDs();
 		try{
 			DBManager.init();
 		}catch(SQLException e){
 			Util.warn(e.getMessage());
 		}
 		getCommand("grenades+").setExecutor(new CommandEx(this));
 		new GrenadesPlusListener(this);
 		new TriggerListener(this);
 		new ThrowBinding(this, throwType);
 		api = new GrenadesPlusAPI(this);
		metricStart();
 		log.log(Level.INFO, PRE + " version " + getDescription().getVersion()+ " is now enabled.");
 	}
 	
 	private void init(){
 		ConfigLoader.loadGeneral();
 		ConfigLoader.loadThrowables();
 		ConfigLoader.loadPlaceables();
 		ConfigLoader.loadRecipes();
 	}
 	
 	private void hook(){
 		Plugin spout = getServer().getPluginManager().getPlugin("Spout");
 		Plugin gp = getServer().getPluginManager().getPlugin("GunsPlus");
 		Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
 		Plugin furnaceAPI = getServer().getPluginManager().getPlugin("FurnaceAPI");
 		Plugin worldguard = getServer().getPluginManager().getPlugin("WorldGuardPlugin");
 		if(spout != null) {
 		    log.log(Level.INFO, PRE+" Plugged into Spout!");
 		}else{
 			log.log(Level.INFO, PRE+" disableing because Spout is missing!");
 			this.setEnabled(false);
 		}
 		if(gp != null) {
 			gunsplus = (GunsPlus) gp;
 			log.log(Level.INFO, PRE+" Plugged into Guns+!");
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
 	}
 	
 	public GrenadesPlusAPI getAPI() {
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
 	
 	public void reload() {
 		allPlaceables.clear();
 		allThrowables.clear();
 		detonator = null;
 		wallDesignPlaceables.clear();
 		init();
 	}
 	
 	public void metricStart() {
 		try {
 			Metrics met = new Metrics(this);
 			Graph g = met.createGraph("Explosive Loadouts Used");
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
