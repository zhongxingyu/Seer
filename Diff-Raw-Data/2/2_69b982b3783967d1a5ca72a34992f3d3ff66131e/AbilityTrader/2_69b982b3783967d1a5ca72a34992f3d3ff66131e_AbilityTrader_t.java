 package com.vxnick.abilitytrader;
 
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 
 public class AbilityTrader extends JavaPlugin {
     public static Economy econ = null;
     public static Permission perms = null;
 	
 	@Override
 	public void onEnable() {
 		if (!setupEconomy() ) {
             getLogger().log(Level.SEVERE, "Vault not found. Disabling AbilityTrader");
             return;
         }
 		
         setupPermissions();
 		saveDefaultConfig();
 		
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {				
 				// Get a list of players and their abilities, and remove expired abilities
 				ConfigurationSection player_section = getConfig().getConfigurationSection("players");
 				
 				if (player_section == null) {
 					return;
 				}
 				
 				Set<String> players = player_section.getKeys(false);
 				
 				for (String player : players) {
 					ConfigurationSection section = getConfig().getConfigurationSection(String.format("players.%s", player));
 					
 					if (section == null) {
 						continue;
 					}
 					
 					Set<String> abilities = section.getKeys(false);
 										
 					for (String ability : abilities) {
 						Integer expires_at = getConfig().getInt(String.format("players.%s.%s.expires_at", player, ability), 0);
 
 						if (expires_at > 0 && expires_at < getUnixTime()) {
 							// Get a list of permissions for the given ability
 							List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 							
 							for (String permission : permissions) {
 								// Remove the permission if the player has it
 								if (perms.has((String) null, player, permission)) {
 									// Apply this to all worlds
 									perms.playerRemove((String) null, player, permission);
 									getConfig().set(String.format("players.%s.%s", player, ability), null);
 								}
 							}
 						}
 					}
 				}
				
				saveConfig();
 			}
 		}, 40L, 1200L);
 	}
 	
 	@Override
 	public void onDisable() {
 
 	}
 	
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
 		
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         
         if (rsp == null) {
             return false;
         }
         
         econ = rsp.getProvider();
         
         return econ != null;
 	}
 	
 	private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         
         perms = rsp.getProvider();
         
         return perms != null;
     }
 	
 	private boolean getAbility(Player player, String ability) {
 		// Get a list of permissions for the given ability
 		List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 		
 		for (String permission : permissions) {
 			// Add the permission if the player doesn't already have it
 			if (!perms.has(player, permission)) {
 				// Apply this to all worlds
 				perms.playerAdd((String) null, (String) player.getName(), permission);
 			}
 		}
 		
 		// Get a list of commands to be run for the given ability
 		List<String> commands = getConfig().getStringList(String.format("abilities.%s.commands", ability));
 		
 		for (String command : commands) {
 			// Run the command
 			getServer().dispatchCommand(null, command);
 		}
 		
 		// Record this player's abilities in the configuration
 		getConfig().set(String.format("players.%s.%s.given_at", player.getName(), ability), System.currentTimeMillis() / 1000L);
 		
 		// Get duration (expiry) for this ability
 		Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 		getConfig().set(String.format("players.%s.%s.expires_at", player.getName(), ability), (duration > 0 ? getUnixTime() + duration : 0));
 		
 		saveConfig();
 		return true;
 	}
 	
 	private int getUnixTime() {
 		return (int) (System.currentTimeMillis() / 1000L);
 	}
 	
 	private String formatDuration(Integer seconds) {
 		// Very simple/hacky way to get a relatively nice format for second values
 		if (seconds == 0) {
 			return "never";
 		} else if (seconds <= 60) {
 			return String.format("%d seconds", seconds);
 		} else if (seconds <= 3600) {
 			return String.format("%d minutes", (seconds / 60));
 		} else if (seconds <= 86400) {
 			return String.format("%d days", (seconds / 3600));
 		}
 		
 		return null;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("ability")) {	
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Sorry, this isn't compatible with the console just yet");
 				return true;
 			}
 			
 			if (!perms.has(sender, "abilitytrader.use")) {
 				sender.sendMessage(ChatColor.RED + "Sorry, you don't have access to Ability Trader");
 				return false;
 			}
 			
 			if (args.length == 0) {
 				return false;
 			}
 			
 			String command = args[0].toLowerCase();
 			Player player = (Player) sender;
 			
 			if (perms.has(player, "abilitytrader.admin")) {
 				if (command.equals("reload")) {
 					player.sendMessage(ChatColor.YELLOW + "Reloading configuration");
 					this.reloadConfig();
 					return true;
 				}
 			}
 			
 			if (command.equals("list")) {
 				
 				ConfigurationSection section = getConfig().getConfigurationSection("abilities");
 				
 				if (section == null) {
 					player.sendMessage(ChatColor.YELLOW + "There are no abilities available!");
 					return true;
 				}
 				
 				Set<String> sections = section.getKeys(false);
 				
 				player.sendMessage(ChatColor.GOLD + "Available Abilities");
 				
 				for (String ability : sections) {
 					String description = getConfig().getString(String.format("abilities.%s.description", ability), "No description");
 					String type = getConfig().getString(String.format("abilities.%s.type", ability), "buy");
 					Integer cost = getConfig().getInt(String.format("abilities.%s.cost", ability), 0);
 					Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 
 					player.sendMessage(ChatColor.YELLOW + String.format("%s -- %s", ability, description));
 					player.sendMessage(String.format("  %s to %s%s", econ.format(cost), type, (type.equals("rent") ? " for " + formatDuration(duration) : "")));
 				}
 			} else if (command.equals("info")) {
 				// Get a list of this player's abilities
 				ConfigurationSection section = getConfig().getConfigurationSection(String.format("players.%s", player.getName()));
 				
 				if (section == null) {
 					player.sendMessage(ChatColor.YELLOW + "You currently have no abilities added");
 					return true;
 				}
 				
 				Set<String> sections = section.getKeys(false);
 				
 				player.sendMessage(ChatColor.GOLD + "My Abilities");
 				
 				for (String ability : sections) {
 					Integer expires_at = getConfig().getInt(String.format("players.%s.%s.expires_at", player.getName(), ability), 0);
 					String time_remaining = expires_at.equals(0) ? "" : String.format("%s remaining", formatDuration(expires_at - getUnixTime()));
 					
 					player.sendMessage(String.format("%s%s", ability, (time_remaining.equals("") ? "" : " -- " + time_remaining)));
 				}
 				
 				
 			} else if (command.equals("get")) {
 				if (args.length == 2) {
 					if (getConfig().getString(String.format("abilities.%s", args[1].toLowerCase())) == null) {
 						player.sendMessage(ChatColor.YELLOW + "Ability not recognised - please check /ability list for available abilities");
 						return true;
 					}
 					
 					String ability = args[1].toLowerCase();
 					Integer cost = getConfig().getInt(String.format("abilities.%s.cost", ability), 0);
 				
 					// Check the player doesn't already have this ability
 					if (getConfig().getString(String.format("players.%s.%s", player.getName(), ability)) != null) {
 						player.sendMessage(ChatColor.YELLOW + "You already have this ability!");
 						return true;
 					}
 					
 					EconomyResponse r = econ.withdrawPlayer(player.getName(), cost);
 				
 					if (r.transactionSuccess()) {
 						if (getAbility(player, ability)) {
 							player.sendMessage(ChatColor.GOLD + String.format("You have been given the '%s' ability!", ability));
 						} else {
 							econ.depositPlayer(player.getName(), cost);
 							player.sendMessage(ChatColor.RED + "Sorry, something went wrong. You have been refunded");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "Sorry, something went wrong. No money has been taken");
 					}
 				}
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Ability Trader Commands");
 				player.sendMessage("/ability info: Show which abilities you have and the time remaining");
 				player.sendMessage("/ability list: List available abilities");
 				player.sendMessage("/ability get <ability>: Buys or rents <ability>");
 			}
 		} else {
 			return false;
 		}
 		
 		return true;
 	}
 }
