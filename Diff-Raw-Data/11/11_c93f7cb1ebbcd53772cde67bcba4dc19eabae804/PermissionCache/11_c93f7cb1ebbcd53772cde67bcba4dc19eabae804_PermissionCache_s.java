 package edgruberman.bukkit.messageformatter;
 
 import java.util.Arrays;
 import java.util.Collection;
import java.util.HashSet;
 import java.util.Map;
import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 import com.google.common.collect.ImmutableSet;
 
 /** thread-safe hasPermission cache manager */
 public class PermissionCache implements Listener, Runnable {
 
     private final Plugin plugin;
 
     /** permission to players that have the permission set true */
     private final Map<String, ImmutableSet<String>> cache = new ConcurrentHashMap<String, ImmutableSet<String>>();
 
     private long period = -1;
     private long updated = -1;
     private int taskId = -1;
 
     public PermissionCache(final Plugin plugin) {
         this.plugin = plugin;
         Bukkit.getPluginManager().registerEvents(this, plugin);
     }
 
     public PermissionCache(final Plugin plugin, final long period, final Collection<String> permissions) {
         this(plugin);
         for (final String permission : permissions) this.put(permission);
         this.run(); // ensure cache is updated for any existing players at instantiation
         this.setPeriod(period);
     }
 
     public PermissionCache(final Plugin plugin, final long period, final String permission) {
         this(plugin, period, Arrays.asList(permission));
     }
 
     /**
      * configure refresh period and start/update/cancel schedule
      *
      * @param period server ticks before refreshing cache
      */
     public void setPeriod(final long period) {
         this.period = period;
         this.schedule();
     }
 
     /** server ticks before refreshing cache */
     public long getPeriod() {
         return this.period;
     }
 
     /** the difference, measured in milliseconds, between the last refresh and midnight, January 1, 1970 UTC */
     public long getUpdated() {
         return this.updated;
     }
 
     /** start refreshing automatically; updates existing schedule */
     public void schedule() {
         if (Bukkit.getServer().getOnlinePlayers().length == 0) return;
 
         if (this.taskId != -1) Bukkit.getScheduler().cancelTask(this.taskId);
         this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this, this.period, this.period);
     }
 
     /** stop cache from refreshing automatically */
     public void cancel() {
         this.period = -1;
         if (this.taskId == -1) return;
 
         Bukkit.getScheduler().cancelTask(this.taskId);
         this.taskId = -1;
     }
 
     /** add a permission to be tracked and update all cached permissions */
     public ImmutableSet<String> put(final String permission) {
         return this.cache.put(permission, ImmutableSet.<String>of());
     }
 
     /** remove a permission from being cached */
     public ImmutableSet<String> remove(final String permission) {
         return this.cache.remove(permission);
     }
 
     /** update all cached permissions */
     @Override
     public void run() {
         this.updated = System.currentTimeMillis();
 
         for (final Map.Entry<String, ImmutableSet<String>> entry : this.cache.entrySet()) {
            final Set<String> have = new HashSet<String>();
             for (final Player player : Bukkit.getOnlinePlayers()) {
                 if (player.hasPermission(entry.getKey()))
                     have.add(player.getName());
             }
            entry.setValue(ImmutableSet.copyOf(have));
         }
 

     }
 
     /**
      * values cached from Bukkit's Player.hasPermission(String permission);
      * thread-safe, accurate as of {@link #getUpdated()};
      * NPE will result if permission has not been put into cache already
      */
     public boolean hasPermission(final String player, final String permission) {
         return this.cache.get(permission).contains(player);
     }
 
     @EventHandler
     public void onPlayerJoin(final PlayerJoinEvent join) {
         this.run();
 
         // restart automatic cache refreshing if configured and not running already
         if (this.taskId == -1 && this.period > 0) this.schedule();
     }
 
     @EventHandler
     public void onPlayerQuit(final PlayerQuitEvent quit) {
         // pause cache refreshing when no players online
         if (Bukkit.getServer().getOnlinePlayers().length == 0)
             Bukkit.getScheduler().cancelTask(this.taskId);
     }
 
 }
