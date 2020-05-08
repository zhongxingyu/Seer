 package com.goldrushmc.bukkit.guns;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 public class Shotgun {
 	Player p;
 	Plugin plugin;
 
 	public HashMap<Player, Boolean> cockHash = new HashMap<Player, Boolean>();
 	public HashMap<Player, Boolean> hasReloadedHash = new HashMap<Player, Boolean>();
 	public GunTools gunTools = new GunTools();
 	public int fireDelay = 60;
 	public List<Integer> firedEntity = new ArrayList<Integer>();
 	public int damage = 12;
 	public int shotNo = 6;
 	boolean canFire = true;
 
 	public Shotgun(Player player, Plugin plu) {
 		p = player;
 		plugin = plu;
 	}
 
 	public void fire() {
 		if(canFire)
 		if (p.getItemInHand().getDurability() < 31) {
 			if (cockHash.get(p)) {
 				
 				//gun fire sound
 				p.playSound(p.getLocation(), Sound.ZOMBIE_METAL,1, -3f);
 				
 				//gun fire smoke effect
 				Location smokePos = gunTools.getSpawnLoc(p);
 				p.playEffect(smokePos, Effect.SMOKE, 0);
 				
 				//smoke and gun for other players
 				List<Player> plList = gunTools.getPlayersWithin(p, 50);
 				double distance = 0;
 				for(int i = 0; i < plList.size();i++) {
 					plList.get(i).playEffect(smokePos, Effect.SMOKE, 0);
 					distance = plList.get(i).getLocation().distance(p.getLocation());
 					if(distance <=50 && distance > 0){
 						plList.get(i).playSound(smokePos, Sound.ZOMBIE_METAL, (float)  (1 - (distance / 50)), -3f);
 					}
 				}
 				
 				Random randomGenerator = new Random();
 				for(int i = 0; i < shotNo; i++) {
 					Snowball snowball = p.launchProjectile(Snowball.class);
 					snowball.setVelocity(p.getLocation().getDirection().multiply(5));
 									
 					int randomInt = randomGenerator.nextInt(2);
 					switch(randomInt) {
 					case 0:
						p.sendMessage(String.valueOf(snowball.getVelocity().getZ()));
 						snowball.setVelocity(snowball.getVelocity().add(new Vector(0, 0, snowball.getVelocity().getZ()/4)));
 						break;
 					case 1:
						p.sendMessage(String.valueOf(snowball.getVelocity().getX()));
 						snowball.setVelocity(snowball.getVelocity().add(new Vector(snowball.getVelocity().getX()/4, 0, 0)));
 						break;
 					case 2:
						p.sendMessage(String.valueOf(snowball.getVelocity().getY()));
 						snowball.setVelocity(snowball.getVelocity().add(new Vector(0, snowball.getVelocity().getY()/4, 0)));
 						break;
 					}					
 					
 					snowball.setShooter(p);					
 					firedEntity.add(snowball.getEntityId());
 					Bukkit.getServer().getScheduler().runTaskLater(plugin, new DestroyAfter(snowball), 6);
 				}
 								
 				p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() + 30));
 				cockHash.put(p, false);
 				
 				FiringDelay fd = new FiringDelay();
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, fd, fireDelay);
 				canFire = false;
 			} else {
 				p.playSound(p.getLocation(), Sound.NOTE_BASS_DRUM,10, -1f);
 			}
 		} else {
 			p.playSound(p.getLocation(), Sound.CLICK,5, 2f);
 		}
 	}
 
 	public void reload() {
 		hasReloadedHash.put(p, false);
 		if (p.getItemInHand().getDurability() > 1 && p.getItemInHand().getDurability() < 32) {
 			for (int i = 0; i < 36; i++) {
 				if (p.getInventory().getItem(i) != null) {
 					if (p.getInventory().getItem(i).getTypeId() == 332) {
 						if (p.getInventory().getItem(i).getAmount() == 1 || p.getInventory().getItem(i).getAmount() == 2) {
 							p.getInventory().clear(i);
 						} else {
 							p.getInventory().getItem(i).setAmount(p.getInventory().getItem(i).getAmount() - 2);
 						}
 						p.playSound(p.getLocation(), Sound.CLICK, 5, 2f);
 						p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() - 30));
 						hasReloadedHash.put(p, true);
 						break;
 					}
 				}
 			}
 			if (!hasReloadedHash.get(p)) {
 				p.sendMessage(ChatColor.DARK_RED + "Out of Ammo!");
 			}
 		}
 	}
 	
 	public void cock() {
 		if (p.getItemInHand().getDurability() < 32) {
 			cockHash.put(p, true);
 			p.playSound(p.getLocation(), Sound.DOOR_OPEN, 5, 1);
 		}		
 	}		
 	
 	class FiringDelay implements Runnable{
 
 		@Override
 		public void run() {
 			canFire = true;
 		}
 	}
 	
 	class DestroyAfter implements Runnable{
 		Snowball snowball;
 		public DestroyAfter(Snowball sn) {
 			snowball = sn;
 		}
 		
 		@Override
 		public void run() {
 			snowball.remove();
 		}
 	}
 }
