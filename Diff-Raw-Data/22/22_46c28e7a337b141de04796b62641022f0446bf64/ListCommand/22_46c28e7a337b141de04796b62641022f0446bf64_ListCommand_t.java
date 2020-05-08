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
 
 package com.minestar.MineStarWarp.commands.warp;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.Warp;
 import com.minestar.MineStarWarp.commands.Command;
 
 public class ListCommand extends Command {
 
     private final int warpsPerPage;
 
     public ListCommand(String syntax, String arguments, String node,
             Server server) {
         super(syntax, arguments, node, server);
         warpsPerPage = Main.config.getInt("warps.warpsPerPage", 8);
        this.description = "Mit '/warp list Zahl' kannst du dir seitenweise alle Warppunkte anschauen. Bsp: /warp list 1";
     }
 
     @Override
     /**
      * Representing the command <br>
      * /warp list <br>
      * Sending the player a list of all warps the player can use. 
      * If warp list my is used it sends a list of warps the player has created
      * 
      * @param player
      *            Called the command
      * @param split
      *            args[0] is page number
      */
     public void execute(String[] args, Player player) {
 
         HashMap<String, Warp> warps;
         boolean showPlayersWarps = args.length != 0
                 && args[0].equalsIgnoreCase("my");
         if (showPlayersWarps) {
             warps = Main.warpManager.getWarpsPlayerIsOwner(player.getName());
             if (warps != null) {
                 player.sendMessage("You have used "
                         + Main.warpManager.usedWarpSlots(player)
                         + " free slots");
                 showWarpList(player, warps);
             }
 
             else
                 player.sendMessage(ChatColor.RED
                         + "You didn't have created a warp!");
         }
         else {
             int maxPageNumber = (int) Math.ceil(Main.warpManager
                     .countWarpsCanUse(player) / (double) warpsPerPage);
             if (maxPageNumber == 0) {
                 player.sendMessage(ChatColor.RED
                         + "You do not have access to any warps!");
                 return;
             }
             int pageNumber = args.length == 0 ? 1 : Integer.parseInt(args[0]);
             if (pageNumber <= maxPageNumber) {
                 warps = Main.warpManager.getWarpsForList(pageNumber,
                         warpsPerPage);
                 player.sendMessage("------------------- Page " + pageNumber
                         + "/" + maxPageNumber + " -------------------");
                 showWarpList(player, warps);
             }
             else
                 player.sendMessage(ChatColor.RED
                         + "Use a pagenumber between 1 and " + maxPageNumber);
 
         }
     }
 
     /**
      * This sends all the warps in a good format to the player
      * 
      * @param player
      *            The reciever
      * @param warps
      *            The warps to present
      */
     private void showWarpList(Player player, HashMap<String, Warp> warps) {
 
         for (String warpName : warps.keySet()) {
 
             Warp warp = warps.get(warpName);
             boolean isOwner = warp.isOwner(player.getName());
             String creator = isOwner ? "you" : warp.getOwner();
             Location loc = warp.getLoc();
             int x = (int) Math.round(loc.getX());
             int y = loc.getBlockY();
             int z = (int) Math.round(loc.getZ());
             ChatColor color;
 
             if (isOwner)
                 color = ChatColor.AQUA;
             else if (warp.isPublic())
                 color = ChatColor.GREEN;
             else
                 color = ChatColor.RED;
 
             String location = " @(" + x + ", " + y + ", " + z + ")";
             String creatorString = (warp.isPublic() ? "(+)" : "(-)") + " by "
                     + creator;
             player.sendMessage(color + "'" + warpName + "'" + ChatColor.WHITE
                     + creatorString + location);
         }
     }
 }
