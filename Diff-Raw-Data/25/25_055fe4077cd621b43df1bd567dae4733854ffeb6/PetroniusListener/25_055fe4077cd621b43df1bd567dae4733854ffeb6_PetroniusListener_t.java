 package me.supermaxman.petronius.Listener;
 
 import me.supermaxman.petronius.Objects.Pet;
 import me.supermaxman.petronius.main.Petronius;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.util.Vector;
 
 public class PetroniusListener implements Listener{
 	double height = 2.1;
 	int i;
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event){
 		final Player p = event.getPlayer();
 		
 		for(Pet pet : Petronius.pets){	
 			if(pet.getOwner().equalsIgnoreCase(p.getName())){
 			
            	pet.getItem().setVelocity(new Vector(
          	(p.getLocation().add(0, height, 0).toVector().subtract(pet.getItem().getLocation().toVector())).getX()*3
         	,0.01
           	,(p.getLocation().add(0, height, 0).toVector().subtract(pet.getItem().getLocation().toVector())).getZ()*3));
           	break;
 			}
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerDisconnect(PlayerQuitEvent event){
 		
	}
	
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event){
 		for(Pet pet : Petronius.pets){	
			if(pet.getItem().getUniqueId().equals(event.getItem().getUniqueId())){
				event.setCancelled(true);
 			}
 		}
 	}
 }
