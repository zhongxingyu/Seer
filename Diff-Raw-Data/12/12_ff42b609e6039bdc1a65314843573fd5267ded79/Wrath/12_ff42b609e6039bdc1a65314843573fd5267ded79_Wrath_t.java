 package me.ryall.wrath;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import me.ryall.wrath.communication.CommunicationManager;
 import me.ryall.wrath.execution.ExecutionManager;
 import me.ryall.wrath.execution.Executioner;
 import me.ryall.wrath.listeners.EntityListener;
 import me.ryall.wrath.listeners.ServerListener;
 import me.ryall.wrath.settings.ConfigManager;
 import me.ryall.wrath.settings.PermissionManager;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Wrath extends JavaPlugin
 {
     public static String PLUGIN_NAME = "Wrath";
     public static String LOG_HEADER = "[" + PLUGIN_NAME + "] ";
     private static Wrath instance = null;
     
     private Logger log;
     private ServerListener serverListener;
     private EntityListener entityListener;
     private ConfigManager configManager;
     private PermissionManager permissionManager;
     private CommunicationManager communicationManager;
     
     public static Wrath get()
     {
         return instance;
     }
     
     public void onEnable()
     {
         instance = this;
         log = Logger.getLogger("Minecraft");
         
         serverListener = new ServerListener();
         entityListener = new EntityListener();
         
         configManager = new ConfigManager();
         permissionManager = new PermissionManager();
         communicationManager = new CommunicationManager();
         
         registerEvents();
         
         //getServer().getScheduler().scheduleSyncRepeatingTask(this, update, 0, 0);
         
         logInfo("Started");
     }
 
     public void onDisable()
     {
         logInfo("Stopped");
     }
     
     public void registerEvents()
     {
         PluginManager pm = getServer().getPluginManager();
         
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Highest, this);
     }
     
     public boolean onCommand(CommandSender _sender, Command _command, String _label, String[] _args)
     {
         Player player = (_sender instanceof Player) ? (Player)_sender : null;
         
         if (_label.equals("wrath"))
         {
             // We need at least two params to successfully execute a command.
             if (_args.length >= 2)
             {
                 String commandName = _args[0];
                 String playerName = _args[1];
                 ArrayList<String> flags = new ArrayList<String>();
                 
                 // See if we have any "special" flags.
                 for (int i = 2; i < _args.length; i++)
                 {
                     flags.add(_args[i]);
                 }
                 
                 // Check the args against the system and execute the target if we can.
                 Executioner executioner = ExecutionManager.getExecutioner(commandName);
                 
                 if (executioner != null)
                 {
                     Player target = findPlayer(playerName);
                     
                     if (target != null)
                     {
                         if (executioner.hasPermission(player))
                         {
                             if (!ExecutionManager.isExecuting(target))
                             {
                                 ExecutionManager.execute(executioner, player, target, flags);
                             }
                             else
                                 communicationManager.error(player, target.getName() + " is already dying a slow horrible death.");
                         }
                         else
                             communicationManager.error(player, "You don't have permission to use the '" + commandName + "' command.");
                     }
                     else
                         communicationManager.error(player, "Could not find the player '" + playerName + "'.");
                 }
                 else
                     communicationManager.error(player, "Could not execute the command '" + commandName + "'.");
             } 
             // If we don't supply enough arguments, show the help.
             else
             {
                 if (player != null)
                 {
                     if (permissionManager.hasStrikePermission(player))
                         communicationManager.command(player, "/wrath strike <player>", "Kill a player with lightning");
                 }
             }
         
             return true;
         }
         
         return false;
     }
     
     public Player findPlayer(String _name)
     {
         for (World world : getServer().getWorlds()) 
         {
             for (Player player : world.getPlayers())
             {
                 if (player.getName().equalsIgnoreCase(_name))
                     return player;
             }
         }
         
         return null;
     }
 
     public ConfigManager getConfig()
     {
         return configManager;
     }
     
     public PermissionManager getPermissions()
     {
         return permissionManager;
     }
     
     public CommunicationManager getComms()
     {
         return communicationManager;
     }
     
     public void logInfo(String _message)
     {
         log.info(LOG_HEADER + _message);
     }
     
     public void logError(String _message)
     {
         log.severe(LOG_HEADER + _message);
     }
 }
