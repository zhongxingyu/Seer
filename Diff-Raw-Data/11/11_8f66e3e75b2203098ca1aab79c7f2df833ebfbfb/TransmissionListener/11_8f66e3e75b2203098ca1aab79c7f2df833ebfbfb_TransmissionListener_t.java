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
         if(plugin.staffChatters.contains(ChatColor.stripColor(event.getPlayer().getName()))) {
             event.getRecipients().clear();
             for(Player p : plugin.getServer().getOnlinePlayers()) {
                 if(p.hasPermission("transmission.staffchat")) {
                     event.getRecipients().add(p);
                 }
             }
            event.setFormat(ChatColor.DARK_AQUA + "[S]<" + ChatColor.WHITE + "%1$s" + ChatColor.DARK_AQUA + "> " + ChatColor.RESET + "%2$s");
            
        } else if ((plugin.getServer().getPluginManager().getPlugin("Tier2") != null) && 
                (event.getPlayer() instanceof Player) &&
                (event.getPlayer().hasMetadata("assistance"))){
        
            if (!event.getPlayer().getDisplayName().equals(event.getPlayer().getName())){
                event.setCancelled(true);
                plugin.getServer().broadcastMessage("<" + event.getPlayer().getName() + "> " + event.getMessage());
            }
         }
     }
 }
