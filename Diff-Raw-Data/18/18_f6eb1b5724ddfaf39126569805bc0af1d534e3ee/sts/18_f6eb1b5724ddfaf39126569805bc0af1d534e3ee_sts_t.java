 package net.othercraft.steelsecurity.commands;
 
 import org.bukkit.command.CommandSender;
 
 public class sts {
 
 	public void command(CommandSender sender, String[] split) {
 		if (split.length==0) {
 			if (sender.hasPermission("steelsecurity.commands.sts")){
 				sender.sendMessage("This server is running Steel Security");
 				sender.sendMessage("For a list of commands type /sts help");
 			}
 		}
 		else {
			if (split[0].equalsIgnoreCase("help")){
 				stshelp sh = new stshelp();
 				sh.sts(sender, split);
 			}
 		}
 	}
 
 }
