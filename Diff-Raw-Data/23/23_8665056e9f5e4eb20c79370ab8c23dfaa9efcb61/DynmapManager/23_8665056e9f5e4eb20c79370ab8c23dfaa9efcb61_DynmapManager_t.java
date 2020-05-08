 package me.cmastudios.plugins.WarhubModChat;
 
 import org.dynmap.DynmapAPI;
 import org.dynmap.DynmapCommonAPI;
 
 public class DynmapManager {
 	private static WarhubModChat plugin;
 	private static boolean enabled = false;
 	public static void disable() {
 		enabled = false;
 	}
 	public static void enable(WarhubModChat instance) {
 		plugin = instance;
		enabled = true;
 	}
 	public static DynmapAPI getDynmapAPI() {
 		if (!enabled) return null;
     	return (DynmapAPI)plugin.getServer().getPluginManager().getPlugin("dynmap");
 	}
 	public static DynmapCommonAPI getDynmapCommonAPI() {
 		if (!enabled) return null;
     	return (DynmapCommonAPI)plugin.getServer().getPluginManager().getPlugin("dynmap");
 	}
 }
