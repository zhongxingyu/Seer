 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.MineStarWarp.listeners;
 
 import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.World;
 
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.MineStarWarp.dataManager.BackManager;
 
 public class PlayerTeleportListener implements Listener {
 
     private BackManager backManager;
 
     public PlayerTeleportListener(BackManager backManager) {
         this.backManager = backManager;
     }
 
     @EventHandler
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         loadChunk(player, event.getTo());
         if (UtilPermissions.playerCanUseCommand(player, "minestarwarp.command.back"))
             backManager.addBack(player);
     }
 
     private void loadChunk(Player player, Location target) {
 
         target.getBlock().getChunk().load();
//        CraftPlayer cPlayer = (CraftPlayer) player;
//        CraftWorld cWorld = (CraftWorld) target.getWorld();
//
//        int minY = (40 / 2) * 2;
//        int maxY = (100 / 2 + 1) * 2;
//        int minX = cWorld.getChunkAt(target).getX() * 16;
//        int minZ = cWorld.getChunkAt(target).getZ() * 16;
//        int j = minX + cWorld.getChunkAt(target).getX() * 16;
//        int i1 = minY;
//        int l1 = minZ + cWorld.getChunkAt(target).getZ() * 16;
//        int j2 = (minX + 16 - minX) + 1;
//        int l2 = (maxY - minY) + 2;
//        int i3 = (minZ + 16 - minZ) + 1;
 
         // SEND PACKET!
 
        // cPlayer.getHandle().netServerHandler.sendPacket(new
        // Packet51MapChunk(0, 0, 0, 0, 0, 0, cWorld.getHandle()));
     }
 }
