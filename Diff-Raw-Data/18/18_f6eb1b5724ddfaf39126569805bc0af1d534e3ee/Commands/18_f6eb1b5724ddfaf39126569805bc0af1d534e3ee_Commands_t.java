 package net.othercraft.steelsecurity.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.othercraft.steelsecurity.Main;
 
 public class Commands implements CommandExecutor{
 
 	public Commands(Main main) {
 		
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] split) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
		if (cmd.getName().equalsIgnoreCase("sts")){
 			sts s = new sts();
 			s.command(sender, split);
			return true;
 		}
 		return false;
 	}
 
 }
