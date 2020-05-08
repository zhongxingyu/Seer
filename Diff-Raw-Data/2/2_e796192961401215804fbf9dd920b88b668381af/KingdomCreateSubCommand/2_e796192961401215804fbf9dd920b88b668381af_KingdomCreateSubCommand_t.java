 package uk.co.quartzcraft.kingdoms.command;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.command.QSubCommand;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;;
 
 public class KingdomCreateSubCommand extends QSubCommand {
 
 	@Override
 	public String getPermission() {
 		return "QCK.Kingdom.create";
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if(args[1] != null) {
 			if(args[2] != null) {
 				sender.sendMessage(ChatPhrase.getPhrase("kingdom_name_single_word"));
 			} else {
 				String kingdomName = Kingdom.createKingdom(args[1], sender);
 				if(kingdomName != null) {
 					if(kingdomName == args[1]) {
 						sender.sendMessage(ChatPhrase.getPhrase("created_kingdom_yes") + ChatColor.WHITE + kingdomName);
					} else if(kingdomName == "name_error") {
 						sender.sendMessage(ChatPhrase.getPhrase("kingdomname_already_used") + ChatColor.WHITE + kingdomName);
 					}
 				} else {
 					sender.sendMessage(ChatPhrase.getPhrase("created_kingdom_no") + ChatColor.WHITE + kingdomName);
 				}
 			}
 		} else {
 			sender.sendMessage(ChatPhrase.getPhrase("specify_kingdom_name") + ChatColor.WHITE + args[1]);
 		}
 		
 	}
 
 }
