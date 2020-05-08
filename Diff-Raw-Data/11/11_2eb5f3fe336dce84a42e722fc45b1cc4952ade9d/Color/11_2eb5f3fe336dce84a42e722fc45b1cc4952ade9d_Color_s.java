 package net.mcforge.plugin.commands;
 
 import net.mcforge.API.CommandExecutor;
 import net.mcforge.API.plugin.Command;
 import net.mcforge.iomodel.Player;
 import net.mcforge.plugin.Main.Main;
 import net.mcforge.server.Server;
 
 public class Color extends Command {
 
 	@Override
 	public String getName() {
 		return "color";
 	}
 
 	@Override
 	public String[] getShortcuts() {
 		return new String[0];
 	}
 
 	@Override
 	public int getDefaultPermissionLevel() {
 		return 0;
 	}
 
 	@Override
 	public boolean isOpCommandDefault() {
 		return false;
 	}
 
 	@Override
 	public void execute(CommandExecutor executor, String[] args) {
 		if (args.length == 0) {
 			executor.sendMessage("Please specify a color!");
 			Main.displayValidColors(executor);
 			return;
 		}
 		Server s = executor.getServer();
 		Player who;
 		if (args.length == 1) {
 			if (executor instanceof Player) {
 				who = (Player)executor;
 			}
 			else {
 				executor.sendMessage("You need to specify a player!");
 				return;
 			}
 		}
 		else {
 			who = s.findPlayer(args[0]);
 			if (who == null) {
 				executor.sendMessage("Player not found!");
 				return;
 			}
 		}
 		String color = args.length == 1 ? args[0] : args[1];
 		net.mcforge.chat.ChatColor parsedColor;
 		if (s.VERSION.equals("6.0.0b5")) //Time to work my gypsy magic
 		    parsedColor = net.mcforge.chat.ChatColor.parse(net.mcforge.backwardscompatible.ChatColor.fromName(color).getColor());
 		else
 		    parsedColor = net.mcforge.chat.ChatColor.fromName(color);
 		if (parsedColor == null) {
 			executor.sendMessage("Invalid color!");
 			Main.displayValidColors(executor);
 			return;
 		}
 		who.setDisplayColor(parsedColor);
		s.sendGlobalMessage(who.getDisplayName() + s.defaultColor + " got the color " + parsedColor + parsedColor.getName());
 	}
 	
 	@Override
 	public void help(CommandExecutor executor) {
 		executor.sendMessage("/color <player> <color> - changes the player's color");
 		if (executor instanceof Player)
 			executor.sendMessage("/color <color> - changes your color");
 		Main.displayValidColors(executor);
 	}
 }
