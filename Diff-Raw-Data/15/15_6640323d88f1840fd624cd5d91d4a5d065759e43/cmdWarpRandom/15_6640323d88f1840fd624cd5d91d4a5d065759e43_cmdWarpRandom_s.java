 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.commands.warp;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.FifthElement.data.filter.PublicFilter;
 import de.minestar.FifthElement.statistics.warp.WarpRandomStat;
 import de.minestar.FifthElement.threads.EntityTeleportThread;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpRandom extends AbstractCommand {
 
     private static final Random rand = new Random();
 
     public cmdWarpRandom(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // GET PUBLIC WARPS
         List<Warp> publicWarps = Core.warpManager.filterWarps(PublicFilter.getInstance());
         if (publicWarps.isEmpty()) {
             PlayerUtils.sendSuccess(player, pluginName, "Es gibt keine ffentlichen Warps.");
             return;
         }
 
         // GET RANDOM WARP
         int index = rand.nextInt(publicWarps.size());
         Warp warp = publicWarps.get(index);
 
         // handle vehicles
         if (player.isInsideVehicle()) {
             if (player.getVehicle() instanceof Animals) {
                 if (!warp.getLocation().getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) {
                     PlayerUtils.sendError(player, pluginName, "Tiere knnen die Welt nicht wechseln!");
                     return;
                 }
                 // get the animal
                 Entity entity = player.getVehicle();
 
                 // leave it
                 player.leaveVehicle();
 
                 // load the chunk
                 warp.getLocation().getChunk().load(true);
 
                 // teleport the animal
                 entity.teleport(warp.getLocation());
 
                 // create a Thread
                 EntityTeleportThread thread = new EntityTeleportThread(player.getName(), entity);
                 Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getPlugin(), thread, 10L);
             } else {
                 PlayerUtils.sendError(player, pluginName, "Du kannst dich mit Fahrzeug nicht teleportieren!");
                 return;
             }
         }

         player.teleport(warp.getLocation());
         PlayerUtils.sendSuccess(player, pluginName, "Willkommen beim zuflligen Warp '" + warp.getName() + "'.");
 
         // FIRE STATISTIC
         StatisticHandler.handleStatistic(new WarpRandomStat(player.getName(), warp.getName()));
     }
 }
