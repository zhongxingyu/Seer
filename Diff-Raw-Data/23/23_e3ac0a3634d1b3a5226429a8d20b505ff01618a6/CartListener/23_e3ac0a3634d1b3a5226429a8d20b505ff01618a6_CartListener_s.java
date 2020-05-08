 package com.vcazan.allaboard;
 
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.event.vehicle.VehicleListener;
 
 
 public class CartListener extends VehicleListener {
 
 		public final AllAboard plugin;
 		
 	    Logger log = Logger.getLogger("Minecraft");
 
 		public CartListener(AllAboard instance) {
 			
 			this.plugin = instance;
 		}
 
 		public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){
 			Vehicle cart = event.getVehicle();
 			Entity hitWhat = event.getEntity();
			
 
 			if (cart.toString() == "CraftMinecart" && plugin.enabled && hitWhat.toString().contains("CraftMinecart") == false){
 					cart.setPassenger(hitWhat);
 			}
 
			
 		}
 		
 }
 
