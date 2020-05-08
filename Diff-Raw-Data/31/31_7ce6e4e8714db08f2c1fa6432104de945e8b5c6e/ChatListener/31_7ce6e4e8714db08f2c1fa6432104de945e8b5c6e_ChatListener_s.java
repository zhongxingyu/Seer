 package me.ultimate.E.ChatManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatListener implements Listener {
 
     @EventHandler
     public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
         Player p = event.getPlayer();
         if (!p.hasPermission("events.chat")) {
            event.setFormat(t("&7&l" + p.getCustomName() + " >> &r&e" + event.getMessage()));
         } else {
            event.setFormat(t("&c" + p.getCustomName() + " &4&l>> &r&7" + event.getMessage()));
         }
     }
     public String t(final String msg) {
         return ChatColor.translateAlternateColorCodes('&', msg);
     }
 }
