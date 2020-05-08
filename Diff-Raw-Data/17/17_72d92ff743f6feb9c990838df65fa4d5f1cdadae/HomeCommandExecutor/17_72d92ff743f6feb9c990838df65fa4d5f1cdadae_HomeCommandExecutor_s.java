 package de.lycake.CakeHomePlugin;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class HomeCommandExecutor implements CommandExecutor{
 	private CakeHomePlugin plugin_;
 	
 	public HomeCommandExecutor(CakeHomePlugin plugin){
 		plugin_ = plugin;
 	}
 
 	/**
 	 * Will execute when a player types a command
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player = null;
 		Location loc;
 		HashMap<String, double[]> homes = plugin_.getHomes();
 		
 		
 		// Home
 		if (cmd.getName().equalsIgnoreCase("home") && sender instanceof Player){	
 			player = (Player) sender;
 			String playername = player.getName();
 			if (args.length == 0){
 				if (!homes.containsKey(playername)){
 					player.sendMessage("You have never set your home");
 					return false;
 				} else {
 					double[] coords = homes.get(playername);
					loc = new Location(player.getWorld(), coords[0], coords[1], coords[2]);
					loc.setY(Math.ceil(loc.getY()));
 					player.teleport(loc);
 					return true;
 				}
 			} else if (args.length > 0 && args[0].equalsIgnoreCase("set")){
 				loc = player.getLocation();
 				loc.setY(Math.ceil(loc.getY()));
 				
 				if (homes.containsKey(playername))
 					homes.remove(playername);
 				double[] coords = new double[3];
 				coords[0] = loc.getX();
 				coords[1] = loc.getY();
 				coords[2] = loc.getZ();
 				homes.put(playername, coords);
 				HomeUtils.saveHomes(homes);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 
 }
