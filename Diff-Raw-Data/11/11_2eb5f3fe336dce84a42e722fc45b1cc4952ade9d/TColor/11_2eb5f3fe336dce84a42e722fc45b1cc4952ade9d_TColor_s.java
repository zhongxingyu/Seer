 package net.mcforge.plugin.commands;
 
 import net.mcforge.API.CommandExecutor;
 import net.mcforge.API.plugin.Command;
 import net.mcforge.iomodel.Player;
 import net.mcforge.plugin.Main.Main;
 import net.mcforge.server.Server;
 
 public class TColor extends Command {
 
 	@Override
 	public String getName() {
 		return "tcolor";
 	}
 
 	@Override
 	public String[] getShortcuts() { 
 		return new String[] { "titlecolor", "tcolour", "titlecolour" };
 	} //not really shortcuts, but just there so it's easier to type
 
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
 			executor.sendMessage("Please specify a tcolor!");
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
 		
 		String prefix = who.getPrefix();
 		if (prefix == null) {
 			prefix = parsedColor.toString();
 		}
 		else {
 			if (prefix.startsWith("["))
 				prefix = prefix.substring(1);
 			else if (prefix.startsWith("&") && prefix.charAt(2) == '[')
 				prefix = prefix.substring(0, 2) + prefix.substring(3, prefix.length());
 			else
 				executor.sendMessage(prefix);
 			
 			if (prefix.endsWith("] "))
 				prefix = prefix.substring(0, prefix.length() - 2);
 			
 			if (!prefix.startsWith("&") || prefix.length() == 1) {
 				if (prefix.charAt(prefix.length() - 2) == '&') {
 					prefix = prefix.substring(0, prefix.length() - 2);
 				}
 				prefix = parsedColor + "[" + prefix + parsedColor + "] ";
 			}
 			else {
 				prefix = prefix.substring(2);
 				if (prefix.charAt(prefix.length() - 2) == '&') {
 					prefix = prefix.substring(0, prefix.length() - 2);
 				}
 				prefix = parsedColor + "[" + prefix + parsedColor + "] ";
 			}
 		}
 		who.setPrefix(prefix);
		s.sendGlobalMessage(who.getDisplayName() + s.defaultColor + " got the tcolor " + parsedColor + parsedColor.getName());
 	}
 	
 	@Override
 	public void help(CommandExecutor executor) {
 		executor.sendMessage("/tcolor <player> <color> - changes the player's title color");
 		if (executor instanceof Player)
 			executor.sendMessage("/tcolor <color> - changes your title color");
 		Main.displayValidColors(executor);
 	}
 }
