 package com.censoredsoftware.Demigods.Engine.Block;
 
 import org.bukkit.Location;
 
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedModelFactory;
 
 public class BlockFactory
 {
 	public static Altar createAltar(Location location)
 	{
 		Altar altar = new Altar();
 		altar.setCenter(TrackedModelFactory.createTrackedLocation(location));
 		altar.setActive(true);
 		altar.generate();
		Altar.save(altar);
 		return altar;
 	}
 }
