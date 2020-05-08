 package com.timvisee.safecreeper.manager;
 
 import java.util.logging.Logger;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.api.PVPArenaAPI;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.plugin.Plugin;
 
 public class SCPVPArenaManager {
 	
 	private Logger log;
 	
 	/**
 	 * Constructor
 	 * @param log Logger
 	 */
 	public SCPVPArenaManager(Logger log) {
 		this.log = log;
 	}
 	
 	/**
 	 * Set up the PVP Arena manager
 	 */
 	public void setup() {
 		// PVP Arena has to be installed/enabled
     	if(!Bukkit.getPluginManager().isPluginEnabled("pvparena")) {
     		this.log.info("Disabling PVPArena usage, plugin not found.");
     		return;
     	}
     	
     	try {
     		// Try to get the PVPArena plugin
     		Plugin paPlugin = (PVPArena) Bukkit.getPluginManager().getPlugin("pvparena");
 	        
     		// The plugin variable may not be null
 	        if (paPlugin == null) {
 	        	this.log.info("Unable to hook into PVPArena, plugin not found!");
 	            return;
 	        }
 	        
 	        // Hooked into PVPArena, show status message
 	        this.log.info("Hooked into PVPArena!");
 	        
     	} catch(NoClassDefFoundError ex) {
     		// Unable to hook into PVPArena, show warning/error message.
     		this.log.info("Error while hooking into PVPArena!");
     		return;
     	} catch(Exception ex) {
     		// Unable to hook into PVPArena, show warning/error message.
     		this.log.info("Error while hooking into PVPArena!");
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
 	 * Check if there's any PVP Arena at a location
 	 * @param loc Location to check on
 	 * @return True if there's any PVP Arena on this location
 	 */
 	public boolean isPVPArenaAt(Location loc) {
     	String aName = PVPArenaAPI.getArenaNameByLocation(loc);
     	return (!aName.equals(""));
     }
 	
 	/**
	 * Get the PVP Arena on a locatoin
 	 * @param loc Location to get the PVP Arena from
 	 * @return PVP Arena
 	 */
     public String getPVPArenaAt(Location loc) {
     	return PVPArenaAPI.getArenaNameByLocation(loc);
     }
 }
