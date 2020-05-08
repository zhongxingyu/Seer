 /* cBorder
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
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
 
 package castro.cBorder;
 
 import org.bukkit.Chunk;
import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 
 
 public class BorderListener implements Listener
 {	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onChunkLoad(ChunkLoadEvent event)
 	{
 		World world = event.getWorld();
 		Border border = BorderMgr.getBorder(world);
 		
 		Chunk chunk = event.getChunk();
 		if(border.isOutsideLimit(chunk)) // Check if chunk is beyond limit --> unload
 			chunk.unload(false, false);
 	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST) // We have to override MultiVerse spawning
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Location to = event.getRespawnLocation();
		Border border = BorderMgr.getBorder(to.getWorld());
		
		Location newLocation = border.getSafe(to);
		if(newLocation != null)
			event.setRespawnLocation(newLocation);
	}
 }
