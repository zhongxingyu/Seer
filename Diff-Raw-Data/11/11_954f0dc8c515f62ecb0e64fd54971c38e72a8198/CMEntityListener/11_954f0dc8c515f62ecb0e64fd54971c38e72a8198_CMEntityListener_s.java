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
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class CMEntityListener extends EntityListener {
 
 	protected HashMap<Integer, MonsterAttack> attacks = new HashMap<Integer, MonsterAttack>();
 	Server sv = null;
 
 	public CMEntityListener(Server bukkitServer) {
 		sv = bukkitServer;
 	}
 
 	@Override
 	public void onEntityDamage(EntityDamageEvent entEvent) {
 		if (entEvent.isCancelled()) {
 			return;
 		}
 		if (entEvent instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) entEvent;
 			if(event.getDamager() instanceof Arrow){
 				entDamage(event.getEntity(), ((Arrow)event.getDamager()).getShooter(), entEvent);
 			}else{
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
 				if (handleKill && CookieMonster.config.disableExpensiveKill) {
 					int c = CMConfig.creatureIndex(monster);
 					if (c >= 0 && !CookieMonster.getRewardHandler().canAffordKill(pl, monster)) {
 						entEvent.setCancelled(true);
 						if (damager instanceof Wolf) {
 							((CraftWolf) damager).setTarget(null);
 						}
 						return;
 					}
 				}
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
 
 	@Override
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (!(event.getEntity() instanceof LivingEntity)){
 			return;
 		} 
 		MonsterAttack at = attacks.get(event.getEntity().getEntityId());
 		if(at != null && !at.handleKill){
 			return;
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
		} else if (CookieMonster.config.alwaysReplaceDrops) {
 			ItemStack newDrops[] = CookieMonster.getRewardHandler().getDropReward(event.getEntity());
 			if (newDrops != null) {
 				if (CookieMonster.config.replaceDrops) {
 					event.getDrops().clear();
 				}
 				event.getDrops().addAll(Arrays.asList(newDrops));
 			}
 		}
 		if (at != null) {
 			if (at.attackTimeAgo() <= CookieMonster.config.damageTimeThreshold) {
 				at.rewardKill(event);
 			}
 			attacks.remove(event.getEntity().getEntityId());
 		} else if (CookieMonster.config.globalCampTrackingEnabled) {
 			Location loc = event.getEntity().getLocation();
 			CookieMonster.killTracker.addKill(loc);
 			if (CookieMonster.killTracker.numKills(loc,
 					CookieMonster.config.deltaX, CookieMonster.config.deltaY,
 					CookieMonster.config.campTrackingTimeout) > CookieMonster.config.campKills) {
 				event.getDrops().clear();
 				return;
 			}
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
 			if (handleKill) {
 				if (CookieMonster.config.campTrackingEnabled) {
 					Location loc = event.getEntity().getLocation();
 					CookieMonster.killTracker.addKill(loc);
 //                    System.out.println("kill added, bringing total about " + loc + " to " + CookieMonster.killTracker.numKills(loc,
 //                            CookieMonster.config.deltaX, CookieMonster.config.deltaY,
 //                            CookieMonster.config.campTrackingTimeout));
 					if (CookieMonster.killTracker.numKills(loc,
 							CookieMonster.config.deltaX, CookieMonster.config.deltaY,
 							CookieMonster.config.campTrackingTimeout) > CookieMonster.config.campKills) {
 						if (lastAttackPlayer != null) {
 							lastAttackPlayer.sendMessage(CMConfig.messages.get("nocampingreward"));
 						}
 						if (CookieMonster.config.disableCampingDrops) {
 							event.getDrops().clear();
 						}
 						return;
 					}
 				}
 				if (lastAttackPlayer != null) {
 					CookieMonster.getRewardHandler().GivePlayerCoinReward(lastAttackPlayer, event.getEntity());
 					ItemStack newDrops[] = CookieMonster.getRewardHandler().getDropReward(event.getEntity());
 					if (newDrops != null) {
 						if (CookieMonster.config.replaceDrops) {
 							event.getDrops().clear();
 						}
 						event.getDrops().addAll(Arrays.asList(newDrops));
 					}
 				}
 			}
 		}
 	}
 }
