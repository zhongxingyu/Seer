 package ru.cubelife.chat;
 
import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandsExecutor implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player))
 			return false;
 		
 		String cmdName = cmd.getName();
 		if(cmdName.equalsIgnoreCase("global") || cmdName.equalsIgnoreCase("g")) {
 			Utils.changeMode((Player) sender, ChatMode.GLOBAL);
 		} else if(cmdName.equalsIgnoreCase("private") || cmdName.equalsIgnoreCase("p")) {
 			Utils.changeMode((Player) sender, ChatMode.PRIVATE);
 		} else if(cmdName.equalsIgnoreCase("sale") || cmdName.equalsIgnoreCase("s")) {
 			Utils.changeMode((Player) sender, ChatMode.SALE);
 		} else if(cmdName.equalsIgnoreCase("help") || cmdName.equalsIgnoreCase("h")) {
 			Utils.changeMode((Player) sender, ChatMode.HELP);
		} else if(cmdName.equalsIgnoreCase("chat")) {
			sender.sendMessage(ChatColor.RED + ": Mayor, smilesdc. DevInLive.");
 		}
 		return false;
 	}
 
 }
