 package com.joshargent.RegionPreserve;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RegionPreserveAPI {
 	
 	private RegionPreservePlugin regionPreservePlugin;
 	private JavaPlugin plugin;
 	
 	public RegionPreserveAPI(JavaPlugin plugin, RegionPreservePlugin regionPreservePlugin)
 	{
 		this.plugin = plugin;
 		this.regionPreservePlugin = regionPreservePlugin;
 	}
 	
 	/**
 	 * Will protect an area specified by a min and max corner point
 	 * 
 	 * @param name   the name of the region. can be null for random name
 	 * @param pos1   a corner point of the region
 	 * @param pos2   the other corner point
 	 * @param save   should the region be saved between server restarts? (recommend save=false)
 	 * @return       the protected region
 	 * @see          ActiveRegion
 	 */
 	public ActiveRegion protectRegion(String name, Location pos1, Location pos2, boolean save)
 	{
 		if(name == null) name = Functions.randomString(10);
 		ActiveRegion region = new ActiveRegion(name, pos1, pos2, save);
 		regionPreservePlugin.regions.add(region);
 		return region;
 	}
 	
 	/**
 	 * Returns an ActiveRegion with the name specified
 	 * 
 	 * @param name   the name of the region to return
 	 * @return       the region. Will be null if region not found
 	 * @see          ActiveRegion
 	 */
 	public ActiveRegion getRegion(String name)
 	{
 		for(ActiveRegion region : regionPreservePlugin.regions)
 		{
 			if(region.getName().equalsIgnoreCase(name)) return region;
 		}
 		return null;
 	}
 
 }
