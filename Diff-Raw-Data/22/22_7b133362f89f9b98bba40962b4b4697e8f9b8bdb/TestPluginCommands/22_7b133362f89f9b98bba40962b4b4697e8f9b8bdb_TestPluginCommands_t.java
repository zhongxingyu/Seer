 package com.williambl.plugins.testplugin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class TestPluginCommands implements CommandExecutor{
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[]args){
		
 		if (commandLabel.equalsIgnoreCase("test")){
			sender.sendMessage("You just sent me a command with argument " + args + "!");
		}
		else {
	    	sender.sendMessage("Incorrect Number of Arguments!");
 	    }
		
		return true;
		
 		
 		
 	}
 }
