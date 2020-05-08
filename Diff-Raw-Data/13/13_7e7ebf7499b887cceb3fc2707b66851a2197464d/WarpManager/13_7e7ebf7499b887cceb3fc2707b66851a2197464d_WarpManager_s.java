 package com.mrz.dyndns.server.warpsuite.managers;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.mrz.dyndns.server.warpsuite.util.MyConfig;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class WarpManager
 {
 	public WarpManager(MyConfig config)
 	{
 		this.config = config;
 		warps = new HashMap<String, SimpleLocation>();
 	}
 	
 	private final MyConfig config;
 	private final Map<String, SimpleLocation> warps;
 	
 	public void loadWarpsFromConfig()
 	{
 		Set<String> warpNames = config.getCustomConfig().getConfigurationSection("Warps").getKeys(false);
 		for(String warpName : warpNames)
 		{
 			warps.put(warpName, loadWarp(warpName));
 			Util.Debug("Warp successfully loaded: " + warpName);
 		}
 	}
 	
 	public int getAmountOfSetWarps()
 	{
 		Set<String> warps = getWarpConfigurationSection();
 		if(warps == null)
 		{
 			return 0;
 		}
 		
 		return config.getCustomConfig().getConfigurationSection("Warps").getKeys(false).size();
 	}
 	
 	public boolean warpIsSet(String warpName)
 	{
 		Set<String> warps = getWarpConfigurationSection();
 		if(warps == null)
 		{
 			return false;
 		}
 		
 		return getWarpConfigurationSection().contains(warpName);
 	}
 	
 	public SimpleLocation loadWarp(String warpName)
 	{
 		warpName = warpName.toLowerCase();
 		
 		double x = config.getCustomConfig().getDouble(warpName + ".X");
 		double y = config.getCustomConfig().getDouble(warpName + ".Y");
 		double z = config.getCustomConfig().getDouble(warpName + ".Z");
 		double yaw = config.getCustomConfig().getDouble(warpName + ".Yaw");
 		double pitch = config.getCustomConfig().getDouble(warpName + ".Pitch");
 		
 		return new SimpleLocation(x, y, z, yaw, pitch);
 	}
 	
 	public void setWarp(String warpName, SimpleLocation warp)
 	{
 		String listingName = warpName;
 		warpName = warpName.toLowerCase();
 		
 		config.getCustomConfig().set("Warps." + warpName + ".X", warp.X);
 		config.getCustomConfig().set("Warps." + warpName + ".Y", warp.Y);
 		config.getCustomConfig().set("Warps." + warpName + ".Z", warp.Z);
 		config.getCustomConfig().set("Warps." + warpName + ".Yaw", warp.Yaw);
 		config.getCustomConfig().set("Warps." + warpName + ".Pitch", warp.Pitch);
 		
 		config.getCustomConfig().set("Warps." + warpName + ".ListingName", listingName);
 		
 		config.saveCustomConfig();
 	}
 	
 	private Set<String> getWarpConfigurationSection()
 	{
		return config.getCustomConfig().getConfigurationSection("Warps").getKeys(false);
 	}
 }
