 package co.networkery.uvbeenzaned;
 
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Snowballer extends JavaPlugin
 {
 	
 	public String pg = ChatColor.AQUA + "[Snowballer] " +  ChatColor.RESET;
 	public SnowballerListener sbr;
 	public configOps cO;
 	public ConfigAccessor config;
 	public ConfigAccessor scores;
 	public Logger log;
 	
 	public void onEnable()
 	{
 		sbr = new SnowballerListener(this);
 		cO = new configOps(this);
 		config = new ConfigAccessor(this, "config.yml");
 		scores = new ConfigAccessor(this, "scores.yml");
 		this.log = getLogger();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.sbr, this);
 		configOps.load();
 		log.info(pg + "All config loaded!");
 	}
 
   public void onDisable()
   {
 	  configOps.save();
 	  log.info(pg + "All config saved!");
   }
 
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
 	  if(sender instanceof Player)
 	  {
 		  Player plcmd = (Player)sender;
 		  if(args.length > 0)
 		  {
 			  switch(args[0])
 			  {
 			  	case "setspawn":
 			  		if(plcmd.isOp())
 			  		{
 			  			SnowballerListener.lobbyspawnlocation = plcmd.getLocation();
 			  			configOps.save();
 				  		plcmd.sendMessage(pg + "The new spawn location is now at: " + SnowballerListener.lobbyspawnlocation.toString() + ".");
 				  		return true;
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
 			  		return true;
 			  	case "setarenaside":
 			  		if(plcmd.isOp())
 			  		{
 			  			if(args.length > 1)
 			  			{
 			  				if(args[1].equalsIgnoreCase("cyan"))
 			  				{
 			  					if(args[2] != null)
 			  					{
 			  						SnowballerListener.teamcyanarenasides.put(args[2], plcmd.getLocation());
 			  						configOps.save();
 				  					plcmd.sendMessage(pg + "Added the arena: " + args[2] + " and location " + SnowballerListener.teamcyanarenasides.get(args[2]).toString() + ".");
 				  					return true;
 			  					}
 			  					plcmd.sendMessage(pg + "You have to have a name for the arena!");
 			  					return true;
 			  				}
 			  				if(args[1].equalsIgnoreCase("lime"))
 			  				{
 			  					if(args[2] != null)
 			  					{
 			  						SnowballerListener.teamlimearenasides.put(args[2], plcmd.getLocation());
 			  						configOps.save();
 				  					plcmd.sendMessage(pg + "Added the arena: " + args[2] + " and location " + SnowballerListener.teamlimearenasides.get(args[2]).toString() + ".");
 				  					return true;
 			  					}
 			  				}
 		  					plcmd.sendMessage(pg + "You have to have a name for the arena!");
 		  					return true;
 			  			}
 			  		}
 			  		plcmd.sendMessage(pg + "You have to be an op to run this command!");
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
 						  			SnowballerListener.randomMap();
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
 					  				SnowballerListener.startIndependentTimerRound();
 						  			plcmd.sendMessage(pg + "You've scheduled a Snowballer game for " + Integer.toString(SnowballerListener.timerdelay / 1000) + " seconds from now.");
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
 				  				pl.teleport(SnowballerListener.lobbyspawnlocation);
 				  				pl.sendMessage(pg + "An admin has ended the current snowballer game!");
 				  				SnowballerListener.teamcyaninarena.remove(pl);
 							}
 							for(Entry<String, Integer> entry : SnowballerListener.teamcyan.entrySet())
 							{
 								Bukkit.getServer().getPlayer(entry.getKey()).getInventory().clear();
 								SnowballerListener.giveTeamArmor(Bukkit.getServer().getPlayer(entry.getKey()), "cyan");
 							}
 							for (int i = 0; i < SnowballerListener.teamlimeinarena.size(); i++)
 							{
 								Player pl = getServer().getPlayer(SnowballerListener.teamlimeinarena.get(i));
 				  				pl.teleport(SnowballerListener.lobbyspawnlocation);
 				  				pl.sendMessage(pg + "An admin has ended the current snowballer game!");
 				  				SnowballerListener.teamlimeinarena.remove(pl);
 							}
 							for(Entry<String, Integer> entry : SnowballerListener.teamlime.entrySet())
 							{
 								Bukkit.getServer().getPlayer(entry.getKey()).getInventory().clear();
 								SnowballerListener.giveTeamArmor(Bukkit.getServer().getPlayer(entry.getKey()), "lime");
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
 			  			if(SnowballerListener.teamcyan.containsKey(plcmd.getName()))
 			  			{
 			  				plcmd.sendMessage(pg + "You are already on team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 			  				return true;
 			  			}
 			  			if(SnowballerListener.teamlime.containsKey(plcmd.getName()))
 			  			{
 			  				plcmd.sendMessage(pg + "You are already on team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			  				return true;
 			  			}
 			  			if(args[1].equalsIgnoreCase("cyan"))
 			  			{
 					  		if(!SnowballerListener.teamcyan.containsKey(plcmd.getName()) && !SnowballerListener.teamlime.containsKey(plcmd.getName()))
 					  		{
 					  			if(scores.getConfig() != null)
 					  			{
 						  			int tmpscore = scores.getConfig().getInt(plcmd.getName());
 									if(tmpscore != 0)
 									{
 										SnowballerListener.teamcyan.put(plcmd.getName(), tmpscore);
 									}
 									else
 									{
 										SnowballerListener.teamcyan.put(plcmd.getName(), 0);
 									}
 					  			}
 								else
 								{
 									SnowballerListener.teamcyan.put(plcmd.getName(), 0);
 								}
 					  			plcmd.teleport(SnowballerListener.lobbyspawnlocation);
					  			plcmd.getInventory().clear();
					  			SnowballerListener.giveTeamArmor(plcmd, "cyan");
 					  			SnowballerListener.sendAllTeamsMsg(pg + plcmd.getName() +" has joined team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					  			if(SnowballerListener.startwithoutop)
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
 									  				SnowballerListener.startIndependentTimerRound();
 										  			plcmd.sendMessage(pg + "A new Snowballer game will start in " + Integer.toString(SnowballerListener.timerdelay / 1000) + " seconds from now.");
 										  			return true;
 								  				}
 								  				else
 								  				{
 								  					plcmd.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 								  					return true;
 								  				}
 							  				}
 								  		}
 							  			else
 							  			{
 							  				plcmd.sendMessage(pg + "There is a game currently in progress right now.  Please wait for the round to end!");
 							  				return true;
 							  			}
 						  			}
 						  			else
 						  			{
 						  				plcmd.sendMessage(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
 						  				plcmd.sendMessage(pg + "Waiting for another player to join....");
 						  			}
 					  			}
 					  			return true;
 					  		}
 			  			}
 			  			if(args[1].equalsIgnoreCase("lime"))
 			  			{
 					  		if(!SnowballerListener.teamlime.containsKey(plcmd.getName()) && !SnowballerListener.teamcyan.containsKey(plcmd.getName()))
 					  		{
 					  			if(scores.getConfig() != null)
 					  			{
 						  			int tmpscore = scores.getConfig().getInt(plcmd.getName());
 									if(tmpscore != 0)
 									{
 										SnowballerListener.teamlime.put(plcmd.getName(), tmpscore);
 									}
 									else
 									{
 										SnowballerListener.teamlime.put(plcmd.getName(), 0);
 									}
 					  			}
 								else
 								{
 									SnowballerListener.teamlime.put(plcmd.getName(), 0);
 								}
 					  			plcmd.teleport(SnowballerListener.lobbyspawnlocation);
					  			plcmd.getInventory().clear();
					  			SnowballerListener.giveTeamArmor(plcmd, "lime");
 					  			SnowballerListener.sendAllTeamsMsg(pg + plcmd.getName() +" has joined team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					  			if(SnowballerListener.startwithoutop)
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
 									  				SnowballerListener.startIndependentTimerRound();
 										  			plcmd.sendMessage(pg + "A new Snowballer game will start in " + Integer.toString(SnowballerListener.timerdelay / 1000) + " seconds from now.");
 										  			return true;
 								  				}
 								  				else
 								  				{
 								  					plcmd.sendMessage(pg + "Cannot start game!  One or both of the teams have no players on them at the moment.");
 								  					return true;
 								  				}
 							  				}
 								  		}
 							  			else
 							  			{
 							  				plcmd.sendMessage(pg + "There is a game currently in progress right now.  Please wait for the round to end!");
 							  				return true;
 							  			}
 						  			}
 						  			else
 						  			{
 						  				plcmd.sendMessage(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
 						  				plcmd.sendMessage(pg + "Waiting for another player to join....");
 						  			}
 					  			}
 					  			return true;
 					  		}
 			  			}
 			  			return true;
 			  		}
 			  		return false;
 			  	case "leave":
 			  		configOps.saveScores();
 			  		if(SnowballerListener.teamcyan.containsKey(plcmd.getName()))
 			  		{
 			  			if(SnowballerListener.teamcyaninarena.contains(plcmd.getName()))
 			  			{
 			  				SnowballerListener.teamcyaninarena.remove(plcmd.getName());
 			  				plcmd.teleport(SnowballerListener.lobbyspawnlocation);
 			  			}
 			  			SnowballerListener.teamcyan.remove(plcmd.getName());
 			  			plcmd.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 			  			plcmd.getInventory().clear();
 			  			plcmd.sendMessage(pg + "You've left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 			  			SnowballerListener.sendAllTeamsMsg(pg + plcmd.getName() + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 			  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 			  			return true;
 			  		}
 			  		if(SnowballerListener.teamlime.containsKey(plcmd.getName()))
 			  		{
 			  			if(SnowballerListener.teamlimeinarena.contains(plcmd.getName()))
 			  			{
 			  				SnowballerListener.teamlimeinarena.remove(plcmd.getName());
 			  				plcmd.teleport(SnowballerListener.lobbyspawnlocation);
 			  			}
 			  			SnowballerListener.teamlime.remove(plcmd.getName());
 			  			plcmd.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 			  			plcmd.getInventory().clear();
 			  			plcmd.sendMessage(pg + "You've left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			  			SnowballerListener.sendAllTeamsMsg(pg + plcmd.getName() + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 			  			SnowballerListener.sendAllTeamsMsg(pg + "There are now " + SnowballerListener.teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 			  			return true;
 			  		}
 		  			plcmd.sendMessage(pg + "You are not on a team!");
 		  			return true;
 			  	case "score":
 			  		if(SnowballerListener.teamcyan.containsKey(plcmd.getName()))
 			  		{
 			  			plcmd.sendMessage(pg + "Your score is " + SnowballerListener.teamcyan.get(plcmd.getName()) + ".");
 			  			return true;
 			  		}
 			  		else
 			  		{
 				  		if(SnowballerListener.teamlime.containsKey(plcmd.getName()))
 				  		{
 				  			plcmd.sendMessage(pg + "Your score is " + SnowballerListener.teamlime.get(plcmd.getName()) + ".");
 				  			return true;
 				  		}
 			  		}
 			  		plcmd.sendMessage(pg + "Join a team to lookup your score!");
 			  		return true;
 			  }
 		  }
 	  }
 	  return false;
   }
 }
