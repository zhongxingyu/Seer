 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 
 public class EntityEventListener implements Listener {
 
 	public ArcherGames plugin;
 
 	public EntityEventListener(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	// TODO: Mob targeting triggers, mob damage triggers.
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onProjectileHit(final ProjectileHitEvent event) {
 		if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5) {
 			if (event.getEntity() instanceof Arrow) {
 				if (event.getEntity().getShooter() instanceof Player) {
 					event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 2F);
 					if (plugin.configToggles.get("arrowDelete")) {
 						event.getEntity().remove();
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onHungerChange(final FoodLevelChangeEvent event) {
 		if (event.getEntity() instanceof Player) {
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onMobTarget(final EntityTargetLivingEntityEvent event) {
 		if (event.getTarget() instanceof Player) {
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5) {
 				// Shouldn't be targetting them!
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onMobDamaged(final EntityDamageByEntityEvent event) {
 		if (event.getDamager() instanceof Player) {
			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5) {
 				event.setCancelled(true);
 			}
 		}
 	}
 }
