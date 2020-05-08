 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.censoredsoftware.Demigods.API.ZoneAPI;
 import com.censoredsoftware.Demigods.Engine.DemigodsUtil;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBattle;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 
 public class EntityListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void damageEvent(EntityDamageEvent event)
 	{
 		// Define variables
 		LivingEntity entity;
 		if(event.getEntityType().equals(EntityType.PLAYER)) // If it's a player
 		{
 			// Define entity as player and other variables
 			entity = (LivingEntity) event.getEntity();
 
 			// NO DAMAGE IN NO PVP ZONES FOR PLAYERS TODO Do we want to keep it that way?
 			if(!ZoneAPI.canTarget(entity))
 			{
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void damageByEntityEvent(EntityDamageByEntityEvent event)
 	{
 		Entity attacked = event.getEntity();
 		Entity attacker = event.getDamager();
 
 		if(attacker instanceof Player)
 		{
 			Player hitting = (Player) attacker;
 
 			// NO PVP
 			if(!ZoneAPI.canTarget(attacked))
 			{
 				hitting.sendMessage(ChatColor.GRAY + "No-PVP in this zone.");
 				event.setCancelled(true);
 				return;
 			}
 
 			if(attacked instanceof Villager) // If it's a villager
 			{
 				// Define villager
 				Villager villager = (Villager) attacked;
 
 				// Define attacker and name
 				if(event.getDamage() > villager.getHealth()) hitting.sendMessage(ChatColor.GRAY + "One weaker than you has been slain by your hand.");
 			}
 			else if(attacked instanceof Player) // If it's a player
 			{
 				// Define player
 				Player hit = (Player) attacked;
 
 				PlayerCharacter hitChar = TrackedPlayer.getTracked(hit).getCurrent();
 				PlayerCharacter hittingChar = TrackedPlayer.getTracked(hitting).getCurrent();
 
 				TrackedBattle.battleProcess(hitChar, hittingChar);
 
				if(event.getDamage() > ((Player) attacked).getHealth()) hittingChar.addKill();
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void entityDeath(EntityDeathEvent event)
 	{
 		if(event.getEntityType().equals(EntityType.PLAYER))
 		{
 			// Define variables
 			Player player = (Player) event.getEntity();
 			PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 			if(character == null) return;
 			String deity = DemigodsUtil.capitalize(character.getDeity().getInfo().getName());
 			int devotion = character.getMeta().getDevotion();
 			int devotionRemoved = (int) Math.ceil(devotion * .19);
 
 			// Set their devotion and add a death
 			character.getMeta().subtractDevotion(devotionRemoved);
 			character.addDeath();
 
 			// Let 'em know
 			player.sendMessage(ChatColor.RED + "You have failed " + deity + "!");
 			player.sendMessage(ChatColor.RED + "You have been stripped of " + devotionRemoved + " devotion!");
 		}
 	}
 }
