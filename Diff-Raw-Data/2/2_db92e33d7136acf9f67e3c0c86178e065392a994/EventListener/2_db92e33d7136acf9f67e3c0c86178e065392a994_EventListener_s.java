 /* 
  * Copyright 2012 James Geboski <jgeboski@gmail.com>
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
 
 package org.mcmmoirc;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.plugin.PluginManager;
 
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.Users;
 
 public class EventListener implements Listener
 {
     public mcMMOIRC mirc;
     
     public EventListener(mcMMOIRC mirc)
     {
         this.mirc = mirc;
     }
     
     public void register()
     {
         PluginManager pm;
         
         pm = mirc.getServer().getPluginManager();
         pm.registerEvents(this, mirc);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChat(PlayerChatEvent e)
     {
         PlayerProfile pp;
         String party, msg;
         Player p;
         
         p  = e.getPlayer();
         pp = Users.getProfile(p);
         msg   = e.getMessage();
         
         if(pp.getPartyChatMode()) {
             party = pp.getParty();
             
             if(!mirc.partyPoints.containsKey(party))
                 return;
             
             e.setCancelled(true);
             
             mirc.partyMessageToGame(p, "chat", party, msg);
             mirc.partyMessageToIRC(p, "chat", party, msg);
        } else if(!pp.getAdminChatMode()) {
             e.setCancelled(true);
             
             mirc.adminMessageToGame(p, "chat", msg);
             mirc.adminMessageToIRC(p, "chat", msg);
         }
     }
 }
