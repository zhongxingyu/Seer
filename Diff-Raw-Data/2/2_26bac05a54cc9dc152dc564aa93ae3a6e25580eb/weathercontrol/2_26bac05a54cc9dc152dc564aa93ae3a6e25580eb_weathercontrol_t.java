 package net.craftrepo.WeatherControl;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class weathercontrol extends JavaPlugin
 {	
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	public HashMap<String, Integer> items = new HashMap<String, Integer>();
 	public HashMap<Player,Boolean> lightningpick = new HashMap<Player,Boolean>();
 	public Set<Location> lightning = new HashSet<Location>();
 	private final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler Permissions = null;
 	public static String logPrefix = "[WeatherControl]";
 	public weathercontrol plugin;
 	private weathercontrolConfiguration confSetup;
 	public static Configuration config;
 	public static String id = null;
 
 	public void configInit()
 	{
 		getDataFolder().mkdirs();
 		config = new Configuration(new File(this.getDataFolder(), "config.yml"));
 		confSetup = new weathercontrolConfiguration(this.getDataFolder(), this);
 	}
 	
 	public void onEnable() 
 	{
 		setupPermissions();
 		configInit();
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, new weathercontrolPlayerListener(this), Event.Priority.Low, this);
 		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, new weathercontrolEntityDamage(this), Event.Priority.Low, this);
 		getServer().getPluginManager().registerEvent(Event.Type.LIGHTNING_STRIKE, new weathercontrolLightningstrike(this), Event.Priority.Normal, this);
 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " enabled!");
 	}
 
 	public void onDisable() 
 	{
 		log.info(logPrefix + " version " + this.getDescription().getVersion() + " disabled!");
 	}
 
 	public boolean isDebugging(final Player player) 
 	{
 		if (debugees.containsKey(player)) 
 			return debugees.get(player);
 		return false;
 	}
 
 	public void setDebugging(final Player player, final boolean value) 
 	{
 		debugees.put(player, value);
 	}
 
 	public void notifyPlayers(String message) 
 	{
 		for (Player p: getServer().getOnlinePlayers()) 
 		{ 
 			p.sendMessage(message);
 		}
 	}
 
 	public void setupPermissions() 
 	{
 		Plugin perms = this.getServer().getPluginManager().getPlugin("Permissions");
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		if (weathercontrol.Permissions == null) 
 		{
 			if (perms != null) 
 			{
 				this.getServer().getPluginManager().enablePlugin(perms);
 				weathercontrol.Permissions = ((Permissions) perms).getHandler();
 				log.info(logPrefix + " version " + pdfFile.getVersion() + " Permissions detected...");
 			}
 			else 
 			{
 				log.severe(logPrefix + " Permissions not detected. Using OP based access.");
 			}
 		}
 	}
 
 	public static String strip(String s) 
 	{
 		String good = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
 		String result = "";
 		for ( int i = 0; i < s.length(); i++ ) 
 		{
 			if ( good.indexOf(s.charAt(i)) >= 0 )
 				result += s.charAt(i);
 		}
 		return result;
 	}
 
 	public boolean onCommand(CommandSender sender, Command commandArg, String commandLabel, String[] arg) 
 	{
 		Player player = (Player) sender;
 		String command = commandArg.getName().toLowerCase();
 		Player target;
 		if (command.equalsIgnoreCase("lightningpick")) 
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.pick"))
 			{
 				if(lightningpick.containsKey(player))
 				{
 					lightningpick.remove(player);
 					player.sendMessage(logPrefix + " Lightningpick is now off!");
 				}
 				else
 				{
 					lightningpick.put(player, true);
 					player.sendMessage(logPrefix + " Lightningpick is now on!");
 				}
 				System.out.println(lightningpick.containsKey(player));
 			}
 		}
 		//strike a player with lightning.
 		if (command.equalsIgnoreCase("lightningstrike")) 
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.lightningstrike"))
 			{
 				World world = player.getWorld();
 				Block targetBlock = player.getTargetBlock(null, 20);
 				if (targetBlock!=null){
 					Location strikeloc = targetBlock.getLocation();
 					lightning.add(strikeloc);
 					world.strikeLightning(strikeloc);
 				}
 				else
 				{
 					player.sendMessage("No block in sight");
 				}
 			}
 			else 
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 			return true;
 		}else if (command.equalsIgnoreCase("strikeplayer")) 
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.strikeplayer"))
 			{
 				if (arg.length <= 1)
 				{
 					if (arg[0] != null)
 					{
 						target = getServer().getPlayer(arg[0]);
 						World world = target.getWorld();
 						lightning.add(target.getLocation());
 						world.strikeLightning(target.getLocation());
 					}
 					else
 					{
 						player.getWorld().strikeLightning(player.getLocation());
 					}
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /lightning {player}");
 				}
 			}
 			else 
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 			return true;
 		}
 		//start a lightning storm
 		else if (command.equalsIgnoreCase("startstorm"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightningstorm"))
 			{
 				if (arg.length == 1)
 				{
 					World world = player.getWorld();
 					world.setStorm(true);
 					world.setThundering(true);
 					Double dur = Double.parseDouble(arg[0]) / 50;
 					dur = dur * 1000;
 					world.setThunderDuration(dur.intValue());
 					world.setWeatherDuration(dur.intValue());
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /startstorm [length]");
 				}
 			}
 			else
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 		}
 		//start a rain/snow storm
 		else if (command.equalsIgnoreCase("startrain"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.rainstorm"))
 			{
 				if (arg.length == 1)
 				{
 					World world = player.getWorld();
 					world.setStorm(true);
 					world.setThundering(false);
 					Double dur = Double.parseDouble(arg[0]) / 50;
 					dur = dur * 1000;
 					world.setWeatherDuration(dur.intValue());
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /startrain [length]");
 				}
 			}
 			else
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 		}
 		//stop a rain/snow storm
 		else if (command.equalsIgnoreCase("stoprain"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.rainstorm"))
 			{
 				World world = player.getWorld();
 				world.setStorm(false);
 			}
 			else
 			{
 				player.sendMessage("You don't have access to this command.");
 				log.info(logPrefix + " - " + player.getDisplayName() + " tried to use command " + command + "! Denied access." );
 			}
 		}
 		else if (command.equalsIgnoreCase("strikecreepers"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.strikecreeper"))
 			{
 				if (arg.length == 1)
 				{
 					double round = 2 * Integer.parseInt(arg[0]);
 					List<Entity> entities = player.getNearbyEntities(round, round, round);
 					for (Entity e : entities)
 					{
 						if (e instanceof Creeper)
 						{
 							World world = e.getWorld();
 							lightning.add(e.getLocation());
 							world.strikeLightning(e.getLocation()).getEntityId();
 						}
 					}
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /strikecreepers [radius]");
 				}
 			}
 		}
 		else if (command.equalsIgnoreCase("strikepig"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.strikepig"))
 			{
 				if (arg.length == 1)
 				{
 					double round = 2 * Integer.parseInt(arg[0]);
 					List<Entity> entities = player.getNearbyEntities(round, round, round);
 					for (Entity e : entities)
 					{
 						if (e instanceof Pig)
 						{
 							World world = e.getWorld();
 							lightning.add(e.getLocation());
 							world.strikeLightning(e.getLocation()).getEntityId();
 						}
 					}
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /strikepig [radius]");
 				}
 			}
 		}
 		else if (command.equalsIgnoreCase("strikerow"))
 		{
 			if (player.isOp() || weathercontrol.Permissions.has(player, "weathercontrol.lightning.row"))
 			{
 				if (arg.length >= 1)
 				{
 					World world = player.getWorld();
 					Block targetBlock = player.getTargetBlock(null, 20);
 					if (targetBlock!=null)
 					{
 						LightningRow thread = new LightningRow();
 						Location strikeloc = targetBlock.getLocation();
 						Integer range = Integer.parseInt(arg[0]);
 				        double rot = (player.getLocation().getYaw() - 90) % 360;
 				        if (rot < 0) {
 				            rot += 360.0;
 				        }
 						PlayerDirection d = getDirection(rot);
 						if(d==PlayerDirection.SOUTH)
 							thread.face=BlockFace.SOUTH;
 						else if(d==PlayerDirection.SOUTH_WEST)
 							thread.face=BlockFace.SOUTH_WEST;
 						else if(d==PlayerDirection.SOUTH_EAST)
 							thread.face=BlockFace.SOUTH_EAST;
 						else if(d==PlayerDirection.NORTH)
 							thread.face=BlockFace.NORTH;
 						else if(d==PlayerDirection.NORTH_WEST)
 							thread.face=BlockFace.NORTH_WEST;
 						else if(d==PlayerDirection.NORTH_EAST)
 							thread.face=BlockFace.NORTH_EAST;
 						else if(d==PlayerDirection.EAST)
 							thread.face=BlockFace.EAST;
 						else if(d==PlayerDirection.WEST)
 							thread.face=BlockFace.WEST;
 						thread.setRange(range);
 						thread.setStart(strikeloc);
 						thread.setCurrent(thread.getStart());
 						thread.setWorld(world);
 						thread.setPlugin(this);
 						thread.id=getServer().getScheduler().scheduleSyncRepeatingTask(this, thread, 1, 5);
 
 					}
 					else
 					{
 						player.sendMessage("No block in sight");
 					}					
 				}
 				else
 				{
 					player.sendMessage("Correct usage is /strikerow [distance]");
 				}
 			}
 		}
 		return true;
 	}
 
     /**
      * Returns direction according to rotation. May return null.
      * Respect goes to sk89q <http://www.sk89q.com> for figuring this out
      * @param rot
      * @return
      */
     private static PlayerDirection getDirection(double rot) {
         if (0 <= rot && rot < 22.5) {
             return PlayerDirection.NORTH;
         } else if (22.5 <= rot && rot < 67.5) {
             return PlayerDirection.NORTH_EAST;
         } else if (67.5 <= rot && rot < 112.5) {
             return PlayerDirection.EAST;
         } else if (112.5 <= rot && rot < 157.5) {
             return PlayerDirection.SOUTH_EAST;
         } else if (157.5 <= rot && rot < 202.5) {
             return PlayerDirection.SOUTH;
         } else if (202.5 <= rot && rot < 247.5) {
             return PlayerDirection.SOUTH_WEST;
         } else if (247.5 <= rot && rot < 292.5) {
             return PlayerDirection.WEST;
         } else if (292.5 <= rot && rot < 337.5) {
             return PlayerDirection.NORTH_WEST;
         } else if (337.5 <= rot && rot < 360.0) {
             return PlayerDirection.NORTH;
         } else {
             return null;
         }
     }
     /**
      * Direction.
      * 
      */
     public enum PlayerDirection {
         NORTH(new Vector(-1, 0, 0), new Vector(0, 0, 1), true),
         NORTH_EAST((new Vector(-1, 0, -1)).normalize(), (new Vector(-1, 0, 1)).normalize(), false),
         EAST(new Vector(0, 0, -1), new Vector(-1, 0, 0), true),
         SOUTH_EAST((new Vector(1, 0, -1)).normalize(), (new Vector(-1, 0, -1)).normalize(), false),
         SOUTH(new Vector(1, 0, 0), new Vector(0, 0, -1), true),
         SOUTH_WEST((new Vector(1, 0, 1)).normalize(), (new Vector(1, 0, -1)).normalize(), false),
         WEST(new Vector(0, 0, 1), new Vector(1, 0, 0), true),
         NORTH_WEST((new Vector(-1, 0, 1)).normalize(), (new Vector(1, 0, 1)).normalize(), false);
         
         private Vector dir;
         private Vector leftDir;
         private boolean isOrthogonal;
         
         PlayerDirection(Vector vec, Vector leftDir, boolean isOrthogonal) {
             this.dir = vec;
             this.leftDir = leftDir;
             this.isOrthogonal = isOrthogonal;
         }
         
         public Vector vector() {
             return dir;
         }
         
         public Vector leftVector() {
             return leftDir;
         }
         
         public boolean isOrthogonal() {
             return isOrthogonal;
         }
     }
 }
