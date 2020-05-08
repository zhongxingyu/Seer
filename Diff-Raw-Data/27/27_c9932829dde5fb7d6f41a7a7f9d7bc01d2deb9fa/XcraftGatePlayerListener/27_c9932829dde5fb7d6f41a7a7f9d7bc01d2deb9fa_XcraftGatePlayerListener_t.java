 package de.xcraft.engelier.XcraftGate;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class XcraftGatePlayerListener extends PlayerListener {
 	private Location location;
 	private String gateName = null;	
 	private XcraftGate plugin = null;
 
 	public XcraftGatePlayerListener(XcraftGate instance) {
 		plugin = instance;
 	}
 
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Location portTo = null;
 		Location portFrom = null;
 
 		location = event.getTo();
 		
		int border = plugin.worlds.get(location.getWorld().getName()).border;
		if (border > 0) {
			double x = location.getX();
			double z = location.getZ();
		
			if (Math.abs(x) >= border || Math.abs(z) >= border) {
				x = Math.abs(x) >= border ? (x > 0 ? border - 1 : -border + 1) : x;
				z = Math.abs(z) >= border ? (z > 0 ? border - 1 : -border + 1) : z;
 				event.setCancelled(true);
				event.getPlayer().teleport(new Location(location.getWorld(), x, location.getY(), z, location.getYaw(), location.getPitch()));
				event.getPlayer().sendMessage(ChatColor.RED	+ "You reached the border of this world.");
			}			
 		}
		
 		portTo = plugin.justTeleported.get(event.getPlayer().getName());		
 		portFrom = plugin.justTeleportedFrom.get(event.getPlayer().getName());
 
 		if (portTo != null && portFrom == null)
 			plugin.justTeleported.remove(event.getPlayer().getName());
 		
 		if (portTo == null && portFrom != null)
 			plugin.justTeleportedFrom.remove(event.getPlayer().getName());
 		
 		if (portTo != null && portFrom != null) {
 			if ((Math.floor(portTo.getX()) != Math.floor(location.getX()) || Math.floor(portTo.getZ()) != Math.floor(location.getZ()))
 				&& (Math.floor(portFrom.getX()) != Math.floor(location.getX()) || Math.floor(portFrom.getZ()) != Math.floor(location.getZ()))) {
 				plugin.justTeleported.remove(event.getPlayer().getName());
 				plugin.justTeleportedFrom.remove(event.getPlayer().getName());
 			}
 		} else if ((gateName = plugin.gateLocations.get(plugin.getLocationString(location))) != null) {
 			plugin.justTeleportedFrom.put(event.getPlayer().getName(), plugin.gates.get(gateName).gateLocation);
 			plugin.gates.get(gateName).portToTarget(event);
 		}
 	}
 }
