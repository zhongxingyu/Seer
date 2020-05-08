 package com.turt2live.antishare.listener;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Jukebox;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.manager.BlockManager;
 import com.turt2live.antishare.manager.HookManager;
 import com.turt2live.antishare.money.Tender.TenderType;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.notification.MessageFactory;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.tekkitcompat.EntityLayer;
 import com.turt2live.antishare.tekkitcompat.ServerHas;
 import com.turt2live.antishare.util.ASUtils;
 
 public class BlockListener implements Listener {
 
 	private BlockManager blocks;
 	private AntiShare plugin = AntiShare.getInstance();
 
 	public BlockListener(BlockManager manager){
 		blocks = manager;
 	}
 
 	// ################# Chunk Load
 
 	@EventHandler
 	public void onChunkLoad(ChunkLoadEvent event){
 		blocks.loadChunk(event.getChunk());
 	}
 
 	// ################# Chunk Unload
 
 	@EventHandler
 	public void onChunkUnload(ChunkUnloadEvent event){
 		blocks.unloadChunk(event.getChunk());
 	}
 
 	// ################# GameMode Block Break
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onGameModeBlockBreak(BlockBreakEvent event){
 		if(!event.isCancelled()){
 			blocks.removeBlock(event.getBlock());
 		}
 	}
 
 	// ################# Block Place
 
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		GameMode existing = null;
 		AlertType type = AlertType.LEGAL;
 		boolean handle = false;
 		if(!event.isCancelled() && plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.disable-placing-mixed-gamemode")){
 			Block source = event.getBlockAgainst();
 			Block relative = event.getBlockPlaced();
 			if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 				GameMode potentialNewGM = player.getGameMode();
 				if(ASUtils.isDroppedOnBreak(relative, source)){
 					handle = true;
 					existing = blocks.getType(source);
 					if(existing != null){
 						if(existing != potentialNewGM){
 							event.setCancelled(plugin.shouldCancel(player, true));
 							type = AlertType.ILLEGAL;
 						}
 					}
 				}
 			}
 		}
 		if(!handle){
 			return;
 		}
 		Block block = event.getBlock();
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to attach " : " attached ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " onto a " + (existing != null ? existing.name().toLowerCase() : "natural") + " block";
 		String playerMessage = plugin.getMessage("blocked-action.attach-block");
 		MessageFactory factory = new MessageFactory(playerMessage);
 		factory.insert(block, player, block.getWorld(), TenderType.BLOCK_PLACE);
 		playerMessage = factory.toString();
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 	}
 
 	// ################# GameMode Block Place
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onGameModePlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		if(!event.isCancelled() && !plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 			blocks.addBlock(player.getGameMode(), event.getBlock());
 			Block second = ASUtils.multipleBlocks(event.getBlock());
 			if(second != null){
 				blocks.addBlock(player.getGameMode(), second);
 			}
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
 			if(blocks.getType(to) == GameMode.CREATIVE){
 				if(deny){
 					event.setCancelled(plugin.shouldCancel(null, true));
 				}else if(!drops){
 					to.setType(Material.AIR);
 				}
 				blocks.removeBlock(to);
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
 			if(blocks.getType(block) == GameMode.CREATIVE){
 				if(deny){
 					event.blockList().remove(i);
 				}else if(!drops){
 					block.setType(Material.AIR);
 					event.blockList().remove(i);
 				}
 				blocks.removeBlock(block);
 			}
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
 		GameMode type = blocks.getType(block);
 
 		// Sanity
 		if(type == null){
 			return;
 		}
 
 		// Setup
 		Location oldLocation = block.getLocation();
 		Location newLocation = block.getRelative(event.getDirection().getOppositeFace()).getLocation();
 
 		// Move
 		blocks.moveBlock(oldLocation, newLocation);
 	}
 
 	// ################# Piston Move (Extend)
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onPistonExtend(BlockPistonExtendEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		for(Block block : event.getBlocks()){
 			// Check for block type
 			GameMode type = blocks.getType(block);
 
 			// Sanity
 			if(type == null){
 				continue;
 			}
 
 			// Setup
 			Location oldLocation = block.getLocation();
 			Location newLocation = block.getRelative(event.getDirection()).getLocation();
 
 			// Move
 			blocks.moveBlock(oldLocation, newLocation);
 		}
 	}
 
 	// ################# Player Interact Entity
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onItemFrameClick(PlayerInteractEntityEvent event){
 		if(!event.isCancelled()
 				&& event.getPlayer().getItemInHand() != null
 				&& (event.getPlayer().getItemInHand().getType() == AntiShare.ANTISHARE_TOOL || event.getPlayer().getItemInHand().getType() == AntiShare.ANTISHARE_SET_TOOL)
 				&& plugin.getPermissions().has(event.getPlayer(), PermissionNodes.TOOL_USE)){
 			Entity entity = event.getRightClicked();
 			GameMode mode = blocks.getType(entity);
 			Material item = Material.AIR;
 			if(ServerHas.mc14xEntities() && entity.getType() == EntityType.ITEM_FRAME){
 				item = Material.ITEM_FRAME;
 			}else if(entity.getType() == EntityType.PAINTING){
 				item = Material.PAINTING;
 			}
 			if(item != Material.AIR){
 				if(event.getPlayer().getItemInHand().getType() == AntiShare.ANTISHARE_SET_TOOL){
 					blocks.removeEntity(entity);
 					ASUtils.sendToPlayer(event.getPlayer(), ChatColor.RED + ASUtils.capitalize(item.name()) + " " + ChatColor.DARK_RED + "REMOVED" + ChatColor.RED + ". (was " + ChatColor.DARK_RED + (mode == null ? "natural" : mode.name()) + ChatColor.RED + ")", true);
 					event.setCancelled(plugin.shouldCancel(event.getPlayer(), true));
 					return;
 				}else{
 					ASUtils.sendToPlayer(event.getPlayer(), ChatColor.WHITE + "That " + ChatColor.YELLOW + ASUtils.capitalize(item.name()) + ChatColor.WHITE + " is " + ChatColor.YELLOW + (mode != null ? mode.name().toLowerCase() : "natural"), true);
 					event.setCancelled(plugin.shouldCancel(event.getPlayer(), true));
 					return;
 				}
 			}
 		}
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
 		GameMode egm = blocks.getType(entity);
 
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
 
 	// ################# Player Interact Block
 
 	@EventHandler (priority = EventPriority.LOW)
 	public void onInteract(PlayerInteractEvent event){
 		if(event.isCancelled() || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR){
 			return;
 		}
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		Action action = event.getAction();
 
 		// Check for AntiShare tool
 		if(plugin.getPermissions().has(player, PermissionNodes.TOOL_USE) && player.getItemInHand() != null
 				&& (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)){
 			if(player.getItemInHand().getType() == AntiShare.ANTISHARE_TOOL){
 				String blockname = block.getType().name().replaceAll("_", " ").toLowerCase();
 				String gamemode = (blocks.getType(block) != null ? blocks.getType(block).name() : "natural").toLowerCase();
 				ASUtils.sendToPlayer(player, "That " + ChatColor.YELLOW + blockname + ChatColor.WHITE + " is a " + ChatColor.YELLOW + gamemode + ChatColor.WHITE + " block.", true);
 
 				// Cancel and stop the check
 				event.setCancelled(plugin.shouldCancel(player, true));
 				return;
 			}else if(player.getItemInHand().getType() == AntiShare.ANTISHARE_SET_TOOL){
 				GameMode gm = blocks.getType(block);
 				switch (action){
 				case LEFT_CLICK_BLOCK:
 					if(gm != null){
 						blocks.removeBlock(block);
 					}
 					blocks.addBlock(player.getGameMode(), block);
 					ASUtils.sendToPlayer(player, ChatColor.GREEN + "Block set as " + ChatColor.DARK_GREEN + player.getGameMode().name(), true);
 					break;
 				case RIGHT_CLICK_BLOCK:
 					blocks.removeBlock(block);
 					ASUtils.sendToPlayer(player, ChatColor.RED + "Block " + ChatColor.DARK_RED + "REMOVED" + ChatColor.RED + ". (was " + ChatColor.DARK_RED + (gm == null ? "natural" : gm.name()) + ChatColor.RED + ")", true);
 					break;
 				}
 				event.setCancelled(plugin.shouldCancel(player, true));
 				return;
 			}
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
 		Boolean drops = null;
 		boolean deny = false;
 		AlertType type = AlertType.LEGAL;
 		String blockGM = "Unknown";
 		boolean extraSpecial = false;
 		String attachedGM = "Unknown";
 		Material attached = Material.AIR;
 
 		// Check hooks
 		if(((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForSignProtection(block) || ((HookManager) plugin.getSystemsManager().getManager(Manager.HOOK)).checkForRegion(player, block)){
 			return; // Don't handle any further, let the other plugin handle it
 		}
 
 		// Check creative/survival blocks
 		if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 			GameMode blockGamemode = blocks.getType(block);
 			if(blockGamemode != null){
 				blockGM = blockGamemode.name().toLowerCase();
 				String oGM = player.getGameMode().name().toLowerCase();
 				if(player.getGameMode() != blockGamemode){
 					deny = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.deny");
 					drops = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.block-drops");
 					if(deny){
 						type = AlertType.ILLEGAL;
 					}
 				}else if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.disable-breaking-mixed-gamemode")){
 					for(BlockFace face : ASUtils.realFaces){
 						Block rel = block.getRelative(face);
 						if(ASUtils.isDroppedOnBreak(rel, block)){
 							GameMode relGamemode = blocks.getType(rel);
 							if(relGamemode != null){
 								attachedGM = relGamemode.name().toLowerCase();
 								if(relGamemode != blockGamemode){
 									extraSpecial = true;
 									deny = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + attachedGM + "-blocks.deny");
 									drops = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + attachedGM + "-blocks.block-drops");
 									if(deny){
 										type = AlertType.ILLEGAL;
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
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(plugin.shouldCancel(player, false));
 		}
 
 		// Alert
 		if(extraSpecial){
 			String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break the attached " + attachedGM + " block " : " broke the attached " + attachedGM + " block ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + attached.name().replace("_", " ");
 			String specialPlayerMessage = plugin.getMessage("blocked-action." + attachedGM + "-attached-block-break");
 			MessageFactory factory = new MessageFactory(specialPlayerMessage);
 			factory.insert(block, player, block.getWorld(), attachedGM.equalsIgnoreCase("creative") ? TenderType.CREATIVE_BLOCK : (attachedGM.equalsIgnoreCase("survival") ? TenderType.SURVIVAL_BLOCK : TenderType.ADVENTURE_BLOCK));
 			specialPlayerMessage = factory.toString();
 			plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, type, (attachedGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : (attachedGM.equalsIgnoreCase("survival") ? AlertTrigger.SURVIVAL_BLOCK : AlertTrigger.ADVENTURE_BLOCK)));
 		}else{
 			String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break the " + blockGM + " block " : " broke the " + blockGM + " block ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			String specialPlayerMessage = plugin.getMessage("blocked-action." + blockGM + "-block-break");
 			MessageFactory factory = new MessageFactory(specialPlayerMessage);
 			factory.insert(block, player, block.getWorld(), blockGM.equalsIgnoreCase("creative") ? TenderType.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? TenderType.SURVIVAL_BLOCK : TenderType.ADVENTURE_BLOCK));
 			specialPlayerMessage = factory.toString();
 			plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, type, (blockGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : (blockGM.equalsIgnoreCase("survival") ? AlertTrigger.SURVIVAL_BLOCK : AlertTrigger.ADVENTURE_BLOCK)));
 		}
 
 		// Handle drops
 		if(drops != null && !deny){
 			if(drops){
 				blocks.removeBlock(block);
 				block.breakNaturally();
 			}else{
 				blocks.removeBlock(block);
 				block.setType(Material.AIR);
 			}
 		}
 
 		// Check for 'attached' blocks
 		if(player.getGameMode() == GameMode.SURVIVAL && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING) && !event.isCancelled()){
 			for(BlockFace face : ASUtils.realFaces){
 				Block rel = block.getRelative(face);
 				if(ASUtils.isDroppedOnBreak(rel, block)){
 					if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.break-as-gamemode")){
 						GameMode gm = blocks.getType(rel);
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
 						}else{
 							rel.breakNaturally();
 						}
 					}else{
 						rel.breakNaturally();
 					}
 					blocks.removeBlock(rel);
 				}
 			}
 		}
 
 		// Check for 'attached' blocks and internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING) && !event.isCancelled()){
 			// Check inventories
 			if(plugin.getListener().getConfig(block.getWorld()).clearBlockInventoryOnBreak()){
 				if(block.getState() instanceof Chest){
 					Chest state = (Chest) block.getState();
 					state.getBlockInventory().clear();
 				}else if(block.getState() instanceof Jukebox){
 					Jukebox state = (Jukebox) block.getState();
 					state.setPlaying(null);
					// TODO: BROKEN. Requires Bukkit fix
 				}else if(block.getState() instanceof Furnace){
 					Furnace state = (Furnace) block.getState();
 					state.getInventory().clear();
 				}else if(block.getState() instanceof BrewingStand){
 					BrewingStand state = (BrewingStand) block.getState();
 					state.getInventory().clear();
 				}
 			}
 
 			// Check for attached blocks
 			if(plugin.getListener().getConfig(block.getWorld()).removeAttachedBlocksOnBreak() && plugin.getConfig().getBoolean("enabled-features.no-drops-when-block-break.paintings-are-attached")){
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
 			}
 
 			if(plugin.getListener().getConfig(block.getWorld()).removeAttachedBlocksOnBreak()){
 				for(BlockFace face : ASUtils.realFaces){
 					Block rel = block.getRelative(face);
 					if(ASUtils.isDroppedOnBreak(rel, block)){
 						if(plugin.getConfig().getBoolean("enabled-features.attached-blocks-settings.break-as-gamemode")){
 							GameMode gm = blocks.getType(rel);
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
 							}else{
 								rel.setType(Material.AIR);
 							}
 						}else{
 							rel.setType(Material.AIR);
 						}
 						blocks.removeBlock(rel);
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
 								blocks.removeBlock(rel);
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
 							blocks.removeBlock(above);
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
 							blocks.removeBlock(above);
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
 
 }
