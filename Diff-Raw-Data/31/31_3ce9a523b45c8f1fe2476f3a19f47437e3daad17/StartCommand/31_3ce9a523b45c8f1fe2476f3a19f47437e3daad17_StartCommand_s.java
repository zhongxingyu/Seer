 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director.commands.dir;
 
 import java.sql.ResultSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import de.minestar.director.Core;
 import de.minestar.director.area.Area;
 import de.minestar.director.area.AreaHandler;
 import de.minestar.director.database.DatabaseHandler;
 import de.minestar.director.threading.BlockSetThread;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class StartCommand extends AbstractCommand {
 
     private AreaHandler aHandler;
     private DatabaseHandler dbHandler;
 
     public StartCommand(String syntax, String arguments, String node, DatabaseHandler dbHandler, AreaHandler aHandler) {
         super(Core.NAME, syntax, arguments, node);
         this.aHandler = aHandler;
         this.dbHandler = dbHandler;
         this.description = "Startet den Bau einer Area";
     }
 
     @Override
     public void execute(String[] args, Player player) {
         String areaName = args[0];
         Area area = aHandler.getArea(areaName);
         if (area == null) {
             PlayerUtils.sendError(player, pluginName, "Es existiert keine Area names '" + areaName + "'!");
             return;
         }
 
         ResultSet rs = dbHandler.getBlocks(area.getAreaName());
         if (rs == null) {
             PlayerUtils.sendError(player, pluginName, "Fehler beim Laden der Blcke!");
             return;
         }
         World world = Bukkit.getWorld(area.getWorldName());
         if (world == null) {
             PlayerUtils.sendError(player, pluginName, "Welt '" + area.getWorldName() + "' wurde nicht gefunden!");
             return;
         }
 
         BlockSetThread thread = new BlockSetThread(world, rs);
        int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), thread, 20);
         thread.setID(taskID);
     }
 }
