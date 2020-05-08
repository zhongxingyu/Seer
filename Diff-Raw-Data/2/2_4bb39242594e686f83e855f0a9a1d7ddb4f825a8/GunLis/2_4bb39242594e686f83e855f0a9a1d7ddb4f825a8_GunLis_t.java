 package com.goldrushmc.bukkit.guns;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 import com.goldrushmc.bukkit.defaults.DefaultListener;
 import com.goldrushmc.bukkit.defaults.DefaultListener;
 
 
 public class GunLis extends DefaultListener {
 
 	public GunLis(JavaPlugin plugin) {
 		super(plugin);
 		// TODO Auto-generated constructor stub
 	}
 
 	public HashMap<Player, Boolean> cockHash = new HashMap<Player, Boolean>();
 	public HashMap<Player, Boolean> hasReloadedHash = new HashMap<Player, Boolean>();
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onRightClick(PlayerInteractEvent e) {		
 		Action eAction = e.getAction();
 		Player p = e.getPlayer();
 		
 		if(!cockHash.containsKey(p)){
 			cockHash.put(p, false);
 		}
 		
 		if (eAction.equals(Action.RIGHT_CLICK_AIR)) {
 			if (p.getItemInHand().getType().equals(Material.CARROT_STICK)) {
 				if (p.getItemInHand().getItemMeta().hasDisplayName()) {
 					if (p.getItemInHand().getItemMeta().getDisplayName()
 							.equals("Colt")) {
 						int max = Material.DIAMOND_HOE.getMaxDurability();
 						if (p.getItemInHand().getDurability() < 25) {
 							if (cockHash.get(p)) {
 								//Bullet bullet = p.launchProjectile(Bullet.class);
 								//p.sendMessage(String.valueOf(bullet.getEntityId()));
 								//p.getWorld().spawnEntity(p.getEyeLocation(), EntityBullet);
 								
 								//gun fire sound
 								p.playSound(p.getLocation(), Sound.ZOMBIE_METAL,1, -3f);
 								
 								//gun fire smoke effect
 								Location smokePos = getSpawnLoc(p);
 								p.playEffect(smokePos, Effect.SMOKE, 0);
 								
 								//smoke and gun for other players
 								List<Player> plList = getPlayersWithin(p, 50);
 								double distance = 0;
 								for(int i = 0; i < plList.size();i++) {
 									plList.get(i).playEffect(smokePos, Effect.SMOKE, 0);
 									distance = plList.get(i).getLocation().distance(p.getLocation());
 									if(distance <=50 && distance > 0){
 										plList.get(i).playSound(smokePos, Sound.ZOMBIE_METAL, (float)  (1 - (distance / 50)), -3f);
 									}
 								}
 								
 								Snowball snowball = p.launchProjectile(Snowball.class);
 								snowball.setVelocity(p.getLocation().getDirection().multiply(5));
 								
 								HeightModTask hmt = new HeightModTask(snowball);
 								Bukkit.getServer().getScheduler().runTaskLater(plugin, hmt, 1);
 								
 								p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() + 4));
 								cockHash.put(p, false);
 							} else {
 								p.playSound(p.getLocation(), Sound.NOTE_BASS_DRUM,10, -1f);
 							}
 						} else {
 							p.playSound(p.getLocation(), Sound.CLICK,5, 2f);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onLeftClick(PlayerInteractEvent e) {		
 		Action eAction = e.getAction();
 		Player p = e.getPlayer();
 		
 		if(p.isSneaking()){
 			Reload(p);
 		} else {
 		if (eAction.equals(Action.LEFT_CLICK_AIR)) {
 			if (p.getItemInHand().getType().equals(Material.CARROT_STICK)) {
 				if (p.getItemInHand().getItemMeta().hasDisplayName()) {
 					if (p.getItemInHand().getItemMeta().getDisplayName()
 							.equals("Colt")) {
 						if (p.getItemInHand().getDurability() < 25) {
 							cockHash.put(p, true);
 							p.playSound(p.getLocation(), Sound.DOOR_OPEN, 5, 1);
 						}
 					}
 				}
 			}
 		}
 		}
 	}
 	
 	public List<Player> getPlayersWithin(Player player, int distance) {
 		List<Player> res = new ArrayList<Player>();
 		int d2 = distance * distance;
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (p.getWorld() == player.getWorld() && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
 				res.add(p);
 			}
 		}
 		return res;
 	}
 	
 	public void Reload(Player p) {
 	hasReloadedHash.put(p, false);
 	if (p.getItemInHand().getType().equals(Material.CARROT_STICK)) {
 		if (p.getItemInHand().getItemMeta().hasDisplayName()) {
 			if (p.getItemInHand().getItemMeta().getDisplayName().equals("Colt")) {
				if (p.getItemInHand().getDurability() > 1 && p.getItemInHand().getDurability() < 26) {
 					for (int i = 0; i < 36; i++) {
 						if(p.getInventory().getItem(i) != null) {
 							if(p.getInventory().getItem(i).getTypeId() == 332){
 								if(p.getInventory().getItem(i).getAmount() == 1){
 									p.getInventory().clear(i);
 								} else {
 									p.getInventory().getItem(i).setAmount(p.getInventory().getItem(i).getAmount() - 1);
 								}
 								p.playSound(p.getLocation(), Sound.CLICK,5, 2f);
 								p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() - 4));
 								hasReloadedHash.put(p, true);
 								break;
 								}
 							}
 						}
 						if(!hasReloadedHash.get(p)){
 							p.sendMessage(ChatColor.DARK_RED + "Out of Ammo!");
 						}
 				}
 			}
 		}
 	}
 	}
 	
 	class HeightModTask implements Runnable{
 		Snowball s;
 		HeightModTask(Snowball snowball){
 			s = snowball;
 		}
 
 		@Override
 		public void run() {			
 			if(!s.isDead()){
 				s.setFallDistance(0);
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, this, 1);
 			}
 		}
 
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onDamage(EntityDamageEvent e) {
 		if (e.getCause().equals(DamageCause.PROJECTILE)) {
 			if (e.getDamage() == 0) {
 				e.setDamage(8);
 			}
 		}
 	}
 
 	public Location getSpawnLoc(Player p) {
 		if (getCardinalDirection(p) == "N") {
 			Location loc = p.getEyeLocation().add(
 					p.getEyeLocation().getDirection()
 							.add(new Vector(0.5, 0, -0.5)));
 			return loc;
 		} else if (getCardinalDirection(p) == "NW") {
 			Location loc = p.getEyeLocation().add(
 					p.getEyeLocation().getDirection()
 							.add(new Vector(-0.5, 0, -0.5)));
 			return loc;
 		} else if (getCardinalDirection(p) == "W") {
 			Location loc = p.getEyeLocation().add(
 					p.getEyeLocation().getDirection()
 							.add(new Vector(-0.5, 0, 0.5)));
 			return loc;
 		} else if (getCardinalDirection(p) == "SW") {
 			Location loc = p.getEyeLocation().add(
 					p.getEyeLocation().getDirection()
 							.add(new Vector(-0.5, 0, -0.5)));
 			return loc;
 		} else {
 			Location loc = p.getEyeLocation().add(
 					p.getEyeLocation().getDirection()
 							.add(new Vector(0.5, 0, 0.5)));
 			return loc;
 		}
 	}
 
 	public static String getCardinalDirection(Player player) {
 		double rotation = (player.getLocation().getYaw() - 90) % 360;
 		if (rotation < 0) {
 			rotation += 360.0;
 		}
 		if (0 <= rotation && rotation < 22.5) {
 			return "N";
 		} else if (22.5 <= rotation && rotation < 67.5) {
 			return "NE";
 		} else if (67.5 <= rotation && rotation < 112.5) {
 			return "E";
 		} else if (112.5 <= rotation && rotation < 157.5) {
 			return "SE";
 		} else if (157.5 <= rotation && rotation < 202.5) {
 			return "S";
 		} else if (202.5 <= rotation && rotation < 247.5) {
 			return "SW";
 		} else if (247.5 <= rotation && rotation < 292.5) {
 			return "W";
 		} else if (292.5 <= rotation && rotation < 337.5) {
 			return "NW";
 		} else if (337.5 <= rotation && rotation < 360.0) {
 			return "N";
 		} else {
 			return null;
 		}
 	}
 }
