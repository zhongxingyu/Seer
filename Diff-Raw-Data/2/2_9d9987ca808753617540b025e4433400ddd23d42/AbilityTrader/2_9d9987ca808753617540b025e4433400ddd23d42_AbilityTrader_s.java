 package com.vxnick.abilitytrader;
 
 import java.text.MessageFormat;
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
 		
 		// Schedule removal of expired player abilities every minute
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				removeExpiredPlayerAbilities();
 			}
 		}, 40L, 1200L);
 	}
 	
 	@Override
 	public void onDisable() {
 
 	}
 	
 	/**
 	 * Initialise Vault Economy
 	 */	
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
 	
 	/**
 	 * Initialise Vault Permissions
 	 */
 	private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         
         perms = rsp.getProvider();
         
         return perms != null;
     }
 	
 	private Set<String> getPlayers() {
 		ConfigurationSection playerSection = getConfig().getConfigurationSection("players");
 		
 		if (playerSection != null) {
 			return playerSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	private Set<String> getPlayerAbilities(String player) {
 		// Check the players configuration section exists
 		if (getPlayers() == null) {
 			return null;
 		}
 		
 		ConfigurationSection playerSection = getConfig().getConfigurationSection(String.format("players.%s", player));
 		
 		if (playerSection != null) {
 			return playerSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	private Set<String> getAvailableAbilities() {
 		ConfigurationSection abilitiesSection = getConfig().getConfigurationSection("abilities");
 		
 		if (abilitiesSection != null) {
 			return abilitiesSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Remove expired player abilities.
 	 * 
 	 * This is called by the async task every minute by default.
 	 */
 	private void removeExpiredPlayerAbilities() {
 		// TODO: Remove empty players
 		Set<String> players = getPlayers();
 		
 		if (players == null) {
 			return;
 		}
 		
 		for (String player : players) {
 			Set<String> playerAbilities = getPlayerAbilities(player);
 			
 			if (playerAbilities != null) {
 				for (String ability : playerAbilities) {
 					long expiresAt = getConfig().getLong(String.format("players.%s.%s.expires_at", player, ability), 0);
 
 					if (expiresAt > 0 && expiresAt < getUnixTime()) {
 						// Get a list of permissions for the current player ability
 						List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 						
 						for (String permission : permissions) {
 							// Remove the permission if the player has it
 							if (perms.has((String) null, player, permission)) {
 								// Apply this to all worlds
 								perms.playerRemove((String) null, player, permission);
 								
 								// Remove the player ability
 								getConfig().set(String.format("players.%s.%s", player, ability), null);
 							}
 						}
 						
 						// Get a list of commands to be run for the current player ability
 						List<String> commands = getConfig().getStringList(String.format("abilities.%s.commands.remove", ability));
 						
 						if (commands != null) {
 							for (String command : commands) {
 								// Run the command
 								getServer().dispatchCommand(getServer().getConsoleSender(), MessageFormat.format(command, player));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		saveConfig();
 	}
 	
 	private boolean givePlayerAbility(Player player, String ability, boolean rented) {
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
 		List<String> commands = getConfig().getStringList(String.format("abilities.%s.commands.add", ability));
 		
 		if (commands != null) {
 			for (String command : commands) {
 				// Run the command
 				getServer().dispatchCommand(getServer().getConsoleSender(), MessageFormat.format(command, player.getName()));
 			}
 		}
 		
 		// Record this player's abilities in the configuration
 		getConfig().set(String.format("players.%s.%s.given_at", player.getName(), ability), System.currentTimeMillis() / 1000L);
 		
 		// Set duration (expiry) for this ability if rented
 		if (rented) {
 			Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 			getConfig().set(String.format("players.%s.%s.expires_at", player.getName(), ability), (duration > 0 ? getUnixTime() + duration : 0));
 		}
 		
 		saveConfig();
 		return true;
 	}
 	
 	/**
 	 * A simple wrapper to return the Unix timestamp.
 	 */
 	private long getUnixTime() {
 		return (System.currentTimeMillis() / 1000L);
 	}
 	
 	/**
 	 * Format a value in seconds into minutes/hours/days.
 	 */
 	private String formatDuration(long seconds) {
 		if (seconds < 60) {
 			return String.format("%d second%s", seconds, (seconds == 1 ? "" : "s"));
 		} else if (seconds < 3600) {
 			return String.format("%d minute%s", (seconds / 60), ((seconds / 60) == 1 ? "" : "s"));
 		} else if (seconds < 86400) {
 			return String.format("%d hour%s", (seconds / 3600), ((seconds / 3600) == 1 ? "" : "s"));
 		} else {
 			return String.format("%d day%s", (seconds / 86400), ((seconds / 86400) == 1 ? "" : "s"));
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("ability")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Sorry, this isn't compatible with the console just yet");
 				return true;
 			}
 			
 			if (!perms.has(sender, "abilitytrader.use")) {
 				sender.sendMessage(ChatColor.RED + "Sorry, you don't have access to Ability Trader");
 				return true;
 			}
 			
 			if (args.length == 0) {
 				return false;
 			}
 			
 			String command = args[0].toLowerCase();
 			Player player = (Player) sender;
 			
 			if (perms.has(player, "abilitytrader.admin")) {
 				if (command.equals("reload")) {
 					player.sendMessage(ChatColor.YELLOW + "Reloading configuration");
 					reloadConfig();
 					return true;
 				}
 			}
 			
 			if (command.equals("list")) {
 				Set<String> availableAbilities = getAvailableAbilities();
 				
 				if (availableAbilities == null) {
 					player.sendMessage(ChatColor.YELLOW + "There are no abilities available!");
 					return true;
 				}
 				
 				player.sendMessage(ChatColor.GOLD + "Available Abilities");
 				
 				for (String ability : availableAbilities) {
 					String description = getConfig().getString(String.format("abilities.%s.description", ability), "No description");
 					Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 					Integer rentCost = getConfig().getInt(String.format("abilities.%s.rent_cost", ability), 0);
 					Integer buyCost = getConfig().getInt(String.format("abilities.%s.buy_cost", ability), 0);
 					
 					player.sendMessage(ChatColor.YELLOW + String.format("%s -- %s", ability, description));
 
 					// Figure out what type of cost this is
 					if (rentCost == 0 && buyCost != 0) {
 						player.sendMessage(String.format("%s to buy", econ.format(buyCost)));
 					} else if (rentCost != 0 && buyCost == 0) {
 						player.sendMessage(String.format("%s to rent for %s", econ.format(rentCost), formatDuration(duration)));
 					} else {
 						player.sendMessage(String.format("%s to buy or %s to rent for %s", econ.format(buyCost), econ.format(rentCost), formatDuration(duration)));
 					}										
 				}
 			} else if (command.equals("info")) {
 				Set<String> playerAbilities = getPlayerAbilities(player.getName());
 				
 				if (playerAbilities == null || playerAbilities.isEmpty()) {
 					player.sendMessage(ChatColor.YELLOW + "You currently have no abilities");
 					return true;
 				}
 				
 				player.sendMessage(ChatColor.GOLD + "My Abilities");
 				
 				for (String ability : playerAbilities) {
 					String description = getConfig().getString(String.format("abilities.%s.description", ability), "No description");
 					long expiresAt = getConfig().getLong(String.format("players.%s.%s.expires_at", player.getName(), ability), 0);
 					String timeRemaining = expiresAt == 0 ? "" : String.format("%s remaining", formatDuration(expiresAt - getUnixTime()));
 					
 					player.sendMessage(String.format(ChatColor.YELLOW + "%s -- %s", ability, description));
 
 					// Only show when remaining time is a positive number - the task doesn't remove abilities at the exact moment of expiry
 					if ((expiresAt - getUnixTime()) > 0) {					
 						
 						if (timeRemaining != "") {
 							player.sendMessage(timeRemaining);
 						}
 					} else {
 						if (timeRemaining != "") {
 							player.sendMessage("expired - pending removal");
 						}
 					}
 				}
 			} else if (command.equals("buy") || command.equals("rent")) {
 				if (args.length < 2) {
 					player.sendMessage(ChatColor.YELLOW + "Please specify an ability");
 					return true;
 				}
 				
 				String requestedAbility = args[1].toLowerCase();
 				
 				// Check this ability exists
 				if (getConfig().getString(String.format("abilities.%s", requestedAbility)) == null) {
 					player.sendMessage(ChatColor.RED + "Ability not found - please check /ability list for available abilities");
 					return true;
 				}
 				
 				// Get type of purchase (rent or buy) and its price
 				String purchaseType = command.equals("rent") ? "rent" : "buy";
 				Integer cost = getConfig().getInt(String.format("abilities.%s.%s_cost", requestedAbility, purchaseType), 0);
 				
 				// Check that the player has specified the correct purchase type
 				if (cost == 0) {
 					player.sendMessage(ChatColor.YELLOW + String.format("You can not %s this ability", purchaseType));
 					return true;
 				}
 				
 				// Check the player doesn't already have this ability
 				if (getConfig().getString(String.format("players.%s.%s", player.getName(), requestedAbility)) != null) {
 					player.sendMessage(ChatColor.YELLOW + "You already have this ability!");
 					return true;
 				}
 				
 				// Attempt to purchase this ability
 				if (econ.getBalance(player.getName()) >= cost) {
 					EconomyResponse r = econ.withdrawPlayer(player.getName(), cost);
 					
 					if (r.transactionSuccess()) {
 						if (givePlayerAbility(player, requestedAbility, purchaseType.equals("rent"))) {
 							player.sendMessage(ChatColor.GOLD + String.format("You have been given the '%s' ability!", requestedAbility));
 						} else {
 							econ.depositPlayer(player.getName(), cost);
 							player.sendMessage(ChatColor.RED + "Sorry, something went wrong. You have been refunded");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "Sorry, something went wrong. No money has been taken");
 					}
 				} else {
 					player.sendMessage(ChatColor.YELLOW + String.format("You do not have enough money to %s this ability!", purchaseType));
 				}
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Ability Trader Commands");
 				player.sendMessage("/ability info -- Show which abilities you have");
 				player.sendMessage("/ability list -- List available abilities");
 				player.sendMessage("/ability buy <ability> -- Buy <ability>");
 				player.sendMessage("/ability rent <ability> -- Rent <ability>");
 			}
 		}
 		
 		return true;
 	}
 }
