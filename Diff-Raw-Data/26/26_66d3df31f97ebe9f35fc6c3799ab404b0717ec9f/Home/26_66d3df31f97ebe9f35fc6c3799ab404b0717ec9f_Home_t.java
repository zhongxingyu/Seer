 package uk.co.thefailboat.TFBCommon.command;
 
 import org.bukkit.ChatColor;
import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.thefailboat.TFBCommon.Main;
 import uk.co.thefailboat.TFBCommon.Objects.SerializableLocation;
 
 public class Home implements CommandExecutor{
 	
 	private Main instance;
 	
 	public Home(Main _instance){
 		this.instance = _instance;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player)) return false;
 		Player player = (Player)sender;
 		String playerLowerName = player.getDisplayName().toLowerCase();
 		
 		if(cmd.getName().toLowerCase().equals("home")){
 			
 			if(instance.Homes.containsKey(playerLowerName)){
				Location loc = instance.Homes.get(playerLowerName).getLocation();
				if(loc.getChunk().load()){
					player.teleport(loc);
					player.sendMessage(ChatColor.AQUA + "Welcome Home!");
				}else{
					player.sendMessage(ChatColor.RED + "Could not load the chunk!");
				}
 				
 				player.sendMessage(ChatColor.AQUA + "Welcome Home!");
 				return true;
 			}
 			else{
 				player.sendMessage(ChatColor.RED + "You have no Home!");
 				return true;
 			}
 		}
 		else if(cmd.getName().toLowerCase().equals("sethome")){
 			
 			if(instance.Homes.containsKey(playerLowerName)) instance.Homes.remove(playerLowerName);
 			
 			instance.Homes.put(playerLowerName, new SerializableLocation(player.getLocation()));
 			instance.SaveConfiguration();
 			
 			player.sendMessage(ChatColor.AQUA + "Home Set.");
 			return true;
 		}
 		return false;
 	}
 
 }
