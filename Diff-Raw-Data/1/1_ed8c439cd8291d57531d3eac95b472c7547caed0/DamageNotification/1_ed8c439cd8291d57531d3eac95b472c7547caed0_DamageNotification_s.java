 package com.martinbrook.tesseractuhc.notification;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.martinbrook.tesseractuhc.UhcParticipant;
 
 public class DamageNotification extends UhcNotification {
 	private UhcParticipant damaged;
 	private DamageCause cause;
 	private Entity damager;
 
 
 
 	public DamageNotification(UhcParticipant damaged, DamageCause cause, Entity damager) {
 		super();
 		this.damaged = damaged;
 		this.cause = cause;
 		this.damager = damager;
 	}
 
 
 	public DamageNotification(UhcParticipant damaged, DamageCause cause) {
 		super();
 		this.damaged = damaged;
 		this.cause = cause;
 		this.damager = null;
 	}
 
 
 	@Override
 	public String formatForPlayers() {
 		if (damager != null) {
 			if (damager instanceof Player) {
 				// PVP damage 
 				if (cause == DamageCause.ENTITY_ATTACK)
 					return ChatColor.RED + damaged.getName() + " was hurt by " + ((Player) damager).getDisplayName();
 				else
 					return null;
 			} else if (damager instanceof Projectile) {
 				LivingEntity source = ((Projectile) damager).getShooter();
 				if (source == null) {
 					if (damager.getType() == EntityType.ARROW)
 						return ChatColor.RED + damaged.getName() + " was shot ";
 					else if (damager.getType() == EntityType.SPLASH_POTION)
 						return ChatColor.RED + damaged.getName() + " was damaged by a splash potion";
 					else return null;
 				} else {
 					if (source instanceof Player) {
 						if (damager.getType() == EntityType.ARROW)
 							return ChatColor.RED + damaged.getName() + " was shot at by " + ((Player) source).getDisplayName();
 						else if (damager.getType() == EntityType.SPLASH_POTION)
 							return ChatColor.RED + damaged.getName() + " was splashed by " + ((Player) source).getDisplayName();
 					} else if (source instanceof Skeleton){
 						return ChatColor.RED + damaged.getName() + " took damage from a skeleton";
 					} else if (source instanceof Ghast) {
 						return ChatColor.RED + damaged.getName() + " took damage from a ghast fireball";
 					}
 				}
 			} else {
 				// Mob damage
 				String type = "an unknown entity";
 				if (damager.getType() == EntityType.BLAZE) type = "a blaze";
 				else if (damager.getType() == EntityType.CAVE_SPIDER) type = "a cave spider";
 				else if (damager.getType() == EntityType.CREEPER) type = "a creeper";
 				else if (damager.getType() == EntityType.ENDER_DRAGON) type = "the dragon";
 				else if (damager.getType() == EntityType.ENDERMAN) type = "an enderman";
 				else if (damager.getType() == EntityType.GHAST) type = "a ghast";
 				else if (damager.getType() == EntityType.IRON_GOLEM) type = "an iron golem";
 				else if (damager.getType() == EntityType.MAGMA_CUBE) type = "a magma cube";
 				else if (damager.getType() == EntityType.PIG_ZOMBIE) type = "a zombie pigman";
 				else if (damager.getType() == EntityType.SILVERFISH) type = "a silverfish";
 				else if (damager.getType() == EntityType.SKELETON) type = "a skeleton";
 				else if (damager.getType() == EntityType.SLIME) type = "a slime";
 				else if (damager.getType() == EntityType.WITCH) type = "a witch";
 				else if (damager.getType() == EntityType.WITHER) type = "a wither";
 				else if (damager.getType() == EntityType.WITHER_SKULL) type = "a wither skull";
 				else if (damager.getType() == EntityType.ZOMBIE) type = "a zombie";
 				else if (damager.getType() == EntityType.WOLF) {
 					AnimalTamer owner = ((Wolf) damager).getOwner();
 					if (owner != null) type = owner.getName() + "'s wolf";
 					else type = "a wolf";
 				}
 				else if (damager.getType() == EntityType.PRIMED_TNT) return null;
 				
 				return ChatColor.RED + damaged.getName() + " took damage from " + type;
 			}
 		}
 		// Environmental damage
 		String type;
 		
 		if (cause == DamageCause.BLOCK_EXPLOSION) type = "TNT";
 		else if (cause == DamageCause.CONTACT) type = "cactus";
 		else if (cause == DamageCause.DROWNING) type = "drowning";
 		else if (cause == DamageCause.FALL) type = "fall";
 		else if (cause == DamageCause.FIRE) type = "burning";
 		else if (cause == DamageCause.FIRE_TICK) type = "burning";
 		else if (cause == DamageCause.LAVA) type = "lava";
 		else if (cause == DamageCause.LIGHTNING) type = "lightning";
 		else if (cause == DamageCause.POISON) type = "poison";
 		else if (cause == DamageCause.STARVATION) type = "starvation";
 		else if (cause == DamageCause.SUFFOCATION) type = "suffocation";
 		else if (cause == DamageCause.VOID) type = "void";
 		else if (cause == DamageCause.WITHER) type = "wither";
 		else return null;
 		
 		return ChatColor.RED + damaged.getName() + " took some " + type + " damage!";
 	}
 
 }
