 package com.TeamNovus.Supernaturals.Commands.Common;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import com.TeamNovus.Supernaturals.Permission;
 import com.TeamNovus.Supernaturals.Supernaturals;
 import com.TeamNovus.Supernaturals.Commands.BaseCommand;
 import com.TeamNovus.Supernaturals.Commands.CommandManager;
 import com.TeamNovus.Supernaturals.Util.StringUtil;
 
 public class DefaultCommands {
 
 	@BaseCommand(aliases = { "help", "?" }, desc = "View all commands and their info.", usage = "[Page]", permission = Permission.NONE, min = 0, max = 1, hidden = true)
 	public void helpCmd(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// help [Page]
 		int max = 6;
 		
		if(args.length != 1) {
			if(!(StringUtil.isInteger(args[1])) || Math.abs(Integer.valueOf(args[1])) * max - max >= CommandManager.getCommands().size()) {
 				sender.sendMessage(ChatColor.RED + "The specified page was not found.");
 				return;
 			}
 		}
 		
		int page = args.length == 1 ? 1 : Math.abs(Integer.valueOf(args[1]));
 		int total = 0;
 		sender.sendMessage(CommandManager.getDark() + "______________[ " + CommandManager.getLight() + Supernaturals.getPlugin().getName() + CommandManager.getDark() + " ]______________");
 
 		sender.sendMessage(ChatColor.GRAY + "Required: < > Optional: [ ]");
 		for (int i = max * page - max; i < CommandManager.getCommands().size() && total < max - 1; i++) {
 			BaseCommand command = CommandManager.getCommands().get(i);
 			if(!(command.hidden())) {
 				sender.sendMessage(CommandManager.getDark() + "- " + "/" + commandLabel + " " + command.aliases()[0] + (command.usage() != "" ? " " + command.usage() : "") + CommandManager.getLight() + " - " + CommandManager.getDark() + command.desc());
 				total++;
 			}
 		}
 		sender.sendMessage(CommandManager.getLight() + "For help type: " + CommandManager.getDark() + "/" + commandLabel + " help [Page]");
 	}
 	
 }
