 package pro.homiecraft.Commands;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class tcbWeather implements CommandExecutor {
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.hasPermission("tcb.Weather") && (sender instanceof Player)){
 			if(cmd.getName().equalsIgnoreCase("weather")){
 				if(args.length < 1) {
 					sender.sendMessage("Missing weather!");
 					sender.sendMessage("usage: /weather sun|rain");
 					return true;
 				}else if(args.length > 2){
 					sender.sendMessage("To many arguments!");
 					sender.sendMessage("usage: /weather sun|rain");
 					return true;
 				}else{
 					Player player = (Player) sender;
 					World world = player.getWorld();
 					
 					if(args[0].equalsIgnoreCase("sun")){
 						world.setStorm(false);
						player.sendMessage("Weather changed!");
 						return true;
 					}else if(args[0].equalsIgnoreCase("rain")){
 						if (args.length < 2){
							player.sendMessage("Mising storm duration");
							player.sendMessage("Usage: /weather rain <minutes>");
 							return true;
 						}else{
 							int min = Integer.parseInt(args[1]);
 							int dur = min * 1200;
 							world.setStorm(true);
 							world.setWeatherDuration(dur);
							player.sendMessage("Weather changed!");
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
