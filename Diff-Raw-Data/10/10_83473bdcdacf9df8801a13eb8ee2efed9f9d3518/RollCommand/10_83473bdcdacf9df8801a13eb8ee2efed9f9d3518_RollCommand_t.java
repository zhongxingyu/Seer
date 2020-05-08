 package com.gildorymrp.gildorym;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class RollCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Integer amount = 1;
 		Integer maxRoll = 20;
 		Integer plus = 0;
 		
 		String rollString = args[0];
		String secondHalf;
 		if (rollString.contains("d")) {
 			amount = Integer.parseInt(rollString.split("d")[0]);
			secondHalf = rollString.split("d")[1];
		} else {
			secondHalf = args[0];
 		}
 		if (amount >= 100) {
 			sender.sendMessage(ChatColor.RED + "You can't roll that many times!");
 			return true;
 		}
 		if (rollString.contains("+")) {
 			plus = Integer.parseInt(secondHalf.split("+")[1]);
 			maxRoll = Integer.parseInt(secondHalf.split("+")[0]);
 		} else {
 			maxRoll = Integer.parseInt(secondHalf);
 		}
 		Set<Integer> rolls = new HashSet<Integer>();
 		Random random = new Random();
 		for (int i = 0; i < amount; i++) {
			rolls.add(random.nextInt(maxRoll) + 1);
 		}
 		String output = ChatColor.GRAY + "(";
 		Integer rollTotal = 0;
 		for (Integer roll : rolls) {
 			output += roll;
 			output += "+";
 			rollTotal += roll;
 		}
 		output += plus + ") = " + rollTotal;
 		if (sender instanceof Player) {
 			for (Player player : ((Player) sender).getWorld().getPlayers()) {
 				if (player.getLocation().distance(((Player) sender).getLocation()) <= 16) {
 					player.sendMessage(output);
 				}
 			}
 		} else {
 			sender.sendMessage(output);
 		}
 		return true;
 	}
 
 }
