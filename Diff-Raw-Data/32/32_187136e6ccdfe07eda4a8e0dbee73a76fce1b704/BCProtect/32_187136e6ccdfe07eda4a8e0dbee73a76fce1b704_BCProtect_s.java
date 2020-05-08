 package com.github.catageek.BCProtect;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.catageek.BCProtect.Persistence.PersistentQuadtree;
 import com.github.catageek.BCProtect.Regions.RegionBuilder;
 
 public final class BCProtect extends JavaPlugin {
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static BCProtect myPlugin;
	public static boolean debugQuadtree = BCProtect.myPlugin.getConfig().getBoolean("debugQuadtree");
 	public static String logPrefix = "BCProtect.";
 
 	public static int minLeaf = 16;
 	public static int initLeaf = 512;
 	public static int MaxListSize = 16;
 	public static PersistentQuadtree tree;
 	private static RegionBuilder rb;
	public static boolean debugRegions = BCProtect.myPlugin.getConfig().getBoolean("debugRegions");
	public static boolean canbuild = BCProtect.myPlugin.getConfig().getBoolean("canbuild");
 	public static String permprefix = "bytecart.";
 	public static Location location = new Location(null, 0, 0, 0);
 
 	public void onEnable(){
 		log.info("BCProtect plugin has been enabled.");
 
 		myPlugin = this;
 
 		tree = new PersistentQuadtree();
 
 		rb = new RegionBuilder();
 
 		getServer().getPluginManager().registerEvents(new BCProtectListener(), this);
 		
 		if (! BCProtect.canbuild)
 			getServer().getPluginManager().registerEvents(new CanBuildListener(), this);
 			
 	}
 
 	public void onDisable(){ 
 		log.info("BCProtect plugin has been disabled.");
 
 		tree.close();
 		tree = null;
 		myPlugin = null;
 		log = null;
 
 	}
 
 	public static RegionBuilder getRegionBuilder() {
 		return rb;
 
 	}
 }
