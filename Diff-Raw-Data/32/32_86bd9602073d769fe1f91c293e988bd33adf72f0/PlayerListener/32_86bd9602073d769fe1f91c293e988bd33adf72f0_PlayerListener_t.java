 package com.turt2live.antishare.listener;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.MetadataValue;
 
 import com.turt2live.antishare.ASUtils;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.MultiWorld;
 import com.turt2live.antishare.Notification;
 import com.turt2live.antishare.enums.AlertType;
 import com.turt2live.antishare.enums.BlockedType;
 import com.turt2live.antishare.enums.NotificationType;
 import com.turt2live.antishare.storage.VirtualInventory;
 
 public class PlayerListener implements Listener {
 
 	private AntiShare plugin;
 
 	public PlayerListener(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		String commandSent = event.getMessage();
 		Player sender = event.getPlayer();
 		if(plugin.getPermissions().has(sender, "AntiShare.allow.commands", sender.getWorld())){
 			Notification.sendNotification(NotificationType.LEGAL_COMMAND, plugin, sender, commandSent, null);
 			return;
 		}
 		if(plugin.config().onlyIfCreative(sender)){
 			if(sender.getGameMode().equals(GameMode.SURVIVAL)){
 				Notification.sendNotification(NotificationType.LEGAL_COMMAND, plugin, sender, commandSent, null);
 				return;
 			}
 		}
 		if(plugin.storage.commandBlocked(commandSent, sender.getWorld())){
 			ASUtils.sendToPlayer(sender, plugin.getConfig().getString("messages.illegalCommand"));
 			Notification.sendNotification(NotificationType.ILLEGAL_COMMAND, plugin, sender, commandSent, null);
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		Player player = event.getPlayer();
 		if(plugin.getPermissions().has(player, "AntiShare.allow.drop", player.getWorld())){
 			Notification.sendNotification(NotificationType.LEGAL_DROP_ITEM, plugin, player, event.getItemDrop().getItemStack().getType().name(), event.getItemDrop().getItemStack().getType());
 			return;
 		}
 		if(plugin.storage.isBlocked(event.getItemDrop().getItemStack(), BlockedType.DROP_ITEM, player.getWorld())){
 			if(plugin.config().onlyIfCreative(player)){
 				if(player.getGameMode().equals(GameMode.CREATIVE)){
 					event.setCancelled(true);
 					Notification.sendNotification(NotificationType.ILLEGAL_DROP_ITEM, plugin, player, event.getItemDrop().getItemStack().getType().name(), event.getItemDrop().getItemStack().getType());
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.drop_item", player.getWorld()));
 				}else{
 					Notification.sendNotification(NotificationType.LEGAL_DROP_ITEM, plugin, player, event.getItemDrop().getItemStack().getType().name(), event.getItemDrop().getItemStack().getType());
 				}
 			}else{
 				event.setCancelled(true);
 				Notification.sendNotification(NotificationType.ILLEGAL_DROP_ITEM, plugin, player, event.getItemDrop().getItemStack().getType().name(), event.getItemDrop().getItemStack().getType());
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.drop_item", player.getWorld()));
 			}
 		}
 		if(event.isCancelled()){
 			return;
 		}
 		// TODO Waiting on less resource intensive solution
 		//		if(plugin.config().getBoolean("other.cannot_throw_into_regions", player.getWorld())){
 		//			Item item = event.getItemDrop();
 		//			ASRegion region = plugin.getRegionHandler().getRegion(item.getLocation());
 		//			if(!plugin.getPermissions().has(player, "AntiShare.allow.throwIntoRegions")){
 		//				if(plugin.getRegionHandler().isRegion(item.getLocation())){
 		//					event.setCancelled(true);
 		//					Notification.sendNotification(NotificationType.ILLEGAL_ITEM_THROW_INTO_REGION, player, region.getName());
 		//					ASUtils.sendToPlayer(player, plugin.config().getString("messages.throwItemIntoRegion", player.getWorld()));
 		//				}else{
 		//					if(region != null){
 		//						Notification.sendNotification(NotificationType.LEGAL_ITEM_THROW_INTO_REGION, player, region.getName());
 		//					}
 		//				}
 		//			}else{
 		//				if(region != null){
 		//					Notification.sendNotification(NotificationType.LEGAL_ITEM_THROW_INTO_REGION, player, region.getName());
 		//				}
 		//			}
 		//		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
 		Player player = event.getPlayer();
 		if(plugin.config().getBoolean("other.inventory_swap", event.getPlayer().getWorld())
 				&& !plugin.getConflicts().INVENTORY_CONFLICT_PRESENT){
 			if(player != null){
 				if(!plugin.getPermissions().has(player, "AntiShare.noswap", player.getWorld())){
 					plugin.storage.getInventoryManager(player, player.getWorld()).switchInventories(player.getGameMode(), event.getNewGameMode());
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.inventory_swap", player.getWorld()));
 				}
 			}
 		}
 		Notification.sendNotification(NotificationType.GAMEMODE_CHANGE, plugin, player, event.getNewGameMode().toString(), null);
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		if(event.isCancelled() || player == null){
 			return;
 		}
 		if(event.getClickedBlock() == null || event.isCancelled()){
 			return;
 		}
 		// Check for inventory mirror
 		if(event.getClickedBlock().getType().equals(Material.CHEST)){
 			Block block = event.getClickedBlock();
 			Chest chest = (Chest) block.getState();
 			if(chest.hasMetadata("invmirror")){
 				List<MetadataValue> lockedTo = chest.getMetadata("invmirror");
 				for(MetadataValue value : lockedTo){
 					if(value.getOwningPlugin().getName().equalsIgnoreCase("AntiShare")){
 						if(!value.asString().equalsIgnoreCase(player.getName())){
 							ASUtils.sendToPlayer(player, ChatColor.RED + "That is not a normal chest! It cannot be used.");
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}
 		if(!plugin.getPermissions().has(player, "AntiShare.allow.interact", player.getWorld())
 				&& plugin.storage.isBlocked(event.getClickedBlock().getType(), BlockedType.INTERACT, player.getWorld())){
 			if(plugin.config().onlyIfCreative(player)){
 				if(player.getGameMode().equals(GameMode.CREATIVE)){
 					if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.interact", player.getWorld()));
 						Notification.sendNotification(NotificationType.ILLEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name(), event.getClickedBlock().getType());
 					}
 				}else{
					if(ASUtils.isInteractable(event.getClickedBlock().getType())){
						Notification.sendNotification(NotificationType.LEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name(), event.getClickedBlock().getType());
					}
 				}
 			}else{
 				if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 					event.setCancelled(true);
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.interact", player.getWorld()));
 					Notification.sendNotification(NotificationType.ILLEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name(), event.getClickedBlock().getType());
 				}
 			}
 		}else{
 			if(ASUtils.isInteractable(event.getClickedBlock().getType())){
 				Notification.sendNotification(NotificationType.LEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name(), event.getClickedBlock().getType());
 			}
 		}
 		if(event.isCancelled()){
 			return;
 		}
 		boolean skip = false;
 		//Egg check
 		if(plugin.config().getBoolean("other.allow_eggs", player.getWorld()) == false){
 			ItemStack possibleEgg = event.getItem();
 			if(possibleEgg != null){
 				if(possibleEgg.getTypeId() != 383){
 					skip = true;
 				}
 			}else{
 				skip = true;
 			}
 			if(!skip){
 				if(plugin.getPermissions().has(player, "AntiShare.allow.eggs", player.getWorld())){
 					Notification.sendNotification(NotificationType.LEGAL_EGG, plugin, player, "MONSTER EGG", null);
 					return;
 				}
 				// At this point the player is not allowed to use eggs, and we are dealing with an egg
 				if(plugin.config().onlyIfCreative(player)){
 					if(player.getGameMode().equals(GameMode.CREATIVE)){
 						event.setCancelled(true);
 						Notification.sendNotification(NotificationType.ILLEGAL_EGG, plugin, player, "MONSTER EGG", null);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.eggs", player.getWorld()));
 					}else{
 						Notification.sendNotification(NotificationType.LEGAL_EGG, plugin, player, "MONSTER EGG", null);
 					}
 				}else{
 					event.setCancelled(true);
 					Notification.sendNotification(NotificationType.ILLEGAL_EGG, plugin, player, "MONSTER EGG", null);
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.eggs", player.getWorld()));
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEggThrow(PlayerEggThrowEvent event){
 		Player player = event.getPlayer();
 		if(plugin.config().getBoolean("other.allow_eggs", player.getWorld()) == false){
 			if(plugin.getPermissions().has(player, "AntiShare.allow.eggs", player.getWorld())){
 				Notification.sendNotification(NotificationType.LEGAL_EGG, plugin, player, "EGG", null);
 				return;
 			}
 			// At this point the player is not allowed to use eggs, and we are dealing with an egg
 			if(plugin.config().onlyIfCreative(player)){
 				if(player.getGameMode().equals(GameMode.CREATIVE)){
 					event.setHatching(false);
 					Notification.sendNotification(NotificationType.ILLEGAL_EGG, plugin, player, "EGG", null);
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.eggs", player.getWorld()));
 				}else{
 					Notification.sendNotification(NotificationType.LEGAL_EGG, plugin, player, "EGG", null);
 				}
 			}else{
 				event.setHatching(false);
 				Notification.sendNotification(NotificationType.ILLEGAL_EGG, plugin, player, "EGG", null);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.eggs", player.getWorld()));
 			}
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event){
 		plugin.getRegionHandler().checkRegion(event.getPlayer(), event.getTo(), event.getFrom());
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		VirtualInventory manager = plugin.storage.getInventoryManager(player, player.getWorld());
 		manager.makeMatch();
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onExpGain(PlayerExpChangeEvent event){
 		if(!plugin.getPermissions().has(event.getPlayer(), "AntiShare.allow.expGain")){
 			if(plugin.config().onlyIfCreative(event.getPlayer())){
 				if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
 					event.setAmount(0);
 					plugin.getDebugger().alertOverrideDebug(ChatColor.RED + "You cannot gain experience!", event.getPlayer(), AlertType.EXP_GAIN);
 				}
 			}else{
 				plugin.getDebugger().alertOverrideDebug(ChatColor.RED + "You cannot gain experience!", event.getPlayer(), AlertType.EXP_GAIN);
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if(player == null){
 			return;
 		}
 		boolean illegal = false;
 		if(!plugin.getPermissions().has(player, "AntiShare.allow.death", player.getWorld())){
 			boolean doCheck = false;
 			if(plugin.config().onlyIfCreative(player)){
 				if(player.getGameMode().equals(GameMode.CREATIVE)){
 					doCheck = true;
 				}
 			}else{
 				doCheck = true;
 			}
 			if(doCheck){
 				for(ItemStack item : event.getDrops()){
 					if(plugin.storage.isBlocked(item, BlockedType.DEATH, player.getWorld())){
 						illegal = true;
 						item.setAmount(0);
 					}
 				}
 			}
 		}
 		if(illegal){
 			ASUtils.sendToPlayer(player, plugin.config().getString("messages.death", player.getWorld()));
 			Notification.sendNotification(NotificationType.ILLEGAL_DEATH, plugin, player, player.getGameMode().toString(), null);
 		}else{
 			Notification.sendNotification(NotificationType.LEGAL_DEATH, plugin, player, player.getGameMode().toString(), null);
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerPortal(PlayerPortalEvent event){
 		onPlayerTeleport(event);
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(plugin.getConflicts().INVENTORY_CONFLICT_PRESENT
 				|| plugin.getConflicts().WORLD_MANAGER_CONFLICT_PRESENT){
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
 			boolean cancel = !MultiWorld.worldSwap(plugin, player, event.getFrom(), event.getTo());
 			if(cancel){
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.worldSwap", event.getTo().getWorld()));
 				Notification.sendNotification(NotificationType.ILLEGAL_WORLD_CHANGE, plugin, player, event.getTo().getWorld().getName(), null);
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerWorldChange(PlayerChangedWorldEvent event){
 		if(plugin.getConflicts().INVENTORY_CONFLICT_PRESENT
 				|| plugin.getConflicts().WORLD_MANAGER_CONFLICT_PRESENT){
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!plugin.getPermissions().has(player, "AntiShare.worlds", player.getWorld())){
 			plugin.storage.switchInventories(player, event.getFrom(), player.getGameMode(), player.getWorld(), player.getGameMode());
 		}
 		Notification.sendNotification(NotificationType.LEGAL_WORLD_CHANGE, plugin, player, player.getWorld().getName(), null);
 	}
 }
