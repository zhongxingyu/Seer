 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.listeners;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import com.comphenix.xp.Action;
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.Presets;
 import com.comphenix.xp.SampleRange;
 import com.comphenix.xp.expressions.NamedParameter;
 import com.comphenix.xp.extra.Permissions;
 import com.comphenix.xp.lookup.LevelingRate;
 import com.comphenix.xp.lookup.MobQuery;
 import com.comphenix.xp.lookup.PlayerQuery;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.rewards.ResourceHolder;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.items.RandomSampling;
 import com.comphenix.xp.rewards.xp.CurrencyHolder;
 import com.comphenix.xp.rewards.xp.ExperienceHolder;
 import com.comphenix.xp.rewards.xp.ExperienceManager;
 import com.comphenix.xp.rewards.xp.RewardEconomy;
 import com.comphenix.xp.rewards.xp.RewardVirtual;
 
 public class ExperienceMobListener extends AbstractExperienceListener {
 
 	/**
 	 * Used to schedule a future reward.
 	 * @author Kristian
 	 */
 	private class FutureReward {
 		public List<ResourceHolder> generated;
 		public Action action;
 		public Configuration config;
 	}
 	
 	private Debugger debugger;
 	
 	// To determine which groups are player is part of
 	private PlayerGroupMembership playerGroups;
 	
 	// To determine spawn reason
 	private Map<Integer, SpawnReason> spawnReasonLookup = new HashMap<Integer, SpawnReason>();
 
 	// The resources to award
 	private Map<Integer, FutureReward> scheduledRewards = new HashMap<Integer, FutureReward>();
 	
 	// Random source
 	private Random random = new Random();
 	
 	// Error report creator
 	private ErrorReporting report = ErrorReporting.DEFAULT;
 	
 	// Economy for currency subtraction
 	private RewardEconomy economy;
 	
 	public ExperienceMobListener(Debugger debugger, PlayerGroupMembership playerGroups, Presets presets) {
 		this.debugger = debugger;
 		this.playerGroups = playerGroups;
 		setPresets(presets);
 	}
 	
 	public void setEconomy(RewardEconomy economy) {
 		this.economy = economy;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) 
 	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
 		
 		try {
 			if (event.getSpawnReason() != null) {
 				spawnReasonLookup.put(event.getEntity().getEntityId(), 
 									  event.getSpawnReason());
 			}
 		
 		// Every entry method must have a generic catcher
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
 		
 		try {
 			Entity attacker = event.getDamager();
 			LivingEntity killer = null;
 			
 			// See if we have a player or an arrow
 			if (attacker instanceof Player)
 				killer = (Player) attacker;
 			else if (attacker instanceof Projectile)
 				killer = ((Projectile) attacker).getShooter();
 			
 			// Only cancel living entity damage events from players
 			if (event.getEntity() instanceof LivingEntity &&
 				killer instanceof Player) {
 				
 				LivingEntity entity = (LivingEntity) event.getEntity();
 				Player playerKiller = (Player) killer;
 				int damage = event.getDamage();
 				
 				// Predict the amount of damage actually inflicted
                 if ((float) entity.getNoDamageTicks() > (float) entity.getMaximumNoDamageTicks() / 2.0F) {
                     if (damage > entity.getLastDamage()) {
                     	damage -= entity.getLastDamage();
                     } else {
                     	return;
                     }
                 }
                 
                 // Will this most likely cause the entity to die?
                 // Note that this doesn't take into account potions and armor.
                 if (entity.getHealth() <= damage) {
                 	// Prevent this damage
                 	if (!onFutureKillEvent(entity, playerKiller)) {
             			// Events will not be directly cancelled for untouchables
             			if (!Permissions.hasUntouchable(playerKiller))
             				event.setCancelled(true);
             			
         				if (hasDebugger())
         					debugger.printDebug(this, "Entity %d kill cancelled: Player %s hasn't got enough resources.",
         							entity.getEntityId(), playerKiller.getName());
                 	}
                 }
 			}
 		
 		// Every entry method must have a generic catcher
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	private boolean onFutureKillEvent(LivingEntity entity, Player killer) {
 		
 		Configuration config = getConfiguration(entity, killer);
 		Collection<NamedParameter> params = null;
 		
 		// Warn, but allow
 		if (config == null) {
 			return true;
 		}
 		
 		// Quickly retrieve the correct action
 		RewardProvider rewards = config.getRewardProvider();
 		Action action = getAction(config, entity, killer);
 		
 		// Allow event
 		if (action == null) {
 			return true;
 		}
 		
 		// Get player-specific parameters
 		if (entity instanceof Player) {
 			params = config.getParameterProviders().getParameters(action, (Player) entity);
 		} else {
 			params = config.getParameterProviders().getParameters(action, entity);
 		}
 			
 		// Generate some rewards
 		List<ResourceHolder> generated = action.generateRewards(params, rewards, random);
 
 		FutureReward future = new FutureReward();
 		future.action = action;
 		future.generated = generated;
 		future.config = config;
 		scheduledRewards.put(entity.getEntityId(), future);
 		
 		// Could we reward the player if this mob was killed?
 		if (killer != null && !action.canRewardPlayer(rewards, killer, generated)) {
 			future.generated = null;
 			return false;
 		}
 		
 		// Allow this event
 		return true;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEntityDeathEvent(EntityDeathEvent event) {
 		
 		LivingEntity entity = event.getEntity();
 		Collection<ResourceHolder> result = null;
 		
 		try {
 			// Only drop experience from mobs
 			if (entity != null) {
 				Player killer = entity.getKiller();
 				result = handleEntityDeath(event, entity, killer);
 			}
 			
 			if (event instanceof PlayerDeathEvent) {
 				handlePlayerDeath((PlayerDeathEvent) event, (Player) entity, result);
 			}
 		
 		// Every entry method must have a generic catcher
 		} catch (Exception e) {
 			report.reportError(debugger, this, e, event);
 		}
 	}
 	
 	private void handlePlayerDeath(PlayerDeathEvent event, Player player, Collection<ResourceHolder> dropped) {
 
 		// Permission check
         if(Permissions.hasKeepExp(player)) {
         	
         	event.setDroppedExp(0);
             event.setKeepLevel(true);
             
             if (hasDebugger())
         		debugger.printDebug(this, "Prevented experience loss for %s.", player.getName());
             
         } else if (dropped != null && dropped.size() > 0) {
         	int total = 0;     	
         
         	// Manual dropped amount
         	event.setDroppedExp(0);
         	
         	// Subtract the dropped experience
         	for (ResourceHolder holder : dropped) {
         		if (holder instanceof ExperienceHolder) {
         			ExperienceHolder exp = (ExperienceHolder) holder;
         			total += exp.getAmount();
         		}
         	}
         	
         	// Set the correct level and experience
         	if (total > 0) {
         		subtractExperience(event, player, revertLevelingRate(player, total));
         	}
         	
             if (hasDebugger())
         		debugger.printDebug(this, "%s took %d experience loss.", player.getName(), total);
         } else {
         	
         	// Display the loss, at least
             if (hasDebugger())
         		debugger.printDebug(this, "%s took %d standard experience loss.", 
         				player.getName(), event.getDroppedExp());
         }
 		
 		// Subtract money
 		if (economy != null && /*config.subtract economy &&*/ dropped != null && dropped.size() > 0) {
 			int total = 0;
 			
 			for (ResourceHolder holder : dropped) {
 				if (holder instanceof CurrencyHolder) {
 					CurrencyHolder currency = (CurrencyHolder) holder;
 					total += currency.getAmount();
 				}
 			}
 			
 			if (total > 0) {
 				economy.economyReward(player, -total, debugger);
 				if (hasDebugger())
 					debugger.printDebug(this, "%s took %d currency loss.",
 						player.getName(), total);
 			}
 		}
 	}
 	
 	// Revert the leveling rate we applied
 	private int revertLevelingRate(Player player, int experience) {
 		
 		Configuration config = getConfiguration(player);
 		ExperienceManager manager = new ExperienceManager(player);	
 		
 		LevelingRate rate = config != null ? config.getLevelingRate() : null;
 		double rateFactor = rate != null ? RewardVirtual.getLevelingFactor(rate, player, manager) : 1;
 		
 		SampleRange sampling = new SampleRange(experience / rateFactor);
 		return sampling.sampleInt(RandomSampling.getThreadRandom());
 	}
 	
 	// Manually subtract experience
 	private void subtractExperience(PlayerDeathEvent event, Player player, int experience) {
 		
 		ExperienceManager manager = new ExperienceManager(player);		
 		int current = manager.getCurrentExp();
 		int after = Math.max(current - experience, 0);
 		int level = manager.getLevelForExp(after);
 		
 		// Calculate the correct amount of experience left
 		event.setKeepLevel(false);
 		event.setNewLevel(level);
 		event.setNewExp(after - manager.getXpForLevel(level));
 		event.setNewTotalExp(Math.max(player.getTotalExperience() - experience, 0));
 	}
 	
 	private Configuration getConfiguration(LivingEntity entity, Player killer) {
 		
 		boolean hasKiller = (killer != null);
 		
 		// Get the correct configuration
 		if (hasKiller)
 			return getConfiguration(killer);
 		else if (entity instanceof Player)
 			return getConfiguration((Player) entity);
 		else
 			return getConfiguration(entity.getWorld());
 	}
 	
 	private Action getAction(Configuration config, LivingEntity entity, Player killer) {
 				
 		if (entity instanceof Player) {
 			
 			Player entityPlayer = (Player) entity;
 			
 			PlayerQuery query = PlayerQuery.fromExact(
 					entityPlayer, 
 					playerGroups.getPlayerGroups(entityPlayer), 
 					killer != null);
 
 			if (config != null) {
 				return config.getPlayerDeathDrop().get(query);
 				
 			} else {
 				// Report this problem
 				if (hasDebugger())
 					debugger.printDebug(this, "No config found for player %d, query: %s", entityPlayer.getName(), query);
 				return null;
 			}
 			
 		} else {
 		
 			Integer id = entity.getEntityId();
 			MobQuery query = MobQuery.fromExact(entity, spawnReasonLookup.get(id), killer != null);
 			
 			if (hasDebugger()) 
 				debugger.printDebug(this, "Mob query: %s", query.toString());
 				
 			if (config != null) {
 				return config.getExperienceDrop().get(query);
 				
 			} else {
 				// Report this problem
 				if (hasDebugger())
 					debugger.printDebug(this, "No config found for mob %d, query: %s", id, query);
 				return null;
 			}
 		}
 	}
 	
 	private Collection<ResourceHolder> handleEntityDeath(EntityDeathEvent event, LivingEntity entity, Player killer) {
 		
 		boolean hasKiller = (killer != null);
 		Integer id = entity.getEntityId();
 		
 		// Values that are either precomputed, or computed on the spot
 		Configuration config = null;
 		Action action = null;
 		FutureReward future = null;
 		
 		// Resources generated and given
 		List<ResourceHolder> generated = null;
 		Collection<ResourceHolder> result = null;
 		
 		// Simplify by adding the reward to the lookup regardless
 		if (!scheduledRewards.containsKey(id)) {
 			// We'll just generate the reward then
 			onFutureKillEvent(entity, killer);
 		}
 		
 		// Retrieve reward from lookup
 		future = scheduledRewards.get(id);
 		
 		if (future != null) {
 			action = future.action;
 			generated = future.generated;
 			config = future.config;
 		} else {
 			config = getConfiguration(entity, killer);
 		}
 	
 		// And we're done with this mob
 		scheduledRewards.remove(id);
 		
 		if (hasDebugger()) {
 			debugger.printDebug(this, "Generated: %s", StringUtils.join(generated, ", "));
 		}
 			
 		// Make sure the reward has been changed
 		if (generated != null && generated.size() > 0) {
 			
 			ChannelProvider channels = config.getChannelProvider();
 			RewardProvider rewards = config.getRewardProvider();
 
 			// Spawn the experience ourself
 			event.setDroppedExp(0);
 			
 			// Reward the killer directly, or just drop it naturally
 			if (killer != null)
				result = action.rewardPlayer(rewards, killer, generated, entity.getLocation());
 			else
 				result = action.rewardAnyone(rewards, entity.getWorld(), generated, entity.getLocation());
 			
 			// Print message
 			config.getMessageQueue().enqueue(killer, action, channels.getFormatter(killer, result, generated));
 			
 			if (hasDebugger())
 				debugger.printDebug(this, "Entity %d: Changed experience drop to %s.", 
 						id, StringUtils.join(result, ", "));
 
 		} else if (action != null && action.getInheritMultiplier() != 1) {
 			
 			// Inherit experience action
 			handleMultiplier(event, id, config.getMultiplier() * action.getInheritMultiplier());
 			
 		} else if (config.isDefaultRewardsDisabled() && hasKiller) {
 			
 			// Disable all mob XP
 			event.setDroppedExp(0);
 			
 			if (hasDebugger())
 				debugger.printDebug(this, "Entity %d: Default mob experience disabled.", id);
 
 		} else if (!config.isDefaultRewardsDisabled() && hasKiller) {
 			
 			handleMultiplier(event, id, config.getMultiplier());
 		}
 		
 		// Remove it from the lookup
 		if (!(entity instanceof Player)) {
 			spawnReasonLookup.remove(id);
 		}
 	
 		return result;
 	}
 	
 	private void handleMultiplier(EntityDeathEvent event, int entityID, double multiplier) {
 		
 		int expDropped = event.getDroppedExp();
 		
 		// Alter the default experience drop too
 		if (multiplier != 1) {
 			SampleRange increase = new SampleRange(expDropped * multiplier);
 			int expChanged = increase.sampleInt(random);
 			
 			event.setDroppedExp(expChanged);
 			
 			if (hasDebugger())
 				debugger.printDebug(this, "Entity %d: Changed experience drop to %d exp.", 
 						entityID, expChanged);
 		}
 	}
 	
 	// Determine if a debugger is attached and is listening
 	private boolean hasDebugger() {
 		return debugger != null && debugger.isDebugEnabled();
 	}
 }
