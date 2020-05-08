 package net.erbros.lottery;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import net.erbros.lottery.register.payment.Methods;
 
 
 public class MainCommandExecutor implements CommandExecutor
 {
 
 	final private Lottery plugin;
 	final private LotteryConfig lConfig;
 	final private LotteryGame lGame;
 
 	public MainCommandExecutor(final Lottery plugin)
 	{
 		this.plugin = plugin;
 		lConfig = plugin.getLotteryConfig();
 		lGame = plugin.getLotteryGame();
 	}
 
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
 	{
 
 		// Lets check if we have found a plugin for money.
 		if (lConfig.useiConomy() && !Methods.hasMethod())
 		{
 			lConfig.debugMsg("No money plugin found yet.");
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Sorry, we haven't found a money plugin yet..");
 			return true;
 		}
 
 		// Can the player access the plugin?
 		if (!sender.hasPermission("lottery.buy"))
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You don't have access to that command.");
 		}
 
 		// If its just /lottery, and no args.
 		if (args.length == 0)
 		{
 			commandNull(sender, args);
 		}
 		else if (args[0].equalsIgnoreCase("buy"))
 		{
 			commandBuy(sender, args);
 		}
 		else if (args[0].equalsIgnoreCase("claim"))
 		{
 			commandClaim(sender, args);
 		}
 		else if (args[0].equalsIgnoreCase("winners"))
 		{
 			commandWinners(sender, args);
 		}
 		else if (args[0].equalsIgnoreCase("help"))
 		{
 			commandHelp(sender, args);
 		}
 		else if (args[0].equalsIgnoreCase("draw"))
 		{
 			if (sender.hasPermission("lottery.admin.draw"))
 			{
 				commandDraw(sender, args);
 			}
 			else
 			{
 				sender.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You don't have access to that command.");
 			}
 		}
 		else if (args[0].equalsIgnoreCase("addtopot"))
 		{
 			if (sender.hasPermission("lottery.admin.addtopot"))
 			{
 				commandAddToPot(sender, args);
 			}
 			else
 			{
 				sender.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You don't have access to that command.");
 			}
 		}
 		else if (args[0].equalsIgnoreCase("config"))
 		{
 			if (sender.hasPermission("lottery.admin.editconfig"))
 			{
 				commandConfig(sender, args);
 			}
 			else
 			{
 				sender.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You don't have access to that command.");
 			}
 		}
 		else
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Hey, I don't recognize that command!");
 		}
 
 		return true;
 	}
 
 	public void commandNull(final CommandSender sender, final String[] args)
 	{
 		// Is this a console? If so, just tell that lottery is running and time until next draw.
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage("Hi Console - The Lottery plugin is running");
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Draw in: " + ChatColor.RED + lGame.timeUntil(
 							false));
 			return;
 		}
 		final Player player = (Player)sender;
 
 		// Check if we got any money/items in the pot.
 		final double amount = lGame.winningAmount();
 		lConfig.debugMsg("pot current total: " + amount);
 		// Send some messages:
 		player.sendMessage(
 				ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Draw in: " + ChatColor.RED + lGame.timeUntil(false));
 		if (lConfig.useiConomy())
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Buy a ticket for " + ChatColor.RED + plugin.Method.format(
 							lConfig.getCost()) + ChatColor.WHITE + " with " + ChatColor.RED + "/lottery buy");
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There is currently " + ChatColor.GREEN + plugin.Method.format(
 							amount) + ChatColor.WHITE + " in the pot.");
 		}
 		else
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Buy a ticket for " + ChatColor.RED + lConfig.getCost() + " " + Etc.formatMaterialName(
 							lConfig.getMaterial()) + ChatColor.WHITE + " with " + ChatColor.RED + "/lottery buy");
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There is currently " + ChatColor.GREEN + amount + " " + Etc.formatMaterialName(
 							lConfig.getMaterial()) + ChatColor.WHITE + " in the pot.");
 		}
 		if (lConfig.getMaxTicketsEachUser() > 1)
 		{
 			player.sendMessage(
					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You got " + ChatColor.RED + lGame.playerInList(
 							(Player)sender) + " " + ChatColor.WHITE + Etc.pluralWording(
 							"ticket", lGame.playerInList(
 							(Player)sender)));
 		}
 		// Number of tickets available?
 		if (lConfig.getTicketsAvailable() > 0)
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY]" + ChatColor.WHITE + " There is " + ChatColor.RED + (lConfig.getTicketsAvailable() - lGame.ticketsSold()) + ChatColor.WHITE + " " + Etc.pluralWording(
 							"ticket", lConfig.getTicketsAvailable() - lGame.ticketsSold()) + " left.");
 		}
 		player.sendMessage(
 				ChatColor.GOLD + "[LOTTERY] " + ChatColor.RED + "/lottery help" + ChatColor.WHITE + " for other commands");
 		// Does lastwinner exist and != null? Show.
 		// Show different things if we are using iConomy over
 		// material.
 		if (lConfig.getLastwinner() != null)
 		{
 			if (lConfig.useiConomy())
 			{
 				player.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Last winner: " + lConfig.getLastwinner() + " (" + plugin.Method.format(
 								lConfig.getLastwinneramount()) + ")");
 			}
 			else
 			{
 				player.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Last winner: " + lConfig.getLastwinner() + " (" + lConfig.getLastwinneramount() + " " + Etc.formatMaterialName(
 								lConfig.getMaterial()) + ")");
 			}
 		}
 
 		// if not iConomy, make players check for claims.
 		if (!lConfig.useiConomy())
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Check if you have won with " + ChatColor.RED + "/lottery claim");
 		}
 	}
 
 	public void commandHelp(final CommandSender sender, final String[] args)
 	{
 		sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Help commands");
 		sender.sendMessage(ChatColor.RED + "/lottery" + ChatColor.WHITE + " : Basic lottery info.");
 		sender.sendMessage(ChatColor.RED + "/lottery buy <n>" + ChatColor.WHITE + " : Buy ticket(s).");
 		sender.sendMessage(ChatColor.RED + "/lottery claim" + ChatColor.WHITE + " : Claim outstandig wins.");
 		sender.sendMessage(ChatColor.RED + "/lottery winners" + ChatColor.WHITE + " : Check last winners.");
 		// Are we dealing with admins?
 		if (sender.hasPermission("lottery.admin.draw"))
 		{
 			sender.sendMessage(ChatColor.BLUE + "/lottery draw" + ChatColor.WHITE + " : Draw lottery.");
 		}
 		if (sender.hasPermission("lottery.admin.addtopot"))
 		{
 			sender.sendMessage(ChatColor.BLUE + "/lottery addtopot" + ChatColor.WHITE + " : Add number to pot.");
 		}
 		if (sender.hasPermission("lottery.admin.editconfig"))
 		{
 			sender.sendMessage(ChatColor.BLUE + "/lottery config" + ChatColor.WHITE + " : Edit the config.");
 		}
 	}
 
 	public void commandBuy(final CommandSender sender, final String[] args)
 	{
 		// Is this a console? If so, just tell that lottery is running and time until next draw.
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You're the console, I can't sell you tickets.");
 			return;
 		}
 		final Player player = (Player)sender;
 
 		int buyTickets = 1;
 		if (args.length > 1)
 		{
 			// How many tickets do the player want to buy?
 			buyTickets = Etc.parseInt(args[1]);
 
 			if (buyTickets < 1)
 			{
 				buyTickets = 1;
 			}
 		}
 
 		final int allowedTickets = lConfig.getMaxTicketsEachUser() - lGame.playerInList(player);
 
 		if (buyTickets > allowedTickets && allowedTickets > 0)
 		{
 			buyTickets = allowedTickets;
 		}
 
 		// Have the admin entered a max number of tickets in the lottery?
 		if (lConfig.getTicketsAvailable() > 0)
 		{
 			// If so, can this user buy the selected amount?
 			if (lGame.ticketsSold() + buyTickets > lConfig.getTicketsAvailable())
 			{
 				if (lGame.ticketsSold() >= lConfig.getTicketsAvailable())
 				{
 					player.sendMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There are no more tickets available");
 					return;
 				}
 				else
 				{
 					buyTickets = lConfig.getTicketsAvailable() - lGame.ticketsSold();
 				}
 			}
 		}
 
 		if (lConfig.getMaxTicketsEachUser() > 0 && lGame.playerInList(
 				player) + buyTickets > lConfig.getMaxTicketsEachUser())
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You already have the maximum of " + lConfig.getMaxTicketsEachUser() + " " + Etc.pluralWording(
 							"ticket", lConfig.getMaxTicketsEachUser()) + " already.");
 			return;
 		}
 
 		if (lGame.addPlayer(player, lConfig.getMaxTicketsEachUser(), buyTickets))
 		{
 			// You got your ticket.
 			if (lConfig.useiConomy())
 			{
 				player.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You got " + buyTickets + " " + Etc.pluralWording(
 								"ticket", buyTickets) + " for " + ChatColor.RED + plugin.Method.format(
 								lConfig.getCost() * buyTickets));
 			}
 			else
 			{
 				player.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You got " + buyTickets + " " + Etc.pluralWording(
 								"ticket",
 								buyTickets) + " for " + ChatColor.RED + lConfig.getCost() * buyTickets + " " + Etc.formatMaterialName(
 								lConfig.getMaterial()));
 			}
 			// Can a user buy more than one ticket? How many
 			// tickets have he bought now?
 			if (lConfig.getMaxTicketsEachUser() > 1)
 			{
 				player.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You now have " + ChatColor.RED + lGame.playerInList(
 								player) + " " + ChatColor.WHITE + Etc.pluralWording(
 								"ticket", lGame.playerInList(player)));
 			}
 			if (lConfig.isBuyingExtendDeadline() && lGame.timeUntil() < lConfig.getBuyingExtendRemaining())
 			{
 				final long timeBonus = (long)(lConfig.getBuyingExtendBase() + (lConfig.getBuyingExtendMultiplier() * Math.sqrt(
 						buyTickets)));
 				lConfig.setNextexec(lConfig.getNextexec() + (timeBonus * 1000));
 			}
 			if (lConfig.useBroadcastBuying())
 			{
 				if (lGame.timeUntil() < lConfig.getBroadcastBuyingTime())
 				{
 					Bukkit.broadcastMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + player.getDisplayName() + ChatColor.WHITE + " just bought " + buyTickets + " " + Etc.pluralWording(
 									"ticket", buyTickets) + "! Draw in " + lGame.timeUntil(true));
 				}
 				else
 				{
 					Bukkit.broadcastMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + player.getDisplayName() + ChatColor.WHITE + " just bought " + buyTickets + " " + Etc.pluralWording(
 									"ticket", buyTickets));
 				}
 			}
 
 		}
 		else
 		{
 			// Something went wrong.
 			player.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You can't afford a ticket");
 		}
 	}
 
 	public void commandClaim(final CommandSender sender, final String[] args)
 	{
 		// Is this a console? If so, just tell that lottery is running and time until next draw.
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You're the console, you don't have an inventory.");
 			return;
 		}
 
 		lGame.removeFromClaimList((Player)sender);
 	}
 
 	public void commandDraw(final CommandSender sender, final String[] args)
 	{
 		// Start a timer that ends in 3 secs.
 		sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Lottery will be drawn at once.");
 		plugin.startTimerSchedule(true);
 	}
 
 	public void commandWinners(final CommandSender sender, final String[] args)
 	{
 		// Get the winners.
 		final ArrayList<String> winnerArray = new ArrayList<String>();
 		try
 		{
 			final BufferedReader in = new BufferedReader(
 					new FileReader(plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
 			String str;
 			while ((str = in.readLine()) != null)
 			{
 				winnerArray.add(str);
 			}
 			in.close();
 		}
 		catch (IOException e)
 		{
 		}
 		String[] split;
 		String winListPrice;
 		for (int i = 0; i < winnerArray.size(); i++)
 		{
 			split = winnerArray.get(i).split(":");
 			if (split[2].equalsIgnoreCase("0"))
 			{
 				winListPrice = plugin.Method.format(Double.parseDouble(split[1]));
 			}
 			else
 			{
 				winListPrice = split[1] + " " + Etc.formatMaterialName(
 						Integer.parseInt(split[2])).toString();
 			}
 			sender.sendMessage((i + 1) + ". " + split[0] + " " + winListPrice);
 		}
 	}
 
 	public void commandAddToPot(final CommandSender sender, final String[] args)
 	{
 		if (args[1] == null)
 		{
 			sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "/lottery addtopot <number>");
 			return;
 		}
 
 		if (args.length < 2)
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Provide a number greater than zero (decimals accepted)");
 		}
 
 		final double addToPot = Etc.parseDouble(args[1]);
 
 		if (addToPot == 0)
 		{
 			sender.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Provide a number greater than zero (decimals accepted)");
 			return;
 		}
 		lConfig.addExtraInPot(addToPot);
 
 		sender.sendMessage(
 				ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Added " + ChatColor.GREEN + addToPot + ChatColor.WHITE + " to pot. Extra total is " + ChatColor.GREEN + lConfig.getExtraInPot());
 	}
 
 	public void commandConfig(final CommandSender sender, final String[] args)
 	{
 		if (args.length == 1)
 		{
 			sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Edit config commands");
 			sender.sendMessage(ChatColor.RED + "/lottery config cost <i>");
 			sender.sendMessage(ChatColor.RED + "/lottery config hours <i>");
 			sender.sendMessage(ChatColor.RED + "/lottery config maxTicketsEachUser <i>");
 			sender.sendMessage(ChatColor.RED + "/lottery config reload");
 			return;
 		}
 		else if (args.length > 2)
 		{
 			if (args[1].equalsIgnoreCase("cost"))
 			{
 				final double newCoin = Etc.parseDouble(args[2]);
 				if (newCoin <= 0)
 				{
 					sender.sendMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Provide a number greater than zero (decimals accepted)");
 				}
 				else
 				{
 					sender.sendMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Cost changed to " + ChatColor.RED + newCoin);
 					lConfig.setCost(newCoin);
 				}
 			}
 			else if (args[1].equalsIgnoreCase("hours"))
 			{
 				final double newHours = Etc.parseDouble(args[2]);
 				if (newHours <= 0)
 				{
 					sender.sendMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Provide a number greater than zero (decimals accepted)");
 				}
 				else
 				{
 					sender.sendMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Hours changed to " + ChatColor.RED + newHours);
 					lConfig.setHours(newHours);
 				}
 
 			}
 			else if (args[1].equalsIgnoreCase("maxTicketsEachUser") || args[1].equalsIgnoreCase("max"))
 			{
 				final int newMaxTicketsEachUser = Etc.parseInt(args[2]);
 				sender.sendMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Max amount of tickets changed to " + ChatColor.RED + newMaxTicketsEachUser);
 				lConfig.setMaxTicketsEachUser(newMaxTicketsEachUser);
 			}
 		}
 		// Lets just reload the config.
 		lConfig.loadConfig();
 		sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Config reloaded");
 	}
 }
