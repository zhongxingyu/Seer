 package com.turt2live.antishare.listener;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
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
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.manager.HookManager;
 import com.turt2live.antishare.manager.RegionManager;
 import com.turt2live.antishare.money.Tender.TenderType;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.notification.MessageFactory;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.PerWorldConfig.ListType;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.tekkitcompat.ItemFrameLayer;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.generic.LevelSaver.Level;
 
 public class RegionListener implements Listener {
 
 	private AntiShare plugin = AntiShare.getInstance();
 	private RegionManager manager;
 
 	public RegionListener(RegionManager manager){
 		this.manager = manager;
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
 		Region asregion = manager.getRegion(event.getEntity().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			return;
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
 		Region asregion = manager.getRegion(event.getPotion().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isThrownPotionAllowed()){
 				type = AlertType.ILLEGAL;
 			}
 		}else{
 			return;
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
 				Region region = manager.getRegion(player.getLocation());
 				if(region != null){
 					if(!region.getConfig().isBlocked(event.getRecipe().getResult().getType(), ListType.CRAFTING)){
 						type = AlertType.LEGAL;
 					}
 				}else{
 					return;
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
 
 	// ################# Player Teleport
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		Region currentRegion = manager.getRegion(event.getFrom());
 		Region toRegion = manager.getRegion(event.getTo());
 
 		// World Split
 		if(currentRegion == null){
 			// Determine alert for World Split
 			plugin.getListener().getConfig(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			plugin.getListener().getConfig(player.getWorld()).checkSplit(player);
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
 
 	// ################# Player Quit
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 
 		// Remove from regions
 		Region region = manager.getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 	}
 
 	// ################# Player Join
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 
 		// Check region
 		Region region = manager.getRegion(player.getLocation());
 		if(region != null){
 			// Add join key
 			player.setMetadata("antishare-regionleave", new FixedMetadataValue(plugin, true));
 
 			// Alert entry
 			region.alertSilentEntry(player); // Sets inventory and Game Mode
 			// This must be done because when the inventory manager releases
 			// a player it resets the inventory to "non-temp"
 		}
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
 			Region region = manager.getRegion(target.getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(target, ListType.MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				return;
 			}
 		}
 
 		// Check if we need to continue based on settings
 		Region asregion = manager.getRegion(target.getLocation());
 		if(asregion == null){
 			return;
 		}
 		if(playerCombat){
 			if(!asregion.getConfig().combatAgainstPlayers()){
 				return;
 			}
 		}else{
 			if(!asregion.getConfig().combatAgainstMobs()){
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
 
 	// ################# Player Game Mode Change
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		boolean checkRegion = true;
 
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
 			Region region = manager.getRegion(player.getLocation());
 			if(region != null){
 				ASUtils.sendToPlayer(player, ChatColor.RED + "You are in a region and therefore cannot change Game Mode", true);
 				event.setCancelled(plugin.shouldCancel(player, false));
 				if(player.hasMetadata("ASlevelChange")){
 					List<MetadataValue> values = player.getMetadata("ASlevelChange");
 					for(MetadataValue value : values){
 						if(value.getOwningPlugin().getName().equalsIgnoreCase("AntiShare")){
 							Level currentLevel = (Level) value.value();
 							currentLevel.setTo(player); // Restore level
 						}
 					}
 				}
 				return;
 			}
 		}
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
 		Region currentRegion = manager.getRegion(event.getFrom());
 		Region toRegion = manager.getRegion(event.getTo());
 
 		// Check split
 		if(plugin.getListener().getConfig(player.getWorld()).isSplitActive()){
 			plugin.getListener().getConfig(player.getWorld()).warnSplit(player);
 			plugin.getListener().getConfig(player.getWorld()).checkSplit(player);
 		}
 
 		if(currentRegion == null){
 			// Determine alert for World Split
 			plugin.getListener().getConfig(player.getWorld()).warnSplit(player);
 
 			// Check world split
 			plugin.getListener().getConfig(player.getWorld()).checkSplit(player);
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
 		Region asregion = manager.getRegion(player.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(command, ListType.COMMAND)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
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
 
 	// ################# Player Death
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 
 		// Remove them from a region (if applicable)
 		Region region = manager.getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
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
 		Region asregion = manager.getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.PICKUP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
 		}
 
 		// Region Check
 		if(manager.getRegion(player.getLocation()) != manager.getRegion(item.getLocation()) && type == AlertType.LEGAL){
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
 		Region asregion = manager.getRegion(item.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(itemStack.getType(), ListType.DROP)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
 		}
 
 		// Region Check
 		if(manager.getRegion(player.getLocation()) != manager.getRegion(item.getLocation()) && type == AlertType.LEGAL){
 			if(!plugin.getPermissions().has(player, PermissionNodes.REGION_THROW)){
 				type = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, true));
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + ASUtils.capitalize(itemStack.getType().name()) + ChatColor.WHITE + " into a region.";
 		String playerMessage = ChatColor.RED + "You cannot throw items into another region!";
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert((Material) null, player, player.getWorld(), TenderType.ITEM_DROP, ASUtils.capitalize(itemStack.getType().name()));
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_DROP);
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
 		Region asregion = manager.getRegion(bottle.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
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
 		Region asregion = manager.getRegion(event.getEgg().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.USE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
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
 			Region asregion = manager.getRegion(cart.getLocation());
 			if(asregion != null){
 				if(asregion.getConfig().clearBlockInventoryOnBreak()){
 					cart.getInventory().clear();
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
 			Region region = manager.getRegion(event.getRightClicked().getLocation());
 			if(region != null){
 				if(!region.getConfig().isBlocked(event.getRightClicked(), ListType.RIGHT_CLICK_MOBS)){
 					type = AlertType.LEGAL;
 				}
 			}else{
 				return;
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
 		Region asregion = manager.getRegion(event.getRightClicked().getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(item, ListType.RIGHT_CLICK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
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
 		Region region = manager.getRegion(event.getVehicle().getLocation());
 		if(region != null){
 			if(!region.getConfig().isBlocked(item, ListType.BLOCK_BREAK)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
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
 		Region asregion = manager.getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_PLACE)){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			return;
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PLACE)){
 			Region playerRegion = manager.getRegion(player.getLocation());
 			Region blockRegion = manager.getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				type = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, true));
 		}
 
 		// Alert
 		if(type == AlertType.ILLEGAL){
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 			String playerMessage = ChatColor.RED + "You cannot place blocks in another region!";
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
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
		AlertType specialType = AlertType.ILLEGAL;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, PermissionNodes.DENY_BLOCK_BREAK, block.getWorld(), block.getType())){
 			specialType = AlertType.LEGAL;
 		}
 		Region asregion = manager.getRegion(block.getLocation());
 		if(asregion != null){
 			if(!asregion.getConfig().isBlocked(block, ListType.BLOCK_BREAK)){
 				specialType = AlertType.LEGAL;
 			}
 		}else{
 			return;
 		}
 
 		// Check hooks
 		if(((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForSignProtection(block) || ((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForRegion(player, block)){
 			return; // Don't handle any further, let the other plugin handle it
 		}
 
 		// Check regions
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_BREAK)){
 			Region playerRegion = manager.getRegion(player.getLocation());
 			Region blockRegion = manager.getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				specialType = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(specialType == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert
 		if(specialType == AlertType.ILLEGAL){
 			String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break " : " broke  ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 			String specialPlayerMessage = ChatColor.RED + "You cannot break blocks that are not in your region";
 			plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, AlertTrigger.BLOCK_BREAK);
 		}
 	}
 
 }
