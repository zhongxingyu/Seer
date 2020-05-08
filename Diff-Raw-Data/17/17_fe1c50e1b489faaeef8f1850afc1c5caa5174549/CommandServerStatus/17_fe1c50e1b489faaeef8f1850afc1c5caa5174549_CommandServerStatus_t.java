 package me.reddy360.theholyflint;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommandServerStatus implements CommandExecutor {
 	PluginMain pluginMain;
 	public CommandServerStatus(PluginMain pluginMain) {
 		this.pluginMain = pluginMain;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if(!sender.hasPermission("thf.serverstatus")){
 			sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
 			return true;
 		}
 		if(!pluginMain.serverStatusSigns){
 			sender.sendMessage(ChatColor.DARK_RED + "You haven't setup a Server Status Sign");
 			return true;
 		}
 		if(args.length != 4){
 			sender.sendMessage("You need 4 arguments, if you're not using a line just do '&r' on the argument");
 			return true;
 		}
		Signs.setServerStatus(toString(args).split("::"));
 		return false;
 	}
	private String toString(String[] args) {
		String returnValue = "";
		for(String message : args){
			returnValue = returnValue + message;
		}
		return returnValue;
	}
 
 }
