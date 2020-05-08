 package to.joe.j2mc.core.util;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ThreadSafePermissionTracker implements Listener, Runnable {
 
     private JavaPlugin plugin;
     private String perm;
     private Set<String> havingPerm;
 
     public ThreadSafePermissionTracker(JavaPlugin plugin, String permission) {
         this.plugin = plugin;
         this.perm = permission;
         this.havingPerm = Collections.synchronizedSet(new HashSet<String>());

         this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 40, 40);
     }
 
     public boolean hasPermission(String player) {
         return this.havingPerm.contains(player);
     }
 
    public boolean hasPermission(Player player) {
        return this.havingPerm.contains(player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
     public void onJoin(PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission(perm)) {
             this.havingPerm.add(event.getPlayer().getName());
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onQuit(PlayerQuitEvent event) {
         this.havingPerm.remove(event.getPlayer().getName());
     }
 
     @Override
     public void run() {
         for (Player player : plugin.getServer().getOnlinePlayers()) {
             if (player.hasPermission(perm)) {
                 this.havingPerm.add(player.getName());
             } else {
                 this.havingPerm.remove(player.getName());
             }
         }
     }
 
 }
