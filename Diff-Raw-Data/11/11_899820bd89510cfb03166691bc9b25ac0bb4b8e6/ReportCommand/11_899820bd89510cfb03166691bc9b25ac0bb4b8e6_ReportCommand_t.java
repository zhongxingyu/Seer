 package friskstick.cops.plugin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class ReportCommand implements CommandExecutor {
 
 	private FriskStick plugin;
 	Server s;
 	
 	public ReportCommand(FriskStick plugin) {
 
 		this.plugin = plugin; 
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command smd, String commandLabel, String[] args) {
 
 		Player player = (Player)sender;
 
 		if(player == null) {
 
 			sender.sendMessage("You cannot run this command in the console!");
 
 		} else {
 
 			if(commandLabel.equalsIgnoreCase("report")) {
 
				if(player.hasPermission("friskstick.report.send")) {
 
 					if(args.length == 0) {
 
 						player.sendMessage("Usage: /report <player>");
 
 					} else if(args.length == 1) {
 
 						Player reported = plugin.getServer().getPlayer(args[0]);
 
 						if(reported == player) {
 
 							player.sendMessage(ChatColor.DARK_RED + "[Report]" + ChatColor.RED + " You cannot report yourself!");
 
 						} else if(reported == null){
 							
 							player.sendMessage(ChatColor.DARK_RED + "[Report]" + ChatColor.RED + " Player does not exist or is offline!");
 							
 						} else {
							for(Player recipient : plugin.getServer().getOnlinePlayers()){
								if(recipient.hasPermission("friskstick.report.receive")){
									recipient.sendMessage(plugin.getConfig().getString("player-report-message").replaceAll("&", "").replaceAll("%snitch%", player.getName()).replaceAll("%reported%", reported.getName()));
								}
							}
 						}
 					}
 
 				}
 
 			}
 
 		}
 
 		return false;
 
 	}
 
 }
