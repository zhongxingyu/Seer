 package net.erbros.lottery;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import net.erbros.lottery.register.payment.Method;
 
 
 public class LotteryGame
 {
 
 	final private Lottery plugin;
 	final private LotteryConfig lConfig;
 
 	public LotteryGame(final Lottery plugin)
 	{
 		this.plugin = plugin;
 		lConfig = plugin.getLotteryConfig();
 	}
 
 	public boolean addPlayer(final Player player, final int maxAmountOfTickets, final int numberOfTickets)
 	{
 
 		// Do the ticket cost money or item?
 		if (lConfig.useiConomy())
 		{
 			// Do the player have money?
 			// First checking if the player got an account, if not let's create
 			// it.
 			plugin.Method.hasAccount(player.getName());
 			final Method.MethodAccount account = plugin.Method.getAccount(player.getName());
 
 			// And lets withdraw some money
 			if (account.hasOver(lConfig.getCost() * numberOfTickets))
 			{
 				// Removing coins from players account.
 				account.subtract(lConfig.getCost() * numberOfTickets);
 			}
 			else
 			{
 				return false;
 			}
 			lConfig.debugMsg("taking " + (lConfig.getCost() * numberOfTickets) + "from account");
 		}
 		else
 		{
 			// Do the user have the item
 			if (player.getInventory().contains(lConfig.getMaterial(), (int)lConfig.getCost() * numberOfTickets))
 			{
 				// Remove items.
 				player.getInventory().removeItem(
 						new ItemStack(lConfig.getMaterial(), (int)lConfig.getCost() * numberOfTickets));
 			}
 			else
 			{
 				return false;
 			}
 
 
 		}
 		// If the user paid, continue. Else we would already have sent return
 		// false
 		try
 		{
 			final BufferedWriter out = new BufferedWriter(
 					new FileWriter(plugin.getDataFolder() + File.separator + "lotteryPlayers.txt", true));
 			for (Integer i = 0; i < numberOfTickets; i++)
 			{
 				out.write(player.getName());
 				out.newLine();
 			}
 			out.close();
 
 		}
 		catch (IOException e)
 		{
 		}
 
 		return true;
 	}
 
 	public Integer playerInList(final Player player)
 	{
 		int numberOfTickets = 0;
 		try
 		{
 			final BufferedReader in = new BufferedReader(
 					new FileReader(plugin.getDataFolder() + File.separator + "lotteryPlayers.txt"));
 
 			String str;
 			while ((str = in.readLine()) != null)
 			{
 
 				if (str.equalsIgnoreCase(player.getName()))
 				{
 					numberOfTickets++;
 				}
 			}
 			in.close();
 		}
 		catch (IOException e)
 		{
 		}
 
 		return numberOfTickets;
 	}
 
 	public ArrayList<String> playersInFile(final String file)
 	{
 		final ArrayList<String> players = new ArrayList<String>();
 		try
 		{
 			final BufferedReader in = new BufferedReader(
 					new FileReader(plugin.getDataFolder() + File.separator + file));
 			String str;
 			while ((str = in.readLine()) != null)
 			{
 				// add players to array.
 				players.add(str);
 			}
 			in.close();
 		}
 		catch (IOException e)
 		{
 		}
 		return players;
 	}
 
 	public double winningAmount()
 	{
 		double amount = 0;
 		final ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 		amount = players.size() * Etc.formatAmount(lConfig.getCost(), lConfig.useiConomy());
 		lConfig.debugMsg("playerno: " + players.size() + " amount: " + amount);
 		// Set the net payout as configured in the config.
 		if (lConfig.getNetPayout() > 0)
 		{
 			amount = amount * lConfig.getNetPayout() / 100;
 		}
 		// Add extra money added by admins and mods?
 		amount += lConfig.getExtraInPot();
 		// Any money in jackpot?
 
 		lConfig.debugMsg("using config store: " + lConfig.getJackpot());
 		amount += lConfig.getJackpot();
 
 
 		// format it once again.
 		amount = Etc.formatAmount(amount, lConfig.useiConomy());
 
 		return amount;
 	}
 
 	public int ticketsSold()
 	{
 		int sold;
 		final ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 		sold = players.size();
 		return sold;
 	}
 
 	public boolean removeFromClaimList(final Player player)
 	{
 		// Do the player have something to claim?
 		final ArrayList<String> otherPlayersClaims = new ArrayList<String>();
 		final ArrayList<String> claimArray = new ArrayList<String>();
 		try
 		{
 			final BufferedReader in = new BufferedReader(
 					new FileReader(plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
 			String str;
 			while ((str = in.readLine()) != null)
 			{
 				final String[] split = str.split(":");
 				if (split[0].equals(player.getName()))
 				{
 					// Adding this to player claim.
 					claimArray.add(str);
 				}
 				else
 				{
 					otherPlayersClaims.add(str);
 				}
 			}
 			in.close();
 		}
 		catch (IOException e)
 		{
 		}
 
 		// Did the user have any claims?
 		if (claimArray.isEmpty())
 		{
 			player.sendMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You did not have anything unclaimed.");
 			return false;
 		}
 		// Do a bit payout.
 		for (int i = 0; i < claimArray.size(); i++)
 		{
 			final String[] split = claimArray.get(i).split(":");
 			final int claimAmount = Integer.parseInt(split[1]);
 			final int claimMaterial = Integer.parseInt(split[2]);
 			player.getInventory().addItem(new ItemStack(claimMaterial, claimAmount));
 			player.sendMessage("You just claimed " + claimAmount + " " + Etc.formatMaterialName(claimMaterial) + ".");
 		}
 
 		// Add the other players claims to the file again.
 		try
 		{
 			final BufferedWriter out = new BufferedWriter(
 					new FileWriter(plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
 			for (int i = 0; i < otherPlayersClaims.size(); i++)
 			{
 				out.write(otherPlayersClaims.get(i));
 				out.newLine();
 			}
 
 			out.close();
 
 		}
 		catch (IOException e)
 		{
 		}
 		return true;
 	}
 
 	public boolean addToClaimList(final String playerName, final int winningAmount, final int winningMaterial)
 	{
 		// Then first add new winner, and after that the old winners.
 		try
 		{
 			final BufferedWriter out = new BufferedWriter(
 					new FileWriter(plugin.getDataFolder() + File.separator + "lotteryClaim.txt", true));
 			out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 			out.newLine();
 			out.close();
 		}
 		catch (IOException e)
 		{
 		}
 		return true;
 	}
 
 	public boolean addToWinnerList(final String playerName, final Double winningAmount, final int winningMaterial)
 	{
 		// This list should be 10 players long.
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
 		// Then first add new winner, and after that the old winners.
 		try
 		{
 			final BufferedWriter out = new BufferedWriter(
 					new FileWriter(plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
 			out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 			out.newLine();
 			// How long is the array? We just want the top 9. Removing index 9
 			// since its starting at 0.
 			if (!winnerArray.isEmpty())
 			{
 				if (winnerArray.size() > 9)
 				{
 					winnerArray.remove(9);
 				}
 				// Go trough list and output lines.
 				for (int i = 0; i < winnerArray.size(); i++)
 				{
 					out.write(winnerArray.get(i));
 					out.newLine();
 				}
 			}
 			out.close();
 
 		}
 		catch (IOException e)
 		{
 		}
 		return true;
 	}
 
 	public long timeUntil()
 	{
 		final long nextDraw = lConfig.getNextexec();
 		final long timeLeft = ((nextDraw - System.currentTimeMillis()) / 1000);
 		return timeLeft;
 	}
 
 	public String timeUntil(final boolean mini)
 	{
 		final long timeLeft = timeUntil();
 		// If negative number, just tell them its DRAW TIME!
 		if (timeLeft < 0)
 		{
 			// Lets make it draw at once.. ;)
 			plugin.startTimerSchedule(true);
 			// And return some string to let the user know we are doing our best ;)
 			if (mini)
 			{
 				return "Soon";
 			}
 			return "Draw will occur soon!";
 
 		}
 
 		return Etc.timeUntil(timeLeft, mini);
 	}
 
 	public boolean getWinner()
 	{
 		final ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 
 		if (players.isEmpty())
 		{
 			Bukkit.broadcastMessage(
 					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "No tickets sold this round. Thats a shame.");
 			return false;
 		}
 		else
 		{
 			// Find rand. Do minus 1 since its a zero based array.
 			int rand = 0;
 
 			// is max number of tickets 0? If not, include empty tickets not sold.
 			if (lConfig.getTicketsAvailable() > 0 && ticketsSold() < lConfig.getTicketsAvailable())
 			{
 				rand = new Random().nextInt(lConfig.getTicketsAvailable());
 				// If it wasn't a player winning, then do some stuff. If it was a player, just continue below.
 				if (rand > players.size() - 1)
 				{
 					// No winner this time, pot goes on to jackpot!
 					final double jackpot = winningAmount();
 
 					lConfig.setJackpot(jackpot);
 
 					addToWinnerList("Jackpot", jackpot, lConfig.useiConomy() ? 0 : lConfig.getMaterial());
 					lConfig.setLastwinner("Jackpot");
 					lConfig.setLastwinneramount(jackpot);
 					Bukkit.broadcastMessage(
 							ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "No winner, we have a rollover! " + ChatColor.GREEN + ((lConfig.useiConomy()) ? plugin.Method.format(
 									jackpot) : +jackpot + " " + Etc.formatMaterialName(
 									lConfig.getMaterial())) + ChatColor.WHITE + " went to jackpot!");
 					clearAfterGettingWinner();
 					return true;
 				}
 			}
 			else
 			{
 				// Else just continue
 				rand = new Random().nextInt(players.size());
 			}
 
 
 			lConfig.debugMsg("Rand: " + Integer.toString(rand));
 			double amount = winningAmount();
 			if (lConfig.useiConomy())
 			{
 				plugin.Method.hasAccount(players.get(rand));
 				final Method.MethodAccount account = plugin.Method.getAccount(players.get(rand));
 
 				// Just make sure the account exists, or make it with default
 				// value.
 				// Add money to account.
 				account.add(amount);
 				// Announce the winner:
 				Bukkit.broadcastMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Congratulations to " + players.get(
 								rand) + " for winning " + ChatColor.RED + plugin.Method.format(amount) + ".");
 				addToWinnerList(players.get(rand), amount, 0);
 			}
 			else
 			{
 				// let's throw it to an int.
 				final int matAmount = (int)Etc.formatAmount(amount, lConfig.useiConomy());
 				amount = (double)matAmount;
 				Bukkit.broadcastMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Congratulations to " + players.get(
 								rand) + " for winning " + ChatColor.RED + matAmount + " " + Etc.formatMaterialName(
 								lConfig.getMaterial()) + ".");
 				Bukkit.broadcastMessage(
 						ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Use " + ChatColor.RED + "/lottery claim" + ChatColor.WHITE + " to claim the winnings.");
 				addToWinnerList(players.get(rand), amount, lConfig.getMaterial());
 
 				addToClaimList(players.get(rand), matAmount, lConfig.getMaterial());
 			}
 			Bukkit.broadcastMessage(
					ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There was a total of " + Etc
							.realPlayersFromList(
 							players).size() + " " + Etc.pluralWording(
 							"player", Etc.realPlayersFromList(
 							players).size()) + " buying " + players.size() + " " + Etc.pluralWording(
 							"ticket", players.size()));
 
 			// Add last winner to config.
 			lConfig.setLastwinner(players.get(rand));
 			lConfig.setLastwinneramount(amount);
 
 			lConfig.setJackpot(0);
 
 
 			clearAfterGettingWinner();
 		}
 		return true;
 	}
 
 	public void clearAfterGettingWinner()
 	{
 
 		// extra money in pot added by admins and mods?
 		// Should this be removed?
 		if (lConfig.clearExtraInPot())
 		{
 			lConfig.setExtraInPot(0);
 		}
 		// Clear file.
 		try
 		{
 			final BufferedWriter out = new BufferedWriter(
 					new FileWriter(plugin.getDataFolder() + File.separator + "lotteryPlayers.txt", false));
 			out.write("");
 			out.close();
 
 		}
 		catch (IOException e)
 		{
 		}
 	}
 
 	public String formatCustomMessageLive(final String message, final Player player)
 	{
 		//Lets give timeLeft back if user provie %draw%
 		String outMessage = message.replaceAll("%draw%", timeUntil(true));
 		//Lets give timeLeft with full words back if user provie %drawLong%
 		outMessage = outMessage.replaceAll("%drawLong%", timeUntil(false));
 		// If %player% = Player name
 		outMessage = outMessage.replaceAll("%player%", player.getDisplayName());
 		// %cost% = cost
 		if (lConfig.useiConomy())
 		{
 			outMessage = outMessage.replaceAll(
 					"%cost%", String.valueOf(
 					Etc.formatAmount(lConfig.getCost(), lConfig.useiConomy())));
 		}
 		else
 		{
 			outMessage = outMessage.replaceAll(
 					"%cost%", String.valueOf(
 					(int)Etc.formatAmount(lConfig.getCost(), lConfig.useiConomy())));
 		}
 
 		// %pot%
 		outMessage = outMessage.replaceAll("%pot%", Double.toString(winningAmount()));
 		// Lets get some colors on this, shall we?
 		outMessage = outMessage.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 		return outMessage;
 	}
 }
