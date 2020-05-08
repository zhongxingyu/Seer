 /*
  * Copyright (C) 2011 MineStar.de 
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
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.data.ASItem;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdStack extends AbstractCommand {
 
     public cmdStack(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /stack <br>
      * Stack your inventory
      * 
      * @param player
      *            Called the command
      * @param split
      */
     public void execute(String[] args, Player player) {
         if (args.length == 0)
             stack(player, player);
         else
             stack(args[0], player);
     }
 
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         if (args.length == 0)
             ConsoleUtils.printError(pluginName, "You don't even have an inventory, so how can you stack it?");
         else
             stack(args[0], console);
     }
 
     private void stack(String targetName, CommandSender sender) {
         Player target = PlayerUtils.getOnlinePlayer(targetName);
         if (target == null)
             ChatUtils.writeError(sender, pluginName, "Spieler '" + targetName + "' wurde nicht gefunden!");
         else if (target.isDead() || !target.isOnline())
             ChatUtils.writeError(sender, pluginName, "Spieler '" + target.getName() + "' ist nicht online!");
         else
             stack(target, sender);
 
     }
     private void stack(Player target, CommandSender sender) {
         ItemStack[] items = target.getInventory().getContents();
         ItemStack item_1 = null;
         ItemStack item_2 = null;
         int len = items.length;
 
         int affected = 0;
 
         for (int i = 0; i < len; i++) {
             item_1 = items[i];
 
             if ((item_1 == null) || (item_1.getAmount() <= 0) || ASItem.getMaxStackSize(item_1.getType()) == 1)
                 continue;
 
             if ((item_1.getTypeId() >= 325) && (item_1.getTypeId() <= 327))
                 continue;
 
             if (item_1.getAmount() < 64) {
                 int needed = 64 - item_1.getAmount();
                 for (int j = i + 1; j < len; j++) {
                     item_2 = items[j];
                     if ((item_2 == null) || (item_2.getAmount() <= 0) || ASItem.getMaxStackSize(item_1.getType()) == 1)
                         continue;
 
                     if (item_2.getTypeId() != item_1.getTypeId() || item_1.getDurability() != item_2.getDurability())
                         continue;
                    
                    if(!item_1.getEnchantments().equals(item_2.getEnchantments()))
                        continue;
 
                     if (item_2.getAmount() > needed) {
                         item_1.setAmount(64);
                         item_2.setAmount(item_2.getAmount() - needed);
                         break;
                     }
                     items[j] = null;
                     item_1.setAmount(item_1.getAmount() + item_2.getAmount());
                     needed = 64 - item_1.getAmount();
                     affected++;
                 }
             }
         }
         if (affected > 0)
             target.getInventory().setContents(items);
 
         PlayerUtils.sendSuccess(target, pluginName, "Items gestackt!");
         ChatUtils.writeSuccess(sender, pluginName, "Items des Spielers '" + target.getName() + "' wurden gestackt!");
     }
 }
