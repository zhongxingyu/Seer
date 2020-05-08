 package co.networkery.uvbeenzaned.SnowBrawl;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionType;
 
 public class PowerListener implements Listener {
 
 	public PowerListener(JavaPlugin p) {
 		p.getServer().getPluginManager().registerEvents(this, p);
 	}
 
 	@EventHandler
 	public void onProjectileLaunch(ProjectileLaunchEvent e) {
 		if (e.getEntity().getShooter().getType() == EntityType.PLAYER) {
 			Player p = (Player) e.getEntity().getShooter();
 			Stats s = new Stats(p);
 			if (s.getPower() == Powers.SLOWDOWN) {
 				if (p.getItemInHand().getType() == Material.POTION) {
 					if (Potion.fromItemStack(p.getItemInHand()).getType() == PotionType.SLOWNESS) {
 						if (e.getEntityType() == EntityType.SPLASH_POTION) {
 							e.getEntity().setVelocity(
 									p.getLocation().getDirection().normalize()
 											.multiply(5));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPotionSplash(PotionSplashEvent e) {
 		if (e.getEntity().getShooter().getType() == EntityType.PLAYER) {
 			Player p = (Player) e.getEntity().getShooter();
 			if (Potion.fromItemStack(e.getPotion().getItem()).getType() == PotionType.SLOWNESS) {
 				if (TeamCyan.hasArenaPlayer(p) || TeamLime.hasArenaPlayer(p)) {
 					if (!e.getAffectedEntities().isEmpty()) {
 						Chat.sendPPM("Slow down affected:", p);
 						for (Entity en : e.getAffectedEntities()) {
 							if (en.getType() == EntityType.PLAYER) {
 								if (TeamCyan.hasArenaPlayer((Player) en)
 										|| TeamLime.hasArenaPlayer((Player) en))
									Chat.sendPM("    " + (Player) en, p);
 							}
 						}
 					} else {
 						Chat.sendPPM("Slowdown affected no players!", p);
 					}
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
 					e.getProjectile().setVelocity(
 							p.getLocation().getDirection().normalize()
 									.multiply(20));
 					p.getLocation().getWorld()
 							.createExplosion(p.getLocation(), 0F);
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
 	public void onArrowHit(ProjectileHitEvent e) {
 		if (e.getEntity() instanceof Arrow) {
 			Arrow arrow = (Arrow) e.getEntity();
 			if (arrow.getShooter() instanceof Player) {
 				if (TeamCyan.hasArenaPlayer((Player) arrow.getShooter())
 						|| TeamLime.hasArenaPlayer((Player) arrow.getShooter())) {
 					arrow.remove();
 				}
 			}
 		}
 	}
 
 }
