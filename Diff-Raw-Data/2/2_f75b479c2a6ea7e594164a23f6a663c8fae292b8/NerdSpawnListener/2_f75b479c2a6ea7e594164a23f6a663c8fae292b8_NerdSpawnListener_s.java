 package nu.nerd.nerdspawn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 
 public class NerdSpawnListener implements Listener
 {
     private final NerdSpawn plugin;
 
     public NerdSpawnListener(NerdSpawn instance)
     {
         plugin = instance;
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event)
     {
         if (!event.getPlayer().hasPlayedBefore()) {
         	if (plugin.getConfig().getBoolean("first-join-alternate-spawn")) {
         		event.getPlayer().teleport(plugin.getFirstSpawnLocation());
         	} else {
         		event.getPlayer().teleport(plugin.getSpawnLocation());
         	}
             if (plugin.getConfig().getBoolean("show-join-message")) {
                 String message = plugin.getConfig().getString("join-message");
                 message = message.replaceAll("%u", event.getPlayer().getName());
                 for (ChatColor c : ChatColor.values())
                    message = message.replaceAll("&" + Integer.toHexString(c.getChar()), c.toString());
 
                 plugin.getServer().broadcastMessage(message);
             }
         }
     }
         
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event)
     {
         if (!event.isBedSpawn() || !plugin.getConfig().getBoolean("allow-bed-spawn"))
             event.setRespawnLocation(plugin.getSpawnLocation());
     }
 }
