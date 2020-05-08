 package me.reddy360.theholyflint.command;
 
 import me.reddy360.theholyflint.PluginMain;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandSetTP implements CommandExecutor {
 	PluginMain pluginMain;
 	public CommandSetTP(PluginMain pluginMain) {
 		this.pluginMain = pluginMain;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label,
 			String[] args) {
 		if(!sender.hasPermission(pluginMain.pluginManager.getPermission("thf.settp"))){
 			sender.sendMessage(ChatColor.DARK_RED + "THERE ALL DEAD!");
 			return true;
 		}else if(!(sender instanceof Player)){
 			sender.sendMessage(ChatColor.DARK_RED + "No player, no coords, no service.");
 			return true;
 		}else if(args.length == 1){
 			Player player = (Player) sender;
			pluginMain.getConfig().set("coords." + args[0], player.getLocation().getWorld() + ":" + player.getLocation().getBlockX() 
 					+ ":" + player.getLocation().getBlockY() + ":" + player.getLocation().getBlockZ());
 			pluginMain.saveConfig();
 			player.sendMessage(ChatColor.GREEN + "TP Sign Coord set at" + pluginMain.getConfig().getString("coords." + args[0]));
 			return true;	
 		}
 		sender.sendMessage(ChatColor.DARK_RED + "Invalid Syntax. /settp [name]");
 		return true;
 	}
 
 }
