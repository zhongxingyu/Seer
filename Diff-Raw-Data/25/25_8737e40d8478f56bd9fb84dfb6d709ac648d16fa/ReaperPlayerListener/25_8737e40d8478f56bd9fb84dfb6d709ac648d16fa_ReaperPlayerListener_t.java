 /* This file is part of FearTheReaper.
 
     FearTheReaper is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FearTheReaper is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with FearTheReaper.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.herocraftonline.fearthereaper;
 
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.herocraftonline.fearthereaper.spawnpoint.Spawn;
 import com.herocraftonline.fearthereaper.spawnpoint.SpawnPoint;
 import com.herocraftonline.fearthereaper.utils.GraveyardUtils;
 
public class ReaperPlayerListener implements Listener {
 
    public ReaperPlayerListener() {   }
 
    @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         if (GraveyardUtils.useBed(event.getPlayer()) && event.isBedSpawn()) {
             return;
         }
 
         if (event.getPlayer().hasPermission("graveyard.closest")) {
             if (SpawnPoint.getAllowedList(event.getPlayer()).size() != 0) {
                 Spawn closest = SpawnPoint.getClosestAllowed(event.getPlayer());
                 if (closest == null) {
                     return;
                 } else if (!closest.getSpawnMessage().equalsIgnoreCase("none")) {
                     event.getPlayer().sendMessage(closest.getSpawnMessage());
                 }
                 event.setRespawnLocation(closest.getLocation());
             }
         }
     }
 }
