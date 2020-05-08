 package de.xghostkillerx.glowstonedrop;
 
 import java.util.Arrays;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.entity.Player;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit Handles the block stuff!
  * Refer to the forum thread:
  * http://bit.ly/oW6iR1
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/rcN2QB
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to XxFuNxX for the original GlowstoneDrop plugin!
  */
 
 public class GlowstoneDropBlockListener implements Listener {
 	public GlowstoneDrop plugin;
 	public GlowstoneDropBlockListener(GlowstoneDrop instance) {
 		plugin = instance;
 	}
 	private boolean message = true;
 	private String[] worlds = {"normal", "nether", "end"};
 
 	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		String enviroment = event.getBlock().getWorld().getEnvironment().toString().toLowerCase();
 		// Check for the item in hand and if it's on the list
 		if (sameItem(player.getItemInHand().getTypeId()) == true
 				&& (event.getBlock().getTypeId() == 89)) {
 			// Check for the config value permissions
 			if (plugin.config.getBoolean("configuration.permissions") == true) {
 				if (Arrays.asList(worlds).contains(enviroment)) {
 					// Block
 					if (plugin.config.getString("worlds." + enviroment).equalsIgnoreCase("block")) {
 						if (player.hasPermission("glowstonedrop.use." + enviroment)) {
							dropBlock(event);
 						} else {
 							message(player);
 						}
 					}
 				}
 			}
 			// Without permissions -> no messages
 			else if (plugin.config.getBoolean("configuration.permissions") == false) {
 				if (Arrays.asList(worlds).contains(enviroment)) {
 					// Block
 					if (plugin.config.getString("worlds." + enviroment).equalsIgnoreCase("block")) {
						dropBlock(event);
 					}
 				}
 			}
 		}
 	}
 
 	// Drop the block
 	private void dropBlock(BlockBreakEvent event) {
 		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(89, 1));
 		event.getBlock().setType(Material.AIR);
 	}
 
 	// Sends a message
 	private void message(Player player) {
 		if (plugin.config.getBoolean("configuration.messages") == true) {
 			String msg = plugin.localization.getString("permission_denied");
 			plugin.message(null, player, msg, null, null);
 		}
 	}
 
 	// Is the item in the hand on the list?
 	private boolean sameItem(int item) {
 		for (int i = 0; i < plugin.itemList.size(); i++) {
 			String itemName = plugin.itemList.get(i);
 			try {
 				Material material = Material.valueOf(itemName);
 				if (material.getId() == item) {
 					return true;
 				}
 			}
 			catch (Exception e) {
 				// Prevent spamming
 				if (message == true) {
 					plugin.log.warning("GlowstoneDrop couldn't load the items! Please check your config!");
 					message = false;
 				}
 			}
 		}
 		return false;
 	}
 }
