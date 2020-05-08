 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.Battle;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.BattleParticipant;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.BattleUtility;
 
 public class BattleListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
 	{
 		if(!canParticipate(event.getEntity()) || !canParticipate(event.getDamager())) return;
 
 		// Define participants
 		BattleParticipant damageeParticipant = defineParticipant(event.getEntity());
 		BattleParticipant damagerParticipant = defineParticipant(event.getDamager());
 
 		// Calculate midpoint location
 		Location midpoint = damagerParticipant.getCurrentLocation().toVector().getMidpoint(event.getEntity().getLocation().toVector()).toLocation(damagerParticipant.getCurrentLocation().getWorld());
 
		if(!BattleUtility.existsNear(midpoint) && !BattleUtility.existsInRadius(midpoint))
 		{
 			// Create new battle
 			Battle battle = Battle.create(damagerParticipant, damageeParticipant);
 
 			// Teleport if needed
 			teleportIfNeeded(damageeParticipant, battle);
 			teleportIfNeeded(damagerParticipant, battle);
 
 			// Debug
 			Demigods.message.broadcast(ChatColor.YELLOW + "Battle started involving " + damagerParticipant.getRelatedCharacter().getName() + " and " + damageeParticipant.getRelatedCharacter().getName() + "!");
 		}
 		else
 		{
 			// Add to existing battle
 			Battle battle = BattleUtility.getInRadius(midpoint);
 
 			// Teleport if needed
 			teleportIfNeeded(damageeParticipant, battle);
 			teleportIfNeeded(damagerParticipant, battle);
 
 			// Add participants from this event
 			battle.getMeta().addParticipant(damageeParticipant);
 			battle.getMeta().addParticipant(damagerParticipant);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onDeath(EntityDeathEvent event)
 	{
 		// Handle returns
 		if(!(event.getEntity() instanceof Player && !(event.getEntity() instanceof Tameable))) return;
 		if(event.getEntity() instanceof Player && PlayerWrapper.getPlayer((Player) event.getEntity()).getCurrent() == null) return;
 		if(event.getEntity() instanceof Tameable && TameableWrapper.getTameable(event.getEntity()) == null) return;
 	}
 
 	private static boolean canParticipate(Entity entity)
 	{
 		if(!(entity instanceof Player) && !(entity instanceof Tameable)) return false;
 		if(entity instanceof Player && PlayerWrapper.getPlayer((Player) entity).getCurrent() == null) return false;
 		if(entity instanceof Tameable && TameableWrapper.getTameable((LivingEntity) entity) == null) return false;
 		return true;
 	}
 
 	private static BattleParticipant defineParticipant(Entity entity)
 	{
 		if(entity instanceof Player) return PlayerWrapper.getPlayer((Player) entity).getCurrent();
 		return TameableWrapper.getTameable((LivingEntity) entity);
 	}
 
 	private static void teleportIfNeeded(BattleParticipant participant, Battle battle)
 	{
 		if(participant.getRelatedCharacter().getOfflinePlayer().isOnline() && !BattleUtility.existsInRadius(participant.getRelatedCharacter().getOfflinePlayer().getPlayer().getLocation())) participant.getRelatedCharacter().getOfflinePlayer().getPlayer().teleport(BattleUtility.randomBorderLocation(battle));
 	}
 }
