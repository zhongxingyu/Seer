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
 package com.turt2live.antishare.listener;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
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
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.entity.ThrownExpBottle;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.manager.HookManager;
 import com.turt2live.antishare.manager.RegionManager;
 import com.turt2live.antishare.money.Tender.TenderType;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.notification.MessageFactory;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.PerWorldConfig;
 import com.turt2live.antishare.regions.PerWorldConfig.ListType;
 import com.turt2live.antishare.regions.Region;
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
 public class BaseListener implements Listener {
 
 	/* 
 	 * TODO: Fix messages to their own listener (MONITOR)
 	 */
 
 	private AntiShare plugin = AntiShare.getInstance();
 	private HashMap<String, PerWorldConfig> config = new HashMap<String, PerWorldConfig>();
 	private boolean hasMobCatcher = false;
 	private HashMap<String, Long> GMCD = new HashMap<String, Long>();
 
 	/**
 	 * Creates a new Listener
 	 */
 	public BaseListener(){
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
 		((RegionManager) plugin.getSystemsManager().getManager(Manager.REGION)).loadWorld(world.getName());
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
 		// TODO: Optimize
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
 				boolean noBody = false;
 				for(int i = 0; i < body.length - 2; i++){
 					Block bodyBlock = body[i];
 					if(bodyBlock.getType() != Material.SOUL_SAND){
 						noBody = true;
 						break;
 					}
 				}
 				if(noBody){
 					continue;
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
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, PermissionNodes.DENY_BLOCK_BREAK, block.getWorld(), block.getType())){
 			type = AlertType.LEGAL;
 		}
 		if(!getConfig(block.getWorld()).isBlocked(block, ListType.BLOCK_BREAK)){
 			type = AlertType.LEGAL;
 		}
 
 		// Check hooks
 		if(((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForSignProtection(block) || ((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForRegion(player, block)){
 			return; // Don't handle any further, let the other plugin handle it
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break " : " broke ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 		String playerMessage = plugin.getMessage("blocked-action.break-block");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(block, player, block.getWorld(), TenderType.BLOCK_BREAK);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_BREAK);
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
 
 		// Sanity check
 		if(block.getType() == Material.AIR){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_PLACE, PermissionNodes.DENY_BLOCK_PLACE, block.getWorld(), block.getType())){
 			type = AlertType.LEGAL;
 		}
 		if(!getConfig(block.getWorld()).isBlocked(block, ListType.BLOCK_PLACE)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, true));
 		}
 
 		// Alert
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 		String playerMessage = plugin.getMessage("blocked-action.place-block");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(block, player, block.getWorld(), TenderType.BLOCK_PLACE);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
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
 
 		// For use from here on in
 		if(block == null){
 			block = player.getWorld().getBlockAt(player.getLocation());
 		}
 
 		// Right click list
 		if(action == Action.RIGHT_CLICK_BLOCK){
 			// Check if they should be blocked
 			if(getConfig(block.getWorld()).isBlocked(block, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
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
 			if(getConfig(block.getWorld()).isBlocked(block, ListType.USE)){
 				type = AlertType.ILLEGAL;
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
 			if(getConfig(block.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.USE)){
 				type = AlertType.ILLEGAL;
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
 			if(!getConfig(player.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 				potion = true;
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
 			if(player.getItemInHand().getDurability() > 32000){
 				if(!getConfig(player.getWorld()).isThrownPotionAllowed()){
 					type = AlertType.ILLEGAL;
 					potion = true;
 				}
 			}else{
 				if(!getConfig(player.getWorld()).isPotionAllowed()){
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
 		if(!getConfig(player.getWorld()).isBlocked(item, ListType.BLOCK_BREAK)){
 			type = AlertType.LEGAL;
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
 			if(!getConfig(player.getWorld()).isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 				type = AlertType.LEGAL;
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
 		if(!getConfig(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 			type = AlertType.LEGAL;
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
 			if(getConfig(player.getWorld()).clearBlockInventoryOnBreak()){
 				cart.getInventory().clear();
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
 		if(!getConfig(player.getWorld()).isBlocked(item, ListType.USE)){
 			type = AlertType.LEGAL;
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
 		if(!getConfig(player.getWorld()).isBlocked(item, ListType.USE)){
 			type = AlertType.LEGAL;
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
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DROP, PermissionNodes.DENY_DROP, player.getWorld(), itemStack.getType())){
 			type = AlertType.LEGAL;
 		}
 		if(!getConfig(player.getWorld()).isBlocked(itemStack.getType(), ListType.DROP)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, true));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name());
 		String playerMessage = plugin.getMessage("blocked-action.drop-item");
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
 		if(!getConfig(player.getWorld()).isBlocked(itemStack.getType(), ListType.PICKUP)){
 			type = AlertType.LEGAL;
 		}
 
 		// Region Check
 		if(((RegionManager) plugin.getSystemsManager().getManager(Manager.REGION)).getRegion(player.getLocation()) != ((RegionManager) plugin.getSystemsManager().getManager(Manager.REGION)).getRegion(item.getLocation()) && type == AlertType.LEGAL){
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
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, PermissionNodes.DENY_DEATH, player.getWorld(), (Material) null)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			List<ItemStack> remove = new ArrayList<ItemStack>();
 			Region asregion = ((RegionManager) plugin.getSystemsManager().getManager(Manager.REGION)).getRegion(player.getLocation());
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
 		if(!getConfig(player.getWorld()).isBlocked(command, ListType.COMMAND)){
 			type = AlertType.LEGAL;
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
 
 	// ################# Player Game Mode Change
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		GameMode to = event.getNewGameMode();
 
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
 			player.setMetadata("ASlevelChange", new FixedMetadataValue(plugin, currentLevel));
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to Game Mode " + ChatColor.YELLOW + to.name();
 		String playerMessage = "no message";
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
 			if(!getConfig(target.getWorld()).isBlocked(target, ListType.MOBS)){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Check if we need to continue based on settings
 		if(playerCombat){
 			if(!getConfig(target.getWorld()).combatAgainstPlayers()){
 				return;
 			}
 		}else{
 			if(!getConfig(target.getWorld()).combatAgainstMobs()){
 				return;
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
 
 	// ################# Player Teleport
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
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
 
 		event.setCancelled(plugin.shouldCancel(player, false));
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(pearl.name());
 		String playerMessage = plugin.getMessage("blocked-action.use-item");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.USE, ASUtils.capitalize(pearl.name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
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
 				if(getConfig(player.getWorld()).isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
 					type = AlertType.LEGAL;
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
 		if(!getConfig(player.getWorld()).isThrownPotionAllowed()){
 			type = AlertType.ILLEGAL;
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
 		if(!getConfig(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 			type = AlertType.ILLEGAL;
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
 
 	// ################# Potion Effect Change Event
 
 	//	@EventHandler (priority = EventPriority.HIGHEST)
 	//	public void onPotionEffectChange(EntityPotionEffectChangeEvent event){
 	//		if(event.isCancelled() || !(event.getEntity() instanceof Player)){
 	//			return;
 	//		}
 	//		if(event.getCause() == PotionChangeCause.BEACON){
 	//			Block beacon = event.getLocation();
 	//			GameMode beaconGM = ((BlockManager)plugin.getSystemsManager().getManager(Manager.BLOCKS)).getType(beacon);
 	//			Player player = (Player) event.getEntity();
 	//			if(player.getGameMode() != beaconGM){
 	//				event.setCancelled(plugin.shouldCancel(player));
 	//			}
 	//		}
 	//	}
 
 }
