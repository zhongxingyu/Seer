 package me.Kruithne.MinecartsMod;
 
import no.runsafe.framework.IConfiguration;
 
 import org.bukkit.Server;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 
 public class VehicleListener implements Listener {
 	
 	IConfiguration config;
 	Server server;
 	MinecartPowerGrid powerGrid;
 
 	public VehicleListener(IConfiguration config, Server server, MinecartPowerGrid powerGrid)
 	{
 		this.powerGrid = powerGrid;
 		this.config = config;
 		this.server = server;
 	}
 	
 	@EventHandler
 	public void onVehicleMove(VehicleMoveEvent event)
 	{
 		Vehicle theVehicle = event.getVehicle();
 		
 		if (theVehicle instanceof PoweredMinecart)
 		{
 			PoweredMinecart thePoweredMinecart = (PoweredMinecart) theVehicle;
 			int fuelLevel = this.powerGrid.getCartFuel(thePoweredMinecart);
 			
 			if (fuelLevel > -1 && thePoweredMinecart.getVelocity().length() < 2)
 				thePoweredMinecart.setVelocity(thePoweredMinecart.getVelocity().multiply(2));
 			
 			this.server.broadcastMessage("Cart " + theVehicle.getEntityId() + " fuel level: " + fuelLevel);
 		}
 	}
 	
 	@EventHandler
 	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event)
 	{
 		if (event.getVehicle() instanceof PoweredMinecart)
 			event.setCancelled(true);
 	}	
 }
