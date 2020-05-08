 package me.benphelps.Plots;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PlotsCommandExecutor implements CommandExecutor {
 
 	private Plots plots;
 
 	public PlotsCommandExecutor(Plots plots) {
 		this.plots = plots;
 		plots.getLogger().info("Command Executor Start");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		plots.getLogger().info("Command Part 1");
 		// Sanity Checks
 		if (args.length > 4) {
 			sender.sendMessage("Too many arguments!");
 			return false;
 		} 
 		if (args.length < 2) {
 			sender.sendMessage("Not enough arguments!");
 			return false;
 		}
 		if (sender instanceof Player == false) {
 			sender.sendMessage("You must be a player!");
 			return false;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("new")){
 			sender.sendMessage("Woot mutherfucker!");
 			plots.creating = true;
 			return true;
 		}
 		
 		return false;
 	}
 
 }
