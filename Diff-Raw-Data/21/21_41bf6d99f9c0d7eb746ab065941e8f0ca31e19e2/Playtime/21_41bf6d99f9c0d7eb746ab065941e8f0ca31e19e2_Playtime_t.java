 package org.nationsatwar.playtime;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Playtime extends JavaPlugin implements Listener
 {
 	Logger log;
 	HashMap<String, PlaytimeEvent> map;
 	PlaytimeEvent temp;
 	
 	public void onEnable()
 	{
 		// read external file for current event stuff
 		log = this.getLogger();
 		getServer().getPluginManager().registerEvents(this, this);
 		map = new HashMap<String,PlaytimeEvent>();
 		getConfig();
 		readConfig();
 		log.info("Playtime has been enabled!");
 	}
 	
 	public void onDisable()
 	{
 		this.saveConfig();
 		log.info("Playtime has been disabled!");
 	}
 
 	@EventHandler // EventPriority.NORMAL by default
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		// check if player is subscribed to an event
 		// if subscribed, teleport to spawn location
 		final Player pl = event.getPlayer();
 		for (final PlaytimeEvent value : map.values()) 
 		{
 			if(value.isSubscribed(pl.getName()))
 			{
 				pl.sendMessage("If your currently-subscribed event has a respawn point set, you will be teleported to it in approximately ten seconds.");
 				// grab time-to-teleport from the event and change message as necessary
 				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
 				{
 					public void run() 
 					{
 						value.teleportToSpawn(pl);
 					}
 				}, 200L);
 				
 			}
 		}
 	}
 
 	public void readConfig()
 	{
 		//get events from config.yml, stuff into the hashmaps
 		if(this.getConfig().getString("events") != null)
 		{
 			Set<String> eventKeys = this.getConfig().getConfigurationSection("events").getKeys(false);
 			Iterator<String> e = eventKeys.iterator();
 			String eventName = null;
 			
 			do
 			{
 				eventName = (String) e.next();
 				String path = "events."+eventName+".";
 				
 				// get basic event from config.yml
 				temp = new PlaytimeEvent(eventName,this.getConfig().getBoolean(path+"hidden"));
 				log.info("Loading event '"+eventName+"'");
 				
 				// get spawn data
 				if(this.getConfig().getString(path+"spawn") != null)
 				{
 					if(this.getConfig().getString(path+".spawn.location.world") != null)
 					{
 						Location l = new Location(getServer().getWorld(this.getConfig().getString(path+"spawn.location.world")), this.getConfig().getDouble(path+"spawn.location.x"), this.getConfig().getDouble(path+"spawn.location.y"), this.getConfig().getDouble(path+"spawn.location.z"));
 						temp.setSpawn(l);
 					}
 					else if(this.getConfig().getString(path+".spawn.player") != null)
 					{
 						temp.setSpawn(this.getConfig().getString(path+".player"));
 					}
 				}
 				
 				map.put(eventName,temp);
 				
 				// get subscribers
 				if(this.getConfig().getConfigurationSection("events."+eventName+".subscribers") != null)
 				{
 					Set<String> subKeys = this.getConfig().getConfigurationSection("events."+eventName+".subscribers").getKeys(false);
 					Iterator<String> s = subKeys.iterator();
 					String subscriber;
 					
 					do
 					{
 						subscriber = (String) s.next();
 						log.info("Adding player '"+subscriber+"'");
 						String sPath = "events."+eventName+".subscribers."+subscriber+".";
 						
 						Location l = new Location(getServer().getWorld(this.getConfig().getString(sPath+"origLocation.world")), this.getConfig().getDouble(sPath+"origLocation.x"), this.getConfig().getDouble(sPath+"origLocation.y"), this.getConfig().getDouble(sPath+"origLocation.z"));
 						
 						temp = map.get(eventName);
 						temp.subscribe(subscriber,l);
 						
 					}while(s.hasNext());
 				}
 				
 				// get event start and end times
 				if(this.getConfig().getString(path+"time") != null)
 				{
 					SimpleDateFormat parse = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
 					if(this.getConfig().getString(path+"time.start") != null)
 					{
 						String s = this.getConfig().getString(path+"time.start");
 						// problem: returns dates in ISO-format as something like 'Mon Feb 16th 21:32:00 GMT 1987'
 						// time is fine, but how to deal with date when month is 'Feb'?
 						log.info(temp.getName() + " " + "start: " + s);
 						GregorianCalendar d = new GregorianCalendar();
 						try 
 						{
 							d.setTime(parse.parse(s));
 						} 
 						catch (ParseException e1) 
 						{
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 						temp.setStartTime(d);
 					}
 					if(this.getConfig().getString(path+"time.end") != null)
 					{
 						String s = this.getConfig().getString(path+"time.end");
 						// problem: returns dates in ISO-format as something like 'Mon Feb 16th 21:32:00 GMT 1987'
 						// time is fine, but how to deal with date when month is 'Feb'?
 						log.info(temp.getName() + " " + "end: " + s);
 						GregorianCalendar d = new GregorianCalendar();
 						try 
 						{
 							d.setTime(parse.parse(s));
 						} 
 						catch (ParseException e1) 
 						{
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 						temp.setEndTime(d);
 					}
 				}
 				
 				map.put(eventName,temp);
 				
 			}while(e.hasNext());
 		}
 	}
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	Player player = null;
     	if(sender instanceof Player)
     	{
     		player = (Player) sender;
     	}
     	
     	if(cmd.getName().equalsIgnoreCase("event"))
     	{ 
 			if(args.length >= 1)
 			{
 	    		if(args[0].equalsIgnoreCase("create"))
 	    		{
 					if(args.length >= 2)
 					{
 		    			if(player != null)
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) == null) // makes sure event with name does not already exist
 		    					{
 		    						String path = "events."+args[1]+".";
 		    						if(args.length >= 3)
 		    						{
 		    							// loop through 2 to < args.length, check for arguments like 'hidden'
 		    							if(args[2].equalsIgnoreCase("hidden"))
 		    							{
 			    							temp = new PlaytimeEvent(args[1], true);
 				    						map.put(args[1],temp);
 				    						this.getConfig().set(path+"hidden",true);
 		    							}
 		    						}
 		    						else
 		    						{
 			    						temp = new PlaytimeEvent(args[1]);
 			    						map.put(args[1],temp);
 			    						this.getConfig().set(path+"hidden",false);
 		    						}
 		    						this.getConfig().set(path+"spawn",false);
 		    					    this.saveConfig();
 		    						player.sendMessage("Event '"+args[1]+"' successfully created.");
 		    					}
 		    					else
 		    					{
 		    						// error; event already exists.
 		    						player.sendMessage("Error: event '"+args[1]+"' already exists.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 							if(map.get(args[1]) == null) // makes sure event with name does not already exist
 							{
 	    						String path = "events."+args[1];
 	    						if(args.length >= 3)
 	    						{
 	    							if(args[2].equalsIgnoreCase("hidden"))
 	    							{
 		    							temp = new PlaytimeEvent(args[1], true);
 			    						map.put(args[1],temp);
 			    						this.getConfig().set(path+".hidden",true);
 	    							}
 	    						}
 	    						else
 	    						{
 		    						temp = new PlaytimeEvent(args[1]);
 		    						map.put(args[1],temp);
 		    						this.getConfig().set(path+".hidden",false);
 	    						}
 	    					    this.saveConfig();
 		    					log.info("Event '"+args[1]+"' successfully created.");
 							}
 							else
 							{
 								// error; event already exists.
 								log.info("Error: event '"+args[1]+"' already exists.");
 							}
 		    			}
 					}
 					else
 					{
 						// instructions on use of command
 						if(player != null)
 						{
 							if(player.hasPermission("playtime.admins"))
 							{
 								player.sendMessage("Usage: /event create [name] - creates an event with the provided name.");
 								player.sendMessage("Options: 'hidden' - event will not appear when using the list command for people without the permission playtime.admins.");
 							}
 							else
 								player.sendMessage("You do not have permission to use this command.");
 						}
 						else
 						{
 							log.info("Usage: /event create [name] - creates an event with the provided name.");
 							log.info("Options: 'hidden' - event will not appear when using the list command for people without the permission playtime.admins.");
 						}
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("end"))
 	    		{
 					if(args.length >= 2) // event name
 					{
 		    			if(player != null)
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) != null) // if event with name is found
 		    					{
 		    						map.remove(args[1]);
 		    						String path = "events."+args[1];
 			    					// remove event from config.yml
 		    						this.getConfig().set(path, null);
 		    						this.saveConfig();
 		    						// message to player using command
 		    						// general message to players (subscribed to event?) that event has ended
 		    						player.sendMessage("Event '"+args[1]+"' successfully ended.");
 		    					}
 		    					else
 		    					{
 		    						// error; event does not exist
 		    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 		    				if(args[1] != null)
 		    				{
 		    					if(map.get(args[1]) != null)
 		    					{
 		    						map.remove(args[1]);
 		    						String path = "events."+args[1];
 		    						this.getConfig().set(path, null);
 		    						this.saveConfig();
 		        					// remove event from file
 		    						// general message to players (subscribed to event?) that event has ended
 		    						log.info("Event '"+args[1]+"' successfully ended.");
 		    					}
 		    					else
 		    					{
 		    						log.info("Error: event '"+args[1]+"' does not exist.");
 		    					}
 		    				}
 		    			}
 					}
 					else
 					{
 						// instructions on use of command
 						if(player != null)
 						{
 							if(player.hasPermission("playtime.admins"))
 								player.sendMessage("Usage: /event end [event] - ends the named event.");
 							else
 								player.sendMessage("You do not have permission to use this command.");
 						}
 						else
 						{
 							log.info("Usage: /event end [event] - ends the named event.");
 						}
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("setspawn"))
 	    		{
 					if(args.length >= 2) // event name
 					{
 		    			if(player != null) // user is player
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) != null) // if event with name is found
 		    					{
 		    						if(args.length >= 3)
 		    						{
 			    	    				if(args[2] != null) // player name
 			    	    				{
 			    	    					// player name was provided
 			    							Player p = null;
 			    	    					p = getServer().getPlayer(args[2]);
 				    						if(p != null)
 				    						{ 
 				    							// get player from server
 				    	    					// set player as event spawn
 				    	    					temp = map.get(args[1]);
 				    	    					temp.setSpawn(p);
 				    	    					map.put(args[1],temp);
 				    	    					String path = "events."+args[1]+".";
 				    	    					this.getConfig().set(path+"spawn.player",p.getName());
 				    	    					this.getConfig().set(path+"spawn.location", null);
 					    					    this.saveConfig();
 				    	    					
 					    						player.sendMessage(p.getName()+" set as spawn for event "+args[1]);
 				    						}
 				    						else
 				    						{
 					    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 				    						}
 			    	    				}
 		    						}
 		    	    				else
 		    	    				{
 		    	    					// no player provided, so take user's current location as spawn
 		    	    					temp = map.get(args[1]);
 		    	    					temp.setSpawn(player.getLocation());
 		    	    					map.put(args[1],temp);
 		    	    					
 		    	    					String path = "events."+args[1]+".";
 		    	    					this.getConfig().set(path+"spawn.location.x",player.getLocation().getX());
 		    	    					this.getConfig().set(path+"spawn.location.y",player.getLocation().getY());
 		    	    					this.getConfig().set(path+"spawn.location.z",player.getLocation().getZ());
 		    	    					this.getConfig().set(path+"spawn.location.world",player.getWorld().getName());
 		    	    					this.getConfig().set(path+"spawn.player",null);
 			    					    this.saveConfig();
 		    	    					
 			    						player.sendMessage("Current location set as spawn for event "+args[1]);
 		    	    				}
 		    					}
 		    					else
 		    					{
 		    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 	    					if(map.get(args[1]) != null) // if event with name is found
 	    					{
 	    						if(args.length >= 3)
 	    						{
 		    	    				if(args[2] != null) // player name or nothing
 		    	    				{
 		    	    					// player name was provided
 		    							Player p = null;
 		    	    					p = getServer().getPlayer(args[2]);
 			    						if(p != null)
 			    						{
 			    							// get player from server
 			    	    					// set player as event spawn
 			    	    					temp = map.get(args[1]);
 			    	    					temp.setSpawn(p);
 			    	    					map.put(args[1],temp);
 			    	    					
 			    	    					String path = "events."+args[1]+".";
 			    	    					this.getConfig().set(path+"spawn.player",p.getName());
 			    	    					this.getConfig().set(path+"spawn.location",null);
 				    					    this.saveConfig();
 			    	    					
 			    	    					log.info(p.getName()+" set as spawn for event "+args[1]);
 			    						}
 			    						else
 			    						{
 				    						log.info("Error: " + args[2] + " is not a valid player.");
 			    						}
 		    	    				}
 		    	    				else
 		    	    				{
 		    	    					// no player provided, so this can't be used
 		    	    					log.info("Error: must specify player");
 		    	    				}
 	    						}
 	    					}
 	    					else
 	    					{
 	    						log.info("Error: event '"+args[1]+"' does not exist.");
 	    					}
 		    			}
 					}
 					else
 					{
 						// help text
 						if(player != null)
 						{
 							if(player.hasPermission("playtime.admins"))
 							{
 								player.sendMessage("Usage: /event setspawn [event] - sets the spawn location for the named event at the user's location.");
 								player.sendMessage("Usage: /event setspawn [event] [player] - sets the spawn location for the named event as the named user; respawning players will be teleported to the named player.");
 							}
 							else
 								player.sendMessage("You do not have permission to use this command.");
 						}
 						else
 						{
 							log.info("Usage: /event setspawn [event] [player] - sets the spawn location for the named event as the named user; respawning players will be teleported to the named player.");
 						}
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("setTime")) 
 	    		{
 	    			if(args.length >= 4)
 					{
 		    			if(map.get(args[1]) != null) // check provided event exists
 		    			{
 		    				if(args[2].equalsIgnoreCase("start") || args[2].equalsIgnoreCase("end")) // or end; most of the checking for either command is the same, except at the point the information is put into the PlaytimeEvent.
 		    				{
 		    					if(args.length >= 5) // at least time and date need to be specified
 		    					{
 		    						// parse date, parse time
 		    						// set date and time in PlaytimeEvent
 		    						
 		    						// time/date format:  YYYY-MM-DD HH:MM:SS TZ
 		    						// Alter command to just take it in that order only?
 		    						// can drop timezone
 		    						// server is apparently using Moscow time
 		    						if(args[3].matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) // YYYY-MM-DD
 		    						{
 			    						String[] date = args[3].split("-");
 			    						
 		    							if(args[4].matches("\\d\\d:\\d\\d:\\d\\d") || args[4].matches("\\d\\d:\\d\\d")) // HH:MM:SS or HH:MM
 			    						{
 				    						String[] time = args[4].split(":");
 				    						// how to turn this into a date?
 				    						GregorianCalendar cal = new GregorianCalendar(); // needs setting with correct timezone
 				    						// date/time format: <almiteycow>  YYYY-MM-DD HH:MM:SS TZ
 				    						// if no timezone is given, assume... UTC?
 				    						// year month day hour minute
 				    						if(time.length >= 3) // if seconds were specified
 				    						{
 				    							cal.set(Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2]),Integer.parseInt(time[0]),Integer.parseInt(time[1]),Integer.parseInt(time[2]));
 				    						}
 				    						else // seconds weren't specified
 				    						{
 					    						cal.set(Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2]),Integer.parseInt(time[0]),Integer.parseInt(time[1]));
 				    						}
 				    						
 				    						// if timezone provided, use that.
 				    						// record it in event, provide both UTC and that timezone when asked about event details
 				    						// translate to server timezone when checking.
 
 			    	    					String path = "events."+args[1]+".time.";
 			    	    					
 				    						if(args[2].equalsIgnoreCase("start"))
 				    						{
 				    							map.get(args[1]).setStartTime(cal);
 				    							this.getConfig().set(path+"start",cal.getTime());
 				    						}
 				    						else
 				    						{
 				    							map.get(args[1]).setEndTime(cal);
 				    							this.getConfig().set(path+"end",cal.getTime());
 				    						}
 				    							
 				    					    this.saveConfig();
 			    						}
 			    						else
 			    						{
 			    							if(player != null)
 			    								player.sendMessage("Error: must provide recogniseable time.");
 			    							else
 			    								log.info("Error: must provide recogniseable time.");
 			    							// error message: must provide recogniseable time
 			    						}
 		    						}
 		    						else
 		    						{
 		    							if(player != null)
 		    								player.sendMessage("Error: must provide recogniseable date.");
 		    							else
 		    								log.info("Error: must provide recogniseable date.");
 		    							// error message: must provide recogniseable date and time
 		    						}
 		    					}
 		    					else // nothing was specified following 'start' or 'end'.
 		    					{
 		    						// help text
 		    						if(args[2].equalsIgnoreCase("start"))
 		    						{
 		    							// helptext for start
 		    							if(player != null)
 		    								player.sendMessage("Usage: /event setTime [event] start yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    							else
 		    								log.info("Usage: /event setTime [event] start yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    						}
 		    						else if (args[2].equalsIgnoreCase("end"))
 		    						{
 		    							// helptext for end
 		    							if(player != null)
 		    								player.sendMessage("Usage: /event setTime [event] end yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    							else
 		    								log.info("Usage: /event setTime [event] end yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    						}
 		    						else
 		    						{
 		    							// helptext for start/end or setTime on the whole?
 		    							if(player != null)
 		    								player.sendMessage("Usage: /event setTime [event] [start/end] yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    							else
 		    								log.info("Usage: /event setTime [event] [start/end] yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    						}
 		    					}
 		    				}
 				    		if(args[2].equalsIgnoreCase("teleport"))
 				    		{
 				    			
 				    		}
 				    		else
 				    		{
 				    			// just used setTime validly; help text
 				    		}
 		    			}
 					}
 	    			else
 	    			{
 	    				// help text
 	    				if(player != null)
 	    				{
 							if(player.hasPermission("playtime.admins"))
 							{
 		    					player.sendMessage("Usage: /event setTime [event] [start/end] yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 		    					player.sendMessage("Usage: /event setTime [event] [start/end] xx:yy:zz - specify time only, assumes current date.");
 		    					player.sendMessage("Usage: /event setTime [event] teleport [seconds] - specifies time in seconds until subscribed players are teleported to spawn.");
 							}
 							else
 								player.sendMessage("You do not have permission to use this command.");
 	    				}
 	    				else
 	    				{
 
 	    					log.info("Usage: /event setTime [event] [start/end] yyyy-mm-dd hh:mm:ss - specify date and time for start or end of event.");
 	    					log.info("Usage: /event setTime [event] [start/end] xx:yy:zz - specify time only, assumes current date.");
 	    					log.info("Usage: /event setTime [event] teleport [seconds] - specifies time in seconds until subscribed players are teleported to spawn.");
 	    				}
 	    			}
 	    			return true;
 	    		}
	    		else if(args[0].equalsIgnoreCase("subscribe")||args[0].equalsIgnoreCase("sub"))
 	    		{
 					if(args.length >= 2) // if event name has been provided
 					{
 						if(map.get(args[1]) != null) // if event is valid
 						{
 			    			if(player != null)
 			    			{
 				    			if(player.hasPermission("playtime.admins")) // check if player has permission to subscribe other players
 							    {
 		    						if(args.length >= 3)
 		    						{
 			    						// subscribing named player to event
 			    						// get player from name
 		    							Player p = null;
 		    							p = getServer().getPlayer(args[2]);
 			    						if(p != null)
 			    						{
 			    							// get player from server
 				    						boolean s = false;
 				    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 				    						{
 				    							if(t.isSubscribed(p.getName()))
 				    								s = true;
 				    						}
 			
 				    						if(!s) // subscribe player to event
 				    						{
 				    							temp.subscribe(p); 
 				    							map.put(args[1],temp);
 
 				    	    					String path = "events."+args[1]+".subscribers."+p.getName()+".";
 				    	    					this.getConfig().set(path+"origLocation.x",p.getLocation().getX());
 				    	    					this.getConfig().set(path+"origLocation.y",p.getLocation().getY());
 				    	    					this.getConfig().set(path+"origLocation.z",p.getLocation().getZ());
 				    	    					this.getConfig().set(path+"origLocation.world",p.getLocation().getWorld().getName());
 					    					    this.saveConfig();
 				    							
 				    							player.sendMessage(p.getName() + " subscribed to event " + args[1] + ".");
 				    							p.sendMessage("You have been subscribed to event " + args[1] + " by " + player.getName() + ".");
 					    						// do not notify player if event is hidden?
 
 				    						}
 				    						else
 				    						{
 				    							player.sendMessage(p.getName() + " is already subscribed to an event.");
 				    							// feedback on which event?
 				    						}
 			    						}
 			    						else
 			    						{
 				    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 			    						}
 		    						}
 			    					else // subscribing self to event
 			    					{
 			    						temp = map.get(args[1]);
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(player.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // if player is not subscribed
 			    						{
 			    							temp.subscribe(player);
 			    							map.put(args[1],temp);
 
 			    	    					String path = "events."+args[1]+".subscribers."+player.getName()+".";
 			    	    					this.getConfig().set(path+"origLocation.x",player.getLocation().getX());
 			    	    					this.getConfig().set(path+"origLocation.y",player.getLocation().getY());
 			    	    					this.getConfig().set(path+"origLocation.z",player.getLocation().getZ());
 			    	    					this.getConfig().set(path+"origLocation.world",player.getLocation().getWorld().getName());
 				    					    this.saveConfig();
 				    					    
 			    							player.sendMessage("You have subscribed to event " + args[1] + ".");
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							player.sendMessage("You are already subscribed to an event.");
 			    						}
 			    					}
 							    }
 				    			else // player does not have permission; can only subscribe self to event
 				    			{
 			    					if(map.get(args[1]) != null) // check event exists
 			    					{
 			    						temp = map.get(args[1]);
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(player.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // if player is not subscribed
 			    						{
 			    							temp.subscribe(player);
 			    							map.put(args[1],temp);
 
 			    	    					String path = "events."+args[1]+".subscribers."+player.getName()+".";
 			    	    					this.getConfig().set(path+"origLocation.x",player.getLocation().getX());
 			    	    					this.getConfig().set(path+"origLocation.y",player.getLocation().getY());
 			    	    					this.getConfig().set(path+"origLocation.z",player.getLocation().getZ());
 			    	    					this.getConfig().set(path+"origLocation.world",player.getLocation().getWorld().getName());
 				    					    this.saveConfig();
 				    					    
 			    							player.sendMessage("You have subscribed to event " + args[1] + ".");
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							player.sendMessage("You are already subscribed to an event.");
 			    						}
 			    					}
 			    					else
 			    					{
 			    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 			    					}
 				    			}
 			    			}
 			    			else // server is user
 			    			{
 			    				// can only subscribe others
 	    						if(args.length >= 3)
 	    						{
 		    						// subscribing named player to event
 		    						// get player from name
 	    							Player p = null;
 	    							p = getServer().getPlayer(args[2]);
 		    						if(p != null)
 		    						{
 		    							// get player from server
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(p.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // subscribe player to event
 			    						{
 			    							temp.subscribe(p); 
 			    							map.put(args[1],temp);
 
 			    	    					String path = "events."+args[1]+".subscribers."+p.getName()+".";
 			    	    					this.getConfig().set(path+"origLocation.x",p.getLocation().getX());
 			    	    					this.getConfig().set(path+"origLocation.y",p.getLocation().getY());
 			    	    					this.getConfig().set(path+"origLocation.z",p.getLocation().getZ());
 			    	    					this.getConfig().set(path+"origLocation.world",p.getLocation().getWorld().getName());
 				    					    this.saveConfig();
 			    							
 			    							log.info(p.getName() + " subscribed to event " + args[1] + ".");
 			    							p.sendMessage("You have been subscribed to event " + args[1] + " by server.");
 				    						// do not notify player if event is hidden?
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							log.info(p.getName() + " is already subscribed to an event.");
 			    							// feedback on which event?
 			    						}
 		    						}
 		    						else
 		    						{
 			    						log.info("Error: " + args[2] + " is not a valid player.");
 		    						}
 	    						}
 			    			}
 						}
 						else
 						{
 							if (player != null)
 							{
 								player.sendMessage("Error: " + args[1] + " is not a valid event");
 							}
 							else
 							{
 								log.info("Error: " + args[1] + " is not a valid event");
 							}
 						}
 					}
 					else
 					{
 						// help text
 						if(player != null)
 						{
 							player.sendMessage("Usage: /event subscribe [event] - subscribes yourself to the named event.");
 							if(player.hasPermission("playtime.admins"))
 							{
 								player.sendMessage("Usage: /event subscribe [event] [player] - subscribes the named player to the named event.");
 							}
 						}
 						else
 						{
 							log.info("Usage: /event subscribe [event] [player] - subscribes the named player to the named event.");
 						}
 					}
 					return true;
 	    		}
	    		else if(args[0].equalsIgnoreCase("unsubscribe")||args[0].equalsIgnoreCase("unsub"))
 	    		{
 	    			if(player != null)
 	    			{
 		    			if(player.hasPermission("playtime.admins"))
 					    {
     						if(args.length >= 3) // if a name has been provided, admin wants to unsubscribe player from event
     						{
     							Player p = null;
     							p = getServer().getPlayer(args[2]);
 	    						if(p != null)
 	    						{
 	    							boolean s = false;
 		    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 		    						{
 		    							if(t.isSubscribed(p.getName()))
 		    							{
 		    								t.unsubscribe(p);
 		    								
 			    	    					String path = "events."+args[1]+".";
 			    	    					this.getConfig().set(path+"subscribers."+p.getName(),null);
 				    					    this.saveConfig();
 				    					    
 		    								player.sendMessage(p.getName() + " unsubscribed from event " + t.getName());
 		    								p.sendMessage("You have been unsubscribed from event " + t.getName() + " by " + player.getName());
 		    								s = true;
 		    							}
 		    						}
 		    						if(!s)
 		    						{
 		    							player.sendMessage("Error: " + p.getName() + " is not subscribed to any event.");
 		    						}
 	    						}
 	    						else
 	    						{
 		    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 	    						}
     						}
     						else // otherwise, admin just wants to unsubscribe from an event they're in
     						{
     							boolean s = false;
 	    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 	    						{
	    							if(t.isSubscribed(player.getName()))
	    							{
		    	    					String path = "events."+t.getName()+".";
 		    	    					this.getConfig().set(path+"subscribers."+player.getName(),null);
 			    					    this.saveConfig();
 			    					    
 	    								player.sendMessage("You have unsubscribed from event " + t.getName());
 	    								s = true;
	    							}
 	    						}
 	    						if(!s)
 	    						{
 	    							player.sendMessage("Error: you are not subscribed to any event.");
 	    						}
     						}
 					    }
 		    			else // player can only unsubscribe themselves
 		    			{
 							boolean s = false;
     						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
     						{
     							if(t.isSubscribed(player.getName()))
     							{
     								t.unsubscribe(player);
     								if(args.length >= 2)
     								{
 		    	    					String path = "events."+args[1]+".";
 		    	    					this.getConfig().set(path+"subscribers."+player.getName(),null);
 			    					    this.saveConfig();
 			    					    
 	    								player.sendMessage("You have unsubscribed from event " + t.getName());
 	    								s = true;
     								}
     								else
     								{
     									// else if player is subscribed to an event and doesn't name it
     									// check if subscribed to multiple, require event name to unsubscribe.
     									// if only subscribed to one event, unsubscribe from it.
     								}
     							}
     						}
     						if(!s)
     						{
     							player.sendMessage("Error: you are not subscribed to any event.");
     						}
 		    			}
 	    			}
 	    			else
 	    			{
 	    				// server wants to unsubscribe someone
 	    				if(args.length >= 3) // if a name has been provided
 						{
 							Player p = null;
 							p = getServer().getPlayer(args[2]);
     						if(p != null)
     						{
     							boolean s = false;
 	    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 	    						{
 	    							if(t.isSubscribed(p.getName()))
 	    							{
 	    								t.unsubscribe(p);
 	    								
 		    	    					String path = "events."+args[1]+".";
 		    	    					this.getConfig().set(path+"subscribers."+p.getName(),null);
 			    					    this.saveConfig();
 			    					    
 	    								log.info(p.getName() + " unsubscribed from event " + t.getName());
 	    								p.sendMessage("You have been unsubscribed from event " + t.getName() + " by server");
 	    								s = true;
 	    							}
 	    						}
 	    						if(!s)
 	    						{
 	    							log.info("Error: " + p.getName() + " is not subscribed to any event.");
 	    						}
     						}
     						else
     						{
     							log.info("Error: " + args[2] + " is not a valid player.");
     						}
 						}
 	    				else
 	    				{
 	    					log.info("Usage: /event unsubscribe [event] [player] - unsubscribes designated player from named event, if they are subscribed to an event.");
 	    				}
 	    			}
 	    			return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("list"))
 	    		{
 	
 	    			if(player != null)
 	    			{
 		    			player.sendMessage("Events in progress: "); // show something else if none are in progress
 		    			for (PlaytimeEvent value : map.values()) 
 		    			{
 		    				if(value.isHidden())
 		    				{
 		    					if(player.hasPermission("playtime.admins"))
 							    {
 			    					player.sendMessage(value.getInfo());
 							    }
 		    				}
 		    				else
 		    					player.sendMessage(value.getInfo());
 		    				// indicate the event currently subscribed to?
 		    			}
 	    			}
 	    			else
 	    			{
 	    				log.info("Events in progress: "); // show something else if none are in progress
 		    			for (PlaytimeEvent value : map.values()) 
 		    			{
 		    				log.info(value.getInfo());
 		    			}
 	    			}
 	    			return true;
 	    		}
 				return true;
 	    	}
 			return true;
     	// remember to add return trues where necessary
     	}
 	    return false;
     }
 }
