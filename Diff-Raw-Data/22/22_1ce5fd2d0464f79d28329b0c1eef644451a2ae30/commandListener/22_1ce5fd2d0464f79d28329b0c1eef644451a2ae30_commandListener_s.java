 package com.codisimus.plugins.repeat.listeners;
 
 import com.codisimus.plugins.repeat.Repeat;
 import java.util.Properties;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 
 /**
  * Listens for /r command and logs every other command
  *
  * @author Codisimus
  */
public class commandListener extends PlayerListener {
     public static Properties memory = new Properties();
 
     /**
      * Executes /r command with replacement and logs every other command
      * 
      * @param event the PlayerCommandPreprocessEvent that occured
      */
     @Override
     public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         String name = player.getName();
         String msg = event.getMessage();
         String[] args = msg.split(" ");
         
         if (args[0].equals("/r")) {
             //Cancel command event so an error is not displayed by Bukkit
             event.setCancelled(true);
             
             //Find the previous command
             msg = memory.getProperty(name);
             if (msg == null) {
                 event.getPlayer().sendMessage("Previous command cannot be repeated because no previous command was found");
                 return;
             }
 
             //Check if there will be replacement
             if (args.length >= 3) {
                 String newMsg = null;
                 
                 //Dispatch commands replacing the 2nd word with every word that follows
                 for (int i = 2; i < args.length; i++) {
                    newMsg = msg.replaceAll(args[1], args[i]);
                     Repeat.server.dispatchCommand(player, newMsg);
                 }
                 
                 //Update the memory with the last command that was dispatched
                 memory.setProperty(name, newMsg);
             }
             else
                 Repeat.server.dispatchCommand(player, msg);
         }
         else
             //Update the memory with the new command
             memory.setProperty(name, msg.substring(1));
     }
 }
