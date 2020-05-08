 package edgruberman.bukkit.messageformatter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 final class PlayerListener extends org.bukkit.event.player.PlayerListener {
     
     PlayerListener(final JavaPlugin plugin) {
         org.bukkit.plugin.PluginManager pluginManager = plugin.getServer().getPluginManager();
         
        pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, this, Main.getEventPriority("PLAYER_LOGIN"), plugin);
         pluginManager.registerEvent(Event.Type.PLAYER_JOIN, this, Main.getEventPriority("PLAYER_JOIN"), plugin);
         pluginManager.registerEvent(Event.Type.PLAYER_CHAT, this, Main.getEventPriority("PLAYER_CHAT"), plugin);
         pluginManager.registerEvent(Event.Type.PLAYER_QUIT, this, Main.getEventPriority("PLAYER_QUIT"), plugin);
         pluginManager.registerEvent(Event.Type.PLAYER_KICK, this, Main.getEventPriority("PLAYER_KICK"), plugin);
     }
     
     @Override
     public void onPlayerLogin(final PlayerLoginEvent event) {
         if (event.getResult().equals(Result.ALLOWED)) return;
         
         MessageLevel level = Main.getMessageLevel(event.getType().name() + "." + event.getResult().name());
         String message = Main.getMessageFormat(event.getType().name() + "." + event.getResult().name());
         
         event.setKickMessage(message);
         Main.messageManager.log(message, level);
     }
     
     @Override
     public void onPlayerJoin(final PlayerJoinEvent event) {
         Main.messageManager.broadcast(
                 String.format(Main.getMessageFormat(event.getType().name()), event.getPlayer().getDisplayName())
                 , Main.getMessageLevel(event.getType().name())
         );
         
         event.setJoinMessage(null);
     }
     
     @Override
     public void onPlayerChat(final PlayerChatEvent event) {
         if (event.isCancelled()) return;
         
         event.setCancelled(true);
         
         PlayerChat custom = new PlayerChat(event.getPlayer(), event.getMessage());
         Bukkit.getServer().getPluginManager().callEvent(custom);
         if (custom.isCancelled()) return;
         
         String message = String.format(Main.getMessageFormat(event.getType().name()), event.getMessage(), event.getPlayer().getDisplayName());
         Main.messageManager.broadcast(message, Main.getMessageLevel(event.getType().name()));
     }
     
     @Override
     public void onPlayerQuit(final PlayerQuitEvent event) {
         Main.messageManager.broadcast(
                 String.format(Main.getMessageFormat(event.getType().name()), event.getPlayer().getDisplayName())
                 , Main.getMessageLevel(event.getType().name())
         );
         
         event.setQuitMessage(null);
     }
     
     @Override
     public void onPlayerKick(final PlayerKickEvent event) {
         if (event.isCancelled()) return;
         
         Main.messageManager.broadcast(
                 String.format(Main.getMessageFormat(event.getType().name()), event.getPlayer().getDisplayName())
                 , Main.getMessageLevel(event.getType().name())
         );
         
         event.setLeaveMessage(null);
     }
 }
