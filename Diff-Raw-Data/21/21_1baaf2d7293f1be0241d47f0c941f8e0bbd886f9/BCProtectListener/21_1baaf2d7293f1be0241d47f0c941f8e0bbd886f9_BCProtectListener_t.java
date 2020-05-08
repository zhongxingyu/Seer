 package com.github.catageek.BCProtect.Listeners;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.WorldInitEvent;
 
 import com.github.catageek.BCProtect.BCProtect;
 import com.github.catageek.BCProtect.Util;
 import com.github.catageek.BCProtect.Regions.RegionBuilderFactory;
 import com.github.catageek.ByteCart.Event.SignCreateEvent;
 import com.github.catageek.ByteCart.Event.UpdaterCreateEvent;
 
 public final class BCProtectListener implements Listener {
 
 	private UpdaterListener updaterlistener;
 
 	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onUpdaterCreate(UpdaterCreateEvent event) {
 		if(updaterlistener == null) {
 			updaterlistener = new UpdaterListener();
 			Bukkit.getServer().getPluginManager().registerEvents(updaterlistener, BCProtect.myPlugin);
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Util.getQuadtree(event.getBlock()).remove(Util.getPoint(BCProtect.location));
 	}
 
 	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
 	public void onSignCreate(SignCreateEvent event) {
 		String name;
 		if ((name = event.getStrings()[1]).equals("BC9001"))
 			RegionBuilderFactory.getTempRegionBuilder().onCreateStation(event.getIc().getBlock().getLocation(BCProtect.location),
 					event.getIc().getCardinal().getOppositeFace(), name);
 	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onWorldInit(WorldInitEvent event) {
		BCProtect.getQuadtreeManager().addWorld(event.getWorld().getName());
	}
 }
