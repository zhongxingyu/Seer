 package me.ultimate.E.ChatManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
import me.ultimate.E.Events;

 public class ChatListener implements Listener {
 
    Events Events = new Events();

     @EventHandler
     public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
         Player p = event.getPlayer();
        String prefix = null;
        String suffix = null;
        for (String perm : Events.getConfig().getConfigurationSection("Prefixes").getKeys(false)) {
            if (p.hasPermission(Events.getConfig().getString(perm + ".Permission")))
                prefix = Events.getConfig().getString(perm + ".Prefix");
        }
        for (String perm : Events.getConfig().getConfigurationSection("Suffixes").getKeys(false)) {
            if (p.hasPermission(Events.getConfig().getString(perm + ".Permission")))
                suffix = Events.getConfig().getString(perm + ".Suffix");
        }
         if (!p.hasPermission("events.chat")) {
            event.setFormat(t(prefix + p.getCustomName() + "&r" + suffix + " &lx>> &r&e" + event.getMessage()));
         } else {
            event.setFormat(t(suffix + "&c&l" + p.getCustomName() + "&r" + suffix + " &4&l>> &r&7" + event.getMessage()));
         }
     }

     public String t(final String msg) {
         return ChatColor.translateAlternateColorCodes('&', msg);
     }
 }
