 package com.censoredsoftware.Demigods.Engine.Block;
 
 import org.bukkit.Location;
 
import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedModelFactory;
 
 public class BlockFactory
 {
 	public static Altar createAltar(Location location)
 	{
 		Altar altar = new Altar();
		Altar.save(altar);

		Demigods.message.broadcast("Altar: " + altar.getId());

 		altar.setCenter(TrackedModelFactory.createTrackedLocation(location));
 		altar.setActive(true);
 		altar.generate();

 		return altar;
 	}
 }
