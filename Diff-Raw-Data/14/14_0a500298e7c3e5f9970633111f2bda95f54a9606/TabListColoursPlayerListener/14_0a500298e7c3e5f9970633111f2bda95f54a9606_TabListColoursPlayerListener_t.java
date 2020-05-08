 /*
  * Customizable colours for the tab player listing.
  * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.Hoot215.TabListColours;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class TabListColoursPlayerListener implements Listener
   {
     private TabListColours plugin;
     
     public TabListColoursPlayerListener(TabListColours instance)
       {
         plugin = instance;
       }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin (PlayerJoinEvent event)
       {
         Player player = event.getPlayer();
         String prefix = plugin.getChat().getPlayerPrefix(player);
         if (prefix != null && !prefix.isEmpty())
           {
            String playerName = player.getName();
            String name = playerName;
            name =
                playerName.length() > 12 ? prefix.substring(0, 2).replace('&',
                    '§')
                    + playerName.substring(0, playerName.length() < 14
                        ? playerName.length() - 1 : 14) : prefix
                    .substring(0, 2).replace('&', '§')
                    + playerName
                    + ChatColor.WHITE;
            player.setPlayerListName(name);
           }
       }
   }
