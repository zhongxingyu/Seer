 package io.github.LilParker.RadioPlugin;
 
 import java.util.HashMap;
 
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 
 public class RadioPluginCommandExecuter implements CommandExecutor {
 	
 	RadioPlugin plugin;
 	public HashMap<String, Float> playerFreqs = new HashMap<String, Float>();
 	
 	public RadioPluginCommandExecuter (RadioPlugin actPlugin) {
 		plugin = actPlugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("radio setfreq")){
 			if(sender instanceof Player){
 				if(args.length == 1){
 					if(((Player) sender).getItemInHand().getTypeId() == plugin.getConfig().getInt("radioitemid")){
 						if(playerFreqs.get(sender.getName()) != null){
 							playerFreqs.put(sender.getName(), Float.parseFloat(args[0]));
 							sender.sendMessage("Your frequency is now " + playerFreqs.get(sender.getName()));
 							return true;
 						}
 					}else{
 						sender.sendMessage("You must have a radio in hand to tune it!");
 						return false;
 					}
 				}else{
 					sender.sendMessage("Need 1 argument!");
 					return false;
 				}
 				
 			}else{
 				sender.sendMessage("You must be a player!");
 				return false;
 			}
 		}
		if(cmd.getName().equalsIgnoreCase("radio getfreq")){
 			if(sender instanceof Player){
 				if(playerFreqs.get(sender.getName()) != null){
 				sender.sendMessage("Your current frequency is " + playerFreqs.get(sender.getName()));
 				}else{
 					sender.sendMessage("Your radio isn't tuned to a frequency");
 					return true;
 				}
 			}else{
 				sender.sendMessage("You must be a player!");
 				return false;
 			}
 		}
 		return false;
 	}
 }
