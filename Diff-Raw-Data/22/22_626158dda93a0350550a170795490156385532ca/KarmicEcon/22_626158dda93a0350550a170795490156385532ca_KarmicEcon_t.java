 package com.mitsugaru.Karmiconomy;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.mitsugaru.Karmiconomy.DatabaseHandler.Field;
 
 public class KarmicEcon
 {
 	private static Karmiconomy plugin;
 	private static Config config;
 	private static Economy eco;
 	private static boolean playerpoints, vault;
 	private static Plugin pointsPlugin;
 
 	public KarmicEcon(Karmiconomy plugin)
 	{
 		KarmicEcon.plugin = plugin;
 		KarmicEcon.config = plugin.getPluginConfig();
 	}
 
 	public boolean setupEconomy()
 	{
 		// Check vault
 		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
 				.getServicesManager()
 				.getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null)
 		{
 			eco = economyProvider.getProvider();
 			vault = true;
 		}
 		// Check playerpoints
 		Plugin playerPointsPlugin = plugin.getServer().getPluginManager()
 				.getPlugin("PlayerPoints");
 		if (playerPointsPlugin != null)
 		{
 			pointsPlugin = playerPointsPlugin;
 			playerpoints = true;
 		}
 		// None fond
 		if (!playerpoints && !vault)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean denyPay(Field field, Player player, double pay, Item item,
 			String command)
 	{
		boolean paid = false;
 		if (vault)
 		{
 			// Deny by player balance
 			final double balance = eco.getBalance(player.getName());
 			if (pay < 0.0)
 			{
 				// Only care about negatives. Need to change to positive for
 				// comparison.
 				pay *= -1;
 				if (pay > balance)
 				{
					paid = true;
 				}
 			}
 		}
 		if (playerpoints)
 		{
 			final int playerPoints = pointsPlugin.getConfig().getInt(
 					"Points." + player.getName());
 			if (pay < 0.0)
 			{
 				pay *= 1;
 				if (pay > playerPoints)
 				{
					paid = true;
 				}
 			}
 		}
		return paid;
 	}
 
 	public static boolean pay(Field field, Player player, Item item,
 			String command)
 	{
		boolean paid = false;
 		final double amount = config.getPayValue(field, item, command);
 		if (amount == 0.0)
 		{
 			// Just record that it happened
 			return true;
 		}
 		if (vault)
 		{
 			EconomyResponse response = null;
 			if (amount > 0.0)
 			{
 				response = eco.depositPlayer(player.getName(), amount);
 			}
 			else if (amount < 0.0)
 			{
 				response = eco.withdrawPlayer(player.getName(), (amount * -1));
 			}
 			if (response != null)
 			{
 				String message = "";
 				switch (response.type)
 				{
 					case FAILURE:
 					{
 						if (config.getDenyPay(field, item))
 						{
 							message = ChatColor.RED + Karmiconomy.TAG
 									+ " Could not pay " + ChatColor.GOLD
 									+ amount + ChatColor.RED + " for "
 									+ ChatColor.AQUA + field.name();
 							player.sendMessage(message);
 							Karmiconomy.sentMessages.put(player.getName(),
 									message);
 							if (config.debugEconomy)
 							{
 								plugin.getLogger()
 										.severe("Eco Failure: "
 												+ response.errorMessage);
 							}
 						}
 						break;
 					}
 					case NOT_IMPLEMENTED:
 					{
 						message = ChatColor.RED + Karmiconomy.TAG
 								+ " Could not pay " + ChatColor.GOLD + amount
 								+ ChatColor.RED + " for " + ChatColor.AQUA
 								+ field.name();
 						player.sendMessage(message);
 						Karmiconomy.sentMessages.put(player.getName(), message);
 						if (config.debugEconomy)
 						{
 							plugin.getLogger().severe(
 									"Eco not implemented: "
 											+ response.errorMessage);
 						}
 						break;
 					}
 					case SUCCESS:
 					{
 						if (config.debugEconomy)
 						{
 							plugin.getLogger().info(
 									"Eco success for player '"
 											+ player.getName()
 											+ "' of amount: " + amount);
 						}
						paid = true;;
 					}
 					default:
 						break;
 				}
 			}
 		}
 		if (playerpoints)
 		{
 			int points = (int) amount;
 			if (points == 0)
 			{
 				return true;
 			}
 			else if (points > 0)
 			{
 				plugin.getServer().dispatchCommand(
 						plugin.getServer().getConsoleSender(),
 						"points give " + player.getName() + " +" + points);
				paid = true;
 			}
 			else if (points < 0)
 			{
 				points *= -1;
 				plugin.getServer().dispatchCommand(
 						plugin.getServer().getConsoleSender(),
 						"points give " + player.getName() + " -" + points);
				paid = true;
 			}
 		}
 		// Unsuccessful transaction
		return paid;
 	}
 }
