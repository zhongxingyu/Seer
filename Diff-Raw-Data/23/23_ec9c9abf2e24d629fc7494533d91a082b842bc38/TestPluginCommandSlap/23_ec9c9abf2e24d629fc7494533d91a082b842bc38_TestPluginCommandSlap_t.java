package tk.morrigan.badb.TestPlugin.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TestPluginCommandSlap {
 	
 	public static Boolean run(CommandSender sender, String label, String[] args) {
 		Player target = null;
 		String tgt = "himself";
 		int dmg = 2;
 		String dmgSpec = ".";
 		if (args.length == 0) {
 			target = (Player) sender;
 		} else if (args.length == 1) {
 			if (Integer.valueOf(args[0]) != null) {
 				target = (Player) sender;
 				dmg = Integer.valueOf(args[0]);
 				dmgSpec = " for " + dmg + " damage.";
 			} else {
 				target = Bukkit.getServer().getPlayerExact(args[0]);
 				tgt = target.getName();
 			}
 		} else if (args.length == 2) {
 			target = Bukkit.getServer().getPlayerExact(args[0]);
 			tgt = target.getName();
 			dmg = Integer.valueOf(args[1]);
 			dmgSpec = " for " + dmg + " damage.";
 		}
 		target.damage(dmg);
 		if (target.isDead()) {
 			Bukkit.getServer().broadcastMessage(sender.getName() + " slapped " + tgt + " to death.");
 		} else {
 			Bukkit.getServer().broadcastMessage(sender.getName() + " slapped " + tgt + dmgSpec);
 		}
 		return true;
 	}
 	
 }
