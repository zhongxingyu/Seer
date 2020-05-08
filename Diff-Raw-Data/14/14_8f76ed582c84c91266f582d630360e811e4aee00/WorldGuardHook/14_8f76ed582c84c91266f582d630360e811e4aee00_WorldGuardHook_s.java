 package com.turt2live.antishare.regions.hooks;
 
 import org.bukkit.plugin.Plugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.regions.Selection;
 
 public class WorldGuardHook implements Hook {
 
 	private boolean hasWorldGuard;
 	private Plugin worldguard;
 
 	public WorldGuardHook(AntiShare plugin){
 		hasWorldGuard = (worldguard = plugin.getServer().getPluginManager().getPlugin("WorldGuard")) != null;
 	}
 
 	@Override
 	public boolean exists(){
 		return hasWorldGuard;
 	}
 
 	public WorldGuardPlugin getWorldGuard(){
 		return (WorldGuardPlugin) worldguard;
 	}
 
 	@Override
 	public boolean hasRegion(Selection location){
 		if(!exists()){
 			return false;
 		}
 		for(String key : getWorldGuard().getRegionManager(location.maximum.getWorld()).getRegions().keySet()){
 			ProtectedRegion region = getWorldGuard().getRegionManager(location.maximum.getWorld()).getRegion(key);
 			// Thanks to Sleaker for letting me use this code :D
 			// Modified from: https://github.com/MilkBowl/LocalShops/blob/master/src/net/milkbowl/localshops/ShopManager.java#L216
			if(location.maximum.getBlockX() > region.getMinimumPoint().getBlockX()
					|| location.minimum.getBlockX() < region.getMaximumPoint().getBlockX()){
 				continue;
			}else if(location.maximum.getBlockY() > region.getMinimumPoint().getBlockY()
					|| location.minimum.getBlockY() < region.getMaximumPoint().getBlockY()){
 				continue;
			}else if(location.maximum.getBlockZ() > region.getMinimumPoint().getBlockZ()
					|| location.minimum.getBlockZ() < region.getMaximumPoint().getBlockZ()){
 				continue;
 			}else{
 				System.out.println("Plane found!");
 				return true; // All 3 planes meet, therefore regions are in contact
 			}
 		}
 		return false; // No region
 	}
 
 	@Override
 	public String getName(){
 		return "WorldGuard Hook";
 	}
 }
