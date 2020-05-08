 package com.craftminecraft.plugins.bansync.plugins;
 
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.World;
 import org.bukkit.plugin.Plugin;
 
 import com.craftminecraft.plugins.bansync.BanSync;
 import com.craftminecraft.plugins.bansync.log.LogLevels;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class WorldGuardHook {
 	private BanSync bansyncinterface = null;
 	private Boolean pluginHooked = false;
 	public static WorldGuardPlugin wg = null;
 	
 	public WorldGuardHook (BanSync p) {
 		bansyncinterface = p;
 	}
 	
 	public Boolean isHooked()
 	{
 		return pluginHooked;
 	}
 	
 	public Boolean HookWorldGuard()
 	{
 		Plugin p = bansyncinterface.getServer().getPluginManager().getPlugin("WorldGuard");
 		if (p != null && p instanceof WorldGuardPlugin) {
 			pluginHooked = true;
 			bansyncinterface.logger.log(LogLevels.INFO, "WorldGuard Found, hooking into WorldGuard.");
 			wg = (WorldGuardPlugin) p;
 			return true;
 		} else {
 			pluginHooked = false;
 			bansyncinterface.logger.log(LogLevels.INFO, "WorldGuard not Found");
 			return false;
 		}
 	}
 	
 	public void ClearWorldGuardRegions(String playerName)
 	{
 		// Do Something...
 		List<World> worlds = bansyncinterface.getServer().getWorlds();
 		
 		for (World w : worlds) {
 			RegionManager rm = wg.getRegionManager(w);
 			Map<String, ProtectedRegion> regions = rm.getRegions();
 			
 			for (String id : regions.keySet()) {
 				if (regions.get(id).getOwners().getPlayers().contains(playerName)) {
					if (regions.get(id).getOwners().getPlayers().size() > 1)
					{
						bansyncinterface.logger.log(LogLevels.INFO, "Found region " + id + ", Removing player as an owner");
						rm.getRegionExact(id).getOwners().removePlayer(playerName);
						try {
							rm.save();
						} catch (ProtectionDatabaseException e) {
							bansyncinterface.logger.log(LogLevels.INFO, "Error removing region " + id + ", " + e.getMessage());
						}
					}
					else
					{
						bansyncinterface.logger.log(LogLevels.INFO, "Found region " + id + ", Removing it");
						rm.removeRegion(id);
						try {
							rm.save();
						} catch (ProtectionDatabaseException e) {
							bansyncinterface.logger.log(LogLevels.INFO, "Error removing region " + id + ", " + e.getMessage());
						}
 					}
 				}
 			}
 		}
 	}
 }
