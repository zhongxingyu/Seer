 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import com.araeosia.ArcherGames.utils.Archer;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.*;
 
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
 					event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), new Float(plugin.arrowExplosionFactor));
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
 	public void onMobTarget(final EntityTargetEvent event) {
 		if (event.getTarget() instanceof Player) {
 			if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 2 || ScheduledTasks.gameStatus == 5) {
 				// Shouldn't be targetting them!
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onMobDamaged(final EntityDamageEvent event) {
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent damageevent = (EntityDamageByEntityEvent) event;
 			if (damageevent.getDamager() instanceof Player) {
 				if (ScheduledTasks.gameStatus == 1 || ScheduledTasks.gameStatus == 5) {
 					event.setCancelled(true);
 				} else if (!(Archer.getByName(((Player) damageevent.getDamager()).getName()).isAlive)) {
 					event.setCancelled(true);
				} else if (!(Archer.getByName(((Player) damageevent.getEntity()).getName()).isAlive)) {
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onEntityExplode(final EntityExplodeEvent event) {
 		if (event.getEntity() instanceof Block) {
 			if (((Block) event.getEntity()).getType() == Material.CHEST) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onItemDropSpawn(final ItemSpawnEvent event) {
 		if (event.getEntity() instanceof Item) {
 			if(ScheduledTasks.gameStatus==1){
 				event.getEntity().remove();
 			}
 		}
 	}
 }
