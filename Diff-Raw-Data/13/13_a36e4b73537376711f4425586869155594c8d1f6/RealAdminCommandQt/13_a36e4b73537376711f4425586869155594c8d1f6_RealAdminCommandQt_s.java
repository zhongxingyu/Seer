 package fr.crafter.tickleman.realadmintools;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class RealAdminCommandQt
 {
 
 	//--------------------------------------------------------------------------------------- command
 	static boolean command(RealAdminToolsPlugin plugin, CommandSender sender, String[] args)
 	{
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			Location location = new Location(
 				player.getWorld(),
 				player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()
 			);
 			if (args.length == 2) {
				location.setX(new Double(args[0]));
				location.setZ(new Double(args[1]));
				location.setY(player.getWorld().getHighestBlockAt(location).getY());
 			} else if (args.length == 3) {
				location.setX(new Double(args[0]));
				location.setY(new Double(args[1]));
				location.setZ(new Double(args[2]));
 			} else {
 				return false;
 			}
 			player.sendMessage(
 				"teleport to "
 				+ location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()
 			);
 			player.teleport(location);
 		}
 		return true;
 	}
 
 }
