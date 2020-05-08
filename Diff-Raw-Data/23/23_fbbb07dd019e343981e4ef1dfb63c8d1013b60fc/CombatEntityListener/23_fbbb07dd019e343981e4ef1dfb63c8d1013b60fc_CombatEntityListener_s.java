 package com.sleaker.combatevents;
 
 import net.milkbowl.administrate.AdminHandler;
 
 import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class CombatEntityListener extends EntityListener {
 	
 	private CombatEventsCore plugin;
 	
 	CombatEntityListener(CombatEventsCore plugin) {
 		this.plugin = plugin;
 	}
 	
	public void onEntityDamaged(EntityDamageEvent event) {
 		//Disregard this event?
 		if (event.isCancelled() || !isValidEntity(event.getEntity()))
 			return;
 		
 		//Convert the entity.
 		LivingEntity cEntity = (LivingEntity) event.getEntity();
 		
 		//Lets only Log this event if it's going to kill the attacked
 		if (cEntity.getHealth() - event.getDamage() <= 0 ) {
 			if (event instanceof EntityDamageByEntityEvent) {
 				EntityDamageByEntityEvent thisEvent = (EntityDamageByEntityEvent) event;
 				
 				if (thisEvent.getDamager() instanceof LivingEntity) {
 					LivingEntity attacker = (LivingEntity) thisEvent.getDamager();
 					//Check if this is a player and we have an admin plugin enabled - otherwise just add to the map
 					if (attacker instanceof Player && plugin.admins != null) {
 						if (!AdminHandler.isGod(((Player) attacker).getName()) && !AdminHandler.isInvisible(((Player) attacker).getName()))
 								plugin.addAttacker(cEntity, attacker);
 					} else
 						plugin.addAttacker(cEntity, attacker);
 				}
 			} else if (event instanceof EntityDamageByProjectileEvent ){
 				EntityDamageByProjectileEvent thisEvent = (EntityDamageByProjectileEvent) event;
 				if (thisEvent.getDamager() instanceof LivingEntity) {
 					LivingEntity attacker = (LivingEntity) thisEvent.getDamager();
 					//Check if this is a player and we have an admin plugin enabled - otherwise just add to the map
 					if (attacker instanceof Player && plugin.admins != null) {
 						if (!AdminHandler.isGod(((Player) attacker).getName()) && !AdminHandler.isInvisible(((Player) attacker).getName()))
 								plugin.addAttacker(cEntity, attacker);
 					} else
 						plugin.addAttacker(cEntity, attacker);
 				}
 			}
 		}	
 	}
 	
 	public void onEntityDeath (EntityDeathEvent event) {
 		//Reasons to disregard this event
 		if ( !isValidEntity(event.getEntity()) )
 			return;
 
 		LivingEntity cEntity = (LivingEntity) event.getEntity();
 
 		//Fire our event if the entity dying is in the killMap
 		if ( plugin.getAttacker(cEntity) != null ) {
 			EntityKilledByEntityEvent kEvent = new EntityKilledByEntityEvent(plugin.getAttacker(cEntity), cEntity, event.getDrops());
 			plugin.getServer().getPluginManager().callEvent(kEvent);
 			//Reset our drops.
 			event.getDrops().addAll(kEvent.getDrops());
 			plugin.removeKilled(cEntity);
 		}
 	}
 	
 	
 	private boolean isValidEntity (Entity thisEntity) {
		if ( !(thisEntity instanceof LivingEntity) || thisEntity instanceof HumanEntity )
 			return false;
 
 		return true;
 	}
 }
