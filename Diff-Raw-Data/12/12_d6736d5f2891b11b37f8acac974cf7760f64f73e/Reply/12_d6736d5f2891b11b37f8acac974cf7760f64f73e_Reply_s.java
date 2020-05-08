 package edgruberman.bukkit.messageformatter.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.messageformatter.Main;
 
 public final class Reply implements CommandExecutor, Listener {
 
     private final Plugin plugin;
     private final Map<CommandSender, CommandSender> fromTo = new HashMap<CommandSender, CommandSender>();
 
     public Reply(final Plugin plugin) {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     // usage: /<command> <Message>
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         if (args.length < 1) {
             Main.messenger.tell(sender, "requiresParameter", "<Message>");
             return false;
         }
 
         CommandSender recipient = this.fromTo.get(sender);
         if (recipient == null) {
             Main.messenger.tell(sender, "replyNotAvailable");
             return true;
         }
 
         final String name = recipient.getName();
        if (recipient instanceof Player && !((Player) recipient).isOnline()) {
             // Ensure latest instance of CommandSender is used for players after they reconnect
             recipient = Bukkit.getPlayerExact(name);
         }
 
         if (recipient == null) {
             Main.messenger.tell(sender, "playerNotFound", name);
             return true;
         }
 
         this.send(recipient, sender, Main.formatColors(sender, args));
         return true;
     }
 
     void send(final CommandSender recipient, final CommandSender sender, final String message) {
         final String senderFormatted = Main.formatSender(sender);
         final String recipientFormatted = Main.formatSender(recipient);
 
         final Level level = (sender instanceof ConsoleCommandSender || recipient instanceof ConsoleCommandSender ? Level.FINEST : Level.FINER);
         this.plugin.getLogger().log(level, "#TELL(" + sender.getName() + ">" + recipient.getName() + ")# " + message);
 
        for (final String format : Main.messenger.getFormatList("tell.sender"))
            Main.messenger.tellMessage(sender, format, senderFormatted, recipientFormatted, message);

         for (final String format : Main.messenger.getFormatList("tell.recipient"))
             Main.messenger.tellMessage(recipient, format, senderFormatted, recipientFormatted, message);
 
         this.fromTo.put(sender, recipient);
         this.fromTo.put(recipient, sender);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(final PlayerQuitEvent quit) {
         this.fromTo.remove(quit.getPlayer());
     }
 
 }
