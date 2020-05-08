 package joren.spawn;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Spawn
  * @version 0.1
  * @author jorencombs
  * 
  * Originally intended to be a stripped-down version of the SpawnMob Bukkit plugin (most recently adapted
  * by jordanneil23).  However, it has been nuked and rewritten enough that there probably isn't much of
  * the original code left.  Also uses TargetBlock code from toi and Raphfrk
  */
 public class Spawn extends JavaPlugin {
 	public static java.util.logging.Logger log = java.util.logging.Logger.getLogger("Minecraft");
 	/** Handle to access the Permissions plugin */
 	public static PermissionHandler Permissions;
 	/** Name of the plugin, used in output messages */
 	protected static String name = "Spawn";
 	/** Path where the plugin's saved information is located */
 	protected static String path = "plugins" + File.separator + name;
 	/** Location of the config YML file */
 	protected static String config = path + File.separator + name + ".yml";
 	/** Header used for console and player output messages */
 	protected static String header = "[" + name + "] ";
 	/** Represents the plugin's YML configuration */
 	protected static List<String> neverSpawn = new ArrayList<String>();
 	protected static List<String> neverKill = new ArrayList<String>();
 	protected static Configuration cfg;
 	/** True if this plugin is to be used with Permissions, false if not */
 	protected boolean permissions = false;
 	/** Limitations on how many entities can be spawned and what the maximum size of a spawned entity should be */
 	protected int spawnLimit, sizeLimit;
 	protected double hSpeedLimit;
 
 	/**
 	 * Initializes plugin description variables (in case they are different from when written)
 	 * and initiates a reload of the plugin
 	 */
 	public void onEnable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		name = pdfFile.getName();
 		header = "[" + name + "] ";
 		path = "plugins" + File.separator + name;
 		config = path + File.separator + name + ".yml";
 		reload();
 		info("Version " + pdfFile.getVersion() + " enabled.");
 	}
 	
 	/**
 	 * Saves plugin configuration to disk so that the plugin can be safely disabled.
 	 */
 	public void onDisable() {
 		save();
 		log.info("Disabled.");
 	}
 
 	/**
 	 * Reloads the plugin by re-reading the configuration file and setting associated variables
 	 * 
 	 * The configuration will be replaced with whatever information is in the file.  Any variables that need to be read from the configuration will be initialized.
 	 * 
 	 * @return boolean: True if reload was successful.  Currently all reloads are considered successful
 	 * since there are fallbacks for cases where the configuration isn't there.
 	 */
 	public boolean reload()
 	{
 		info("(re)loading...");
 		File file = new File(config);
 		cfg = new Configuration(file);
 		if(!file.exists())
 		{
 			warning("Could not find a configuration file, saving a new one...");
 			if (!saveDefault())
 			{
 				warning("Running on default values, but could not save a new configuration file.");
 			}
 		}
 		else
 		{
 			cfg.load();
 			sizeLimit = cfg.getInt("settings.size-limit", 100);
 			spawnLimit = cfg.getInt("settings.spawn-limit", 300);
 			hSpeedLimit = cfg.getDouble("settings.horizontal-speed-limit", 10);
 			permissions = cfg.getBoolean("settings.use-permissions", true);
 			neverSpawn = cfg.getStringList("never.spawn", neverSpawn);
 			neverKill = cfg.getStringList("never.kill", neverKill);
 			if (permissions)
 				setupPermissions();
 		}
 		info("done.");
 		return true;
 	}
 
 	/**
 	 * Saves a new default configuration file, overwriting old configuration and file in the process
 	 * Any existing configuration will be replaced with the default configuration and saved to disk.  Any variables that need to be read from the configuration will be initialized
 	 * @return boolean: True if successful, false otherwise
 	 */
 	public boolean saveDefault()
 	{
 		info("Resetting configuration file with default values...");
 		cfg = new Configuration(new File(config));
 
 		neverSpawn = Arrays.asList("Animals", "Creature", "Entity", "Explosive", "FallingSand", "Fish", "Flying", "HumanEntity", "LivingEntity", "Monster", "Painting", "Player", "Projectile", "Vehicle", "WaterMob");
 		neverKill = Arrays.asList("Animals", "Creature", "Entity", "Explosive", "FallingSand", "Fish", "Flying", "HumanEntity", "LivingEntity", "Monster", "Painting", "Player", "Projectile", "Vehicle", "WaterMob");
 
 		cfg.setProperty("alias.cavespider", Arrays.asList("CaveSpider"));
 		cfg.setProperty("alias.chicken", Arrays.asList("Chicken"));
 		cfg.setProperty("alias.cow", Arrays.asList("Cow"));
 		cfg.setProperty("alias.creeper", Arrays.asList("Creeper"));
 		cfg.setProperty("alias.supercreeper", Arrays.asList("Creeper"));
 		cfg.setProperty("alias.supercreeper-parameters", "/a");
 		cfg.setProperty("alias.enderman", Arrays.asList("Enderman"));
 		cfg.setProperty("alias.endermen", Arrays.asList("Enderman"));
 		cfg.setProperty("alias.ghast", Arrays.asList("Ghast"));
 		cfg.setProperty("alias.giant", Arrays.asList("Giant"));
 		cfg.setProperty("alias.pig", Arrays.asList("Pig"));
 		cfg.setProperty("alias.pigzombie", Arrays.asList("PigZombie"));
 		cfg.setProperty("alias.zombiepigman", Arrays.asList("PigZombie"));
 		cfg.setProperty("alias.pigman", Arrays.asList("PigZombie"));
 		cfg.setProperty("alias.sheep", Arrays.asList("Sheep"));
 		cfg.setProperty("alias.silverfish", Arrays.asList("Silverfish"));
 		cfg.setProperty("alias.skeleton", Arrays.asList("Skeleton"));
 		cfg.setProperty("alias.slime", Arrays.asList("Slime"));
 		cfg.setProperty("alias.spider", Arrays.asList("Spider"));
 		cfg.setProperty("alias.squid", Arrays.asList("Squid"));
 		cfg.setProperty("alias.wolf", Arrays.asList("Wolf"));
 		cfg.setProperty("alias.werewolf", Arrays.asList("Wolf"));
 		cfg.setProperty("alias.werewolf-parameters", "/a");
 		cfg.setProperty("alias.dog", Arrays.asList("Wolf"));
 		cfg.setProperty("alias.zombie", Arrays.asList("Zombie"));
 		cfg.setProperty("alias.friendly", Arrays.asList("Chicken", "Cow", "Pig", "Sheep", "Squid"));
 		cfg.setProperty("alias.hostile", Arrays.asList("CaveSpider", "Creeper", "Enderman", "Ghast", "Giant", "Silverfish", "Skeleton", "Slime", "Spider", "Zombie"));
 		cfg.setProperty("alias.provoke", Arrays.asList("Enderman", "PigZombie", "Wolf"));
 		cfg.setProperty("alias.provoke-parameters", "/a");
 		cfg.setProperty("alias.burnable", Arrays.asList("Enderman", "Skeleton", "Zombie"));
 		cfg.setProperty("alias.day", Arrays.asList("Chicken", "Cow", "Pig", "Sheep", "Squid"));
 		cfg.setProperty("alias.night", Arrays.asList("Creeper", "Enderman", "Skeleton", "Spider", "Zombie"));
 		cfg.setProperty("alias.cave", Arrays.asList("CaveSpider", "Creeper", "Enderman", "Silverfish", "Skeleton", "Slime", "Spider", "Zombie"));
 		cfg.setProperty("alias.boss", Arrays.asList("Ghast", "Giant"));
 		cfg.setProperty("alias.flying", Arrays.asList("Ghast"));
 		cfg.setProperty("alias.mob", Arrays.asList("CaveSpider", "Chicken", "Creeper", "Cow", "Enderman", "Pig", "PigZombie", "Sheep", "Silverfish", "Skeleton", "Slime", "Spider", "Squid", "Wolf", "Zombie"));
 		cfg.setProperty("alias.kill", Arrays.asList("CaveSpider", "Chicken", "Creeper", "Cow", "Enderman", "Ghast", "Giant", "Pig", "PigZombie", "Sheep", "Silverfish", "Skeleton", "Slime", "Spider", "Squid", "Wolf", "Zombie"));
 		cfg.setProperty("alias.meat", Arrays.asList("Pig", "Cow", "Chicken"));
 		cfg.setProperty("alias.meat-parameters", "/f:60");
 		
 		//Transit
 		cfg.setProperty("alias.boat", Arrays.asList("Boat"));
 		cfg.setProperty("alias.cart", Arrays.asList("Minecart"));
 		cfg.setProperty("alias.minecart", Arrays.asList("Minecart"));
 		cfg.setProperty("alias.poweredminecart", Arrays.asList("PoweredMinecart"));
 		cfg.setProperty("alias.locomotive", Arrays.asList("PoweredMinecart"));
 		cfg.setProperty("alias.storageminecart", Arrays.asList("StorageMinecart"));
 		cfg.setProperty("alias.train", Arrays.asList("Minecart"));
 		cfg.setProperty("alias.transit", Arrays.asList("Boat", "Minecart", "PoweredMinecart", "StorageMinecart"));
 
 		//Projectiles
 		cfg.setProperty("alias.arrow", Arrays.asList("Arrow"));
 		cfg.setProperty("alias.egg", Arrays.asList("Egg"));
 		cfg.setProperty("alias.fireball", Arrays.asList("Fireball"));
 		cfg.setProperty("alias.snowball", Arrays.asList("Snowball"));
 		cfg.setProperty("alias.projectile", Arrays.asList("Arrow", "Egg", "Fireball", "Snowball"));
 
 		//Explosives
 		cfg.setProperty("alias.lightning", Arrays.asList("LightningStrike"));
 		cfg.setProperty("alias.lightningstrike", Arrays.asList("LightningStrike"));
 		cfg.setProperty("alias.strike", Arrays.asList("LightningStrike"));
 		cfg.setProperty("alias.primedtnt", Arrays.asList("PrimedTNT"));
 		cfg.setProperty("alias.tnt", Arrays.asList("TNTPrimed"));
 		cfg.setProperty("alias.weather", Arrays.asList("Weather"));
 		cfg.setProperty("alias.explosive", Arrays.asList("LightningStrike", "PrimedTNT", "Weather"));
 		
 		//Drops
 		cfg.setProperty("alias.experience", Arrays.asList("ExperienceOrb"));
 		cfg.setProperty("alias.experienceorb", Arrays.asList("ExperienceOrb"));
 		cfg.setProperty("alias.orb", Arrays.asList("ExperienceOrb"));
 		cfg.setProperty("alias.xp", Arrays.asList("ExperienceOrb"));
 		cfg.setProperty("alias.xporb", Arrays.asList("ExperienceOrb"));
 		cfg.setProperty("alias.item", Arrays.asList("Item"));
 		cfg.setProperty("alias.mirage", Arrays.asList("Item"));
 		cfg.setProperty("alias.mirage-parameters", "/i:264,0");
 		cfg.setProperty("alias.fireworks", Arrays.asList("Item"));
 		cfg.setProperty("alias.fireworks-parameters", "/i:331,0/f:60/v:2");
 		
 		
 
 		//Example Player List
 		cfg.setProperty("player-alias.example", Arrays.asList("JohnDoe", "JohnDoesBrother"));
 		
 
 		permissions = false;
 		spawnLimit = 100;
 		sizeLimit = 50;
 		hSpeedLimit = 10;
 
 		if (save())
 		{
 			reload();
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	/**
 	 * Saves the configuration file, overwriting old file in the process
 	 * 
 	 * @return boolean: True if successful, false otherwise.
 	 */
 	public boolean save()
 	{
 		info("Saving configuration file...");
 		File dir = new File(path);
 		cfg.setProperty("settings.use-permissions", permissions);
 		cfg.setProperty("settings.spawn-limit", spawnLimit);
 		cfg.setProperty("settings.size-limit", sizeLimit);
 		cfg.setProperty("settings.horizontal-speed-limit", hSpeedLimit);
 		cfg.setProperty("never.spawn", neverSpawn);
 		cfg.setProperty("never.kill", neverKill);
 		if(!dir.exists())
 		{
 			if (!dir.mkdir())
 			{
 				severe("Could not create directory " + path + "; if there is a file with this name, please rename it to something else.  Please make sure the server has rights to make this directory.");
 				return false;
 			}
 			info("Created directory " + path + "; this is where your configuration file will be kept.");
 		}
 		cfg.save();
 		File file = new File(config);
 		if (!file.exists())
 		{
 			severe("Configuration could not be saved! Please make sure the server has rights to output to " + config);
 			return false;
 		}
 		info("Saved configuration file: " + config);
 		return true;
 	}
 	
 	/**
 	 * Sets a flag intended to prevent this entity from ever being spawned by this plugin
 	 * 
 	 * This is intended for situations where the entity threw an exception indicating that the
 	 * game really, really, really was not happy about being told to spawn that entity.  Flagging
 	 * this entity is supposed to stop any player (even the admin) from spawning this entity
 	 * regardless of permissions, aliases, etc.
 	 * 
 	 * @param ent - The entity class.  No instance of this class will be spawned using this plugin
 	 */
 	
 	public void flag(Class<Entity> ent)
 	{
 		if (neverSpawn.contains(ent.getSimpleName()))
 			return;
 		neverSpawn.add(ent.getSimpleName());
 	}
 	
 	/**
 	 * Utility function; returns a list of players associated with the supplied alias.
 	 * 
 	 * This can be restricted by permissions in two ways:  permission can be denied for a specific
 	 * alias, or permission can be denied for spawning players in general.  User should be aware that
 	 * if permission is denied on alias Derp, but there is a player named DerpIsDerp, a list
 	 * containing a single player named DerpIsDerp will be returned.
 	 * 
 	 * @param alias: An alias/name associated with one or more players
 	 * @param sender: The person who asked for the alias
 	 * @param permsPrefix: The intended purpose of this list, used as a permissions prefix
 	 * @return PlayerAlias: A list of players combined with parameters.  If no players were found, returns a size 0 array (not null)
 	 */
 	
 	protected PlayerAlias lookupPlayers(String alias, CommandSender sender, String permsPrefix)
 	{
 		List<Player> list = new ArrayList<Player>();
 		List<String> names = new ArrayList<String>();
 		String params = "";
 		Player[] derp = new Player[0];//Needed for workaround below
 		if (alias == null)
 			return new PlayerAlias(list.toArray(derp), params);
 		names = cfg.getStringList("player-alias." + alias.toLowerCase(), names);
 		params = cfg.getString("alias." + alias.toLowerCase() + "-parameters", params);
 		if ((names.size() > 0) && allowedTo(sender, permsPrefix + "." + alias))
 		{
 			for (Iterator<String> i = names.iterator(); i.hasNext();)
 			{
 				String name = i.next();
 				Player target = getServer().getPlayerExact(name);
 				if (target != null)
 					list.add(target);
 			}
 		}
 		else if (allowedTo(sender, permsPrefix + ".player"))
 		{
 			Player target = getServer().getPlayer(alias);
 			if (target == null)
 				target = getServer().getPlayerExact(alias);
 			if (target != null)
 				list.add(target);
 		}
 		return new PlayerAlias(list.toArray(derp), params); // what a ridiculous workaround
 	}
 
 	/**
 	 * Utility function; returns a list of entity classes associated with the supplied alias.
 	 * 
 	 * This can be restricted by permissions on each entity type (NOT by alias type).  If player
 	 * has permissions for only some of the entities indicated by an alias, a partial list will
 	 * be returned.
 	 * 
 	 * @param alias: An alias/name associated with one or more entity types
 	 * @param sender: The person who asked for the alias
 	 * @param permsPrefix: The intended purpose of this list, used as a permissions prefix
 	 * @return Alias: A list of entities combined with parameters.  If no entities were found, returns a size 0 array (not null)
 	 */
 	public Alias lookup(String alias, CommandSender sender, String permsPrefix)
 	{
 		List<Class<Entity>> list = new ArrayList<Class<Entity>>();
 		List<String> names = new ArrayList<String>();
 		String params = "";
 		Class<Entity>[] derp = new Class[0];//Needed for ridiculous workaround below
 		if (alias == null)
 			return new Alias(list.toArray(derp), params);
 		if (alias.toLowerCase().startsWith("org.bukkit.entity."))//allow user to specify formal name to avoid conflict (e.g. player named Zombie and not able to use lowercase because of lack of alias, which would be generated after using the formal name once)
 		{
 			if (alias.length() > 18)
 				alias = alias.substring(17);
 			else
 				return new Alias(list.toArray(derp), params);//an empty list since they didn't finish specifying the class
 		}
 		names = cfg.getStringList("alias." + alias.toLowerCase(), names);
 		params = cfg.getString("alias." + alias.toLowerCase() + "-parameters", params);
 		if (names.size() > 0)
 			for (Iterator<String> i = names.iterator(); i.hasNext();)
 			{
 				String entName = "org.bukkit.entity." + i.next();
 				try
 				{
 					Class<?> c = (Class<?>) Class.forName(entName);
 					if (Entity.class.isAssignableFrom(c))
 					{
 						if (allowedTo(sender, permsPrefix + "." + c.getSimpleName()) && !neverSpawn.contains(c.getSimpleName()))
 						{
 							list.add((Class<Entity>) c);
 						}
 					}
 				}
 				catch (ClassNotFoundException e)
 				{
 					warning("Config file says that " + alias + " is a " + entName + ", but could not find that class.  Skipping...");
 				}
 			}
 		else
 		{
 			try
 			{
 				Class<?> c = (Class<?>) Class.forName("org.bukkit.entity." + alias);
 				if (Entity.class.isAssignableFrom(c))
 				{
 					if (allowedTo(sender, permsPrefix + "." + c.getSimpleName()) && !neverSpawn.contains(c.getSimpleName()))
 					{
 						list.add((Class<Entity>) c);
 						cfg.setProperty("alias." + alias.toLowerCase(), Arrays.asList(c.getSimpleName()));
 						info("Class " + c.getName() + " has not been invoked before; adding alias to configuration");
 					}
 				}
 			}
 			catch (ClassNotFoundException e)
 			{
 				;//om nom nom
 			}
 		}
 		return new Alias(list.toArray(derp), params); // what a ridiculous workaround to make it return an array
 	}
 	
 	/**
 	 * A ridiculously complicated function for handling player commands
 	 * @param sender: The person sending the command
 	 * @param command: The command being called
 	 * @param commandLabel: Dunno what the difference is between this and command.getName()
 	 * @param args: The list of arguments given for the command
 	 * 
 	 * @return boolean: True if successful (also indicates player used syntax correctly), false otherwise
 	 */
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
 	{
 		int[] ignore = {8, 9};
 		if (command.getName().equalsIgnoreCase("ent"))
 		{
 			if (allowedTo(sender, "spawn"))
 			{
 				if ((args.length > 0)&&(args.length < 4)) 
 				{
 					if (args[0].equalsIgnoreCase("kill") || args[0].toLowerCase().startsWith("kill/"))
 					{
 						if (!allowedTo(sender, "spawn.kill"))
 						{
 							printHelp(sender);
 							return false;
 						}
 						String type=args[0]; // save parameters for later in case mob is not specified
 						int radius = 0;
 						if (args.length > 2) //Should be /sm kill <type> <radius>
 						{
 							type=args[1];
 							try
 							{
 								radius = Integer.parseInt(args[2]);
 							}
 							catch (NumberFormatException e)
 							{
 								printHelp(sender);
 								return false;
 							}
 						}
 						else if (args.length > 1) //Should be either /sm kill <type> or /sm kill <radius>
 						{
 							try
 							{
 								radius = Integer.parseInt(args[1]);
 							}
 							catch (NumberFormatException e)
 							{
 								type=args[1];
 							}
 						}
 						String name = type, params = "";
 						if (type.contains("/")) // if the user specified parameters, distinguish them from the name of the entity
 						{
 							name = type.substring(0, type.indexOf("/"));
 							params = type.substring(type.indexOf("/"));
 						}
 						
 						Alias alias = lookup(name, sender, "spawn.kill-ent");
 						String mobParam[] = (name + alias.getParams() + params).split("/"); //user-specified params go last, so they can override alias-specified params
 						Class<Entity>[] targetEnts = alias.getTypes();
 						if (targetEnts.length == 0)
 						{
 							sender.sendMessage(ChatColor.RED + "Invalid mob type.");
 							return false;
 						}
 						int healthValue=100, sizeValue=1;
 						boolean angry = false, color = false, fire = false, health = false, mount = false, size = false, target = false, owned = false, naked = false;
 						PlayerAlias owner=new PlayerAlias(), targets=new PlayerAlias();
 						DyeColor colorCode=DyeColor.WHITE;
 						if (mobParam.length>1)
 						{
 							for (int j=1; j<mobParam.length; j++)
 							{
 								String paramName = mobParam[j].substring(0, 1);
 								String param = null;
 								if (mobParam[j].length() > 2)
 									param = mobParam[j].substring(2);
 								if (paramName.equalsIgnoreCase("a"))
 								{
 									if(allowedTo(sender, "spawn.kill.angry"))
 									{
 										angry=true;
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("c"))
 								{
 									if(allowedTo(sender, "spawn.kill.color"))
 									{
 										color=true;
 										try
 										{
 											colorCode = DyeColor.getByData(Byte.parseByte(param));
 										}
 										catch (NumberFormatException e)
 										{
 											try
 											{
 												colorCode = DyeColor.valueOf(DyeColor.class, param.toUpperCase());
 											} catch (IllegalArgumentException f)
 											{
 												sender.sendMessage(ChatColor.RED + "Color parameter must be a valid color or a number from 0 to 15.");
 												return false;
 											}
 										}
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("f"))
 								{
 									if(allowedTo(sender, "spawn.kill.fire"))
 										fire=true;
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("h"))
 								{
 									if (allowedTo(sender, "spawn.kill.health"))
 									{
 										try
 										{
 											if (param.endsWith("%"))
 											{
 												sender.sendMessage(ChatColor.RED + "Health parameter must be an integer (Percentage not supported for kill)");
 												return false;
 											}
 											else
 											{
 												healthValue = Integer.parseInt(param);
 												health=true;
 											}
 										} catch (NumberFormatException e)
 										{
 											sender.sendMessage(ChatColor.RED + "Health parameter must be an integer (Percentage not supported for kill)");
 											return false;
 										}
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("m"))
 								{
 									if(allowedTo(sender, "spawn.kill.mount"))
 									{
 										mount=true;
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("n"))
 								{
 									if(allowedTo(sender, "spawn.kill.naked"))
 										naked=true;
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("o"))
 								{
 									if(allowedTo(sender, "spawn.kill.owner"))
 									{
 										owned = true;
 										owner = lookupPlayers(param, sender, "kill.owner"); // No need to validate; null means that it will kill ALL owned wolves.\
 										if ((owner.getPeople().length == 0)&&(param != null)) // If user typed something, it means they wanted a specific player and would probably be unhappy with killing ALL owners.
 											sender.sendMessage(ChatColor.RED + "Could not locate player by that name.");
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("s"))
 								{
 									if(allowedTo(sender, "spawn.kill.size"))
 									{
 										try
 										{
 											size = true;
 											sizeValue = Integer.parseInt(param); //Size limit only for spawning, not killing.
 										} catch (NumberFormatException e)
 										{
 											sender.sendMessage(ChatColor.RED + "Size parameter must be an integer.");
 											return false;
 										}
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 								else if (paramName.equalsIgnoreCase("t"))
 								{
 									try
 									{
 										if(allowedTo(sender, "spawn.kill.target"))
 										{
 											target=true;
 											targets = lookupPlayers(param, sender, "kill.target");
 											if ((targets.getPeople().length == 0) && (param != null)) // If user actually bothered to typed something, it means they were trying for a specific player and probably didn't intend for mobs with ANY targets.
 											{
 												sender.sendMessage(ChatColor.RED + "Could not find a target by that name");
 												return false;
 											}
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									} catch (NumberFormatException e)
 									{
 										sender.sendMessage(ChatColor.RED + "Size parameter must be an integer.");
 										return false;
 									}
 								}
 								else
 								{
 									sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 									return false;
 								}
 							}
 						}
 						int bodyCount=0;
 						if ((radius != 0)&&(!(sender instanceof Player)))
 						{
 							sender.sendMessage(ChatColor.RED + "...and where did you think I'd measure that radius from, Mr Console?");
 							return false;
 						}
 							
 						bodyCount=Kill(sender, targetEnts, radius, angry, color, colorCode, fire, health, healthValue, mount, naked, owned, owner.getPeople(), size, sizeValue, target, targets.getPeople());
 						sender.sendMessage(ChatColor.BLUE + "Killed " + bodyCount + " " + mobParam[0] + "s.");
 						return true;
 					}
 					// Done with /kill
 					else if (allowedTo(sender, "spawn.spawn"))
 					{
 						if (!(sender instanceof Player))
 						{
 							printHelp(sender);
 							return false;
 						}
 						Player player = (Player) sender;
 						Location loc=player.getLocation();
 						Block targetB = new TargetBlock(player, 300, 0.2, ignore).getTargetBlock();
 						if (targetB!=null)
 						{
 							loc.setX(targetB.getLocation().getX());
 							loc.setY(targetB.getLocation().getY() + 1);
 							loc.setZ(targetB.getLocation().getZ());
 						}
 
 						int count=1;
 						String[] passengerList = args[0].split(";"); //First, get the passenger list
 						Ent index = null, index2 = null;
 						for (int i=0; i<passengerList.length; i++)
 						{
 							if (index != null)
 								index.setPassenger(index2);
 							
 							String name = passengerList[i], params = "";
 							if (passengerList[i].contains("/")) // if the user specified parameters, distinguish them from the name of the entity
 							{
 								name = passengerList[i].substring(0, passengerList[i].indexOf("/"));
 								params = passengerList[i].substring(passengerList[i].indexOf("/"));
 							}
 
 							PlayerAlias playerAlias = lookupPlayers(name, sender, "spawn.spawn-player");
 							Alias alias = lookup(name, sender, "spawn.spawn-ent");
 							if (playerAlias.getPeople().length > 0)
 								params = playerAlias.getParams() + params;
 							else
 								params = alias.getParams() + params;
 							String mobParam[] = (name + params).split("/"); //Check type for params
 							Player[] people = playerAlias.getPeople();
 							Class<Entity>[] results = alias.getTypes();
 							if (results.length == 0 && people.length == 0)
 							{
 								sender.sendMessage(ChatColor.RED + "Invalid mob type.");
 								return false;
 							}
 
 							int healthValue=100, itemType=17, itemAmount=1, size=1, fireTicks=-1;
 							short itemDamage=0;
 							Byte itemData=null;
 							double velRandom=0;
 							Vector velValue = new Vector(0,0,0);
 							boolean setSize = false, health = false, healthIsPercentage = true, angry = false, bounce = false, color = false, mount = false, target = false, tame = false, naked = false, velocity = false;
 							PlayerAlias targets=new PlayerAlias();
 							PlayerAlias owner=new PlayerAlias();
 							DyeColor colorCode=DyeColor.WHITE;
 							if (mobParam.length>1)
 							{
 								for (int j=1; j<mobParam.length; j++)
 								{
 									String paramName = mobParam[j].substring(0, 1);
 									String param = null;
 									if (mobParam[j].length() > 2)
 										param = mobParam[j].substring(2);
 									if (paramName.equalsIgnoreCase("a"))
 									{
 										if(allowedTo(sender, "spawn.angry"))
 										{
 											angry=true;
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("b"))
 									{
 										if(allowedTo(sender, "spawn.bounce"))
 										{
 											bounce=true;
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("c"))
 									{
 										if(allowedTo(sender, "spawn.color"))
 										{
 											color=true;
 											try
 											{
 												colorCode = DyeColor.getByData(Byte.parseByte(param));
 											}
 											catch (NumberFormatException e)
 											{
 												try
 												{
 													colorCode = DyeColor.valueOf(DyeColor.class, param.toUpperCase());
 												} catch (IllegalArgumentException f)
 												{
 													sender.sendMessage(ChatColor.RED + "Color parameter must be a valid color or a number from 0 to 15.");
 													return false;
 												}
 											}
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("f"))
 									{
 										if(allowedTo(sender, "spawn.fire"))
 											try
 											{
 												fireTicks = Integer.parseInt(param)*20;
 											} catch (NumberFormatException e)
 											{
 												sender.sendMessage(ChatColor.RED + "Fire parameter must be an integer.");
 												return false;
 											}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("h"))
 									{
 										if (allowedTo(sender, "spawn.health"))
 										{
 											try
 											{
 												if (param.endsWith("%"))
 												{
 													healthIsPercentage=true;
 													healthValue = Integer.parseInt(param.substring(0, param.indexOf("%")));
 													health=true;
 												}
 												else
 												{
 													healthIsPercentage=false;
 													healthValue = Integer.parseInt(param);
 													health=true;
 												}
 											} catch (NumberFormatException e)
 											{
 												sender.sendMessage(ChatColor.RED + "Health parameter must be an integer or a percentage");
 												return false;
 											}
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("i"))
 									{
 										if(allowedTo(sender, "spawn.item"))
 										{
 											String specify[] = param.split(",");
 											if (specify.length>3)
 												itemData = Byte.parseByte(specify[3]);
 											if (specify.length>2)
 												itemDamage = Short.parseShort(specify[2]);
 											if (specify.length>1)
 												itemAmount = Integer.parseInt(specify[1]);
 											if (specify.length>0)
 												itemType = Integer.parseInt(specify[0]);
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("m"))
 									{
 										if(allowedTo(sender, "spawn.mount"))
 										{
 											mount=true;
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("n"))
 									{
 										if(allowedTo(sender, "spawn.naked"))
 											naked=true;
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("o"))
 									{
 										if(allowedTo(sender, "spawn.owner"))
 										{
 											tame=true;
 											owner = lookupPlayers(param, sender, "spawn.owner"); // No need to validate; null means that it will be tame but unownable.  Could be fun.
 											if ((owner.getPeople().length == 0)&&(param != null)) // If user typed something, it means they wanted a specific player and would probably be unhappy with killing ALL owners.
 												sender.sendMessage(ChatColor.RED + "Could not locate player by that name.");
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("s"))
 									{
 										if(allowedTo(sender, "spawn.size"))
 										{
 											try
 											{
 												setSize = true;
 												size = Integer.parseInt(param);
 												if (size > sizeLimit)
 													size = sizeLimit;
 											} catch (NumberFormatException e)
 											{
 												sender.sendMessage(ChatColor.RED + "Size parameter must be an integer.");
 												return false;
 											}
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("t"))
 									{
 										try
 										{
 											if(allowedTo(sender, "spawn.target"))
 											{
 												target=true;
 												targets = lookupPlayers(param, sender, "spawn.target");
 												if (targets.getPeople().length == 0)
 												{
 													sender.sendMessage(ChatColor.RED + "Could not find a target by that name");
 													return false;
 												}
 											}
 											else
 											{
 												sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 												return false;
 											}
 										} catch (NumberFormatException e)
 										{
 											sender.sendMessage(ChatColor.RED + "Size parameter must be an integer.");
 											return false;
 										}
 									}
 									else if (paramName.equalsIgnoreCase("v"))
 									{
 										if(allowedTo(sender, "spawn.velocity"))
 										{
 											velocity = true;
 											String specify[] = param.split(",");
 											if (specify.length==3)
 											{
 												velValue.setX(Double.parseDouble(specify[0]));
 												velValue.setY(Double.parseDouble(specify[1]));
 												velValue.setZ(Double.parseDouble(specify[2]));
 											}	
 											else
 											try
 											{
 												velRandom = Double.parseDouble(param);
 											} catch (NumberFormatException e)
 											{
 												sender.sendMessage(ChatColor.RED + "Velocity parameter must be an integer.");
 												return false;
 											}
 										}
 										else
 										{
 											sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 											return false;
 										}
 									}
 									else
 									{
 										sender.sendMessage(ChatColor.RED + "Invalid parameter " + paramName);
 										return false;
 									}
 								}
 							}
 							index2=index;
 							if (people.length == 0)
 								index = new Ent(results, mobParam[0], angry, bounce, color, colorCode, fireTicks, health, healthIsPercentage, healthValue, itemType, itemAmount, itemDamage, itemData, mount, naked, tame, owner.getPeople(), index2, setSize, size, target, targets.getPeople(), velocity, velRandom, velValue);
 							else
 								index = new Person(people, mobParam[0], angry, bounce, color, colorCode, fireTicks, health, healthIsPercentage, healthValue, itemType, itemAmount, itemDamage, itemData, mount, naked, tame, owner.getPeople(), index2, setSize, size, target, targets.getPeople(), velocity, velRandom, velValue);
 						}
 						
 						if (args.length > 1)
 						{
 							try
 							{
 								count=Integer.parseInt(args[1]);
 								if (count < 1)
 								{
 									sender.sendMessage(ChatColor.RED + "Invalid number - must be at least one.");
 									return false;
 								}
 							}
 							catch (Exception e)
 							{
 
 								return false;
 							}
 						}
 						if (count > (spawnLimit/passengerList.length))
 						{
 							info("Player " + sender.getName() + " tried to spawn more than " + spawnLimit + " entities.");
 							count = spawnLimit/passengerList.length;
 						}
 						if (index.spawn(player, this, loc, count))
 							sender.sendMessage(ChatColor.BLUE + "Spawned " + count + " " + index.description());
 						else
 							sender.sendMessage(ChatColor.RED + "Some things just weren't meant to be spawned.  Check server log.");
 						return true;
 					}
 				}
 				else
 				{
 					printHelp(sender);
 					return false;
 				}
 			}
 		}
 		else if (command.getName().equalsIgnoreCase("ent-admin"))
 		{
			if (allowedTo(sender, "spawn.admin"))
 			{
 				if ((args.length > 0)) 
 				{
 					if (args[0].equalsIgnoreCase("save"))
 					{
 						sender.sendMessage(ChatColor.GREEN + "Saving configuration file...");
 						if (save())
 							sender.sendMessage(ChatColor.GREEN + "Done.");
 						else
 							sender.sendMessage(ChatColor.RED + "Could not save configuration file - please see server log.");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("reset"))
 					{
 						sender.sendMessage(ChatColor.GREEN + "Resetting configuration file...");
 						if (saveDefault())
 							sender.sendMessage(ChatColor.GREEN + "Done.");
 						else
 							sender.sendMessage(ChatColor.RED + "Could not save configuration file - please see server log.");
 						return true;
 					}
 					else if (args[0].equalsIgnoreCase("reload"))
 					{
 						sender.sendMessage(ChatColor.GREEN + "Reloading Spawn...");
 						if (reload())
 							sender.sendMessage(ChatColor.GREEN + "Done.");
 						else
 							sender.sendMessage(ChatColor.RED + "An error occurred while reloading - please see server log.");
 						return true;
 					}
 				}
 			}
 		}
 		else
 			sender.sendMessage("Unknown console command. Type \"help\" for help"); // No reason to tell them what they CAN'T do, right?
 		return false;
 	}
 	
 	/**
 	 * Prints help in accordance with the player's permissions
 	 * 
 	 * @param sender: The person being "helped"
 	 */
 	public void printHelp(CommandSender sender)
 	{
 		if (allowedTo(sender, "spawn.admin"))
 		{
 			sender.sendMessage(ChatColor.GREEN + "/spawn-admin reload");
 			sender.sendMessage(ChatColor.YELLOW + "Reloads Spawn plugin");
 			sender.sendMessage(ChatColor.GREEN + "/spawn-admin save");
 			sender.sendMessage(ChatColor.YELLOW + "Saves Spawn's configuration file");
 			sender.sendMessage(ChatColor.GREEN + "/spawn-admin reset");
 			sender.sendMessage(ChatColor.RED + "Overwrites Spawn's configuration file with default settings");
 			sender.sendMessage(ChatColor.YELLOW + "Alternative commands: " + ChatColor.WHITE + "/ent-admin, /spawn-entity-admin, /sp-admin, /se-admin, /s-admin");
 		}
 		if (allowedTo(sender, "spawn.spawn") && sender instanceof Player)
 		{
 			sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/<paramname>:<param>/<paramname>:<param>");
 			sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with parameters");
 			if (allowedTo(sender, "spawn.angry"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/a:");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns an angry/powered version of <entity>");
 			}
 			if (allowedTo(sender, "spawn.bounce"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/b:");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> projectile that bounces on impact");
 			}
 			if (allowedTo(sender, "spawn.color"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/c:<color code 0-15>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> that has the specified color");
 			}
 			if (allowedTo(sender, "spawn.fire"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/f:<number of seconds>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> that burns for specified number of (unlagged) seconds");
 				sender.sendMessage(ChatColor.YELLOW + "Entities that specify a fuse also use this value");
 			}
 			if (allowedTo(sender, "spawn.health"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/h:<health>" + ChatColor.YELLOW + " OR " + ChatColor.BLUE + "/spawn <mob>/h:<health%>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified health (usually only works for 1-10, can also use percentage)");
 			}
 			if (allowedTo(sender, "spawn.item"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn Item/i:<type>,<amount/stack>,<damage>,<data>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns an item stack of specified type number, amount per stack, damage value, and data value.");
 			}
 			if (allowedTo(sender, "spawn.mount"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/m:");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with a mount (saddle)");
 			}
 			if (allowedTo(sender, "spawn.naked"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/n:");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with clothing irretrievably destroyed");
 			}
 			if (allowedTo(sender, "spawn.owner"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/o:<player name>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns tame <entity> with specified player as owner; if unspecified, will be unownable");
 			}
 			if (allowedTo(sender, "spawn.passenger"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entparams>;<entparams2>" + ChatColor.AQUA + "[;<entparams3>...] <number>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with parameters riding <ent2>" + ChatColor.DARK_AQUA + " riding <ent3>...");
 			}
 			if (allowedTo(sender, "spawn.size"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/s:<size>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified size (usually only works for slimes)");
 			}
 			if (allowedTo(sender, "spawn.target"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/t:<player name>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified player as target");
 			}
 			if (allowedTo(sender, "spawn.velocity"))
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/v:<velocity>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified velocity (random direction)");
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/v:<x>,<y>,<z>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified velocity and direction");
 				sender.sendMessage(ChatColor.BLUE + "/spawn <entity>/v:<x>,<y>,<z>/v:<offsetvelocity>");
 				sender.sendMessage(ChatColor.YELLOW + "Spawns <entity> with specified direction plus an offset in a random direction");
 			}
 			sender.sendMessage(ChatColor.YELLOW + "Alternative commands: " + ChatColor.WHITE + "/ent, /spawn-entity, /sp, /se, /s");
 		}
 		if (allowedTo(sender, "spawn.kill"))
 		{
 			sender.sendMessage(ChatColor.BLUE + "/spawn kill");
 			sender.sendMessage(ChatColor.YELLOW + "Kills all entities and gives a body count");
 			sender.sendMessage(ChatColor.BLUE + "/spawn kill<params>");
 			sender.sendMessage(ChatColor.YELLOW + "Kills all entities with <optional parameters> and gives a body count");
 			if (sender instanceof Player)
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn kill <enttype><params> <radius>");
 				sender.sendMessage(ChatColor.YELLOW + "Kills all entities of <type> with <optional parameters> within <optional radius> of you and gives a body count");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE + "/spawn kill <enttype><params>");
 				sender.sendMessage(ChatColor.YELLOW + "Kills all entities of <type> with <optional parameters> and gives a body count");
 			}
 		}
 	}
 
 	/**
 	 * Probably the only leftover from SpawnMob.  Should replace with a PluginListener...
 	 * 
 	 * Tests to see if permissions is working; if so, sets our Permissions handle so we can access it.
 	 * Otherwise, sets permissions to false.
 	 */
 	private void setupPermissions()
 	{
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (Spawn.Permissions == null) {
 			if (test != null) {
 				Spawn.Permissions = ((Permissions)test).getHandler();
 				info("Permission system found, plugin enabled");
 			} else {
 				info("Permission system not detected! Please go into the SpawnMob.properties and set use-permissions to false.");
 				info("Please go into the SpawnMob.properties and set use-permissions to false.");
 				permissions = false;
 			}
 		}
 	}
 	
 	/**
 	 * Checks to see if sender has permission OR is an op.  If not using permissions, only op is tested.
 	 * @param sender: The person whose permission is being checked
  	 * @param permission The permission being checked (e.g. "exampleplugin.examplepermnode")
 	 * @returns boolean: True if player has the permission node OR if player is an op
 	 */
 	boolean allowedTo(CommandSender sender, String permission)
 	{
 		if (sender.isOp())
 			return true;
 		else if (permissions && sender instanceof Player)
 			return Permissions.has((Player)sender, permission);
 		return false;
 	}
 	
 	/**
 	 * Test to see whether type (usually a CraftBukkit Entity of some kind) uses any of the (Bukkit)
 	 * interfaces in types.  (e.g. CraftCow implements Cow).  Useful for determining if an entity
 	 * is in our list of creature types we want to work on.
 	 * 
 	 * @param types: A list of types that is being used to filter Entities
 	 * @param type: The type being filtered
 	 * @return boolean: True if type uses any of the types as an Interface
 	 */
 	private boolean hasClass(Class<Entity>[] types, Class<? extends Entity> type)
 	{
 		for (int i=0; i<types.length; i++)
 			for (int j=0; j<type.getInterfaces().length; j++)
 				if (type.getInterfaces()[j]==types[i])
 					return true;
 		return false;
 	}
 	
 	/**
 	 * Tests to see whether subject exists as a member of array.
 	 * @param subject: An object being filtered
 	 * @param array: A list of objects being used as a filter
 	 * @return boolean: True if array has subject in it
 	 */
 	private boolean existsIn(Object subject, Object[] array)
 	{
 		for (int i=0; i<array.length; i++)
 			if (array[i]==subject)
 				return true;
 		return false;
 	}
 	
 	/**
 	 * Determines if an entity meets the criteria for slaughter.  If so, removes it.
 	 * @param types: The types of entity referred to by the alias
 	 * @param angry: If true, only kills angry entities
 	 * @param color: If true, only kills entities of a specific colorCode 
 	 * @param colorCode: Used to decide what color of entity to kill (e.g. only kill blue sheep).
 	 * @param fire: If true, only kills burning entities
 	 * @param health: If true, only kills entities of a specific health value
 	 * @param healthValue: Used to decide exactly how healthy an entity needs to be to die
 	 * @param mount: If true, will only kill mounted entities (e.g. saddled pigs)
 	 * @param owned: If true, will only kill owned entities (e.g. owned wolves) (default is to IGNORE owned entities, use carefully!)
 	 * @param owner: If set and owned is true, only kills entities owned by that player.  If set to null and owned is true, kills ALL owned entities
 	 * @param naked: If true, only kills naked entities (e.g. sheared sheep)
 	 * @param size: If true, only kills entities of a specific size
 	 * @param sizeValue: Used to decide exactly how big an entity needs to be to die
 	 * @param target: If true, only kills entities that currently have a target
 	 * @param targets: If set and target is true, only killed entities with these targets.  If null and target is true, kills entities with ANY target
 	 * 
 	 * @return boolean: true is ent was killed, false if it lived
 	 */
 	private boolean KillSingle(Entity ent, Class<Entity>[] types, boolean angry, boolean color, DyeColor colorCode, boolean fire, boolean health, int healthValue, boolean mount, boolean naked, boolean owned, AnimalTamer[] owner, boolean size, int sizeValue, boolean target, Player[] targets)
 	{
 		Class<? extends Entity> type = ent.getClass();
 		try
 		{
 			if (hasClass(types, type))
 			{
 				for (int i=0; i<type.getInterfaces().length; i++)
 					if (neverKill.contains(type.getInterfaces()[i].getSimpleName()))
 						return false; // Never, ever, kill somethingn on this list
 				
 				Method ownerMethod;
 				//CULLING STAGE - each test returns false if it fails to meet it.
 				
 				//ANGRY (default is to kill either way)
 				if (angry)
 				{
 					Method angryMethod = null;
 					try
 					{
 						angryMethod = type.getMethod("isAngry");
 						if (!(Boolean)angryMethod.invoke(ent))
 							return false;
 					} catch (NoSuchMethodException e)
 					{
 						try
 						{
 							angryMethod = type.getMethod("isPowered");
 							if (!(Boolean)angryMethod.invoke(ent))
 								return false;
 						} catch (NoSuchMethodException f){return false;};//yeah, we have to rely on Exceptions to find out if it has a method or not, how sad is that?
 					}
 				}
 				
 				//COLOR (default is to kill either way)
 				
 				if (color)
 				{
 					Method colorMethod;
 					try
 					{
 						colorMethod = type.getMethod("getColor");
 						if (colorCode!=colorMethod.invoke(ent))
 							return false;
 					} catch (NoSuchMethodException e){return false;}
 				}
 				
 				//FIRE (default is to kill either way)
 				
 				if (fire)
 					if (ent.getFireTicks() < 1)
 						return false;
 				
 				//HEALTH (default is to kill either way)
 				
 				if (health)
 				{
 					try
 					{
 						Method healthMethod = type.getMethod("getHealth");
 						if ((Integer)healthMethod.invoke(ent)!=healthValue)
 							return false;
 						if (ent instanceof ExperienceOrb)
 							if (((ExperienceOrb)ent).getExperience()!=healthValue)
 								return false;
 					} catch (NoSuchMethodException e){return false;}
 				}
 				
 				//MOUNT (default is to leave mounted ents alone)
 				
 				Method mountMethod;
 				try
 				{
 					mountMethod = type.getMethod("hasSaddle");
 					if (mount != (Boolean)mountMethod.invoke(ent))
 						return false;
 				} catch (NoSuchMethodException e){if (mount) return false;}
 				
 				//NAKED (default is to leave naked ents alone)
 				
 				Method shearMethod;
 				try
 				{
 					shearMethod = type.getMethod("isSheared");
 					if (naked != (Boolean)shearMethod.invoke(ent))
 						return false;
 				} catch (NoSuchMethodException e){if (naked) return false;}
 					
 				//OWNER (default is to leave owned ents alone)
 				
 				try
 				{
 					ownerMethod = type.getMethod("getOwner");
 					AnimalTamer entOwner = (AnimalTamer) ownerMethod.invoke(ent); // If Bukkit ever adds a getOwner that does not return this, it will break.
 					if (owned)
 					{
 						if (entOwner == null) //Cull all the unowned ents
 							return false;
 						if (owner.length > 0) //If owner is unspecified, then don't cull ANY owned ents
 							if (!existsIn(entOwner, owner)) // Otherwise, cull wolves owned by someone not in the list
 								return false;
 					}
 					else // Default is to NOT kill owned ents.  (Tamed ents with null owner will still be killed)
 						if (entOwner != null)
 							return false;
 				} catch(NoSuchMethodException e){if (owned) return false;}
 				
 				//SIZE (default is to kill either way)
 				
 				if (size)
 				{
 					Method sizeMethod = null;
 					try
 					{
 						sizeMethod = type.getMethod("getSize");
 						if (sizeValue != (Integer)sizeMethod.invoke(ent, sizeValue))
 							return false;
 					} catch (NoSuchMethodException e){return false;};
 				}
 
 				//TARGET (default is to kill either way)
 
 				Method targetMethod;
 				try
 				{
 					if (target)
 					{
 						targetMethod = type.getMethod("getTarget");
 						LivingEntity targetLiving = (LivingEntity)targetMethod.invoke(ent);
 						if (targetLiving == null) // Cull all living ents without a target
 							return false;
 						if (targets.length > 0) // If target is unspecified, don't cull ANY mobs with targets
 							if (!existsIn(targetLiving, targets))
 								return false;
 					}
 				} catch (NoSuchMethodException e){if (target) return false;}
 				
 				ent.remove();
 				return true;
 			}
 		} catch(InvocationTargetException e)
 		{
 			warning("Target " + type.getSimpleName() + " has a method for doing something, but threw an exception when it was invoked:");
 			e.printStackTrace();
 		} catch(IllegalAccessException e)
 		{
 			warning("Target " + type.getSimpleName() + " has a method for doing something, but threw an exception when it was invoked:");
 			e.printStackTrace();
 		} 
 		return false;
 	}
 	
 	/**
 	 * Searches for and kills all entities that meet the specified criteria and returns a body count
 	 * 
 	 * @param sender: The person who sent out the hit.  Used for radius parameter.
 	 * @param types: The types of entity referred to by the alias
 	 * @param radius: If positive, only kills entities within radius of sender.  Will throw casting exception if positive and sender is the console.
 	 * @param angry: If true, only kills angry entities
 	 * @param color: If true, only kills entities of a specific colorCode 
 	 * @param colorCode: Used to decide what color of entity to kill (e.g. only kill blue sheep).
 	 * @param fire: If true, only kills burning entities
 	 * @param health: If true, only kills entities of a specific health value
 	 * @param healthValue: Used to decide exactly how healthy an entity needs to be to die
 	 * @param mount: If true, will only kill mounted entities (e.g. saddled pigs)
 	 * @param owned: If true, will only kill owned entities (e.g. owned wolves) (default is to IGNORE owned entities, use carefully!)
 	 * @param owner: If set and owned is true, only kills entities owned by that player.  If set to null and owned is true, kills ALL owned entities
 	 * @param naked: If true, only kills naked entities (e.g. sheared sheep)
 	 * @param size: If true, only kills entities of a specific size
 	 * @param sizeValue: Used to decide exactly how big an entity needs to be to die
 	 * @param target: If true, only kills entities that currently have a target
 	 * @param targets: If set and target is true, only killed entities with these targets.  If null and target is true, kills entities with ANY target
 	 * 
 	 * @return int: how many entities were slain
 	 */
 	public int Kill(CommandSender sender, Class<Entity>[] types, int radius, boolean angry, boolean color, DyeColor colorCode, boolean fire, boolean health, int healthValue, boolean mount, boolean naked, boolean owned, Player[] owner, boolean size, int sizeValue, boolean target, Player[] targets)
 	{
 		int bodycount=0;
 		List<Entity> ents;
 		if (radius > 0)
 		{
 			ents = ((Player)sender).getNearbyEntities(radius, radius, radius);
 			for(Iterator<Entity> iterator = ents.iterator(); iterator.hasNext();)
 			{
 				Entity ent = iterator.next();
 				if (ent.getLocation().distance(((Player)sender).getLocation()) <= radius)
 					if (KillSingle(ent, types, angry, color, colorCode, fire, health, healthValue, mount, naked, owned, owner, size, sizeValue, target, targets))
 						bodycount++;
 			}
 		}
 		else
 		{
 			for (Iterator<World> worlditerator = getServer().getWorlds().iterator(); worlditerator.hasNext();)
 			{
 				ents = worlditerator.next().getEntities();
 				for(Iterator<Entity> iterator = ents.iterator(); iterator.hasNext();)
 				{
 					Entity ent = iterator.next();
 					if (KillSingle(ent, types, angry, color, colorCode, fire, health, healthValue, mount, naked, owned, owner, size, sizeValue, target, targets))
 						bodycount++;
 				}
 			}
 		}
 		return bodycount;
 	}
 	
 	/**
 	 * Logs an informative message to the console, prefaced with this plugin's header
 	 * @param message: String
 	 */
 	protected static void info(String message)
 	{
 		log.info(header + message);
 	}
 
 	/**
 	 * Logs a severe error message to the console, prefaced with this plugin's header
 	 * Used to log severe problems that have prevented normal execution of the plugin
 	 * @param message: String
 	 */
 	protected static void severe(String message)
 	{
 		log.severe(header + message);
 	}
 
 	/**
 	 * Logs a warning message to the console, prefaced with this plugin's header
 	 * Used to log problems that could interfere with the plugin's ability to meet admin expectations
 	 * @param message: String
 	 */
 	protected static void warning(String message)
 	{
 		log.warning(message);
 	}
 
 	/**
 	 * Logs a message to the console, prefaced with this plugin's header
 	 * @param level: Logging level under which to send the message
 	 * @param message: String
 	 */
 	protected static void log(java.util.logging.Level level, String message)
 	{
 		log.log(level, header + message);
 	}
 }
 
