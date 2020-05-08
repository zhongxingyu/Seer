 package de.xcraft.engelier.XcraftGate;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class XcraftGateGatePort implements Runnable {
 
 	private Player player;
 	private Location location;
 	
 	public XcraftGateGatePort(Player player, Location location) {
 		this.player = player;
 		this.location = location;
 	}
 	
 	@Override
 	public void run() {
/*
 * 		This got fixed in CB819
 *  
 *		if (player.getWorld() != this.location.getWorld()) {
 *			// trigger an extra teleport just to change the world
 *			// bukkit doesn't implement world- and location-changing in one single step
 *			player.teleport(this.location); 
 *		}
 */
 		this.player.teleport(this.location);
 	}
 	
 }
