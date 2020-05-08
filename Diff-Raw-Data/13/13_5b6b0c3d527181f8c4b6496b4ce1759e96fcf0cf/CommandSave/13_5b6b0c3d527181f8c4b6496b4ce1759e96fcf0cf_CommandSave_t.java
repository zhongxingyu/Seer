 package me.arno.blocklog.commands;
 
 import me.arno.blocklog.BlockLog;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 
 public class CommandSave extends BlockLogCommand {
 	public CommandSave(BlockLog plugin) {
 		super(plugin, "blocklog.save", true);
 	}
 
 	public boolean execute(Player player, Command cmd, String[] args) {
 		if(args.length > 1) {
 			player.sendMessage(ChatColor.WHITE + "/bl save [amount|all]");
 			return true;
 		}
 		
 		if(!hasPermission(player)) {
 			player.sendMessage("You don't have permission");
 			return true;
 		}
 		
		if(args.length == 0) {
			plugin.saveLogs(100, player);
		} else if(args.length == 1) {
			if(args[0].equalsIgnoreCase("all")) {
				plugin.saveLogs(0, player);
			} else {
				plugin.saveLogs(Integer.valueOf(args[0]), player);
			}
 		}
 		return true;
 	}
 
 }
