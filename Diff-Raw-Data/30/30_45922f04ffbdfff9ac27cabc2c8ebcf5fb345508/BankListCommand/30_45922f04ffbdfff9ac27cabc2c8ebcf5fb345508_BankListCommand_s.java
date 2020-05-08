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
 
 package com.minestar.MineStarWarp.commands.bank;
 
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.commands.ExtendedCommand;
 
 public class BankListCommand extends ExtendedCommand {
 
     public BankListCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
         this.description = Main.localization.get(BANK_LIST_DESCRIPTION);
     }
 
     @Override
     /**
      * Representing the command <br>
      * /bank list (#) <br>
      * This send all banks as a list
      * 
      * @param player
      *            Called the command
      * @param args
      *            (args[0] = pageNumber)
      */
     public void execute(String[] args, Player player) {
 
         int pageNumber = 1;
 
         if (args.length != 0 && !args[0].matches("\\d*")) {
             player.sendMessage(ChatColor.RED
                     + Main.localization.get(BANK_LIST_PAGE_NUMBER));
             return;
         }
         else if (args.length == 1)
             pageNumber = Integer.parseInt(args[0]);
 
         int banksPerPage = Main.config.getInt("banks.banksPerPage", 10);
         int maxPage = (int) Math.ceil(Main.bankManager.countBanks()
                 / (double) banksPerPage);
 
        if (pageNumber > maxPage) {
             player.sendMessage(ChatColor.RED
                     + Main.localization.get(BANK_LIST_HIGH_PAGE,
                             Integer.toString(maxPage)));
             return;
         }
 
         TreeMap<String, Location> banks = Main.bankManager.getBanksForList(
                 pageNumber, banksPerPage);
 
        if (banks == null) {
            player.sendMessage(ChatColor.RED
                    + Main.localization.get(BANK_LIST_NO_BANKS));
            return;
        }

         player.sendMessage(ChatColor.WHITE
                 + Main.localization.get(BANK_LIST_PAGE_HEAD,
                         Integer.toString(pageNumber), Integer.toString(maxPage)));
         showBankList(player, banks);
     }
 
     /**
      * This list all banks formatted to the command caller
      * 
      * @param player
      *            The command caller
      * @param warps
      *            The warps to present
      */
     private void showBankList(Player player, TreeMap<String, Location> banks) {
 
         for (Entry<String, Location> bank : banks.entrySet()) {
             String playerName = bank.getKey();
             Location loc = bank.getValue();
 
             int x = (int) Math.round(loc.getX());
             int y = loc.getBlockY();
             int z = (int) Math.round(loc.getZ());
 
             player.sendMessage(ChatColor.AQUA + "'" + playerName + "' " + " @("
                     + x + ", " + y + ", " + z);
         }
     }
 }
