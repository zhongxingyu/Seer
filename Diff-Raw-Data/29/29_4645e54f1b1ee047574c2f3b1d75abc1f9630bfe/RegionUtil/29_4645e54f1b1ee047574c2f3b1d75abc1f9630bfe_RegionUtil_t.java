 package com.wolflink289.bukkit.worldregions.util;
 
 import org.bukkit.Location;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.Flag;
 import com.sk89q.worldguard.protection.flags.IntegerFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.wolflink289.bukkit.worldregions.WorldRegionsPlugin;
 
 public class RegionUtil {
 	
 	// Constructor
 	private RegionUtil() {
 	}
 	
 	// Methods
 	/**
 	 * Is the state flag allowed in the specified location?
 	 * 
 	 * @param flag the flag to check
 	 * @param location the specified location
 	 * @return if the flag is allowed
 	 */
 	static public boolean getFlag(StateFlag flag, Location location) {
 		return WorldRegionsPlugin.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location).allows(flag);
 	}
 	
 	// TODO javadoc
 	static public Object getFlagAsObject(Flag<?> flag, Location location) {
 		ApplicableRegionSet regions = WorldRegionsPlugin.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		return regions.getFlag(flag);
 	}
 	
 	// TODO javadoc
 	static public Object getFlag(Flag<?> flag, Location location) {
 		ApplicableRegionSet regions = WorldRegionsPlugin.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		return regions.getFlag(flag);
 	}
 	
 	// TODO javadoc
 	static public boolean getFlagExists(Flag<?> flag, Location location) {
 		ApplicableRegionSet regions = WorldRegionsPlugin.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
 		
 		for (ProtectedRegion region : regions) {
 			if (region.getFlag(flag) == null) continue;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	// TODO javadoc
 	static public Integer getFlag(IntegerFlag flag, Location location) {
 		ApplicableRegionSet regions = WorldRegionsPlugin.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		return (Integer) regions.getFlag(flag);
 	}
 }
