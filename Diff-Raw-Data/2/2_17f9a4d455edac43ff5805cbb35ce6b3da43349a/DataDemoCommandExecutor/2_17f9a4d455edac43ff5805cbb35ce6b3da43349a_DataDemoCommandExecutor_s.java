 package edu.unca.rbruce.DataDemo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.google.common.base.Joiner;
 
 /*
  * This is a sample CommandExectuor
  */
 public class DataDemoCommandExecutor implements CommandExecutor {
 	private final DataDemo plugin;
 
 	/*
 	 * This command executor needs to know about its plugin from which it came
 	 * from
 	 */
 	public DataDemoCommandExecutor(DataDemo plugin) {
 		this.plugin = plugin;
 	}
 
 	/*
 	 * On command set the sample message
 	 */
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED
 					+ "you must be logged on to use these commands");
 			return false;
 		} else if (command.getName().equalsIgnoreCase("god")
 				&& sender.hasPermission("DataDemo.god")) {
 			Player fred = (Player) sender;
 			plugin.setMetadata(fred, "god", true, plugin);
 			sender.sendMessage(ChatColor.RED + fred.getName()
 					+ " you are a god now");
 			plugin.logger.info(fred.getName() + " has been made a god");
 			return true;
 		} else if (command.getName().equalsIgnoreCase("human")
 				&& sender.hasPermission("DataDemo.god")) {
 			Player fred = (Player) sender;
 			plugin.setMetadata(fred, "god", false, plugin);
 			sender.sendMessage(ChatColor.RED + fred.getName()
 					+ " you are human now");
 			plugin.logger.info(fred.getName() + " is no longer a god");
 			return true;
 		} else if (command.getName().equalsIgnoreCase("message")
				&& sender.hasPermission("DataDemo.message") && args.length == 0) {
 			this.plugin.getConfig().set("sample.message",
 					Joiner.on(' ').join(args));
 			return true;
 
 		} else {
 			return false;
 		}
 	}
 
 }
