 package co.networkery.uvbeenzaned;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Team {
 	
 	public static String pg = ChatColor.GOLD + "[" + ChatColor.AQUA + "Snowballer" + ChatColor.GOLD + "] " + ChatColor.RESET;
 	
 	public static boolean Join(String player, String team)
 	{
 		Player p = Bukkit.getPlayer(player);
 		if(SnowballerListener.teamcyan.contains(p.getName()))
 		{
 			p.sendMessage(pg + "You are already on team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 			return true;
 		}
 		if(SnowballerListener.teamlime.contains(p.getName()))
 		{
 			p.sendMessage(pg + "You are already on team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			return true;
 		}
 		if(team.equalsIgnoreCase("cyan"))
 		{
 			if(!SnowballerListener.teamcyan.contains(p.getName()) && !SnowballerListener.teamlime.contains(p.getName()))
 			{
 				p.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
 				SnowballerListener.teamcyan.add(p.getName());
 				p.setRemoveWhenFarAway(false);
 				Chat.sendAllTeamsMsg(pg + Utils.getNamewColor(p) + " has joined team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 				Chat.sendAllTeamsMsg(pg + ChatColor.AQUA + "CYAN" + ChatColor.RESET + " players: " + SnowballerListener.teamcyan.size());
 				p.getInventory().clear();
 				Rank.giveRank(Bukkit.getPlayer(p.getName()));
 				Utils.refreshAllTags();
 				p.setPlayerListName(ChatColor.AQUA + p.getName());
 				if(SnowballerListener.config.getConfig().getBoolean("startwithoutop"))
 				{
 		  			if(!SnowballerListener.teamlime.isEmpty())
 		  			{
 		  				if(SnowballerListener.gameon == false)
 			  			{
 			  				if(SnowballerListener.timergame == false)
 			  				{
 				  				if(!SnowballerListener.teamcyan.isEmpty() && !SnowballerListener.teamlime.isEmpty())
 				  				{
 					  				SnowballerListener.timergame = true;
 					  				Round.startIndependentTimerRound();
 						  			p.sendMessage(pg + "A new Snowballer game will start in " + Integer.toString(SnowballerListener.config.getConfig().getInt("timerdelay") / 1000) + " seconds from now.");
 						  			return true;
 				  				}
 				  				else
 				  				{
 				  					p.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 				  					return true;
 				  				}
 			  				}
 				  		}
 			  			else
 			  			{
 			  				p.sendMessage(pg + "There is a game currently in progress right now.  Please wait for the round to end!");
 			  				return true;
 			  			}
 		  			}
 		  			else
 		  			{
 		  				p.sendMessage(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
 		  				p.sendMessage(pg + "Waiting for another player to join....");
 		  			}
 				}
 				return true;
 			}
 		}
 		if(team.equalsIgnoreCase("lime"))
 		{
 			if(!SnowballerListener.teamlime.contains(p.getName()) && !SnowballerListener.teamcyan.contains(p.getName()))
 			{
 				p.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
 				SnowballerListener.teamlime.add(p.getName());
 				p.setRemoveWhenFarAway(false);
 				Chat.sendAllTeamsMsg(pg + Utils.getNamewColor(p) + " has joined team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 				Chat.sendAllTeamsMsg(pg + ChatColor.GREEN + "LIME" + ChatColor.RESET + " players: " + SnowballerListener.teamlime.size());
 				p.getInventory().clear();
 				Rank.giveRank(Bukkit.getPlayer(p.getName()));
 				Utils.refreshAllTags();
 				p.setPlayerListName(ChatColor.GREEN + p.getName());
 				if(SnowballerListener.config.getConfig().getBoolean("startwithoutop"))
 				{
 		  			if(!SnowballerListener.teamcyan.isEmpty())
 		  			{
 		  				if(SnowballerListener.gameon == false)
 			  			{
 			  				if(SnowballerListener.timergame == false)
 			  				{
 				  				if(!SnowballerListener.teamlime.isEmpty() && !SnowballerListener.teamcyan.isEmpty())
 				  				{
 					  				SnowballerListener.timergame = true;
 					  				Round.startIndependentTimerRound();
 						  			p.sendMessage(pg + "A new Snowballer game will start in " + Integer.toString(SnowballerListener.config.getConfig().getInt("timerdelay") / 1000) + " seconds from now.");
 						  			return true;
 				  				}
 				  				else
 				  				{
 				  					p.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 				  					return true;
 				  				}
 			  				}
 				  		}
 			  			else
 			  			{
 			  				p.sendMessage(pg + "There is a game currently in progress right now.  Please wait for the round to end!");
 			  				return true;
 			  			}
 		  			}
 		  			else
 		  			{
 		  				p.sendMessage(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
 		  				p.sendMessage(pg + "Waiting for another player to join....");
 		  			}
 				}
 				return true;
 			}
 		}
 		return true;
 	}
 	
 	public static boolean Leave(String player, boolean quiet)
 	{
 		Player p = Bukkit.getPlayer(player);
   		if(SnowballerListener.teamcyan.contains(p.getName()))
   		{
   			SnowballerListener.scores.saveConfig();
   			if(SnowballerListener.teamcyaninarena.contains(p.getName()))
   			{
   				SnowballerListener.teamcyaninarena.remove(p.getName());
   				p.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
   			}
   			SnowballerListener.teamcyan.remove(p.getName());
   			if(SnowballerListener.hitcnts.containsKey(p.getPlayer().getName()))
   			{
   				SnowballerListener.hitcnts.remove(p.getPlayer().getName());
   			}
   			p.setRemoveWhenFarAway(true);
   			p.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
   			p.getInventory().clear();
   			p.setPlayerListName(p.getName());
   			p.sendMessage(pg + "You've left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
   			SnowballerListener.checkTeamsInArena();
   			SnowballerListener.terminateAll();
   			Chat.sendAllTeamsMsg(pg + p.getName() + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
   			Chat.sendAllTeamsMsg(pg + ChatColor.GREEN + "LIME" + ChatColor.RESET + " players: " + SnowballerListener.teamlime.size());
   			Utils.refreshAllTags();
   			return true;
   		}
 		if(SnowballerListener.teamlime.contains(p.getName()))
 		{
 			SnowballerListener.scores.saveConfig();
 			if(SnowballerListener.teamlimeinarena.contains(p.getName()))
 			{
 				SnowballerListener.teamlimeinarena.remove(p.getName());
 				p.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
 			}
 			SnowballerListener.teamlime.remove(p.getName());
 			if(SnowballerListener.hitcnts.containsKey(p.getPlayer().getName()))
 			{
 				SnowballerListener.hitcnts.remove(p.getPlayer().getName());
 			}
 			p.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 			p.getInventory().clear();
 			p.setPlayerListName(p.getName());
 			p.sendMessage(pg + "You've left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			SnowballerListener.checkTeamsInArena();
 			SnowballerListener.terminateAll();
 			Chat.sendAllTeamsMsg(pg + p.getName() + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			Chat.sendAllTeamsMsg(pg + ChatColor.GREEN + "LIME" + ChatColor.RESET + " players: " + SnowballerListener.teamlime.size());
 			Utils.refreshAllTags();
 			return true;
 		}
 		if(!quiet)
 		{
 			p.sendMessage(pg + "You are not on a team!");
 			return true;
 		}
 		return true;
 	}
 
 }
