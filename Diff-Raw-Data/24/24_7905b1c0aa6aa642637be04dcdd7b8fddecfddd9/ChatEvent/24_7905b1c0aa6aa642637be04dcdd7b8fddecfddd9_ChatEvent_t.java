 package com.carlgo11.preventip.player;
 
 import com.carlgo11.preventip.Main;
 import java.util.regex.Matcher;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatEvent implements Listener {
 
     Main plugin;
 
     public ChatEvent(Main plug)
     {
         super();
         this.plugin = plug;
     }
 
     @EventHandler
     public void PlayerChat(AsyncPlayerChatEvent e)
     {
         if (!plugin.getConfig().getBoolean("ignore-chat")) {
             Player p = e.getPlayer();
             String msg = e.getMessage();
             String[] args = msg.split(" ");
             Boolean match = false;
             if (!p.hasPermission("preventip.ignore")) {
                 for (int i = 0; i < args.length; i++) {
                     Matcher ipre = plugin.ipPattern.matcher(args[i].toString());
                     Matcher hnre = plugin.hostnamePattern.matcher(args[i].toString());
                     Matcher hpre = plugin.httpPattern.matcher(args[i].toString());
                    if (hnre.find() && plugin.blockhostname) {
                         if (!hpre.find() || plugin.ignorehttp) {
                             match = true;
                             break;
                         }
                     }
                    if (ipre.find() && plugin.blockip) {
                         match = true;
                     }
                 }
                 if (match) {
                     e.setCancelled(true);
                     Action.action(p, plugin);
                 }
             }
         }
     }
 }
