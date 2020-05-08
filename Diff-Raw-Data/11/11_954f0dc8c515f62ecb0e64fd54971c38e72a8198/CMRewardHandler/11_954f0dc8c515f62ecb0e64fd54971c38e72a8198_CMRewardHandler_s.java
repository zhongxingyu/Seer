 package com.pi.coelho.CookieMonster;
 
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.*;
 
 import org.bukkit.inventory.ItemStack;
 
 public class CMRewardHandler {
 
 	public void GivePlayerCoinReward(Player p, Entity e) {
 		int c = CMConfig.creatureIndex(e, p);

 		if (c >= 0) {
 			if (e instanceof Player) {
 				long l = CookieMonster.playerListener.playerLifeLength(e);
 				if (CookieMonster.config.playerReverseProtect
 						&& (l < 0 || l < CookieMonster.config.playerRewardWait)) {
 					// then there is a penalty, not reward, for killing the player
 					GivePlayerCoinReward(p, c, p.getItemInHand().getTypeId(), true,
 							((Player) e));
 				} else {
 					GivePlayerCoinReward(p, c, p.getItemInHand().getTypeId(), false,
 							((Player) e));
 				}
 			} else {
 				GivePlayerCoinReward(p, c, p.getItemInHand().getTypeId());
 			}
 		}
 	}
 
 	public void GivePlayerMobSpawnerCoinReward(Player p) {
 		GivePlayerCoinReward(p, CMConfig.creatureIndex("MobSpawner"),
 				p.getItemInHand().getTypeId());
 	}
 
 	public boolean canAffordMobSpawner(Player p) {
 		final int c = CMConfig.creatureIndex("MobSpawner");
 		return canAffordKill(p, c);
 	}
 
 	public boolean hasPenalty(Player p, Entity e) {
 		final int c = CMConfig.creatureIndex(e);
 		return c >= 0 ? canAffordKill(p, c) : false;
 	}
 
 	/*
 	private boolean hasPenalty(Player p, int c) {
 		return CookieMonster.config.Monster_Drop[c].getMinCoin(p.getItemInHand().getTypeId()) < 0;
 	}
 	*/
 
 	public boolean canAffordKill(Player p, Entity e) {
 		final int c = CMConfig.creatureIndex(e);
 
 		if (c >= 0 && e instanceof Player) {
 			//System.out.println("player killed");
 			final double min = CookieMonster.config.Monster_Drop[c].getMinCoin(p.getItemInHand().getTypeId());
 			final long l = CookieMonster.playerListener.playerLifeLength(e);
 			if (CookieMonster.config.playerReverseProtect
 					&& (l < 0 || l < CookieMonster.config.playerRewardWait)) {
 				// then there is a penalty, not reward, for killing the player
 				if (min > 0 && !CMEcon.canAfford(p, min)) {
 					playerNotAfford(p, c);
 					return false;
 				}
 			}
 			return min >= 0 || CMEcon.canAfford(p, -min);
 		}
 
 		return c >= 0 ? canAffordKill(p, c) : true;
 	}
 
 	private boolean canAffordKill(Player p, int c) {
 		final double min = CookieMonster.config.Monster_Drop[c].getMinCoin(p.getItemInHand().getTypeId());
 		if (c >= 0 && min < 0
 				&& !CMEcon.canAfford(p, -min)) {
 			playerNotAfford(p, c);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * send player notification that can't afford to kill this entity
 	 * @param p
 	 * @param c
 	 */
 	private void playerNotAfford(Player p, int c) {
 		if (CookieMonster.config.Monster_Drop[c].itemHasReward(p.getItemInHand().getTypeId())) {
 			p.sendMessage(CMConfig.messages.get("itemnotafford").
 					replace("<item>", Material.getMaterial(p.getItemInHand().getTypeId()).name()).
 					replace("<monster>", CMConfig.CreatureNodes[c]));
 		} else {
 			p.sendMessage(CMConfig.messages.get("notafford").
 					replace("<item>", Material.getMaterial(p.getItemInHand().getTypeId()).name()).
 					replace("<monster>", CMConfig.CreatureNodes[c]));
 		}
 	}
 
 	public double MinMobSpawnerCoinReward(int itemId) {
 		final int c = CMConfig.creatureIndex("MobSpawner");
 		if (c >= 0) {
 			return CookieMonster.config.Monster_Drop[c].getMinCoin(itemId);
 		}
 		return 0;
 	}
 
 	private void GivePlayerCoinReward(Player p, int m, int itemId) {
 		GivePlayerCoinReward(p, m, itemId, false, null);
 	}
 
 	private void GivePlayerCoinReward(Player p, int m, int itemId,
 			boolean reverseReward, Player victim) {
 		if (m < 0 || !CMEcon.hasAccount(p)) {
 			return;
 		}
 		try {
 			double amount = CookieMonster.config.Monster_Drop[m].getCoinReward(itemId);
 			if (CookieMonster.config.intOnly) {
 				amount = Math.round(amount);
 			}
 			if (reverseReward) {
 				if (amount > 0) {
 					amount *= -1;
 				} else {
 					reverseReward = false;
 				}
 			}
 			//System.out.println(CookieMonster.config.Monster_Drop[m].getMaxCoin());
 			String pre = "";
 			if (CookieMonster.config.Monster_Drop[m].itemHasReward(itemId)) {
 				pre = "item";
 			}
 			final Material i = Material.getMaterial(itemId);
 			if (amount != 0) {
 				if (amount > 0.0) {
 					if (CMConfig.isPlayer(m)) {
 						if (CookieMonster.config.playerPaysReward) {
 							// only pay what player can afford
 							double amt = CMEcon.getBalance(victim);
 							if (amount > amt) {
 								amount = amt > 0 ? amt : 0;
 							}
 							CMEcon.subtractMoney(victim, amount);
 						}
 						CMEcon.addMoney(p, amount);
 						p.sendMessage(CMConfig.messages.get(pre + "playerreward").
 								replace("<amount>", CMEcon.format(amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<player>", victim != null ? victim.getDisplayName() : "?").
 								replace("<time>", String.valueOf(CookieMonster.config.playerRewardWait / 1000)));
 						if (CookieMonster.config.playerPaysReward) {
 							victim.sendMessage(CMConfig.messages.get("victimpay").
 									replace("<amount>", CMEcon.format(amount)).
 									replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 									replace("<player>", p.getDisplayName()).
 									replace("<time>", String.valueOf(CookieMonster.config.playerRewardWait / 1000)));
 						}
 					} else {
 						CMEcon.addMoney(p, amount);
 						p.sendMessage(CMConfig.messages.get(pre + "reward").
 								replace("<amount>", CMEcon.format(amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<monster>", CMConfig.CreatureNodes[m]));
 					}
 				} else if (amount < 0.0) {
 					CMEcon.subtractMoney(p, -amount);
 					if (reverseReward && CMConfig.isPlayer(m)) {
 						p.sendMessage(CMConfig.messages.get(pre + "playercamppenalty").
 								replace("<amount>", CMEcon.format(-amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<player>", victim != null ? victim.getDisplayName() : "?").
 								replace("<time>", String.valueOf(CookieMonster.config.playerRewardWait / 1000)));
 					} else if (CMConfig.isPlayer(m)) {
 						p.sendMessage(CMConfig.messages.get(pre + "playerpenalty").
 								replace("<amount>", CMEcon.format(-amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<player>", victim != null ? victim.getDisplayName() : "?").
 								replace("<time>", String.valueOf(CookieMonster.config.playerRewardWait / 1000)));
 					} else {
 						p.sendMessage(CMConfig.messages.get(pre + "penalty").
 								replace("<amount>", CMEcon.format(-amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<monster>", CMConfig.CreatureNodes[m]));
 					}
 					if (CMConfig.isPlayer(m) && CookieMonster.config.playerPaysReward) {
 						CMEcon.addMoney(victim, -amount);
 						victim.sendMessage(CMConfig.messages.get("victimprotection").
 								replace("<amount>", CMEcon.format(-amount)).
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<player>", p.getDisplayName()).
 								replace("<time>", String.valueOf(CookieMonster.config.playerRewardWait / 1000)));
 					}
 
 				}
 			} else if (amount == 0) {
 				if (CMConfig.isCreature(m)) {
 					if (CMConfig.messages.get(pre + "norewardCreature").length() > 0) {
 						p.sendMessage(CMConfig.messages.get(pre + "norewardCreature").
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<monster>", CMConfig.CreatureNodes[m]));
 					}
 				} else if (CMConfig.isPlayer(m)) {
 					if (CMConfig.messages.get(pre + "norewardPlayer").length() > 0) {
 						p.sendMessage(CMConfig.messages.get(pre + "norewardPlayer").
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<monster>", CMConfig.CreatureNodes[m]));
 					}
 				} else {
 					if (CMConfig.messages.get(pre + "norewardMonster").length() > 0) {
 						p.sendMessage(CMConfig.messages.get(pre + "norewardMonster").
 								replace("<item>", i == null ? "?" + itemId + "?" : i.name()).
 								replace("<monster>", CMConfig.CreatureNodes[m]));
 					}
 				}
 			}
 		} catch (Exception e) {
 			p.sendMessage(ChatColor.RED + "Unexpected Error processing Reward");
 			CookieMonster.Log(Level.SEVERE, "Unexpected Error processing Reward", e);
 		}
 	}
 	/*
 	public void GivePlayerDropReward(Player p, int m) {
 	for (int i = 0; i < CookieMonster.config.Monster_Drop[m].length; i++) {
 	double random = Math.floor(Math.random() * 100);
 	if (random < CookieMonster.config.Monster_Drop[m][i][2]) {
 	ItemStack item = new ItemStack(CookieMonster.config.Monster_Drop[m][i][0],
 	CookieMonster.config.Monster_Drop[m][i][2], (short) 0);
 	p.getWorld().dropItemNaturally(p.getLocation(), item);
 	}
 	}
 	}*/
 
 	public ItemStack[] getDropReward(Entity e) {
 		return getDropReward(CMConfig.creatureIndex(e));
 	}
 
 	public ItemStack[] getMSDropReward() {
 		return getDropReward(CMConfig.creatureIndex("MobSpawner"));
 	}
 
 	private ItemStack[] getDropReward(int c) {
 		if (c >= 0) {
 			return CookieMonster.config.Monster_Drop[c].getDropsReward();
 		}
 		return null;
 	}
 }
