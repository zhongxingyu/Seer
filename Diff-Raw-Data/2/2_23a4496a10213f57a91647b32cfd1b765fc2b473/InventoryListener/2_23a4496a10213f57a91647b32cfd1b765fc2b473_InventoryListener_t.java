 package com.turt2live.antishare.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.manager.InventoryManager;
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.materials.ServerHas;
 
 public class InventoryListener implements Listener {
 
 	private AntiShare plugin = AntiShare.getInstance();
 	private InventoryManager manager;
 
 	public InventoryListener(InventoryManager manager){
 		this.manager = manager;
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
 			manager.fixInventory(player, event.getFrom());
 			return;
 		}
 
 		// Check temp
 		if(manager.isInTemporary(player)){
 			manager.removeFromTemporary(player);
 		}
 
 		// Inventory check
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (player.getGameMode()){
 			case CREATIVE:
 				manager.saveCreativeInventory(player, from);
 				manager.saveEnderCreativeInventory(player, from);
 				break;
 			case SURVIVAL:
 				manager.saveSurvivalInventory(player, from);
 				manager.saveEnderSurvivalInventory(player, from);
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(player.getGameMode() == GameMode.ADVENTURE){
 						manager.saveAdventureInventory(player, from);
 						manager.saveEnderAdventureInventory(player, from);
 					}
 				}
 				break;
 			}
 
 			// Check for linked inventories
 			manager.checkLinks(player, to, from);
 
 			// Update the inventories (check for merges)
 			manager.refreshInventories(player, true);
 
 			// Set to
 			switch (player.getGameMode()){
 			case CREATIVE:
 				manager.getCreativeInventory(player, to).setTo(player);
 				manager.getEnderCreativeInventory(player, to).setTo(player); // Sets to the ender chest, not the player
 				break;
 			case SURVIVAL:
 				manager.getSurvivalInventory(player, to).setTo(player);
 				manager.getEnderSurvivalInventory(player, to).setTo(player); // Sets to the ender chest, not the player
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(player.getGameMode() == GameMode.ADVENTURE){
 						manager.getAdventureInventory(player, to).setTo(player);
 						manager.getEnderAdventureInventory(player, to).setTo(player); // Sets to the ender chest, not the player
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
 
 	// ################# Player Quit
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 
 		// Tell the inventory manager to release this player
 		manager.releasePlayer(player);
 	}
 
 	// ################# Player Join
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 
 		// Tell the inventory manager to prepare this player
 		manager.loadPlayer(player);
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
 
 		// Check to see if we should even bother
 		if(!plugin.getConfig().getBoolean("handled-actions.gamemode-inventories")){
 			return;
 		}
 
 		// Check temp
 		if(manager.isInTemporary(player)){
 			manager.removeFromTemporary(player);
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (from){
 			case CREATIVE:
 				manager.saveCreativeInventory(player, player.getWorld());
 				manager.saveEnderCreativeInventory(player, player.getWorld());
 				break;
 			case SURVIVAL:
 				manager.saveSurvivalInventory(player, player.getWorld());
 				manager.saveEnderSurvivalInventory(player, player.getWorld());
 				break;
 			default:
 				if(ServerHas.adventureMode()){
 					if(from == GameMode.ADVENTURE){
 						manager.saveAdventureInventory(player, player.getWorld());
 						manager.saveEnderAdventureInventory(player, player.getWorld());
 					}
 				}
 				break;
 			}
 
 			// Update inventories
 			manager.refreshInventories(player, true);
 
 			// Set to
 			switch (to){
 			case CREATIVE:
 				manager.getCreativeInventory(player, player.getWorld()).setTo(player);
 				manager.getEnderCreativeInventory(player, player.getWorld()).setTo(player);
 				break;
 			case SURVIVAL:
 				manager.getSurvivalInventory(player, player.getWorld()).setTo(player);
 				manager.getEnderSurvivalInventory(player, player.getWorld()).setTo(player);
 				break;
 			default:
 				if(ServerHas.adventureMode()){
					if(to == GameMode.ADVENTURE){
 						manager.getAdventureInventory(player, player.getWorld()).setTo(player);
 						manager.getEnderAdventureInventory(player, player.getWorld()).setTo(player);
 					}
 				}
 				break;
 			}
 		}else{
 			return;
 		}
 
 		// Alerts
 		String message = "no message";
 		String playerMessage = "Your inventory has been changed to " + ChatColor.YELLOW + to.name();
 		if(!plugin.getConfig().getBoolean("other.send-gamemode-change-message")){
 			playerMessage = "no message";
 		}
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 }
