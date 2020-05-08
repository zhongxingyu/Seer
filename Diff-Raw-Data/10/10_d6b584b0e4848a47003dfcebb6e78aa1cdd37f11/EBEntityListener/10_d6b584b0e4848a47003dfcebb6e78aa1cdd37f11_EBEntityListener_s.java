 package net.playminecraft.enderblock;
 
 import org.bukkit.event.entity.EndermanPickupEvent;
 import org.bukkit.event.entity.EndermanPlaceEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class EBEntityListener extends EntityListener {
 
 	public EBEntityListener() {}
 	
 	@Override
 	public void onEndermanPickup(EndermanPickupEvent event) {
 		event.setCancelled(true);
 	}
 	
 	@Override
 	public void onEndermanPlace(EndermanPlaceEvent event) {
 		event.setCancelled(true);
 	}
 }
