 package me.MnC.MnC_SERVER_MOD.chat;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import me.MnC.MnC_SERVER_MOD.MinecraftPlayer;
 import me.MnC.MnC_SERVER_MOD.UserManager;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 
 public class TellTabCompleter implements TabCompleter
 {
 	public List<String> onTabComplete(CommandSender sender, Command cmd, String line, String[] args)
 	{
		List<String> tabCompletions = new LinkedList<String>();
 		if(sender instanceof Player)
 		{
 			MinecraftPlayer player = UserManager.getInstance().getUser(sender.getName());
 			for(String recipient : player.getLastTellRecipients())
 			{
 				tabCompletions.add(recipient);
 			}
 			
 			for(String tellsender : player.getLastTellSenders())
 			{
 				if(!tabCompletions.contains(tellsender))
 					tabCompletions.add(tellsender);
 			}
 		}
 		return tabCompletions;
 	}
 }
