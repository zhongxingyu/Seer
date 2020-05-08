 package com.titankingdoms.dev.TitanIRC;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCApi;
import com.titankingdoms.dev.TitanIRC.api.IRCChannel;
import com.titankingdoms.dev.TitanIRC.api.IRCServer;
 import com.titankingdoms.dev.TitanIRC.api.event.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
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
 public class IRCListener implements Listener {
     private TitanIRC instance;
 
     /**
      * This class is used only for listening to events thrown by this plugin.
      * It should not be used out side of TitanIRC.
      * @param instance Current TitanIRC instance
      */
     public IRCListener(TitanIRC instance)
     {
         this.instance = instance;
     }
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserJoinChannel(IRCUserJoinChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.convertToGame(Format.IRCToGame.Join(e.getUserNick(), e.getChannel().getName())));
         IRCApi.messageAllChannelsExcept(e.getChannel(), Format.IRCToIRC.Join(e.getUserNick(), e.getChannel().getName()));
         instance.debug("User " + e.getUserNick() + " joined channel " + e.getChannel().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserPartChannel(IRCUserPartChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.convertToGame(Format.IRCToGame.Part(e.getUserNick(), e.getChannel().getName())));
         IRCApi.messageAllChannelsExcept(e.getChannel(), Format.IRCToIRC.Part(e.getUserNick(), e.getChannel().getName()));
         instance.debug("User " + e.getUserNick() + " left channel " + e.getChannel().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserQuitServer(IRCUserQuitServerEvent e)
     {
         instance.getServer().broadcastMessage(Format.convertToGame(Format.IRCToGame.Quit(e.getUserNick())));
        for(IRCServer s : IRCApi.getIrcServers())
        {
            if(s.getBotName() == e.getServer().getBotName() && s.getServerAddress() == e.getServer().getServerAddress())
                continue;
            else
                for(IRCChannel c : s.getChannels())
                    c.message(Format.IRCToIRC.Quit(e.getUserNick()));
        }
         instance.debug("User " + e.getUserNick() + " quit. ");
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserMessageChannel(IRCUserMessageChannelEvent e)
     {
         instance.getServer().broadcastMessage(Format.convertToGame(Format.IRCToGame.Message(e.getUserNick(), e.getChannel().getName(), e.getMessage())));
         IRCApi.messageAllChannelsExcept(e.getChannel(), Format.IRCToIRC.Message(e.getUserNick(), e.getChannel().getName(), e.getMessage()));
         instance.debug("User " + e.getUserNick() + " sent " + e.getMessage() + " to channel " + e.getChannel().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onIRCUserCommand(IRCUserCommandEvent e)
     {
         if(e.getCommand().equalsIgnoreCase("list"))
         {
             String players = "Online players: ";
             if(instance.getServer().getOnlinePlayers().length == 0)
                 players = "No one is currently connected";
             else
                 for(Player p : instance.getServer().getOnlinePlayers())
                 {
                     players += Format.GameToIRC.NameFormat(p.getName()) + " ";
                 }
             e.getChannel().message(players);
         }
     }
 
 }
