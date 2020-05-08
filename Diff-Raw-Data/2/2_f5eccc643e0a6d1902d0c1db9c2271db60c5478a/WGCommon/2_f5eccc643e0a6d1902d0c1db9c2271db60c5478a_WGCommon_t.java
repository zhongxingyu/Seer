 package com.wolflink289.bukkit.worldregions.misc;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import com.sk89q.worldguard.bukkit.ConfigurationManager;
 import com.sk89q.worldguard.bukkit.WorldConfiguration;
 import com.sk89q.worldguard.protection.flags.Flag;
 import com.wolflink289.bukkit.worldregions.WorldRegionsPlugin;
 
 /**
  * Common WorldGuard flag checks.
  * 
  * @author Wolflink289
  */
 public class WGCommon {
 	
 	/**
 	 * Are regions diabled for the specified world?
 	 * 
 	 * @param world the world to check.
 	 * @return true if regions are disabled.
 	 */
 	static public boolean areRegionsDisabled(World world) {
 		ConfigurationManager cfg = WorldRegionsPlugin.getWorldGuard().getGlobalStateManager();
 		WorldConfiguration wcfg = cfg.get(world);
 		
 		return !wcfg.useRegions;
 	}
 	
 	/**
 	 * Will the flag apply to the player?
 	 * 
 	 * @param player the player to check.
 	 * @param flag the flag to check.
 	 * @return true if the flag applies to the player.
 	 */
 	static public boolean willFlagApply(Player player, Flag<?> flag) {
 		// Permission?
		if (player.hasPermission("worldregions.bypass.flag." + flag.getName())) return false;
 		
 		// Bypass?
 		// TODO fix: if (WorldRegionsPlugin.getWorldGuard().getGlobalRegionManager().hasBypass(player, player.getWorld())) return false;
 		
 		return true;
 	}
 }
