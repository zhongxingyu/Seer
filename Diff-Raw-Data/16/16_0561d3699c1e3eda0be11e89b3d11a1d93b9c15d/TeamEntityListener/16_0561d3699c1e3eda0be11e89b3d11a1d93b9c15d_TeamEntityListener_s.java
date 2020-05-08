 package com.java.phondeux.team;
 
 import java.sql.SQLException;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class TeamEntityListener extends EntityListener {
 	public Team parent;
 
 	public TeamEntityListener(Team team) {
 		this.parent = team;
 	}
 	
 	public String getDeathMessage(Entity entity) {
 		String msg = "";
 		
 		if (entity == null) {
			msg = "UKNOWN";
 		} else if (entity instanceof Blaze) {
 			msg = "BLAZE";
 		} else if (entity instanceof CaveSpider) {
 			msg = "CAVESPIDER";
 		} else if (entity instanceof Creeper) {
 			msg = "CREEPER";
 		} else if (entity instanceof EnderDragon) {
 			msg = "ENDERDRAGON";
 		} else if (entity instanceof Enderman) {
 			msg = "ENDERMAN";
 		} else if (entity instanceof Ghast) {
 			msg = "GHAST";
 		} else if (entity instanceof Giant) {
 			msg = "GIANT";
 		} else if (entity instanceof MagmaCube) {
 			msg = "MAGMACUBE";
 		} else if (entity instanceof PigZombie) {
 			msg = "PIGZOMBIE";
 		} else if (entity instanceof Player) {
			String weaponType = ((Player) entity).getItemInHand().getType().toString();
			if (weaponType.endsWith("_AXE") || weaponType.endsWith("_HOE") || weaponType.endsWith("_PICKAXE") || weaponType.endsWith("_SPADE") || weaponType.endsWith("_SWORD")) {
				msg = "WEAPON";
			} else if (weaponType.endsWith("BOW")) {
				msg = "BOW";
			} else {
 				msg = "FIST";
 			}
 		} else if (entity instanceof Projectile) {
 			if (entity instanceof Arrow) {
 				if (((Arrow) entity).getShooter() == null) {
 					msg = "DISPENSER";
 				}
 			} else if (entity instanceof Fireball) {
 				if (((Fireball) entity).getShooter() instanceof Blaze) {
 					msg = "BLAZE";
 				} else if (((Fireball) entity).getShooter() instanceof Ghast) {
 					msg = "GHAST";
 				}
 			}
 		} else if (entity instanceof Silverfish) {
 			msg = "SILVERFISH";
 		} else if (entity instanceof Skeleton) {
 			msg = "SKELETON";
 		} else if (entity instanceof Slime) {
 			msg = "SLIME";
 		} else if (entity instanceof Spider) {
 			msg = "SPIDER";
 		} else if (entity instanceof TNTPrimed) {
 			msg = "TNT";
 		} else if (entity instanceof Wolf) {
 			if (((Wolf) entity).isTamed()) {
 				msg = "TAMEWOLF";
 			} else {
 				msg = "WOLF";
 			}
 		} else if (entity instanceof Zombie) {
 			msg = "ZOMBIE";
 		}
 		return msg;
 	}
 	
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (event.getEntity() instanceof Player) {
 			EntityDamageEvent cause = event.getEntity().getLastDamageCause();
 			String victim = ((Player) event.getEntity()).getName();
 			String causeStr = "UNKNOWN";
 			Integer killerid = 0;
 			
 			if (cause instanceof EntityDamageByEntityEvent) {
 				Entity killer = ((EntityDamageByEntityEvent) cause).getDamager();
 				if (killer instanceof Player) {
 					killerid = parent.th.playerGetID(((Player) killer).getName());
 				}
 				causeStr = getDeathMessage(killer);
 			} else if (cause == null) {
				causeStr = "UNKOWN";
 			} else {
 				switch (cause.getCause()) {
 					case CONTACT:
 						causeStr = "CACTUS";
 						break;
 					case DROWNING:
 						causeStr = "DROWNING";
 						break;
 					case FALL:
 						causeStr = "FALL";
 						break;
 					case FIRE:
 					case FIRE_TICK:
 						causeStr = "FIRE";
 						break;
 					case LAVA:
 						causeStr = "LAVA";
 						break;
 					case LIGHTNING:
 						causeStr = "LIGHTNING";
 						break;
 					case STARVATION:
 						causeStr = "STARVATION";
 						break;
 					case SUFFOCATION:
 						causeStr = "SUFFOCATION";
 						break;
 					case SUICIDE:
 						causeStr = "SUICIDE";
 						break;
 					case VOID:
 						causeStr = "VOID";
 						break;
 					}
 			}
 			
 			try {
 				parent.eh.CreateEvent().PlayerDeath(killerid, parent.th.playerGetID(victim), causeStr);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
