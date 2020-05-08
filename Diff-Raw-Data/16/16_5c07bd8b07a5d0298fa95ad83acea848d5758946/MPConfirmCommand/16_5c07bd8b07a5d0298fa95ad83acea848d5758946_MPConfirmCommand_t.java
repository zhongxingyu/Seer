 package me.sinnoh.MasterPromote.Commands;
 
 import java.util.List;
 
 import me.sinnoh.MasterPromote.MasterPromote;
 import me.sinnoh.MasterPromote.MasterPromotePermissions;
 import me.sinnoh.MasterPromote.Events.PlayerPromoteEvent.PROMOTIONTYPE;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class MPConfirmCommand implements CommandExecutor 
 {
 	
 	public MasterPromote plugin = MasterPromote.instance;
 	
 	public boolean onCommand(CommandSender sender, Command command, String label,	String[] args) 
 	{
     	if(sender instanceof Player)
     	{
     		Player player = (Player) sender;
     		if(plugin.confirm.containsKey(player))
     		{
     			String group = plugin.confirm.get(player);
       			@SuppressWarnings("unchecked")
 				List<String> ranks = (List<String>) plugin.config.getList("Ranks");
    			for(String value : ranks)
     			{
    				String[] rank = value.split(",");
     				String group1 = rank[0];
     				Double price = Double.parseDouble(rank[1]);
     				if(group.equals(group1))
     				{
     					if(plugin.economy.has(player.getName(), price))
     					{
     						plugin.economy.withdrawPlayer(player.getName(), price);
     						MasterPromotePermissions.promote(player, group, PROMOTIONTYPE.BOUGHT);
     						String msg = plugin.messages.getString("BoughtRank").replace("&", "\u00A7");
     						player.sendMessage(msg.replace("<group>", group));
     						plugin.confirm.remove(player);
     						System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /confirm");
     						System.out.println("[MasterPromote]Player " + player.getName() + " has bought " + group);
     						return true;
     					}
     					else
     					{
     						String msg = plugin.messages.getString("NoMoney").replace("&", "\u00A7");
     						player.sendMessage(msg.replace("<group>", group));
     						return true;
     					}
     				}
     			}
     		}
     	}
     	else
     	{
     		System.out.println("[MasterPromote]You have to be a player to use this command!");
     		return true;
     	}
 		return false;
 	}
 
 }
