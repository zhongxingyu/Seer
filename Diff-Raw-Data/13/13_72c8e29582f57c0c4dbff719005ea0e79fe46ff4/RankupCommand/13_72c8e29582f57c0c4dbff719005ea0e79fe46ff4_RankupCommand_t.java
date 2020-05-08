 /*
  * RankupCommand.java
  * 
  * Promote
  * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
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
 
 package com.wolvencraft.promote.cmd;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.promote.CommandManager;
 import com.wolvencraft.promote.Promote;
 import com.wolvencraft.promote.CommandManager.Command;
 import com.wolvencraft.promote.hooks.EconomyHook;
 import com.wolvencraft.promote.hooks.PermissionsHook;
 import com.wolvencraft.promote.ranks.PromotionLadder;
 import com.wolvencraft.promote.ranks.PromotionRank;
 import com.wolvencraft.promote.settings.Configuration;
 import com.wolvencraft.promote.settings.Language;
 import com.wolvencraft.promote.settings.PermissionNode;
 import com.wolvencraft.promote.util.Message;
 import com.wolvencraft.promote.util.ReplacementPair;
 
 public class RankupCommand {
     
     @Command(
             alias = {"rankup", "promote", "promoteme"},
             minArgs = 0,
             maxArgs = 1,
             permission = PermissionNode.Rankup,
             allowConsole = false,
             usage = "/rankup",
             description = "Gain a promotion for a price"
             )
     public static boolean rankup(List<String> args) {
         if(args.size() == 0) {
             Player player = (Player) CommandManager.getSender();
             
             List<PromotionLadder> ladders = Promote.getInstance().getPromotionManager().getByRank(PermissionsHook.getRank(player));
             if(ladders.size() == 0) {
                 Message.sendFormattedError(Language.ERROR_RANK_NOT_IN_LADDER);
                 return false;
             } else if(ladders.size() > 1) {
                 Message.sendFormattedError(Language.ERROR_MULTIPLE_LADDERS);
                 String available = "";
                 for(PromotionLadder ladder : ladders) {
                     available += " " + ladder.getName() + ",";
                 }
                 available = available.substring(0, available.length() - 1);
                 Message.send(available);
                 return false;
             }
             
             return performPromotion(ladders.get(0), player);
         } else if(args.size() == 1) {
             if(args.get(0).equalsIgnoreCase("reload")) {
                 if(!PermissionNode.Admin.has(CommandManager.getSender())) {
                     Message.sendFormattedError(Language.ERROR_ACCESS);
                     return false;
                 }
                 
                 Promote.getInstance().reloadSettings();
                 Message.sendFormattedSuccess(Language.GENERAL_RELOADED);
             } else {
                 Player player = (Player) CommandManager.getSender();
                 
                 PromotionLadder ladder = Promote.getInstance().getPromotionManager().getByName(args.get(0));
                 if(ladder == null) {
                     Message.sendFormattedError(Language.ERROR_LADDER_NOT_FOUND);
                     return false;
                 }
                 
                 return performPromotion(ladder, player);
             }
             
             return true;
         } else {
             Message.sendFormattedError(Language.ERROR_ARGUMENTS);
             return false;
         }
     }
     
     /**
      * Performs the necessary checks for the player promotion
      * @param ladder Promotional ladder 
      * @param player Player to promote
      * @return <b>true</b> if the player was promoted, <b>false</b> otherwise
      */
     public static boolean performPromotion(PromotionLadder ladder, Player player) {
         PromotionRank nextRank = ladder.getNext(PermissionsHook.getRank(player));
         if(nextRank == null) {
             Message.sendFormattedError(Language.ERROR_RANK_HIGHEST);
             return false;
         }
         
         if(!EconomyHook.withdraw(player, nextRank.getPrice())) {
             Message.sendFormattedError(
                     Language.ERROR_NO_MONEY,
                     new ReplacementPair("<RANK>", nextRank.getName()),
                     new ReplacementPair("<PRICE>", nextRank.getPrice() + "")
                     );
             return false;
         }
         
        PermissionsHook.setRank(player, nextRank.getName());
         
         if(Configuration.BROADCAST.toBoolean()) {
             Message.broadcast(
                     Language.RANKS_BROADCAST,
                     player,
                     new ReplacementPair("<RANK>", nextRank.getName()),
                     new ReplacementPair("<PRICE>", nextRank.getPrice() + "")
                     );
         }
         
         Message.sendFormattedSuccess(
                 Language.RANKS_PROMOTED,
                 new ReplacementPair("<RANK>", nextRank.getName()),
                 new ReplacementPair("<PRICE>", nextRank.getPrice() + "")
                 );
         
         Message.log("Player " + player.getName() + " has been promoted to " + nextRank.getName() + " for " + nextRank.getPrice());
         return true;
     }
 
 }
