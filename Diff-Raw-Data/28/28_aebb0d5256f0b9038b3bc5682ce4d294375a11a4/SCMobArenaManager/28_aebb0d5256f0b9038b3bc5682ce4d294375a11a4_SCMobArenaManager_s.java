 package com.timvisee.safecreeper.manager;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.plugin.Plugin;
 
 import com.garbagemule.MobArena.MobArena;
 import com.garbagemule.MobArena.MobArenaHandler;
 import com.garbagemule.MobArena.framework.Arena;
 
 public class SCMobArenaManager {
 	
 	private MobArenaHandler ma;
 	private Logger log;
 	
 	/**
 	 * Constructor
 	 * @param log Logger
 	 */
 	public SCMobArenaManager(Logger log) {
 		this.log = log;
 	}
 	
 	/**
 	 * Set up the hook between Safe Creeper and MobArena
 	 */
 	public void setup() {
 		// MobArena has to be installed/enabled
     	if(!Bukkit.getServer().getPluginManager().isPluginEnabled("MobArena")) {
     		this.log.info("Disabling MobArena usage, plugin not found.");
     		return;
     	}
     	
     	try {
     		// Try to get the MobArenap plugin
     		Plugin plugin = (MobArena) Bukkit.getPluginManager().getPlugin("MobArena");
 	        
 	        if (plugin == null) {
 	        	this.log.info("Unable to hook into MobArena, plugin not found!");
 	            return;
 	        }
 	        
 	        // Hooked into MobArena, show a status message
 	        this.ma = new MobArenaHandler();
 	        this.log.info("Hooked into MobArena!");
 	        
     	} catch(NoClassDefFoundError ex) {
     		// Unable to hook into MobArena, show warning/error message.
     		this.log.info("Error while hooking into MobArena!");
     		return;
     	} catch(Exception ex) {
     		// Unable to hook into MobArena, show warning/error message.
     		this.log.info("Error while hooking into MobArena!");
     		return;
     	}
 	}
 	
 	/**
 	 * Get the logger instance
 	 * @return Logger instance
 	 */
 	public Logger getLogger() {
 		return this.log;
 	}
 	
 	/**
 	 * Set the logger instance
 	 * @param log Logger instance
 	 */
 	public void setLogger(Logger log) {
 		this.log = log;
 	}
 	
 	/**
 	 * Get the mob arena handler
 	 * @return Get the mob arena handler instance
 	 */
 	public MobArenaHandler getMobArenaHandler() {
 		return this.ma;
 	}
 	
 	/**
 	 * Set the mob arena handler instance
 	 * @param ma
 	 */
 	public void setMobArenaHandler(MobArenaHandler ma) {
 		this.ma = ma;
 	}
 	
 	/**
 	 * Is a location inside any Mob Arena region
 	 * @param loc The location to check
 	 * @return True if the location is inside any arena
 	 */
 	public boolean isInRegion(Location loc) {
 		return this.ma.inRegion(loc);
 	}
 	
 	/**
 	 * Get the arena at any locatoin
 	 * @param loc Location to get the arena from
 	 * @return Arena
 	 */
 	public Arena getArenaAt(Location loc) {
 		return this.ma.getArenaAtLocation(loc);
 	}
 }
