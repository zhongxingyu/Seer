 package friskstick.cops.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import friskstick.cops.plugin.FriskStick;
 
 /**
  * 
  * The class that is responsible for handling the '/report [player]' command. 
  *
  */
 
 public class ReportCommand implements CommandExecutor {
 
 	private FriskStick plugin;
 	private Server s;
 	
 	/**
 	 * The constructor for {@link ReportCommand this} class.
	 * @param plugin The {@link FriskStick} object required for the command to function.
 	 */
 
 	public ReportCommand(FriskStick plugin) {
 
 		this.plugin = plugin; 
 
 	}
 	
 	/**
 	 * The method that, in this case, executes the '/report [player]' command.
 	 */
 
 	public boolean onCommand(CommandSender sender, Command smd, String commandLabel, String[] args) {
 
 		Player player = null;
 
 		if(sender instanceof Player) {
 
 			player = (Player)sender;
 
 		}
 
 		if(player == null) {
 
 			sender.sendMessage("You cannot run this command in the console!");
 
 		} else {
 
 			if(commandLabel.equalsIgnoreCase("report")) {
 
 				if(plugin.getConfig().getBoolean("allow-reporting")) {
 
 					if(player.hasPermission("friskstick.report.send") || player.isOp()) {
 
 						if(args.length == 0) {
 
 							player.sendMessage("Usage: /report <player>");
 
 						} else if(args.length == 1) {
 
 							Player reported = plugin.getServer().getPlayer(args[0]);
 
 							if(reported == player) {
 
 								player.sendMessage(ChatColor.DARK_RED + "[Report]" + ChatColor.RED + " You cannot report yourself!");
 
 							} else if(reported == null){
 
 								player.sendMessage(ChatColor.DARK_RED + "[Report]" + ChatColor.RED + " Player does not exist or is offline!");
 
 							} else {
 
 								for(Player recipient : plugin.getServer().getOnlinePlayers()) {
 
 									if(recipient.hasPermission("friskstick.report.receive") || recipient.isOp()) {
 
 										recipient.sendMessage(plugin.getConfig().getString("player-report-message").replaceAll("&", "").replaceAll("%snitch%", player.getName()).replaceAll("%reported%", reported.getName()));
 
 									}
 
 								}
 
 							}
 
 						}
 
 					} else {
 
 						player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use this command!");
 
 					}
 
 				}
 
 			}
 
 		}
 
 		return false;
 
 	}
 
 }
