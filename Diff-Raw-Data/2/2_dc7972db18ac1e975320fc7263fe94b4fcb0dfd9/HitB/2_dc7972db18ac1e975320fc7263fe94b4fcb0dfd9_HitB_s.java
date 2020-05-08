 package they4kman.hitb;
 
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * BlockCraft for Bukkit
  *
  * @author Zach "theY4Kman" Kanzler
  */
 public class HitB extends JavaPlugin
 {
     private final HitBPlayerListener playerListener = new HitBPlayerListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    
 
     public void onEnable()
     {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_CHAT,
            new BlockCraftPlayerListener(this), Priority.Low, this);
     }
     
     public void onDisable()
     {
     }
     
     public boolean isDebugging(final Player player)
     {
         if (debugees.containsKey(player))
             return debugees.get(player);
         else
             return false;
     }
 
     public void setDebugging(final Player player, final boolean value)
     {
         debugees.put(player, value);
     }
 }
 
