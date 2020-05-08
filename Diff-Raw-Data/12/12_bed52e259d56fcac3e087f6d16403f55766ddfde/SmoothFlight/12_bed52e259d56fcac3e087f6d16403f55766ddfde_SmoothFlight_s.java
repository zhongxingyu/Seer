 package me.cmesh.SmoothFlight;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.*;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.nijiko.permissions.PermissionHandler;
 
 public class SmoothFlight extends JavaPlugin
 {
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	public Material flyTool;
 	public double flySpeed;
 	public double flySpeedSneak;
 	public int hunger;
 	public boolean opHunger; 
 	public boolean smoke;
 	public int maxHeight;
 	public int minHeight;
 	public List<String> worlds;
 	public List<String> dreamWorlds;
     public PermissionHandler permissionsPlugin = null;
     public PermissionManager permissionsExPlugin = null;
 	
 	private SFPlayerListener listener;
 	
 	@Override
 	public void onEnable()
 	{
 		setupPermissions();
 		config();
 		listener = new SFPlayerListener(this);
 		fixLogger();
 	}
 	
 	private void config()
 	{	
 		reloadConfig();
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		FileConfiguration config = getConfig();
 		
 		try
 		{
 			flyTool = Material.getMaterial(config.getString("smoothflight.fly.tool", Material.FEATHER.name()));
 		}
 		catch (java.lang.NullPointerException e)
 		{
 			log.info("[SMOOTHFLIGHT] Could not load material: " + config.getString("smoothflight.fly.tool"));
 		}
 		
 		String base = "smoothflight.fly.";
 		
 		flySpeed = config.getDouble(base + "speed", 1.0);
 		flySpeedSneak = config.getDouble(base+"flySpeedSneak", 0.5);
 		hunger = config.getInt(base+"hunger", 20);
 		opHunger = config.getBoolean(base+"opHunger", true);
 		smoke = config.getBoolean(base+"smoke", true);
 		maxHeight = config.getInt(base+"maxHeight", 200);
 		minHeight = config.getInt(base+"minHeight", 50);
 		
 		worlds = config.getStringList(base + "worlds");
 		dreamWorlds = new ArrayList<String>();
 		saveConfig();
 	}
 	
 	//For DreamLand
 	public void addDreamWorld(String world)
 	{
 		worlds.add(world);
 		dreamWorlds.add(world);
 	}
 	
 	private void fixLogger()
 	{
 		log.setFilter(new Filter() 
 		{
             public boolean isLoggable(LogRecord record)
             {
                 if(record.getMessage().toLowerCase().endsWith(" was kicked for floating too long!"))
                 {
                 	Player p = getServer().getPlayer(record.getMessage().split(" ")[0]);
                     SFPlayer player = listener.Players.get(p.getUniqueId());
                    return !player.isFlying();
                 }
                 return true;
             }
         });
 	}
 	
 	public boolean PermissionEnabled()
 	{
 		return permissionsPlugin != null;
 	}
 	public boolean PermissionExEnabled()
 	{
 		return permissionsExPlugin != null;
 	}
 	
 	private void setupPermissions()
 	{
 		Plugin p = getServer().getPluginManager().getPlugin("Permissions");
 		if (p != null && p.isEnabled()) 
 		{
 			permissionsPlugin = ((com.nijikokun.bukkit.Permissions.Permissions)p).getHandler();
 		}
 		
 		Plugin q = getServer().getPluginManager().getPlugin("PermissionsEx");
         if (q != null && q.isEnabled()) 
         {
         	permissionsExPlugin = PermissionsEx.getPermissionManager();
         }
 	}
 }
