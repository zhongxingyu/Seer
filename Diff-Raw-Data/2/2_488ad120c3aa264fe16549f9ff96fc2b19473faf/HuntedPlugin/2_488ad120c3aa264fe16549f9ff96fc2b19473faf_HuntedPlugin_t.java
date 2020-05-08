 package com.bendude56.hunted;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HuntedPlugin extends JavaPlugin {
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	public boolean spoutEnabled;
 	public SpConnect spoutConnect;
 	public World manhuntWorld;
 	
 	/*public boolean friendlyFire = false;
 	public boolean pvpOnly = false;
 	public boolean hostileMobs = true;
 	public boolean passiveMobs = true;
 	public int offlineTimeout = 1;
 	public boolean allowSpectators;*/
 
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "Unloaded from memory...");
 	}
 
 	@Override
 	public void onEnable() {
 		if (getServer().getPluginManager().isPluginEnabled("Spout")) {
 			spoutEnabled = true;
 			spoutConnect = new SpConnect();
 		} else {
 			log(Level.WARNING, "Spout installation not detected!");
 			spoutEnabled = false;
 		}
 		if (!new File("manhunt").exists()) {
 			log(Level.WARNING, "World 'manhunt' does not exist... Creating new world...");
 			manhuntWorld = WorldCreator.name("manhunt").environment(Environment.NORMAL).createWorld();
 		} else if (!new File("manhunt").isDirectory()) {
 			log(Level.SEVERE, "A file exists by the name of 'manhunt'! Delete/rename it and restart the server...");
 			return;
 		} else {
 			log(Level.INFO, "Loading world 'manhunt'...");
 			manhuntWorld = WorldCreator.name("manhunt").environment(Environment.NORMAL).createWorld();
 		}
 		new CmdExec();
 		new HuntedPlayerListener();
 		new HuntedEntityListener();
 		log(Level.INFO, "Version " + getDescription().getVersion() + " loaded into memory...");
 	}
 	
 	public void log(Level level, String message) {
 		log.log(level, "[" + this.getDescription().getName() + "] " + message);
 	}
 	
 	public static HuntedPlugin getInstance() {
		return (HuntedPlugin) Bukkit.getServer().getPluginManager().getPlugin("Hunted");
 	}
 	
 	public World getWorld() {
 		return manhuntWorld;
 	}
 
 }
