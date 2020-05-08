 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Jukebox;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.entity.ThrownExpBottle;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.painting.PaintingBreakEvent;
 import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import com.turt2live.antishare.money.Tender.TenderType;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.notification.MessageFactory;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.storage.LevelSaver;
 import com.turt2live.antishare.storage.LevelSaver.Level;
 import com.turt2live.antishare.storage.PerWorldConfig;
 import com.turt2live.antishare.storage.PerWorldConfig.ListType;
 
 /**
  * The core listener - Listens to all events needed by AntiShare and handles them
  * 
  * @author turt2live
  */
 public class ASListener implements Listener {
 
 	private AntiShare plugin = AntiShare.getInstance();
 	private HashMap<World, PerWorldConfig> config = new HashMap<World, PerWorldConfig>();
 	private boolean hasMobCatcher = false;
 	private HashMap<String, Long> GMCD = new HashMap<String, Long>();
 
 	/**
 	 * Creates a new Listener
 	 */
 	public ASListener(){
 		reload();
 	}
 
 	/**
 	 * Reloads lists
 	 */
 	public void reload(){
 		config.clear();
 		for(World world : Bukkit.getWorlds()){
 			config.put(world, new PerWorldConfig(world));
 		}
 		hasMobCatcher = plugin.getServer().getPluginManager().getPlugin("MobCatcher") != null;
 	}
 
 	/**
 	 * Gets the configuration for the world
 	 * 
 	 * @param world the world
 	 * @return the configuration
 	 */
 	public PerWorldConfig getConfig(World world){
 		return config.get(world);
 	}
 
 	// ################# World Load
 
 	@EventHandler
 	public void onWorldLoad(WorldLoadEvent event){
 		World world = event.getWorld();
 		config.put(world, new PerWorldConfig(world));
 	}
 
 	// ################# World Unload
 
 	@EventHandler
 	public void onWorldUnload(WorldUnloadEvent event){
 		if(event.isCancelled())
 			return;
 		World world = event.getWorld();
 		config.remove(world);
 	}
 
 	// ################# Block Break
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean special = false;
 		boolean region = false;
 		Boolean drops = null;
 		boolean deny = false;
 		AlertType specialType = AlertType.LEGAL;
 		String blockGM = "Unknown";
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, block.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(block.getWorld()).isBlocked(block, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Check hooks
 		if(plugin.getHookManager().checkSourceBlockForProtection(block)){
 			return; // Don't handle any further, let the other plugin handle it
 		}
 
 		// Check creative/survival blocks
 		if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 			GameMode blockGamemode = plugin.getBlockManager().getType(block);
 			if(blockGamemode != null){
 				blockGM = blockGamemode.name().toLowerCase();
 				String oGM = player.getGameMode().name().toLowerCase();
 				if(player.getGameMode() != blockGamemode){
 					special = true;
 					deny = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.deny");
 					drops = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.block-drops");
 					if(deny){
 						specialType = AlertType.ILLEGAL;
 					}
 				}
 			}
 		}
 
 		// Check regions
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_BREAK)){
 			ASRegion playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			ASRegion blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				special = true;
 				region = true;
 				specialType = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL || specialType == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}else{
 			plugin.getBlockManager().removeBlock(block);
 		}
 
 		// Alert
 		if(special){
 			if(region){
 				if(specialType == AlertType.ILLEGAL){
 					String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break " : " broke  ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 					String specialPlayerMessage = ChatColor.RED + "You cannot break blocks that are not in your region";
 					plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, AlertTrigger.BLOCK_BREAK);
 				}
 			}else{
 				String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break the " + blockGM + " block " : " broke the " + blockGM + " block ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 				String specialPlayerMessage = plugin.getMessage("blocked-action." + blockGM + "-block-break");
 				MessageFactory factory = new MessageFactory(specialPlayerMessage);
 				factory.insert(block, player, block.getWorld(), blockGM.equalsIgnoreCase("creative") ? TenderType.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? TenderType.SURVIVAL_BLOCK : TenderType.ADVENTURE_BLOCK));
 				specialPlayerMessage = factory.toString();
 				plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, (blockGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? AlertTrigger.SURVIVAL_BLOCK : AlertTrigger.ADVENTURE_BLOCK)));
 			}
 		}else{
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break " : " broke ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			String playerMessage = plugin.getMessage("blocked-action.break-block");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(block, player, block.getWorld(), TenderType.BLOCK_BREAK);
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_BREAK);
 		}
 
 		// Handle drops
 		if(drops != null && !deny && special){
 			if(drops){
 				plugin.getBlockManager().removeBlock(block);
 				block.breakNaturally();
 			}else{
 				plugin.getBlockManager().removeBlock(block);
 				block.setType(Material.AIR);
 			}
 		}
 
 		// Check for 'attached' blocks and internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING) && !event.isCancelled()){
 			// Check inventories
 			if(config.get(block.getWorld()).clearBlockInventoryOnBreak()){
 				if(block.getState() instanceof Chest){
 					Chest state = (Chest) block.getState();
 					state.getBlockInventory().clear();
 				}else if(block.getState() instanceof Jukebox){
 					Jukebox state = (Jukebox) block.getState();
 					state.setPlaying(null);
 				}else if(block.getState() instanceof Furnace){
 					Furnace state = (Furnace) block.getState();
 					state.getInventory().clear();
 				}else if(block.getState() instanceof BrewingStand){
 					BrewingStand state = (BrewingStand) block.getState();
 					state.getInventory().clear();
 				}
 			}
 
 			// Check for attached blocks
 			if(config.get(block.getWorld()).removeAttachedBlocksOnBreak()){
 				for(BlockFace face : BlockFace.values()){
 					Block rel = block.getRelative(face);
 					if(ASUtils.isDroppedOnBreak(rel, block)){
 						plugin.getBlockManager().removeBlock(rel);
 						rel.setType(Material.AIR);
 					}
 				}
 
 				// Check for falling sand/gravel exploit
 				boolean moreBlocks = true;
 				Block active = block;
 				if(block.getType() == Material.SAND || block.getType() == Material.GRAVEL ||
 						block.getRelative(BlockFace.UP).getType() == Material.SAND || block.getRelative(BlockFace.UP).getType() == Material.GRAVEL){
 					do{
 						Block below = active.getRelative(BlockFace.DOWN);
 						active = below;
 						if(below.getType() == Material.AIR){
 							continue;
 						}
 						if(ASUtils.canBreakFallingBlock(below.getType())){
 							// Remove all sand/gravel above this block
 							boolean checkMoreBlocks = true;
 							Block above = block.getRelative(BlockFace.UP);
 							do{
 								if(above.getType() == Material.SAND || above.getType() == Material.GRAVEL){
 									above.setType(Material.AIR);
 									above = above.getRelative(BlockFace.UP);
 								}else{
 									checkMoreBlocks = false;
 								}
 							}while (checkMoreBlocks);
 							moreBlocks = false;
 						}else{
 							moreBlocks = false;
 						}
 					}while (moreBlocks);
 				}
 
 				/* We need to check the blocks above for falling blocks, as the following can happen:
 				 * [SAND][TORCH]
 				 * [SAND]
 				 * [DIRT][DIRT]
 				 * 
 				 * Break the bottom SAND block and the torch falls
 				 */
 				do{
 					Block above = active.getRelative(BlockFace.UP);
 					if(ASUtils.isAffectedByGravity(above.getType())){
 						for(BlockFace face : BlockFace.values()){
 							Block rel = above.getRelative(face);
 							if(ASUtils.isDroppedOnBreak(rel, above)){
 								rel.setType(Material.AIR);
 								plugin.getBlockManager().removeBlock(rel);
 							}
 						}
 					}else{
 						moreBlocks = false;
 					}
 					active = above;
 				}while (moreBlocks);
 
 				// Cacti check
 				active = block;
 				if(block.getType() == Material.CACTUS){
 					moreBlocks = true;
 					List<Location> breakBlocks = new ArrayList<Location>();
 					do{
 						Block above = active.getRelative(BlockFace.UP);
 						if(above.getType() == Material.CACTUS){
 							plugin.getBlockManager().removeBlock(above);
 							breakBlocks.add(above.getLocation());
 						}else{
 							moreBlocks = false;
 						}
 						active = above;
 					}while (moreBlocks);
 					for(int i = breakBlocks.size() - 1; i > -1; i--){
 						Location location = breakBlocks.get(i);
 						location.getBlock().setType(Material.AIR);
 					}
 				}
 
 				// Reed (Sugar Cane) check
 				active = block;
 				if(block.getType() == Material.SUGAR_CANE_BLOCK){
 					moreBlocks = true;
 					List<Location> breakBlocks = new ArrayList<Location>();
 					do{
 						Block above = active.getRelative(BlockFace.UP);
 						if(above.getType() == Material.SUGAR_CANE_BLOCK){
 							plugin.getBlockManager().removeBlock(above);
 							breakBlocks.add(above.getLocation());
 						}else{
 							moreBlocks = false;
 						}
 						active = above;
 					}while (moreBlocks);
 					for(int i = breakBlocks.size() - 1; i > -1; i--){
 						Location location = breakBlocks.get(i);
 						location.getBlock().setType(Material.AIR);
 					}
 				}
 			}
 		}
 	}
 
 	// ################# Block Place
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockPlace(BlockPlaceEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Sanity check
 		if(block.getType() == Material.AIR){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_PLACE, block.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_PLACE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(block.getWorld()).isBlocked(block, ListType.BLOCK_PLACE)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PLACE)){
 			ASRegion playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			ASRegion blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}else{
 			// Handle block place for tracker
 			if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 				plugin.getBlockManager().addBlock(player.getGameMode(), block);
 			}
 		}
 
 		// Alert
 		if(region){
 			if(type == AlertType.ILLEGAL){
 				String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 				String playerMessage = ChatColor.RED + "You cannot place blocks in another region!";
 				plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 			}
 		}else{
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			String playerMessage = plugin.getMessage("blocked-action.place-block");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(block, player, block.getWorld(), TenderType.BLOCK_PLACE);
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 		}
 	}
 
 	// ################# Player Interact Block
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onInteract(PlayerInteractEvent event){
 		if(event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_AIR)
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		Action action = event.getAction();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.RIGHT_CLICK;
 
 		// Check for AntiShare tool
 		if(plugin.getPermissions().has(player, PermissionNodes.TOOL_USE) && player.getItemInHand() != null
 				&& (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)){
 			if(player.getItemInHand().getType() == AntiShare.ANTISHARE_TOOL){
 				String blockname = block.getType().name().replaceAll("_", " ").toLowerCase();
 				String gamemode = (plugin.getBlockManager().getType(block) != null ? plugin.getBlockManager().getType(block).name() : "natural").toLowerCase();
 				ASUtils.sendToPlayer(player, "That " + ChatColor.YELLOW + blockname + ChatColor.WHITE + " is a " + ChatColor.YELLOW + gamemode + ChatColor.WHITE + " block.", true);
 
 				// Cancel and stop the check
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		// For use from here on in
 		if(block == null){
 			block = player.getWorld().getBlockAt(player.getLocation());
 		}
 
 		// Right click list
 		if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
 			// Check if they should be blocked
 			ASRegion asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(block, ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(config.get(block.getWorld()).isBlocked(block, ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, block.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.right-click");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(block, player, block.getWorld(), TenderType.RIGHT_CLICK);
 			playerMessage = factory.toString();
 		}
 
 		// If this event is triggered as legal from the right click, check use lists
 		if(type == AlertType.LEGAL){
 			ASRegion asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(block, ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(config.get(block.getWorld()).isBlocked(block, ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			// Check if they should be blocked
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, block.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.right-click");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(block, player, block.getWorld(), TenderType.RIGHT_CLICK);
 			playerMessage = factory.toString();
 		}
 
 		// If the event is triggered as legal from the use lists, check the player's item in hand
 		if(type == AlertType.LEGAL && action == Action.RIGHT_CLICK_BLOCK && player.getItemInHand() != null){
 			// Check if they should be blocked
 			ASRegion asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(player.getItemInHand().getType(), ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(config.get(block.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + player.getItemInHand().getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.use-item");
 			trigger = AlertTrigger.USE_ITEM;
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(block, player, block.getWorld(), TenderType.USE);
 			playerMessage = factory.toString();
 		}
 
 		// Check for eye of ender / ender pearl
 		if((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
 				&& player.getItemInHand() != null
 				&& (player.getItemInHand().getType() == Material.EYE_OF_ENDER
 				|| player.getItemInHand().getType() == Material.ENDER_PEARL)){
 			boolean potion = false;
 			ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(player.getItemInHand().getType(), ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 					potion = true;
 				}
 			}else{
 				if(!config.get(player.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 					potion = true;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 			if(type == AlertType.ILLEGAL && potion){
 				message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + player.getItemInHand().getType().name().replace("_", " ");
 				playerMessage = plugin.getMessage("blocked-action.use-item");
 				trigger = AlertTrigger.USE_ITEM;
 				MessageFactory factory = new MessageFactory(playerMessage);
 				factory.insert(block, player, block.getWorld(), TenderType.USE);
 				playerMessage = factory.toString();
 			}
 		}
 
 		// Check for potion
 		if((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
 				&& player.getItemInHand() != null
 				&& player.getItemInHand().getType() == Material.POTION){
 			boolean potion = false;
 			ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(player.getItemInHand().getDurability() > 32000){
 				if(region != null){
 					if(!region.getConfig().isThrownPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}else{
 					if(!config.get(player.getWorld()).isThrownPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}
 			}else{
 				if(region != null){
 					if(!region.getConfig().isPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}else{
 					if(!config.get(player.getWorld()).isPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 			if(type == AlertType.ILLEGAL && potion){
 				message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + player.getItemInHand().getType().name().replace("_", " ");
 				playerMessage = plugin.getMessage("blocked-action.use-item");
 				trigger = AlertTrigger.USE_ITEM;
 				MessageFactory factory = new MessageFactory(playerMessage);
 				factory.insert(block, player, block.getWorld(), TenderType.USE);
 				playerMessage = factory.toString();
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 			if(hasMobCatcher && player.getItemInHand() != null){
 				ItemStack item = player.getItemInHand();
 				if(item.getType() == Material.EGG || item.getType() == Material.MONSTER_EGG){
 					item.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
 				}
 			}
 		}
 	}
 
 	// ################# Player Interact Entity
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onInteractEntity(PlayerInteractEntityEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Convert entity -> item ID
 		Material item = Material.AIR;
 		if(event.getRightClicked() instanceof StorageMinecart){
 			item = Material.STORAGE_MINECART;
 		}else if(event.getRightClicked() instanceof PoweredMinecart){
 			item = Material.POWERED_MINECART;
 		}else if(event.getRightClicked() instanceof Boat){
 			item = Material.BOAT;
 		}else if(event.getRightClicked() instanceof Minecart){
 			item = Material.MINECART;
 		}else if(event.getRightClicked() instanceof Painting){
 			item = Material.PAINTING;
 		}else if(event.getRightClicked() instanceof Sheep){
 			item = Material.SHEARS;
 		}
 
 		// If the entity is not found, check for interacted entities
 		if(item == Material.AIR){
 			ASRegion region = plugin.getRegionManager().getRegion(event.getRightClicked().getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				if(!config.get(player.getWorld()).isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMBAT_MOBS, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Handle event
 			if(type == AlertType.ILLEGAL){
 				event.setCancelled(true);
 			}
 
 			// Alert (with sanity check)
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 			String playerMessage = plugin.getMessage("blocked-action.right-click");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(null, player, player.getWorld(), TenderType.RIGHT_CLICK, ASUtils.capitalize(item.name()));
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 			return; // Nothing was found in the right click check (item), so stop here
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(event.getRightClicked().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + "a mob";
 		String playerMessage = plugin.getMessage("blocked-action.right-click");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.RIGHT_CLICK, "a mob");
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 	}
 
 	// ################# Cart Death Check
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onCartDeath(VehicleDestroyEvent event){
 		if(event.isCancelled())
 			return;
 		Entity attacker = event.getAttacker();
 		Vehicle potentialCart = event.getVehicle();
 
 		// Sanity checks
 		if(attacker == null || !(potentialCart instanceof StorageMinecart)){
 			return;
 		}
 		if(!(attacker instanceof Player)){
 			return;
 		}
 
 		// Setup
 		Player player = (Player) attacker;
 		StorageMinecart cart = (StorageMinecart) potentialCart;
 
 		// Check internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING)){
 			// Check inventories
 			ASRegion asregion = plugin.getRegionManager().getRegion(cart.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().clearBlockInventoryOnBreak()){
 					cart.getInventory().clear();
 				}
 			}else{
 				if(config.get(player.getWorld()).clearBlockInventoryOnBreak()){
 					cart.getInventory().clear();
 				}
 			}
 		}
 	}
 
 	// ################# Egg Check
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEggThrow(PlayerEggThrowEvent event){
 		Player player = event.getPlayer();
 		AlertType type = AlertType.ILLEGAL;
 		Material item = Material.EGG;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(event.getEgg().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setHatching(false);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 		String playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 	}
 
 	// ################# Experience Bottle Check
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onExpBottle(ExpBottleEvent event){
 		if(event.getExperience() == 0)
 			return;
 
 		ThrownExpBottle bottle = event.getEntity();
 		LivingEntity shooter = bottle.getShooter();
 		AlertType type = AlertType.ILLEGAL;
 		Material item = Material.EXP_BOTTLE;
 
 		// Sanity Check
 		if(!(shooter instanceof Player)){
 			return;
 		}
 
 		// Setup
 		Player player = (Player) shooter;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(bottle.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setExperience(0);
 			event.setShowEffect(false);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 		String playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		if(type == AlertType.ILLEGAL){ // We don't want to show legal events because of spam
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 		}
 	}
 
 	// ################# Drop Item
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onDrop(PlayerDropItemEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		Item item = event.getItemDrop();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DROP, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.DROP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(itemStack.getType(), ListType.DROP)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Region Check
 		if(plugin.getRegionManager().getRegion(player.getLocation()) != plugin.getRegionManager().getRegion(item.getLocation()) && type == AlertType.LEGAL){
 			if(!plugin.getPermissions().has(player, PermissionNodes.REGION_THROW)){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name());
 		String playerMessage = plugin.getMessage("blocked-action.drop-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name()) + ChatColor.WHITE + " into a region.";
 			playerMessage = ChatColor.RED + "You cannot throw items into another region!";
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.ITEM_DROP, ASUtils.capitalize(itemStack.getType().name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_DROP);
 	}
 
 	// ################# Pickup Item
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPickup(PlayerPickupItemEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		Item item = event.getItem();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_PICKUP, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.PICKUP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(itemStack.getType(), ListType.PICKUP)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Region Check
 		if(plugin.getRegionManager().getRegion(player.getLocation()) != plugin.getRegionManager().getRegion(item.getLocation()) && type == AlertType.LEGAL){
 			if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PICKUP)){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name());
 		String playerMessage = plugin.getMessage("blocked-action.pickup-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name()) + ChatColor.WHITE + " from a region.";
 			playerMessage = ChatColor.RED + "You cannot pickup items from another region!";
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.ITEM_PICKUP, ASUtils.capitalize(itemStack.getType().name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_PICKUP);
 	}
 
 	// ################# Player Death
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		List<ItemStack> drops = event.getDrops();
 		AlertType type = AlertType.ILLEGAL;
 		int illegalItems = 0;
 
 		// Remove them from a region (if applicable)
 		ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			List<ItemStack> remove = new ArrayList<ItemStack>();
 			ASRegion asregion = plugin.getRegionManager().getRegion(player.getLocation());
 			for(ItemStack item : drops){
 				if(asregion != null){
 					if(asregion.getConfig().isBlocked(item.getType(), ListType.DEATH)){
 						illegalItems++;
 						remove.add(item);
 					}
 				}else{
 					if(config.get(player.getWorld()).isBlocked(item.getType(), ListType.DEATH)){
 						illegalItems++;
 						remove.add(item);
 					}
 				}
 			}
 			// Remove items
 			for(ItemStack item : remove){
 				drops.remove(item);
 			}
 		}
 
 		// Determine new status
 		if(illegalItems == 0){
 			type = AlertType.LEGAL;
 		}
 
 		// Alert
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " died with " + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + illegalItems + " illegal item(s).";
 		String playerMessage = plugin.getMessage("blocked-action.die-with-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.DEATH);
 		factory.insertAmount(illegalItems);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.PLAYER_DEATH);
 	}
 
 	// ################# Player Command
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		String command = event.getMessage().toLowerCase();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check if they should be blocked
		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMMANDS, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		ASRegion asregion = plugin.getRegionManager().getRegion(player.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(command, ListType.COMMAND)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(command, ListType.COMMAND)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use the command " : " used the command ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + command;
 		String playerMessage = plugin.getMessage("blocked-action.command");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.COMMAND);
 		factory.insertCommand(command);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.COMMAND, !(event.getMessage().toLowerCase().startsWith("/as money")));
 	}
 
 	// ################# Player Move
 
 	@EventHandler
 	public void onMove(PlayerMoveEvent event){
 		if(event.isCancelled())
 			return;
 
 		// Significant move check
 		if(event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ())
 			return;
 
 		Player player = event.getPlayer();
 		ASRegion currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		ASRegion toRegion = plugin.getRegionManager().getRegion(event.getTo());
 
 		// Check split
 		if(config.get(player.getWorld()).isSplitActive()){
 			config.get(player.getWorld()).warnSplit(player);
 			config.get(player.getWorld()).checkSplit(player);
 		}
 
 		if(currentRegion == null){
 			// Determine alert for World Split
 			config.get(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			config.get(player.getWorld()).checkSplit(player);
 		}
 
 		// Check regions
 		if(currentRegion != toRegion){
 			if(currentRegion != null){
 				currentRegion.alertExit(player);
 			}
 			if(toRegion != null){
 				toRegion.alertEntry(player);
 			}
 		}
 	}
 
 	// ################# Player Game Mode Change
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		GameMode from = player.getGameMode();
 		GameMode to = event.getNewGameMode();
 		boolean ignore = true;
 		boolean checkRegion = true;
 
 		// Automatically close all open windows
 		InventoryView active = player.getOpenInventory();
 		if(active != null){
 			active.close();
 		}
 
 		// Implement cooldown if needed
 		if(plugin.getConfig().getBoolean("gamemode-change-cooldown.use") && !plugin.getPermissions().has(player, PermissionNodes.NO_GM_CD)){
 			long time = (long) Math.abs(plugin.getConfig().getDouble("gamemode-change-cooldown.time-in-seconds")) * 1000;
 			long now = System.currentTimeMillis();
 			if(time > 0){
 				if(GMCD.containsKey(player.getName())){
 					long lastUsed = GMCD.get(player.getName());
 					if(now - lastUsed > time){
 						// Allow
 						GMCD.put(player.getName(), now);
 					}else{
 						// Deny
 						event.setCancelled(true);
 						int seconds = (int) (time - (now - lastUsed)) / 1000;
 						String s = "";
 						if(seconds == 0 || seconds > 1){
 							s = "s";
 						}
 						ASUtils.sendToPlayer(player, ChatColor.RED + "You must wait at least " + seconds + " more second" + s + " before changing Game Modes.", true);
 						return;
 					}
 				}else{
 					GMCD.put(player.getName(), now);
 				}
 			}
 		}
 
 		// Change level if needed
 		Level currentLevel = new Level(player.getLevel(), player.getExp());
 		if(plugin.getConfig().getBoolean("enabled-features.change-level-on-gamemode-change")
 				&& !event.isCancelled()
 				&& !plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			Level desired = LevelSaver.getLevel(player.getName(), event.getNewGameMode());
 			LevelSaver.saveLevel(player.getName(), player.getGameMode(), currentLevel);
 			desired.setTo(player);
 		}
 
 		// Check to see if we should even bother
 		if(!plugin.getConfig().getBoolean("handled-actions.gamemode-inventories")){
 			return;
 		}
 
 		// Tag check
 		if(player.hasMetadata("antishare-regionleave")){
 			player.removeMetadata("antishare-regionleave", plugin);
 			checkRegion = false;
 		}
 
 		// Region Check
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_ROAM) && checkRegion){
 			ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				ASUtils.sendToPlayer(player, ChatColor.RED + "You are in a region and therefore cannot change Game Mode", true);
 				event.setCancelled(true);
 				currentLevel.setTo(player); // Restore level
 				return;
 			}
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (from){
 			case CREATIVE:
 				plugin.getInventoryManager().saveCreativeInventory(player, player.getWorld());
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, player.getWorld());
 				break;
 			case ADVENTURE:
 				plugin.getInventoryManager().saveAdventureInventory(player, player.getWorld());
 				break;
 			}
 
 			// Set to
 			switch (to){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, player.getWorld()).setTo(player);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, player.getWorld()).setTo(player);
 				break;
 			case ADVENTURE:
 				plugin.getInventoryManager().getAdventureInventory(player, player.getWorld()).setTo(player);
 				break;
 			}
 
 			// Ender Chest (saving and loading handled internally)
 			plugin.getInventoryManager().updateEnderChest(player, to, from);
 
 			// Check for open inventories and stuff
 			player.closeInventory();
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to Game Mode " + ChatColor.YELLOW + to.name();
 		String playerMessage = ignore ? "no message" : "Your inventory has been changed to " + ChatColor.YELLOW + to.name();
 		if(!plugin.getConfig().getBoolean("other.send-gamemode-change-message")){
 			playerMessage = "no message";
 		}
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 	// ################# Player Combat
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onCombat(EntityDamageByEntityEvent event){
 		if(event.isCancelled())
 			return;
 		DamageCause cause = event.getCause();
 		Entity attacker = event.getDamager();
 		Entity target = event.getEntity();
 		AlertType type = AlertType.ILLEGAL;
 		boolean playerCombat = false;
 		Player playerAttacker = null;
 
 		// Check case
 		switch (cause){
 		case ENTITY_ATTACK:
 			// attacker = entity
 			if(attacker instanceof Player){
 				playerAttacker = (Player) attacker;
 			}else{
 				return;
 			}
 			break;
 		case PROJECTILE:
 			// attacker = Projectile
 			Projectile projectile = (Projectile) attacker;
 			LivingEntity shooter = projectile.getShooter();
 			if(shooter instanceof Player){
 				playerAttacker = (Player) shooter;
 			}else{
 				return;
 			}
 			break;
 		default:
 			return;
 		}
 
 		// Determine if we are hitting a mob or not, and whether it is legal
 		if(target instanceof Player){
 			// target = Player
 			playerCombat = true;
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_PLAYERS, playerAttacker.getWorld())){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			// target = other entity
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_MOBS, playerAttacker.getWorld())){
 				type = AlertType.LEGAL;
 			}
 			ASRegion region = plugin.getRegionManager().getRegion(target.getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(target, ListType.MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				if(!config.get(target.getWorld()).isBlocked(target, ListType.MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}
 		}
 
 		// Check if we need to continue based on settings
 		ASRegion asregion = plugin.getRegionManager().getRegion(target.getLocation());
 		if(playerCombat){
 			if(asregion != null){
 				if(!asregion.getConfig().combatAgainstPlayers()){
 					return;
 				}
 			}else{
 				if(!config.get(target.getWorld()).combatAgainstPlayers()){
 					return;
 				}
 			}
 		}else{
 			if(asregion != null){
 				if(!asregion.getConfig().combatAgainstMobs()){
 					return;
 				}
 			}else{
 				if(!config.get(target.getWorld()).combatAgainstMobs()){
 					return;
 				}
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.HIT_MOB;
 		TenderType tender = TenderType.HIT_MOB;
 		String targetFactoryName;
 		if(playerCombat){
 			String playerName = ((Player) target).getName();
 			message = ChatColor.YELLOW + playerAttacker.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to hit " + ChatColor.RED : " hit " + ChatColor.GREEN) + playerName;
 			playerMessage = plugin.getMessage("blocked-action.hit-player");
 			trigger = AlertTrigger.HIT_PLAYER;
 			tender = TenderType.HIT_PLAYER;
 			targetFactoryName = playerName;
 		}else{
 			String targetName = target.getClass().getName().replace("Craft", "").replace("org.bukkit.craftbukkit.entity.", "").trim();
 			message = ChatColor.YELLOW + playerAttacker.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to hit a " + ChatColor.RED : " hit a " + ChatColor.GREEN) + targetName;
 			playerMessage = plugin.getMessage("blocked-action.hit-mob");
 			targetFactoryName = targetName;
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, playerAttacker, playerAttacker.getWorld(), tender);
 		if(tender == TenderType.HIT_MOB){
 			factory.insertHitMob(targetFactoryName);
 		}else{
 			factory.insertHitPlayer(targetFactoryName);
 		}
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, playerAttacker, playerMessage, type, trigger);
 	}
 
 	// ################# Entity Target
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityTarget(EntityTargetEvent event){
 		if(event.isCancelled())
 			return;
 		Entity target = event.getTarget();
 		Player playerTarget = null;
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check target
 		if(target instanceof Player){
 			playerTarget = (Player) target;
 		}else{
 			return;
 		}
 
 		// Check permissions
 		if(!plugin.isBlocked(playerTarget, PermissionNodes.ALLOW_COMBAT_MOBS, playerTarget.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 	}
 
 	// ################# Piston Move (Extend)
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPistonExtend(BlockPistonExtendEvent event){
 		if(event.isCancelled())
 			return;
 		for(Block block : event.getBlocks()){
 			// Check for block type
 			GameMode type = plugin.getBlockManager().getType(block);
 
 			// Sanity
 			if(type == null){
 				continue;
 			}
 
 			// Setup
 			Location oldLocation = block.getLocation();
 			Location newLocation = block.getRelative(event.getDirection()).getLocation();
 
 			// Move
 			plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 		}
 	}
 
 	// ################# Piston Move (Retract)
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPistonRetract(BlockPistonRetractEvent event){
 		if(event.isCancelled())
 			return;
 		if(!event.isSticky()){ // Only handle moving blocks
 			return;
 		}
 		Block block = event.getBlock().getRelative(event.getDirection()).getRelative(event.getDirection());
 
 		// Check for block type
 		GameMode type = plugin.getBlockManager().getType(block);
 
 		// Sanity
 		if(type == null){
 			return;
 		}
 
 		// Setup
 		Location oldLocation = block.getLocation();
 		Location newLocation = block.getRelative(event.getDirection().getOppositeFace()).getLocation();
 
 		// Move
 		plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 	}
 
 	// ################# Player Join
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		// Tell the inventory manager to prepare this player
 		plugin.getInventoryManager().loadPlayer(player);
 
 		// Check region
 		ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			// Add join key
 			player.setMetadata("antishare-regionleave", new FixedMetadataValue(plugin, true));
 
 			// Alert entry
 			region.alertSilentEntry(player); // Sets inventory and Game Mode
 			// This must be done because when the inventory manager releases
 			// a player it resets the inventory to "non-temp"
 		}
 
 		// Money (fines/rewards) status
 		plugin.getMoneyManager().showStatusOnLogin(player);
 	}
 
 	// ################# Player Quit
 
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 
 		// Remove from regions
 		ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().releasePlayer(player);
 	}
 
 	// ################# Player Kicked
 
 	@EventHandler
 	public void onKick(PlayerKickEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 
 		// Remove from regions
 		ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().releasePlayer(player);
 	}
 
 	// ################# Player World Change
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onWorldChange(PlayerChangedWorldEvent event){
 		Player player = event.getPlayer();
 		World to = player.getWorld();
 		World from = event.getFrom();
 		boolean ignore = true;
 
 		// Check to see if we should even bother checking
 		if(!plugin.getConfig().getBoolean("handled-actions.world-transfers")){
 			// Fix up inventories
 			plugin.getInventoryManager().fixInventory(player, event.getFrom());
 			return;
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		// Inventory check
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (player.getGameMode()){
 			case CREATIVE:
 				plugin.getInventoryManager().saveCreativeInventory(player, from);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, from);
 				break;
 			case ADVENTURE:
 				plugin.getInventoryManager().saveAdventureInventory(player, from);
 				break;
 			}
 
 			// Check for linked inventories
 			plugin.getInventoryManager().checkLinks(player, to, from);
 
 			// Set to
 			switch (player.getGameMode()){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, to).setTo(player);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, to).setTo(player);
 				break;
 			case ADVENTURE:
 				plugin.getInventoryManager().getAdventureInventory(player, to).setTo(player);
 				break;
 			}
 
 			// Ender Chest (saving and loading handled internally)
 			plugin.getInventoryManager().updateEnderChest(player, to, from);
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to world " + ChatColor.YELLOW + to.getName();
 		String playerMessage = ignore ? "no message" : "Your inventory has been changed to " + ChatColor.YELLOW + to.getName();
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 	// ################# Player Teleport
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled())
 			return;
 		Player player = event.getPlayer();
 		ASRegion currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		ASRegion toRegion = plugin.getRegionManager().getRegion(event.getTo());
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check teleport cause for ender pearl
 		Material pearl = Material.ENDER_PEARL;
 		if(event.getCause() == TeleportCause.ENDER_PEARL){
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())
 					|| !plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 			if(!config.get(player.getWorld()).isBlocked(pearl, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 			if(!config.get(player.getWorld()).isBlocked(pearl, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			type = AlertType.LEGAL;
 		}
 
 		// Check type
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 
 			// Alert (with sanity check)
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(pearl.name());
 			String playerMessage = plugin.getMessage("blocked-action.use-item");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert(null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(pearl.name()));
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 
 			// Kill off before region check
 			return;
 		}
 
 		if(currentRegion == null){
 			// Determine alert for World Split
 			config.get(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			config.get(player.getWorld()).checkSplit(player);
 		}
 
 		// Check regions
 		if(currentRegion != toRegion){
 			if(currentRegion != null){
 				currentRegion.alertExit(player);
 			}
 			if(toRegion != null){
 				toRegion.alertEntry(player);
 			}
 		}
 	}
 
 	// ################# Player Craft Item Event
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onCrafting(CraftItemEvent event){
 		if(event.isCancelled())
 			return;
 
 		HumanEntity he = event.getWhoClicked();
 		if((he instanceof Player)){
 			Player player = (Player) he;
 			AlertType type = AlertType.ILLEGAL;
 			if(player.getGameMode() == GameMode.CREATIVE){
 				if(plugin.isBlocked(player, PermissionNodes.MAKE_ANYTHING, player.getWorld())){
 					type = AlertType.LEGAL;
 				}
 				ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 				if(region != null){
 					if(!region.getConfig().isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
 						type = AlertType.LEGAL;
 					}
 				}else{
 					if(config.get(player.getWorld()).isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
 						type = AlertType.LEGAL;
 					}
 				}
 			}else{
 				type = AlertType.LEGAL;
 			}
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to craft an item" : " crafted an item");
 			String playerMessage = plugin.getMessage("blocked-action.crafting");
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.CRAFTING);
 			if(type == AlertType.ILLEGAL){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	// ################# Potion Splash Event
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPotionSplash(PotionSplashEvent event){
 		if(event.isCancelled() || !(event.getPotion().getShooter() instanceof Player))
 			return;
 
 		Player player = (Player) event.getPotion().getShooter();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.USE_ITEM;
 
 		// Right click list
 		// Check if they should be blocked
 		ASRegion asregion = plugin.getRegionManager().getRegion(event.getPotion().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isThrownPotionAllowed()){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isThrownPotionAllowed()){
 				type = AlertType.ILLEGAL;
 			}
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())
 				|| !plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Set messages
 		message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(Material.POTION.name());
 		playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.USE);
 		factory.insertBlock(Material.POTION);
 		playerMessage = factory.toString();
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 		}
 	}
 
 	// ################# Projectile Launch Event
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onProjectileLaunch(ProjectileLaunchEvent event){
 		if(event.isCancelled() || !(event.getEntity().getShooter() instanceof Player))
 			return;
 
 		Player player = (Player) event.getEntity().getShooter();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.USE_ITEM;
 		Material item = Material.AIR;
 
 		// Check for entity
 		if(event.getEntity() instanceof EnderPearl){
 			item = Material.ENDER_PEARL;
 		}
 
 		if(item == Material.AIR){
 			return;
 		}
 
 		// Right click list
 		// Check if they should be blocked
 		ASRegion asregion = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			if(!config.get(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())
 				|| !plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Set messages
 		message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 		playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(null, player, player.getWorld(), TenderType.USE);
 		factory.insertBlock(item);
 		playerMessage = factory.toString();
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 		}
 	}
 
 	// ################# Painting Break Event
 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPaintingBreak(PaintingBreakEvent event){
 		if(event.isCancelled() || !plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.paintings-are-attached")){
 			return;
 		}
 		if(event.getCause() == RemoveCause.PHYSICS){
 			// Removed by something
 			Painting painting = event.getPainting();
 			Location block = painting.getLocation().getBlock().getRelative(painting.getAttachedFace()).getLocation();
 			GameMode gamemode = plugin.getBlockManager().getRecentBreak(block);
 			if(gamemode != null && gamemode == GameMode.CREATIVE){
 				event.setCancelled(true);
 				painting.remove();
 			}
 		}
 	}
 
 }
