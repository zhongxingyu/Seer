 package com.github.CorporateCraft.cceconomy.Commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.github.CorporateCraft.cceconomy.*;
 
 public class CmdBalance
 {
 	public static boolean CommandUse(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (sender instanceof Player)
 		{
 	        Player player = (Player) sender;
 	        if (args.length > 1)
 	        {
 	      	   return false;
 	        }
 	        if (args.length == 1)
 	        {
 	       	   if(player.hasPermission("CCEconomy.balothers"))
 		       {
 	       		   String playersname;
 	       		   try
 	       		   {
 	       			   Player target = sender.getServer().getPlayer(args[0]);
 	       			   playersname = target.getName();
 	       		   }
 	       		   catch (Exception e)
 	       		   {
 	       			   playersname = args[0];
 	       		   }
 	       		   String balance = BalChecks.Bal(playersname);
 		       	   if(balance == null)
 		       	   {
 		       		   player.sendMessage(CCEconomy.messages + "That player is not in my records. If the player is offline, please use the full name.");
 		       		   return true;
 		       	   }
 		       	   player.sendMessage(CCEconomy.messages + playersname + "'s balance is: " + CCEconomy.money + "$" + balance);
 		       	   return true;
 		       }
 	       }
 	       if(player.hasPermission("CCEconomy.bal"))
 	       {
 	       	   String balance = BalChecks.Bal(player.getName());
 	       	   if(balance == null)
 	       	   {
 	       		   player.sendMessage(CCEconomy.messages + "You do not seem to exist let me add you now.");
 	       		   PlayerToFile.AddPlayerToList(player.getName());
 	       		   return true;
 	       	   }
	       	   player.sendMessage(CCEconomy.money + "$" + balance);
 	       	   return true;
 	       }
 	    } 
 		else
 		{
 			if (args.length == 1)
 		          {
 					String playersname;
 					try
 					{
 						Player target = sender.getServer().getPlayer(args[0]);
 						playersname = target.getName();
 					}
 					catch (Exception e)
 					{
 						playersname = args[0];
 					}
 					String balance = BalChecks.Bal(playersname);
 					if(balance == null)
 					{
 						sender.sendMessage(CCEconomy.messages + "That player is not in my records. If the player is offline, please use the full name.");
 						return true;
 					}
 					sender.sendMessage(CCEconomy.messages + playersname + "'s balance is: " + CCEconomy.money + "$" + balance);
 					return true;
 		       }
 			else
 			{
 				sender.sendMessage(CCEconomy.messages + "Log in to use this command");
 				return true;
 			}
 		}
 		return false;
 	}
 }
