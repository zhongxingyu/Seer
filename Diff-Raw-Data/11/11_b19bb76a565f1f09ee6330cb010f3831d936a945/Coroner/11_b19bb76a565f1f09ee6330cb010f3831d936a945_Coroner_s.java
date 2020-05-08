 package net.minebot.causeofdeath;
 
 import java.util.Stack;
 
 import org.bukkit.Material;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class Coroner {
 
 	public Entity getKiller() {
 		return killer;
 	}
 
 	public Player getWolfOwner() {
 		return wolfOwner;
 	}
 
 	public String getWeapon() {
 		return weapon;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Stack<String> getCauses() {
 		return (Stack<String>)causes.clone();
 	}
 
 	private Entity killer = null;
 	private Player wolfOwner = null;
 	private String weapon = null;
 	private Stack<String> causes = null;
 	
 	public Coroner(EntityDeathEvent event) {
 		causes = new Stack<String>();
 		
 		determineCause(event);
 	}
 	
 	private void determineCause(EntityDeathEvent event) {
 		causes.push("generic"); //Fallback in case nothing else turns up
 		
 		//Raw cause of death
 		EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
 		
 		//Damage by another entity?
 		if (damageEvent instanceof EntityDamageByEntityEvent) {
 			causes.push("entity");
 			Entity damager = ((EntityDamageByEntityEvent) damageEvent).getDamager();
 			killer = damager;
 			
 			if (damager instanceof Player) {
 				//A murder most foul!
 				causes.push("pvp");
 				Material weaponType = ((Player)damager).getItemInHand().getType();
 				
 				if (weaponType == Material.AIR) {
 					causes.push("pvp_fists");
 					weapon = "fists";
 				}
 				else {
 					weapon = weaponType.toString();
 				}
 			}
 			
 			else if (damager instanceof Creature || damager instanceof Slime) {
 				//Slain by monster
 				causes.push("monster");
 				
 				if (damager instanceof Tameable && ((Tameable)damager).isTamed()) {
 					//PVP Wolf attack?
 					causes.push("pvp_wolf");
 					wolfOwner = (Player)((Tameable)damager).getOwner();
 				}
 				else {
 					String mobType = getCreatureType(damager);
 					if (mobType != null) causes.push(mobType);
 				}
 			}
 			
 			else if (damager instanceof Projectile) {
 				//Arrow or other projectile
 				causes.push("projectile");
 				if (damager instanceof Arrow) {
 					causes.push("arrow");
 				}
 				else if (damager instanceof Fireball) {
 					//probable Ghast
 					causes.push("fireball");
 				}
 				if (((Projectile) damager).getShooter() instanceof Player) {
 					causes.push("pvp");
 					causes.push("pvp_projectile");
 					weapon = ((Projectile)damager).toString().replace("Craft", "");
 					killer = (Player)((Projectile)damager).getShooter();
 				}
 				if (((Projectile)damager).getShooter() == null) {
 					causes.push("dispenser");
 					weapon = ((Projectile)damager).toString().replace("Craft", "");
 				}
 			}
 			
 			else if (damager instanceof TNTPrimed) {
 				causes.push("explosion");
 				causes.push("tnt");
 			}
 		}
 		
 		//All other death causes
 		else if (damageEvent != null) {
 			String cause = damageEvent.getCause().toString().toLowerCase();
 			if (cause.equals("fire_tick")) cause = "fire";
 			if (cause.contains("explosion")) cause = "explosion";
 			causes.push(cause);
 		}
 	}
 	
 	public String getCreatureType(Entity entity) {
         if (entity instanceof Blaze)
             return "blaze";
         if (entity instanceof CaveSpider)
             return "cave_spider";
         if (entity instanceof Chicken)
             return "chicken";
         if (entity instanceof Cow)
             return "cow";
         if (entity instanceof Creeper)
             return "creeper";
         if (entity instanceof EnderDragon)
             return "enderdragon";
         if (entity instanceof Enderman)
             return "enderman";
         if (entity instanceof Ghast)
             return "ghast";
         if (entity instanceof Giant)
             return "giant";
         if (entity instanceof MagmaCube)
             return "magma_cube";
         if (entity instanceof MushroomCow)
             return "mooshroom";
         if (entity instanceof Pig)
             return "pig";
         if (entity instanceof PigZombie)
             return "zombie_pigman";
         if (entity instanceof Sheep)
             return "sheep";
         if (entity instanceof Skeleton)
             return "skeleton";
         if (entity instanceof Slime)
             return "slime";
         if (entity instanceof Silverfish)
             return "silverfish";
         if (entity instanceof Snowman)
             return "snowgolem";
         if (entity instanceof Spider)
             return "spider";
         if (entity instanceof Squid)
             return "squid";
         if (entity instanceof Villager)
             return "villager";
         if (entity instanceof Zombie)
             return "zombie";
         if (entity instanceof Wolf)
             return "wolf";
         
         return null;
 	}
 }
