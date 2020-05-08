 package com.ayan4m1.multiarrow;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.ayan4m1.multiarrow.arrows.ArrowType;
 
 /**
  * MultiArrow entity listener
  * @author ayan4m1
  */
 public class MultiArrowEntityListener extends EntityListener {
 	private MultiArrow plugin;
 
 	public MultiArrowEntityListener(MultiArrow instance) {
 		plugin = instance;
 	}
 
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event instanceof EntityDamageByProjectileEvent) {
 			EntityDamageByProjectileEvent eventProjectile = (EntityDamageByProjectileEvent)event;
 			if (eventProjectile.getProjectile() instanceof Arrow) {
 				Arrow arrow = (Arrow) eventProjectile.getProjectile();
 				Entity target = eventProjectile.getEntity();
 
 				if (plugin.activeArrowEffect.containsKey(arrow)) {
 					event.setCancelled(true);
 					ArrowType arrowType = plugin.activeArrowType.get(((Player)arrow.getShooter()).getName());
 					if (plugin.chargeFee((Player)arrow.getShooter(), arrowType)) {
 						plugin.activeArrowEffect.get(arrow).hitEntity(arrow, target);
 					}
					if (plugin.config.getArrowRemove(arrowType)) {
 						arrow.remove();
 					}
 					plugin.activeArrowEffect.remove(arrow);
 				}
 			}
 		}
 	}
 }
