 package com.bradsproject.BradleyJewell.bZone;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import java.io.FileInputStream;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 
 /**
  * bSwarm for Bukkit
  * 
  * @author BradleyJewell
  */
 
 /*
  * TODO: pvp, number of players allowed in zone
  */
 
 public class bZone extends JavaPlugin
 {
 	final HashMap<String, String> defaults = new HashMap<String, String>();
 	final List<Zone> zones = new ArrayList<Zone>();
 	final List<Wilderness> wilds = new ArrayList<Wilderness>();
 	final List<String> whitelist = new ArrayList<String>();
 	final Map<String, bZonePlayer> players = new HashMap<String, bZonePlayer>();
 	
 	final Map<String, Integer> itemValues = new HashMap<String, Integer>();
 	final Map<String, Integer> itemCosts = new HashMap<String, Integer>();
 	
 	private final bZoneEntityListener entityListener = new bZoneEntityListener(this);
 	private final bZonePlayerListener playerListener = new bZonePlayerListener(this);
 	private final bZoneBlockListener blockListener = new bZoneBlockListener(this);
 	Server server;
 	Yaml yaml;
 	
 	public void onEnable()
 	{
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Low, this);
 		//Event.Type.BLOCK_PHYSICS
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Low, this);
 		
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.High, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Low, this);
 		
 		parseConfig();
 		
 		new bZoneHealthTask(this);
 		
 		// EXAMPLE: Custom code, here we just output some info so we can check
 		// all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion()
 				+ " is enabled!");
 	}
 	
 	public void onDisable()
 	{
 		// NOTE: All registered events are automatically unregistered when a
 		// plugin is disabled
 		
 		// EXAMPLE: Custom code, here we just output some info so we can check
 		// all is well
 		System.out.println("bZone has been disabled!");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void parseConfig()
 	{
 		yaml = new Yaml(new SafeConstructor());
 		InputStream input;
 		BufferedReader buf;
 		try
 		{
 			input = new FileInputStream(new File("plugins/bZone/zones.yml"));
 			FileReader white = new FileReader("white-list.txt");
 			buf = new BufferedReader(white);
 		} catch (FileNotFoundException e)
 		{
 			System.out.println("bZone could not find zones.yml file, disabling...");
 			PluginManager pm = getServer().getPluginManager();
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		String line;
 		try
 		{
 			line = buf.readLine();
 			while(line != null)
 			{
 				whitelist.add(line);
 				System.out.println("Whitelist: " + line);
 				line = buf.readLine();
 			}
 		} catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Map<String,Object> map = (Map<String,Object>) yaml.load(input);
 		Map<String, Object> swarmsNode = (Map<String, Object>) map.get("zones");
 		for(String groupKey : swarmsNode.keySet())
 		{
 			String name = groupKey;
 			Map<String, Object> value = (Map<String, Object>) swarmsNode.get(groupKey);
 			String w = value.get("world").toString();
 			World world = getServer().getWorld(w);
 			
 			if(name.equals("wilderness"))
 			{
 				Wilderness wild = new Wilderness(world);
 				wild.hurting = Boolean.parseBoolean(value.get("hurting").toString());
 				
 				List<String> playersNode = (List<String>) value.get("players");
 				for(String player : playersNode)
 				{
 					wild.players.add(player);
 				}
 				
 				List<String> monstersNode = (List<String>) value.get("creatures");
 				for(String monster : monstersNode)
 				{
 					wild.creatures.add(monster);
 				}
 				
 				Map<String,Object> protectionNode = (Map<String,Object>) value.get("protection");
 				for(String protect : protectionNode.keySet())
 				{
 					boolean isProtected = Boolean.parseBoolean(protectionNode.get(protect).toString());
 					wild.protection.put(protect, isProtected);
 				}
 				
 				wilds.add(wild);
 				System.out.println("bZone loaded wilderness for: " + wild.world.getName());
 			}
 			else
 			{
 				String[] min = value.get("min").toString().split(",");
 				String[] max = value.get("max").toString().split(",");
 				
 				Zone zone = new Zone(
 						name,
 						world,
 						Double.parseDouble(min[0]),
 						Double.parseDouble(min[1]),
 						Double.parseDouble(min[2]),
 						Double.parseDouble(max[0]),
 						Double.parseDouble(max[1]),
 						Double.parseDouble(max[2])
 				);
 				
 				zone.healing = Boolean.parseBoolean(value.get("healing").toString());
 				
 				List<String> playersNode = (List<String>) value.get("players");
 				for(String player : playersNode)
 				{
 					zone.players.add(player);
 				}
 				
 				List<String> monstersNode = (List<String>) value.get("creatures");
 				for(String monster : monstersNode)
 				{
 					zone.creatures.add(monster);
 				}
 				
 				Map<String,Object> protectionNode = (Map<String,Object>) value.get("protection");
 				for(String protect : protectionNode.keySet())
 				{
 					boolean isProtected = Boolean.parseBoolean(protectionNode.get(protect).toString());
 					zone.protection.put(protect, isProtected);
 				}
 				
 				zones.add(zone);
 				System.out.println("bZone loaded zone: " + zone.name);
 			}
 		}
 	}
 	
 	public Zone getZone(Location location)
 	{
 		for(Zone zone : zones)
 		{
 			if(zone.contains(location))
 			{
 				return zone;
 			}
 		}
 		return null;
 	}
 	
 	public Wilderness getWilderness(Location location)
 	{
 		for(Wilderness wild : wilds)
 		{
 			if(wild.world.getName().equals(location.getWorld().getName()))
 			{
 				return wild;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if(!(sender instanceof Player))
 			return false;
 		
 		Player player = (Player) sender;
 		String commandName = cmd.getName().toLowerCase();
 		
 		if(commandName.equals("zone"))
 		{
 			Location location = player.getLocation();
 			Zone zone = getZone(location);
 			if(zone != null)
 			{
 				player.sendMessage("You are in the zone: " + zone.name);
 				return true;
 			}
 			else
 			{
 				player.sendMessage("You are in the wilderness.");
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean cancelAction(Location location, Player player, String type)
 	{
 		try {
 			Zone zone = getZone(location);
 			if(zone == null)
 			{
 				Wilderness wild = getWilderness(location);
				if(!wild.hasPlayer(player.getName()) && wild.protection.get(type) == true)
 				{
 					player.sendMessage("The wilderness has "+type+" protection.");
 					return true;
 				}
 			}
 			else if(!zone.hasPlayer(player.getName()) && (zone.protection.get(type) == true))
 			{
 				player.sendMessage("Zone "+zone.name+" has "+type+" protection.");
 				return true;
 			}
 		} catch(NullPointerException e)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 	public boolean cancelAction(Location location, String type)
 	{
 		try {
 			Zone zone = getZone(location);
 			if(zone == null)
 			{
 				Wilderness wild = getWilderness(location);
 				if(wild.protection.get(type) == true)
 				{
 					return true;
 				}
 			}
 			else if((zone.protection.get(type) == true))
 			{
 				return true;
 			}
 		} catch(NullPointerException e)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 }
