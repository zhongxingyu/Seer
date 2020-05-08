 package com.bradsproject.BradleyJewell.bSwarm;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import java.io.FileInputStream;
 
 import net.minecraft.server.Entity;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * bSwarm for Bukkit
  * 
  * @author BradleyJewell
  */
 public class bSwarm extends JavaPlugin
 {
 	final HashMap<String, String> defaults = new HashMap<String, String>();
 	final List<bSwarmTask> tasks = new ArrayList<bSwarmTask>();
 	final List<bSwarmTask> swarms = new ArrayList<bSwarmTask>();
 	Server server;
 	Yaml yaml;
 	
 	public static PermissionHandler Permissions = null;
 	
 	public void onEnable()
 	{
 		// Register our events
 		// PluginManager pm = getServer().getPluginManager();
 		
 		try
 		{
 			parseConfig();
 		} catch (FileNotFoundException e)
 		{
 			System.out.println("bSwarm configuration file not found!");
 		}
 		
 		setupPermissions();
 		
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
 		System.out.println("bSwarm has been disabled!");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void parseConfig() throws FileNotFoundException
 	{
 		defaults.put("type", "zombie");
 		defaults.put("num", "10");
 		defaults.put("delay", "20");
 		
 		yaml = new Yaml(new SafeConstructor());
 		InputStream input = new FileInputStream(new File("plugins/bSwarm/swarms.yml"));
 		Map<String, Object> map = (Map<String, Object>) yaml.load(input);
 		Map<String, Object> swarmsNode = (Map<String, Object>) map.get("swarms");
 		boolean auto;
 		for(String groupKey: swarmsNode.keySet())
 		{
 			auto = false;
 			bSwarmTask swarm = new bSwarmTask(this);
 			swarm.name = groupKey;
 			Map<String, Object> value = (Map<String, Object>) swarmsNode.get(groupKey);
 			
 			if(Boolean.parseBoolean(value.get("automated").toString()))
 			{
 				auto = true;
 				swarm.automated = true;
 				if(value.containsKey("timeofday"))
 				{
 					swarm.timeofday = Integer.parseInt(value.get("timeofday").toString());
 				} else if(value.containsKey("minutesinterval"))
 				{
 					swarm.minutesinterval = Integer.parseInt(value.get("minutesinterval")
 							.toString());
 				}
 			}
 			
 			if(value.containsKey("location"))
 			{
 				String w = value.get("world").toString();
 				World world = getServer().getWorld(w);
 				String[] loc = value.get("location").toString().split(",");
 				Location location = new Location(world, Double.parseDouble(loc[0]), Double
 						.parseDouble(loc[1]), Double.parseDouble(loc[2]));
 				if(world != null && loc != null)
 				{
 					swarm.location = location;
 				} else
 				{
 					System.out.println("bSwarm could not find world: "
 							+ value.get("world").toString());
 					continue;
 				}
 			}
 			
 			swarm.radius = Integer.parseInt(value.get("radius").toString());
 			swarm.ticksdelay = Integer.parseInt(value.get("ticksdelay").toString());
 			swarm.radius = Integer.parseInt(value.get("radius").toString());
 			
 			Map<String, Object> monstersNode = (Map<String, Object>) value.get("monsters");
 			for(String monster: monstersNode.keySet())
 			{
 				int num = Integer.parseInt(monstersNode.get(monster).toString());
 				swarm.monsters.put(monster, num);
 			}
 			
 			if(auto)
 			{
 				tasks.add(swarm);
 				System.out.println("Starting config task...");
 				swarm.id = getServer().getScheduler().scheduleSyncDelayedTask(this, swarm, 0);
 			}
 			swarms.add(swarm);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
	public CraftEntity spawn(Location location, String type) throws Exception
 	{
 		try
 		{
			CraftWorld cworld = (CraftWorld) location.getWorld();
 			WorldServer world = (cworld).getHandle();
 			Constructor<CraftEntity> craft = (Constructor<CraftEntity>) ClassLoader
 					.getSystemClassLoader().loadClass(
 							"org.bukkit.craftbukkit.entity.Craft" + type).getConstructors()[0];
 			Constructor<Entity> entity = (Constructor<Entity>) ClassLoader
 					.getSystemClassLoader().loadClass("net.minecraft.server.Entity" + type)
 					.getConstructors()[0];
 			return craft.newInstance((CraftServer) server, entity.newInstance(world));
 		} catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 	
 	public void swarm(Player player)
 	{
 		String type = defaults.get("type").substring(0, 1).toUpperCase()
 				+ defaults.get("type").substring(1);
 		swarm(player, type);
 	}
 	
 	public void swarm(Player player, String type)
 	{
 		int num = Integer.parseInt(defaults.get("num"));
 		swarm(player, type, num);
 	}
 	
 	public void swarm(Player player, String type, int num)
 	{
 		long delay = Integer.parseInt(defaults.get("delay"));
 		swarm(player, type, num, delay);
 	}
 	
 	public void swarm(Player player, String type, int num, long delay)
 	{
		System.out.println("(" + player + "," + type + "," + num + "," + delay + ")!!");
 		bSwarmTask task = new bSwarmTask(this, player);
 		task.type = type;
 		task.num = num;
 		
 		if(delay <= 0)
 			delay = 1;
 		
 		task.id = getServer().getScheduler().scheduleSyncRepeatingTask(this, task, delay,
 				delay);
 		tasks.add(task);
 	}
 	
 	public void setupPermissions()
 	{
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 		
 		if(bSwarm.Permissions == null)
 		{
 			if(test != null)
 			{
 				bSwarm.Permissions = ((Permissions) test).getHandler();
 			} else
 			{
 				
 			}
 		}
 	}
 	
 	public boolean isMonsterName(String word)
 	{
 		if(word.equalsIgnoreCase("creeper") || word.equalsIgnoreCase("skeleton")
 				|| word.equalsIgnoreCase("spider") || word.equalsIgnoreCase("zombie")
 				|| word.equalsIgnoreCase("ghast") || word.equalsIgnoreCase("giant")
 				|| word.equalsIgnoreCase("pigzombie") || word.equalsIgnoreCase("slime"))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public void nuke(World world)
 	{
 		List<LivingEntity> mobs = world.getLivingEntities();
 		for(LivingEntity m: mobs)
 		{
 			if(!(m instanceof Player) && !(m instanceof Animals))
 			{
 				m.remove();
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
 			String[] args)
 	{
 		if(!(sender instanceof Player))
 			return false;
 		
 		Player player = (Player) sender;
 		String commandName = cmd.getName().toLowerCase();
 		
 		if(commandName.equals("bswarm"))
 		{
 			if(!(bSwarm.Permissions == null || bSwarm.Permissions
 					.has(player, "bswarm.bswarm")))
 			{
 				player.sendMessage("You do not have permission to use that command.");
 				return false;
 			}
 			try
 			{
 				if(args.length > 0)
 				{
 					handleSwarmArguments(args, player, null);
 					return true;
 				} else
 				{
 					player.sendMessage("Sending your swarm...");
 					swarm(player);
 					return true;
 				}
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		} else if(commandName.equals("bswarmnuke") || commandName.equals("bnuke"))
 		{
 			if(!(bSwarm.Permissions == null || bSwarm.Permissions.has(player, "bswarm.nuke")))
 			{
 				player.sendMessage("You do not have permission to use that command.");
 				return false;
 			}
 			nuke(player.getWorld());
 			return true;
 		}
 		return false;
 	}
 	
 	public void handleSwarmArguments(String[] args, Player sender, Player victim)
 	{
 		String type = args[0].toLowerCase();
 		type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
 		
 		Player p;
 		
 		if(type.equalsIgnoreCase("cancel"))
 		{
 			cancelTasks(sender);
 			return;
 		} else if(!isMonsterName(type))
 		{
 			List<Player> players = getServer().matchPlayer(type);
 			if(players.size() == 0)
 			{
 				for(bSwarmTask swarm: swarms)
 				{
 					if(swarm.name.equalsIgnoreCase(type))
 					{
 						tasks.add(swarm);
 						swarm.curMonsters = new HashMap<String, Integer>(swarm.monsters);
 						swarm.player = sender;
 						swarm.id = getServer().getScheduler().scheduleSyncDelayedTask(this,
 								swarm, 0);
 						return;
 					}
 				}
 				sender.sendMessage("Invalid bSwarm command (invalid player, monster, or swarm name)");
 				return;
 			}
 			p = players.get(0);
 			try
 			{
 				if(p.getEntityId() > 0)
 				{
 					if((bSwarm.Permissions != null)
 							&& !bSwarm.Permissions.has(sender, "bswarm.bswarm.player")
 							&& victim == null)
 					{
 						sender.sendMessage("You do not have permission to use that command.");
 						return;
 					}
 					
 					sender.sendMessage("Sending your swarm...");
 					
 					String[] s = new String[args.length - 1];
 					int count = 0;
 					for(String sp: args)
 					{
 						if(!type.equalsIgnoreCase(sp))
 						{
 							s[count] = sp;
 							count++;
 						}
 					}
 					
 					if(s.length > 1)
 					{
 						handleSwarmArguments(s, p, victim);
 						return;
 					} else
 					{
 						swarm(p);
 						return;
 					}
 				} else
 				{
 					sender
 							.sendMessage("Invalid bSwarm command (invalid player or monster name)");
 					return;
 				}
 			} catch (NullPointerException e)
 			{
 				sender.sendMessage("Invalid bSwarm command (invalid player or monster name)");
 				return;
 			}
 		} else if(bSwarm.Permissions != null
 				&& !bSwarm.Permissions.has(victim, "bswarm.bswarm." + type.toLowerCase()))
 		{
 			sender.sendMessage("You do not have permission to use that command.");
 			return;
 		}
 		
 		if(victim != null)
 			p = victim;
 		else
 			p = sender;
 		
 		if(args.length > 1)
 		{
 			int num = Integer.parseInt(args[1]);
 			
 			if(bSwarm.Permissions != null && !bSwarm.Permissions.has(p, "bswarm.bswarm.num"))
 			{
 				p.sendMessage("You do not have permission to use that command.");
 				return;
 			}
 			
 			if(args.length > 2)
 			{
 				long delay = Integer.parseInt(args[2]);
 				
 				if(bSwarm.Permissions != null
 						&& !bSwarm.Permissions.has(p, "bswarm.bswarm.delay"))
 				{
 					p.sendMessage("You do not have permission to use that command.");
 					return;
 				}
 				
 				swarm(p, type, num, delay);
 			} else
 			{
 				swarm(p, type, num);
 			}
 		} else
 		{
 			swarm(p, type);
 		}
 	}
 	
 	public void cancelTasks(Player player)
 	{
 		int count = 0;
 		final List<bSwarmTask> remove = new ArrayList<bSwarmTask>();
 		
 		for(bSwarmTask task: tasks)
 		{
 			if(task.player == player)
 			{
 				getServer().getScheduler().cancelTask(task.id);
 				remove.add(task);
 				count++;
 			}
 		}
 		
 		for(bSwarmTask r: remove)
 		{
 			tasks.remove(r);
 		}
 		
 		if(count > 0)
 			player.sendMessage(count + " swarm(s) cancelled");
 		else
 			player.sendMessage("You have no active swarms");
 	}
 	
 }
