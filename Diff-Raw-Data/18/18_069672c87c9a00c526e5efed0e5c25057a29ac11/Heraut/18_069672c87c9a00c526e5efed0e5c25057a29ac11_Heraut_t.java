 package nl.giantit.minecraft.GiantShop.Misc;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.command.CommandSender;
 
 
 /**
  *
  * @author Giant
  */
 public class Heraut {
 	
     public static String parse(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);//input.replaceAll("(&([a-fA-F0-9]))", "§$2").replace("\\\\\u00A7", "&");
     }
 
 	public static void say (Player player, String message) {
 		player.sendMessage(parse(message));
 	}
 	
 	public static void say (CommandSender sender, String message) {
 		sender.sendMessage(message);
 	}
 
 	public static void broadcast (String message) {
 		for(Player p : GiantShop.getPlugin().getSrvr().getOnlinePlayers()) {
 			p.sendMessage(parse(message));
 		}
 	}
 	
 	public static void broadcast (String message, Boolean opOnly) {
 		if(!opOnly) {
 			broadcast(message);
 			return;
 		}
 		
 		for(Player p : GiantShop.getPlugin().getSrvr().getOnlinePlayers()) {
 			if(p.isOp()) {
 				p.sendMessage(parse(message));
 			}
 		}
 	}
 }
