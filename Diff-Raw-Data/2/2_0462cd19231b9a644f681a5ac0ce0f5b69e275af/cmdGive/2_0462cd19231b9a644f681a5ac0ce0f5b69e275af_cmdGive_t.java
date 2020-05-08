 /*
 b * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package de.minestar.AdminStuff.commands;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.data.ASItem;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdGive extends AbstractExtendedCommand {
 
     public cmdGive(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /give <ItemID or Name>[:SubID] <Amount> <br>
      * This gives the player a specified itemstack
      * 
      * @param player
      *            Called the command
      * @param split
      * 	          split[0] is the targets name
      *            split[1] is the item name
      *            split[2] is the itemamount
      */
     public void execute(String[] args, Player player) {
         Player target = PlayerUtils.getOnlinePlayer(args[0]);
         if (target == null) {
             PlayerUtils.sendError(player, pluginName, "Spieler '" + args[0] + "' nicht gefunden!");
             return;
         }
         String ID = ASItem.getIDPart(args[1]);
         int amount = 1;
         if (args.length == 2)
             amount = 64;
         else if (args.length == 3) {
             try {
                 amount = Integer.parseInt(args[2]);
             } catch (Exception e) {
                 PlayerUtils.sendError(player, ID, args[2] + " ist keine Zahl! Itemanzahl auf Eins gesetzt!");
             }
             if (amount < 1) {
                 PlayerUtils.sendError(player, pluginName, args[2] + "ist kleiner als Eins! Itemanzahl auf Eins gesetzt!");
                 amount = 1;
             }
         }
         byte data = ASItem.getDataPart(args[1]);
         ItemStack item = ASItem.getItemStack(ID, amount);
         if (item == null) {
            PlayerUtils.sendError(player, pluginName, "'" + args[1] + "' wurde nicht gefunden");
             return;
         }
         item.setDurability(data);
         target.getInventory().addItem(item);
 
         // when item has a sub id
         String itemName = item.getType().name() + (data == 0 ? "" : ":" + data);
 
         PlayerUtils.sendSuccess(player, pluginName, "Spieler '" + target.getName() + "' erhaelt " + amount + " mal " + itemName);
         PlayerUtils.sendInfo(target, "Du erhaelst " + amount + " mal " + itemName);
         ConsoleUtils.printInfo(pluginName, "GIVE: " + player.getName() + " TO " + target.getName() + " : " + amount + " x " + itemName);
     }
 }
