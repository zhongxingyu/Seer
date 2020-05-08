 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of AdminStuff.
  * 
  * AdminStuff is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * AdminStuff is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with AdminStuff.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.AdminStuff.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdBlockCount extends AbstractCommand {
 
     private PlayerManager pManager;
 
     public cmdBlockCount(String syntax, String arguments, String node, PlayerManager pManager) {
         super(Core.NAME, syntax, arguments, node);
         this.pManager = pManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // count blocks
         if (pManager.isInSelectionMode(player)) {
             Location[] corner = pManager.getSelectedBlocks(player);
             // have to select corner first
             if (corner == null || corner[0] == null || corner[1] == null) {
                 PlayerUtils.sendError(player, pluginName, "Du hast keine zwei Bloecke selektiert!");
                 return;
             }
             countBlocks(player, corner);
 
             // deactivate mode
             pManager.setSelectionMode(player, false);
         }
         // activate selection mode
         else {
             pManager.setSelectionMode(player, true);
             PlayerUtils.sendSuccess(player, pluginName, "Du bist nun im Selektionsmodus!");
             PlayerUtils.sendInfo(player, pluginName, "Markiere mit einem Linksklick Block 1 und mit Rechtsklick Block 2!");
             PlayerUtils.sendInfo(player, pluginName, "Fuehre dann den Befehl noch einmal aus!");
         }
     }
 
     private void countBlocks(Player player, Location[] corner) {
 
         Map<Material, Counter> blockCounter = new HashMap<Material, Counter>();
         fillMap(blockCounter);
 
         World world = player.getWorld();
         Vector min = getMinimum(corner);
         Vector max = getMaximum(corner);
         Material mat = null;
         Counter total = new Counter();
 
         // count all blocks in the selected area
         for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
             for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                 for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                     mat = world.getBlockAt(x, y, z).getType();
                     if (!mat.equals(Material.AIR) && mat.isBlock()) {
                         blockCounter.get(mat).increase();
                         total.increase();
                     }
                 }
             }
         }
 
         printStatistic(player, blockCounter, total);
     }
 
     private void printStatistic(Player player, Map<Material, Counter> blockCounter, Counter total) {
         PlayerUtils.sendSuccess(player, pluginName, "Es befinden sich " + total.getCount() + " Bloecke in dem Gebiet!");
         Counter counter = null;
 
         for (Entry<Material, Counter> entry : blockCounter.entrySet()) {
             counter = entry.getValue();
             if (counter.getCount() != 0L) {
                float percent = (counter.getCount() / total.getCount()) * 100;
                 PlayerUtils.sendInfo(player, entry.getKey().name() + " : " + counter.getCount() + " of " + total.getCount() + " ( " + percent + "% )");
             }
         }
 
     }
 
     // fill hashmap with all block entities
     private void fillMap(Map<Material, Counter> blockCounter) {
         Material[] mats = Material.values();
         for (Material mat : mats)
             if (mat.isBlock())
                 blockCounter.put(mat, new Counter());
     }
 
     private Vector getMaximum(Location[] corner) {
         //@formatter:off
         return new Vector(
                 Math.max(corner[0].getX(), corner[1].getX()),
                 Math.max(corner[0].getY(), corner[1].getY()),
                 Math.max(corner[0].getZ(), corner[1].getZ())
                 ); 
         //@formatter:on
     }
 
     private Vector getMinimum(Location[] corner) {
         //@formatter:off
         return new Vector(
                 Math.min(corner[0].getX(), corner[1].getX()),
                 Math.min(corner[0].getY(), corner[1].getY()),
                 Math.min(corner[0].getZ(), corner[1].getZ())
                 ); 
         //@formatter:on
     }
 
     // private class to avoid autoboxing
     private class Counter {
         private long count = 0L;
 
         public void increase() {
             ++count;
         }
 
         public long getCount() {
             return count;
         }
     }
 }
