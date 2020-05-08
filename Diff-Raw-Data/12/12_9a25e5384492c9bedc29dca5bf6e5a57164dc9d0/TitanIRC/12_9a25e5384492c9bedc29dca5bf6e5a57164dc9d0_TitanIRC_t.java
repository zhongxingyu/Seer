 package com.titankingdoms.dev.TitanIRC;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCServer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Copyright (C) 2012 Chris Ward
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
 public class TitanIRC extends JavaPlugin {
     public PermsBridge bridge = new PermsBridge(this);
     public PlayerListener playerListener = new PlayerListener(this);
     public IRCListener ircListener = new IRCListener(this);
     private ArrayList<Thread> threads = new ArrayList<Thread>();
 
     /**
      * We all know what this does
      */
     public void onEnable()
     {
         if(!new File(getConfig().getCurrentPath()).exists())
             saveDefaultConfig();
         getServer().getPluginManager().registerEvents(bridge, this);
         getServer().getPluginManager().registerEvents(playerListener, this);
         getServer().getPluginManager().registerEvents(ircListener, this);
         List<Map<?, ?>> serverConnectionList = getConfig().getMapList("servers");
         for(Map<?, ?> server : serverConnectionList)
         {
             Thread conn = new Thread(new ServerConnection((String)server.get("nick"), (String)server.get("address"), (Integer)server.get("port"), ((ArrayList<String>)server.get("channels")).toArray(new String[((ArrayList<String>)server.get("channels")).size()]), this));
             conn.start();
             threads.add(conn);
         }
 
         Format.instance = this;
     }
 
     /**
      * List of IRC Server connections. Please use the IRCApi.getIrcServers instead of this method.
      */
     public ArrayList<ServerConnection> serverConnections = new ArrayList<ServerConnection>();
 
     /**
      * Disconnects from all of the IRC servers.
      */
     public void onDisable()
     {
         for(ServerConnection server : serverConnections)
             server.disconnect();
         for(Thread t : threads)
             t.interrupt();
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
     {
         if(cmd.getName().equalsIgnoreCase("irc"))
         {
             //TODO: List people in each channel?
         }
         return false;
     }
 
     /**
      * Writes debug information to the log. Nothing happens if debug disabled.
      * @param text Message to debug
      */
     public void debug(String text)
     {
         if(getConfig().getBoolean("debug"))
         {
             getLogger().info("TitanIRC Debug: " + text);
         }
     }
 
    public String getMessage(String raw)
    {
        return raw.substring(1).split(":", 2)[1];
    }

 }
