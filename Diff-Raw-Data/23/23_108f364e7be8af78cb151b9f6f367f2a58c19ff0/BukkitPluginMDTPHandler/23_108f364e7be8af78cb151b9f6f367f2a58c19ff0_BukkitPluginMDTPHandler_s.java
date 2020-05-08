 package io.github.md678685.BukkitPluginMD;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BukkitPluginMDTPHandler implements CommandExecutor {
 	
	@SuppressWarnings("unused")
 	private BukkitPluginMD plugin;
 
 	public BukkitPluginMDTPHandler(BukkitPluginMD plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("MDTP")){
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("You can't teleport someone to the console!");
 				return true;
 			} else {
 				if (args.length == 1) {
					
 				}
 			}
 		}
 		return false;
 	}
 
 }
