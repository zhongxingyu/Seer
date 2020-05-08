 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Jukebox;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.EnderSignal;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Hanging;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.ThrownExpBottle;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.entity.minecart.ExplosiveMinecart;
 import org.bukkit.entity.minecart.HopperMinecart;
 import org.bukkit.entity.minecart.PoweredMinecart;
 import org.bukkit.entity.minecart.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import com.turt2live.antishare.compatibility.type.BlockLogger;
 import com.turt2live.antishare.config.ASConfig;
 import com.turt2live.antishare.config.ASConfig.InteractionSettings;
 import com.turt2live.antishare.cuboid.Cuboid;
 import com.turt2live.antishare.io.GameModeIdentity;
 import com.turt2live.antishare.io.LevelSaver;
 import com.turt2live.antishare.io.LevelSaver.Level;
 import com.turt2live.antishare.io.PotionSaver;
 import com.turt2live.antishare.manager.CuboidManager.CuboidPoint;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.antishare.util.ASUtils.EntityPattern;
 import com.turt2live.antishare.util.Action;
 import com.turt2live.antishare.util.GamemodeAbstraction;
 import com.turt2live.antishare.util.MobPattern;
 import com.turt2live.antishare.util.ProtectionInformation;
 import com.turt2live.materials.MaterialAPI;
 
 /**
  * AntiShare listener
  * 
  * @author turt2live
  */
 public class ASListener implements Listener{
 
 	private static AntiShare plugin = AntiShare.p;
 
 	public static final String FALLING_METADATA_KEY = "antishare-falling-original-gamemode";
 	public static final String LOGBLOCK_METADATA_KEY = "antishare-logblock";
 	public static final String NO_PICKUP_METADATA_KEY = "antishare-logblock";
 	public final FixedMetadataValue EMPTY_METADATA;
 
 	private final Map<String, Long> gamemodeCooldowns = new HashMap<String, Long>();
 
 	private boolean hasMobCatcher;
 
 	public ASListener(){
 		hasMobCatcher = plugin.getServer().getPluginManager().getPlugin("MobCatcher") != null;
 		EMPTY_METADATA = new FixedMetadataValue(plugin, true);
 	}
 
 	private ASConfig configFor(Location location){
 		if(plugin.getRegionManager().isRegion(location)){
 			return plugin.getRegionManager().getRegion(location).getConfig();
 		}
 		return plugin.getWorldConfigs().getConfig(location.getWorld());
 	}
 
 	private InteractionSettings configFor(GameMode gamemode1, GameMode gamemode2, Location location){
 		ASConfig c = configFor(location);
 		switch (gamemode1){
 		case ADVENTURE:
 			switch (gamemode2){
 			case CREATIVE:
 				return c.adventureBreakCreative;
 			case SURVIVAL:
 				return c.adventureBreakSurvival;
 			}
 			break;
 		case SURVIVAL:
 			switch (gamemode2){
 			case ADVENTURE:
 				return c.survivalBreakAdventure;
 			case CREATIVE:
 				return c.survivalBreakCreative;
 			}
 			break;
 		case CREATIVE:
 			switch (gamemode2){
 			case ADVENTURE:
 				return c.creativeBreakAdventure;
 			case SURVIVAL:
 				return c.creativeBreakSurvival;
 			}
 			break;
 		}
 		return null;
 	}
 
 	private void breakBlock(Block block, GameMode playerGamemode, ASConfig config, boolean isAttachment, boolean water){
 		GameMode breakAs = playerGamemode;
 		if(isAttachment ? config.naturalSettings.breakAsAttached : water ? config.naturalSettings.breakAsWater : config.naturalSettings.breakAsPiston){
 			breakAs = plugin.getBlockManager().getType(block);
 			if(breakAs == null){
 				breakAs = playerGamemode;
 			}
 		}
 		if(breakAs == null){
 			block.breakNaturally();
 		}else{
 			switch (breakAs){
 			case CREATIVE:
 				block.setType(Material.AIR);
 				break;
 			case ADVENTURE:
 				if(GamemodeAbstraction.isAdventureCreative()){
 					block.setType(Material.AIR);
 				}else{
 					block.breakNaturally();
 				}
 				break;
 			case SURVIVAL:
 				block.breakNaturally();
 				break;
 			}
 		}
 		plugin.getBlockManager().removeBlock(block);
 	}
 
 	private void breakBlock(Block block, GameMode playerGamemode, ASConfig config, boolean isAttachment){
 		breakBlock(block, playerGamemode, config, isAttachment, false);
 	}
 
 	private boolean doGameModeChange(Player player, GameMode from, GameMode to){
 		boolean ignore = true;
 		boolean checkRegion = true;
 		boolean cancel = false;
 
 		if(!player.hasMetadata(NO_PICKUP_METADATA_KEY)){
 			player.setMetadata(NO_PICKUP_METADATA_KEY, EMPTY_METADATA);
 		}
 
 		// Automatically close all open windows
 		InventoryView active = player.getOpenInventory();
 		if(active != null){
 			active.close();
 		}
 
 		// Implement cooldown if needed
 		if(plugin.settings().cooldownSettings.enabled && !AntiShare.hasPermission(player, PermissionNodes.NO_GAMEMODE_COOLDOWN)){
 			long time = (long) Math.abs(plugin.settings().cooldownSettings.seconds) * 1000;
 			long now = System.currentTimeMillis();
 			if(time > 0){
 				if(gamemodeCooldowns.containsKey(player.getName())){
 					long lastUsed = gamemodeCooldowns.get(player.getName());
 					if(now - lastUsed > time){
 						// Allow
 						gamemodeCooldowns.put(player.getName(), now);
 					}else{
 						// Deny
 						cancel = true;
 						int seconds = (int) (time - (now - lastUsed)) / 1000;
 						plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("gamemode-wait", String.valueOf(seconds)), true); //ASUtils.sendToPlayer(player, ChatColor.RED + "You must wait at least " + seconds + " more second" + s + " before changing Game Modes.", true);
 						player.removeMetadata(NO_PICKUP_METADATA_KEY, plugin);
 						return cancel;
 					}
 				}else{
 					gamemodeCooldowns.put(player.getName(), now);
 				}
 			}
 		}
 
 		// Change level if needed
 		Level currentLevel = new Level(player.getLevel(), player.getExp());
 		if(plugin.settings().gamemodeChangeSettings.changeLevel && !AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 			Level desired = LevelSaver.getLevel(player.getName(), to);
 			LevelSaver.saveLevel(player.getName(), player.getGameMode(), currentLevel);
 			desired.setTo(player);
 		}
 
 		// Change balance if needed
 		boolean alert = false;
 		if(plugin.settings().gamemodeChangeSettings.changeBalance && !AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 			if(plugin.getMoneyManager().getRawEconomyHook() != null){
 				plugin.getMoneyManager().getRawEconomyHook().switchBalance(player.getName(), from, to);
 				alert = true;
 			}
 		}
 
 		// Change potion effects if needed
 		if(plugin.settings().gamemodeChangeSettings.changePotionEffects && !AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 			PotionSaver.saveEffects(player, from);
 			PotionSaver.applySavedEffects(player, to);
 		}
 
 		// Check to see if we should even bother
 		if(!plugin.settings().features.inventories){
 			player.removeMetadata(NO_PICKUP_METADATA_KEY, plugin);
 			return cancel;
 		}
 
 		// Tag check
 		if(player.hasMetadata("antishare-regionleave")){
 			player.removeMetadata("antishare-regionleave", plugin);
 			checkRegion = false;
 		}
 
 		// Region Check
 		if(!AntiShare.hasPermission(player, PermissionNodes.REGION_ROAM) && checkRegion){
 			Region region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("region-gamemode"), true);
 				cancel = true;
 				currentLevel.setTo(player); // Restore level
 				// Restore balance if needed
 				if(plugin.settings().gamemodeChangeSettings.changeBalance && !AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 					if(plugin.getMoneyManager().getRawEconomyHook() != null){
 						plugin.getMoneyManager().getRawEconomyHook().switchBalance(player.getName(), to, from);
 					}
 				}
 				// Restore effects
 				if(plugin.settings().gamemodeChangeSettings.changePotionEffects && !AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 					PotionSaver.saveEffects(player, to);
 					PotionSaver.applySavedEffects(player, from);
 				}
 				player.removeMetadata(NO_PICKUP_METADATA_KEY, plugin);
 				return cancel;
 			}
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		if(!AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 			// Check for open inventories and stuff
 			player.closeInventory();
 
 			plugin.getInventoryManager().onGameModeChange(player, to, from);
 
 			// For alerts
 			ignore = false;
 		}
 
 		if(alert){
 			if(plugin.getMoneyManager().getRawEconomyHook() != null){
 				String formatted = plugin.getMoneyManager().getRawEconomyHook().format(plugin.getMoneyManager().getBalance(player));
 				plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("balance-change", formatted), true);
 			}
 		}
 		if(!ignore){
 			plugin.getMessages().notifyParties(player, Action.GAMEMODE_CHANGE, false, MaterialAPI.capitalize(to.name()));
 		}
 
 		player.removeMetadata(NO_PICKUP_METADATA_KEY, plugin);
 		return cancel;
 	}
 
 	private void scheduleGameModeChange(PlayerGameModeChangeEvent event){
 		final Player player = event.getPlayer();
 		final GameMode from = player.getGameMode();
 		final GameMode to = event.getNewGameMode();
 		player.setMetadata(NO_PICKUP_METADATA_KEY, EMPTY_METADATA);
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			@Override
 			public void run(){
 				doGameModeChange(player, from, to);
 			}
 		}, 2);
 	}
 
 	// Code used from feildmaster's gist on recursive player fetching
 	// https://gist.github.com/feildmaster/6e8f6bfa0aa55cbab208
 	private Player getPlayer(final Entity damager){
 		if(damager == null){
 			return null;
 		}else if(damager instanceof Player){
 			return (Player) damager;
 		}else if(damager instanceof Tameable){
 			AnimalTamer tamer = ((Tameable) damager).getOwner();
 			if(tamer instanceof Entity){
 				return getPlayer((Entity) tamer);
 			}
 		}else if(damager instanceof Projectile){
 			return getPlayer(((Projectile) damager).getShooter());
 		}
 		return null;
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onChunkLoad(ChunkLoadEvent event){
 		plugin.getBlockManager().loadChunk(event.getChunk());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onChunkUnload(ChunkUnloadEvent event){
 		plugin.getBlockManager().unloadChunk(event.getChunk());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onWorldLoad(WorldLoadEvent event){
 		plugin.getRegionManager().loadWorld(event.getWorld().getName());
 	}
 
 	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBlockFall(EntityChangeBlockEvent event){
 		if(event.getEntity() instanceof FallingBlock){
 			boolean falling = event.getTo() == Material.AIR;
 			FallingBlock sand = (FallingBlock) event.getEntity(); // May not be sand
 			Location location = sand.getLocation();
 			location.setY(location.getY() + sand.getFallDistance()); // For lag. In testing sand.getFallDistance() did not exceed 0.04
 			Block block = location.getBlock();
 			ASConfig c = configFor(location);
 			if(falling){
 				if(c.naturalSettings.breakSand && sand.getDropItem()){
 					GameMode type = plugin.getBlockManager().getType(block);
 					if(GamemodeAbstraction.isCreative(type)){
 						sand.setDropItem(false);
 					}
 					if(type != null){
 						sand.setMetadata(FALLING_METADATA_KEY, new FixedMetadataValue(plugin, type));
 					}
 				}
 				plugin.getBlockManager().removeBlock(block);
 			}else if(sand.hasMetadata(FALLING_METADATA_KEY)){
 				List<MetadataValue> meta = sand.getMetadata(FALLING_METADATA_KEY);
 				GameMode type = null;
 				for(MetadataValue v : meta){
 					if(v.getOwningPlugin().getName().equalsIgnoreCase("AntiShare") && v.value() instanceof GameMode){
 						type = (GameMode) v.value();
 					}
 				}
 				if(type != null){
 					plugin.getBlockManager().addBlock(type, block);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onExplode(EntityExplodeEvent event){
 		ASConfig c = configFor(event.getLocation());
 		List<Block> list = new ArrayList<Block>();
 		list.addAll(event.blockList());
 		Iterator<Block> iterate = list.iterator();
 		while(iterate.hasNext()){
 			Block block = iterate.next();
 			GameMode type = plugin.getBlockManager().getType(block);
 			if(GamemodeAbstraction.isCreative(type)){
 				if(c.naturalSettings.breakAsBomb){
 					block.setMetadata(LOGBLOCK_METADATA_KEY, EMPTY_METADATA);
 					plugin.getHookManager().sendBlockBreak("EXPLOSION", block.getLocation(), block.getType(), block.getData());
 					plugin.getBlockManager().removeBlock(block);
 					block.setType(Material.AIR);
 				}
 				event.blockList().remove(block);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onItemSpawn(ItemSpawnEvent event){
 		final Block block = event.getLocation().getBlock();
 		if(block.getType() != Material.AIR && !MaterialAPI.isSimilar(block.getType(), event.getEntity().getItemStack().getType())){
 			return;
 		}
 		ASConfig c = configFor(event.getLocation());
 		if(c.naturalSettings.breakAsAttached){
 			GameMode type = plugin.getBlockManager().getType(block);
 			if(GamemodeAbstraction.isCreative(type)){
 				event.setCancelled(true);
 			}
 		}
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			@Override
 			public void run(){
 				block.setMetadata(LOGBLOCK_METADATA_KEY, EMPTY_METADATA);
 				plugin.getHookManager().sendBlockBreak(null, block.getLocation(), block.getType(), block.getData());
 				plugin.getBlockManager().removeBlock(block);
 			}
 		}, 2);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onEntityMake(BlockPlaceEvent event){
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 		boolean illegal = false;
 		ASConfig c = configFor(block.getLocation());
 		MobPattern snowGolemPattern = ASUtils.getMobPattern(EntityPattern.SNOW_GOLEM);
 		MobPattern ironGolemPattern = ASUtils.getMobPattern(EntityPattern.IRON_GOLEM);
 		MobPattern witherPattern = ASUtils.getMobPattern(EntityPattern.WITHER);
 		MobPattern pattern = null;
 		if(snowGolemPattern != null && snowGolemPattern.exists(block)){
 			pattern = snowGolemPattern;
 		}else if(ironGolemPattern != null && ironGolemPattern.exists(block)){
 			pattern = ironGolemPattern;
 		}else if(witherPattern != null && witherPattern.exists(block)){
 			pattern = witherPattern;
 		}
 		if(pattern == null){
 			return;
 		}
 		String mobName = pattern.name;
 
 		ProtectionInformation info = ASUtils.isBlocked(player, null, c.craftedMobs, pattern.entityType, PermissionNodes.PACK_MOB_MAKE);
 		illegal = info.illegal;
 
 		plugin.getMessages().notifyParties(player, Action.CRAFTED_MOB, illegal, mobName);
 		if(illegal){
 			event.setCancelled(true);
 			pattern.scheduleUpdate(block);
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 		Block block = event.getBlock();
 		if(plugin.getHookManager().checkForSignProtection(block)){
 			return; // Let them deal with it
 		}
 		Player player = event.getPlayer();
 		Location blockLocation = block.getLocation();
 		GameMode blockGamemode = null, attachedGamemode = null;
 		boolean isRegion = false, isAttached = false, isGamemode = false;
 		boolean illegal = false;
 		InteractionSettings interaction = null;
 		Material attachedMaterial = Material.AIR;
 		ASConfig c = configFor(blockLocation);
 
 		ProtectionInformation info = ASUtils.isBlocked(player, block, c.blockBreak, PermissionNodes.PACK_BLOCK_BREAK);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region blockRegion = info.targetRegion;
 
 		if(!AntiShare.hasPermission(player, PermissionNodes.FREE_PLACE)){
 			blockGamemode = plugin.getBlockManager().getType(block);
 			if(blockGamemode != null){
 				if(!GamemodeAbstraction.isMatch(blockGamemode, player.getGameMode())){
 					isGamemode = true;
 					interaction = configFor(player.getGameMode(), blockGamemode, blockLocation);
 				}else if(!c.naturalSettings.allowMismatchedGM){
 					for(BlockFace face : ASUtils.TRUE_BLOCK_FACES){
 						Block rel = block.getRelative(face);
 						if(MaterialAPI.isDroppedOnBreak(rel, block, true)){
 							attachedGamemode = plugin.getBlockManager().getType(rel);
 							if(attachedGamemode != null){
 								if(!GamemodeAbstraction.isMatch(blockGamemode, attachedGamemode)){
 									isAttached = true;
 									interaction = configFor(player.getGameMode(), attachedGamemode, blockLocation);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		if(!illegal && interaction != null){
 			illegal = interaction.deny;
 		}
 		if(illegal){
 			event.setCancelled(true);
 		}else{
 			plugin.getBlockManager().removeBlock(block);
 		}
 
 		Action action = Action.BLOCK_BREAK;
 		String[] extras = null;
 		if(isRegion){
 			action = Action.REGION_BLOCK_BREAK;
 			extras = new String[] {blockRegion == null ? plugin.getMessages().getMessage("wilderness") : blockRegion.getName()};
 		}else{
 			if(isGamemode || isAttached){
 				action = Action.GAMEMODE_BLOCK_BREAK;
 				extras = new String[] {MaterialAPI.capitalize((isGamemode ? blockGamemode : attachedGamemode).name())};
 			}
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, isAttached ? attachedMaterial : block.getType(), extras);
 
 		if(interaction != null && !interaction.deny){
 			block.setMetadata(LOGBLOCK_METADATA_KEY, EMPTY_METADATA);
 			plugin.getHookManager().sendBlockBreak(player.getName(), block.getLocation(), block.getType(), block.getData());
 			plugin.getBlockManager().removeBlock(block);
 			if(interaction.drop){
 				block.breakNaturally();
 			}else{
 				block.setType(Material.AIR);
 			}
 		}
 
 		if(event.isCancelled() || AntiShare.hasPermission(player, PermissionNodes.BREAK_ANYTHING)){
 			return;
 		}
 
 		if(GamemodeAbstraction.isCreative(player.getGameMode())){
 			if(c.naturalSettings.emptyInventories){
 				if(block.getState() instanceof Chest){
 					Chest state = (Chest) block.getState();
 					state.getBlockInventory().clear();
 				}else if(block.getState() instanceof Jukebox){
 					Jukebox state = (Jukebox) block.getState();
 					state.setPlaying(null);
 				}else if(block.getState() instanceof InventoryHolder){
 					InventoryHolder state = (InventoryHolder) block.getState();
 					state.getInventory().clear();
 				}
 			}
 		}
 
 		if(c.naturalSettings.removeAttached){
 			for(Entity e : block.getChunk().getEntities()){
 				if(e instanceof ItemFrame){
 					double d2 = e.getLocation().distanceSquared(block.getLocation());
 					if(d2 < 1.65 && d2 > 1.6 || d2 > 0.5 && d2 < 0.51){
 						plugin.getHookManager().sendEntityBreak(player.getName(), e.getLocation(), Material.ITEM_FRAME, BlockLogger.DEFAULT_DATA);
 						e.remove();
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 		Location blockLocation = block.getLocation();
 		boolean isRegion = false, isAttach = false;
 		boolean illegal = false;
 		GameMode existing = null;
 		ASConfig c = configFor(blockLocation);
 
 		ProtectionInformation info = ASUtils.isBlocked(player, block, c.blockPlace, PermissionNodes.PACK_BLOCK_PLACE);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region blockRegion = info.targetRegion;
 
 		if(!illegal && !c.naturalSettings.allowMismatchedGM){
 			Block source = event.getBlockAgainst();
 			Block relative = event.getBlockPlaced();
 			if(!AntiShare.hasPermission(player, PermissionNodes.FREE_PLACE)){
 				GameMode potentialNewGM = player.getGameMode();
 				if(MaterialAPI.isDroppedOnBreak(relative, source, true)){
 					existing = plugin.getBlockManager().getType(source);
 					if(existing != null){
 						if(existing != potentialNewGM){
 							illegal = true;
 							isAttach = true;
 						}
 					}
 				}
 			}
 		}
 
 		if(illegal){
 			event.setCancelled(true);
 		}else{
 			if(!AntiShare.hasPermission(player, PermissionNodes.FREE_PLACE)){
 				plugin.getBlockManager().addBlock(player.getGameMode(), block);
 			}
 		}
 
 		Action action = Action.BLOCK_PLACE;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_BLOCK_PLACE;
 			extra = new String[] {blockRegion == null ? plugin.getMessages().getMessage("wilderness") : blockRegion.getName()};
 		}else if(isAttach){
 			action = Action.PLACE_GAMEMODE_ATTACHMENT;
 			extra = new String[] {MaterialAPI.capitalize(existing.name())};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, block.getType(), extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onEat(PlayerItemConsumeEvent event){
 		Player player = event.getPlayer();
 		boolean illegal = false;
 		ASConfig c = configFor(player.getLocation());
 		ItemStack hand = event.getItem();
 
 		ProtectionInformation info = ASUtils.isBlocked(player, hand, c.eat, PermissionNodes.PACK_EAT, c);
 		illegal = info.illegal;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		Action eventAction = Action.EAT_SOMETHING;
 		plugin.getMessages().notifyParties(player, eventAction, illegal, hand.getType());
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onToolInteract(PlayerInteractEvent event){
 		org.bukkit.event.block.Action action = event.getAction();
 		switch (action){
 		case LEFT_CLICK_AIR:
 		case RIGHT_CLICK_AIR:
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		ItemStack hand = player.getItemInHand();
 		if(hand == null){
 			hand = new ItemStack(Material.AIR);
 		}
 
 		if(AntiShare.hasPermission(player, PermissionNodes.TOOL_USE) && hand.getDurability() == AntiShare.ANTISHARE_TOOL_DATA){
 			String blockName = MaterialAPI.capitalize(block.getType().name());
 			if(hand.getType() == AntiShare.ANTISHARE_TOOL){
 				GameMode type = plugin.getBlockManager().getType(block);
 				if(type == null){
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-natural", blockName), true);
 				}else{
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-type", blockName, MaterialAPI.capitalize(type.name())), true);
 				}
 				event.setCancelled(true);
 				return;
 			}else if(hand.getType() == AntiShare.ANTISHARE_SET_TOOL){
 				GameMode gamemode = plugin.getBlockManager().getType(block);
 				plugin.getBlockManager().removeBlock(block);
 				switch (action){
 				case RIGHT_CLICK_BLOCK:
 					if(gamemode == null){
 						plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-removed-natural"), true);
 					}else{
 						plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-removed", MaterialAPI.capitalize(gamemode.name())), true);
 					}
 					break;
 				case LEFT_CLICK_BLOCK:
 					plugin.getBlockManager().addBlock(player.getGameMode(), block);
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-set", MaterialAPI.capitalize(player.getGameMode().name())), true);
 					break;
 				}
 				event.setCancelled(true);
 				return;
 			}else if(hand.getType() == AntiShare.ANTISHARE_CUBOID_TOOL && AntiShare.hasPermission(player, PermissionNodes.CREATE_CUBOID)){
 				CuboidPoint point = null;
 				switch (action){
 				case RIGHT_CLICK_BLOCK:
 					point = CuboidPoint.POINT2;
 					break;
 				case LEFT_CLICK_BLOCK:
 					point = CuboidPoint.POINT1;
 					break;
 				}
 				plugin.getCuboidManager().updateCuboid(player.getName(), point, block.getLocation());
 				Cuboid cuboid = plugin.getCuboidManager().getCuboid(player.getName());
 				int volume = cuboid == null ? 0 : cuboid.getVolume();
 				String pointName = point == CuboidPoint.POINT1 ? "1" : "2";
 				plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("cuboid-updated", pointName, String.valueOf(volume)), true);
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onUseInteract(PlayerInteractEvent event){
 		org.bukkit.event.block.Action action = event.getAction();
 		switch (action){
 		case LEFT_CLICK_AIR:
 		case RIGHT_CLICK_AIR:
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		boolean illegal = false, isRegion = false;
 		Location blockLocation = block.getLocation();
 		ASConfig c = configFor(blockLocation);
 		ItemStack hand = player.getItemInHand();
 		if(hand == null || hand.getType() == Material.AIR){
 			return; // Do not process
 		}
 
 		ProtectionInformation information = ASUtils.isBlocked(player, hand, blockLocation, c.use, PermissionNodes.PACK_USE, c);
 		illegal = information.illegal;
 		isRegion = information.isRegion;
 		Region blockRegion = information.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 			if(hasMobCatcher){
 				ItemStack trueHand = player.getItemInHand();
 				if(trueHand != null){
 					if(trueHand.getType() == Material.EGG || trueHand.getType() == Material.MONSTER_EGG){
 						trueHand.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
 					}
 				}
 			}
 		}
 
 		Action eventAction = Action.USE_SOMETHING;
 		String[] extra = new String[0];
 		if(isRegion){
 			eventAction = Action.REGION_USE_SOMETHING;
 			extra = new String[] {blockRegion == null ? plugin.getMessages().getMessage("wilderness") : blockRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, eventAction, illegal, hand.getType(), extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onInteractInteract(PlayerInteractEvent event){
 		org.bukkit.event.block.Action action = event.getAction();
 		switch (action){
 		case LEFT_CLICK_AIR:
 		case LEFT_CLICK_BLOCK:
 		case RIGHT_CLICK_AIR:
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		boolean illegal = false, isRegion = false;
 		ASConfig c = configFor(block.getLocation());
 
 		ProtectionInformation information = ASUtils.isBlocked(player, block, c.interact, PermissionNodes.PACK_USE);
 		illegal = information.illegal;
 		isRegion = information.isRegion;
 		Region blockRegion = information.targetRegion;
 
 		if(!c.naturalSettings.allowMismatchedGM
 				&& plugin.getBlockManager().getType(block) != null
 				&& !GamemodeAbstraction.isMatch(player.getGameMode(), plugin.getBlockManager().getType(block))
 				&& !AntiShare.hasPermission(player, PermissionNodes.FREE_PLACE)
 				&& !illegal){
 			illegal = true;
 		}
 
 		if(illegal){
 			event.setCancelled(true);
 			if(hasMobCatcher){
 				ItemStack trueHand = player.getItemInHand();
 				if(trueHand != null){
 					if(trueHand.getType() == Material.EGG || trueHand.getType() == Material.MONSTER_EGG){
 						trueHand.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
 					}
 				}
 			}
 		}
 
 		Action eventAction = Action.INTERACT_SOMETHING;
 		String[] extra = new String[0];
 		if(isRegion){
 			eventAction = Action.REGION_INTERACT_SOMETHING;
 			extra = new String[] {blockRegion == null ? plugin.getMessages().getMessage("wilderness") : blockRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, eventAction, illegal, block.getType(), extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onVechicleDestroy(VehicleDestroyEvent event){
 		Entity attacker = event.getAttacker();
 		if(!(attacker instanceof Player)){
 			return;
 		}
 		Player player = (Player) attacker;
 		boolean illegal = false;
 		Material item = Material.AIR;
 		boolean isRegion = false;
 		ASConfig c = configFor(event.getVehicle().getLocation());
 		if(event.getVehicle() instanceof StorageMinecart){
 			item = Material.STORAGE_MINECART;
 		}else if(event.getVehicle() instanceof PoweredMinecart){
 			item = Material.POWERED_MINECART;
 		}else if(event.getVehicle() instanceof ExplosiveMinecart){
 			item = Material.EXPLOSIVE_MINECART;
 		}else if(event.getVehicle() instanceof HopperMinecart){
 			item = Material.HOPPER_MINECART;
 		}else if(event.getVehicle() instanceof Boat){
 			item = Material.BOAT;
 		}else if(event.getVehicle() instanceof Minecart){
 			item = Material.MINECART;
 		}
 		if(item == Material.AIR){
 			return;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(player, event.getVehicle().getLocation(), c.blockBreak, item, PermissionNodes.PACK_BLOCK_BREAK);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region vehicleRegion = info.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 		}else{
 			if(event.getVehicle() instanceof StorageMinecart
 					&& c.naturalSettings.emptyInventories
 					&& GamemodeAbstraction.isCreative(player.getGameMode())
 					&& !AntiShare.hasPermission(player, PermissionNodes.BREAK_ANYTHING)){
 				StorageMinecart m = (StorageMinecart) event.getVehicle();
 				m.getInventory().clear();
 			}
 		}
 
 		Action action = Action.BLOCK_BREAK;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_BLOCK_BREAK;
 			extra = new String[] {vehicleRegion == null ? plugin.getMessages().getMessage("wilderness") : vehicleRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onEntityInteract(PlayerInteractEntityEvent event){
 		Player player = event.getPlayer();
 		ItemStack hand = player.getItemInHand();
 		boolean illegal = false;
 		boolean isRegion = false, isItemFrame = false, isInteract = false;
 		ASConfig c = configFor(event.getRightClicked().getLocation());
 		Entity entity = event.getRightClicked();
 		GameMode gamemode = plugin.getBlockManager().getType(entity);
 		if(hand == null){
 			hand = new ItemStack(Material.AIR);
 		}
 		Material rightClicked = Material.AIR;
 		if(entity instanceof StorageMinecart){
 			rightClicked = Material.STORAGE_MINECART;
 		}else if(entity instanceof PoweredMinecart){
 			rightClicked = Material.POWERED_MINECART;
 		}else if(entity instanceof Boat){
 			rightClicked = Material.BOAT;
 		}else if(entity instanceof Minecart){
 			rightClicked = Material.MINECART;
 		}else if(entity instanceof Painting){
 			rightClicked = Material.PAINTING;
 		}else if(entity instanceof ItemFrame){
 			rightClicked = Material.ITEM_FRAME;
 		}else if(entity instanceof Player){
 			return; // We don't need to protect against right clicking players
 		}
 
 		if(hand.getDurability() == AntiShare.ANTISHARE_TOOL_DATA && AntiShare.hasPermission(player, PermissionNodes.TOOL_USE) && rightClicked != Material.AIR){
 			if(hand.getType() == AntiShare.ANTISHARE_TOOL){
 				if(gamemode == null){
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-natural", MaterialAPI.capitalize(rightClicked.name())), true);
 				}else{
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-type", MaterialAPI.capitalize(rightClicked.name()), MaterialAPI.capitalize(gamemode.name())), true);
 				}
 				event.setCancelled(true);
 				return;
 			}else if(hand.getType() == AntiShare.ANTISHARE_SET_TOOL){
 				plugin.getBlockManager().removeEntity(entity);
 				if(gamemode == null){
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-removed-natural"), true);
 				}else{
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-removed", MaterialAPI.capitalize(gamemode.name())), true);
 				}
 				event.setCancelled(true);
 				return;
 			}else if(hand.getType() == AntiShare.ANTISHARE_CUBOID_TOOL && AntiShare.hasPermission(player, PermissionNodes.CREATE_CUBOID)){
 				plugin.getCuboidManager().updateCuboid(player.getName(), CuboidPoint.POINT2, entity.getLocation());
 				Cuboid cuboid = plugin.getCuboidManager().getCuboid(player.getName());
 				int volume = cuboid == null ? 0 : cuboid.getVolume();
 				plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("cuboid-updated", "2", String.valueOf(volume)), true);
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		String regionPermission = PermissionNodes.REGION_USE;
 		if(rightClicked == Material.ITEM_FRAME && !c.naturalSettings.allowMismatchedGM){
 			isItemFrame = true;
 			if(!GamemodeAbstraction.isMatch(gamemode, player.getGameMode())){
 				illegal = true;
 			}
 			if(gamemode == null || AntiShare.hasPermission(player, PermissionNodes.ITEM_FRAMES)){
 				illegal = false;
 			}
 		}else if(rightClicked == Material.AIR){
 			isInteract = true;
 			regionPermission = PermissionNodes.REGION_ATTACK_MOBS;
 			if(c.interactMobs.contains(entity.getType())){
 				illegal = true;
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMBAT_MOBS, PermissionNodes.DENY_COMBAT_MOBS, entity.getType().getName())){
 				illegal = false;
 			}
 		}
 
 		if(c.interact.has(rightClicked)){
 			illegal = true;
 		}
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_INTERACT, PermissionNodes.DENY_INTERACT, rightClicked)){
 			illegal = false;
 		}
 
 		Region playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 		Region entityRegion = plugin.getRegionManager().getRegion(entity.getLocation());
 		if(!AntiShare.hasPermission(player, regionPermission)){
 			if(playerRegion != entityRegion){
 				illegal = true;
 				isRegion = true;
 			}
 		}
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		Action action = Action.USE_SOMETHING;
 		String[] extra = null;
 		String main = MaterialAPI.capitalize(rightClicked.name());
 		if(isRegion){
 			action = Action.REGION_INTERACT_MOB;
 			extra = new String[] {entityRegion == null ? plugin.getMessages().getMessage("wilderness") : entityRegion.getName()};
 			if(isInteract){
 				main = entity.getType().getName();
 			}else if(!isItemFrame){
 				action = Action.REGION_USE_SOMETHING;
 				extra = new String[] {MaterialAPI.capitalize(hand.getType().name()),
 						entityRegion == null ? plugin.getMessages().getMessage("wilderness") : entityRegion.getName()};
 			}
 		}else{
 			action = Action.INTERACT_MOB;
 			if(isInteract){
 				main = entity.getType().getName();
 			}else if(!isItemFrame){
 				action = Action.USE_SOMETHING;
				extra = new String[] {MaterialAPI.capitalize(hand.getType().name())};
 			}
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, main, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onEggThrow(PlayerEggThrowEvent event){
 		if(!event.isHatching()){
 			return;
 		}
 		Player player = event.getPlayer();
 		boolean illegal = false;
 		boolean isRegion = false;
 		Egg egg = event.getEgg();
 		Material material = Material.EGG;
 		ASConfig c = configFor(egg.getLocation());
 
 		ProtectionInformation info = ASUtils.isBlocked(player, egg.getLocation(), c.drop, material, PermissionNodes.PACK_DROP);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region eggRegion = info.targetRegion;
 
 		if(illegal){
 			event.setHatching(false);
 		}
 
 		Action action = Action.ITEM_THROW;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_ITEM_THROW;
 			extra = new String[] {eggRegion == null ? plugin.getMessages().getMessage("wilderness") : eggRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, material, extra);
 	}
 
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onExpBottle(ExpBottleEvent event){
 		ThrownExpBottle bottle = event.getEntity();
 		LivingEntity shooter = bottle.getShooter();
 		if(event.getExperience() == 0 || !(shooter instanceof Player)){
 			return;
 		}
 
 		Player player = (Player) shooter;
 		boolean illegal = false;
 		boolean isRegion = false;
 		Material item = Material.EXP_BOTTLE;
 		ASConfig c = configFor(bottle.getLocation());
 
 		ProtectionInformation info = ASUtils.isBlocked(player, bottle.getLocation(), c.drop, item, PermissionNodes.PACK_DROP);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region bottleRegion = info.targetRegion;
 
 		if(illegal){
 			event.setExperience(0);
 			event.setShowEffect(false);
 		}
 
 		Action action = Action.ITEM_THROW;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_ITEM_THROW;
 			extra = new String[] {bottleRegion == null ? plugin.getMessages().getMessage("wilderness") : bottleRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onThrow(PlayerDropItemEvent event){
 		Player player = event.getPlayer();
 		boolean illegal = false;
 		boolean isRegion = false;
 		Item drop = event.getItemDrop();
 		Material item = event.getItemDrop().getItemStack().getType();
 		ASConfig c = configFor(drop.getLocation());
 
 		ProtectionInformation info = ASUtils.isBlocked(player, drop.getItemStack(), drop.getLocation(), c.drop, PermissionNodes.PACK_DROP);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region dropRegion = info.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		Action action = Action.ITEM_THROW;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_ITEM_THROW;
 			extra = new String[] {dropRegion == null ? plugin.getMessages().getMessage("wilderness") : dropRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPickup(PlayerPickupItemEvent event){
 		Player player = event.getPlayer();
 		boolean illegal = false;
 		boolean isRegion = false;
 		Item drop = event.getItem();
 		Material item = event.getItem().getItemStack().getType();
 		ASConfig c = configFor(drop.getLocation());
 
 		if(player.hasMetadata(NO_PICKUP_METADATA_KEY)){
 			event.setCancelled(true);
 			return;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(player, drop.getItemStack(), drop.getLocation(), c.pickup, PermissionNodes.PACK_PICKUP);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region dropRegion = info.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		Action action = Action.ITEM_PICKUP;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_ITEM_PICKUP;
 			extra = new String[] {dropRegion == null ? plugin.getMessages().getMessage("wilderness") : dropRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		List<ItemStack> drops = event.getDrops();
 		int illegalItems = 0, amount = 0;
 		ASConfig c = configFor(player.getLocation());
 
 		Region playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 		if(playerRegion != null){
 			playerRegion.alertExit(player);
 		}
 
 		List<ItemStack> r = new ArrayList<ItemStack>();
 		for(ItemStack item : drops){
 			boolean remove = false;
 			ProtectionInformation info = ASUtils.isBlocked(player, item, player.getLocation(), c.death, PermissionNodes.PACK_DEATH);
 			remove = info.illegal;
 			if(remove){
 				r.add(item);
 				illegalItems++;
 				amount += item.getAmount();
 			}
 		}
 		for(ItemStack remove : r){
 			drops.remove(remove);
 		}
 
 		plugin.getMessages().notifyParties(player, Action.ITEM_THROW_DEATH, illegalItems > 0, String.valueOf(illegalItems), String.valueOf(amount));
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		Player player = event.getPlayer();
 		String command = event.getMessage().substring(1).toLowerCase(); // Cut off the slash
 		boolean illegal = false;
 		ASConfig c = configFor(player.getLocation());
 
 		String[] arguments = command.split(" ");
 		if(arguments.length == 0){
 			return;
 		}
 
 		StringBuilder current = new StringBuilder();
 		current.append(arguments[0]).append(" ");
 		for(int i = 0; i < arguments.length; i++){
 			if(c.commands.contains(current.toString().trim())){
 				illegal = true;
 				break;
 			}
 			if(i + 1 < arguments.length){
 				current.append(arguments[i + 1]).append(" ");
 			}
 		}
 
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_COMMANDS, PermissionNodes.DENY_COMMANDS, (Material) null)){
 			illegal = false;
 		}
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		plugin.getMessages().notifyParties(player, Action.COMMAND_USED, illegal, "/" + command);
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onMove(PlayerMoveEvent event){
 		if(event.getTo().getBlock().equals(event.getPlayer().getLocation().getBlock())){
 			return;
 		}
 
 		Player player = event.getPlayer();
 		Region currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		Region toRegion = plugin.getRegionManager().getRegion(event.getTo());
 		if(currentRegion != toRegion){
 			if(currentRegion != null){
 				currentRegion.alertExit(player);
 			}
 			if(toRegion != null){
 				toRegion.alertEntry(player);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		Player player = event.getPlayer();
 		if(player.hasMetadata("antishare-joined")){
 			scheduleGameModeChange(event);
 			player.removeMetadata("antishare-joined", plugin);
 		}else{
 			GameMode from = player.getGameMode();
 			GameMode to = event.getNewGameMode();
 			boolean cancel = doGameModeChange(player, from, to);
 			if(cancel){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onGameModeChangeInventories(PlayerGameModeChangeEvent event){
 		GameModeIdentity.setChangedGameMode(event.getPlayer().getName());
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onCombat(EntityDamageByEntityEvent event){
 		Entity attacker = event.getDamager();
 		Entity target = event.getEntity();
 		boolean illegal = false;
 		boolean isRegion = false, isPlayerCombat = false;
 		Player playerAttacker = getPlayer(attacker);
 		ASConfig c = configFor(target.getLocation());
 
 		if(playerAttacker == null){
 			return;
 		}
 
 		if(target instanceof Player){
 			isPlayerCombat = true;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(playerAttacker, target.getLocation(), c.attackMobs, target.getType(), isPlayerCombat ? PermissionNodes.PACK_COMBAT_PLAYERS : PermissionNodes.PACK_COMBAT_MOBS);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region entityRegion = info.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		Action action = isPlayerCombat ? Action.HIT_PLAYER : Action.HIT_MOB;
 		String name = target instanceof Player ? ((Player) target).getName() : MaterialAPI.capitalize(target.getType().getName());
 		String[] extra = null;
 		if(isRegion){
 			action = isPlayerCombat ? Action.REGION_HIT_PLAYER : Action.REGION_HIT_MOB;
 			extra = new String[] {entityRegion == null ? plugin.getMessages().getMessage("wilderness") : entityRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(playerAttacker, action, illegal, name, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPistonExtend(BlockPistonExtendEvent event){
 		for(Block block : event.getBlocks()){
 			GameMode type = plugin.getBlockManager().getType(block);
 			if(type == null){
 				continue;
 			}
 			Location oldLocation = block.getLocation();
 			Location newLocation = block.getRelative(event.getDirection()).getLocation();
 			plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 		}
 		ASConfig c = configFor(event.getBlock().getLocation());
 		if(c.naturalSettings.breakAsPiston){
 			int dest = event.getLength() + 1; // Destination block
 			Block block = event.getBlock().getRelative(event.getDirection(), dest);
 			if(MaterialAPI.canPistonBreak(block.getType())){
 				block.setMetadata(LOGBLOCK_METADATA_KEY, EMPTY_METADATA);
 				plugin.getHookManager().sendBlockBreak("PISTON", block.getLocation(), block.getType(), block.getData());
 				breakBlock(block, plugin.getBlockManager().getType(block), c, false);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPistonRetract(BlockPistonRetractEvent event){
 		if(!event.isSticky()){
 			return;
 		}
 		Block block = event.getBlock().getRelative(event.getDirection()).getRelative(event.getDirection());
 		GameMode type = plugin.getBlockManager().getType(block);
 		if(type == null){
 			return;
 		}
 		Location oldLocation = block.getLocation();
 		Location newLocation = block.getRelative(event.getDirection().getOppositeFace()).getLocation();
 		plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 	}
 
 	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onBlockFlow(BlockFromToEvent event){
 		Block to = event.getToBlock();
 		if(MaterialAPI.canBeBrokenByWater(to.getType())){
 			to.setMetadata(LOGBLOCK_METADATA_KEY, EMPTY_METADATA);
 			plugin.getHookManager().sendBlockBreak("WATER", to.getLocation(), to.getType(), to.getData());
 			breakBlock(to, plugin.getBlockManager().getType(to), configFor(to.getLocation()), false, true);
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onProjectileLaunch(ProjectileLaunchEvent event){
 		Projectile projectile = event.getEntity();
 		LivingEntity shooter = projectile.getShooter();
 		if(!(shooter instanceof Player)){
 			return;
 		}
 		Player player = (Player) shooter;
 		boolean illegal = false;
 		Material item = Material.AIR;
 		ASConfig c = configFor(player.getLocation());
 
 		if(projectile instanceof EnderPearl){
 			item = Material.ENDER_PEARL;
 		}else if(projectile instanceof EnderSignal){
 			item = Material.EYE_OF_ENDER;
 		}else if(projectile instanceof Snowball){
 			item = Material.SNOW_BALL;
 		}
 
 		if(item == Material.AIR){
 			return;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(player, null, c.drop, item, PermissionNodes.PACK_DROP);
 		illegal = info.illegal;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		plugin.getMessages().notifyParties(player, Action.ITEM_THROW, illegal, item);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPotionSplash(PotionSplashEvent event){
 		ThrownPotion projectile = event.getEntity();
 		LivingEntity shooter = projectile.getShooter();
 		if(!(shooter instanceof Player)){
 			return;
 		}
 
 		Player player = (Player) shooter;
 		boolean illegal = false;
 		Material item = Material.POTION;
 		ASConfig c = configFor(projectile.getLocation());
 
 		if(c.thrownPotions){
 			illegal = true;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(player, null, c.use, item, PermissionNodes.PACK_USE);
 		illegal = info.illegal;
 
 		if(illegal){
 			event.setCancelled(true);
 		}
 
 		plugin.getMessages().notifyParties(player, Action.USE_SOMETHING, illegal, item);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onCrafting(CraftItemEvent event){
 		if(event.getWhoClicked() instanceof Player){
 			boolean illegal = false;
 			Player player = (Player) event.getWhoClicked();
 			ASConfig c = configFor(player.getLocation());
 			if(GamemodeAbstraction.isCreative(player.getGameMode())){
 				if(c.craft.has(event.getRecipe().getResult())){
 					illegal = true;
 				}
 				if(!plugin.isBlocked(player, PermissionNodes.MAKE_ANYTHING, null, event.getRecipe().getResult().getType())){
 					illegal = false;
 				}
 			}
 			if(illegal){
 				event.setCancelled(true);
 			}
 			plugin.getMessages().notifyParties(player, Action.CRAFTED_ITEM, illegal, MaterialAPI.capitalize(event.getRecipe().getResult().getType().name()));
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onHangingBreak(HangingBreakEvent event){
 		if(event.getCause() == RemoveCause.PHYSICS){
 			Hanging hanging = event.getEntity();
 			Location block = hanging.getLocation().getBlock().getRelative(hanging.getAttachedFace()).getLocation();
 			GameMode gamemode = plugin.getBlockManager().getRecentBreak(block);
 			if(gamemode != null && gamemode == GameMode.CREATIVE){
 				event.setCancelled(true);
 				plugin.getHookManager().sendEntityBreak(null, hanging.getLocation(), hanging instanceof ItemFrame ? Material.ITEM_FRAME : Material.PAINTING, BlockLogger.DEFAULT_DATA);
 				hanging.remove();
 				plugin.getBlockManager().removeEntity(hanging);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onHangingPlace(HangingPlaceEvent event){
 		Player player = event.getPlayer();
 		Hanging hanging = event.getEntity();
 		Material item = Material.PAINTING;
 		boolean illegal = false;
 		boolean isRegion = false;
 		ASConfig c = configFor(hanging.getLocation());
 		if(hanging instanceof ItemFrame){
 			item = Material.ITEM_FRAME;
 		}
 
 		ProtectionInformation info = ASUtils.isBlocked(player, hanging.getLocation(), c.blockPlace, item, PermissionNodes.PACK_BLOCK_PLACE);
 		illegal = info.illegal;
 		isRegion = info.isRegion;
 		Region hangingRegion = info.targetRegion;
 
 		if(illegal){
 			event.setCancelled(true);
 		}else{
 			plugin.getBlockManager().addEntity(player.getGameMode(), hanging);
 		}
 
 		Action action = Action.BLOCK_PLACE;
 		String[] extra = null;
 		if(isRegion){
 			action = Action.REGION_BLOCK_PLACE;
 			extra = new String[] {hangingRegion == null ? plugin.getMessages().getMessage("wilderness") : hangingRegion.getName()};
 		}
 		plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onHangingBreak(HangingBreakByEntityEvent event){
 		Entity remover = event.getRemover();
 		Hanging hanging = event.getEntity();
 		Material item = Material.PAINTING;
 		GameMode hangingGamemode = plugin.getBlockManager().getType(hanging);
 		boolean illegal = false;
 		boolean isRegion = false, isGameMode = false;
 		if(hanging instanceof ItemFrame){
 			item = Material.ITEM_FRAME;
 		}
 		if(remover instanceof Player){
 			Player player = (Player) remover;
 			ItemStack hand = player.getItemInHand();
 			ASConfig c = configFor(hanging.getLocation());
 			if(hand == null){
 				hand = new ItemStack(Material.AIR);
 			}
 
 			if(AntiShare.hasPermission(player, PermissionNodes.TOOL_USE) && hand.getDurability() == AntiShare.ANTISHARE_TOOL_DATA){
 				if(hand.getType() == AntiShare.ANTISHARE_TOOL){
 					if(hangingGamemode == null){
 						plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-natural", MaterialAPI.capitalize(item.name())), true);
 					}else{
 						plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-type", MaterialAPI.capitalize(item.name()), MaterialAPI.capitalize(hangingGamemode.name())), true);
 					}
 					event.setCancelled(true);
 					return;
 				}else if(hand.getType() == AntiShare.ANTISHARE_SET_TOOL){
 					plugin.getBlockManager().removeEntity(hanging);
 					plugin.getBlockManager().addEntity(player.getGameMode(), hanging);
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("block-set", MaterialAPI.capitalize(player.getGameMode().name())), true);
 					event.setCancelled(true);
 					return;
 				}else if(hand.getType() == AntiShare.ANTISHARE_CUBOID_TOOL && AntiShare.hasPermission(player, PermissionNodes.CREATE_CUBOID)){
 					plugin.getCuboidManager().updateCuboid(player.getName(), CuboidPoint.POINT1, hanging.getLocation());
 					Cuboid cuboid = plugin.getCuboidManager().getCuboid(player.getName());
 					int volume = cuboid == null ? 0 : cuboid.getVolume();
 					plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("cuboid-updated", "1", String.valueOf(volume)), true);
 					event.setCancelled(true);
 					return;
 				}
 			}
 
 			ProtectionInformation info = ASUtils.isBlocked(player, hanging.getLocation(), c.blockBreak, item, PermissionNodes.PACK_BLOCK_BREAK);
 			illegal = info.illegal;
 			isRegion = info.isRegion;
 			Region hangingRegion = info.targetRegion;
 
 			InteractionSettings i = null;
 			if(hangingGamemode != null && !AntiShare.hasPermission(player, PermissionNodes.FREE_PLACE)){
 				if(!GamemodeAbstraction.isMatch(hangingGamemode, player.getGameMode())){
 					i = configFor(player.getGameMode(), hangingGamemode, hanging.getLocation());
 					isGameMode = true;
 				}
 			}
 
 			if(!illegal && i != null){
 				illegal = i.deny;
 			}
 
 			if(illegal){
 				event.setCancelled(true);
 			}else{
 				plugin.getBlockManager().removeEntity(hanging);
 			}
 
 			Action action = Action.BLOCK_BREAK;
 			String[] extra = null;
 			if(isRegion){
 				action = Action.REGION_BLOCK_BREAK;
 				extra = new String[] {hangingRegion == null ? plugin.getMessages().getMessage("wilderness") : hangingRegion.getName()};
 			}else if(isGameMode){
 				action = Action.GAMEMODE_BLOCK_BREAK;
 				extra = new String[] {MaterialAPI.capitalize(hangingGamemode.name())};
 			}
 			plugin.getMessages().notifyParties(player, action, illegal, item, extra);
 
 			if(i != null && !event.isCancelled() && isGameMode){
 				plugin.getHookManager().sendEntityBreak(player.getName(), hanging.getLocation(), item, BlockLogger.DEFAULT_DATA);
 				if(i.drop){
 					hanging.getWorld().dropItemNaturally(hanging.getLocation(), new ItemStack(item));
 					if(hanging instanceof ItemFrame){
 						ItemFrame frame = (ItemFrame) hanging;
 						hanging.getWorld().dropItemNaturally(hanging.getLocation(), frame.getItem());
 					}
 				}
 				plugin.getBlockManager().removeEntity(hanging);
 				hanging.remove();
 			}
 		}else{
 			if(GamemodeAbstraction.isCreative(hangingGamemode)){
 				event.setCancelled(true);
 				hanging.remove();
 				plugin.getBlockManager().removeEntity(hanging);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		player.setMetadata("antishare-joined", new FixedMetadataValue(plugin, true));
 
 		// Tell the inventory manager to prepare this player
 		plugin.getInventoryManager().loadPlayer(player.getName());
 
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
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		player.removeMetadata("antishare-joined", plugin);
 
 		// Remove from regions
 		Region region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertExit(player);
 		}
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().unloadPlayer(player);
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onWorldChange(PlayerChangedWorldEvent event){
 		Player player = event.getPlayer();
 		World to = player.getWorld();
 		World from = event.getFrom();
 		boolean ignore = true;
 
 		// Check to see if we should even bother checking
 		if(!plugin.settings().perWorldInventories){
 			// Fix up inventories
 			plugin.getInventoryManager().mergeAllWorlds(player, from); // Internal save
 			return;
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		// Inventory check
 		if(!AntiShare.hasPermission(player, PermissionNodes.NO_SWAP)){
 			plugin.getInventoryManager().onWorldChange(player, from);
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		if(!ignore){
 			plugin.getMessages().notifyParties(player, Action.WORLD_CHANGE, false, to.getName());
 		}
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		Player player = event.getPlayer();
 		Region currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		Region toRegion = plugin.getRegionManager().getRegion(event.getTo());
 		boolean illegal = false;
 		ASConfig c = configFor(event.getFrom());
 
 		// Check teleport cause for ender pearl
 		Material pearl = Material.ENDER_PEARL;
 		if(event.getCause() == TeleportCause.ENDER_PEARL){
 			if(c.use.has(pearl)){
 				illegal = true;
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, PermissionNodes.DENY_USE, pearl)){
 				illegal = false;
 			}
 		}
 
 		// Check type
 		if(illegal){
 			event.setCancelled(true);
 
 			plugin.getMessages().notifyParties(player, Action.USE_SOMETHING, illegal, pearl, MaterialAPI.capitalize(pearl.name()));
 
 			// Kill off before region check
 			return;
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
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBurn(BlockBurnEvent event){
 		plugin.getBlockManager().removeBlock(event.getBlock());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onFade(BlockFadeEvent event){
 		plugin.getBlockManager().removeBlock(event.getBlock());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onDecay(LeavesDecayEvent event){
 		plugin.getBlockManager().removeBlock(event.getBlock());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onSpread(BlockSpreadEvent event){
 		if(!configFor(event.getBlock().getLocation()).naturalSettings.spreading){
 			return;
 		}
 		Block source = event.getSource();
 		GameMode sourceGamemode = plugin.getBlockManager().getType(source);
 		if(sourceGamemode != null){
 			plugin.getBlockManager().addBlock(sourceGamemode, event.getBlock());
 		}
 	}
 
 	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onItemMove(InventoryMoveItemEvent event){
 		if(event.getSource() == null || event.getDestination() == null){
 			return;
 		}
 		InventoryHolder holderSource = event.getSource().getHolder();
 		InventoryHolder holderDestination = event.getDestination().getHolder();
 		Location sourceLocation = ASUtils.getLocation(holderSource);
 		Location destinationLocation = ASUtils.getLocation(holderDestination);
 		if(sourceLocation == null || destinationLocation == null){
 			return;
 		}
 		GameMode source = plugin.getBlockManager().getType(sourceLocation.getBlock());
 		GameMode destination = plugin.getBlockManager().getType(destinationLocation.getBlock());
 		ASConfig config = configFor(sourceLocation);
 		if(config != null && config.naturalSettings.spreading){
 			if(source != null && destination != null && !GamemodeAbstraction.isMatch(source, destination)){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 }
