 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
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
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.entity.ThrownExpBottle;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
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
 
 import com.turt2live.antishare.cuboid.CuboidManager.CuboidPoint;
 import com.turt2live.antishare.money.Tender.TenderType;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.notification.MessageFactory;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.storage.PerWorldConfig;
 import com.turt2live.antishare.storage.PerWorldConfig.ListType;
 import com.turt2live.antishare.tekkitcompat.EntityLayer;
 import com.turt2live.antishare.tekkitcompat.HangingListener;
 import com.turt2live.antishare.tekkitcompat.ItemFrameLayer;
 import com.turt2live.antishare.tekkitcompat.PaintingListener;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.generic.LevelSaver;
 import com.turt2live.antishare.util.generic.LevelSaver.Level;
 
 /**
  * The core listener - Listens to all events needed by AntiShare and handles them
  * 
  * @author turt2live
  */
 public class ASListener implements Listener {
 
 	private AntiShare plugin = AntiShare.getInstance();
 	private HashMap<String, PerWorldConfig> config = new HashMap<String, PerWorldConfig>();
 	private boolean hasMobCatcher = false;
 	private HashMap<String, Long> GMCD = new HashMap<String, Long>();
 
 	/**
 	 * Creates a new Listener
 	 */
 	public ASListener(){
 		reload();
 		if(ServerHas.hangingEvents()){
 			plugin.getServer().getPluginManager().registerEvents(new HangingListener(), plugin);
 		}else{
 			plugin.getServer().getPluginManager().registerEvents(new PaintingListener(), plugin);
 		}
 	}
 
 	/**
 	 * Reloads lists
 	 */
 	public void reload(){
 		config.clear();
 		for(World world : Bukkit.getWorlds()){
 			config.put(world.getName(), new PerWorldConfig(world.getName()));
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
 		if(!config.containsKey(world.getName())){
 			config.put(world.getName(), new PerWorldConfig(world.getName()));
 		}
 		return config.get(world.getName());
 	}
 
 	/**
 	 * Gets the configuration for the world
 	 * 
 	 * @param world the world
 	 * @return the configuration
 	 */
 	public PerWorldConfig getConfig(String world){
 		if(!config.containsKey(world)){
 			config.put(world, new PerWorldConfig(world));
 		}
 		return config.get(world);
 	}
 
 	// ################# World Load
 
 	@EventHandler
 	public void onWorldLoad(WorldLoadEvent event){
 		World world = event.getWorld();
 		config.put(world.getName(), new PerWorldConfig(world.getName()));
 		plugin.getBlockManager().loadWorld(world.getName());
 		plugin.getRegionManager().loadWorld(world.getName());
 	}
 
 	// ################# World Unload
 
 	@EventHandler
 	public void onWorldUnload(WorldUnloadEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		World world = event.getWorld();
 		config.remove(world.getName());
 	}
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onEntityMake(BlockPlaceEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		AlertType type = AlertType.LEGAL;
 		Player player = event.getPlayer();
 		/*
 		 * 0 = None
 		 * 1 = Snow Golem
 		 * 2 = Iron Golem
 		 * 3 = Wither
 		 */
 		int mob = 0;
 		Block block = event.getBlock();
 		if(block.getType() == Material.PUMPKIN){
 			Block body1 = block.getRelative(BlockFace.DOWN);
 			Block body2 = body1.getRelative(BlockFace.DOWN);
 			Block arm1 = body1.getRelative(BlockFace.EAST);
 			Block arm2 = body1.getRelative(BlockFace.WEST);
 			Block arm3 = body1.getRelative(BlockFace.NORTH);
 			Block arm4 = body1.getRelative(BlockFace.SOUTH);
 			if(body1.getType() == Material.SNOW_BLOCK && body2.getType() == Material.SNOW_BLOCK){
 				mob = 1;
 			}else if(body1.getType() == Material.IRON_BLOCK && body2.getType() == Material.IRON_BLOCK){
 				boolean isGolem = false;
 				if(arm1.getType() == Material.IRON_BLOCK && arm2.getType() == Material.IRON_BLOCK){
 					isGolem = true;
 				}else if(arm3.getType() == Material.IRON_BLOCK && arm4.getType() == Material.IRON_BLOCK){
 					isGolem = true;
 				}
 				if(isGolem){
 					mob = 2;
 				}
 			}
 		}else if(block.getType() == Material.SKULL){
 			Block[] body1 = {block.getRelative(BlockFace.DOWN), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH), // Side of T 
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.NORTH), // HEAD
 					block.getRelative(BlockFace.SOUTH)}; // HEAD
 			Block[] body2 = {block.getRelative(BlockFace.DOWN), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST), // Side of T 
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.EAST), // HEAD
 					block.getRelative(BlockFace.WEST)}; // HEAD
 			Block[] body3 = {block.getRelative(BlockFace.DOWN), // Right of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.WEST), // HEAD
 					block.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)}; // HEAD
 			Block[] body4 = {block.getRelative(BlockFace.DOWN), // Left of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.EAST), // HEAD
 					block.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST)}; // HEAD
 			Block[] body5 = {block.getRelative(BlockFace.DOWN), // Right of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.NORTH), // HEAD
 					block.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH)}; // HEAD
 			Block[] body6 = {block.getRelative(BlockFace.DOWN), // Left of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH), // Center of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH), // Side of T
 					block.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN), // Base of T
 					block.getRelative(BlockFace.SOUTH), // HEAD
 					block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH)}; // HEAD
 			Block[][] bodies = {body1, body2, body3, body4, body5, body6};
 			boolean matched = false;
 			for(Block[] body : bodies){
 				for(int i = 0; i < body.length - 2; i++){
 					Block bodyBlock = body[i];
 					if(bodyBlock.getType() != Material.SOUL_SAND){
 						continue;
 					}
 				}
 				Block head1 = body[body.length - 2];
 				Block head2 = body[body.length - 1];
 				// TODO: BUKKIT-3406
 				//				if(head1.getType() != Material.SKULL || head2.getType() != Material.SKULL){
 				//					continue;
 				//				}
 				if(head1.getType() == Material.SKULL || head2.getType() == Material.SKULL){
 					matched = true;
 				}else{
 					continue;
 				}
 				break;
 			}
 			if(!matched){
 				return;
 			}
 			mob = 3;
 		}else{
 			return; // Not a mob
 		}
 		String mobName = "Unknown";
 		switch (mob){
 		case 1:
 			mobName = "Snow Golem";
 			if(!plugin.getConfig().getBoolean("enabled-features.mob-creation.allow-snow-golems")){
 				type = AlertType.LEGAL;
 			}
 			break;
 		case 2:
 			mobName = "Iron Golem";
 			if(!plugin.getConfig().getBoolean("enabled-features.mob-creation.allow-iron-golems")){
 				type = AlertType.LEGAL;
 			}
 			break;
 		case 3:
 			mobName = "Wither";
 			if(!plugin.getConfig().getBoolean("enabled-features.mob-creation.allow-wither")){
 				type = AlertType.LEGAL;
 			}
 			break;
 		default:
 			return;
 		}
 		if(plugin.isBlocked(player, PermissionNodes.ALLOW_MOB_CREATION, PermissionNodes.DENY_MOB_CREATION, player.getWorld(), (Material) null)){
 			type = AlertType.ILLEGAL;
 		}
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to create a " : " spawned a ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + mobName;
 		String playerMessage = plugin.getMessage("blocked-action.create-mob");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(block, player, block.getWorld(), TenderType.MOB_MAKE);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.CREATE_MOB);
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 	}
 
 	// ################# Block Break
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onBlockBreak(BlockBreakEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean special = false;
 		boolean region = false;
 		Boolean drops = null;
 		boolean deny = false;
 		AlertType specialType = AlertType.LEGAL;
 		String blockGM = "Unknown";
 		boolean extraSpecial = false;
 		String attachedGM = "Unknown";
 		Material attached = Material.AIR;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, PermissionNodes.DENY_BLOCK_BREAK, block.getWorld(), block.getType())){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(block.getWorld()).isBlocked(block, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Check hooks
 		if(plugin.getHookManager().checkForSignProtection(block)){
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
 				}else if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.disable-breaking-mixed-gamemode")){
 					for(BlockFace face : ASUtils.realFaces){
 						Block rel = block.getRelative(face);
 						if(ASUtils.isDroppedOnBreak(rel, block)){
 							GameMode relGamemode = plugin.getBlockManager().getType(rel);
 							if(relGamemode != null){
 								attachedGM = relGamemode.name().toLowerCase();
 								if(relGamemode != blockGamemode){
 									special = true;
 									extraSpecial = true;
 									deny = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + attachedGM + "-blocks.deny");
 									drops = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + attachedGM + "-blocks.block-drops");
 									if(deny){
 										specialType = AlertType.ILLEGAL;
 									}
 									attached = rel.getType();
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Check regions
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_BREAK)){
 			Region playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			Region blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				special = true;
 				region = true;
 				specialType = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL || specialType == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
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
 				if(extraSpecial){
 					String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break the attached " + attachedGM + " block " : " broke the attached " + attachedGM + " block ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + attached.name().replace("_", " ");
 					String specialPlayerMessage = plugin.getMessage("blocked-action." + attachedGM + "-attached-block-break");
 					MessageFactory factory = new MessageFactory(specialPlayerMessage);
 					factory.insert(block, player, block.getWorld(), attachedGM.equalsIgnoreCase("creative") ? TenderType.CREATIVE_BLOCK : (attachedGM.equalsIgnoreCase("survival") ? TenderType.SURVIVAL_BLOCK : TenderType.ADVENTURE_BLOCK));
 					specialPlayerMessage = factory.toString();
 					plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, (attachedGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : (attachedGM.equalsIgnoreCase("survival") ? AlertTrigger.SURVIVAL_BLOCK : AlertTrigger.ADVENTURE_BLOCK)));
 				}else{
 					String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break the " + blockGM + " block " : " broke the " + blockGM + " block ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 					String specialPlayerMessage = plugin.getMessage("blocked-action." + blockGM + "-block-break");
 					MessageFactory factory = new MessageFactory(specialPlayerMessage);
 					factory.insert(block, player, block.getWorld(), blockGM.equalsIgnoreCase("creative") ? TenderType.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? TenderType.SURVIVAL_BLOCK : TenderType.ADVENTURE_BLOCK));
 					specialPlayerMessage = factory.toString();
 					plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, (blockGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? AlertTrigger.SURVIVAL_BLOCK : AlertTrigger.ADVENTURE_BLOCK)));
 				}
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
 
 		// Check for 'attached' blocks
 		if(player.getGameMode() == GameMode.SURVIVAL && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING) && !event.isCancelled()){
 			for(BlockFace face : ASUtils.realFaces){
 				Block rel = block.getRelative(face);
 				if(ASUtils.isDroppedOnBreak(rel, block)){
 					if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.break-as-gamemode")){
 						GameMode gm = plugin.getBlockManager().getType(rel);
 						if(gm != null){
 							switch (gm){
 							case CREATIVE:
 								rel.setType(Material.AIR);
 								break;
 							case SURVIVAL:
 								rel.breakNaturally();
 								break;
 							default:
 								if(ServerHas.adventureMode()){
 									if(gm == GameMode.ADVENTURE){
 										rel.setType(Material.AIR);
 									}
 								}
 								break;
 							}
 						}
 					}else{
 						rel.setType(Material.AIR);
 					}
 					plugin.getBlockManager().removeBlock(rel);
 				}
 			}
 		}
 
 		// Check for 'attached' blocks and internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING) && !event.isCancelled()){
 			// Check inventories
 			if(getConfig(block.getWorld()).clearBlockInventoryOnBreak()){
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
 			if(getConfig(block.getWorld()).removeAttachedBlocksOnBreak() && plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.paintings-are-attached")){
 				if(ServerHas.mc14xItems()){
 					for(Entity e : block.getChunk().getEntities()){
 						if(EntityLayer.isEntity(e, "ItemFrame")){
 							double d2 = e.getLocation().distanceSquared(block.getLocation());
 							if((d2 < 1.65 && d2 > 1.6) || (d2 > 0.5 && d2 < 0.51)){
 								e.remove();
 							}
 						}
 					}
 
 				}
 				for(BlockFace face : ASUtils.realFaces){
 					Block rel = block.getRelative(face);
 					if(ASUtils.isDroppedOnBreak(rel, block)){
 						if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.break-as-gamemode")){
 							GameMode gm = plugin.getBlockManager().getType(rel);
 							if(gm != null){
 								switch (gm){
 								case CREATIVE:
 									rel.setType(Material.AIR);
 									break;
 								case SURVIVAL:
 									rel.breakNaturally();
 									break;
 								default:
 									if(ServerHas.adventureMode()){
 										if(gm == GameMode.ADVENTURE){
 											rel.setType(Material.AIR);
 										}
 									}
 									break;
 								}
 							}
 						}else{
 							rel.setType(Material.AIR);
 						}
 						plugin.getBlockManager().removeBlock(rel);
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
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onBlockPlace(BlockPlaceEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 		boolean special = false;
 		GameMode existing = null;
 
 		// Sanity check
 		if(block.getType() == Material.AIR){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_PLACE, PermissionNodes.DENY_BLOCK_PLACE, block.getWorld(), block.getType())){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_PLACE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(block.getWorld()).isBlocked(block, ListType.BLOCK_PLACE)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PLACE)){
 			Region playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			Region blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Check for 'attached placing'
 		if(type == AlertType.LEGAL && plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.disable-placing-mixed-gamemode")){
 			Block source = event.getBlockAgainst();
 			Block relative = event.getBlockPlaced();
 			if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 				GameMode potentialNewGM = player.getGameMode();
 				if(ASUtils.isDroppedOnBreak(relative, source)){
 					existing = plugin.getBlockManager().getType(source);
 					if(existing != null){
 						if(existing != potentialNewGM){
 							type = AlertType.ILLEGAL;
 							special = true;
 						}
 					}
 				}
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, true));
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
 			if(special){
 				String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to attach " : " attached ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " onto a " + existing.name().toLowerCase() + " block";
 				String playerMessage = plugin.getMessage("blocked-action.attach-block");
 				MessageFactory factory = new MessageFactory(playerMessage);
 				factory.insert(block, player, block.getWorld(), TenderType.BLOCK_PLACE);
 				playerMessage = factory.toString();
 				plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 			}else{
 				String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 				String playerMessage = plugin.getMessage("blocked-action.place-block");
 				MessageFactory factory = new MessageFactory(playerMessage);
 				factory.insert(block, player, block.getWorld(), TenderType.BLOCK_PLACE);
 				playerMessage = factory.toString();
 				plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 			}
 		}
 	}
 
 	// ################# Player Interact Block
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onInteract(PlayerInteractEvent event){
 		if(event.isCancelled() || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR){
 			return;
 		}
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
 				event.setCancelled(plugin.shouldCancel(player, true));
 				return;
 			}
 		}
 
 		// For use from here on in
 		if(block == null){
 			block = player.getWorld().getBlockAt(player.getLocation());
 		}
 
 		// Right click list
 		if(action == Action.RIGHT_CLICK_BLOCK){
 			// Check if they should be blocked
 			Region asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(block, ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(getConfig(block.getWorld()).isBlocked(block, ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, block.getWorld(), block.getType())){
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
 			Region asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(block, ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(getConfig(block.getWorld()).isBlocked(block, ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			// Check if they should be blocked
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, block.getWorld(), block.getType())){
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
 			Region asregion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().isBlocked(player.getItemInHand().getType(), ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}else{
 				if(getConfig(block.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.USE)){
 					type = AlertType.ILLEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), player.getItemInHand().getType())){
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
 		if(action == Action.RIGHT_CLICK_BLOCK
 				&& player.getItemInHand() != null
 				&& (player.getItemInHand().getType() == Material.EYE_OF_ENDER
 				|| player.getItemInHand().getType() == Material.ENDER_PEARL)){
 			boolean potion = false;
 			Region region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(player.getItemInHand().getType(), ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 					potion = true;
 				}
 			}else{
 				if(!getConfig(player.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.RIGHT_CLICK)){
 					type = AlertType.ILLEGAL;
 					potion = true;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), player.getItemInHand().getType())){
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
 		if(action == Action.RIGHT_CLICK_BLOCK
 				&& player.getItemInHand() != null
 				&& player.getItemInHand().getType() == Material.POTION){
 			boolean potion = false;
 			Region region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(player.getItemInHand().getDurability() > 32000){
 				if(region != null){
 					if(!region.getConfig().isThrownPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}else{
 					if(!getConfig(player.getWorld()).isThrownPotionAllowed()){
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
 					if(!getConfig(player.getWorld()).isPotionAllowed()){
 						type = AlertType.ILLEGAL;
 						potion = true;
 					}
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), player.getItemInHand().getType())){
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
 			event.setCancelled(plugin.shouldCancel(player, true));
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 			if(hasMobCatcher && player.getItemInHand() != null){
 				ItemStack item = player.getItemInHand();
 				if(item.getType() == Material.EGG || item.getType() == Material.MONSTER_EGG){
 					item.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
 				}
 			}
 		}
 	}
 
 	// ################# Destroy Vehicle
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onVechicleDestroy(VehicleDestroyEvent event){
 		if(event.isCancelled() || !(event.getAttacker() instanceof Player)){
 			return;
 		}
 		Player player = (Player) event.getAttacker();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Convert entity -> item ID
 		Material item = Material.AIR;
 		if(event.getVehicle() instanceof StorageMinecart){
 			item = Material.STORAGE_MINECART;
 		}else if(event.getVehicle() instanceof PoweredMinecart){
 			item = Material.POWERED_MINECART;
 		}else if(event.getVehicle() instanceof Boat){
 			item = Material.BOAT;
 		}else if(event.getVehicle() instanceof Minecart){
 			item = Material.MINECART;
 		}
 
 		if(item == Material.AIR){
 			return;
 		}
 
 		// Check permissions
 		Region region = plugin.getRegionManager().getRegion(event.getVehicle().getLocation());
 		if(region != null){
 			if(!region.getConfig().isBlocked(item, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(item, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, PermissionNodes.DENY_BLOCK_BREAK, player.getWorld(), item)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break " : " broke ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + "a " + ASUtils.capitalize(item.name());
 		String playerMessage = plugin.getMessage("blocked-action.break-block");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.BLOCK_BREAK, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_BREAK);
 
 		// Remove drops if required
 		if(type == AlertType.LEGAL && !event.isCancelled()){
 			if(!plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING, player.getWorld()) && player.getGameMode() == GameMode.CREATIVE){
 				if(plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.vehicles")){
 					event.setCancelled(true);
 					event.getVehicle().remove();
 				}
 			}
 		}
 	}
 
 	// ################# Player Interact Entity
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onItemFrameClick(PlayerInteractEntityEvent event){
 		if(event.isCancelled()
 				|| !plugin.getConfig().getBoolean("enabled-features.disable-item-frame-cross-game-mode")
 				|| !ServerHas.mc14xEntities()
 				|| event.getRightClicked().getType() != EntityType.ITEM_FRAME){
 			return;
 		}
 
 		// Setup
 		Player player = event.getPlayer();
 		AlertType type = AlertType.LEGAL;
 		Material item = Material.ITEM_FRAME;
 		Entity entity = event.getRightClicked();
 		GameMode egm = plugin.getBlockManager().getType(entity);
 
 		// Handle
 		if(egm != player.getGameMode()){
 			type = AlertType.ILLEGAL;
 		}
 		if(plugin.getPermissions().has(player, PermissionNodes.ITEM_FRAMES, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
		if(egm == null){
			type = AlertType.LEGAL;
		}
 
 		// Cancel if needed
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 		String playerMessage = plugin.getMessage("blocked-action.right-click");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.RIGHT_CLICK, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 	}
 
 	// ################# Player Interact Entity (2)
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onInteractEntity(PlayerInteractEntityEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 		}else{
 			if(ServerHas.mc14xItems()){
 				if(ItemFrameLayer.isItemFrame(event.getRightClicked())){
 					item = ItemFrameLayer.getItemFrame();
 				}
 			}
 		}
 
 		// If the entity is not found, check for interacted entities
 		if(item == Material.AIR){
 			Region region = plugin.getRegionManager().getRegion(event.getRightClicked().getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				if(!getConfig(player.getWorld()).isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMBAT_MOBS, PermissionNodes.DENY_COMBAT_MOBS, player.getWorld(), ASUtils.getEntityName(event.getRightClicked()))){
 				type = AlertType.LEGAL;
 			}
 
 			// Handle event
 			if(type == AlertType.ILLEGAL){
 				event.setCancelled(plugin.shouldCancel(player, false));
 			}
 
 			// Alert (with sanity check)
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 			String playerMessage = plugin.getMessage("blocked-action.right-click");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert((Material) null, player, player.getWorld(), TenderType.RIGHT_CLICK, ASUtils.capitalize(item.name()));
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 			return; // Nothing was found in the right click check (item), so stop here
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), item)){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(event.getRightClicked().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + "a mob";
 		String playerMessage = plugin.getMessage("blocked-action.right-click");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.RIGHT_CLICK, "a mob");
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 	}
 
 	// ################# Cart Death Check
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onCartDeath(VehicleDestroyEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 			Region asregion = plugin.getRegionManager().getRegion(cart.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().clearBlockInventoryOnBreak()){
 					cart.getInventory().clear();
 				}
 				//			}else{
 				if(getConfig(player.getWorld()).clearBlockInventoryOnBreak()){
 					cart.getInventory().clear();
 				}
 			}
 		}
 	}
 
 	// ################# Egg Check
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onEggThrow(PlayerEggThrowEvent event){
 		Player player = event.getPlayer();
 		AlertType type = AlertType.ILLEGAL;
 		Material item = Material.EGG;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), item)){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(event.getEgg().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(item, ListType.USE)){
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
 		factory.insert((Material) null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 	}
 
 	// ################# Experience Bottle Check
 
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onExpBottle(ExpBottleEvent event){
 		if(event.getExperience() == 0){
 			return;
 		}
 
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
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), item)){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(bottle.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(item, ListType.USE)){
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
 		factory.insert((Material) null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(item.name()));
 		playerMessage = factory.toString();
 		if(type == AlertType.ILLEGAL){ // We don't want to show legal events because of spam
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 		}
 	}
 
 	// ################# Drop Item
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onDrop(PlayerDropItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Item item = event.getItemDrop();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DROP, PermissionNodes.DENY_DROP, player.getWorld(), itemStack.getType())){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.DROP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(itemStack.getType(), ListType.DROP)){
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
 			event.setCancelled(plugin.shouldCancel(player, true));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name());
 		String playerMessage = plugin.getMessage("blocked-action.drop-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name()) + ChatColor.WHITE + " into a region.";
 			playerMessage = ChatColor.RED + "You cannot throw items into another region!";
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.ITEM_DROP, ASUtils.capitalize(itemStack.getType().name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_DROP);
 	}
 
 	// ################# Pickup Item
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onPickup(PlayerPickupItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Item item = event.getItem();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_PICKUP, PermissionNodes.DENY_PICKUP, player.getWorld(), itemStack.getType())){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.PICKUP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(itemStack.getType(), ListType.PICKUP)){
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
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name());
 		String playerMessage = plugin.getMessage("blocked-action.pickup-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name()) + ChatColor.WHITE + " from a region.";
 			playerMessage = ChatColor.RED + "You cannot pickup items from another region!";
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.ITEM_PICKUP, ASUtils.capitalize(itemStack.getType().name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_PICKUP);
 	}
 
 	// ################# Player Death
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		List<ItemStack> drops = event.getDrops();
 		AlertType type = AlertType.ILLEGAL;
 		int illegalItems = 0;
 
 		// Remove them from a region (if applicable)
 		Region region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, PermissionNodes.DENY_DEATH, player.getWorld(), (Material) null)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			List<ItemStack> remove = new ArrayList<ItemStack>();
 			Region asregion = plugin.getRegionManager().getRegion(player.getLocation());
 			for(ItemStack item : drops){
 				if(asregion != null){
 					if(plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, PermissionNodes.DENY_DEATH, player.getWorld(), item.getType(), true)
 							&& asregion.getConfig().isBlocked(item.getType(), ListType.DEATH)){
 						illegalItems++;
 						remove.add(item);
 					}
 				}else{
 					if(plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, PermissionNodes.DENY_DEATH, player.getWorld(), item.getType(), true)
 							&& getConfig(player.getWorld()).isBlocked(item.getType(), ListType.DEATH)){
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
 		factory.insert((Material) null, player, player.getWorld(), TenderType.DEATH);
 		factory.insertAmount(illegalItems);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.PLAYER_DEATH);
 	}
 
 	// ################# Player Command
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		String command = event.getMessage().toLowerCase();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMMANDS, PermissionNodes.DENY_COMMANDS, player.getWorld(), command)){
 			type = AlertType.LEGAL;
 		}
 		Region asregion = plugin.getRegionManager().getRegion(player.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(command, ListType.COMMAND)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(command, ListType.COMMAND)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use the command " : " used the command ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + command;
 		String playerMessage = plugin.getMessage("blocked-action.command");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.COMMAND);
 		factory.insertCommand(command);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.COMMAND, !(event.getMessage().toLowerCase().startsWith("/as money")));
 	}
 
 	// ################# Player Move
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onMove(PlayerMoveEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 
 		// Significant move check
 		if(event.getTo().getBlock().equals(event.getPlayer().getLocation().getBlock())){
 			return;
 		}
 
 		Player player = event.getPlayer();
 		Region currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		Region toRegion = plugin.getRegionManager().getRegion(event.getTo());
 
 		// Check split
 		if(getConfig(player.getWorld()).isSplitActive()){
 			getConfig(player.getWorld()).warnSplit(player);
 			getConfig(player.getWorld()).checkSplit(player);
 		}
 
 		if(currentRegion == null){
 			// Determine alert for World Split
 			getConfig(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			getConfig(player.getWorld()).checkSplit(player);
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
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 						event.setCancelled(plugin.shouldCancel(player, false));
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
 			Region region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				ASUtils.sendToPlayer(player, ChatColor.RED + "You are in a region and therefore cannot change Game Mode", true);
 				event.setCancelled(plugin.shouldCancel(player, false));
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
 				plugin.getInventoryManager().saveEnderCreativeInventory(player, player.getWorld());
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, player.getWorld());
 				plugin.getInventoryManager().saveEnderSurvivalInventory(player, player.getWorld());
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(from == GameMode.ADVENTURE){
 						plugin.getInventoryManager().saveAdventureInventory(player, player.getWorld());
 						plugin.getInventoryManager().saveEnderAdventureInventory(player, player.getWorld());
 					}
 				}
 				break;
 			}
 
 			// Update inventories
 			plugin.getInventoryManager().refreshInventories(player, true);
 
 			// Set to
 			switch (to){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, player.getWorld()).setTo(player);
 				plugin.getInventoryManager().getEnderCreativeInventory(player, player.getWorld()).setTo(player);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, player.getWorld()).setTo(player);
 				plugin.getInventoryManager().getEnderSurvivalInventory(player, player.getWorld()).setTo(player);
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(from == GameMode.ADVENTURE){
 						plugin.getInventoryManager().getAdventureInventory(player, player.getWorld()).setTo(player);
 						plugin.getInventoryManager().getEnderAdventureInventory(player, player.getWorld()).setTo(player);
 					}
 				}
 				break;
 			}
 
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
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onCombat(EntityDamageByEntityEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_PLAYERS, PermissionNodes.DENY_COMBAT_PLAYERS, playerAttacker.getWorld(), ((Player) target).getName())){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			// target = other entity
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_MOBS, PermissionNodes.DENY_COMBAT_MOBS, playerAttacker.getWorld(), ASUtils.getEntityName(target))){
 				type = AlertType.LEGAL;
 			}
 			Region region = plugin.getRegionManager().getRegion(target.getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(target, ListType.MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				if(!getConfig(target.getWorld()).isBlocked(target, ListType.MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}
 		}
 
 		// Check if we need to continue based on settings
 		Region asregion = plugin.getRegionManager().getRegion(target.getLocation());
 		if(playerCombat){
 			if(asregion != null){
 				if(!asregion.getConfig().combatAgainstPlayers()){
 					return;
 				}
 			}else{
 				if(!getConfig(target.getWorld()).combatAgainstPlayers()){
 					return;
 				}
 			}
 		}else{
 			if(asregion != null){
 				if(!asregion.getConfig().combatAgainstMobs()){
 					return;
 				}
 			}else{
 				if(!getConfig(target.getWorld()).combatAgainstMobs()){
 					return;
 				}
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(playerAttacker, false));
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
 			String targetName = ASUtils.getEntityName(target);
 			message = ChatColor.YELLOW + playerAttacker.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to hit a " + ChatColor.RED : " hit a " + ChatColor.GREEN) + targetName;
 			playerMessage = plugin.getMessage("blocked-action.hit-mob");
 			targetFactoryName = targetName;
 		}
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, playerAttacker, playerAttacker.getWorld(), tender);
 		if(tender == TenderType.HIT_MOB){
 			factory.insertHitMob(targetFactoryName);
 		}else{
 			factory.insertHitPlayer(targetFactoryName);
 		}
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, playerAttacker, playerMessage, type, trigger);
 	}
 
 	// ################# Entity Target
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onEntityTarget(EntityTargetLivingEntityEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		LivingEntity target = event.getTarget();
 		Player playerTarget = null;
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check target
 		if(target instanceof Player){
 			playerTarget = (Player) target;
 		}else{
 			return;
 		}
 
 		// Check permissions
 		if(!plugin.isBlocked(playerTarget, PermissionNodes.ALLOW_COMBAT_MOBS, PermissionNodes.DENY_COMBAT_MOBS, playerTarget.getWorld(), ASUtils.getEntityName(target))){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(playerTarget, false));
 		}
 	}
 
 	// ################# Piston Move (Extend)
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onPistonExtend(BlockPistonExtendEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onPistonRetract(BlockPistonRetractEvent event){
 		if(event.isCancelled()){
 			return;
 		}
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
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		// Tell the inventory manager to prepare this player
 		plugin.getInventoryManager().loadPlayer(player);
 
 		// Check region
 		Region region = plugin.getRegionManager().getRegion(player.getLocation());
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
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 
 		// Remove from regions
 		Region region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().releasePlayer(player);
 	}
 
 	// ################# Player World Change
 
 	@EventHandler (priority = EventPriority.MONITOR)
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
 				plugin.getInventoryManager().saveEnderCreativeInventory(player, from);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, from);
 				plugin.getInventoryManager().saveEnderSurvivalInventory(player, from);
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(player.getGameMode() == GameMode.ADVENTURE){
 						plugin.getInventoryManager().saveAdventureInventory(player, from);
 						plugin.getInventoryManager().saveEnderAdventureInventory(player, from);
 					}
 				}
 				break;
 			}
 
 			// Check for linked inventories
 			plugin.getInventoryManager().checkLinks(player, to, from);
 
 			// Update the inventories (check for merges)
 			plugin.getInventoryManager().refreshInventories(player, true);
 
 			// Set to
 			switch (player.getGameMode()){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, to).setTo(player);
 				plugin.getInventoryManager().getEnderCreativeInventory(player, to).setTo(player); // Sets to the ender chest, not the player
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, to).setTo(player);
 				plugin.getInventoryManager().getEnderSurvivalInventory(player, to).setTo(player); // Sets to the ender chest, not the player
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(player.getGameMode() == GameMode.ADVENTURE){
 						plugin.getInventoryManager().getAdventureInventory(player, to).setTo(player);
 						plugin.getInventoryManager().getEnderAdventureInventory(player, to).setTo(player); // Sets to the ender chest, not the player
 					}
 				}
 				break;
 			}
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to world " + ChatColor.YELLOW + to.getName();
 		String playerMessage = ignore ? "no message" : "Your inventory has been changed to " + ChatColor.YELLOW + to.getName();
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 	// ################# Player Teleport
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Region currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		Region toRegion = plugin.getRegionManager().getRegion(event.getTo());
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check teleport cause for ender pearl
 		Material pearl = Material.ENDER_PEARL;
 		if(event.getCause() == TeleportCause.ENDER_PEARL){
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), pearl)
 					|| !plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), pearl)){
 				type = AlertType.LEGAL;
 			}
 			if(!getConfig(player.getWorld()).isBlocked(pearl, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 			if(!getConfig(player.getWorld()).isBlocked(pearl, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			type = AlertType.LEGAL;
 		}
 
 		// Check type
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 
 			// Alert (with sanity check)
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(pearl.name());
 			String playerMessage = plugin.getMessage("blocked-action.use-item");
 			MessageFactory factory = new MessageFactory(playerMessage);
 			factory.insert((Material) null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(pearl.name()));
 			playerMessage = factory.toString();
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 
 			// Kill off before region check
 			return;
 		}
 
 		// World Split
 		if(currentRegion == null){
 			// Determine alert for World Split
 			getConfig(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			getConfig(player.getWorld()).checkSplit(player);
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
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onCrafting(CraftItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 
 		HumanEntity he = event.getWhoClicked();
 		if((he instanceof Player)){
 			Player player = (Player) he;
 			AlertType type = AlertType.ILLEGAL;
 			if(player.getGameMode() == GameMode.CREATIVE){
 				if(plugin.isBlocked(player, PermissionNodes.MAKE_ANYTHING, player.getWorld(), event.getRecipe().getResult().getType())){
 					type = AlertType.LEGAL;
 				}
 				Region region = plugin.getRegionManager().getRegion(player.getLocation());
 				if(region != null){
 					if(!region.getConfig().isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
 						type = AlertType.LEGAL;
 					}
 				}else{
 					if(getConfig(player.getWorld()).isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
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
 				event.setCancelled(plugin.shouldCancel(player, true));
 			}
 		}
 	}
 
 	// ################# Potion Splash Event
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onPotionSplash(PotionSplashEvent event){
 		if(event.isCancelled() || !(event.getPotion().getShooter() instanceof Player)){
 			return;
 		}
 
 		Player player = (Player) event.getPotion().getShooter();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.USE_ITEM;
 
 		// Right click list
 		// Check if they should be blocked
 		Region asregion = plugin.getRegionManager().getRegion(event.getPotion().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isThrownPotionAllowed()){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isThrownPotionAllowed()){
 				type = AlertType.ILLEGAL;
 			}
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), Material.POTION)
 				|| !plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), Material.POTION)){
 			type = AlertType.LEGAL;
 		}
 
 		// Set messages
 		message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(Material.POTION.name());
 		playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.USE);
 		factory.insertBlock(Material.POTION);
 		playerMessage = factory.toString();
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 		}
 	}
 
 	// ################# Projectile Launch Event
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onProjectileLaunch(ProjectileLaunchEvent event){
 		if(event.isCancelled() || !(event.getEntity().getShooter() instanceof Player)){
 			return;
 		}
 
 		Player player = (Player) event.getEntity().getShooter();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.USE_ITEM;
 		Material item = Material.AIR;
 
 		// Check for entity
 		if(event.getEntity() instanceof EnderPearl){
 			item = Material.ENDER_PEARL;
 		}else if(event.getEntity() instanceof Snowball){
 			item = Material.SNOW_BALL;
 		}
 
 		if(item == Material.AIR){
 			return;
 		}
 
 		// Right click list
 		// Check if they should be blocked
 		Region asregion = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			if(!getConfig(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, PermissionNodes.DENY_RIGHT_CLICK, player.getWorld(), item)
 				|| !plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, player.getWorld(), item)){
 			type = AlertType.LEGAL;
 		}
 
 		// Set messages
 		message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(item.name());
 		playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.USE);
 		factory.insertBlock(item);
 		playerMessage = factory.toString();
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 		}
 	}
 
 	// ################# Block Flow Event
 
 	@EventHandler (priority = EventPriority.HIGH)
 	public void onBlockFlow(BlockFromToEvent event){
 		if(event.isCancelled() || !plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection")){
 			return;
 		}
 		boolean deny = plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection-mode.deny");
 		boolean drops = plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection-mode.block-drops");
 		Block to = event.getToBlock();
 		if(ASUtils.canBeBrokenByWater(to.getType())){
 			if(plugin.getBlockManager().getType(to) == GameMode.CREATIVE){
 				if(deny){
 					event.setCancelled(plugin.shouldCancel(null, true));
 				}else if(!drops){
 					to.setType(Material.AIR);
 				}
 				plugin.getBlockManager().removeBlock(to);
 			}
 		}
 	}
 
 	// ################# Entity Explode Event
 
 	@EventHandler (priority = EventPriority.HIGH)
 	public void onExplode(EntityExplodeEvent event){
 		if(event.isCancelled() || !plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection")){
 			return;
 		}
 		boolean deny = plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection-mode.deny");
 		boolean drops = plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.natural-protection-mode.block-drops");
 		for(int i = 0; i < event.blockList().size(); i++){
 			Block block = event.blockList().get(i);
 			if(plugin.getBlockManager().getType(block) == GameMode.CREATIVE){
 				if(deny){
 					event.blockList().remove(i);
 				}else if(!drops){
 					block.setType(Material.AIR);
 					event.blockList().remove(i);
 				}
 				plugin.getBlockManager().removeBlock(block);
 			}
 		}
 	}
 
 	// ################# Player Interact Event (2)
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onInteract2(PlayerInteractEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		if(plugin.getPermissions().has(player, PermissionNodes.CREATE_CUBOID, player.getWorld())){
 			ItemStack item = player.getItemInHand();
 			if(item != null){
 				if(item.getType() == AntiShare.ANTISHARE_CUBOID_TOOL){
 					CuboidPoint point = null;
 					switch (event.getAction()){
 					case RIGHT_CLICK_BLOCK:
 						point = CuboidPoint.POINT2;
 						break;
 					case LEFT_CLICK_BLOCK:
 						point = CuboidPoint.POINT1;
 						break;
 					default:
 						break;
 					}
 					if(point != null){
 						Location location = event.getClickedBlock().getLocation();
 						plugin.getCuboidManager().updateCuboid(player.getName(), point, location);
 						ASUtils.sendToPlayer(player, ChatColor.GREEN + "Point " + (point == CuboidPoint.POINT1 ? "1" : "2")
 								+ " set as ("
 								+ location.getBlockX() + ", "
 								+ location.getBlockY() + ", "
 								+ location.getBlockZ() + ", "
 								+ location.getWorld().getName()
 								+ "). Volume = " + plugin.getCuboidManager().getCuboid(player.getName()).getVolume(), true);
 						event.setCancelled(plugin.shouldCancel(player, true));
 					}
 				}
 			}
 		}
 	}
 
 	// ################# Potion Effect Change Event
 
 	//	@EventHandler (priority = EventPriority.HIGHEST)
 	//	public void onPotionEffectChange(EntityPotionEffectChangeEvent event){
 	//		if(event.isCancelled() || !(event.getEntity() instanceof Player)){
 	//			return;
 	//		}
 	//		if(event.getCause() == PotionChangeCause.BEACON){
 	//			Block beacon = event.getLocation();
 	//			GameMode beaconGM = plugin.getBlockManager().getType(beacon);
 	//			Player player = (Player) event.getEntity();
 	//			if(player.getGameMode() != beaconGM){
 	//				event.setCancelled(plugin.shouldCancel(player));
 	//			}
 	//		}
 	//	}
 
 }
