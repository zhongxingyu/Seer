 package net.bot2k3.siebe.Mineplayers;
 
 import java.util.*;
 
 import net.bot2k3.siebe.*;
 
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.plugin.java.*;
 import org.bukkit.scheduler.*;
 
 /**
  * Provides the main plugin interface.
  */
 public class MineplayersPlugin extends JavaPlugin implements Listener
 {
     private HashMap<String, Boolean> afkPlayers;
     private HashMap<String, Boolean> afkPlayersAuto;
     private HashMap<String, GregorianCalendar> afkLastSeen;
     
     /**
      * Occurs when the plugin is being enabled.
      */
     public void onEnable()
     {
         Server server = this.getServer();
     
         this.afkLastSeen = new HashMap<String, GregorianCalendar>();
         this.afkPlayers = new HashMap<String, Boolean>();
         this.afkPlayersAuto = new HashMap<String, Boolean>();
         
         // register for events
         server.getPluginManager().registerEvents(this, this);
                
         // register the idle timer.
         server.getScheduler().scheduleSyncRepeatingTask(
             this,
             new MineplayersIdleTimer(this),
             600,
             600);
     }
 
     /**
      * Occurs when the plugin is being disabled.
      */
     public void onDisable()
     {
     }
     
     /**
      * Occurs when a command has been sent.
      */
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         // get the player from the server.
         String playerName = sender.getName(); 
         Player player = this.getServer().getPlayerExact(playerName);
         
         if (player != null)
         {
             String name = command.getName();
             if (name.equals("afk")) 
             {
                 this.handleAFKCommand(player, playerName);
                 return true;
             }
             else if (name.equals("list"))
             {
                 this.handleListCommand(player);
                 return true;
             }
         }
 
         return false;
     }
     
     /**
      * Occurs when the idle timer ticks.
      */
     public void onIdleTimerTick()
     {
         GregorianCalendar now = new GregorianCalendar();
         
         // any player we haven't seen for 3 minutes is now AFK.
         now.roll(Calendar.MINUTE, -3);
         
         Server server = this.getServer();
         Player[] players = server.getOnlinePlayers();
         
         for (int i = 0; i < players.length; i++)
         {
             String playerName = players[i].getName();
             
            if (this.afkLastSeen.containsKey(playerName) && this.afkLastSeen.get(playerName).after(now))
             {
                 if (this.afkPlayersAuto.containsKey(playerName) && this.afkPlayersAuto.get(playerName))
                 {
                     // this player is no longer AFK.
                     this.afkPlayers.put(playerName, false);
                     this.afkPlayersAuto.put(playerName, false);
                     
                     server.broadcastMessage(ChatColor.YELLOW + playerName + " is no longer away.");
                 }
             }
             else
             {
                 if (!this.afkPlayers.containsKey(playerName) || !this.afkPlayers.get(playerName))
                 {
                     // mark this player as AFK, broadcast the message.
                     this.afkPlayers.put(playerName, true);
                     this.afkPlayersAuto.put(playerName, true);
                     
                     server.broadcastMessage(ChatColor.YELLOW + playerName + " is now away (idle).");
                 }
             }
         }
     }
     
     /**
      * Occurs when a player joins.
      */
 	@EventHandler
     public void onPlayerJoinEvent(PlayerJoinEvent e)
     {
         Player player = e.getPlayer();
         String playerName = player.getName();
     
         this.afkPlayers.put(playerName, false);
         this.afkPlayersAuto.put(playerName, false);
         this.afkLastSeen.put(playerName, new GregorianCalendar());
         
         player.sendMessage(this.getListMessage());
     }
     
     /**
      * Occurs when a player quits.
      */
     @EventHandler
     public void onPlayerQuitEvent(PlayerQuitEvent e)
     {
         String playerName = e.getPlayer().getName();
         
         this.afkPlayers.remove(playerName);
         this.afkPlayersAuto.remove(playerName);
         this.afkLastSeen.remove(playerName);
     }
     
     /**
      * Occurs when a player interacts with something.
      */
     @EventHandler
     public void OnPlayerInteractEvent(PlayerInteractEvent e)
     {
         this.setLastSeen(e.getPlayer());
     }
     
     /**
      * Occurs when a player moves.
      */
     @EventHandler
     public void OnPlayerMoveEvent(PlayerMoveEvent e)
     {
         this.setLastSeen(e.getPlayer());
     }
     
     /**
      * Occurs when a player chats.
      */
     @EventHandler
     public void OnPlayerChatEvent(PlayerChatEvent e)
     {
         this.setLastSeen(e.getPlayer());
     }
     
     private void handleAFKCommand(Player player, String playerName)
     {
         // if the player is currently AFK, mark them as not-AFK, and vice-versa.
         Server server = this.getServer();
         
         // whatever happens, this was explicit.
         this.afkPlayersAuto.put(playerName, false);
         
         if (!this.afkPlayers.containsKey(playerName) || !this.afkPlayers.get(playerName))
         {
             this.afkPlayers.put(playerName, true);
             
             server.broadcastMessage(ChatColor.YELLOW + playerName + " is now away.");
             
             // set the new player name, make sure to truncate it.
             playerName = ChatColor.GRAY + playerName;
             player.setPlayerListName(playerName.substring(0, Math.min(16, playerName.length())));
         }
         else 
         {
             this.afkPlayers.put(playerName, false);
             
             // reset the player name, of course.
             player.setPlayerListName(playerName);
             server.broadcastMessage(ChatColor.YELLOW + playerName + " is no longer away.");
         }
     }
     
     private void handleListCommand(Player player)
     {
         player.sendMessage(this.getListMessage());
     }
     
     private String getListMessage()
     {
         Server server = this.getServer();
         Player[] players = server.getOnlinePlayers();
         
         int playerCount = players.length;
         int playerMax = server.getMaxPlayers();
         
         String message = ChatColor.WHITE + "There are currently " + playerCount + "/" + playerMax + " players online:\n";
         String messagePlayers = "";
         
         for (int i = 0; i < playerCount; i++)
         {
             String playerName = players[i].getName();
             
             if (messagePlayers.length() > 0)
             {
                 messagePlayers += ChatColor.WHITE;
                 messagePlayers += ", ";
             }
             
             if (this.afkPlayers.containsKey(playerName) && this.afkPlayers.get(playerName))
             {
                 messagePlayers += ChatColor.GRAY;
                 messagePlayers += playerName;
                 
                 if (this.afkPlayersAuto.containsKey(playerName) && this.afkPlayersAuto.get(playerName))
                 {
                     messagePlayers += " (Idle)";
                 }
                 else
                 {
                     messagePlayers += " (Away)";
                 }
             }
             else
             {
                 messagePlayers += playerName;
             }
         }
         
         return message + messagePlayers;
     }
     
     private void setLastSeen(Player player)
     {
         this.afkLastSeen.put(player.getName(), new GregorianCalendar());
         
         // automatically update AFK-ness.
         this.onIdleTimerTick();
     }
 }
