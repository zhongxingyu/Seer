 package com.carlgo11.preventip.player;
 
 import com.carlgo11.preventip.Main;
 import java.util.List;
 import java.util.regex.Matcher;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class CommandEvent implements Listener {
 
     Main plugin;
 
     public CommandEvent(Main plug)
     {
         super();
         this.plugin = plug;
     }
 
     @EventHandler
     public void PlayerCommand(PlayerCommandPreprocessEvent e)
     {
         if (!plugin.getConfig().getBoolean("ignore-commands")) {
             String msg = e.getMessage();
             String[] args = msg.split(" ");
             String cmd = args[0].toString();
             Player p = e.getPlayer();
             List<String> blockedcmds = plugin.getConfig().getStringList("available-cmds");
             boolean match = false;
             if (blockedcmds.contains(cmd) || blockedcmds.contains("*")) {
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
 }
