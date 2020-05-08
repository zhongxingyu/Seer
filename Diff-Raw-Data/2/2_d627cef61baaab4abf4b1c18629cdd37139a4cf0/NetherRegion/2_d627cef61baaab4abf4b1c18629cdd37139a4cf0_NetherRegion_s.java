 package com.untamedears.netherregion;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NetherRegion  extends JavaPlugin implements Listener {
 
	private static final Logger LOG = Logger.getLogger("RealisticBiomes");
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		LOG.info("[Nether Region] is now enabled.");
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerPortalEvent(PlayerPortalEvent e) {
 		Location loc = e.getPlayer().getLocation().clone();
 		loc.setWorld(e.getTo().getWorld());
 		Location newLoc = e.getPortalTravelAgent().findOrCreate(loc);
 		e.setTo(newLoc);
 	}
 }
