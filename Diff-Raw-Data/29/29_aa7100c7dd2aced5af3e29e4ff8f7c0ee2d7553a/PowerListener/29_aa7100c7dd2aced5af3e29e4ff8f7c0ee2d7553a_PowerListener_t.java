 package co.networkery.uvbeenzaned.SnowBrawl;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.potion.PotionType;
 
 public class PowerListener implements Listener {
 
 	public PowerListener(JavaPlugin p) {
 		p.getServer().getPluginManager().registerEvents(this, p);
 	}
 
 	@EventHandler
 	public void onProjectileLaunch(ProjectileLaunchEvent e) {
 		if (e.getEntity().getShooter().getType() == EntityType.PLAYER) {
 			Player p = (Player) e.getEntity().getShooter();
 			if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 				Stats s = new Stats(p);
 				if (p.getItemInHand().getType() == Material.POTION) {
 					if (s.hasPower(Powers.SLOWDOWN)) {
 						if (Potion.fromItemStack(p.getItemInHand()).getType() == PotionType.SLOWNESS) {
 							if (e.getEntityType() == EntityType.SPLASH_POTION) {
 								e.getEntity().setVelocity(p.getLocation().getDirection().normalize().multiply(5));
 							}
 						}
 					}
 					if (s.hasPower(Powers.SPONTANEOUS_COMBUSTION)) {
 						if (Potion.fromItemStack(p.getItemInHand()).getType() == PotionType.POISON) {
 							if (e.getEntityType() == EntityType.SPLASH_POTION) {
 								e.getEntity().setVelocity(p.getLocation().getDirection().normalize().multiply(5));
 							}
 						}
 					}
 					if (s.hasPower(Powers.BLINDNESS)) {
 						if (Potion.fromItemStack(p.getItemInHand()).getType() == PotionType.REGEN) {
 							if (e.getEntityType() == EntityType.SPLASH_POTION) {
 								p.getInventory().remove(p.getItemInHand());
 								e.getEntity().setVelocity(p.getLocation().getDirection().normalize().multiply(5));
 							}
 						}
 					}
 				}
 				if (e.getEntityType() == EntityType.SNOWBALL) {
 					if (new Stats(p).hasPower(Powers.VELOCITY)) {
 						e.getEntity().setVelocity(p.getLocation().getDirection().normalize().multiply(2));
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPotionSplash(PotionSplashEvent e) {
 		if (e.getEntity().getShooter().getType() == EntityType.PLAYER) {
 			Player p = (Player) e.getEntity().getShooter();
 			Stats s = new Stats(p);
 			if (Potion.fromItemStack(e.getPotion().getItem()).getType() == PotionType.SLOWNESS) {
 				if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 					if (s.hasPower(Powers.SLOWDOWN)) {
 						if (!e.getAffectedEntities().isEmpty()) {
 							Chat.sendPPM("Slowdown affected:", p);
 							for (Entity en : e.getAffectedEntities()) {
 								if (en.getType() == EntityType.PLAYER) {
 									if (TeamCyan.hasArenaPlayer((Player) en) || TeamLime.hasArenaPlayer((Player) en))
 										Chat.sendPM("    " + ((Player) en).getName(), p);
 								}
 							}
 						} else {
 							Chat.sendPPM("Slowdown affected no players!", p);
 						}
 					}
 				}
 			}
 			if (Potion.fromItemStack(e.getPotion().getItem()).getType() == PotionType.POISON) {
 				if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 					if (s.hasPower(Powers.SPONTANEOUS_COMBUSTION)) {
 						if (!e.getAffectedEntities().isEmpty()) {
 							Chat.sendPPM("Spontaneous Combustion affected:", p);
 							for (Entity en : e.getAffectedEntities()) {
 								if (en.getType() == EntityType.PLAYER) {
 									if (TeamCyan.hasArenaPlayer((Player) en) || TeamLime.hasArenaPlayer((Player) en))
 										((Player) en).setFireTicks(200);
 									Chat.sendPM("    " + ((Player) en).getName(), p);
 								}
 							}
 						} else {
 							Chat.sendPPM("Spontaneous Combustion affected no players!", p);
 						}
 					}
 				}
 			}
 			if (Potion.fromItemStack(e.getPotion().getItem()).getType() == PotionType.REGEN) {
 				if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 					if (s.hasPower(Powers.BLINDNESS)) {
 						if (!e.getAffectedEntities().isEmpty()) {
 							Chat.sendPPM("Blindness affected:", p);
 							for (Entity en : e.getAffectedEntities()) {
 								if (en.getType() == EntityType.PLAYER) {
 									if (TeamCyan.hasArenaPlayer((Player) en) || TeamLime.hasArenaPlayer((Player) en))
 										Chat.sendPM("    " + ((Player) en).getName(), p);
 								}
 							}
 						} else {
 							Chat.sendPPM("Blindness affected no players!", p);
 						}
 					}
 				}
 			}
 			if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 				if (s.hasPower(Powers.SPEED)) {
 					PowerCoolDown.start(p, 30000);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerShootBow(EntityShootBowEvent e) {
 		if (e.getEntity().getType() == EntityType.PLAYER) {
 			Player p = (Player) e.getEntity();
 			if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 				if (new Stats(p).hasPower(Powers.SNIPER)) {
 					e.getProjectile().setVelocity(p.getLocation().getDirection().normalize().multiply(20));
 					p.getLocation().getWorld().createExplosion(p.getLocation(), 0F);
 					Location smoke = p.getLocation();
 					smoke.setY(smoke.getY() + 1);
 					for (int i = 0; i < 8; i++)
 						p.getWorld().playEffect(smoke, Effect.SMOKE, i);
 					PowerCoolDown.start(p, 30000);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
 		Player p = e.getPlayer();
 		if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 			if (new Stats(p).hasPower(Powers.SNIPER)) {
 				PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100);
 				if (e.isSneaking()) {
 					if (p.getItemInHand().getType() == Material.BOW) {
 						p.addPotionEffect(pe, false);
 					}
 				} else {
 					if (p.hasPotionEffect(PotionEffectType.SLOW)) {
 						p.removePotionEffect(PotionEffectType.SLOW);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
	public void onPlayerSwitchItem(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
			if (new Stats(p).hasPower(Powers.SNIPER)) {
				if (e.getPlayer().getInventory().getItem(e.getNewSlot()) != null && e.getPlayer().getInventory().getItem(e.getNewSlot()).getType() == Material.BOW) {
					if (p.isSneaking()) {
						PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100);
						p.addPotionEffect(pe, false);
					}
				} else {
					if (p.hasPotionEffect(PotionEffectType.SLOW)) {
						p.removePotionEffect(PotionEffectType.SLOW);
					}
				}
			}
		}
	}

	@EventHandler
 	public void onProjectileHit(ProjectileHitEvent e) {
 		if (e.getEntity() instanceof Arrow) {
 			Arrow arrow = (Arrow) e.getEntity();
 			if (arrow.getShooter() instanceof Player) {
 				if (TeamCyan.hasArenaPlayer((Player) arrow.getShooter()) || TeamLime.hasArenaPlayer((Player) arrow.getShooter())) {
 					arrow.remove();
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onItemInteract(PlayerInteractEvent e) {
 		if (TeamCyan.hasArenaPlayer(e.getPlayer()) || TeamLime.hasArenaPlayer(e.getPlayer())) {
 			if (e.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD && e.getAction() == Action.LEFT_CLICK_AIR) {
 				if (new Stats(e.getPlayer()).hasPower(Powers.SMITE)) {
 					ArrayList<String> players = new ArrayList<String>();
 					if (TeamCyan.hasArenaPlayer(e.getPlayer())) {
 						players.addAll(TeamLime.getPlayersinarena());
 					} else if (TeamLime.hasArenaPlayer(e.getPlayer())) {
 						players.addAll(TeamCyan.getPlayersinarena());
 					}
 					int playeramount = players.size();
 					Random r = new Random();
 					r.setSeed(System.currentTimeMillis());
 					int randnum = r.nextInt(playeramount);
 					int playernum = 0;
 					for (String pl : players) {
 						if (randnum == playernum) {
 							Bukkit.getPlayer(pl).getWorld().strikeLightningEffect(Bukkit.getPlayer(pl).getLocation());
 							Bukkit.getPlayer(pl).damage(17);
 							Chat.sendPPM("You've been smitten by " + e.getPlayer().getName() + ChatColor.RESET + "!", Bukkit.getPlayer(pl));
 							Chat.sendPPM("You've just smitten " + pl + ChatColor.RESET + "!", e.getPlayer());
 							break;
 						}
 						playernum++;
 					}
 					e.getPlayer().getInventory().remove(Material.BLAZE_ROD);
 				}
 				Stats s = new Stats(e.getPlayer());
 				if (s.hasPower(Powers.SMITE)) {
 					PowerCoolDown.start(e.getPlayer(), 60000);
 				}
 			}
 		}
 	}
 
 }
