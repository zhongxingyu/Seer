 package com.CC.Commands;
 
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.CC.Arenas.GameManager;
 import com.CC.Commands.Staff.EndGame;
 import com.CC.General.User;
 import com.CC.General.UserManager;
 import com.CC.General.onStartup;
 import com.CC.Messages.PlayerMessages;
 
 public class StaffCommands implements CommandExecutor 
 {
 	private final EndGame endgame;
 	private final onStartup plugin;
 	private final GameManager gamemanager;
 	private final PlayerMessages messages;
 	private final UserManager um;
 	
 	public StaffCommands(onStartup instance)
 	{
 		plugin = instance;
 		endgame = new EndGame(plugin);
 		gamemanager  = plugin.getGameManager();
 		messages = plugin.getMessages();
 		um = plugin.getUserManager();
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args)
 	{
 		if(!(sender instanceof Player))
 		{
 			sender.sendMessage("Sorry but this command can only be used in game");
 			return false;
 		}
 		Player player = (Player)sender;
 		if(!player.hasPermission("ClusterChunk.Admin")){
 			player.sendMessage(messages.noPermissionCommand(player));
 			return false;
 		}
 		
 		if(string.equalsIgnoreCase("endgame"))
 		{
 			if(args.length == 0)
 			{
 				player.sendMessage(new StringBuilder(GREEN.toString()).append("Correct Usage: /endgame <Game> <Reason> || /endgame <Game> <Reason>").toString());
 				return false;
 			}
 			else if(args.length == 1)
 			{
 				if(gamemanager.isGame(args[0]))
 				{
 					return endgame.endGame(player, gamemanager.getGame(args[0]), " No specified reason");
 					
 				}
 				else
 				{
 					player.sendMessage(new StringBuilder(RED.toString()).append("The game you have specified does not exist").toString());
 					return false;
 				}
 			}
 			else if(args.length >= 2)
 			{
 				System.out.println(glue(args, " "));
 				if(gamemanager.isGame(args[0]))
 				{
 					return endgame.endGame(player, gamemanager.getGame(args[0]), glue(args, " "));
 					
 				}
 				else
 				{
 					player.sendMessage(new StringBuilder(RED.toString()).append("The game you have specified does not exist").toString());
 					return false;
 				}
 				
 			}
 		}
 		else if(string.equalsIgnoreCase("rep"))
 		{
 			if(args.length == 0)
 			{
 				player.sendMessage("Correct Usage: /rep <player>");
 			}
 			else if(args.length == 1)
 			{
 				if(Bukkit.getPlayer(args[0]) != null)
 				{
 					player.sendMessage(ChatColor.GRAY + args[0] + "'s Reputation is " + ChatColor.DARK_GRAY + um.getUser(Bukkit.getPlayer(args[0])).getReputation());
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + "The player you have specified does not exist");
 				}
 			}
			else if(args.length == 2)
 			{
 				if(player.hasPermission("ClusterChunk.Admin"))
 				{
 					if(args[0].equalsIgnoreCase("add"))
 					{
 						if(Bukkit.getPlayer(args[1]) != null)
 						{
 							if(isInt(args[2])){
 								User user = um.getUser(Bukkit.getPlayer(args[1]));
 								int old = user.getReputation();
 								if(old + Integer.parseInt(args[2]) <= 10)
 								{
 									user.changeReputation(old + Integer.parseInt(args[2]));
 									player.sendMessage(args[1] + "'s reputation is now " + user.getReputation());
									um.updatePlayer(user, "stats");
 								}
 								else
 								{
 									player.sendMessage(ChatColor.RED + "You may not make a player's reputation more than 10");
 									player.sendMessage(ChatColor.RED + "Please try a different amount");
 								}
 								
 							}
 							else
 							{
 								player.sendMessage(ChatColor.RED + args[2] + " is not an integer!");
 							}
 						}
 						else
 						{
 							player.sendMessage(ChatColor.RED + "The specified user does not exist");
 						}
 					}
 					else if(args[0].equalsIgnoreCase("take"))
 					{
 						if(Bukkit.getPlayer(args[1]) != null)
 						{
 							if(isInt(args[2])){
 								User user = um.getUser(Bukkit.getPlayer(args[1]));
 								int old = user.getReputation();
 								if(old - Integer.parseInt(args[2]) >= 0)
 								{
									user.changeReputation(old + Integer.parseInt(args[2]));
 									player.sendMessage(args[1] + "'s reputation is now " + user.getReputation());
									um.updatePlayer(user, "stats");
 								}
 								else
 								{
 									player.sendMessage(ChatColor.RED + "You may not make a player's reputation negative!");
 									player.sendMessage(ChatColor.RED + "Please try a different amount");
 								}
 								
 							}
 							else
 							{
 								player.sendMessage(ChatColor.RED + args[2] + " is not an integer!");
 							}
 						}
 						else
 						{
 							player.sendMessage(ChatColor.RED + "The specified user does not exist");
 						}
 					}
 				}
 				else
 				{
 					player.sendMessage("Correct Usage: /rep <player>");
 				}
 			}
 		}
 		else if(string.equalsIgnoreCase("kick"))
 		{
 			if(player.hasPermission("ClusterChunk.Admin"))
 			{
 				if(args.length == 0 )
 				{
 					player.sendMessage("Correct Usage: /kick <player> <reason>(optional)");
 				}
 				else if(args.length == 1)
 				{
 					if(Bukkit.getPlayer(args[0]) != null)
 					{
 						Bukkit.getPlayer(args[0]).kickPlayer(ChatColor.RED + "You have been kicked by an admin");
 						player.sendMessage("You have kicked " + args[0]);
 					}
 					else
 					{
 						player.sendMessage(ChatColor.RED + "The specified player does not exist!");
 					}
 				
 				}
 				else if(args.length > 1)
 				{
 					if(Bukkit.getPlayer(args[0]) != null)
 					{
 						Bukkit.getPlayer(args[0]).kickPlayer(ChatColor.RED + player.getName() + ": " + glue(args, " "));
 						player.sendMessage("You have kicked " + args[0]);
 					}
 					else
 					{
 						player.sendMessage(ChatColor.RED + "The specified player does not exist!");
 					}
 				}
 			}
 		}
 		return false;
 		
 	}
 	
 	public String glue(String[] array, String glue) 
         {
             StringBuilder glued = new StringBuilder();
             for(int i = 1; i < array.length;i++) 
             {
 		glued.append(array[i]);
                 if(i != array.length-1)
 		glued.append(glue);
             }
             return glued.toString();
         }
 	
 	
 	public boolean isInt(String string){
 		try{
 			Integer.parseInt(string);
 		}catch(Exception e){
 			return false;
 		}
 		return true;
 	}
 
 }
