 package com.censoredsoftware.demigods.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.battle.Battle;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Pet;
 
 public class EntityListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void damageEvent(EntityDamageEvent event)
 	{
 		if(Demigods.isDisabledWorld(event.getEntity().getLocation())) return;
		if(event.getEntity() instanceof Player)
 		{
			if(!Battle.Util.canTarget(Battle.Util.defineParticipant(event.getEntity()))) event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void damageByEntityEvent(EntityDamageByEntityEvent event)
 	{
 		if(Demigods.isDisabledWorld(event.getEntity().getLocation())) return;
 
 		Entity attacked = event.getEntity();
 		Entity attacker = event.getDamager();
 
 		if(attacker instanceof Player)
 		{
 			Player hitting = (Player) attacker;
 
			// No PvP
			if(!DPlayer.Util.getPlayer(hitting).canPvp() || !Battle.Util.canTarget(Battle.Util.defineParticipant(attacked)))
 			{
 				hitting.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.NO_PVP_ZONE));
 				event.setCancelled(true);
 				return;
 			}
 
 			if(attacked instanceof Tameable && ((Tameable) attacked).isTamed() && Pet.Util.getTameable((LivingEntity) attacked) != null && DPlayer.Util.getPlayer(hitting).getCurrent() != null && DCharacter.Util.areAllied(DPlayer.Util.getPlayer(hitting).getCurrent(), Pet.Util.getTameable((LivingEntity) attacked).getOwner()))
 			{
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void entityDeath(EntityDeathEvent event)
 	{
 		if(Demigods.isDisabledWorld(event.getEntity().getLocation())) return;
 
 		if(event.getEntity() instanceof Player)
 		{
 			Player player = (Player) event.getEntity();
 			DCharacter playerChar = DPlayer.Util.getPlayer(player).getCurrent();
 
 			EntityDamageEvent damageEvent = player.getLastDamageCause();
 
 			if(damageEvent instanceof EntityDamageByEntityEvent)
 			{
 				EntityDamageByEntityEvent damageByEvent = (EntityDamageByEntityEvent) damageEvent;
 				Entity damager = damageByEvent.getDamager();
 
 				if(damager instanceof Player)
 				{
 					Player attacker = (Player) damager;
 					DCharacter attackChar = DPlayer.Util.getPlayer(attacker).getCurrent();
 					if(attackChar != null && playerChar != null && DCharacter.Util.areAllied(attackChar, playerChar)) DCharacter.Util.onCharacterBetrayCharacter(attackChar, playerChar);
 					else DCharacter.Util.onCharacterKillCharacter(attackChar, playerChar);
 				}
 			}
 		}
 		else if(event.getEntity() instanceof Tameable && ((Tameable) event.getEntity()).isTamed())
 		{
 			LivingEntity entity = event.getEntity();
 			Pet wrapper = Pet.Util.getTameable(entity);
 			if(wrapper == null) return;
 			DCharacter owner = wrapper.getOwner();
 			if(owner == null) return;
 			String damagerMessage = "";
 			if(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager() instanceof Player)
 			{
 				DCharacter damager = DPlayer.Util.getPlayer((Player) ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager()).getCurrent();
 				if(damager != null) damagerMessage = " by " + damager.getDeity().getColor() + damager.getName();
 			}
 			if(entity.getCustomName() != null) Demigods.message.broadcast(owner.getDeity().getColor() + owner.getName() + "'s " + ChatColor.YELLOW + entity.getType().getName().replace("Entity", "").toLowerCase() + ", " + owner.getDeity().getColor() + entity.getCustomName() + ChatColor.YELLOW + ", was slain" + damagerMessage + ChatColor.YELLOW + ".");
 			else Demigods.message.broadcast(owner.getDeity().getColor() + owner.getName() + "'s " + ChatColor.YELLOW + entity.getType().getName().replace("Entity", "").toLowerCase() + " was slain" + damagerMessage + ChatColor.YELLOW + ".");
 			wrapper.delete();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onTame(EntityTameEvent event)
 	{
 		if(Demigods.isDisabledWorld(event.getEntity().getLocation())) return;
 
 		LivingEntity entity = event.getEntity();
 		AnimalTamer owner = event.getOwner();
 		DCharacter character = DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(owner.getName())).getCurrent();
 		if(character != null) Pet.Util.create(entity, DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(owner.getName())).getCurrent());
 	}
 }
