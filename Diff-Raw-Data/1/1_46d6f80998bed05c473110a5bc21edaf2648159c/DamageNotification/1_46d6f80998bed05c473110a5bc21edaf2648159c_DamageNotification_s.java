 package com.martinbrook.tesseractuhc.notification;
 
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.martinbrook.tesseractuhc.UhcPlayer;
 
 public class DamageNotification extends UhcNotification {
 	private UhcPlayer damaged;
 	private int damageAmount;
 	private DamageCause cause;
 	private Entity damager;
 
 
 
 	public DamageNotification(UhcPlayer damaged, int damageAmount, DamageCause cause, Entity damager) {
 		super();
 		this.damaged = damaged;
 		this.damageAmount = damageAmount;
 		this.cause = cause;
 		this.damager = damager;
 	}
 
 
 	public DamageNotification(UhcPlayer damaged, int damageAmount, DamageCause cause) {
 		super();
 		this.damaged = damaged;
 		this.damageAmount = damageAmount;
 		this.cause = cause;
 		this.damager = null;
 	}
 
 
 	@Override
 	public String formatForPlayers() {
 		if (damager != null) {
 			if (damager instanceof Player) {
 				// PVP damage 
 				if (cause == DamageCause.ENTITY_ATTACK)
 					return damaged.getName() + " was hurt by " + ((Player) damager).getDisplayName() + " (" + (damageAmount / 2.0) + " hearts)!";
 				if (cause == DamageCause.PROJECTILE)
 					return damaged.getName() + " was shot at by " + ((Player) damager).getDisplayName() + " (" + (damageAmount / 2.0) + " hearts)!";
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
 				
 				return damaged.getName() + " took " + (damageAmount/2.0) + " hearts of damage from " + type;
 			}
 		}
 		// Environmental damage
 		String type = "unknown";
 		
 		if (cause == DamageCause.BLOCK_EXPLOSION) type = "TNT";
 		else if (cause == DamageCause.CONTACT) type = "cactus";
 		else if (cause == DamageCause.DROWNING) type = "drowning";
 		else if (cause == DamageCause.FALL) type = "fall";
 		else if (cause == DamageCause.FIRE) return null;
 		else if (cause == DamageCause.FIRE_TICK) type = "burning";
 		else if (cause == DamageCause.LAVA) type = "lava";
 		else if (cause == DamageCause.LIGHTNING) type = "lightning";
 		else if (cause == DamageCause.MAGIC) type = "magic";
 		else if (cause == DamageCause.POISON) type = "poison";
 		else if (cause == DamageCause.STARVATION) type = "starvation";
 		else if (cause == DamageCause.SUFFOCATION) type = "suffocation";
 		else if (cause == DamageCause.VOID) type = "void";
 		else if (cause == DamageCause.WITHER) type = "wither";
 		
 		return damaged.getName() + " took " + (damageAmount/2.0) + " hearts of " + type + " damage!";
 	}
 
 }
