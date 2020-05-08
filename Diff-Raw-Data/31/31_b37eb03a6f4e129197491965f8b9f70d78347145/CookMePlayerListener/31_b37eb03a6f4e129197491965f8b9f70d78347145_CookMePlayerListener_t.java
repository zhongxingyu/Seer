 package de.dustplanet.cookme;
 
 import java.sql.Timestamp;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  * CookMePlayerListener
  * Handles the players activities!
  *
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin for his awesome code snippet!
  *
  */
 
 public class CookMePlayerListener implements Listener {
 	private CookMe plugin;
 	private boolean message = true;
 	public CookMePlayerListener(CookMe instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		String effect;
 		Player player = event.getPlayer();
 		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (!event.hasItem()) return;
 		// Check if player is affected
 		if (!player.hasPermission("cookme.safe")) {
 			// Check for item & right clicking
			if (sameItem(player.getItemInHand().getTypeId()) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
 				// No block like bed, chest, etc...
 				if (plugin.noBlocks) {
 					if (event.hasBlock()) {
 						if (event.getClickedBlock().getType() == Material.BED
 								|| event.getClickedBlock().getType() == Material.BED_BLOCK
 								|| event.getClickedBlock().getType() == Material.FENCE_GATE
 								|| event.getClickedBlock().getType() == Material.FURNACE
 								|| event.getClickedBlock().getType() == Material.BURNING_FURNACE
 								|| event.getClickedBlock().getType() == Material.CHEST
 								|| event.getClickedBlock().getType() == Material.WORKBENCH
 								|| event.getClickedBlock().getType() == Material.IRON_DOOR
 								|| event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK
 								|| event.getClickedBlock().getType() == Material.WOODEN_DOOR
 								|| event.getClickedBlock().getType()== Material.WOOD_DOOR)
 							return;
 					}
 				}
 				// If the player is in cooldown phase cancel it
				if (!plugin.cooldownManager.hasCooldown(player, now)) {
 					// Check for food level
 					if (player.getFoodLevel() != 20) {
 						// Make a temp double and a value between 0 and 99
 						double random = Math.random() * 100, temp = 0;
 						int i = 0;
 						// Get the number for the effect
 						for(i = 0; i < plugin.percentages.length; i++) {
 							temp += plugin.percentages[i];
 							if (random <= temp) break;
 						}
 						// EffectStrenght, Duration etc.
 						int randomEffectStrength = (int)(Math.random()*16);
 						int minimum = plugin.minDuration, maximum = plugin.maxDuration;
 						int randomEffectTime = (int)(Math.random() * ((maximum - minimum)  + 1)  + minimum);
 						// Player gets random damage, stack minus 1
 						if (i == 0) {
 							int randomDamage = (int) (Math.random()*9) +1;
 							effect = plugin.localization.getString("damage");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.damage(randomDamage);
 						}			
 						// Player dies, stack minus 1
 						if (i == 1) {
 							effect = plugin.localization.getString("death");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.setHealth(0);
 						}
 						// Random venom damage (including green hearts :) )
 						if (i == 2) {
 							effect = plugin.localization.getString("venom");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, randomEffectTime, randomEffectStrength));
 						}
 						// Food bar turns green (poison)
 						if (i == 3) {
 							effect = plugin.localization.getString("hungervenom");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, randomEffectTime, randomEffectStrength));
 						}
 						// Sets the food level down. Stack minus 1
 						if (i == 4) {
 							int currentFoodLevel = player.getFoodLevel(), randomFoodLevel = (int)(Math.random()*currentFoodLevel);
 							effect = plugin.localization.getString("hungerdecrease");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.setFoodLevel(randomFoodLevel);
 						}
 						// Confusion
 						if (i == 5) {
 							effect = plugin.localization.getString("confusion");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, randomEffectTime, randomEffectStrength));
 						}
 						// Blindness
 						if (i == 6) {
 							effect = plugin.localization.getString("blindness");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, randomEffectTime, randomEffectStrength));
 						}
 
 						// Weakness
 						if (i == 7) {
 							effect = plugin.localization.getString("weakness");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, randomEffectTime, randomEffectStrength));
 
 						}
 						// Slowness
 						if (i == 8) {
 							effect = plugin.localization.getString("slowness");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, randomEffectTime, randomEffectStrength));
 
 						}
 						// Slowness for blocks
 						if (i == 9) {
 							effect = plugin.localization.getString("slowness_blocks");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, randomEffectTime, randomEffectStrength));
 
 						}
 						// Instant Damage
 						if (i == 10) {
 							effect = plugin.localization.getString("instant_damage");
 							message(player, effect);
 							decreaseItem(player, event);
 							player.addPotionEffect(new PotionEffect(PotionEffectType.HARM, randomEffectTime, randomEffectStrength));
 
 						}
 						// Refusing
 						if (i == 11) {
 							effect = plugin.localization.getString("refusing");
 							message(player, effect);
 							event.setCancelled(true);
 						}
 
 						// Add player to cooldown list
						if (plugin.cooldown != 0) plugin.cooldownManager.addPlayer(player);
 					}
 				}
 			}
 		}
 	}
 
 	private void message(Player player, String message) {
 		if (plugin.messages) {
 			plugin.message(null, player, message, null, null);
 		}
 	}
 
 	// Is the item in the list? Yes or no
 	private boolean sameItem(int item) {
 		for (String itemName : plugin.itemList) {
 			try {
 				Material material = Material.valueOf(itemName);
 				if (material.getId() == item) return true;
 			}
 			catch (IllegalArgumentException e1) {
 				try {
 					int id = Integer.valueOf(itemName);
 					if (id == item) return true;
 				}
 				catch (NumberFormatException e2) {
 					// Prevent spamming
 					if (message) {
 						plugin.log.warning("[CookMe] couldn't load the foods! Please check your config!");
 						plugin.log.warning("[CookMe] The following item id/name is invalid: " + itemName);
 						message = false;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	// Sets the raw food -1
 	@SuppressWarnings("deprecation")
 	private void decreaseItem (Player player, PlayerInteractEvent event) {
 		ItemStack afterEating = player.getItemInHand();
 		if (afterEating.getAmount() == 1) player.setItemInHand(null);
 		else {
 			afterEating.setAmount(afterEating.getAmount() -1);
 			player.setItemInHand(afterEating);
 		}
 		player.updateInventory();
 		event.setCancelled(true);
 	}
 }
