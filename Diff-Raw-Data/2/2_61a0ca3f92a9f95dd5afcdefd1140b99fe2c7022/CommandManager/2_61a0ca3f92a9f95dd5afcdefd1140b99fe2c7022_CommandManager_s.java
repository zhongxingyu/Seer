 package net.nabaal.majiir.realtimerender;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommandManager implements CommandExecutor {
 	
 	private final RealtimeRender plugin;
 	
 	public CommandManager(RealtimeRender plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args[0].equals("zooms")) {
 			if (!sender.isOp()) {
 				sender.sendMessage(ChatColor.RED + "You must be an op to do that!");
 				return true;
 			}
 			plugin.setRedoZooms(true);
 			sender.sendMessage(ChatColor.GREEN + "All zoom levels will be regenerated during the next cycle.");
 			return true;
 		}
 		return false;
 	}
 
 }
