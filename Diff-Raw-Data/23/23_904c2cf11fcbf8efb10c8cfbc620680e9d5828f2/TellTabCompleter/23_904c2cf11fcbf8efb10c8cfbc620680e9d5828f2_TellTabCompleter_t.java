 package me.MnC.MnC_SERVER_MOD.chat;
 
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import me.MnC.MnC_SERVER_MOD.MinecraftPlayer;
 import me.MnC.MnC_SERVER_MOD.UserManager;
 
import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 
 public class TellTabCompleter implements TabCompleter
 {
 	public List<String> onTabComplete(CommandSender sender, Command cmd, String line, String[] args)
 	{
		LinkedList<String> tabCompletions = new LinkedList<String>();
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
		if(args.length > 0)
		{
			for(Player p : Bukkit.getServer().getOnlinePlayers())
			{
				if(!tabCompletions.contains(p.getName()))
					tabCompletions.add(p.getName());
			}
			Iterator<String> iterator =  tabCompletions.iterator();
			while(iterator.hasNext())
			{
				String completion = iterator.next();
				if(!completion.toLowerCase().startsWith(args[0].toLowerCase()))
					iterator.remove();
			}
		}
		
 		return tabCompletions;
 	}
 }
