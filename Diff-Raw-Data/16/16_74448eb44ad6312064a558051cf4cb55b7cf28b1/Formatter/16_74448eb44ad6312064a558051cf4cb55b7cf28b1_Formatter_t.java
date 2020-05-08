 package edgruberman.bukkit.messageformatter;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
import edgruberman.bukkit.messagemanager.MessageColor;
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 import edgruberman.bukkit.messagemanager.channels.Channel;
 
 /**
  * Formats messages according to the plugin's configuration.
  */
 final class Formatter implements Listener {
 
     static boolean cancelQuitAfterKick = false;
     static boolean cancelNextQuit = false;
 
     private final Plugin plugin;
 
     Formatter(final Plugin plugin) {
         // TODO Adjust event priorities according to configuration
         // for (Method method : this.getClass().getDeclaredMethods()) {
         //    if (method.getParameterTypes().length != 1 || !(method.getParameterTypes()[0].isInstance(Event.class))) continue;
         //    method.getAnnotation(EventHandler.class).
         //}
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerLogin(final PlayerLoginEvent event) {
         if (event.getResult().equals(Result.ALLOWED)) return;
 
         final MessageLevel level = Main.getMessageLevel(event.getClass().getSimpleName() + "." + event.getResult().name());
         String message = Main.getMessageFormat(event.getClass().getSimpleName() + "." + event.getResult().name());
         final ChatColor color = MessageManager.getDispatcher().getChannelConfiguration(Channel.Type.PLAYER, this.plugin).getColor(level);
 
         message = String.format(message, event.getKickMessage());
        message = MessageColor.colorize(color, message);
         event.setKickMessage(message);
     }
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent event) {
         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), Main.formatSender(event.getPlayer()));
         event.setJoinMessage(message);
     }
 
     @EventHandler
     public void onPlayerChat(final PlayerChatEvent event) {
         if (event.isCancelled()) return;
 
         String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), event.getMessage(), Main.formatSender(event.getPlayer()));
         message = Main.formatColors(event.getPlayer(), message);
         event.setMessage(message);
     }
 
     @EventHandler
     public void onPlayerKick(final PlayerKickEvent event) {
         if (event.isCancelled()) return;
 
         if (Formatter.cancelQuitAfterKick) Formatter.cancelNextQuit = true;
 
         final MessageLevel level = Main.getMessageLevel(event.getClass().getSimpleName());
 
         final ChatColor base = MessageManager.getDispatcher().getChannelConfiguration(Channel.Type.PLAYER, this.plugin).getColor(level);
         String reason = String.format(Main.getMessageFormat(event.getClass().getSimpleName() + ".reason"), event.getReason());
        reason = MessageColor.colorize(base, reason);
         event.setReason(reason);
 
         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), Main.formatSender(event.getPlayer()), reason);
         event.setLeaveMessage(message);
     }
 
     @EventHandler
     public void onPlayerQuit(final PlayerQuitEvent event) {
         if (Formatter.cancelNextQuit) {
             event.setQuitMessage(null);
             Formatter.cancelNextQuit = false;
             return;
         }
 
         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), Main.formatSender(event.getPlayer()));
         event.setQuitMessage(message);
     }
 
 }
