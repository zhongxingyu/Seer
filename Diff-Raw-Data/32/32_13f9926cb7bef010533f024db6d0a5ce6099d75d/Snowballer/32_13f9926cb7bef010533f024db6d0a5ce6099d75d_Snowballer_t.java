 package co.networkery.uvbeenzaned;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Snowballer extends JavaPlugin
 {
 	
 	public String pg = ChatColor.GOLD + "[" + ChatColor.AQUA + "Snowballer" + ChatColor.GOLD + "] " + ChatColor.RESET;
 	public SnowballerListener sbr;
 	public Logger log;
 	
 	public void onEnable()
 	{
 		sbr = new SnowballerListener(this);
 		this.log = getLogger();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.sbr, this);
 		log.info(pg + "All config loaded!");
 	}
 
   public void onDisable()
   {
 	  SnowballerListener.config.saveConfig();
 	  log.info(pg + "All config saved!");
   }
 
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
 	  if(sender instanceof Player)
 	  {
 		  Player plcmd = (Player)sender;
 		  if(args.length > 0)
 		  {
 			  switch(args[0].toLowerCase())
 			  {
 			  	case "setspawn":
 			  		if(plcmd.isOp())
 			  		{
 			  			SnowballerListener.config.getConfig().set("lobbyspawnlocation", LTSTL.loc2str(plcmd.getLocation()));
 				  		plcmd.sendMessage(pg + "The new spawn location is now at: " + SnowballerListener.config.getConfig().getString("lobbyspawnlocation") + ".");
 				  		return true;
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "addarenaside":
 			  		if(plcmd.isOp())
 			  		{
 			  			if(args.length > 1)
 			  			{
 			  				if(args[1].equalsIgnoreCase("cyan"))
 			  				{
 			  					if(args[2] != null)
 			  					{
 			  						SnowballerListener.config.getConfig().set("teamcyanarenasides." + args[2], LTSTL.loc2str(plcmd.getLocation()));
 				  					plcmd.sendMessage(pg + "Added side for arena " + args[2] + " and location " + SnowballerListener.config.getConfig().getString("teamcyanarenasides." + args[2]) + ".");
 				  					SnowballerListener.config.saveConfig();
 				  					return true;
 			  					}
 			  					plcmd.sendMessage(pg + "You have to have a name for the arena!");
 			  					return true;
 			  				}
 			  				if(args[1].equalsIgnoreCase("lime"))
 			  				{
 			  					if(args[2] != null)
 			  					{
 			  						SnowballerListener.config.getConfig().set("teamlimearenasides." + args[2], LTSTL.loc2str(plcmd.getLocation()));
 				  					plcmd.sendMessage(pg + "Added side for arena " + args[2] + " and location " + SnowballerListener.config.getConfig().getString("teamlimearenasides." + args[2]) + ".");
 				  					SnowballerListener.config.saveConfig();
 				  					return true;
 			  					}
 			  				}
 		  					plcmd.sendMessage(pg + "You have to have a name for the arena!");
 		  					return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "removearena":
 			  		if(plcmd.isOp())
 			  		{
 			  			if(args.length > 1)
 			  			{
 			  				if(SnowballerListener.config.getConfig().getConfigurationSection("teamcyanarenasides").contains(args[1]) || SnowballerListener.config.getConfig().getConfigurationSection("teamlimearenaside").contains(args[1]))
 			  				{
 				  				if(SnowballerListener.config.getConfig().getConfigurationSection("teamcyanarenasides").contains(args[1]))
 				  				{
 				  					SnowballerListener.config.getConfig().set("teamcyanarenasides." + args[1], null);
 				  				}
 				  				if(SnowballerListener.config.getConfig().getConfigurationSection("teamlimearenasides").contains(args[1]))
 				  				{
 				  					SnowballerListener.config.getConfig().set("teamlimearenasides." + args[1], null);
 				  				}
 				  				SnowballerListener.scores.saveConfig();
 				  				plcmd.sendMessage(pg + "Removed arena " + args[1] + " successfully!");
 			  					return true;
 			  				}
 			  				plcmd.sendMessage(pg + "There is no arena named " + args[1] + ".");
 		  					return true;
 			  			}
 			  			plcmd.sendMessage(pg + "Please provide a name to remove this arena.");
 	  					return true;
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "arenalist":
 		  			String arenalist = "";
 	  				plcmd.sendMessage(pg + "List of arenas:");
 	  				for(String k1 : SnowballerListener.config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false))
 	  				{
 	  					arenalist = arenalist + k1 + ", ";
 	  				}
 	  				arenalist = arenalist + ChatColor.AQUA + " (" + SnowballerListener.config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false).size() + ")";
 	  				plcmd.sendMessage(arenalist);
 	  				return true;
 			  	case "warparena":
 			  		if(plcmd.isOp() || plcmd.hasPermission("snowballer.warparena"))
 			  		{
 			  			if(args.length > 1)
 			  			{
 				  			if(SnowballerListener.config.getConfig().get("teamcyanarenasides." + args[1]) != null)
 				  			{
 				  				plcmd.sendMessage(pg + "Teleporting to: " + ChatColor.GOLD + args[1]);
 				  				plcmd.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("teamcyanarenasides." + args[1])));
 				  				Utils.checkPlayerStuck(500);
 				  				return true;
 				  			}
 				  			else
 				  			{
 				  				plcmd.sendMessage(pg + "That arena does not exist!  Type /sbr arenalist to get a list of arenas.");
 				  				return true;
 				  			}
 			  			}
 			  			else
 			  			{
 			  				plcmd.sendMessage(pg + "You need to supply an arena name!  Type /sbr arenalist to get a list of arenas.");
 			  				return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You do not have permission to warp to arenas!");
 			  		return true;
 			  	case "start":
 			  		if(plcmd.isOp())
 			  		{
 			  			if(SnowballerListener.gameon == false)
 			  			{
 			  				if(SnowballerListener.timergame == false)
 			  				{
 				  				if(!SnowballerListener.teamcyan.isEmpty() && !SnowballerListener.teamlime.isEmpty())
 				  				{
 					  				SnowballerListener.gameon = true;
 						  			Round.randomMap();
 						  			plcmd.sendMessage(pg + "You've started a Snowballer game.");
 						  			return true;
 				  				}
 				  				else
 				  				{
 				  					plcmd.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 				  					return true;
 				  				}
 			  				}
 			  				else
 			  				{
 			  					plcmd.sendMessage(pg + "A timer based game is running right now.");
 			  					return true;
 			  				}
 				  		}
 			  			else
 			  			{
 			  				plcmd.sendMessage(pg + "There is already a Snowballer game in progess.");
 			  				return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "start-timer":
 			  		if(plcmd.isOp())
 			  		{
 			  			if(SnowballerListener.gameon == false)
 			  			{
 			  				if(SnowballerListener.timergame == false)
 			  				{
 				  				if(!SnowballerListener.teamcyan.isEmpty() && !SnowballerListener.teamlime.isEmpty())
 				  				{
 					  				SnowballerListener.timergame = true;
 					  				Round.startIndependentTimerRound();
 						  			plcmd.sendMessage(pg + "You've scheduled a Snowballer game for " + Integer.toString(SnowballerListener.config.getConfig().getInt("timerdelay") / 1000) + " seconds from now.");
 						  			return true;
 				  				}
 				  				else
 				  				{
 				  					plcmd.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 				  					return true;
 				  				}
 			  				}
 			  				else
 			  				{
 			  					plcmd.sendMessage(pg + "A timer based game is running right now.");
 			  					return true;
 			  				}
 				  		}
 			  			else
 			  			{
 			  				plcmd.sendMessage(pg + "There is already a Snowballer game in progess.");
 			  				return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "stop":
 			  		if(plcmd.isOp())
 			  		{
 		  				if(SnowballerListener.timergame == true)
 		  				{
 		  					SnowballerListener.timergame = false;
 		  					plcmd.sendMessage(pg + "Stopped game timer.");
 		  				}
 			  			if(SnowballerListener.gameon == true)
 			  			{
 			  				SnowballerListener.gameon = false;
 							for (int i = 0; i < SnowballerListener.teamcyaninarena.size(); i++)
 							{
 								Player pl = getServer().getPlayer(SnowballerListener.teamcyaninarena.get(i));
 				  				pl.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
 				  				pl.sendMessage(pg + "An admin has ended the current snowballer game!");
 				  				SnowballerListener.teamcyaninarena.remove(pl);
 							}
 							for(String pl : SnowballerListener.teamcyan)
 							{
 								Bukkit.getServer().getPlayer(pl).getInventory().clear();
 								Rank.giveRank(Bukkit.getServer().getPlayer(pl));
 							}
 							for (int i = 0; i < SnowballerListener.teamlimeinarena.size(); i++)
 							{
 								Player pl = getServer().getPlayer(SnowballerListener.teamlimeinarena.get(i));
 				  				pl.teleport(LTSTL.str2loc(SnowballerListener.config.getConfig().getString("lobbyspawnlocation")));
 				  				pl.sendMessage(pg + "An admin has ended the current snowballer game!");
 				  				SnowballerListener.teamlimeinarena.remove(pl);
 							}
 							for(String pl : SnowballerListener.teamlime)
 							{
 								Bukkit.getServer().getPlayer(pl).getInventory().clear();
 								Rank.giveRank(Bukkit.getServer().getPlayer(pl));
 							}
 				  			SnowballerListener.teamcyaninarena.clear();
 				  			SnowballerListener.teamlimeinarena.clear();
 				  			plcmd.sendMessage(pg + "You've ended the current Snowballer game.");
 				  			return true;
 			  			}
 			  			else
 			  			{
 			  				plcmd.sendMessage(pg + "There is no game in progress right now.");
 			  				return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "join":
 			  		if(args.length > 1)
 			  		{
 			  			 return Team.Join(plcmd.getName(), args[1]);
 			  		}
 			  		return false;
 			  	case "leave":
 			  		SnowballerListener.scores.saveConfig();
			  		return Team.Leave(plcmd.getName(), false, false);
 			  	case "score":
 			  		if(args.length == 1)
 			  		{
 				  		if(SnowballerListener.teamcyan.contains(plcmd.getName()))
 				  		{
 				  			plcmd.sendMessage(pg + ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(plcmd.getName()) + ChatColor.RESET + ChatColor.GOLD + "] " + ChatColor.RESET + "Your score is " + ChatColor.GOLD + String.valueOf(SnowballerListener.scores.getConfig().getInt(plcmd.getName())) + ChatColor.RESET + ".");
 				  			return true;
 				  		}
 				  		else
 				  		{
 					  		if(SnowballerListener.teamlime.contains(plcmd.getName()))
 					  		{
 					  			plcmd.sendMessage(pg + ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(plcmd.getName()) + ChatColor.RESET + ChatColor.GOLD + "] " + ChatColor.RESET + "Your score is " + ChatColor.GOLD + String.valueOf(SnowballerListener.scores.getConfig().getInt(plcmd.getName())) + ChatColor.RESET + ".");
 					  			return true;
 					  		}
 				  		}
 				  		plcmd.sendMessage(pg + "Join a team to lookup your score!");
 				  		return true;
 			  		}
 		  			if(args.length > 1)
 		  			{
 				  		if(SnowballerListener.scores.getConfig().contains(args[1]))
 				  		{
 				  			plcmd.sendMessage(pg + ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(args[1]) + ChatColor.RESET + ChatColor.GOLD + "] " + ChatColor.RESET + Utils.getNamewColor(args[1]) + "'s score is " + ChatColor.GOLD + String.valueOf(SnowballerListener.scores.getConfig().getInt(args[1])) + ChatColor.RESET + ".");
 				  			return true;
 				  		}
 				  		plcmd.sendMessage(pg + "That player does not exist in the score records!");
 				  		return true;
 		  			}
 			  	case "stats":
 			  		plcmd.sendMessage(pg + ChatColor.GOLD + String.valueOf(SnowballerListener.config.getConfig().getInt("snowballthrowncount")) + ChatColor.RESET + " snowballs have been thrown since records began.");
 			  		return true;
 			  	case "teamstats":
 			  		Chat.getTeamStats(plcmd.getName());
 			  		return true;
 			  }
 		  }
 	  }
 	return false;
   }
 }
