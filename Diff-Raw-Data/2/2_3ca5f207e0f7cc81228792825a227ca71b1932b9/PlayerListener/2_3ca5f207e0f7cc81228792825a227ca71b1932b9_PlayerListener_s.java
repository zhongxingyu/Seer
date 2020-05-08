 /**
  * dejiplus - Package: net.syamn.dejiplus.listener
  * Created: 2013/01/10 6:11:35
  */
 package net.syamn.dejiplus.listener;
 
 import net.syamn.dejiplus.ConfigurationManager;
 import net.syamn.dejiplus.Dejiplus;
 import net.syamn.dejiplus.Perms;
 import net.syamn.dejiplus.feature.GeoIP;
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 /**
  * PlayerListener (PlayerListener.java)
  * @author syam(syamn)
  */
 public class PlayerListener implements Listener{
     private final Dejiplus plugin;
     private final ConfigurationManager config;
 
     public PlayerListener(final Dejiplus plugin){
         this.plugin = plugin;
         this.config = plugin.getConfigs();
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(final PlayerJoinEvent event){
         final Player player = event.getPlayer();
         
         if (config.getUseGeoIP() && !Perms.GEOIP_HIDE.has(player)){
             final String geoMsg = GeoIP.getInstance().getGeoIpString(player, config.getUseSimpleFormatOnJoin());
            final String message = Util.coloring(config.getMessageGeoIP()).replace("%PLAYER%", player.getName().replace("%LOCATION%", geoMsg));
             
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
                 @Override
                 public void run(){
                     for (final Player p : Bukkit.getOnlinePlayers()){
                         if (Perms.GEOIP_SEND.has(p)){
                             Util.message(p, message);
                         }
                     }
                 }
             }, 1L);
         }
     }
 }
