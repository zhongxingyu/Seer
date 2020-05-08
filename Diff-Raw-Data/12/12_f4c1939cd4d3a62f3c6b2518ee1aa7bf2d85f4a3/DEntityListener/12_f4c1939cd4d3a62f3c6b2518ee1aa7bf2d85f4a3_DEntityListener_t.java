 package com.legit2.Demigods.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.legit2.Demigods.Demigods;
 import com.legit2.Demigods.Utilities.DUtil;
 
 public class DEntityListener implements Listener
 {
 	static Demigods plugin;
 	
 	public DEntityListener(Demigods instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void damageEvent(EntityDamageEvent event)
 	{
 		// Define variables
 		LivingEntity attackedEntity;
 		if(event.getEntityType().equals(EntityType.PLAYER)) // IF IT'S A PLAYER
 		{
 			// Define entity as player and other variables
 			attackedEntity = (LivingEntity) event.getEntity();
 			Player attackedPlayer = (Player) attackedEntity;
 			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) attackedEntity.getLastDamageCause();
 			Entity attacker = damageEvent.getDamager();
 			
 			if(attacker instanceof Player)
 			{
 				if(!DUtil.canTarget(attackedPlayer, attackedPlayer.getLocation()))
 				{
 					((Player) attacker).sendMessage(ChatColor.GRAY + "Stop, you!");
 					event.setCancelled(true);
 					return;
 				}
 				
 				/*
 				if(damageEvent.getDamage() > attackedPlayer.getHealth())
 				{
 					// For player deaths, we first check their opponent for # of souls and determine soul drops from there...
 					if(DUtil.getNumberOfSouls((attackedPlayer)) == 0) // If they have no souls then we know to drop a new soul on death
 					{
 						attackedEntity.getLocation().getWorld().dropItemNaturally(attackedEntity.getLocation(), DSouls.getSoulFromEntity(attackedEntity));
 					}
 					else // Else we cancel their death and subtract a soul
 					{
 						if(DUtil.getNumberOfSouls(attackedPlayer) > 0)
 						{
 							ItemStack usedSoul = DUtil.useSoul(attackedPlayer);
 						
 							DUtil.serverMsg("TEMP: " + attackedPlayer.getName() + " just lost 1 " + usedSoul.getType().name().toLowerCase() + "!");
 							
 							DUtil.serverMsg("TEMP: Attempting to cancel death...");
 							event.setCancelled(true);
 						}
 					}
 				}
 				*/
 			}
 		}
 		else if(event.getEntityType().equals(EntityType.VILLAGER)) // IF IT'S A VILLAGER
 		{
 			// Define villager
 			LivingEntity villager = (LivingEntity) event.getEntity();
 			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) villager.getLastDamageCause();
 			
 			// Define attacker and name
 			Player attacker = null;
 			if(damageEvent.getDamager() != null) attacker = (Player) damageEvent.getDamager();
 
 			if(damageEvent.getDamager() instanceof Player && damageEvent.getDamage() > villager.getHealth())
 			{
 				//villager.getLocation().getWorld().dropItemNaturally(villager.getLocation(), DSouls.getSoulFromEntity(villager));
 				if(attacker != null) attacker.sendMessage(ChatColor.GRAY + "One weaker than you has been slain by your hand.");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void entityDeath(EntityDeathEvent event)
 	{
 		// TODO
 	}
 
 }
