 package pro.homiecraft.Commands;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import pro.homiecraft.TheCommandBook;
 import pro.homiecraft.Config.warpConfig;
 
 public class tcbWarp implements CommandExecutor {
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender.hasPermission("tcb.warp")) {
 			if (cmd.getName().equalsIgnoreCase("warp")) {
 				if (args.length < 1){
 					sender.sendMessage("Missing warp name!");
 					sender.sendMessage("usage: /warp warpName");
 					return true;
 				}else if(args.length > 1){
 					sender.sendMessage("To many arguments!");
 					sender.sendMessage("usage: /warp warpName");
 					return true;
 				}else{
 					Player player = (Player) sender;
 					
 					String cworld = player.getWorld().getName();
 					
 					File warpFile = new File(TheCommandBook.pluginST.getDataFolder() + "/data/warps/" + args[0] + ".yml");
 					if (warpFile.exists()){
 						warpConfig.reloadWarpConfig(args[0]);
 						
 						int x = warpConfig.getWarpConfig(args[0]).getInt(args[0] + "." + cworld + ".X");
 						int y = warpConfig.getWarpConfig(args[0]).getInt(args[0] + "." + cworld + ".Y");
 						int z = warpConfig.getWarpConfig(args[0]).getInt(args[0] + "." + cworld + ".Z");
 						float yaw = warpConfig.getWarpConfig(args[0]).getInt(args[0] + "." + cworld + ".yaw");
 						float pitch = warpConfig.getWarpConfig(args[0]).getInt(args[0] + "." + cworld + ".pitch");
 						
 						Location warp = new Location(Bukkit.getWorld(cworld), x, y, z);
						player.teleport(warp);
 						player.getLocation().setPitch(pitch);
 						player.getLocation().setYaw(yaw);
 						
 						player.sendMessage("Warping to: " + args[0]);
 						return true;
 					}else{
 						sender.sendMessage("Warp " + args[0] + " does not exists!");
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
