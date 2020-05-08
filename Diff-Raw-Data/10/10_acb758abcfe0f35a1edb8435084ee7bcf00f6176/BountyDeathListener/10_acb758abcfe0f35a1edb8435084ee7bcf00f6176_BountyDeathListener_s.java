 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager.bounty.listeners;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.forgenz.mobmanager.MMComponent;
 import com.forgenz.mobmanager.bounty.config.BountyConfig;
 import com.forgenz.mobmanager.bounty.config.BountyType;
 import com.forgenz.mobmanager.bounty.config.BountyWorldConfig;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 
 public class BountyDeathListener implements Listener
 {
 	private final static Pattern underscore = Pattern.compile("_", Pattern.LITERAL);
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent == false)
 		{
 			return;
 		}
 		
 		EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
 		
 		// Make sure the entity is a living entity
 		if (damage.getEntity() instanceof LivingEntity == false)
 		{
 			return;
 		}
 		LivingEntity entity = (LivingEntity) damage.getEntity();
 
 		// Fetch the Bounty config
 		BountyConfig cfg = MMComponent.getBounties().getConfig();
 		
 		// Fetch the config for the world the entity is in
 		BountyWorldConfig worldCfg = cfg.getWorldConfig(entity.getWorld());
 		// If no config was found Bounties is disabled here
 		if (worldCfg == null)
 		{
 			return;
 		}
 		
 		ExtendedEntityType entityType = ExtendedEntityType.valueOf(entity);
 		
 		// If the reward is 0.0 we ignore the death
 		double reward;
 		if ((reward = worldCfg.getReward(entityType)) == 0.0)
 		{
 			return;
 		}
 		
 		// Dropping a negative amount of items makes no sense
 		if (cfg.bountyType == BountyType.ITEM && reward < 0.0)
 		{
 			return;
 		}
 	
 		// Fetch the player which killed the mob
 		Player player = getDamager(damage.getDamager(), worldCfg.allowPetKills);
 		// If there is no player, what are we doing???
 		if (player == null)
 		{
 			return;
 		}
 		
 		// Apply appropriate multipliers to the reward
 		reward = cfg.applyMultipliers(reward, player, entity, entityType);
 		
 		// If the reward is 0.0 we ignore the death
 		// It could have been reduced to 0.0 after lots of small multipliers
 		if (reward == 0.0)
 		{
 			return;
 		}
 		// Dropping a negative amount of items makes no sense
 		if (cfg.bountyType == BountyType.ITEM && reward < 0.0)
 		{
 			return;
 		}
 
 		// Attempt to fetch the mobs custom name
 		String mobName = worldCfg.getMobName(entity);
 		// Else just use the mob type
 		if (mobName == null)
 		{
 			mobName = entityType.toString().charAt(0) + entityType.toString().substring(1).toLowerCase();
 			mobName = underscore.matcher(mobName).replaceAll(" ");
 		}
 		
 		Object objReward = null;
 		switch (cfg.bountyType)
 		{
 		case EXP:
 			objReward = handleExp(player, reward);
 			break;
 		case ITEM:
 			int count = handleItemDrop(event.getDrops(), cfg.itemDrop, reward);
 			if (count > 0)
 				objReward = count;
 			break;
 		case MONEY:
 			handleMoney(cfg, player, reward);
 			objReward = Math.abs(reward);
 			break;
 		}
 		
 		if (objReward != null)
 		{
 			if (reward > 0.0)
 			{
 				if (cfg.rewardPlayerMessage.length() > 0)
 					player.sendMessage(String.format(cfg.rewardPlayerMessage, objReward, mobName));
 			}
 			else
 			{
 				if (cfg.finePlayerMessage.length() > 0)
 					player.sendMessage(String.format(cfg.finePlayerMessage, objReward, mobName));
 			}
 		}
 	}
 	
 	public Player getDamager(Entity damager, boolean allowPets)
 	{
 		// Check if the damager is a player
 		if (damager instanceof Player)
 		{
 			return (Player) damager;
 		}
 		// Check if the damager is a projectile from a player
 		else if (damager instanceof Projectile)
 		{
 			Projectile projectile = (Projectile) damager;
 			
 			if (projectile.getShooter() instanceof Player)
 			{
 				return (Player) projectile.getShooter();
 			}
 		}
 		// Check if the damager is a pet tamed by a player
 		else if (allowPets && damager instanceof Tameable)
 		{
 			Tameable pet = (Tameable) damager;
 			
 			if (pet.isTamed() && pet.getOwner() instanceof Player)
 			{
 				Player player = (Player) pet.getOwner();
 				// Only players with this permission can get bounty from their pets killing mobs
 				if (player.hasPermission("mobmanager.bounty.petreward"))
 				{
 					return player;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public void handleMoney(BountyConfig cfg, Player player, double reward)
 	{
 		Economy econ = MMComponent.getBounties().getEconomy();
 		
 		if (econ == null)
 			return;
 		
 		if (reward == 0.0)
 		{
 			return;
 		}
 		else if (reward > 0)
 		{
 			econ.depositPlayer(player.getName(), reward);
 		}
 		else
 		{
 			reward = Math.abs(reward);
 			double balance = econ.getBalance(player.getName());
 			if (balance - reward < 0)
 			{
 				reward = balance;
 			}
 			
			econ.withdrawPlayer(player.getName(), balance);
 		}
 	}
 	
 	public int handleItemDrop(List<ItemStack> drops, Material material, double reward)
 	{
 		int count = (int) reward;
 		reward -= count;
 		
 		if (Math.random() <= reward)
 		{
 			++count;
 		}
 		
 		int amount = count;
 		
 		while (count > 0)
 		{
 			ItemStack item = new ItemStack(material);
 			
 			boolean flag = material.getMaxStackSize() >= count;
 			item.setAmount(flag ? count : material.getMaxStackSize());
 			
 			// Add the item to the drops
 			System.out.print("Count: " + item.getAmount());
 			drops.add(item);
 			
 			if (flag)
 			{
 				break;
 			}
 			else
 			{
 				count -= material.getMaxStackSize();
 			}
 		}
 		
 		return amount;
 	}
 	
 	public int handleExp(Player player, double reward)
 	{
 		boolean give = reward > 0.0;
 		reward = Math.abs(reward);
 		
 		int exp = (int) reward;
 		
 		if (Math.random() <= reward - exp)
 		{
 			++exp;
 		}
 		
 		player.giveExp(give ? exp : -exp);
 		
 		return exp;
 	}
 }
