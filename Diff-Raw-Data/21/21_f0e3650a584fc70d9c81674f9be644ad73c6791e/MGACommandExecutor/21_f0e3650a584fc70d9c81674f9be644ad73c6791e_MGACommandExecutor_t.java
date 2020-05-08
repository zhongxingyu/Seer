 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class MGACommandExecutor implements CommandExecutor {
 
 	public MGACommandExecutor(Main plugin) {
 	}
 	@Override
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 			if(cmd.getName().equalsIgnoreCase("mga")){
				if (args.length == 0){
					return false;
				}
 				if (args[0].equalsIgnoreCase("version")) {
 				//Show version to sender and return true
 				sender.sendMessage("MineguildAdmin V0.3");
 				return true;
 			}
			return false;
 			}
			return false;
 	}
 
 }
 
