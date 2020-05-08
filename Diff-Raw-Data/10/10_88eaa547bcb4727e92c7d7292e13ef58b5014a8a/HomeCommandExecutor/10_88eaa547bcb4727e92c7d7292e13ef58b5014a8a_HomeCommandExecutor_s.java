 package de.lycake.CakeHomePlugin;
 
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class HomeCommandExecutor implements CommandExecutor{
 	CakeHomePlugin plugin_;
 	public HashMap<Player, Location> homes_;
 	
 	public HomeCommandExecutor(CakeHomePlugin plugin){
 		plugin_ = plugin;
 		homes_ = plugin.homes_;
 	}
 
 	/**
 	 * Will execute when a player types a command
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player;
 		
 		// Home
 		if (cmd.getName().equalsIgnoreCase("home") && sender instanceof Player){	
 			if (args.length == 0){
 				player = (Player) sender;
 				if (!homes_.containsKey(player)){
 					player.sendMessage("You have never set your home");
 					return false;
 				}
 				
				Location loc = homes_.get(player);
 				player.teleport(loc);
 				
 				return true;
 			} else if (args.length > 1 && args[0].equalsIgnoreCase("set")){
 				player = (Player) sender;
				homes_.put(player, player.getLocation());
 				saveHomes();
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Saves the homes to file
 	 */
 	public void saveHomes(){
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("homes.cak"));
 			oos.writeObject(homes_);
 			oos.flush();
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 }
