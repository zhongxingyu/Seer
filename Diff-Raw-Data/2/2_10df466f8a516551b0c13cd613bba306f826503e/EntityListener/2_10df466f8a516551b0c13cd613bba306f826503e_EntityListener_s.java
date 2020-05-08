 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCustom.events;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import de.dariusmewes.TimoliaCustom.Message;
 import de.dariusmewes.TimoliaCustom.TimoliaCustom;
 import de.dariusmewes.TimoliaCustom.commands.protnpc;
 import de.dariusmewes.TimoliaCustom.commands.sapopvp;
 
 public class EntityListener implements Listener {
 
 	private static World sapoWorld;
 	private static int pos1X;
 	private static int pos1Y;
 	private static int pos1Z;
 	private static int pos2X;
 	private static int pos2Y;
 	private static int pos2Z;
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		EntityType t = event.getEntity().getType();
 
 		// protnpc
 		if (t == EntityType.VILLAGER && event.getDamager().getType() == EntityType.PLAYER) {
 			Player p1 = (Player) event.getDamager();
 			if (protnpc.active.contains(p1.getName())) {
 				if (protnpc.prot.contains(event.getEntity().getUniqueId())) {
 					protnpc.prot.remove(event.getEntity().getUniqueId());
 					protnpc.active.remove(p1.getName());
 					p1.sendMessage(TimoliaCustom.PREFIX + "Der NPC-Schutz wurde aufgehoben!");
 					event.setCancelled(true);
 
 				} else {
 					protnpc.prot.add(event.getEntity().getUniqueId());
 					protnpc.active.remove(p1.getName());
 					p1.sendMessage(TimoliaCustom.PREFIX + "Der NPC wurde geschtzt!");
 					event.setCancelled(true);
 				}
 				return;
 			}
 
 			if (protnpc.prot.contains(event.getEntity().getUniqueId())) {
 				p1.kickPlayer(TimoliaCustom.PREFIX + "Can't touch this!");
				Message.console(p1 + " wollte einen NPC schlagen! --> Kick!");
 				protnpc.addName(p1.getName(), (Villager) event.getEntity());
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		// sapopvp
 
 		if (t == EntityType.PLAYER) {
 			Player victim = (Player) event.getEntity();
 			if (!insideArena(victim) || !ingame(victim))
 				return;
 
 			Player attacker;
 
 			if (event.getDamager().getType() == EntityType.ARROW || event.getDamager().getType() == EntityType.EGG || event.getDamager().getType() == EntityType.SNOWBALL || event.getDamager().getType() == EntityType.SPLASH_POTION) {
 				Projectile bullet = (Projectile) event.getDamager();
 
 				if (bullet.getShooter().getType() != EntityType.PLAYER)
 					return;
 
 				attacker = (Player) bullet.getShooter();
 
 				if (!ingame(attacker) || sameTeam(victim, attacker)) {
 					event.setCancelled(true);
 					return;
 				}
 
 				if (bullet.getType() == EntityType.EGG)
 					victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2));
 				else if (bullet.getType() == EntityType.SNOWBALL)
 					victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 2));
 
 			} else if (event.getDamager().getType() == EntityType.PLAYER) {
 				attacker = (Player) event.getDamager();
 				if (!ingame(attacker) || sameTeam(victim, attacker)) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
 	public static boolean insideArena(Player p) {
 		int px = p.getLocation().getBlockX();
 		int py = p.getLocation().getBlockY();
 		int pz = p.getLocation().getBlockZ();
 		if (p.getWorld() == sapoWorld) {
 			return pos1X < px && px < pos2X && pos1Y < py && py < pos2Y && pos1Z < pz && pz < pos2Z;
 		} else {
 			return false;
 		}
 	}
 
 	private boolean sameTeam(Player p1, Player p2) {
 		if (sapopvp.gold.contains(p1.getName())) {
 			return sapopvp.gold.contains(p2.getName());
 		} else if (sapopvp.eisen.contains(p1.getName())) {
 			return sapopvp.eisen.contains(p2.getName());
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean ingame(Player p) {
 		if (sapopvp.gold.contains(p.getName()) || sapopvp.eisen.contains(p.getName()))
 			return true;
 		else
 			return false;
 	}
 
 	public static void saveArenaCoords(String[] pos1, String[] pos2) {
 		sapoWorld = Bukkit.getWorld(pos1[0]);
 		pos1X = Math.min(Integer.valueOf(pos1[1]), Integer.valueOf(pos2[1]));
 		pos2X = Math.max(Integer.valueOf(pos1[1]), Integer.valueOf(pos2[1]));
 
 		pos1Y = Math.min(Integer.valueOf(pos1[2]), Integer.valueOf(pos2[2]));
 		pos2Y = Math.max(Integer.valueOf(pos1[2]), Integer.valueOf(pos2[2]));
 
 		pos1Z = Math.min(Integer.valueOf(pos1[3]), Integer.valueOf(pos2[3]));
 		pos2Z = Math.max(Integer.valueOf(pos1[3]), Integer.valueOf(pos2[3]));
 	}
 
 }
