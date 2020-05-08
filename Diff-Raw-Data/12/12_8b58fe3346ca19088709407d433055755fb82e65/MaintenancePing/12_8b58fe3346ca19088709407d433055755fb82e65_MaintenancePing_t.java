 package net.daboross.bungeedev.ncommon.listeners;
 
 import java.net.InetSocketAddress;
 import java.util.Random;
import java.util.logging.Level;
 import lombok.RequiredArgsConstructor;
 import net.daboross.bungeedev.ncommon.config.SharedConfig;
 import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.ServerPing;
 import net.md_5.bungee.api.event.LoginEvent;
 import net.md_5.bungee.api.event.ProxyPingEvent;
 import net.md_5.bungee.event.EventHandler;
 import net.md_5.bungee.api.ServerPing.*;
 import net.md_5.bungee.api.plugin.Listener;
 
 @RequiredArgsConstructor
 public class MaintenancePing implements Listener {
 
     private static final String KICK = ChatColor.translateAlternateColorCodes('&', "&4N&5L&4M&5C&e is down for maintenance.\n&eWe'll be back soon!");
     private static final String DEF_PING1 = ChatColor.translateAlternateColorCodes('&', "&2 < &4N&5L&4M&5C&2 >");
     private static final String DEF_PING2 = ChatColor.translateAlternateColorCodes('&', "&2 < &4N&5L&4M&5C&2 | &4Check &cwww.nlmc.pw&4 for more info. &2 >");
    private static final String FORMAT = ChatColor.translateAlternateColorCodes('&', "[Ping] from: '&4%s&r', to: '&4%s&r', response: '&4%s&f'");
     private final SharedConfig config;
     private final Random r = new Random();
 
     @EventHandler
     public void onLogin(LoginEvent evt) {
         evt.setCancelled(true);
         evt.setCancelReason(config.getString("maintenance.kick", KICK));
     }
 
     @EventHandler
     public void onPing(ProxyPingEvent evt) {
         ServerPing ping = evt.getResponse();
         ping.setPlayers(new Players(0, 0, getPlayers(config.getString("ping.players", "Testing this\nPlayer List\nIs fun").split("\n"))));
         String[] descriptions;
        InetSocketAddress hostAddress = evt.getConnection().getVirtualHost();
        String host = hostAddress == null ? null : hostAddress.getHostString();
         if (host != null && !host.contains("nlmc")) {
             descriptions = config.getString("ping.notusingnlmc", DEF_PING2).split("\n");
         } else if (config.getBoolean("ping.maintenance", false)) {
             String[] versions = config.getString("ping.version", "1.7.5").split("\n");
             ping.setVersion(new Protocol(versions[r.nextInt(versions.length)], Integer.MAX_VALUE));
             descriptions = config.getString("ping.ping2", DEF_PING2).split("\n");
         } else {
             descriptions = config.getString("ping.ping1", DEF_PING1).split("\n");
         }
         String description = descriptions[r.nextInt(descriptions.length)];
        ProxyServer.getInstance().getLogger().log(Level.INFO, String.format(FORMAT, evt.getConnection().getAddress().getHostString(), host, description));
         ping.setDescription(description);
     }
 
     private PlayerInfo[] getPlayers(String... names) {
         PlayerInfo[] result = new PlayerInfo[names.length];
         for (int i = 0; i < result.length; i++) {
             result[i] = new PlayerInfo(names[i], "");
         }
         return result;
     }
 }
