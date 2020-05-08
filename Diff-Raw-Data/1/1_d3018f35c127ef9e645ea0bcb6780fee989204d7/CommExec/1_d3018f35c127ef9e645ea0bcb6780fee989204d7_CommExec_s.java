 package me.fogest.mctrade.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import me.fogest.mctrade.MCTrade;
 
 public class CommExec implements CommandExecutor {
 	private MCTrade plugin;
 
 	public CommExec(final MCTrade plugin) {
 		this.plugin = plugin;
 	}
 
	@Override
 	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("mctrade")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Y U NO PLAYER??!111");
 				return true;
 			}
 			return true;
 		}
 		return false;
 	}
 }
