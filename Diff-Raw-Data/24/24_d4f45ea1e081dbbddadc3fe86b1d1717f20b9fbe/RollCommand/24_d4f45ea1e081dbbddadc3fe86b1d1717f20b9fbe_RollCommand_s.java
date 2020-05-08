 package com.gildorymrp.gildorym;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class RollCommand implements CommandExecutor {
 
 	private final int MAXIMUM_DIE_SIZE = 100;
 	private final int MAXIMUM_NUMBER_OF_DICE = 10;
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 
 		int[] rollInfo;
 		// rollInfo[0] = Number of dice to roll, default 1
 		// rollInfo[1] = Size of dice to roll, default 20
 		// rollInfo[2] = Arithmetic roll modifier, default 0
 
 		// Attempts to parse arguments for the /roll command
 		try {
 			rollInfo = parseArgs(args);
 		} catch (NumberFormatException ex) {
 			sender.sendMessage(ChatColor.RED + "Invalid roll!");
 			return true;
 		}
 
 		// Filters out invalid arguments (rolling 0 or negative dice; rolling
 		// too many/too large)
 		if (rollInfo[0] < 1 || rollInfo[1] < 1) {
 			sender.sendMessage(ChatColor.RED
 					+ "You can't roll a zero or negative number!");
 			return true;
 		} else if (rollInfo[0] > MAXIMUM_NUMBER_OF_DICE) {
 			sender.sendMessage(ChatColor.RED
 					+ "You can't roll that many times!");
 			return true;
 		} else if (rollInfo[1] > MAXIMUM_DIE_SIZE) {
 			sender.sendMessage(ChatColor.RED
 					+ "You can't roll that large of a number!");
 			return true;
 		}
 
 		// Performs rolls based on rollInfo.
 		int[] rolls = roll(rollInfo);
 
 		// Generates output message and calculates overall total roll.
 		String output = ChatColor.GRAY + "(";
 		Integer rollTotal = 0;
 		for (Integer roll : rolls) {
 			output += roll;
 			output += "+";
 			rollTotal += roll;
 		}
 		rollTotal += rollInfo[2];
 		output += rollInfo[2] + ") = " + rollTotal;
 
 		if (sender instanceof Player) {
 			// Generates human-friendly message when player runs the command
 			String message = ChatColor.BLUE + sender.getName() + ChatColor.GRAY
 					+ "/" + ChatColor.BLUE + ((Player) sender).getDisplayName()
 					+ ChatColor.GRAY + " rolled " + ChatColor.YELLOW
 					+ rollInfo[0] + "d" + rollInfo[1];
 			if (rollInfo[2] > 0) {
 				message = message + "+" + rollInfo[2];
 			} else if (rollInfo[2] < 0) {
 				message = message + "-" + rollInfo[2];
 			}
 
 			// Sends message and output to all players within 24 of sender
 			for (Player player : ((Player) sender).getWorld().getPlayers()) {
 				if (player.getLocation().distance(
 						((Player) sender).getLocation()) <= 24) {
 					player.sendMessage(message);
 					player.sendMessage(output);
 				}
 			}
 		} else {
 			// Sends message to console when run by console
 			sender.sendMessage(output);
 		}
 		return true;
 	}
 
 	// Parses the arguments for the roll command
 	// Returns an array of 3 integers for the number of dice to roll,
 	// the die size and
 	// the arithmetic modifier for the overall roll, respectively.
 	private int[] parseArgs(String[] args) throws NumberFormatException {
 		int[] rollInfo = new int[3];
 		String rollString = args[0];
 
		if (args[0] == null) {
 			// If no arguments are given, defaults to 1d20+0
 			return new int[] { 1, 20, 0 };
 		}
 
 		// If roll is in form xd?, sets the amount of dice rolled to x
 		if (rollString.contains("d")) {
 			String amountString = rollString.split("d")[0];
 			rollInfo[0] = Integer.parseInt(amountString);
			rollString.substring(amountString.length() + 1);
 		} else {
 			rollInfo[0] = 1;
 		}
 
 		// If roll is in form ?+x, sets arithmetic modifier of roll to x
 		if (rollString.contains("+")) {
 			String modString = rollString.split("+")[1];
 			rollInfo[2] = Integer.parseInt(modString);
			rollString = rollString.substring(0, rollString.length()
					- modString.length() - 1);
 		}
 
 		// If roll is in form ?-x, sets arithmetic modifier of roll to -x
 		if (rollString.contains("-")) {
			String modString = rollString.split("-")[1];
			rollInfo[2] = -1 * Integer.parseInt(modString);
			rollString = rollString.substring(0, rollString.length()
					- modString.length() - 1);
 		}
 
 		// What remains of the string is set as the size of the dice to be used
 		rollInfo[1] = Integer.parseInt(rollString);
 
 		return rollInfo;
 	}
 
 	// Performs rollInfo[0] rolls of a rollInfo[1] sized die
 	private int[] roll(int[] rollInfo) {
 		int[] rolls = new int[rollInfo[0]];
 		for (int i = 0; i < rollInfo[0]; i++) {
 			rolls[i] = new Random().nextInt(rollInfo[1]) + 1;
 		}
 		return rolls;
 	}
 
 }
