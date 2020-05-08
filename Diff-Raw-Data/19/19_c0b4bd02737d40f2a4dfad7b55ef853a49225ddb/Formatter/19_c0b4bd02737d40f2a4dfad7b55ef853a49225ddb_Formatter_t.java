 package edgruberman.bukkit.messageformatter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.messageformatter.util.PermissionCache;
 
 /** format displayed messages according to plugin configuration */
 final class Formatter implements Listener {
 
     private final Plugin plugin;
     private boolean hideNextQuit = false;
     private final PermissionCache permissions;
 
     Formatter(final Plugin plugin, final long asyncPermissionCache) {
         this.plugin = plugin;
         this.permissions = new PermissionCache(plugin, asyncPermissionCache, "messageformatter.colors");
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerLogin(final PlayerLoginEvent login) {
         if (login.getResult() == Result.ALLOWED) return;
 
         final String reason = Main.courier.format("login." + login.getResult().name() + ".+reason", login.getKickMessage());
         Main.courier.broadcast("login." + login.getResult().name() + ".broadcast", Main.formatSender(login.getPlayer()), reason);
         login.setKickMessage(reason);
     }
 
    @EventHandler(priority = EventPriority.HIGHEST) // process as late as possible
     public void onPlayerJoin(final PlayerJoinEvent join) {
         if (join.getJoinMessage() == null) return;
 
         Main.courier.broadcast("join", Main.formatSender(join.getPlayer()));
         join.setJoinMessage(null);
     }
 
     @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // LOW to avoid plugins duplicating the processing of AsyncPlayerChatEvent
     public void onPlayerChat(final AsyncPlayerChatEvent chat) {
         if (chat instanceof AsyncFormatChat) return;
 
         final AsyncFormatChat custom = new AsyncFormatChat(chat.isAsynchronous(), chat.getPlayer(), chat.getMessage(), chat.getRecipients());
         Bukkit.getServer().getPluginManager().callEvent(custom);
         if (custom.isCancelled()) return;
 
         if (this.permissions.hasPermission(custom.getPlayer().getName(), "messageformatter.colors"))
             custom.setMessage(ChatColor.translateAlternateColorCodes('&', custom.getMessage()));
 
         Main.courier.broadcast("chat", Main.formatSender(custom.getPlayer()), custom.getMessage());
         chat.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerDeath(final PlayerDeathEvent death) {
         if (death.getDeathMessage() == null) return;
 
         Main.courier.broadcast("death", Main.formatSender(death.getEntity()), death.getDeathMessage());
         death.setDeathMessage(null);
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerKick(final PlayerKickEvent kick) {
         if (!this.plugin.getConfig().getBoolean("quitAfterKick")) this.hideNextQuit = true;
         if (kick.getLeaveMessage() == null) return;
 
         final String reason = Main.courier.format("kick.+reason", kick.getReason());
         Main.courier.broadcast("kick.broadcast", Main.formatSender(kick.getPlayer()), reason);
         kick.setReason(reason);
         kick.setLeaveMessage(null);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerKickMonitor(final PlayerKickEvent kick) {
         // if kick occurred, leave next quit hidden as configured
         if (!kick.isCancelled()) return;
 
         // kick did not occur, do not cancel next quit message
         this.hideNextQuit = false;
         return;
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerQuit(final PlayerQuitEvent quit) {
         if (this.hideNextQuit) {
             quit.setQuitMessage(null);
             this.hideNextQuit = false;
             return;
         }
 
         if (quit.getQuitMessage() == null) return;
 
         Main.courier.broadcast("quit", Main.formatSender(quit.getPlayer()));
         quit.setQuitMessage(null);
     }
 
 }
