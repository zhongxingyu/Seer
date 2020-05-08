 package edgruberman.bukkit.messageformatter.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messageformatter.Main;
 import edgruberman.bukkit.messageformatter.commands.util.Action;
 import edgruberman.bukkit.messageformatter.commands.util.Context;
 import edgruberman.bukkit.messageformatter.commands.util.Parser;
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
public final class Reply extends Action implements Listener {
 
     /**
      * Index of last private messages sent From, To.
      */
     static Map<CommandSender, CommandSender> last = new HashMap<CommandSender, CommandSender>();
 
     public Reply(final JavaPlugin plugin) {
         super(plugin, "reply");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @Override
     public boolean perform(final Context context) {
         if (context.arguments.size() < 1) return false;
 
        CommandSender recipient = Reply.last.get(context.sender);
         if (recipient == null || (recipient instanceof Player && !((Player) recipient).isOnline())) {
             Main.messageManager.send(context.sender, "Unable to send reply; Last sender not found", MessageLevel.WARNING, false);
             return true;
         }
 
        if (recipient instanceof Player) {
            // Ensure latest instance of CommandSender is used for players after they reconnect
            recipient = this.handler.command.getPlugin().getServer().getPlayerExact(recipient.getName());
        }

         String message = Parser.join(context.arguments.subList(0, context.arguments.size())).trim();
         message = Main.formatColors(context.sender, message);
         Tell.send(recipient, context.sender, message);
         return true;
     }
 
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent quit) {
        Reply.last.remove(quit.getPlayer());
    }

 }
