 package uk.co.quartzcraft.kingdoms.command;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.command.QSubCommand;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;
 
 public class KingdomOpenSubCommand extends QSubCommand {
 
 	@Override
 	public String getPermission() {
 		return "QCK.Kingdom.open";
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = (Player) sender;
 		if(args[0].equalsIgnoreCase("open")) {
 			if(Kingdom.isOpen(QKPlayer.getKingdom(player))) {
 				sender.sendMessage(ChatPhrase.getPhrase("kingdom_already_open"));
 			} else {
                 if(Kingdom.setOpen(QKPlayer.getKingdom(player), true)) {
                     sender.sendMessage(ChatPhrase.getPhrase("kingdom_now_open"));
                 } else {
                     sender.sendMessage(ChatPhrase.getPhrase("failed_open_kingdom"));
                 }
 			}
 		}
 
        if(args[0].equalsIgnoreCase("open")) {
             if(Kingdom.isOpen(QKPlayer.getKingdom(player))) {
                 if(Kingdom.setOpen(QKPlayer.getKingdom(player), false)) {
                     sender.sendMessage(ChatPhrase.getPhrase("kingdom_now_closed"));
                 } else {
                     sender.sendMessage(ChatPhrase.getPhrase("failed_close_kingdom"));
                 }
 
             } else {
                 sender.sendMessage(ChatPhrase.getPhrase("kingdom_already_closed"));
             }
         }
 	}
 }
