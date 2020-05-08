 package de.codeinfection.quickwango.Announcer;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class Announcer extends JavaPlugin
 {
     protected static final Logger log = Logger.getLogger("Minecraft");
     public static boolean debugMode = false;
     protected static PermissionHandler permissionHandler = null;
     
     protected Server server;
     protected PluginManager pm;
     protected Configuration config;
     protected BukkitScheduler scheduler;
     protected File dataFolder;
 
     protected boolean instantStart = false;
 
     public void onEnable()
     {
         this.server = this.getServer();
         this.pm = this.server.getPluginManager();
         this.config = this.getConfiguration();
         this.scheduler = this.server.getScheduler();
         this.dataFolder = this.getDataFolder();
 
         this.dataFolder.mkdirs();
         // Create default config if it doesn't exist.
         if (!(new File(this.dataFolder, "config.yml")).exists())
         {
             this.defaultConfig();
         }
         this.loadConfig();
 
         debugMode = this.config.getBoolean("debug", debugMode);
         this.instantStart = this.config.getBoolean("instantStart", this.instantStart);
         Pattern pattern = Pattern.compile("^(\\d+)([tsmhd])?$", Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(this.config.getString("interval", "15"));
         matcher.find();
         int interval = 0;
         try
         {
             interval = Integer.valueOf(String.valueOf(matcher.group(1)));
         }
         catch (NumberFormatException e)
         {
             error("The given interval was invalid!", e);
             return;
         }
         String unitSuffix = matcher.group(2);
         if (unitSuffix == null)
         {
             unitSuffix = "m";
         }
         switch (unitSuffix.toLowerCase().charAt(0))
         {
             case 'd':
                 interval *= 24;
             case 'h':
                 interval *= 60;
             case 'm':
                 interval *= 60;
             case 's':
                 interval *= 20;
         }
 
         debug("Calculated a interval of " + interval + " ticks");
 
        this.getCommand("announce").setExecutor(new AnnounceCommand(this.server, this.dataFolder));
        this.getCommand("reloadannouncer").setExecutor(new ReloadannouncerCommand(this));

         Permissions permissions = (Permissions)this.pm.getPlugin("Permissions");
         if (permissions != null)
         {
             permissionHandler = permissions.getHandler();
         }
 
         try
         {
             AnnouncerTask task = new AnnouncerTask(server, dataFolder);
 
             debug("Start instantly? - " + String.valueOf(this.instantStart));
             if (this.scheduler.scheduleAsyncRepeatingTask(this, task, (this.instantStart ? 0 : interval), interval) < 0)
             {
                 error("Failed to schedule the announcer task!");
                 return;
             }
         }
         catch (IOException e)
         {
             error("An error occurred while reading the message files:");
             error(e.getLocalizedMessage());
             return;
         }
 
         System.out.println(this.getDescription().getName() + " (v" + this.getDescription().getVersion() + ") enabled");
     }
 
     public void onDisable()
     {
         this.scheduler.cancelTasks(this);
         System.out.println(this.getDescription().getName() + " Disabled");
     }
 
     private void loadConfig()
     {
         this.config.load();
     }
 
     private void defaultConfig()
     {
         this.config.setProperty("interval", "15");
         this.config.setProperty("instantStart", this.instantStart);
         this.config.setProperty("debug", debugMode);
 
         this.config.save();
     }
 
     public static void log(String msg)
     {
         log.log(Level.INFO, "[Announcer] " + msg);
     }
 
     public static void error(String msg)
     {
         log.log(Level.SEVERE, "[Announcer] " + msg);
     }
 
     public static void error(String msg, Throwable t)
     {
         log.log(Level.SEVERE, "[Announcer] " + msg, t);
     }
 
     public static void debug(String msg)
     {
         if (debugMode)
         {
             log("[debug] " + msg);
         }
     }
 
     public static boolean has(Player player, String permission)
     {
         if (player.isOp())
         {
             return true;
         }
         else if (permissionHandler != null)
         {
             return permissionHandler.has(player, permission);
         }
         else
         {
             return player.hasPermission(permission);
         }
     }
 }
