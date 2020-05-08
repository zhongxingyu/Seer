 package com.titankingdoms.dev.TitanIRC.api;
 
 import com.titankingdoms.dev.TitanIRC.ServerConnection;
 import com.titankingdoms.dev.TitanIRC.TitanIRC;
 import org.bukkit.Bukkit;
 
 import java.util.ArrayList;
 
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
 public class IRCApi {
     public static IRCServer[] getIrcServers() {
         ArrayList<IRCServer> list = new ArrayList<IRCServer>();
         for(ServerConnection conn : ((TitanIRC) Bukkit.getServer().getPluginManager().getPlugin("TitanIRC")).serverConnections)
             list.add(new IRCServer(conn));
         return list.toArray(new IRCServer[list.size()]);
     }
     public static void messageAllChannels(String message) {
         for(IRCServer server : getIrcServers())
         {
             for(IRCChannel chan : server.getChannels())
             {
                 chan.message(message);
             }
         }
     }
 

 }
