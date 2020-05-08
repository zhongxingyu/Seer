 package com.ftwinston.KillerMinecraft;
 
 import org.bukkit.Location;
 import org.bukkit.TravelAgent;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 public class PortalHelper
 {
 	PortalHelper(TravelAgent agent)
 	{
 		this.agent = agent;
 	}
 	
 	private TravelAgent agent;
 	private Location destination = null;
 	private boolean useExitPortal = true;
 
 	public Location getDestination() { return destination; }
 	public void setDestination(Location loc) { destination = loc; }
 	
 	public boolean getUseExitPortal() { return useExitPortal; }
 	public void setUseExitPortal(boolean use) { useExitPortal = use; }
 	
 	public int getExitPortalSearchRadius() { return agent.getSearchRadius(); }
 	public void setExitPortalSearchRadius(int radius) { agent.setSearchRadius(radius); }
 	
	// not clear on what this actually means
 	public int getExitPortalCreationRadius() { return agent.getCreationRadius(); }
 	public void setExitPortalCreationRadius(int radius) { agent.setCreationRadius(radius); }
 	
 	public void setupScaledDestination(World destinationWorld, Location entranceLocation, double scaleFactor)
 	{
 		setDestination(new Location(destinationWorld, (entranceLocation.getX() * scaleFactor), entranceLocation.getY(), (entranceLocation.getZ() * scaleFactor), entranceLocation.getYaw(), entranceLocation.getPitch()));
 	}
 	
 	void performTeleport(TeleportCause cause, Entity entity)
 	{
 		if ( destination == null )
 			return; // null destination means no teleporting
 		
 		if ( useExitPortal )
 			destination = agent.findOrCreate(destination);
 		
 		entity.teleport(destination, cause);
 	}
 }
