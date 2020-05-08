 package kr.kkiro.projects.bukkit.EntityProtect.events;
 
 import java.util.List;
 
 import kr.kkiro.projects.bukkit.EntityProtect.bukkit.EntityProtect;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.ChatUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.EntityActivity;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.EntityUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.PermissionUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.cache.BreedCache;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.config.Config;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.DatabaseUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.EntitySet;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.PlayerSet;
 
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 
 public class EntityListener implements Listener {
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		LivingEntity original = event.getEntity();
 		List<Entity> entities = original.getNearbyEntities(1, 1, 1);
 		if (!EntityUtils.isEnabled(original.getType()))
 			return;
 		if(event.getSpawnReason().equals(SpawnReason.EGG)) {
 			BreedCache.getInstance().refresh();
 			for (Entity entity : entities) {
 				if (!entity.getType().equals(EntityType.EGG)) continue;
 				String player = BreedCache.getInstance().findPlayer(entity);
 				if (player == null) continue;
 				if (!EntityUtils.registerEntity(player, original)) {
 					event.setCancelled(true);
 				}
 				return;
 			}
 		}
 		if(event.getSpawnReason().equals(SpawnReason.BREEDING)) {
 			BreedCache.getInstance().refresh();
 			Boolean foundPlayer = false;
 			String player = "";
 			short naturalCount = 0;
 			for (Entity entity : entities) {
 				if (!entity.getType().equals(original.getType())) continue;
 				String playerMatch = BreedCache.getInstance().findPlayer(entity);
 				if(playerMatch == null) continue;
 				if(!foundPlayer) {
 					player = playerMatch;
 					if (!EntityUtils.registerEntity(player, original)) {
 						event.setCancelled(true);
 						return;
 					}
 					foundPlayer = true;
 				}
 				if (naturalCount < 2) {
 					if(Config.getBoolean("protect-environment.set-owner-when-breeding-natural") &&
 							DatabaseUtils.searchEntity(entity.getUniqueId()) == null) {
 						if (!EntityUtils.registerEntity(player, entity, true)) {
 							if (entity instanceof Creature) {
 								((Creature) entity).setHealth(0);
 							} else {
 								entity.remove();
 							}
 						} else {
 							naturalCount += 1;
 							//TODO: try to respawn mobs
 						}
 					}
 				}
 			}
 			if (naturalCount > 0) {
 				PlayerSet playerSet = DatabaseUtils.searchPlayer(player);
 				if (playerSet == null) {
 					playerSet = new PlayerSet();
 					playerSet.setPlayer(player);
 					playerSet.setBreedCount(0);
 					DatabaseUtils.save(playerSet);
 				}
 				int breedCount = playerSet.getBreedCount();
 				int maxBreedCount = Config.getInt("general.max-entities-per-player");
 				int remainBreedCount = maxBreedCount-breedCount;
 				Player exactPlayer = EntityProtect.getInstance().getServer().getPlayerExact(player);
 				if(exactPlayer != null) {
 					ChatUtils.sendLang(exactPlayer, "breed-setowner", Integer.toString(naturalCount),
 							"#mobs."+original.getType().getName(), Integer.toString(remainBreedCount));
 				}
 				ChatUtils.sendLang(EntityProtect.getInstance().getServer().getConsoleSender(), "console.breed-setowner",
 						player, Integer.toString(naturalCount),
 						"#mobs."+original.getType().getName(), Integer.toString(remainBreedCount));
 			}
 		}
 	}
 
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		LivingEntity entity = event.getEntity();
 		if (!EntityUtils.isEnabled(entity.getType()))
 			return;
 		EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 		Player killer = null;
 		if(entity.getKiller() != null) {
 			killer = entity.getKiller();
			if(!PermissionUtils.canBypass(EntityActivity.DROP, killer, entityset)) {
 				event.getDrops().clear();
 				event.setDroppedExp(0);
 			}
 		}
 		EntityUtils.removeEntity(entity, killer.getName());
 		
 	}
 
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 		Entity entity = event.getEntity();
 		if (!EntityUtils.isEnabled(entity.getType()))
 			return;
 		if(event.getCause().equals(DamageCause.ENTITY_ATTACK)) return;
 		EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 		if(!PermissionUtils.canBypass(EntityActivity.ENVIRONMENT_DAMAGE, entityset != null)) {
 			event.setCancelled(true);
 			event.setDamage(0);
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		if(!(event.getEntity() instanceof LivingEntity)) return;
 		LivingEntity entity = (LivingEntity)(event.getEntity());
 		if (!EntityUtils.isEnabled(entity.getType()))
 			return;
 		EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 		if(event.getDamager() instanceof Player) {
 			Player player = (Player)(event.getDamager());
 			if (entityset != null) {
 				String owner = entityset.getOwnerItem().getPlayer();
 				ChatUtils.sendLang(player, "owner-info", 
 						"#mobs."+entity.getType().getName(),
 						(owner.equals(player.getName())) ? "#you" : owner);
 			}
 			if(!PermissionUtils.canBypass(EntityActivity.DAMAGE, player, entityset)) {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 			if(!PermissionUtils.canBypass(EntityActivity.DAMAGE_1HP, player, entityset)) {
 				event.setDamage(1);
 			}
 			if(!PermissionUtils.canBypass(EntityActivity.SLAY, player, entityset)) {
 				if(event.getDamage() >= entity.getHealth()) {
 					entity.setHealth(event.getDamage() + 1);
 				}
 			}
 		} else {
 			if(!PermissionUtils.canBypass(EntityActivity.MOB_DAMAGE, entityset != null)) {
 				event.setCancelled(true);
 				event.setDamage(0);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onEntityTame(EntityTameEvent event) {
 		if (!EntityUtils.isEnabled(event.getEntity().getType()))
 			return;
 		LivingEntity entity = event.getEntity();
 		AnimalTamer tamer = event.getOwner();
 		if(entity instanceof Horse) {
 			Player player = EntityProtect.getInstance().getServer().getPlayerExact(tamer.getName());
 			EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 			if(player == null) {
 				EntityProtect.severe("Tried to get Player from EntityTameEvent, but failed!");
 				event.setCancelled(true);
 				return;
 			}
 			if(!PermissionUtils.canBypass(EntityActivity.TAME_HORSE, player, entityset)) {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 			
 		}
 		if (!EntityUtils.registerEntity(tamer.getName(), entity)) {
 			event.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void onVehicleEnter(VehicleEnterEvent event) {
 		if (!EntityUtils.isEnabled(event.getVehicle().getType()))
 			return;
 		Vehicle entity = event.getVehicle();
 		EntitySet entityset = DatabaseUtils.searchEntity(entity.getUniqueId());
 		Entity rider = event.getEntered();
 		if(rider instanceof Player) {
 			Player player = (Player) rider;
 			if(!PermissionUtils.canBypass(EntityActivity.RIDE, player, entityset)) {
 				ChatUtils.sendLang(player, "access-denied");
 				event.setCancelled(true);
 				EntityUtils.playEffect(player, entity);
 				return;
 			}
 		} else {
 			if(!PermissionUtils.canBypass(EntityActivity.RIDE, entityset != null)) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 }
