 package com.TeamNovus.Supernaturals.Listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Supernaturals;
 import com.TeamNovus.Supernaturals.Events.PlayerClassChangeEvent;
 import com.TeamNovus.Supernaturals.Events.PlayerManaChangeEvent;
 import com.TeamNovus.Supernaturals.Models.ItemBag;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 import com.TeamNovus.Supernaturals.Player.Class.Power;
 import com.TeamNovus.Supernaturals.Player.Statistics.Cooldown;
 import com.comphenix.taghelper.ReceiveNameTagEvent;
 import com.comphenix.taghelper.TagHelperMod;
 
 public class PlayerListener implements Listener {
 	
 	@EventHandler
 	public void onRecieveNameTag(ReceiveNameTagEvent event) {
 		SNPlayer player = SNPlayers.i.get(event.getWatched());

 		event.setTag(player.getPlayerClass().getColor() + event.getWatched().getName());
 	}
 	
 	@EventHandler
 	public void onPlayerClassChange(final PlayerClassChangeEvent event) {
 		final TagHelperMod tagHelper = (TagHelperMod) Bukkit.getPluginManager().getPlugin("TagHelper");
 
 		if(tagHelper != null) {
 			Bukkit.getScheduler().runTaskLater(Supernaturals.getPlugin(), new Runnable() {
 				
 				@Override
 				public void run() {
 					tagHelper.refreshPlayer(event.getPlayer());		
 				}
 			}, 1);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerManaChange(PlayerManaChangeEvent event) {			
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 		
 		if(player.getMana() < player.getMaxMana()) {
 			if(player.getMana() + event.getAmount() > player.getMaxMana()) {
 				event.setAmount(player.getMaxMana() - player.getMana());
 			}
 			
 			if(player.isVerbose()) {
 				player.sendMessage(ChatColor.GREEN + "+ " + event.getAmount() + " mana!");
 			}			
 		} else {
 			event.setAmount(0);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(final PlayerLoginEvent event) {
 		SNPlayers.i.get(event.getPlayer());
 		
 		Bukkit.getServer().getScheduler().runTaskAsynchronously(Supernaturals.getPlugin(), new Runnable() {
 			
 			public void run() {
 				SNPlayer player = SNPlayers.i.get(event.getPlayer());
 				
 				if(player.isOnline()) {
 					player.syncFields(false);
 					player.updateGUI();
 				}
 			}
 		});
 	}
 	
 	@EventHandler
 	public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
 		SNPlayers.i.get(event.getPlayer());
 		
 		Bukkit.getServer().getScheduler().runTaskAsynchronously(Supernaturals.getPlugin(), new Runnable() {
 			
 			public void run() {
 				SNPlayer player = SNPlayers.i.get(event.getPlayer());
 				
 				if(player.isOnline()) {
 					player.syncFields(false);
 					player.updateGUI();
 				}
 			}
 		});
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {	
 		// Bind all Spells to the BLAZE_ROD
 		if (!(event.getPlayer().getItemInHand().getData().getItemType().equals(Material.BLAZE_ROD)))
 			return;
 
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 		
 		if(Supernaturals.getPlugin().getConfig().getStringList("settings.disabled-worlds").contains(player.getPlayer().getWorld().getName().toLowerCase())) {
 			player.sendMessage(ChatColor.RED + "Supernaturals is disabled in " + ChatColor.YELLOW + player.getPlayer().getWorld().getName() + ChatColor.RED + "!");
 			return;
 		}
 		
 		// Bind/Switch:
 		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
 				event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 
 			player.setNextBinding();
 
 			if (player.getSelectedPower() != null) {
 				player.sendMessage(ChatColor.GREEN + "Wand bound to " + ChatColor.YELLOW + player.getSelectedPower().getName() + ChatColor.GREEN + "!");
 			}
 		}
 
 		// Cast:
 		if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
 			if (player.getSelectedPower() != null) {
 				Power power = player.getSelectedPower();
 								
 				if(player.getRemainingCooldownTicks(power) > 0) {
 					player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.YELLOW + player.getRemainingCooldownTicks(power) / 20.0 + ChatColor.RED + " seconds to cast this spell!");
 				} else if (power.getRequired().has(player)) {
 					if (power.cast(player)) {
 						power.getConsume().consume(player);
 						player.setCooldown(new Cooldown(power, power.getCooldown()));
 					}
 				} else {
 					player.sendMessage(ChatColor.BLUE + "Requires:");
 					if (power.getRequired().getExpCost() != 0)
 						player.sendMessage(ChatColor.BLUE + "   Experience: " + ChatColor.YELLOW + power.getRequired().getExpCost());
 					if (power.getRequired().getHealthCost() != 0)
 						player.sendMessage(ChatColor.BLUE + "   Health: " + ChatColor.YELLOW + power.getRequired().getHealthCost());
 					if (power.getRequired().getHungerCost() != 0)
 						player.sendMessage(ChatColor.BLUE + "   Hunger: " + ChatColor.YELLOW + power.getRequired().getHungerCost());
 					if (power.getRequired().getManaCost() != 0)
 						player.sendMessage(ChatColor.BLUE + "   Mana: " + ChatColor.YELLOW + power.getRequired().getManaCost());
 					if (power.getRequired().getItemBagCost() != new ItemBag())
 						player.sendMessage(ChatColor.BLUE + "   Items: " + ChatColor.YELLOW + power.getRequired().getItemBagCost().toString());
 				}
 			} else {
 				player.sendMessage(ChatColor.RED + "Your wand is not bound to a power!");
 			}
 		}
 	}
 
 }
