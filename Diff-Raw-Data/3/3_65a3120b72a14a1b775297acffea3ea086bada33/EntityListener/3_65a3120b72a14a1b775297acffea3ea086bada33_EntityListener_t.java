 package com.gmail.metoray.ScubaDiver;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class EntityListener implements Listener {
 	@EventHandler
 	public void onPlayerDamage(EntityDamageEvent event){
 		if(event.getEntityType() != EntityType.PLAYER){
 			return;
 		}
 		Player p = (Player)event.getEntity();
		if(p.getInventory().getHelmet()==null){
			return;
		}
 		if(p.getInventory().getHelmet().getType()==Material.GLASS){
 			event.setCancelled(true);
 		}
 	}
 }
