 package com.herocraftonline.dev.heroes;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.persistance.PlayerManager;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HEntityListener extends EntityListener {
 
 	private final Heroes plugin;
 	private final PlayerManager playerManager;
 	private HashMap<Entity, Player> kills = new HashMap<Entity, Player>();
 
 	public HEntityListener(Heroes plugin) {
 		this.plugin = plugin;
 		playerManager = new PlayerManager(plugin);
 	}
 
 	public void onEntityDeath(EntityDeathEvent event) {
 		Entity defender = event.getEntity();
 		Player attacker = kills.get(defender);
 		if (attacker != null) {
 			// Get the player's class definition
 			HeroClass playerClass = plugin.getPlayerManager().getClass(attacker);
 			// Get the sources of experience for the player's class
 			Set<ExperienceType> expSources = playerClass.getExperienceSources();
 			if (expSources.contains(ExperienceType.KILLING)) {
 				if (defender instanceof LivingEntity) {
 					if (defender instanceof Player) {
 						playerManager.setExp(attacker, playerManager.getExp(attacker) + Properties.playerKillingExp);
 					} else {
 						CreatureType type = null;
 						try {
 							Class<?>[] interfaces = defender.getClass().getInterfaces();
 							for (Class<?> c : interfaces) {
 								if (LivingEntity.class.isAssignableFrom(c)) {
 									type = CreatureType.fromName(c.getSimpleName());
 									break;
 								}
 							}
 						} catch (IllegalArgumentException e) {}
 						if (type != null) {
							playerManager.setExp(attacker, playerManager.getExp(attacker) + Properties.creatureKillingExp.get("creeper"));
 						}
 					}
 				}
 			}
 		}
 		kills.remove(defender);
 	}
 
 	public void onEntityDamage(EntityDamageEvent event) {
 		Entity defender = event.getEntity();
 		if (defender instanceof LivingEntity) {
 			if (((LivingEntity) defender).getHealth() - event.getDamage() <= 0) {
 				Entity attacker = null;
 				if (event instanceof EntityDamageByProjectileEvent) {
 					EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
 					attacker = subEvent.getDamager();
 				} else if (event instanceof EntityDamageByEntityEvent) {
 					EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
 					attacker = subEvent.getDamager();
 				}
 				if (attacker != null && attacker instanceof Player) {
 					kills.put(defender, (Player) attacker);
 				} else {
 					kills.remove(defender);
 				}
 			}
 		}
 	}
 
 }
