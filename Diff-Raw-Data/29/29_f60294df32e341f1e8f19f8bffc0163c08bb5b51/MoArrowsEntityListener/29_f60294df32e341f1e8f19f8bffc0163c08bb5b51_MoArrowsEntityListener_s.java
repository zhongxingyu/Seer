 package com.gmail.l0g1clvl.MoArrows;
 
 import java.util.List;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import com.gmail.l0g1clvl.MoArrows.arrows.ArrowType;
 import com.gmail.l0g1clvl.MoArrows.arrows.ArrowEffect;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.util.Random;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Listens for entity events and raises arrow effect events
  * @author MrAverage with code from ayan4m1
  */
 
 public class MoArrowsEntityListener extends JavaPlugin implements Listener {
 	private MoArrows plugin;
 	public static double damageMultiplier;
 	public String sendToArrow;
 	public String sendToDamage;
 	public static Boolean canShoot;
 
 	@EventHandler
 	public void onProjectileHit(ProjectileHitEvent event) {
 		WorldGuardHook wg = new WorldGuardHook();
 	
 		sendToArrow = "";
 		sendToDamage = "";
 		
 		//Need this in BOTH onProjectileHit AND onEntityDamage
 		ArrowType arrowType = ArrowType.Normal; 			
 
 		if (!(event.getEntity() instanceof Arrow)) {
 			return;
 		}
 
 		Arrow arrow = (Arrow)event.getEntity();
 		//plugin.log.info("arrow = " + event.getEntity().getEntityId());
 		if (!(arrow.getShooter() instanceof Player)) {
 			return;
 		}
 	
 		//Checks arrowID list for fired arrows, then assigns type
 		//based on the arrows unique ID.
 		for (int i = 0; i < plugin.arrowID.length; i++) {
 			if (plugin.arrowID[i].contains("" + event.getEntity().getEntityId())) {
 				String delim = "[.]";
 				String parse[] = plugin.arrowID[i].split(delim);
 				sendToArrow = parse[parse.length-2];
 				sendToDamage = parse[parse.length-1];
				plugin.log.info("#debug at:" + parse[parse.length-2] + " id:" + event.getEntity().getEntityId() + " stance: " + sendToDamage);
 				plugin.arrowID[i] = "";
 			}
 		}
 	
 		
 		//This removes server errors for arrows falling as 
 		//a result of block desruction.
 		if (sendToArrow != "") {
 			arrowType = ArrowType.valueOf(sendToArrow);
 		}
 		
 		//Ignore this event if there is a targetable entity within one block
 		List<Entity> entities = arrow.getNearbyEntities(1D, 1D, 1D);
 		int entCount = entities.size();
 		for(Entity ent : entities) {
 			if ((ent instanceof Arrow) || (ent instanceof Item) || (ent == arrow.getShooter())) {
 				entCount--;
 			}
 		}
 
 		//Only raise the onGroundHitEvent if there are no valid entities nearby
 		if (entCount == 0) {
 			if (arrowType != ArrowType.Normal) {
 				if (true) {
 					ArrowEffect arrowEffect = null;
 					String className = "com.gmail.l0g1clvl.MoArrows.arrows." + arrowType.toString() + "ArrowEffect";
 					try {
 						arrowEffect = (ArrowEffect)Class.forName(className).newInstance();
 					} catch (ClassNotFoundException e) {
 						plugin.log.warning("Failed to find class " + className);
 					} catch (InstantiationException e) {
 						plugin.log.warning("Could not instantiate class " + className);
 					} catch (IllegalAccessException e) {
 						plugin.log.warning("Could not access class " + className);
 					}
 
 					Player player = (Player) arrow.getShooter();
 					Location location = arrow.getLocation();
 					Boolean canShoot = wg.canShoot(player, arrow.getLocation());
 					if (canShoot) {
 						arrowEffect.onGroundHitEvent(arrow);
 					} else {
 						player.sendMessage(ChatColor.RED + "You are firing into a protected area!");
 						plugin.log.warning("[MoArrows] " + player.getName() + " tried to fire into protected area at X:" 
 								+ arrow.getLocation().getX() + ", Y:" + + arrow.getLocation().getY() + ", Z:" + 
 								arrow.getLocation().getZ());
 					}
 					sendToDamage = "s";
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 		
 		WorldGuardHook wg = new WorldGuardHook();
 		
 		//Need this in BOTH onProjectileHit AND onEntityDamage
 		ArrowType arrowType = ArrowType.Normal;	
 				
 		if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
 			return;
 		}
 
 		if (!(event instanceof EntityDamageByEntityEvent)) {
 			return;
 		}
 
 		EntityDamageByEntityEvent ebe = (EntityDamageByEntityEvent)event;
 		if (!(ebe.getDamager() instanceof Arrow)) {
 			return;
 		}
 		
 		Arrow arrow = (Arrow)ebe.getDamager();
 		if (!(arrow.getShooter() instanceof Player)) {
 			return;
 		}
 		
 		//This removes server errors for arrows falling as 
 		//a result of block desruction.
 		if (sendToArrow != "") {
 			arrowType = ArrowType.valueOf(sendToArrow);
 		}
 
 		if (arrowType != ArrowType.Normal) {
 				ArrowEffect arrowEffect = null;
 				String className = "com.gmail.l0g1clvl.MoArrows.arrows." + arrowType.toString() + "ArrowEffect";
 			
 				try {
 					arrowEffect = (ArrowEffect)Class.forName(className).newInstance();
 				} catch (ClassNotFoundException e) {
 					plugin.log.warning("Failed to find class " + className);
 				} catch (InstantiationException e) {
 					plugin.log.warning("Could not instantiate class " + className);
 				} catch (IllegalAccessException e) {
 					plugin.log.warning("Could not access class " + className);
 				}
 				
 				Player player = (Player) arrow.getShooter();
 				Location location = arrow.getLocation();
 				Boolean canShoot = wg.canShoot(player, arrow.getLocation());
 				if (canShoot) {
 					arrowEffect.onEntityHitEvent(arrow, event.getEntity());
 				} else {
 					player.sendMessage(ChatColor.RED + "You are firing into a protected area!");
 					plugin.log.warning("[MoArrows] " + player.getName() + " tried to fire into protected area at X:" 
 							+ arrow.getLocation().getX() + ", Y:" + + arrow.getLocation().getY() + ", Z:" + 
 							arrow.getLocation().getZ());
 				}
 			
 		}
 		
 //------------------------BEGIN "ARMOR PENALTY" CODE--------------------------
 		
 		Player player = (Player) arrow.getShooter();
 		double damageReduction = 0;
 		
 		String helm = String.valueOf(player.getInventory().getHelmet());
 		String chest = String.valueOf(player.getInventory().getChestplate());
 		String pants = String.valueOf(player.getInventory().getLeggings());
 		String boots = String.valueOf(player.getInventory().getBoots());
 
 		if (plugin.allowArmorPenalty) {
 			if (helm.contains("DIAMOND_HELMET")) {
 				damageReduction += 0.4;
 			} else if (helm.contains("IRON_HELMET")) {
 				damageReduction += 0.3;
 			} else if (helm.contains("GOLD_HELMET")) {
 				damageReduction += 0.2;
 			} else if (helm.contains("LEATHER_HELMET")) {
 				damageReduction += 0.1;
 			} else {
 				damageReduction += 0.1;
 			}
 			if (chest.contains("DIAMOND_CHESTPLATE")) {
 				damageReduction += 0.6;
 			} else if (chest.contains("IRON_CHESTPLATE")) {
 				damageReduction += 0.5;
 			} else if (chest.contains("GOLD_CHESTPLATE")) {
 				damageReduction += 0.4;
 			} else if (chest.contains("LEATHER_CHESTPLATE")) {
 				damageReduction += 0.3;
 			} else {
 				damageReduction += 0.2;
 			}
 			if (pants.contains("DIAMOND_LEGGINGS")) {
 				damageReduction += 0.5;
 			} else if (pants.contains("IRON_LEGGINGS")) {
 				damageReduction += 0.4;
 			} else if (pants.contains("GOLD_LEGGINGS")) {
 				damageReduction += 0.3;
 			} else if (pants.contains("LEATHER_LEGGINGS")) {
 				damageReduction += 0.2;
 			} else {
 				damageReduction += 0.1;
 			}
 			if (boots.contains("DIAMOND_BOOTS")) {
 				damageReduction += 0.4;
 			} else if (boots.contains("IRON_BOOTS")) {
 				damageReduction += 0.3;
 			} else if (boots.contains("GOLD_BOOTS")) {
 				damageReduction += 0.2;
 			} else if (boots.contains("LEATHER_BOOTS")) {
 				damageReduction += 0.2;
 			} else {
 				damageReduction += 0.2;
 			}
 		} else {
 			damageReduction = 1;
 		}
 		
 //------------------------BEGIN "MASSIVE CRITICALS" CODE--------------------------
 		
 		//debug output
		plugin.log.info("#debug Initial damage: " + String.valueOf(event.getDamage()));
 		
 		Random randomGenerator = new Random();
 		int critNum = randomGenerator.nextInt(100);
 		
 		if (damageReduction > 1.5) {
 			player.sendMessage(ChatColor.RED + "You are heavily encumbered.. your arrows do little damage!");
 		}
 		
 		if (sendToArrow.contains("Razor")) {
 			plugin.tempCritChance = (plugin.baseCritChance*2);
 			plugin.tempMassiveChance = (plugin.baseMassiveChance*2);
 		} else {
 			plugin.tempCritChance = plugin.baseCritChance;
 			plugin.tempMassiveChance = plugin.baseMassiveChance;
 		}
 		
 		if (sendToDamage.contains("c")) {
 			if (plugin.allowCrits && (critNum >= 0 && critNum < plugin.tempCritChance)) { 
 				damageMultiplier = (((event.getDamage()*plugin.baseCritMultiplier)/damageReduction)*plugin.baseDamageMultiplier)*plugin.baseCrouchMultiplier;
 				if (event.getEntity() instanceof LivingEntity)
 					player.sendMessage(ChatColor.YELLOW + "Critical hit!");
 			} else if (plugin.allowCrits && (critNum >= plugin.tempCritChance && critNum < (plugin.baseCritChance + plugin.tempMassiveChance))) {	
 					damageMultiplier = (((event.getDamage()*plugin.baseMassiveMultiplier)/damageReduction)*plugin.baseDamageMultiplier)*plugin.baseCrouchMultiplier;
 					if (event.getEntity() instanceof LivingEntity)
 						player.sendMessage(ChatColor.RED + "MASSIVE CRIT!");
 			} else {
 				damageMultiplier = ((event.getDamage()*plugin.baseCrouchMultiplier)/damageReduction)*plugin.baseDamageMultiplier;
 			}
 		} else {
 			if (plugin.allowCrits && (critNum >= 0 && critNum < plugin.tempCritChance)) { 
 				damageMultiplier = ((event.getDamage()*plugin.baseCritMultiplier)/damageReduction)*plugin.baseDamageMultiplier;
 				if (event.getEntity() instanceof LivingEntity)
 					player.sendMessage(ChatColor.YELLOW + "Critical hit!");
 			} else if (plugin.allowCrits && (critNum >= plugin.tempCritChance && critNum < (plugin.tempCritChance + plugin.tempMassiveChance))) {	
 				damageMultiplier = ((event.getDamage()*plugin.baseMassiveMultiplier)/damageReduction)*plugin.baseDamageMultiplier;
 				if (event.getEntity() instanceof LivingEntity)
 					player.sendMessage(ChatColor.RED + "MASSIVE CRIT!");
 			} else {
 				damageMultiplier = ((event.getDamage())/damageReduction)*plugin.baseDamageMultiplier;
 			}
 		}
 		
 		if (arrowType != ArrowType.Poison) {
 			event.setDamage((int)Math.floor(damageMultiplier));
 		}
 		
 		//debug output
		plugin.log.info("#debug Final damage:" + damageMultiplier + " rounded to:" + (int)Math.floor(damageMultiplier));
		plugin.log.info("#debug Final crit chance:" + plugin.tempCritChance + " Final massive chance:" + plugin.tempMassiveChance);
 		
 		// reset flags and temp variables
 		sendToDamage = "";
 		plugin.tempCritChance = plugin.baseCritChance;
 	    plugin.tempMassiveChance = plugin.baseMassiveChance;
 	
 	}
 }
