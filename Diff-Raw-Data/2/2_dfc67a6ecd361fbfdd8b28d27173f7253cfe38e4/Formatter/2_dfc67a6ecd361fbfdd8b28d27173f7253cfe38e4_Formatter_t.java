 package edgruberman.bukkit.messageformatter;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.messagemanager.MessageDisplay;
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
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerLogin(final PlayerLoginEvent event) {
         if (event.getResult().equals(Result.ALLOWED)) return;
 
         final MessageLevel level = Main.getMessageLevel(event.getClass().getSimpleName() + "." + event.getResult().name());
         String message = Main.getMessageFormat(event.getClass().getSimpleName() + "." + event.getResult().name());
         final List<ChatColor> color = MessageManager.getDispatcher().getChannelConfiguration(Channel.Type.PLAYER, this.plugin).getColor(level);
 
         message = String.format(message, event.getKickMessage());
         message = MessageDisplay.translate(color, message);
         event.setKickMessage(message);
     }
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent event) {
         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), Main.formatSender(event.getPlayer()));
         event.setJoinMessage(message);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerChat(final PlayerChatEvent event) {
         String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), event.getMessage(), Main.formatSender(event.getPlayer()));
         message = Main.formatColors(event.getPlayer(), message);
         event.setMessage(message);
     }
 
     @EventHandler
     public void onPlayerDeath(final PlayerDeathEvent event) {
         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), event.getDeathMessage());
         event.setDeathMessage(message);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerKick(final PlayerKickEvent event) {
         if (Formatter.cancelQuitAfterKick) Formatter.cancelNextQuit = true;
 
         final MessageLevel level = Main.getMessageLevel(event.getClass().getSimpleName());
 
         final List<ChatColor> base = MessageManager.getDispatcher().getChannelConfiguration(Channel.Type.PLAYER, this.plugin).getColor(level);
         String reason = String.format(Main.getMessageFormat(event.getClass().getSimpleName() + ".reason"), event.getReason());
         reason = MessageDisplay.translate(base, reason);
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
 
        if (event.getQuitMessage() == null) return;

         final String message = String.format(Main.getMessageFormat(event.getClass().getSimpleName()), Main.formatSender(event.getPlayer()));
         event.setQuitMessage(message);
     }
 
 }
