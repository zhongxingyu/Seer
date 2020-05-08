 package com.github.catageek.BCProtect.Listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.github.catageek.BCProtect.BCProtect;
 import com.github.catageek.BCProtect.Regions.RegionBuilder;
 import com.github.catageek.BCProtect.Regions.RegionBuilderFactory;
 import com.github.catageek.ByteCart.Event.UpdaterPassRouterEvent;
 import com.github.catageek.ByteCart.Event.UpdaterPassStationEvent;
 
 public final class UpdaterListener implements Listener {
 	
 	UpdaterListener() {
 	}
 
 	private UpdaterMoveListener updatermovelistener;
 
 	@EventHandler
 	public void onUpdaterPassRouter(UpdaterPassRouterEvent event) {
 		if (updatermovelistener == null) {
 			updatermovelistener = new UpdaterMoveListener();
 			Bukkit.getServer().getPluginManager().registerEvents(updatermovelistener, BCProtect.myPlugin);
 		}
 		getRegionBuilder(event.getVehicleId()).onPassRouter(event.getCenter().getLocation(BCProtect.location),
 				event.getFrom(), event.getTo(), event.getIc().getName(), event.getUpdaterLevel());
 	}
 
 	@EventHandler
 	public void onUpdaterPassStation(UpdaterPassStationEvent event) {
 		getRegionBuilder(event.getVehicleId()).onPassStation(event.getIc().getBlock().getLocation(BCProtect.location),
				event.getIc().getCardinal(), event.getIc().getName(), event.getUpdaterLevel());
 	}
 
 	/**
 	 * @return a regionbuilder
 	 */
 	static RegionBuilder getRegionBuilder(int id) {
 		return RegionBuilderFactory.getRegionBuilder(id);
 	}
 }
