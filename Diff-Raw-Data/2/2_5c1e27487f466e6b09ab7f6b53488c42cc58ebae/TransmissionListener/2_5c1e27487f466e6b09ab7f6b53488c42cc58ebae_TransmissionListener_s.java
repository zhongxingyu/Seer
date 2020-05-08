 package at.junction.transmission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class TransmissionListener implements Listener {
     private Transmission plugin;
 
     public TransmissionListener (Transmission instance) {
         plugin = instance;
     }
     
     @EventHandler
     public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if(plugin.staffChatters.contains(event.getPlayer().getName())) {
             event.getRecipients().clear();
             for(Player p : plugin.getServer().getOnlinePlayers()) {
                 if(p.hasPermission("transmission.staffchat")) {
                     event.getRecipients().add(p);
                 }
             }
             event.setFormat(ChatColor.DARK_AQUA + "[STAFF]<" + ChatColor.WHITE + "%1$s" + ChatColor.DARK_AQUA + "> " + ChatColor.RESET + "%2$s");
         }
     }
 }
