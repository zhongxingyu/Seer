 /*
  * Wormhole X-Treme Worlds Plugin for Bukkit
  * Copyright (C) 2011 Dean Bailey
  * 
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.wormhole_xtreme.worlds.handler;
 
 import java.util.logging.Level;
 
 import org.bukkit.Chunk;
 
 import com.wormhole_xtreme.worlds.WormholeXTremeWorlds;
 import com.wormhole_xtreme.worlds.world.WorldManager;
 import com.wormhole_xtreme.worlds.world.WormholeWorld;
 
 /**
  * @author alron
  * 
  */
 public class WorldHandler {
 
     private static final WormholeXTremeWorlds thisPlugin = WormholeXTremeWorlds.getThisPlugin();
 
     /**
      * Adds the chunk sticky and loads the Wormhole World if it is unloaded.
      * 
      * @param stickyChunk
      *            the sticky chunk
      * @param ownerPlugin
      *            the owner plugin
      * @return true, if successful
      */
     public boolean addStickyChunk(final Chunk stickyChunk, final String ownerPlugin) {
         if ((stickyChunk != null) && (ownerPlugin != null)) {
             final String worldName = stickyChunk.getWorld().getName();
             WormholeWorld wormholeWorld = WorldManager.getWorld(worldName);
             thisPlugin.prettyLog(Level.FINE, false, "Sticky Chunk Addition: " + stickyChunk.toString() + " World: " + stickyChunk.getWorld().getName() + " Plugin: " + ownerPlugin);
             if (wormholeWorld != null) {
                 if (wormholeWorld.isWorldLoaded() && wormholeWorld.addStickyChunk(stickyChunk, ownerPlugin)) {
                    if ( !wormholeWorld.getThisWorld().isChunkLoaded(stickyChunk)) {
                        wormholeWorld.getThisWorld().loadChunk(stickyChunk);
                         thisPlugin.prettyLog(Level.FINE, false, "Loaded Sticky Chunk: " + stickyChunk.toString());
                     }
                     return WorldManager.addWorld(wormholeWorld);
                 }
                 else if (WorldManager.loadWorld(wormholeWorld) && ((wormholeWorld = WorldManager.getWorld(worldName)) != null) && wormholeWorld.addStickyChunk(stickyChunk, ownerPlugin)) {
                    if ( !wormholeWorld.getThisWorld().isChunkLoaded(stickyChunk)) {
                        wormholeWorld.getThisWorld().loadChunk(stickyChunk);
                         thisPlugin.prettyLog(Level.FINE, false, "Loaded Sticky Chunk: " + stickyChunk.toString());
                     }
                     return WorldManager.addWorld(wormholeWorld);
                 }
             }
         }
         return false;
     }
 
     /**
      * Delete sticky chunk only if a world is loaded.
      * 
      * @param stickyChunk
      *            the sticky chunk
      * @param ownerPlugin
      *            the owner plugin
      * @return true, if successful
      */
     public boolean removeStickyChunk(final Chunk stickyChunk, final String ownerPlugin) {
         if ((stickyChunk != null) && (ownerPlugin != null)) {
             final String worldName = stickyChunk.getWorld().getName();
             final WormholeWorld wormholeWorld = WorldManager.getWorld(worldName);
             thisPlugin.prettyLog(Level.FINE, false, "Sticky Chunk Removal: " + stickyChunk.toString() + " World: " + stickyChunk.getWorld().getName() + " Plugin: " + ownerPlugin);
             if ((wormholeWorld != null) && wormholeWorld.isWorldLoaded() && wormholeWorld.removeStickyChunk(stickyChunk, ownerPlugin)) {
                if (wormholeWorld.getThisWorld().isChunkLoaded(stickyChunk) && wormholeWorld.isStickyChunk(stickyChunk)) {
                    wormholeWorld.getThisWorld().unloadChunk(stickyChunk.getX(), stickyChunk.getZ());
                    thisPlugin.prettyLog(Level.FINE, false, "Unloaded Formet Sticky Chunk: " + stickyChunk.toString());
                 }
                 return WorldManager.addWorld(wormholeWorld);
             }
         }
         return false;
     }
 }
