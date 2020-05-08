 package com.github.roburrito.bukkit.godly;
 
 /*Might be missing a few imports here*/
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.roburrito.bukkit.godly.Godly;
 
 /*This class handles all commands beginning with "/favor"*/
 public class FavorCommand implements CommandExecutor {
  
 	/*variable and constructor only necessary if methods are used from the main class*/
 	private Godly plugin;
  
 	public FavorCommand(Godly plugin) {
 		this.plugin = plugin;
 	}
  
 	/*TODO: add all commands listed here in Plugin.yml for Godly*/
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if(sender instanceof Player) {
 			player = (Player)sender;
 		
 			if(args.length == 0) {
 				/*TODO: List the player's favors*/
 			}
 		}
 		/*Are there favor-based commands that servers can ask?*/
 		if(player == null) { 
 			sender.sendMessage(Colors.Rose + "Only players can asketh thy Gods for favors!");
 		}
 
 		return false;
 	}
 }
