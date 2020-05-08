 package ru.flydev.kartograf.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import ru.flydev.kartograf.FlyClasses;
 
 public class FRpg implements CommandExecutor {
 	private FlyClasses plugin;
 
 	public FRpg(FlyClasses plugin){
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("frpg")){ // If the player typed /basic then do the following...
			sender.sendMessage("Yes!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("frpg")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("This command can only be run by a player.");
 			}
			return true;
 		}
 		return false;
 	}
 }
