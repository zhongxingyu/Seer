 package de.dustplanet.superwheat;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * SuperWheat for CraftBukkit/Bukkit
  * Handles block activities!
  * Refer to the forum thread:
  * http://bit.ly/superwheatthread
  * Refer to the dev.bukkit.org page: http://bit.ly/superwheatpage
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to thescreem for the original SuperWheat plugin!
  */
 
 public class SuperWheatBlockListener implements Listener {
 
 	public SuperWheat plugin;
 	public SuperWheatBlockListener(SuperWheat instance){
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event)  {
 		// Physical means jump on it
 		if (event.getAction() == Action.PHYSICAL) {
 			Block block = event.getClickedBlock();
 			if (block == null) return;
 			// If the block is farmland (soil)
 			if (block.getType() == Material.SOIL && !plugin.wheatTrampling) {
 				// Deny event and set the block
 				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
 				event.setCancelled(true);
 				block.setTypeIdAndData(block.getType().getId(), block.getData(), true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockFromTo(BlockFromToEvent event) {
 		// If wart flows "over" it
 		if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER) {
 			// If the block is a wheat
 			if (event.getToBlock().getType() == Material.CROPS && plugin.wheatEnabled) {
 				final Block block = event.getToBlock();
 				// If water flows "over" it
 				// Fully grown
 				if ((byte) block.getData() == 7) {
 					// Should we cancel this?
 					if (plugin.wheatPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						block.setTypeId(0);
 						if (plugin.wheatWaterDropSeeds) dropSeeds(block);
 						if (plugin.wheatWaterDropWheat) dropWheat(block);
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 							public void run() {
 								block.setTypeIdAndData(Material.CROPS.getId(), (byte) 0, true);
 							}
 						}, (20 * plugin.wheatDelayWater));
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.wheatPreventWater) event.setCancelled(true);
 			}
 			else if (event.getToBlock().getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 				final Block block = event.getToBlock();
 				// Fully grown
 				if ((byte) block.getData() == 3) {
 					// Should we cancel this?
 					if (plugin.netherWartPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						block.setTypeId(0);
 						if (plugin.netherWartWaterDropNetherWart) dropNetherWart(block);
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 							public void run() {
 								block.setTypeIdAndData(Material.NETHER_WARTS.getId(), (byte) 0, true);
 							}
 						}, (20 * plugin.netherWartDelayWater));
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.netherWartPreventWater) event.setCancelled(true);
 			}
 			else if (event.getToBlock().getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 				final Block block = event.getToBlock();
 				// If water flows "over" it
 				// Fully grown
 				if ((byte) block.getData() >= 8) {
 					// Should we cancel this?
 					if (plugin.cocoaPlantPreventWaterGrown) event.setCancelled(true);
 					else {
 						final byte data = (byte) (block.getData() - 8);
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						block.setTypeId(0);
 						if (plugin.cocoaPlantWaterDropCocoaPlant) dropCocoaBeans(block);
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 							public void run() {
 								block.setTypeIdAndData(Material.COCOA.getId(), data, true);
 							}
 						}, (20 * plugin.cocoaPlantDelayWater));
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.cocoaPlantPreventWater) event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockPistonExtend (BlockPistonExtendEvent event) {
 		// Get the block the piston pushed
 		final Block block = event.getBlock().getRelative(event.getDirection());
 		// If the block is a wheat block
 		if (block.getType() == Material.CROPS && plugin.wheatEnabled) {
 			// Mature
 			if ((byte) block.getData() == 7) {
 				// Should we cancel this?
 				if (plugin.wheatPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					block.setTypeId(0);
 					if (plugin.wheatPistonDropSeeds) dropSeeds(block);
 					if (plugin.wheatPistonDropWheat) dropWheat(block);
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 						public void run() {
 							// First -> Farmland (soil)
 							block.getRelative(BlockFace.DOWN).setType(Material.SOIL);
 							block.setTypeIdAndData(Material.CROPS.getId(), (byte) 0, true);
 						}
 					}, (20 * plugin.wheatDelayPiston));
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.wheatPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 			// Mature
 			if ((byte) block.getData() == 3) {
 				// Should we cancel this?
 				if (plugin.netherWartPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					block.setTypeId(0);
 					if (plugin.netherWartPistonDropNetherWart) dropNetherWart(block);
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 						public void run() {
 							block.setTypeIdAndData(Material.NETHER_WARTS.getId(), (byte) 0, true);
 						}
 					}, (20 * plugin.netherWartDelayPiston));
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.netherWartPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 			// Mature
 			if ((byte) block.getData() >= 8) {
 				// Should we cancel this?
 				if (plugin.cocoaPlantPreventPistonGrown) event.setCancelled(true);
 				else {
 					final byte data = (byte) (block.getData() - 8);
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					block.setTypeId(0);
 					if (plugin.cocoaPlantPistonDropCocoaPlant) dropCocoaBeans(block);
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 						public void run() {
 							block.setTypeIdAndData(Material.COCOA.getId(), data, true);
 						}
 					}, (20 * plugin.cocoaPlantDelayPiston));
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.cocoaPlantPreventPiston) event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		final Block block = event.getBlock();
 		// If that block is a crop (Block ID #59)...
 		if (block.getType() == Material.CROPS && plugin.wheatEnabled) {
 			Player player = event.getPlayer();
 			// If the data for the crop isn't 7 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if((byte) block.getData() != 7) {
 				if (!player.hasPermission("SuperWheat.wheat.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					player.sendMessage(plugin.config.getString("message"));
 				}
 			}
 			// Else, if the data for the block IS 7 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if((byte) block.getData() == 7 && player.hasPermission("SuperWheat.wheat.regrowing")) {
 				event.setCancelled(true);
 				block.setTypeId(0);
 				// Set the block to a data value of 0, which is what the crop looks
 				// like right when you just plant it. With a light delay...
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					public void run() {
 						block.setTypeIdAndData(59, (byte) 0, true);
 					}
 				}, (20 * plugin.wheatDelayHit));
 				// Drop wheat from the crop. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropWheat(block);
 				// Drop seeds, too (0-3)
 				if (player.hasPermission("SuperWheat.wheat.seed")) {
 					if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropSeeds(block);
 				}
 			}
 		}
 		else if (block.getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 			Player player = event.getPlayer();
 			// If the data for the crop isn't 3 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if ((byte) block.getData() != 3) {
 				if (!player.hasPermission("SuperWheat.netherwart.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					player.sendMessage(plugin.config.getString("message"));
 				}
 			}
 			// Else, if the data for the block IS 3 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if ((byte) block.getData() == 3 && player.hasPermission("SuperWheat.netherwart.regrowing")) {
 				event.setCancelled(true);
 				block.setTypeId(0);
 				// Set the block to a data value of 0, which is what the wart looks
 				// like right when you just plant it. With a light delay...
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					public void run() {
 						block.setTypeIdAndData(Material.NETHER_WARTS.getId(), (byte) 0, true);
 					}
 				}, (20 * plugin.netherWartDelayHit));
 				// Drop wheat from the crop. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropNetherWart(block);
 			}
 		}
 		else if (block.getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 			Player player = event.getPlayer();
 			// If the data for the crop isn't at least 8 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if ((byte) block.getData() < 8) {
 				if (!player.hasPermission("SuperWheat.cocoaplant.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					player.sendMessage(plugin.config.getString("message"));
 				}
 			}
 			// Else, if the data for the block IS 8 or higher (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if ((byte) block.getData() >= 8 && player.hasPermission("SuperWheat.cocoaplant.regrowing")) {
 				event.setCancelled(true);
 				final byte data = (byte) (block.getData() - 8);
 				block.setTypeId(0);
 				// Set the block to a data value of 0, which is what the plant looks
 				// like right when you just place it. With a light delay...
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					public void run() {
 						block.setTypeIdAndData(Material.COCOA.getId(), data, true);
 					}
 				}, (20 * plugin.cocoaPlantDelayHit));
 				// Drop wheat from the crop. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropCocoaBeans(block);
 			}
 		}
 	}
 
 	// Drops wheat
 	private void dropWheat(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT, (int) (Math.random() * 3) + 1));
 	}
 
 	// Drops seeds
 	private void dropSeeds(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SEEDS, (int) (Math.random() * 4)));
 	}
 
 	// Drops netherWart
 	private void dropNetherWart(Block block) {
		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_STALK, (int) (Math.random() * 4) + 2));
 	}
 
 	// Drops cocoa beans
 	private void dropCocoaBeans(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.INK_SACK, 3, (short) 3));
 	}
 }
