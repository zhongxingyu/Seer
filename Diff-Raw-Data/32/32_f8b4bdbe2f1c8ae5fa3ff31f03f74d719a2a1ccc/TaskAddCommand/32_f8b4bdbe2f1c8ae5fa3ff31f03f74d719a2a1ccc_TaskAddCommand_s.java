 package org.fuzzyslippers.ToDoCraft.commands;
 
 //import lib.PatPeter.*;
 
 import java.sql.SQLException;
 
 import org.fuzzyslippers.ToDoCraft.commands.MiscFunctions;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class TaskAddCommand implements CommandExecutor {
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("addtask")){
 			String sendername = sender.getName();
 			String priority = "";
 			if(args[0].equalsIgnoreCase("High")){
 				priority = "High";
 			}else if (args[0].equalsIgnoreCase("Normal")){
 				priority = "Normal";
 			}else if (args[0].equalsIgnoreCase("Low")){
 				priority = "Low";
 			} else {
 				return false;
 			}
 			String task = MiscFunctions.argsToString(args, 1, " ");
			try {
				MiscFunctions.writeDBTasks(priority, sendername, task);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				sender.sendMessage(ChatColor.DARK_RED+"Something went wrong when adding your task! Contact an admin!");
				MiscFunctions.report(e.toString());
			}
 			return true;
 		}
 		return false;
 	}
 }
