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
 
 import java.util.Collection;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
import de.minestar.AdminStuff.Core;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdButcher extends AbstractExtendedCommand {
 
     public cmdButcher(String syntax, String arguments, String node) {
        super(Core.NAME, syntax, arguments, node);
         this.description = "Toetet Monster im Umkreis";
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // more parameter then used
         if (args.length > 2)
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
         else {
             int radius = getRadius(args);
             String entityName = getEntity(args);
             if (entityName == null)
                 deleteItems(radius, null, player.getLocation(), player, player.getWorld());
             else {
                 EntityType type = EntityType.fromName(entityName);
                 if (type == null)
                     PlayerUtils.sendError(player, pluginName, "Invalid mob name!");
                 else
                     deleteItems(radius, type, player.getLocation(), player, player.getWorld());
             }
         }
     }
 
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         // more parameter then used
         if (args.length > 2)
             ConsoleUtils.printError(pluginName, getHelpMessage());
         else {
             World world = Bukkit.getWorld(args[0]);
             if (world != null) {
                 ConsoleUtils.printError(pluginName, "World '" + args[0] + "' does not exist!");
                 ConsoleUtils.printError(pluginName, getHelpMessage());
             } else {
                 String entityName = getEntity(args);
                 if (entityName == null)
                     deleteItems(-1, null, null, console, world);
                 else {
                     EntityType type = EntityType.fromName(entityName);
                     if (type == null)
                         ConsoleUtils.printError(pluginName, "Invalid mob name!");
                     else
                         deleteItems(-1, type, null, console, world);
                 }
             }
         }
     }
 
     private int getRadius(String[] args) {
         int radius = -1;
         for (String arg : args) {
             if (arg.startsWith("r")) {
                 radius = Integer.parseInt(arg.substring(1));
             }
         }
 
         return radius;
     }
 
     private String getEntity(String[] args) {
         for (String arg : args)
             if (arg.startsWith("m"))
                 return arg.substring(1);
 
         return null;
     }
 
     private void deleteItems(int radius, EntityType type, Location position, CommandSender sender, World world) {
 
         Collection<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class);
         long counter = 0L;
         // delete all
         if (radius == -1 && type == null) {
             for (LivingEntity entity : entities) {
                 if (entity instanceof Player)
                     continue;
                 entity.remove();
                 ++counter;
             }
         } else if (radius == -1 && type != null) {
             for (Entity entity : entities) {
                 if (entity instanceof Player)
                     continue;
                 if (entity.getType().equals(type)) {
                     entity.remove();
                     ++counter;
                 }
             }
         } else if (radius != -1 && type == null) {
             // square faster than square root
             radius *= radius;
             for (Entity entity : entities) {
                 if (entity instanceof Player)
                     continue;
                 if (isIn(entity.getLocation(), position, radius)) {
                     entity.remove();
                     ++counter;
                 }
             }
         } else if (radius != -1 && type != null) {
             radius *= radius;
             for (Entity entity : entities) {
                 if (entity instanceof Player)
                     continue;
                 if (entity.getType().equals(type) && isIn(entity.getLocation(), position, radius)) {
                     entity.remove();
                     ++counter;
                 }
             }
         }
         ChatUtils.writeSuccess(sender, pluginName, counter + " Tiere wurden wurden entfernt!");
     }
 
     private boolean isIn(Location itemP, Location playerP, int radius) {
         return radius >= itemP.distanceSquared(playerP);
     }
 
 }
