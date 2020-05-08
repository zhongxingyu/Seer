 package com.pi.coelho.CookieMonster;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class CMEntityListener implements Listener {
 
 	protected HashMap<Integer, MonsterAttack> attacks = new HashMap<Integer, MonsterAttack>();
 	Server sv = null;
 
 	public CMEntityListener(Server bukkitServer) {
 		sv = bukkitServer;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamage(EntityDamageEvent entEvent) {
 		if (entEvent.isCancelled()) {
 			return;
 		}
 		if (entEvent instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) entEvent;
 			if (event.getDamager() instanceof Arrow) {
 				entDamage(event.getEntity(), ((Arrow) event.getDamager()).getShooter(), entEvent);
 			} else {
 				entDamage(event.getEntity(), event.getDamager(), entEvent);
 			}
 		} else {
 			monsterDamaged(entEvent.getEntity(), null, true);
 		}
 	}
 
 	void entDamage(Entity monster, Entity damager, EntityDamageEvent entEvent) {
 		if (monster instanceof LivingEntity) {
 			//System.out.println(damager);
 			Player pl = null;
 			if (damager instanceof Player) {
 				pl = (Player) damager;
 			} else if (damager instanceof Wolf && CookieMonster.config.allowWolfHunt) {
 				if (((CraftWolf) damager).isTamed()) {
 					AnimalTamer at = ((CraftWolf) damager).getOwner();
 					if (at instanceof Player) {
 						pl = (Player) at;
 					}
 				}
 			}
 			boolean handleKill = CookieMonster.config.cmEnabled(entEvent.getEntity().getLocation());
 			if (pl == null) {
 				monsterDamaged(monster, null, handleKill);
 			} else {
 				monsterDamaged(monster, pl, handleKill);
 			}
 		}
 	}
 
 	public void monsterDamaged(Entity monster, Player player, boolean handleKill) {
 		if (!attacks.containsKey(monster.getEntityId())) {
 			if (player != null) {
 				attacks.put(monster.getEntityId(), new MonsterAttack(player, handleKill));
 			}
 		} else {
 			attacks.get(monster.getEntityId()).setAttack(player, handleKill);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		EntityDeathEvent entd = new EntityDeathEvent(event.getEntity(), event.getDrops(), event.getDroppedExp());
 		onEntityDeath(entd);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) {
 			return;
 		}
 		MonsterAttack at = attacks.get(event.getEntity().getEntityId());
 		if (at != null && !at.handleKill) {
 			return;
 		}
 
 		// check against camping
 		if (CookieMonster.config.globalCampTrackingEnabled) {
 			Location loc = event.getEntity().getLocation();
 			CookieMonster.killTracker.addKill(loc);
 //			System.out.println("kill added, bringing total about " + loc + " to " + CookieMonster.killTracker.numKills(loc,
 //					CookieMonster.config.deltaX, CookieMonster.config.deltaY,
 //					CookieMonster.config.campTrackingTimeout));
 			if (CookieMonster.killTracker.numKills(loc,
 					CookieMonster.config.deltaX, CookieMonster.config.deltaY,
 					CookieMonster.config.campTrackingTimeout) > CookieMonster.config.campKills) {
 				if (at != null && at.lastAttackPlayer != null) {
 					at.lastAttackPlayer.sendMessage(CMConfig.messages.get("nocampingreward"));
 				}
 
 				// don't remove player exp or drops, but don't reward the kill, either
 				if (!(event.getEntity() instanceof Player)) {
 					if (CookieMonster.config.disableCampingDrops) {
 						event.getDrops().clear();
 					}
 					if (CookieMonster.config.disableCampingExp) {
 						event.setDroppedExp(0);
 					}
 				} else {
 					Player vic = (Player) event.getEntity();
 					CookieMonster.playerListener.addRespawnInv(vic);
 					event.getDrops().clear();
 					event.setDroppedExp(0);
 				}
 				return;
 			}
 		}
 
 		if (event.getEntity() instanceof Player) {
 			if (at != null) {
 				if (at.attackTimeAgo() <= CookieMonster.config.damageTimeThreshold) {
 					at.rewardKill(event);
 				}
 				attacks.remove(event.getEntity().getEntityId());
 			}
 			return;
 		}
 
 
 		if (CookieMonster.config.disableAnoymDrop) {
 			if (at == null) {
 				event.getDrops().clear();
 			}
 		} else if ((at == null || at.attackTimeAgo() > CookieMonster.config.damageTimeThreshold)
 				&& CookieMonster.config.alwaysReplaceDrops) {
 			// if this is not a rewarded kill & alwaysReplaceDrops, set the drops
 			ItemStack newDrops[] = CookieMonster.getRewardHandler().getDropReward(event.getEntity());
 			if (newDrops != null) {
 				if (CookieMonster.config.replaceDrops) {
 					event.getDrops().clear();
 				}
 				event.getDrops().addAll(Arrays.asList(newDrops));
 			}
 		} else if (at != null) {
 			if (at.attackTimeAgo() <= CookieMonster.config.damageTimeThreshold) {
 				at.rewardKill(event);
 			}
 			attacks.remove(event.getEntity().getEntityId());
 		}
 	}
 
 	public class MonsterAttack {
 
 		long lastAttackTime;
 		Player lastAttackPlayer;
 		boolean handleKill = true;
 
 		public MonsterAttack(Player attacker, boolean handleKill) {
 			setAttack(attacker, handleKill);
 		}
 
 		public long attackTimeAgo() {
 			return lastAttackTime > 0 ? System.currentTimeMillis() - lastAttackTime : 0;
 		}
 
 		public final void setAttack(Player attacker, boolean handleKill) {
 			lastAttackPlayer = attacker;
 			this.handleKill = handleKill;
 			lastAttackTime = System.currentTimeMillis();
 		}
 
 		public void rewardKill(EntityDeathEvent event) {
 			if (handleKill && lastAttackPlayer != null) {
 				CookieMonster.getRewardHandler().GivePlayerCoinReward(lastAttackPlayer, event.getEntity());
 				//if (!(event.getEntity() instanceof Player)) {
 				ItemStack newDrops[] = CookieMonster.getRewardHandler().getDropReward(event.getEntity());
 				if (newDrops != null) {
 					if (CookieMonster.config.replaceDrops) {
 						event.getDrops().clear();
 					}
 					event.getDrops().addAll(Arrays.asList(newDrops));
 				}
 				if (CookieMonster.config.expMultiplier != 1) {
 					event.setDroppedExp((int) (event.getDroppedExp() * CookieMonster.config.expMultiplier));
 				}
 				//}
 			}
 		}
 	}
 }
