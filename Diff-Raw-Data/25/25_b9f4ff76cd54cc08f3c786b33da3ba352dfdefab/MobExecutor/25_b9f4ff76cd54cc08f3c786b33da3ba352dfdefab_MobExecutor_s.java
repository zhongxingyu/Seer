package me.cain.mobspawn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 
 public class MobExecutor implements CommandExecutor
 {
 	static Util util;
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args)
 	{
 		if(s instanceof Player) {
 			if(c.getName().equalsIgnoreCase("mob")) {
 				if(args.length < 1) { return false; }
 				else {
 					if(s.hasPermission("mob.spawn")) {
 						try {
 							((Player) s).getWorld().spawnCreature(((Player) s).getTargetBlock(null, 0).getLocation(), CreatureType.valueOf(args[0].toUpperCase()));
 						} catch (Exception e) {
 							util.deny((Player) s, "Something went wrong. Try again?");
 						}
 						util.success((Player) s, "Mob spawned successfully!");
 					} else {
 						util.deny((Player) s, "Mob spawn denied. No permission.");
 					}
 				}
 			}
 		} else {
 			s.sendMessage(ChatColor.RED + "You must be in-game to spawn a mob.");
 		}
 		return false;
 	}
 
 }
