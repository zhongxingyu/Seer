 package uk.co.thefailboat.TFBCommon.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.thefailboat.TFBCommon.Main;
 import uk.co.thefailboat.TFBCommon.Objects.SerializableLocation;
 
 public class Warp implements CommandExecutor{
 	
 	private Main instance;
 	
 	public Warp(Main _instance){
 		this.instance = _instance;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player)) return false;
 		Player player = (Player)sender;
 		
 		
 		if(cmd.getName().toLowerCase().equals("warp")){
 			
 			if(args.length < 1){
 				player.sendMessage(ChatColor.RED + "You must specify a warp!");
 				return false;
 			}
 			String warp = args[0].toLowerCase();
 			if(instance.Warps.containsKey(warp)){
				player.teleport(instance.Warps.get(warp).getLocation());
				player.sendMessage(ChatColor.AQUA + "Warped to " + warp);
 				return true;
 			}else{
 				player.sendMessage(ChatColor.RED + "That warp does not exist!");
 			}
 		}
 		
 		
 		else if(cmd.getName().toLowerCase().equals("setwarp")){
 			
 			if(args.length < 1){
 				player.sendMessage(ChatColor.RED + "You must specify a warp name!");
 				return false;
 			}
 			
 			String warp = args[0].toLowerCase();
 			
 			if(instance.Warps.containsKey(warp)) instance.Warps.remove(warp);
 			instance.Warps.put(warp, new SerializableLocation(player.getLocation()));
 			instance.SaveConfiguration();
 			
 			player.sendMessage(ChatColor.AQUA + "The warp " + warp + " has been set to your position.");
 			return true;
 		}
 		
 		
 		else if(cmd.getName().toLowerCase().equals("listwarps")){
 			String message = ChatColor.AQUA + "Available Warps: ";
 			for(String warp : instance.Warps.keySet()){
 				message += warp + ", ";
 			}
 			message = message.substring(0, message.length()-2); //remove the trailing ", "
 			
 			sender.sendMessage(message);
 			return true;
 		}
 		return false;
 	}
 
 }
