 package com.github.kaitoyuuki.MagicArrows;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.Vector;
 import org.bukkit.entity.Fireball;
 
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.object.TownyPermission;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.GlobalRegionManager;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 
 @SuppressWarnings("unused")
 public class BowListener implements Listener {
 	private MagicArrows plugin;
 	private Towny towny;
 	private WorldGuardPlugin worldguard;
 
 	private TownyUniverse townyUniverse;
 	private PlayerCacheUtil playerCacheUtil;
 	private GlobalRegionManager grm;
 
 	public BowListener(MagicArrows plugin) {
 		this.plugin = plugin;
 		towny = this.plugin.getTowny();
 		worldguard = this.plugin.getWG();
 		if (towny != null) {
 			townyUniverse = towny.getTownyUniverse();
 		}
 		if (worldguard != null) {
 			grm = worldguard.getGlobalRegionManager();
 		}
 
 	}
 	public boolean freezeBlock(Location loc) {
 		Block block = loc.getBlock();
 		if (block.getType() == Material.STATIONARY_WATER){
 			block.setType(Material.ICE);
 			return true;
 		}
 		else if (block.getType() == Material.STATIONARY_LAVA){
 			block.setType(Material.OBSIDIAN);
 			return true;
 		}
 		else if (block.getType() == Material.LAVA){
 			block.setType(Material.COBBLESTONE);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	@EventHandler
 	public void onArrowLaunch(EntityShootBowEvent event) {
 		ItemStack bow = event.getBow();
 		Projectile projectile = (Projectile) event.getProjectile();
 		Player player = null;
 		World world = projectile.getWorld();
 		if(bow != null && event.getEntityType() == EntityType.PLAYER){
 			for (Player pcheck : Bukkit.getServer().getOnlinePlayers()) {
 				if (pcheck.getEntityId() == event.getEntity().getEntityId()) {
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
 								return;
 							}
 							else if (id == 385) {
 								if (player.hasPermission("magicarrows.fireball")) {
 									Random generator = new Random();
 									int r = generator.nextInt(10);
 									if (r < 3) {
 										Effect effect = Effect.GHAST_SHRIEK;
 										World pworld = player.getWorld();
 										Location loc = player.getLocation();
 										pworld.playEffect(loc, effect, 0);
 									}
 									((Projectile) projectile.getWorld().spawnEntity(projectile.getLocation(), EntityType.FIREBALL)).setShooter(player);
 									Effect effect = Effect.GHAST_SHOOT;
 									World pworld = player.getWorld();
 									Location loc = player.getLocation();
 									pworld.playEffect(loc, effect, 0);
 									int slot = bowslot - 1;
 									PlayerInventory inventory = player.getInventory();
 									ItemStack stack = inventory.getItem(slot);
 									int amt = stack.getAmount();
 									stack.setAmount(amt - 1);
 									inventory.setItem(slot, stack);
 									projectile.remove();
 									plugin.incFireBall();
 								}
 							}
 						} catch (NullPointerException e) {
 							
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onArrowHit(ProjectileHitEvent event) {
 		Projectile projectile = (Projectile) event.getEntity();
 		Location loc = projectile.getLocation();
 		Block block = loc.getBlock();
 		Entity shooter = projectile.getShooter();
 		Player player = null;
 		World world = projectile.getWorld();
 		for (Player pcheck : Bukkit.getServer().getOnlinePlayers()) {
 			if (pcheck.getEntityId() == shooter.getEntityId()) {
 				player = pcheck;
 			}
 		}
		if (player == null) {
			return;
		}
		else if (player != null) {
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
 							return;
 						}
 						else if (id == 50) {
 							//torch: place a torch where the arrow stopped
 							if (player.hasPermission("magicarrows.torch")) {
 								if (towny != null) {
 									if (townyUniverse != null) {
 										if(!PlayerCacheUtil.getCachePermission(player, loc, block.getTypeId(), TownyPermission.ActionType.BUILD)) {
 											return;
 										}
 									}
 								}
 								else if (worldguard != null) {
 									if (grm != null) {
 										if (!grm.hasBypass(player, world) || !grm.canBuild(player, loc)) {
 											return;
 										}
 									}
 								}
 								else if (block.getType() != Material.AIR) {
 									return;
 								}
 								else {
 									block.setTypeId(50);
 									int slot = bowslot - 1;
 									PlayerInventory inventory = player.getInventory();
 									ItemStack stack = inventory.getItem(slot);
 									int amt = stack.getAmount();
 									stack.setAmount(amt - 1);
 									inventory.setItem(slot, stack);
 									projectile.remove();
 									plugin.incTorch();
 									return;
 								}
 							}
 						}
 						else if (id == 46) {
 							//tnt: create an explosion where the arrow stops
 							if (player.hasPermission("magicarrows.tnt")) {
 								if (towny != null) {
 									if (townyUniverse != null) {
 										if(!PlayerCacheUtil.getCachePermission(player, loc, block.getTypeId(), TownyPermission.ActionType.BUILD)) {
 											return;
 										}
 									}
 								}
 								else if (worldguard != null) {
 									if (grm != null) {
 										if (!grm.hasBypass(player, world) || !grm.canBuild(player, loc)) {
 											return;
 										}
 									}
 								}
 								else {
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
 									return;
 								}
 							}
 						}
 						else if (id == 259) {
 							//flint and steel: real fire arrows
 							if (player.hasPermission("magicarrows.fire")) {
 								if (towny != null) {
 									if (townyUniverse != null) {
 										if(!PlayerCacheUtil.getCachePermission(player, loc, block.getTypeId(), TownyPermission.ActionType.BUILD)) {
 											return;
 										}
 										// also check if flint & steel is allowed here
 										if(!PlayerCacheUtil.getCachePermission(player, loc, block.getTypeId(), TownyPermission.ActionType.ITEM_USE)) {
 											// TODO figure out how to check specifically about flint and steel.
 											return;
 										}
 									}
 								}
 								else if (worldguard != null) {
 									if (grm != null) {
 										if (!grm.hasBypass(player, world) || !grm.canBuild(player, loc)) {
 											return;
 										}
 										// TODO also need to check if lighters are allowed here. How do I do that?
 									}
 								}
 								else if (block.getType() != Material.AIR){
 									return;
 								}
 								else {
 									block.setTypeId(51);
 									int slot = bowslot - 1;
 									PlayerInventory inventory = player.getInventory();
 									ItemStack stack = inventory.getItem(slot);
 									short dmg = stack.getDurability();
									dmg++;
 									stack.setDurability(dmg);
 									projectile.remove();
 									plugin.incFire();
 								}
 							}
 						}
 						else if (id == 262) {
 							//arrow: double arrow? no, if I want to do something
 							//like this, it would have to be when shooting.
 
 						}
 						else if (id == 264) {
 							//diamond(maybe iron instead?): lightning charge
 							if (player.hasPermission("magicarrows.smite")) {
 								world.strikeLightning(loc);
 								int slot = bowslot - 1;
 								PlayerInventory inventory = player.getInventory();
 								ItemStack stack = inventory.getItem(slot);
 								int amt = stack.getAmount();
 								stack.setAmount(amt - 1);
 								inventory.setItem(slot, stack);
 								projectile.remove();
 								plugin.incSmite();
 							}
 
 						}
 						else if (id == 326) {
 							//water bucket: water balloon?
 
 						}
 						else if (id == 332) {
 							//snowball: freezer shot
 							//hmm. I may have to set this one up differently...
 							//as it is now, you will have to freeze from the bottom of the lake up.
 							// Ah, actually, I can use this part for if it hits a block or an entity.
 							if (player.hasPermission("magicarrows.freeze")) {
 								plugin.incIce();
 								int slot = bowslot - 1;
 								PlayerInventory inventory = player.getInventory();
 								ItemStack stack = inventory.getItem(slot);
 								int amt = stack.getAmount();
 								stack.setAmount(amt - 1);
 								inventory.setItem(slot, stack);
 								projectile.remove();
 								// Check the blocks in the appropriate radius around the arrow
 								// If water, lava, fire, or an entity is found
 								// freeze it. Produce smoke effect around the entity
 								// and prevent it from moving or attacking for five seconds or so.
 								// appropriate radius... 3 or so, I guess.
 								// get these from config later, maybe.
 								int radius = 3;
 								int frzTime = 5;
 								
 								int diameter = 2*radius;
 								Location check = loc;
 
 								double x, y, z;
 								for (x = loc.getX() - radius; x < loc.getX() + radius; x++) {
 									for (y = loc.getY() - radius - 1; y < loc.getY() - 1 + radius; y++) {
 										for (z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
 											check = new Location(world, x, y, z);
 											if (check.distance(loc) < radius) {
 												freezeBlock(check);
 											}
 										}
 									}
 								}
 								for (x = loc.getX() - radius; x < loc.getX() + radius; x++) {
 									for (y = loc.getY() - radius - 1; y < loc.getY() - 1 + radius; y++) {
 										for (z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
 											check = new Location(world, x, y, z);
 											if (check.distance(loc) < radius) {
 												freezeBlock(check);
 											}
 										}
 									}
 								}
 								
 							}
 						}
 						else if (id == 385) {
 							//firecharge: ghast fireball
 							//this one might be better to do at the start...
 							if (player.hasPermission("magicarrows.fireball")) {
 
 							}
 						}
 
 
 
 						else {
 							return;
 						}
 					} catch (NullPointerException e) {
 						return;
 					}
 				}
 				else {
 					return;
 				}
 			}
 		}
 		else {
 			return;
 		}
 	}
 }
