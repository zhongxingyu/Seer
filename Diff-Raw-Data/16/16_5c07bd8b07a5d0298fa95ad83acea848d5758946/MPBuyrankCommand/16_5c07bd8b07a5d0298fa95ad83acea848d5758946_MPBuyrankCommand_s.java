 package me.sinnoh.MasterPromote.Commands;
 
 import java.util.List;
 
 import me.sinnoh.MasterPromote.MasterPromote;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class MPBuyrankCommand implements CommandExecutor 
 {
 
 	public static MasterPromote plugin = MasterPromote.instance;
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
 	{
     	if(sender instanceof Player)
     	{
     		Player player = (Player) sender;
     		if(args.length ==1)
     		{
     			@SuppressWarnings("unchecked")
 				List<String> ranks = (List<String>) plugin.config.getList("Ranks");
     			for(String s : ranks)
     			{
     				String[] rank = s.split(",");
     				String group = rank[0];
     				Double price = Double.parseDouble(rank[1]);
     				
         			if(args[0].equalsIgnoreCase((group)))
         			{
         				if(player.hasPermission("MasterPromote.rank.buy." + group))
         				{
         					if(plugin.economy.has(player.getName(), price))
         					{
			    				plugin.confirm.put(player, args[0]);
 			    				String msg = plugin.messages.getString("BuyRank").replace("&", "\u00A7");
 			    				if(price == 1)
 			    				{
 				    				msg = msg.replace("<price>", price + " " + plugin.economy.currencyNameSingular());
 			    				}
 			    				else
 			    				{
 			    				    msg = msg.replace("<price>", price + " " + plugin.economy.currencyNamePlural());
 			    				}
			    				player.sendMessage(msg.replace("<group>", args[0]));
 			    				player.sendMessage(plugin.messages.getString("Confirm").replace("&","\u00A7"));
 								System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /buyrank");
 			    				return true;
         					}
         					else
         					{
         						player.sendMessage(plugin.messages.getString("NoMoney").replace("&", "\u00A7"));
         						return true;
         					}
         				}
         				else
         				{
         					player.sendMessage(plugin.messages.getString("NoPermissions").replace("&","\u00A7"));
         	    			System.out.println("[MasterPromote]Player " + player.getName() + " tried to use /buyrank");
         					return true;
         				}
         			}
     			}
     			player.sendMessage(plugin.messages.getString("CantBuyRank").replace("&", "\u00A7"));
     			return true;
     		}
     	}
     	else
     	{
     		System.out.println("You have to be a player to use this command");
     		return true;
     	}
 		return false;
 	}
 
 }
