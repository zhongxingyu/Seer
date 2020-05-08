 package com.titankingdoms.dev.TitanIRC;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCApi;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserJoinChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserMessageChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserPartChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserQuitServerEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 /**
  * Copyright (C) 2012 Chris Ward
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class IRCListener implements Listener {
     public TitanIRC instance;
     public IRCListener(TitanIRC instance)
     {
         this.instance = instance;
     }
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserJoinChannel(IRCUserJoinChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.IRCToGame.Join(e.getUserNick(), e.getChannel().getName()));
        IRCApi.messageAllChannels(Format.IRCToIRC.Join(e.getUserNick(), e.getChannel().getName()));
         instance.debug("User " + e.getUserNick() + " joined channel " + e.getChannel().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserPartChannel(IRCUserPartChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.IRCToGame.Part(e.getUserNick(), e.getChannel().getName()));
        IRCApi.messageAllChannels(Format.IRCToIRC.Part(e.getUserNick(), e.getChannel().getName()));
         instance.debug("User " + e.getUserNick() + " left channel " + e.getChannel().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserQuitServer(IRCUserQuitServerEvent e)
     {
         instance.getServer().broadcastMessage(Format.IRCToGame.Quit(e.getUserNick()));
         IRCApi.messageAllChannels(Format.IRCToIRC.Quit(e.getUserNick()));
         instance.debug("User " + e.getUserNick() + " quit. ");
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserMessageChannel(IRCUserMessageChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.IRCToGame.Message(e.getUserNick(), e.getChannel().getName(), e.getMessage()));
        IRCApi.messageAllChannels(Format.IRCToIRC.Message(e.getUserNick(), e.getChannel().getName(), e.getMessage()));
         instance.debug("User " + e.getUserNick() + " sent " + e.getMessage() + " to channel " + e.getChannel().getName());
     }
 
 }
