 package com.github.kaitoyuuki.MagicArrows;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.Vector;
 
 @SuppressWarnings("unused")
 public class BowListener implements Listener {
 	private MagicArrows plugin;
 
 	public BowListener(MagicArrows plugin) {
 		this.plugin = plugin;
 
 	}
 	@EventHandler
 	public void onArrowHit(ProjectileHitEvent event) {
 		Location loc = event.getEntity().getLocation();
 		Block block = loc.getBlock();
 		Projectile projectile = (Projectile) event.getEntity();
 		Entity shooter = projectile.getShooter();
 		Player player = null;
 		List<Integer> hotslots = new ArrayList<Integer>();
 		for (int i = 0; i < 8; i++) {
 			hotslots.add(i);
 		}
 		for (Player pcheck : Bukkit.getServer().getOnlinePlayers()) {
 			if (pcheck.getEntityId() == shooter.getEntityId()) {
 				player = pcheck;
 			}
 		}
 		if (player != null) {
 			Material modifier = Material.AIR;
 			int id = 0;
 			if (player.getInventory().getItemInHand().getType() == Material.BOW) {
 				int bowslot = player.getInventory().getHeldItemSlot();
 				if (bowslot > 0) {
 					try {
 						modifier = player.getInventory().getItem(bowslot - 1).getType();
 						if (modifier == Material.AIR){
 							id = 0;
 						}
 						else {
 							id = player.getInventory().getItem(bowslot - 1).getTypeId();
 						}
 						if (id == 0) {
 							//do nothing
 						}
 						else if (id == 50) {
 							//torch: place a torch where the arrow stopped
 							if (player.hasPermission("magicarrows.torch")) {
 								block.setTypeId(50);
 								int slot = bowslot - 1;
 								PlayerInventory inventory = player.getInventory();
 								ItemStack stack = inventory.getItem(slot);
 								int amt = stack.getAmount();
 								stack.setAmount(amt - 1);
 								inventory.setItem(slot, stack);
 								projectile.remove();
								plugin.incTorch();
 							}
 						}
 						else if (id == 46) {
 							//tnt: create an explosion where the arrow stops
 							if (player.hasPermission("magicarrows.tnt")) {
 								int blast = plugin.getConfig().getInt("arrows.tnt");
 								projectile.getWorld().createExplosion(projectile.getLocation(), blast);
 								int slot = bowslot - 1;
 								PlayerInventory inventory = player.getInventory();
 								ItemStack stack = inventory.getItem(slot);
 								int amt = stack.getAmount();
 								stack.setAmount(amt - 1);
 								inventory.setItem(slot, stack);
 								projectile.remove();
								plugin.incTNT();
 							}
 						}
 						else if (id == 259) {
 							//flint and steel: real fire arrows
 							if (player.hasPermission("magicarrows.fire")) {
 							block.setTypeId(51);
 							projectile.remove();
							plugin.incFire();
 							}
 						}
 						else if (id == 262) {
 							//arrow: double arrow? no, if I want to do something
 							//like this, it would have to be when shooting.
 							
 						}
 						else if (id == 264) {
 							//diamond(maybe iron instead?): lightning charge
 							if (player.hasPermission("magicarrows.smite")) {
 								
 							}
 							
 						}
 						else if (id == 326) {
 							//water bucket: water balloon?
 							
 						}
 						else if (id == 332) {
 							//snowball: freezer shot
 							//hmm. I may have to set this one up differently...
 							//as it is now, you will have to freeze from the bottom of the lake up.
 							if (player.hasPermission("magicarrows.freeze")) {
 								
 							}
 						}
 						else if (id == 385) {
 							//firecharge: ghast fireball
 							//this one might be better to do at the start...
 							if (player.hasPermission("magicarrows.fireball")) {
 								
 							}
 						}
 						
 						
 						
 						else {
 							//do nothing
 						}
 					} catch (NullPointerException e) {
 						//it was air.
 					}
 				}
 				else {
 					//no modifier
 				}
 			}
 		}
 		else {
 			//stop here
 		}
 	}
 }
