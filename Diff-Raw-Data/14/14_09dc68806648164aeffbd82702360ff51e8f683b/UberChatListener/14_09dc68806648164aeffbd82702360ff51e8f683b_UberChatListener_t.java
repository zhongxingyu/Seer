 package net.daboross.bungeedev.uberchat;
 
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
 
 /**
  *
  * @author Dabo Ross
  */
 public class UberChatListener implements Listener {
 
    @EventHandler
     public void onChat(ChatEvent e) {
         e.setMessage(UberChatSensor.getSensoredMessage(e.getMessage()));
         String m = e.getMessage();
         if (e.getSender() instanceof ProxiedPlayer) {
             ProxiedPlayer pl = (ProxiedPlayer) e.getSender();
             for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
                 if (!p.getServer().getInfo().getName().equals(pl.getServer().getInfo().getName())) {
                     p.sendMessage(String.format(UberChatStatics.FORMAT.CHAT, pl.getDisplayName(), m));
                 }
             }
         }
     }
 }
