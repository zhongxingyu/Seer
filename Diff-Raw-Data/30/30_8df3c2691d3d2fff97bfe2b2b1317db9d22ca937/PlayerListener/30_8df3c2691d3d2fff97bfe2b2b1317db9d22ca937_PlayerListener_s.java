 package com.thebkkingdom.Shotgun;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener implements Listener {
 
 	@EventHandler
 	public void playerInteract(PlayerInteractEvent event) {
 
 		Player player = event.getPlayer();
 		Location playerLocation = player.getLocation();
 		World world = player.getWorld();
 
 		if (event.getAction() == Action.LEFT_CLICK_AIR) {
 			if (player.getItemInHand().getType() == Material.BOOK) {
				if (player.hasPermission("shotgun.shotgun") || player.isOp()) {
 
 					world.playEffect(playerLocation, Effect.BOW_FIRE, 50);
 					world.createExplosion(playerLocation, -1);
 					world.createExplosion(playerLocation, -1);
 					world.playEffect(playerLocation, Effect.SMOKE, 105);
 
					player.launchProjectile(Arrow.class);
					player.launchProjectile(Arrow.class);
					player.launchProjectile(Arrow.class);
					player.launchProjectile(Arrow.class);
					player.launchProjectile(Arrow.class);
 
 					if (player.getInventory().contains(
 							new ItemStack(Material.ARROW, 5))) {
 						player.getInventory().removeItem(
 								new ItemStack(Material.ARROW, 5));
 					} else {
 						player.sendMessage(ChatColor.BLUE
 								+ "[Shotgun] You need at least 5 arrows to use the shotgun!");
 					}
 				}
 			}
 
 			if (player.getItemInHand().getType() == Material.SNOW_BALL) {
 				player.getWorld().playEffect(playerLocation, Effect.EXTINGUISH,
 						100);
 			}
 
 			if (event.getAction() == Action.RIGHT_CLICK_AIR) {
 				if (player.getItemInHand().getType() == Material.BOOK) {
 
 					world.spawnCreature(playerLocation, EntityType.FIREBALL);
 				}
 			}
 		}
 	}
 }
