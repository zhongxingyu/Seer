 package com.titankingdoms.dev.TitanIRC.api.event;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCChannel;
 import com.titankingdoms.dev.TitanIRC.api.IRCServer;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
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
 public class IRCUserMessageChannelEvent extends Event {
     public static HandlerList handlerList = new HandlerList();
     public static HandlerList getHandlerList()
     {
         return handlerList;
     }
     public HandlerList getHandlers()
     {
         return handlerList;
     }
 
     public IRCUserMessageChannelEvent(String hostmask, String message, IRCChannel chan)
     {
         this.chan = chan;
         this.hostmask = hostmask;
         this.message = message;
     }
 
     private String hostmask;
     private IRCChannel chan;
     private String message;
 
     public String getMessage()
     {
         return message;
     }
     public String getUserNick()
     {
         return hostmask.substring(1).split("!")[0];
     }
     public String getUserIdent()
     {
         return hostmask.split("!")[1].split("@")[0];
     }
     public String getUserHostname()
     {
         return hostmask.split("@")[1].split(" ")[0];
     }
     public String getUserHostmask()
     {
         return hostmask;
     }
 
     public IRCServer getServer()
     {
         return chan.getServer();
     }
 
     public IRCChannel getChannel()
     {
         return chan;
     }
 }
