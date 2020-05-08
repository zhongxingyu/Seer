 package uk.co.quartzcraft.kingdoms.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.command.QSubCommand;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;;
 
 public class KingdomDeleteSubCommand extends QSubCommand {
 
 	@Override
 	public String getPermission() {
 		return "QCK.Kingdom.delete";
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if(args[1] != null) {
 			String kingdomName = Kingdom.deleteKingdom(args[1], sender);
 			if(kingdomName != null) {
 				if(kingdomName == args[1]) {
 					sender.sendMessage(ChatPhrase.getPhrase("deleted_kingdom_yes") + ChatColor.WHITE + kingdomName);
				} else if(kingdomName == "quartz error") {
 					sender.sendMessage(ChatPhrase.getPhrase("deleted_kingdom_no") + ChatColor.WHITE + kingdomName);
 				}
 			} else {
 				sender.sendMessage(ChatPhrase.getPhrase("deleted_kingdom_no") + ChatColor.WHITE + kingdomName);
 			}
 		} else {
 			sender.sendMessage(ChatPhrase.getPhrase("specify_kingdom_name") + ChatColor.WHITE + args[1]);
 		}
 		
 	}
 
 }
