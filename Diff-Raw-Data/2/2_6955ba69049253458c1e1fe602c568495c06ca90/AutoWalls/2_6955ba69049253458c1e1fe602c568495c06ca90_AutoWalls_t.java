 /*
  * AutoWalls by jkush321 is licensed under the
  * Creative Commons Attribution-NonCommercial 3.0 Unported License
  * 
  * You are fully allowed to modify the source code for your own network
  * of servers, but you may not distribute the modified code outside of
  * your servers.
  * 
  * AutoWalls was originally a personal project that was standalone for
  * my own private server, and it slowly accumulated into a giant plugin.
  * 
  * AutoWalls is for dedicated servers that are willing to run just Walls.
  * 
  * The license requires attribution and you have to give credit to jkush321
  * no matter how many changes were made to the code. In some clearly stated
  * way everyone who goes on the server must be able to easily see and be aware
  * of the fact that this code originated from jkush321 and was modified by
  * you or your team.
  * 
  * For more information visit http://bit.ly/AutoWalls
  * 
  */
 
 package com.jkush321.autowalls;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.event.weather.WeatherChangeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.jkush321.autowalls.kits.Kit;
 import com.jkush321.autowalls.kits.KitManager;
 
 public class AutoWalls extends JavaPlugin implements Listener {
 
 	public static Plugin plugin = Bukkit.getPluginManager().getPlugin("AutoWalls");
 	public static final Logger logger = Logger.getLogger("Minecraft");
 	public static List<Player> playing = new CopyOnWriteArrayList<Player>();
 	public static List<Player> redTeam = new CopyOnWriteArrayList<Player>();
 	public static List<Player> blueTeam = new CopyOnWriteArrayList<Player>();
 	public static List<Player> greenTeam = new CopyOnWriteArrayList<Player>();
 	public static List<Player> orangeTeam = new CopyOnWriteArrayList<Player>();
 	public static List<Player> votedFor1 = new ArrayList<Player>();
 	public static List<Player> votedFor2 = new ArrayList<Player>();
 	public static boolean gameInProgress = false;
 	public static boolean voting = false;
 	public static FileConfiguration config;
 	public static boolean gameOver = false;
 	public static int teamSize;
 	public static int[] redSpawn = new int[3];
 	public static int[] blueSpawn = new int[3];
 	public static int[] greenSpawn = new int[3];
 	public static int[] orangeSpawn = new int[3];
 	public static int mapNumber;
 	public static String announcerName;
 	public static Thread beat;
 	public static Thread announcer;
 	public static Thread dropper;
 	public static Thread joinTimer;
 	public static boolean mapVotes;
 	public static boolean blockSneaking;
 	public static boolean disableHealing;
 	public static boolean arrowLightning;
 	public static int arrowLightningChance;
 	public static boolean canJoin = false;
 	public static List<Sign> graves = new ArrayList<Sign>();
 	public static List<String> graveMessages;
 	public static String fullKickMessage;
 	public static String priorityKickMessage;
 	public static boolean teamTeleports;
 	public static String votelink = "";
 	public static int priorityPerDollar;
 	private static Map<Player, Long> lastEvent = new ConcurrentHashMap<>();
 	public static int secondsBeforeTeleport;
 	public final static String version = "1.1r1";
 	public static int earlyJoinPriority, lateJoinPriority;
 	public static boolean lateJoins;
 	public static boolean preventFireBeforeWallsFall;
 	public static boolean useTabApi;
 	public static ArrayList<String> dead = new ArrayList<String>();
 
 	public void onEnable()
 	{
 		plugin = this;
 		
 		getServer().getPluginManager().registerEvents(this, this);
 		config = getConfig();
 		
 		config.addDefault("votes.players.jkush321", 500);
 		config.addDefault("votes.players.example_player", 2);
 		config.addDefault("priorities", true);
 		config.addDefault("team-size", 4);
 		config.addDefault("next-map", 1);
 		config.addDefault("announcer-name", "Announcer");
 		config.addDefault("announcements", "Seperate Announements With SemiColons;You should have at least 2 messages;Your message here!");
 		config.addDefault("map-votes", true);
 		config.addDefault("prevent-sneaking-after-walls-fall", true);
 		config.addDefault("disable-healing-after-walls-fall", true);
 		config.addDefault("rare-lightning-strike-on-arrow-land", true);
 		config.addDefault("one-in-blank-chance-of-lightning", 250);
 		config.addDefault("seconds-before-can-join-team", 60);
 		config.addDefault("grave-messages", Arrays.asList("He was loved","Loved by many","Will be missed","Died young","In our hearts","Has been lost","All gone now","Will be mourned","Had a good life","Withered away" ));
 		config.addDefault("full-server-message", "The server is full and your priority is not high enough!");
 		config.addDefault("priority-kick-message", "Someone with higher priority joined!");
 		config.addDefault("team-teleports", true);
 		config.addDefault("game-length-in-minutes", 15);
 		config.addDefault("vote-link", "my-vote-link.com");
 		config.addDefault("priority-per-dollar", 5);
 		config.addDefault("seconds-before-teleport", 3);
 		config.addDefault("early-join-priority", 1);
 		config.addDefault("late-join-priority", 25);
 		config.addDefault("late-joins", true);
 		config.addDefault("prevent-fire-before-walls-fall", true);
 		config.addDefault("max-color-cycler-time", 120);
 		config.addDefault("use-tab-api", true);
 		
 		config.options().copyDefaults(true);
 	    saveConfig();	    
 	    
 	    announcerName = config.getString("announcer-name");
 	    mapNumber = config.getInt("next-map");
 	    mapVotes = config.getBoolean("map-votes");
 	    blockSneaking = config.getBoolean("prevent-sneaking-after-walls-fall");
 	    disableHealing = config.getBoolean("disable-healing-after-walls-fall");
 	    arrowLightning = config.getBoolean("rare-lightning-strike-on-arrow-land");
 	    arrowLightningChance = config.getInt("one-in-blank-chance-of-lightning");
 	    graveMessages=config.getStringList("grave-messages");
 	    fullKickMessage=config.getString("full-server-message");
 	    priorityKickMessage=config.getString("priority-kick-message");
 	    JoinTimer.timeleft = config.getInt("seconds-before-can-join-team");
 	    teamTeleports = config.getBoolean("team-teleports");
 	    WallDropper.time=config.getInt("game-length-in-minutes") * 60;
 	    votelink = config.getString("vote-link");
 	    priorityPerDollar=config.getInt("priority-per-dollar");
 	    secondsBeforeTeleport=config.getInt("seconds-before-teleport");
 	    earlyJoinPriority = config.getInt("early-join-priority");
 	    lateJoinPriority = config.getInt("late-join-priority");
 	    lateJoins = config.getBoolean("late-joins");
 	    preventFireBeforeWallsFall = config.getBoolean("prevent-fire-before-walls-fall");
 	    ColorCycler.MAX_COLOR_TIME = config.getInt("max-color-cycler-time");
 	    useTabApi = config.getBoolean("use-tab-api");
 	    
 	    if (mapNumber == 1)
 	    {	
 			redSpawn[0] = 297;
 			redSpawn[1] = 118;
 			redSpawn[2] = -848;
 			
 			blueSpawn[0] = 403;
 			blueSpawn[1] = 118;
 			blueSpawn[2] = -848;
 			
 			greenSpawn[0] = 403;
 			greenSpawn[1] = 118;
 			greenSpawn[2] = -736;
 			
 			orangeSpawn[0] = 291;
 			orangeSpawn[1] = 118;
 			orangeSpawn[2] = -736;
 	    }
 	    else
 	    {
 	    	redSpawn[0] = -868;
 			redSpawn[1] = 74;
 			redSpawn[2] = -212;
 			
 			blueSpawn[0] = -868;
 			blueSpawn[1] = 74;
 			blueSpawn[2] = -132;
 			
 			greenSpawn[0] = -718;
 			greenSpawn[1] = 74;
 			greenSpawn[2] = -132;
 			
 			orangeSpawn[0] = -718;
 			orangeSpawn[1] = 74;
 			orangeSpawn[2] = -212;
 	    }
 	    
 	    teamSize = config.getInt("team-size");
 	    
 	    Announcer a = new Announcer();
 	    
 	    //My CC3.0 Attribution license requires you to leave this in some way
 	    //If you have forked it you can say...
 	    //"This server runs MyFork by Me based on AutoWalls by Jkush321" or something similar
 	    String[] announcements = config.getString("announcements").split(";");
 	    Announcer.messages.add("This server runs AutoWalls by jkush321");
 	    for (String s : announcements)
 	    {
 	    	Announcer.messages.add(s);
 	    }
 	    
 	    announcer = new Thread(a);
 	    announcer.start();
 	    
 	    beat = new Thread(new Heartbeat());
 	    beat.start();
 	    
 	    joinTimer = new Thread(new JoinTimer());
 	    joinTimer.start();
 
 		dropper = new Thread(new WallDropper());
 		dropper.start();
 		
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 			public void run()
 			{
 				ColorCycler.tick();
 			}
 		}, 0L, 20L);
 		
 		Grenades.init();
 		KitManager.fillKits();
 		
 		if (Bukkit.getPluginManager().getPlugin("TagAPI")!= null)
 		{
 			Bukkit.getPluginManager().registerEvents(new ColoredNames(), this);
 			Tags.useTagAPI=true;
 			System.out.println("[AutoWalls] Successfully hooked into TagAPI!");
 		}
 		if (Bukkit.getPluginManager().getPlugin("TabAPI")!=null)
 		{
 			useTabApi=true;
 			System.out.println("[AutoWalls] Successfully hooked into TagAPI!");
 		}
 		else if (useTabApi)
 		{
 			System.out.println("[AutoWalls] Error! TabAPI is not installed but it was set to be used in the config!");
 			useTabApi = false;
 		}
 	}
 	@SuppressWarnings("deprecation")
 	public void onDisable()
 	{
 		announcer.stop();
 		beat.stop();
 		dropper.stop();
 	}
 	
 	public boolean onCommand(CommandSender cmdSender, Command cmd, String cmdString, String[] args)
 	{
 		if (gameOver) return true;
 		if (cmd.getLabel().equalsIgnoreCase("join"))
 		{
 			if (cmdSender instanceof Player)
 			{
 				Player p = (Player) cmdSender;
 				boolean allowed = false;
 				if (config.getInt("votes.players." + p.getName()) >= earlyJoinPriority && !gameInProgress) { allowed = true; }
 				if (canJoin && !gameInProgress){ allowed = true; }
 				if (playing.size()<teamSize*4 && config.getInt("votes.players." + p.getName()) >= lateJoinPriority && WallDropper.time > 0) { allowed = true; }
 				if (!allowed)
 				{
 					cmdSender.sendMessage(ChatColor.DARK_RED + "You can not join the game at this time!");
 					return true;
 				}
 				if (args.length == 0) // Add to random team
 				{
 					if (redTeam.size()<teamSize)
 						joinTeam(p, "red");
 					else if (blueTeam.size()<teamSize)
 						joinTeam(p, "blue");
 					else if (greenTeam.size()<teamSize)
 						joinTeam(p, "green");
 					else if (orangeTeam.size()<teamSize)
 						joinTeam(p, "orange");
 					else p.sendMessage(ChatColor.RED + "Every team is full!");
 				}
 				else if (args.length == 1) // Add to specified team
 				{
 					if (args[0].equalsIgnoreCase("red"))
 						joinTeam(p,"red");
 					else if (args[0].equalsIgnoreCase("blue"))
 						joinTeam(p,"blue");
 					else if (args[0].equalsIgnoreCase("green"))
 						joinTeam(p,"green");
 					else if (args[0].equalsIgnoreCase("orange"))
 						joinTeam(p,"orange");
 					else p.sendMessage(ChatColor.DARK_RED + "The Team " + args[0] + " Is Invalid!");
 				}
 				else p.sendMessage(ChatColor.RED + "Too Many Arguments. /join <red|blue|green|orange>");
 			}
 			else cmdSender.sendMessage("You can't join a team, console :P");
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("leave"))
 		{
 			if (cmdSender instanceof Player)
 			{
 				if (playing.contains((Player) cmdSender)){
 					Bukkit.broadcastMessage(ChatColor.YELLOW + cmdSender.getName() + ChatColor.DARK_RED + " has left the game!");
 					((Player) cmdSender).setHealth(0);
 					leaveTeam((Player) cmdSender);
 				}
 				else cmdSender.sendMessage(ChatColor.DARK_RED + "You aren't on a team");
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("playing"))
 		{
 			if (!(cmdSender instanceof Player)) {
 				//If sent from console do...
 				
 				cmdSender.sendMessage(ChatColor.GRAY + "There are " + playing.size() + " people playing");
 				String s = (ChatColor.GRAY + "Red: " + ChatColor.WHITE);
 				for (Player pl : redTeam)
 				{
 					s+=pl.getName() + ", ";
 				}
 				cmdSender.sendMessage(s.substring(0,s.length()-2));
 				s=(ChatColor.GRAY + "Blue: " + ChatColor.WHITE);
 				for (Player pl : blueTeam)
 				{
 					s+=pl.getName() + ", ";
 				}
 				cmdSender.sendMessage(s.substring(0,s.length()-2));
 				s=(ChatColor.GRAY + "Green: " + ChatColor.WHITE);
 				for (Player pl : greenTeam)
 				{
 					s+=pl.getName() + ", ";
 				}
 				cmdSender.sendMessage(s.substring(0,s.length()-2));
 				s=(ChatColor.GRAY + "Orange: " + ChatColor.WHITE);
 				for (Player pl : orangeTeam)
 				{
 					s+=pl.getName() + ", ";
 				}
 				cmdSender.sendMessage(s.substring(0,s.length()-2));
 				return true;
 				
 			} else {
 			Player p = (Player) cmdSender;
 			p.sendMessage(ChatColor.GRAY + "There are " + playing.size() + " people playing");
 			String s = (ChatColor.GRAY + "Red: " + ChatColor.WHITE);
 			for (Player pl : redTeam)
 			{
 				s+=pl.getName() + ", ";
 			}
 			p.sendMessage(s.substring(0,s.length()-2));
 			s=(ChatColor.GRAY + "Blue: " + ChatColor.WHITE);
 			for (Player pl : blueTeam)
 			{
 				s+=pl.getName() + ", ";
 			}
 			p.sendMessage(s.substring(0,s.length()-2));
 			s=(ChatColor.GRAY + "Green: " + ChatColor.WHITE);
 			for (Player pl : greenTeam)
 			{
 				s+=pl.getName() + ", ";
 			}
 			p.sendMessage(s.substring(0,s.length()-2));
 			s=(ChatColor.GRAY + "Orange: " + ChatColor.WHITE);
 			for (Player pl : orangeTeam)
 			{
 				s+=pl.getName() + ", ";
 			}
 			p.sendMessage(s.substring(0,s.length()-2));
 			return true;
 			}
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("time"))
 		{
 			int minutes = 0;
 			int seconds = 0;
 			if (!gameInProgress)
 			{
 				cmdSender.sendMessage(ChatColor.GRAY + "The game hasn't started yet!"); return true;
 			}
 			minutes = WallDropper.time / 60;
 			seconds = WallDropper.time % 60;
 			
 			if (minutes==0 && seconds==0) {cmdSender.sendMessage(ChatColor.GRAY + "The Walls Already Dropped!"); return true;}
 			cmdSender.sendMessage(ChatColor.GRAY + "The walls will drop in " + minutes + " minutes and " + seconds + " seconds!");
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("team"))
 		{
 			Player p = (Player) cmdSender;
 			if (!playing.contains(p))
 			{
 				p.sendMessage(ChatColor.YELLOW + "You are not in game and have no team!"); return true;
 			}
 			else
 			{
 				if (redTeam.contains(p))
 				{
 					p.sendMessage(ChatColor.YELLOW + "You are on the red team with...");
 					for (Player pl : redTeam)
 					{
 						if (pl!=p) p.sendMessage(ChatColor.YELLOW + "-" + pl.getName());
 					}
 					if (redTeam.size()==1) p.sendMessage(ChatColor.YELLOW + "No one else :[");
 				}
 				else if (blueTeam.contains(p))
 				{
 					p.sendMessage(ChatColor.YELLOW + "You are on the blue team with...");
 					for (Player pl : blueTeam)
 					{
 						if (pl!=p) p.sendMessage(ChatColor.YELLOW + "-" + pl.getName());
 					}
 					if (blueTeam.size()==1) p.sendMessage(ChatColor.YELLOW + "No one else :[");
 				}
 				else if (greenTeam.contains(p))
 				{
 					p.sendMessage(ChatColor.YELLOW + "You are on the green team with...");
 					for (Player pl : greenTeam)
 					{
 						if (pl!=p) p.sendMessage(ChatColor.YELLOW + "-" + pl.getName());
 					}
 					if (greenTeam.size()==1) p.sendMessage(ChatColor.YELLOW + "No one else :[");
 				}
 				else if (orangeTeam.contains(p))
 				{
 					p.sendMessage(ChatColor.YELLOW + "You are on the orange team with...");
 					for (Player pl : orangeTeam)
 					{
 						if (pl!=p) p.sendMessage(ChatColor.YELLOW + "-" + pl.getName());
 					}
 					if (orangeTeam.size()==1) p.sendMessage(ChatColor.YELLOW + "No one else :[");
 				}
 				return true;
 			}
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tp"))
 		{
 			if (args.length == 1)
 			{
 				Player p = (Player) cmdSender;
 				Player p2 = Bukkit.getPlayer(args[0]);
 				if (p2!=null && p2.isOnline())
 				{
 					if (!playing.contains(p) || p.hasPermission("walls.op")) p.teleport(p2);
 					else
 					{
 						if (teamTeleports && secondsBeforeTeleport > 0)
 						{
 							if (redTeam.contains(p) && redTeam.contains(p2)) TeleportManager.createTpRunnable(p, p2);
 							else if (blueTeam.contains(p) && blueTeam.contains(p2)) TeleportManager.createTpRunnable(p, p2);
 							else if (greenTeam.contains(p) && greenTeam.contains(p2)) TeleportManager.createTpRunnable(p, p2);
 							else if (orangeTeam.contains(p) && orangeTeam.contains(p2)) TeleportManager.createTpRunnable(p, p2);
 							else { p.sendMessage(ChatColor.YELLOW + p2.getName() + " is not on your team!"); return true; }
 							p.sendMessage(ChatColor.YELLOW + "You will be teleported to " + ChatColor.DARK_GREEN + p2.getName() + ChatColor.YELLOW + "if you do not move for " + ChatColor.YELLOW + secondsBeforeTeleport + ChatColor.YELLOW + " seconds");
 						}
 						else if (teamTeleports)
 						{
 							if (redTeam.contains(p) && redTeam.contains(p2)) p.teleport(p2);
 							else if (blueTeam.contains(p) && blueTeam.contains(p2)) p.teleport(p2);
 							else if (greenTeam.contains(p) && greenTeam.contains(p2)) p.teleport(p2);
 							else if (orangeTeam.contains(p) && orangeTeam.contains(p2)) p.teleport(p2);
 							else p.sendMessage(ChatColor.YELLOW + p2.getName() + " is not on your team!");
 						}
 						else p.sendMessage(ChatColor.DARK_AQUA + "This server has team teleporting disabled!");
 					}
 				}
 				else p.sendMessage(ChatColor.GRAY + "That player is not online!");
 			}
 			else if (args.length == 4)
 			{
 				Player p = Bukkit.getPlayer(args[0]);
 				if (p.isOnline() && p != null)
 				{
 					double x, y, z;
 					try{
 						x = Double.parseDouble(args[1]);
 						y = Double.parseDouble(args[2]);
 						z = Double.parseDouble(args[3]);
 					}catch (Exception e)
 					{
 						cmdSender.sendMessage(ChatColor.DARK_RED + "Invalid coordinates");
 						return true;
 					}
 					p.teleport(new Location(p.getWorld(), x, y, z));
 				}
 				else
 					cmdSender.sendMessage(ChatColor.DARK_RED + "Player not found.");
 			}
 			else cmdSender.sendMessage("Invalid Arguments. /tp playername");
 			
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("pri"))
 		{
 			if (args.length!=2 && args.length!=3) return false;
 			if (!cmdSender.hasPermission("walls.op")) return false;
 			Player pl = Bukkit.getPlayer(args[0]);
 			int a = Integer.parseInt(args[1]);
 			if (config.isSet("votes.players." + pl.getName()))
 			{
 				config.set("votes.players." + pl.getName(), config.getInt("votes.players." + pl.getName()) + a);
 			}
 			else config.set("votes.players." + pl.getName(), a);
 			if (Bukkit.getPlayer(pl.getName()) != null && Bukkit.getPlayer(pl.getName()).isOnline()) Bukkit.getPlayer(pl.getName()).sendMessage(ChatColor.YELLOW + "Your priority is now " + config.getInt("votes.players." + pl.getName()));
 			if (args.length==3) Bukkit.broadcastMessage(ChatColor.AQUA + pl.getName() + " Donated To Us And Now Has Login Priority of " + config.getInt("votes.players." + pl.getName()) + "! :D Thank you very much, " + pl.getName());
 			saveConfig();
 			if (!pl.isOnline()) { cmdSender.sendMessage("Done!"); return true; }
 			pl.setDisplayName(pl.getName());
 			if (config.isSet("votes.players." + pl.getName()) && config.getInt("votes.players." + pl.getName()) >= 20) { pl.setDisplayName(ChatColor.DARK_AQUA + pl.getName() + ChatColor.WHITE); }
 			if (config.isSet("votes.players." + pl.getName()) && config.getInt("votes.players." + pl.getName()) >= 250) { pl.setDisplayName(ChatColor.DARK_RED + pl.getName() + ChatColor.WHITE); }
 			if (config.getBoolean("priorities") == true)
 			{
 				if (config.isSet("votes.players." + pl.getName())) { pl.setDisplayName(ChatColor.YELLOW + "[" + config.getInt("votes.players." + pl.getName()) + "]" + ChatColor.GRAY + pl.getDisplayName() + ChatColor.WHITE); }
 				else pl.setDisplayName(ChatColor.GRAY + "[0]" + pl.getDisplayName() + ChatColor.WHITE);
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tpplayers"))
 		{
 			if (!cmdSender.hasPermission("walls.op")) return false;
 			for (Player p : playing)
 			{
 				if (p!=(Player)cmdSender)
 					p.teleport((Player)cmdSender);
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tpspecs"))
 		{
 			if (!cmdSender.hasPermission("walls.op")) return false;
 			for (Player p : Bukkit.getOnlinePlayers())
 			{
 				if (!playing.contains(p) && p!=(Player)cmdSender)
 					p.teleport((Player)cmdSender);
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tpall"))
 		{
 			if (!cmdSender.hasPermission("walls.op")) return false;
 			for (Player p : Bukkit.getOnlinePlayers())
 			{
 				if (p!=(Player)cmdSender)
 					p.teleport((Player)cmdSender);
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tphere"))
 		{
 			if (!cmdSender.hasPermission("walls.op")) return false;
 			if (args.length!=1) { cmdSender.sendMessage(ChatColor.RED + "Invalid arguments"); return true; }
 			Player pl = Bukkit.getPlayer(args[0]);
 			if (pl!=null && pl.isOnline())
 			{
 				pl.teleport((Player)cmdSender);
 			}
 			else cmdSender.sendMessage(ChatColor.RED + "Player is not online");
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tc"))
 		{
 			Player p = (Player) cmdSender;
 			if (!playing.contains(p)) { p.sendMessage(ChatColor.RED + "You have to be on a team to teamchat!"); return true; }
 			if (!TeamChat.teamChatting.contains(p)) { TeamChat.teamChatting.add(p); p.sendMessage(ChatColor.YELLOW + "You are now team chatting!"); return true;}
 			if (TeamChat.teamChatting.contains(p)) { TeamChat.teamChatting.remove(p); p.sendMessage(ChatColor.YELLOW + "You have disabled team chatting!"); return true; }
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("tell") || cmd.getLabel().equalsIgnoreCase("t"))
 		{
 			if (cmdSender instanceof Player)
 			{
 				Player p = (Player) cmdSender;
 				if (args.length < 2) { cmdSender.sendMessage(ChatColor.GRAY + "Invalid arguments... /tell [name] [message]"); return true; }
 				String msg="";
 				boolean first = true;
 				for (String s : args)
 				{
 					if (!first)
 						msg+=s+" ";
 					else first = false;
 				}
 				msg=msg.trim();
 				Player who = Bukkit.getPlayer(args[0]);
 				if (playing.contains(who) && !playing.contains(p)) { p.sendMessage(ChatColor.GRAY + "You can not private message that person!"); }
 				else { p.sendMessage(ChatColor.GRAY + "[" + p.getName() + ChatColor.STRIKETHROUGH + " >" + ChatColor.RESET + who.getName() + "] "+ ChatColor.WHITE + msg); who.sendMessage(ChatColor.WHITE + "[" + p.getName() + ChatColor.STRIKETHROUGH + " >" + ChatColor.RESET + who.getName() + "] " + ChatColor.WHITE + msg); }
 				return true;
 			}
 			else
 			{
 				if (args.length < 2) { cmdSender.sendMessage(ChatColor.GRAY + "Invalid arguments... /tell [name] [message]"); return true; }
 				String msg="";
 				boolean first = true;
 				for (String s : args)
 				{
 					if (!first)
 						msg+=s+" ";
 					else first = false;
 				}
 				msg=msg.trim();
 				Player who = Bukkit.getPlayer(args[0]);
 				if (!who.isOnline() || who==null) { cmdSender.sendMessage(ChatColor.DARK_RED + "Player not found"); return true;}
 					cmdSender.sendMessage(ChatColor.GRAY + "[Private] " + ChatColor.WHITE + msg); who.sendMessage(ChatColor.GRAY + "[Private] " + ChatColor.WHITE + msg);
 				return true;
 			}
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("me"))
 		{
 			if (args.length == 0) { cmdSender.sendMessage(ChatColor.GRAY + "Invalid arguments... /me [message]"); return true; }
 			Player p = (Player) cmdSender;
 			String msg = "";
 			for (String s : args)
 			{
 				msg+=s + " ";
 			}
 			msg=msg.trim();
 			TeamChat.say(p, p.getDisplayName() + " " + ChatColor.GRAY + "* " + msg);
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("forcestart"))
 		{
 			if (playing.size() >= 2 && !gameInProgress && JoinTimer.timeleft <= 0) {
 				Bukkit.broadcastMessage(ChatColor.DARK_RED + "FORCE STARTING GAME");
 				startGame();
 			}
 			else
 			{
 				cmdSender.sendMessage(ChatColor.GRAY + "There have to be at least 2 players, and the game can not be started yet, and the join timer must be over!");
 			}
 			return true;
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("day"))
 		{
 			Bukkit.getWorld("walls").setTime(100);
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("night"))
 		{
 			Bukkit.getWorld("walls").setTime(14000);
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("yell"))
 		{
 			if (config.getInt("votes.players." + cmdSender.getName()) >= 20 || !(cmdSender instanceof Player) || cmdSender.hasPermission("walls.op"))
 			{
 				String message = "";
 				for (String s : args)
 				{
 					message+=s + " ";
 				}
 				message=message.trim();
 				if (args.length != 0) { Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[Yell] " + ChatColor.AQUA + cmdSender.getName() + ": " + ChatColor.WHITE + message); }
 				else cmdSender.sendMessage(ChatColor.AQUA + "Usage... /yell [message]");
 			}
 			else cmdSender.sendMessage(ChatColor.AQUA + "You need at least 20 priority to do that.");
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("forcedrop"))
 		{
 			if (WallDropper.time <= 5) { cmdSender.sendMessage(ChatColor.AQUA + "The walls have already dropped!"); }
 			else { WallDropper.time = 5; }
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("forceend"))
 		{
 			endGame("ADMINS", "No one.");
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("fly"))
 		{
 			if (!gameInProgress) { cmdSender.sendMessage(ChatColor.RED + "The game did not start yet, no reason to fly"); return true; }
 			if (playing.contains((Player) cmdSender)) { cmdSender.sendMessage(ChatColor.RED + "You are in game! lol"); return true; }
 			((Player) cmdSender).setAllowFlight(true);
 			cmdSender.sendMessage(ChatColor.YELLOW + "You are now able to fly!");
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("kit"))
 		{
 			if (args.length == 1) {
 				if (JoinTimer.timeleft > 0 || !gameInProgress) {
 
 					if (KitManager.findKit(args[0]) != null) {
 						Kit k = KitManager.findKit(args[0]);
 						int p = 0;
 						if (config
 								.isSet("votes.players." + cmdSender.getName()))
 							p = config.getInt("votes.players."
 									+ cmdSender.getName());
 						if (k.getRequiredPriority() <= p) {
 							KitManager.setKit((Player) cmdSender, k);
 							cmdSender.sendMessage(ChatColor.DARK_AQUA + "Selected kit "
 									+ k.getName());
 						} else {
 							cmdSender
 									.sendMessage(ChatColor.DARK_RED + "That kit is not available to you! You can get " + priorityPerDollar + " priority for every $1 donated");
 						}
 					} else {
 						cmdSender.sendMessage(ChatColor.DARK_RED + "That kit was not found.");
 					}
 				} else
 					cmdSender.sendMessage(ChatColor.DARK_RED + "It is too late to choose a kit!");
 			} else if (args.length == 0) {
 					int p = 0;
 					if (config.isSet("votes.players." + cmdSender.getName()))
 						p = config.getInt("votes.players." + cmdSender.getName());
 					String m1 = (ChatColor.GRAY + "Available Kits: " + ChatColor.WHITE);
 					for (Kit k : KitManager.kitList)
 					{
 						if (k.getRequiredPriority()<=p)
 							m1 += "(" + k.getRequiredPriority() + ")" + k.getName() + ", ";
 					}
 					String m2 = (ChatColor.GRAY + "Unavailable Kits: " + ChatColor.WHITE);
 					for (Kit k : KitManager.kitList)
 					{
 						if (k.getRequiredPriority()>p)
 						m2 += "(" + k.getRequiredPriority() + ")" + k.getName() + ", ";
 					}
 					m1 = m1.substring(0, m1.length()-2) + ".";
 					m2 = m2.substring(0, m2.length()-2) + ".";
 					cmdSender.sendMessage(m1);
 					cmdSender.sendMessage(m2);
 					cmdSender.sendMessage(ChatColor.DARK_AQUA + "To unlock the unavaible kits you can donate for priority. You get " + priorityPerDollar + " priority for $1");
 				}
 				else
 				{
 					cmdSender.sendMessage(ChatColor.DARK_RED + "/kit [name]");
 				}
 		}
 		else if (cmd.getLabel().equalsIgnoreCase("prefix"))
 		{
 			if (args.length < 2)
 			{
 				return false;
 			}
 			else
 			{
 				String playerName = "";
 				if (Bukkit.getPlayer(args[0]) == null || !Bukkit.getPlayer(args[0]).isOnline())
 				{
 					playerName = args[0];
 				}
 				else
 				{
 					playerName = Bukkit.getPlayer(args[0]).getName();
 				}
 				String fullPrefix = "";
 				if (args.length == 2) fullPrefix = args[1];
 				else
 				{
 					for (int i = 0; i < args.length; i++)
 					{
 						if (i > 0)
 						{
 							fullPrefix += args[i] + " ";
 						}
 					}
 					fullPrefix = fullPrefix.trim();
 				}
 				config.set("prefix." + playerName, fullPrefix);
 				saveConfig();
 				if (Bukkit.getPlayer(playerName).isOnline() && Bukkit.getPlayer(playerName) != null)
 				{
 					if (config.isSet("prefix." + Bukkit.getPlayer(playerName).getName())) Bukkit.getPlayer(playerName).setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("prefix." + Bukkit.getPlayer(playerName).getName()).replace("{pri}", config.getInt("votes.players." + Bukkit.getPlayer(playerName).getName())+"") + Bukkit.getPlayer(playerName).getName() + ChatColor.WHITE));
 				}
 				cmdSender.sendMessage(ChatColor.YELLOW + "Set " + playerName + "'s prefix to " + ChatColor.WHITE + "\"" + fullPrefix + ChatColor.WHITE + "\"");
 			}
 		}
 		else return false;
 		
 		return true;
 	}
 	
 	public void joinTeam(Player p, String team)
 	{
 		if (playing.contains(p)) {p.sendMessage(ChatColor.RED + "You are already on a team!"); }
 		else
 		{
 			if (team == "red")
 			{
 				if (redTeam.size() == teamSize)
 				{
 					p.sendMessage(ChatColor.RED + "That team is full!"); return;
 				}
 				redTeam.add(p);
 			}
 			if (team == "blue")
 			{
 				if (blueTeam.size() == teamSize)
 				{
 					p.sendMessage(ChatColor.RED + "That team is full!"); return;
 				}
 				blueTeam.add(p);
 			}
 			if (team == "green")
 			{
 				if (greenTeam.size() == teamSize)
 				{
 					p.sendMessage(ChatColor.RED + "That team is full!"); return;
 				}
 				greenTeam.add(p);
 			}
 			if (team == "orange")
 			{
 				if (orangeTeam.size() == teamSize)
 				{
 					p.sendMessage(ChatColor.RED + "That team is full!"); return;
 				}
 				orangeTeam.add(p);
 			}
 			playing.add(p);
 			p.setAllowFlight(false);
 			p.setGameMode(GameMode.SURVIVAL);
 			for (Player pl : Bukkit.getOnlinePlayers())
 			{
 				if (p != pl && !playing.contains(p)) p.hidePlayer(pl);
 			}
 			removeDeadPlayer(p.getName());
 			Tabs.updateAll();
 			Tags.refreshPlayer(p);
 			Bukkit.broadcastMessage(ChatColor.RED + p.getName() + " has joined the " + team + " team!");
 			int remaining = (teamSize * 4) - playing.size();
 			String s = "s";
 			if (remaining == 1) s = "";
 			Bukkit.broadcastMessage(ChatColor.AQUA + "There is room for " + remaining + " more player" + s + "!");
 			if (remaining == 0 && !gameInProgress)
 			{
 				Bukkit.broadcastMessage(ChatColor.GREEN + "It is time for the game to start! " + ChatColor.RED + "Go be the best you can be now!");
 				startGame();
 			}
 			if (gameInProgress && lateJoins)
 			{
 				if (team.equals("red"))
 				{
 					p.teleport(new Location(p.getWorld(), redSpawn[0], redSpawn[1], redSpawn[2]));
 				}
 				else if (team.equals("blue"))
 				{
 					p.teleport(new Location(p.getWorld(), blueSpawn[0], blueSpawn[1], blueSpawn[2]));
 				}
 				else if (team.equals("orange"))
 				{
 					p.teleport(new Location(p.getWorld(), orangeSpawn[0], orangeSpawn[1], orangeSpawn[2]));
 				}
 				else if (team.equals("green"))
 				{
 					p.teleport(new Location(p.getWorld(), greenSpawn[0], greenSpawn[1], greenSpawn[2]));
 				}
 				p.sendMessage(ChatColor.YELLOW + "It is too late to receive a kit!");
 				
 				p.sendMessage(ChatColor.YELLOW + "Good Luck!");
 			}
 			p.setHealth(20);
 			p.setFoodLevel(20);
 			p.setExp(0);
 			p.setLevel(0);
 			p.setNoDamageTicks(60);
 		}
 	}
 	public void leaveTeam(Player p)
 	{
 		if (playing.contains(p)) playing.remove(p);
 		if (redTeam.contains(p)) redTeam.remove(p);
 		if (blueTeam.contains(p)) blueTeam.remove(p);
 		if (greenTeam.contains(p)) greenTeam.remove(p);
 		if (orangeTeam.contains(p)) orangeTeam.remove(p);
 		if (TeamChat.teamChatting.contains(p)) TeamChat.teamChatting.remove(p);
 		if (WallDropper.time > 0 && gameInProgress && lateJoins) { Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "A player with " + lateJoinPriority + "+ priority may " + ChatColor.YELLOW + "/join and take " + p.getName() + "'s place!"); }
 		for (Player pl : Bukkit.getOnlinePlayers())
 		{
 			if (pl!=p)
 			{
 				if (!p.canSee(pl))
 					p.showPlayer(pl);
 			}
 		}
 		Tags.refreshPlayer(p);
 		Tabs.updateAll();
 		checkStats();
 	}
 	public void startGame()
 	{
 		if (gameInProgress) return;
 		if (!redTeam.isEmpty())
 			for (Player p : redTeam)
 			{
 				p.teleport(new Location(p.getWorld(),redSpawn[0],redSpawn[1],redSpawn[2]));
 			}
 		if (!blueTeam.isEmpty())
 			for (Player p : blueTeam)
 			{
 				p.teleport(new Location(p.getWorld(),blueSpawn[0],blueSpawn[1],blueSpawn[2]));
 			}
 		if (!greenTeam.isEmpty())
 			for (Player p : greenTeam)
 			{
 				p.teleport(new Location(p.getWorld(),greenSpawn[0],greenSpawn[1],greenSpawn[2]));
 			}
 		if (!orangeTeam.isEmpty())
 			for (Player p : orangeTeam)
 			{
 				p.teleport(new Location(p.getWorld(),orangeSpawn[0],orangeSpawn[1],orangeSpawn[2]));
 			}
 		for (Player p : playing)
 		{
 			p.sendMessage(ChatColor.YELLOW + "Good Luck!");
 			if (KitManager.getKit(p) != null)
 			{
 				p.getInventory().addItem(KitManager.getKit(p).getItemStack());
 			}
 		}
 		gameInProgress=true;
 		for (Player p : Bukkit.getOnlinePlayers())
 		{
 			if (!playing.contains(p))
 			{
 				spectate(p);
 			}
 		}
 	}
 	public void endGame(String team, String players)
 	{
 		if (!gameInProgress) return;
 		gameInProgress=false;
 		gameOver=true;
 		try {
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		for (Player p : playing)
 		{
 			p.setHealth(0);
 			Tags.refreshPlayer(p);
 		}
 		if (mapVotes)
 		{
 			Bukkit.broadcastMessage(ChatColor.DARK_RED + "The " + team + " team has won the game!");
 			Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Winning Players:  "+ ChatColor.DARK_GREEN + players);
 			try { Thread.sleep(1000); } catch (Exception e) { }
 			Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "It is time to vote for the next map!");
 			Bukkit.broadcastMessage(ChatColor.YELLOW + "1 - The Walls   - by Hypixel - Modified by staff team");
 			Bukkit.broadcastMessage(ChatColor.YELLOW + "2 - The Walls 2 - by Hypixel - Modified by staff team");
 			Bukkit.broadcastMessage(ChatColor.GRAY + "Type the number you want in chat. Vote will last 30 seconds");
 			
 			voting = true;
 			
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new VoteResult(), 20L * 30L);
 		}
 		else
 		{
 			for (Player p : Bukkit.getOnlinePlayers())
 			{
 				p.kickPlayer(ChatColor.RED + "The " + team + " team has won the game! " + ChatColor.DARK_AQUA + "Reconnect and type /join");
 				Bukkit.shutdown();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onLeave(PlayerQuitEvent e)
 	{
 		if (playing.contains(e.getPlayer()) && gameInProgress) e.getPlayer().setHealth(0);
 		else if (playing.contains(e.getPlayer()) && !gameInProgress) leaveTeam(e.getPlayer());
 		if (getLastEvent(e.getPlayer()) != 0) lastEvent.remove(e.getPlayer());
 		checkStats();
 		Tags.refreshPlayer(e.getPlayer());
 		Tabs.removePlayer(e.getPlayer());
 		e.setQuitMessage(ChatColor.AQUA + "- " + ChatColor.DARK_AQUA + e.getPlayer().getName() + ChatColor.GRAY + " has left");
 	}
 	@EventHandler
 	public void onDeath(PlayerDeathEvent e)
 	{
 		try{ 
 		if (!playing.contains(e.getEntity())) {
 			e.setDeathMessage(""); 
 			if (e.getEntity().getInventory().getSize() > 0)
 			{
 				while (e.getDrops().size()>0)
 					e.getDrops().remove(0);
 			}
 			return;
 		}
 		if (gameInProgress && playing.contains(e.getEntity()))
 		{
 			playing.remove(e.getEntity());
 			if (redTeam.contains(e.getEntity())) redTeam.remove(e.getEntity());
 			if (blueTeam.contains(e.getEntity())) blueTeam.remove(e.getEntity());
 			if (greenTeam.contains(e.getEntity())) greenTeam.remove(e.getEntity());
 			if (orangeTeam.contains(e.getEntity())) orangeTeam.remove(e.getEntity());
 			if (TeamChat.teamChatting.contains(e.getEntity())) TeamChat.teamChatting.remove(e.getEntity());
 			if (playing.size()>1)
 				e.setDeathMessage(ChatColor.YELLOW + e.getEntity().getName() + ChatColor.DARK_RED + " " + e.getDeathMessage().split(e.getEntity().getName() + " ")[1] + ChatColor.DARK_GREEN + " " + playing.size() + " Players Remain");
 			createGrave(e.getEntity().getLocation(), e.getEntity().getName());
 			checkStats();
 			Tags.refreshPlayer(e.getEntity());
 			addDeadPlayer(e.getEntity().getName());
 			Tabs.updateAll();
 		}
 		} catch (Exception ex) { ex.printStackTrace(); }
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onLogin(PlayerLoginEvent e)
 	{	
 		if (config.isSet("votes.players." + e.getPlayer().getName()) && config.getInt("votes.players." + e.getPlayer().getName()) >= 20) { e.getPlayer().setDisplayName(ChatColor.DARK_AQUA + e.getPlayer().getName() + ChatColor.WHITE); }
 		if (config.isSet("votes.players." + e.getPlayer().getName()) && config.getInt("votes.players." + e.getPlayer().getName()) >= 250) { e.getPlayer().setDisplayName(ChatColor.DARK_RED + e.getPlayer().getName() + ChatColor.WHITE); }
 		
 		if (config.getBoolean("priorities") == true)
 		{
 			if (config.isSet("votes.players." + e.getPlayer().getName())) { e.getPlayer().setDisplayName(ChatColor.YELLOW + "[" + config.getInt("votes.players." + e.getPlayer().getName()) + "]" + ChatColor.GRAY + e.getPlayer().getDisplayName() + ChatColor.WHITE); }
 			else e.getPlayer().setDisplayName(ChatColor.WHITE + "[0]" + e.getPlayer().getDisplayName());
 		}
 		if (e.getPlayer().hasPermission("walls.op")) e.getPlayer().setDisplayName(ChatColor.DARK_BLUE + "[" + ChatColor.DARK_GREEN + "Admin" + ChatColor.DARK_BLUE + "]" + ChatColor.DARK_RED + e.getPlayer().getName() + ChatColor.GRAY + ChatColor.WHITE);
		if (config.isSet("prefix." + e.getPlayer().getName())) e.getPlayer().setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("prefix." + e.getPlayer().getName())).replace("{pri}", config.getInt("votes.players." + e.getPlayer().getName())+"") + e.getPlayer().getName() + ChatColor.WHITE);
 		if (Bukkit.getOnlinePlayers().length == Bukkit.getMaxPlayers())
 		{
 			if (config.isSet("votes.players." + e.getPlayer().getName()) && (config.getBoolean("priorities") || config.getInt("votes.players." + e.getPlayer().getName()) > 5))
 			{
 				int pl = config.getInt("votes.players." + e.getPlayer().getName());
 				int l = 999999;
 				Player low = null;
 				for (int i = Bukkit.getOnlinePlayers().length -  1; i > -1; i--)
 				{
 					Player p = Bukkit.getOnlinePlayers()[i];
 					if (!playing.contains(p))
 					{
 						if (!config.isSet("votes.players." + p.getName()))
 						{
 							p.kickPlayer(priorityKickMessage);
 							if (!e.getPlayer().isBanned()) 
 								if ((Bukkit.hasWhitelist() && e.getPlayer().isWhitelisted()) || !Bukkit.hasWhitelist())
 									e.allow();
 							return;
 						}
 						if (config.getInt("votes.players." + p.getName()) < l)
 						{
 							low = p;
 							l = config.getInt("votes.players." + p.getName());
 						}
 					}
 				}
 				if (pl > l) { low.kickPlayer("Someone with higher priority joined!"); /*e.allow();*/ return; }
 				
 			}
 			e.disallow(Result.KICK_FULL, fullKickMessage);
 		}
 				
 		e.getPlayer().getInventory().clear();
 		e.getPlayer().getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
 		
 	}
 	@EventHandler
 	public void onJoin(PlayerJoinEvent e)
 	{
 		e.setJoinMessage(ChatColor.AQUA + "+ " + ChatColor.DARK_AQUA + e.getPlayer().getName() + ChatColor.GRAY + " is now online");
 		if (gameInProgress) {
 			spectate(e.getPlayer());
 			for (Player p : playing)
 			{
 				p.hidePlayer(e.getPlayer());
 			}
 		}
 		if (e.getPlayer().hasPermission("walls.op"))
 		{
 			UpdateChecker.checkAndSendMessage(e.getPlayer());
 		}
 		Tabs.addPlayer(e.getPlayer());
 	}
 	public void checkStats()
 	{
 		if (!gameInProgress) return;
 		
 		if (redTeam.size()==playing.size())
 		{
 			String s = "";
 			for (Player p : redTeam)
 			{
 				s += (ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY + ", ");
 			}
 			s=s.substring(0, s.length() - 4);
 			endGame("red", s);
 		}
 		else if (blueTeam.size()==playing.size())
 		{
 			String s = "";
 			for (Player p : blueTeam)
 			{
 				s += (ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY + ", ");
 			}
 			s=s.substring(0, s.length() - 4);
 			endGame("blue", s);
 		}
 		else if (greenTeam.size()==playing.size())
 		{
 			String s = "";
 			for (Player p : greenTeam)
 			{
 				s += (ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY + ", ");
 			}
 			s=s.substring(0, s.length() - 4);
 			endGame("green", s);
 		}
 		else if (orangeTeam.size()==playing.size())
 		{
 			String s = "";
 			for (Player p : orangeTeam)
 			{
 				s += (ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY + ", ");
 			}
 			s=s.substring(0, s.length() - 4);
 			endGame("orange", s);
 		}
 	}
 	public void spectate(Player p)
 	{
 		p.setAllowFlight(true);
 		p.sendMessage(ChatColor.YELLOW + "You are now spectating!");
 		p.sendMessage(ChatColor.YELLOW + "You can enable flying with /fly");
 		p.setGameMode(GameMode.ADVENTURE);
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e)
 	{
 		if (e.getPlayer().hasPermission("walls.op")) return;
 		if (!playing.contains(e.getPlayer())) e.setCancelled(true);
 		if (!gameInProgress) e.setCancelled(true);
 		if (mapNumber==1)
 		{
 			if (e.getBlock().getX()==347) e.setCancelled(true);
 			if (e.getBlock().getZ()==-793) e.setCancelled(true);
 			if (e.getBlock().getX()>408) e.setCancelled(true);
 			if (e.getBlock().getZ()<-853) e.setCancelled(true);
 			if (e.getBlock().getX()<286) e.setCancelled(true);
 			if (e.getBlock().getZ()>-731) e.setCancelled(true);
 			if (e.getBlock().getY() > 139) {e.setCancelled(true); e.getPlayer().sendMessage(ChatColor.RED + "You can't build over the height limit. This prevents getting over walls."); }
 		}
 		else
 		{
 			if (e.getBlock().getZ()==-182) e.setCancelled(true);
 			if (e.getBlock().getZ()==-164) e.setCancelled(true);
 			if (e.getBlock().getX()==-785) e.setCancelled(true);
 			if (e.getBlock().getX()==-803) e.setCancelled(true);
 			if (e.getBlock().getZ()>-103) e.setCancelled(true);
 			if (e.getBlock().getX()<-863) e.setCancelled(true);
 			if (e.getBlock().getX()>-725) e.setCancelled(true);
 			if (e.getBlock().getZ()<-243) e.setCancelled(true);
 			if (e.getBlock().getY() > 95) {e.setCancelled(true); e.getPlayer().sendMessage(ChatColor.RED + "You can't build over the heigt limit. This prevents getting over walls."); }
 		}
 		if (e.getBlock() instanceof Sign)
 		{
 			if (graves.contains((Sign) e.getBlock()))
 			{
 				e.setCancelled(true);
 				e.getPlayer().sendMessage(ChatColor.AQUA + "You can not touch this grave!");
 			}
 		}
 	}
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e)
 	{
 		if (e.getPlayer().hasPermission("walls.op")) return;
 		if (!playing.contains(e.getPlayer())) e.setCancelled(true);
 		if (!gameInProgress) e.setCancelled(true);
 		if (mapNumber==1)
 		{
 			if (e.getBlock().getX()==347) e.setCancelled(true);
 			if (e.getBlock().getZ()==-793) e.setCancelled(true);
 			if (e.getBlock().getX()>408) e.setCancelled(true);
 			if (e.getBlock().getZ()<-853) e.setCancelled(true);
 			if (e.getBlock().getX()<286) e.setCancelled(true);
 			if (e.getBlock().getZ()>-731) e.setCancelled(true);
 			if (e.getBlock().getY() > 138) {e.setCancelled(true); e.getPlayer().sendMessage(ChatColor.RED + "You can't build over the heigt limit. This prevents getting over walls."); }
 		}
 		else
 		{
 			if (e.getBlock().getZ()==-182) e.setCancelled(true);
 			if (e.getBlock().getZ()==-164) e.setCancelled(true);
 			if (e.getBlock().getX()==-785) e.setCancelled(true);
 			if (e.getBlock().getX()==-803) e.setCancelled(true);
 			if (e.getBlock().getZ()>-103) e.setCancelled(true);
 			if (e.getBlock().getX()<-863) e.setCancelled(true);
 			if (e.getBlock().getX()>-725) e.setCancelled(true);
 			if (e.getBlock().getZ()<-243) e.setCancelled(true);
 			if (e.getBlock().getY() > 94) {e.setCancelled(true); e.getPlayer().sendMessage(ChatColor.RED + "You can't build over the heigt limit. This prevents getting over walls."); }
 		}
 	}
 	/*@EventHandler
 	public void onVote(VotifierEvent e)
 	{
 		Vote v = e.getVote();
 		Player p = Bukkit.getPlayer(v.getUsername());
 		if (config.isSet("votes.players." + v.getUsername()))
 		{
 			config.set("votes.players." + v.getUsername(), config.getInt("votes.players." + v.getUsername()) + 1);
 		}
 		else config.set("votes.players." + v.getUsername(), 1);
 		if (Bukkit.getPlayer(v.getUsername()).isOnline()) Bukkit.getPlayer(v.getUsername()).sendMessage("�eThank's for voting! Your priority is now " + config.getInt("votes.players." + v.getUsername()));
 		Bukkit.broadcastMessage("�3" + v.getUsername() + " Voted For The Server On Planet Minecraft And Now Has Login Priority of " + config.getInt("votes.players." + v.getUsername()) + "! You Can Vote By Clicking This Link " + votelink + " It Is Easy, No Registration Required");
 		saveConfig();
 		p.setDisplayName(p.getName());
 		if (config.isSet("votes.players." + p.getName()) && config.getInt("votes.players." + p.getName()) >= 20) { p.setDisplayName("�3" + p.getName() + "�f"); }
 		if (config.isSet("votes.players." + p.getName()) && config.getInt("votes.players." + p.getName()) >= 250) { p.setDisplayName("�4" + p.getName() + "�f"); }
 		
 		if (config.getBoolean("priorities") == true)
 		{
 			if (config.isSet("votes.players." + p.getName())) { p.setDisplayName("�e[" + config.getInt("votes.players." + p.getName()) + "]�7" + p.getDisplayName() + "�f"); }
 			else p.setDisplayName("�f[0]" +p.getDisplayName());
 		}
 	}*/
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent e)
 	{
 		if (gameInProgress) spectate(e.getPlayer());
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onChat(AsyncPlayerChatEvent e)
 	{
 		setLastEventToNow(e.getPlayer());
 		if (WallDropper.timeContinued < 0 && WallDropper.timeContinued >= -30 && (e.getMessage().toLowerCase().contains(" lag") || e.getMessage().toLowerCase().startsWith("lag")))
 		{
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Please do not send messages about lag while the walls are falling ;)");
 		}
 		else if (voting)
 		{
 			if (e.getMessage().trim().length() == 1)
 			{
 				if (e.getMessage().trim().equals("1"))
 				{
 					if (votedFor1.contains(e.getPlayer())) { e.getPlayer().sendMessage(ChatColor.GRAY + "You have already voted for that map!"); e.setCancelled(true); return; }
 					if (votedFor2.contains(e.getPlayer())) { e.getPlayer().sendMessage(ChatColor.GRAY + "Your vote for map 2 has been deleted!"); votedFor2.remove(e.getPlayer()); }
 					votedFor1.add(e.getPlayer());
 					e.getPlayer().sendMessage(ChatColor.GRAY + "You have successfully voted for map 1!");
 					e.setCancelled(true);
 				}
 				else if (e.getMessage().trim().equals("2"))
 				{
 					if (votedFor2.contains(e.getPlayer())) { e.getPlayer().sendMessage(ChatColor.GRAY + "You have already voted for that map!"); e.setCancelled(true); return; }
 					if (votedFor1.contains(e.getPlayer())) { e.getPlayer().sendMessage(ChatColor.GRAY + "Your vote for map 1 has been deleted!"); votedFor1.remove(e.getPlayer()); }
 					votedFor2.add(e.getPlayer());
 					e.getPlayer().sendMessage(ChatColor.GRAY + "You have successfully voted for map 2!");
 					e.setCancelled(true);
 				}
 				else e.getPlayer().sendMessage(ChatColor.GRAY + "Invalid input, type a 1 or a 2.");
 			}
 			else
 			{
 				//e.getPlayer().sendMessage("�7There is a vote in progress, type a 1 or a 2.");
 			}
 			//e.setCancelled(true);
 		}
 		//if (gameOver || voting) e.setCancelled(true);
 		
 		if (!e.isCancelled())
 		{
 			e.setMessage(e.getPlayer().getDisplayName() + ": " + e.getMessage());
 			e.setCancelled(TeamChat.say(e.getPlayer(), e.getMessage()));
 		}
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onDamage(EntityDamageByEntityEvent e)
 	{
 		if (e.isCancelled()) return;		
 		
 		//no spectators hitting animals
 		if (!(e.getEntity() instanceof Player)) { if (e.getDamager() instanceof Player) { if (!playing.contains((Player) e.getDamager())) e.setCancelled(true); return; } } 
 		
 		//no arrows shot at spectators
 		if (e.getEntity() instanceof Player) { if (!playing.contains((Player) e.getEntity()) && e.getDamager().getType().equals(EntityType.ARROW)) { e.setCancelled(true); return; } }
 		
 		if (e.getDamager().getType().equals(EntityType.ARROW) && e.getEntity() instanceof Player)
 		{
 			if (playing.contains((Player) e.getEntity()))
 			{
 				Arrow arrow = (Arrow) e.getDamager();
 				if (arrow.getShooter() instanceof Player)
 				{
 					Player d = (Player) arrow.getShooter();
 					if (redTeam.contains((Player)e.getEntity()) && redTeam.contains(d)) { d.sendMessage(ChatColor.RED + "You can not team kill!"); e.setCancelled(true); return; }
 					if (blueTeam.contains((Player)e.getEntity()) && blueTeam.contains(d)) { d.sendMessage(ChatColor.RED + "You can not team kill!"); e.setCancelled(true); return; }
 					if (greenTeam.contains((Player)e.getEntity()) && greenTeam.contains(d)) { d.sendMessage(ChatColor.RED + "You can not team kill!"); e.setCancelled(true); return; }
 					if (orangeTeam.contains((Player)e.getEntity()) && orangeTeam.contains(d)) { d.sendMessage(ChatColor.RED + "You can not team kill!"); e.setCancelled(true); return; }
 				}
 			}
 		}
 		if (!(e.getDamager() instanceof Player)) return;
 		if (!(e.getEntity() instanceof Player)) return;
 		
 		Player p = (Player) e.getEntity();
 		Player damager = (Player) e.getDamager();
 		
 		setLastEventToNow(p);
 		
 		if (!playing.contains(p) && playing.contains(damager)) { damager.sendMessage(ChatColor.RED + "There is a spectator there, don't hurt it"); e.setCancelled(true); return; } 
 		if (!playing.contains(damager) && playing.contains(p)) { e.setCancelled(true); damager.sendMessage(ChatColor.RED + "You Are Not In This Fight!"); return; }
 		
 		if (!playing.contains(p) && !playing.contains(damager))
 		{
 			/*if (p.getLocation().getBlockX() <= 357 && p.getLocation().getBlockX() >= 337 && p.getLocation().getBlockZ() >= -804 && p.getLocation().getBlockZ() <= -782 && p.getLocation().getBlockY() >= 152 && p.getLocation().getBlockY() <= 155)
 			{
 				if (damager.getLocation().getBlockX() <= 357 && damager.getLocation().getBlockX() >= 337 && damager.getLocation().getBlockZ() >= -804 && damager.getLocation().getBlockZ() <= -782 && damager.getLocation().getBlockY() >= 152 && damager.getLocation().getBlockY() <= 155)
 				{
 					return;
 				}
 			}*/
 			e.setCancelled(true); //damager.sendMessage("�cIf you want to fight do it in the area above spawn");
 		}
 		
 		if (redTeam.contains(p) && redTeam.contains(damager)) { e.setCancelled(true); damager.sendMessage(ChatColor.RED + "You Can Not Team Kill!"); return; }
 		if (blueTeam.contains(p) && blueTeam.contains(damager)) { e.setCancelled(true); damager.sendMessage(ChatColor.RED + "You Can Not Team Kill!"); return; }
 		if (greenTeam.contains(p) && greenTeam.contains(damager)) { e.setCancelled(true); damager.sendMessage(ChatColor.RED + "You Can Not Team Kill!"); return; }
 		if (orangeTeam.contains(p) && orangeTeam.contains(damager)) { e.setCancelled(true); damager.sendMessage(ChatColor.RED + "You Can Not Team Kill!"); return; }
 		if (WallDropper.time > 0 && playing.contains(p) && playing.contains(damager)) { damager.sendMessage(ChatColor.RED + "The walls haven't dropped yet! Why are you hitting " + p.getName() + "?"); e.setCancelled(true); return; }
 	}
 	@EventHandler
 	public void onDroppedItem(PlayerDropItemEvent e)
 	{
 		setLastEventToNow(e.getPlayer());
 		if (!playing.contains(e.getPlayer()) && !e.getPlayer().hasPermission("walls.op")) e.setCancelled(true);
 	}
 	@EventHandler 
 	public void onPickUp(PlayerPickupItemEvent e)
 	{
 		if (!playing.contains(e.getPlayer())) e.setCancelled(true);
 	}
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onInteract(PlayerInteractEvent e)
 	{
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
 		{
 			if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST)
 			{
 				Sign s = (Sign) e.getClickedBlock().getState();
 				SignUI.onClick(e.getPlayer(), s.getLine(0), s.getLine(1), s.getLine(2), s.getLine(3));
 			}
 		}
 		if (playing.contains(e.getPlayer()) && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
 		{
 			if (e.getPlayer().getItemInHand() != null)
 			{
 				if (e.getPlayer().getItemInHand().getType() == Material.NETHER_STAR)
 				{
 					if (ColorCycler.colorTime.containsKey(e.getPlayer()))
 					{
 						if (ColorCycler.colorTime.get(e.getPlayer()) == 0)
 						{
 							e.getPlayer().sendMessage(ChatColor.RED + "Your ability to do that has worn off!");
 						}
 						else
 						{
 							ColorCycler.cycle(e.getPlayer());
 						}
 					}
 					else
 					{
 						ColorCycler.cycle(e.getPlayer());
 					}
 				}
 				else if (e.getPlayer().getItemInHand().getType() == Material.SNOW_BALL)
 				{
 					if (e.getPlayer().getItemInHand().getItemMeta().hasDisplayName())
 					{
 						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Basic"))
 						{
 							e.getPlayer().setMetadata("last-grenade", new FixedMetadataValue(this, "basic"));
 							System.out.println("Meep");
 						}
 					}
 					else
 					{
 						if (e.getPlayer().hasMetadata("last-grenade")) e.getPlayer().removeMetadata("last-grenade", this);
 					}
 				}
 				else if (e.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL && WallDropper.time > 0)
 				{
 					e.getPlayer().sendMessage(ChatColor.RED + "You can not do that until the walls fall!");
 					e.setCancelled(true);
 				}
 			}
 		}
 		if (e.getPlayer().hasPermission("walls.op")) { e.setCancelled(false); return; }
 		if ((e.getPlayer().getLocation().getBlockY() > 139 && mapNumber == 1) || (e.getPlayer().getLocation().getBlockY() > 125 && mapNumber == 2))
 		{
 			e.setCancelled(false);
 			return;
 		}
 		else
 		{
 			if (playing.contains(e.getPlayer()))
 			{
 				setLastEventToNow(e.getPlayer());
 				if ((e.getPlayer().getItemInHand() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) || e.getAction() == Action.LEFT_CLICK_BLOCK)
 				{
 					if (e.getPlayer().getItemInHand().getType() != Material.AIR)
 					{
 						for (Player p : Bukkit.getOnlinePlayers())
 						{
 							if (!playing.contains(p))
 							{
 								if (p.getLocation().distance(e.getClickedBlock().getLocation()) <= 2)
 								{
 									p.teleport(p.getLocation().add(new Location(p.getWorld(), 0, 2, 0)));
 									p.sendMessage(ChatColor.YELLOW + "You have been moved over to allow " + e.getPlayer().getName() + " to place a block");
 								}
 							}
 						}
 					}
 					if (e.getPlayer().getItemInHand().getType() != Material.FLINT_AND_STEEL && e.getPlayer().getItemInHand().getType() == Material.FIREBALL && WallDropper.time > 0 && preventFireBeforeWallsFall)
 					{
 						e.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't place fire until the walls have fallen!");
 						e.setCancelled(true);
 					}
 				}
 			}
 		}
 		if (!gameInProgress) e.setCancelled(true);
 		if (!playing.contains(e.getPlayer())) {e.setCancelled(true);}
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onEntitySpawn(CreatureSpawnEvent e)
 	{
 		if (e.getEntity().getType().equals(EntityType.CREEPER) || e.getEntity().getType().equals(EntityType.ENDERMAN) || e.getEntity().getType().equals(EntityType.SLIME) || e.getEntity().getType().equals(EntityType.SKELETON) || e.getEntity().getType().equals(EntityType.SPIDER) || e.getEntity().getType().equals(EntityType.ZOMBIE)) e.setCancelled(true);
 	}
 	@EventHandler
 	public void onPing(ServerListPingEvent e)
 	{
 		String message = "AutoWalls Server";
 		if (!gameInProgress && !gameOver)
 		{
 			message=(ChatColor.DARK_GREEN + "Getting ready to start!");
 		}
 		else if (gameInProgress && WallDropper.time > 0)
 		{
 			int mins = WallDropper.time / 60;
 			int secs = WallDropper.time % 60;
 			message=(ChatColor.DARK_GREEN + "Walls drop in "+ ChatColor.YELLOW + mins + ChatColor.DARK_RED + " mins, " + ChatColor.YELLOW + secs + ChatColor.DARK_RED + " secs!");
 		}
 		else if (gameInProgress)
 		{
 			message=(ChatColor.YELLOW + "" + playing.size() + ChatColor.DARK_RED + " players alive!");
 		}
 		else if (gameOver && !voting)
 		{
 			message=ChatColor.DARK_GREEN + "Game has ended!";
 		}
 		else {
 			message=ChatColor.DARK_AQUA + "Voting for the next map!";
 		}
 		e.setMotd(message);
 	}
 	@EventHandler
 	public void onWeather(WeatherChangeEvent e)
 	{
 		e.setCancelled(true);
 	}
 	@EventHandler
 	public void onSneak(PlayerToggleSneakEvent e)
 	{
 		if (playing.contains(e.getPlayer()) && WallDropper.time<=0 && blockSneaking)
 			if (e.isSneaking()==true) e.setCancelled(true);
 	}
 	@EventHandler
 	public void onEat(EntityRegainHealthEvent e)
 	{
 		if (e.getEntity() instanceof Player)
 		{
 			if (playing.contains((Player) e.getEntity()) && disableHealing && WallDropper.time<=0) { 
 				Random r = new Random();
 				e.setAmount(r.nextInt( (20 - ((Player)e.getEntity()).getHealth()) / 2 )); 
 			} 
 		}
 	}
 	@EventHandler
 	public void onProjectileLand(ProjectileHitEvent e)
 	{
 		if (e.getEntityType() == EntityType.ARROW && arrowLightning)
 		{
 			if (e.getEntity().getShooter() != null)
 			{
 				if (e.getEntity().getShooter() instanceof Player)
 				{
 					Player shooter = (Player) e.getEntity().getShooter();
 					if (WallDropper.time <= 0)
 					{
 						Random r = new Random();
 						int rand = r.nextInt(arrowLightningChance);
 						if (rand==0)
 						{
 							Bukkit.broadcastMessage(ChatColor.DARK_RED + shooter.getName() + ChatColor.RED + " Has Shot A Rare Lightning Arrow!");
 							e.getEntity().getWorld().strikeLightning(e.getEntity().getLocation());
 						}
 					}
 				}
 			}
 		}
 		else if (e.getEntity().getType() == EntityType.SNOWBALL)
 		{
 			if (e.getEntity().hasMetadata("grenade-type"))
 			{
 				Grenades.handleLanding(e, e.getEntity());
 			}
 			//e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), .8F, true);
 		}
 	}
 	public void createGrave(Location l, String playername)
 	{
 		Random r = new Random();
 		l.getBlock().setType(Material.SIGN_POST);
 		l.getBlock().setData((byte) r.nextInt(16));
 		Sign s = (Sign) l.getBlock().getState();
 		s.setLine(0, "R.I.P.");
 		s.setLine(1, playername);
 		int i = r.nextInt(graveMessages.size());
 		s.setLine(3, graveMessages.get(i));
 		s.update();
 		graves.add(s);
 	}
 	@EventHandler
 	public void onPistonRetract (BlockPistonRetractEvent e)
 	{
 		if (e.getRetractLocation().getBlock().getType() == Material.SAND || e.getRetractLocation().getBlock().getType() == Material.GRAVEL) e.setCancelled(true);
 	}
 	@EventHandler
 	public void onPistonExtend (BlockPistonExtendEvent e)
 	{
 		for (Block b : e.getBlocks())
 		{
 			if (b.getType()==Material.SAND || b.getType()==Material.GRAVEL) e.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onExplode (EntityExplodeEvent e)
 	{
 		List<Block> newList = new ArrayList<Block>();
 		newList.addAll(e.blockList());
 		
 		for (Block b : newList)
 		{
 			if (b.getType() == Material.SAND || b.getType() == Material.GRAVEL) { e.blockList().remove(b); }
 		}
 	}
 	@EventHandler
 	public void onTp (PlayerTeleportEvent e)
 	{
 		for (Player p : Bukkit.getOnlinePlayers())
 		{
 			for (Player p2 : playing)
 			{
 				if (p!=p2 && !playing.contains(p))
 				{
 					p2.hidePlayer(p);
 				}
 				else if (p!=p2 && playing.contains(p))
 				{
 					p2.showPlayer(p);
 				}
 			}
 		}
 	}
 	public static void setLastEvent(Player p, long millis)
 	{
 		if (lastEvent.containsKey(p)) lastEvent.remove(p);
 		lastEvent.put(p, millis);
 	}
 	public static void setLastEventToNow(Player p)
 	{
 		if (lastEvent.containsKey(p)) lastEvent.remove(p);
 		lastEvent.put(p, System.currentTimeMillis());
 	}
 	public static long getLastEvent(Player p)
 	{
 		if (lastEvent.containsKey(p)) return lastEvent.get(p);
 		return 0;
 	}
 	public static int getTicksFromLastEvent(Player p)
 	{
 		if (lastEvent.containsKey(p)) return (int)Math.floor((double)((System.currentTimeMillis() - lastEvent.get(p)) / 50));
 		return Integer.MAX_VALUE;
 	}
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent e)
 	{
 		setLastEventToNow(e.getPlayer());
 	}
 	@EventHandler
 	public void onSignUpdate(SignChangeEvent e)
 	{
 		if (ChatColor.stripColor(e.getLine(0).trim()).equalsIgnoreCase("[Join]") && !e.getPlayer().hasPermission("walls.op"))
 		{
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "No placing special signs!");
 		}
 		if (ChatColor.stripColor(e.getLine(0).trim()).equalsIgnoreCase("[Kit]") && !e.getPlayer().hasPermission("walls.op"))
 		{
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "No placing special signs!");
 		}
 	}
 	@EventHandler
 	public void onProjLaunch(ProjectileLaunchEvent e)
 	{
 		if (e.getEntity().getShooter().hasMetadata("last-grenade"))
 		{
 			e.getEntity().setMetadata("grenade-type", new FixedMetadataValue(this, e.getEntity().getShooter().getMetadata("last-grenade").get(0).asString()));
 		}
 	}
 	public static void addDeadPlayer(String name)
 	{
 		if (!dead.contains(name)) dead.add(name);
 		Tabs.updateAll();
 	}
 	public static void removeDeadPlayer(String name)
 	{
 		if (dead.contains(name)) dead.remove(name);
 		Tabs.updateAll();
 	}
 }
