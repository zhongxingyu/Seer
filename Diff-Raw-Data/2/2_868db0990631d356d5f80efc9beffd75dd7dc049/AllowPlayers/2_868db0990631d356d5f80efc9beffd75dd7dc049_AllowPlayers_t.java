 /* 
  * Copyright 2011-2012 James Geboski <jgeboski@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.AllowPlayers;
 
 import java.io.File;
 import java.util.HashMap;
 
 import net.minecraft.server.MinecraftServer;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.earth2me.essentials.Essentials;
 
 import org.AllowPlayers.command.CAllowPlayers;
 import org.AllowPlayers.command.COnlineMode;
 
 public class AllowPlayers extends JavaPlugin
 {
     public static final String pluginName = "AllowPlayers";
     
     public Essentials essentials;
     public Configuration config;
     public Watcher watcher;
     public boolean online;
     
     private EventListener events;
     
     public void onLoad()
     {
         config   = new Configuration(new File(getDataFolder(), "config.yml"));
         events   = new EventListener(this);
         watcher  = new Watcher(this);
         online   = true;
     }
     
     public void onEnable()
     {
         PluginManager pm;
         Plugin plugin;
         
         pm     = getServer().getPluginManager();
         plugin = pm.getPlugin("Essentials");
         
         if(plugin == null) {
             Log.severe("Unable to find Essentials");
             return;
         }
         
         essentials = (Essentials) plugin;
         
         getCommand("allowplayers").setExecutor(new CAllowPlayers(this));
         getCommand("onlinemode").setExecutor(new COnlineMode(this));
         
         config.load();
         events.register();
         watcher.start();
     }
     
     public void onDisable()
     {
         try {
             watcher.quit();
             watcher.join();
         } catch(Exception e) {}
     }
     
     /**
      * Test if a CommandSender has a specific permission
      * 
      * @param sender  A CommandSender
      * @param perm    A string containing the permission node
      * 
      * @return TRUE if the player has permission, otherwise FALSE
      **/
     public boolean hasPermission(CommandSender sender, String perm)
     {
         if(!(sender instanceof Player))
             return true;
         
         if(((Player) sender).hasPermission(perm))
             return true;
         
         Message.severe(sender, "You don't have permission to do that!");
         return false;
     }
     
     /**
      * Sends a message to all online players with a specific permission
      * 
      * @param perm    A string containing the permission node
      * @param format  A format string
      * @param args    Arguments corresponding to @param format
      **/
     public void broadcast(String perm, String format, Object ... args)
     {
         Player[] players = getServer().getOnlinePlayers();
         
         for(Player player : players) {
             if(player.hasPermission(perm))
                 Message.info((CommandSender) player, format, args);
         }
         
         Log.info(format, args);
     }
     
     /**
      * Set's AllowPlayers' minecraft.net status to online or offline
      * 
      * @param online  TRUE to set minecraft.net as being online, FALSE
      *                to set minecraft.net as being offline
      **/
     public void setOnline(boolean online)
     {
         String msg = null;
         
         if(!this.online && online) {
             msg = ChatColor.GREEN + "Minecraft.net has come back " +
                 "online.";
         } else if(this.online && !online) {
             msg = ChatColor.RED + "Minecraft.net has gone " +
                 "offline. Do not logout!";
         }
         
         if(msg != null)
             broadcast("allowplayers.msg.notify", msg);
         
         this.online = online;
     }
     
     /**
      * Set the server's online mode state. Online mode determines if
      * a user is validly logged in with a player name.
      * 
      * @param mode  TRUE to enable online, FALSE to disable
      **/
     public void setOnlineMode(boolean mode)
     {
        ((MinecraftServer) ((CraftServer) getServer()).getServer()).setOnlineMode(mode);
     }
 }
