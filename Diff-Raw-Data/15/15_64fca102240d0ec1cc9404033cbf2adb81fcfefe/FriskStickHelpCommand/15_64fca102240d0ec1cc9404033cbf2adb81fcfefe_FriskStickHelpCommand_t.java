 package friskstick.cops.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import friskstick.cops.plugin.FriskStick;
 
 public class FriskStickHelpCommand implements CommandExecutor {
 
 	public FriskStickHelpCommand(FriskStick plugin) {
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		Player player = null;
 
 		if(sender instanceof Player) {
 
 			player = (Player)sender;
 
 		}
 
		/**
		 * Check if the command is run my a player(if not, send error message)
		 */
 		if(player == null) {
 
			sender.sendMessage(ChatColor.RED + "Error: You cannot run this command in the console!");
 
 		} else {
 
 			if(commandLabel.equalsIgnoreCase("friskstick")) {
 
 				if(player.hasPermission("friskstick.help") || player.isOp()) {
 
 					player.sendMessage(ChatColor.AQUA + "***************FriskStick Help***************");
 					player.sendMessage(ChatColor.DARK_AQUA + "This plugin frisks players for illegal items.");
 
 				} else {
 
 					player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
 
 				}
 
 			}
 
 		}
 
 		return false;
 	}
 
 }
