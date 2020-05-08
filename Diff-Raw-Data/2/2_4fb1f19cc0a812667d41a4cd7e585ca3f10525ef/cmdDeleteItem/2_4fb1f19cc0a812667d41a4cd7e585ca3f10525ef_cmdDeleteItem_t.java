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
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdDeleteItem extends AbstractExtendedCommand {
 
     public cmdDeleteItem(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Loescht Items vom Boden";
     }
 
     @Override
     public void execute(String[] args, Player player) {
         // more parameter then used
         if (args.length > 2)
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
         else {
             int radius = getRadius(args);
             int itemId = getItemId(args);
            if (itemId != -1 && Material.getMaterial(itemId) == null) {
                 PlayerUtils.sendError(player, pluginName, "The item id " + itemId + " is not a valid id!");
                 return;
             }
             deleteItems(radius, itemId, player.getLocation(), player, player.getWorld());
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
                 int itemId = getItemId(args);
                 if (Material.getMaterial(itemId) == null) {
                     ConsoleUtils.printError(pluginName, "The item id " + itemId + " is not a valid id!");
                     return;
                 }
                 deleteItems(-1, itemId, null, console, world);
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
 
     private int getItemId(String[] args) {
         int itemId = -1;
         for (String arg : args) {
             if (arg.startsWith("i")) {
                 itemId = Integer.parseInt(arg.substring(1));
             }
         }
 
         return itemId;
     }
 
     private void deleteItems(int radius, int itemId, Location position, CommandSender sender, World world) {
 
         Collection<Item> items = world.getEntitiesByClass(Item.class);
         long counter = 0L;
         // delete all
         if (radius == -1 && itemId == -1) {
             for (Item item : items) {
                 item.remove();
                 ++counter;
             }
         } else if (radius == -1 && itemId != -1) {
             for (Item item : items) {
                 if (item.getItemStack().getTypeId() == itemId) {
                     item.remove();
                     ++counter;
                 }
             }
         } else if (radius != -1 && itemId == -1) {
             // square faster than square root
             radius *= radius;
             for (Item item : items) {
                 if (isIn(item.getLocation(), position, radius)) {
                     item.remove();
                     ++counter;
                 }
             }
         } else {
             radius *= radius;
             for (Item item : items) {
                 if (item.getItemStack().getTypeId() == itemId && isIn(item.getLocation(), position, radius)) {
                     item.remove();
                     ++counter;
                 }
             }
         }
         ChatUtils.writeSuccess(sender, pluginName, counter + " items wurden entfernt!");
     }
 
     private boolean isIn(Location itemP, Location playerP, int radius) {
         return radius >= itemP.distanceSquared(playerP);
     }
 }
