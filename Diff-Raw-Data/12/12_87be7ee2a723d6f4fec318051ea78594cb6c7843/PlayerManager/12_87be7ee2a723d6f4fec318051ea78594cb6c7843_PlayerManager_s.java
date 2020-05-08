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
 
 package de.minestar.AdminStuff.manager;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.events.FakeJoinEvent;
 import de.minestar.minestarlibrary.events.FakeQuitEvent;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class PlayerManager {
 
     private Map<String, Set<String>> recipients = new HashMap<String, Set<String>>();
 
     public PlayerManager() {
     }
 
     // ******************************************************
     // *************** BLOCK COUNTING ***********************
     // ******************************************************
 
     private Map<String, Location[]> blockCountSel = new HashMap<String, Location[]>();
 
     public boolean isInSelectionMode(Player player) {
         return blockCountSel.containsKey(player.getName().toLowerCase());
     }
 
     public void setSelectionMode(Player player, boolean isIn) {
         if (isIn)
             blockCountSel.put(player.getName().toLowerCase(), new Location[2]);
         else
             blockCountSel.remove(player.getName().toLowerCase());
     }
 
     public void setSelectedBlock(Player player, Block target, boolean leftClick) {
         String pName = player.getName().toLowerCase();
         Location[] corners = blockCountSel.get(pName);
         if (corners != null) {
             if (leftClick) {
                 corners[0] = target.getLocation();
                 PlayerUtils.sendSuccess(player, "Block 1 markiert");
             } else {
                 corners[1] = target.getLocation();
                 PlayerUtils.sendSuccess(player, "Block 2 markiert");
             }
         }
         blockCountSel.put(pName, corners);
     }
 
     public Location[] getSelectedBlocks(Player player) {
         return blockCountSel.get(player.getName().toLowerCase());
     }
 
     // ******************************************************
     // ************* RECIPIENT CHAT HANDELING ***************
     // ******************************************************
 
     public Set<String> getRecipients(String playerName) {
         return recipients.get(playerName.toLowerCase());
     }
 
     public void clearRecipients(String playerName) {
         recipients.remove(playerName.toLowerCase());
     }
 
     public void setRecipients(String playerName, Set<String> recs) {
         recipients.put(playerName.toLowerCase(), recs);
     }
 
     // ******************************************************
     // ************** COMMON USER MODIFACTION ***************
     // ******************************************************
 
     public void updatePrefix(Player player, MinestarPlayer mPlayer) {
         Boolean slapped = mPlayer.getBoolean("adminstuff.slapped");
         Boolean afk = mPlayer.getBoolean("adminstuff.afk");
        String displayName = mPlayer.getNickName().replaceFirst("[AFK] ", "").replaceFirst("was fished ", "");
         String prefix = "";
         if (afk != null && afk)
             prefix += "[AFK] ";
         if (slapped != null && slapped)
             prefix += "was fished ";
         displayName = prefix + displayName;
         mPlayer.setNickName(displayName);
     }
 
     // ******************************************************
     // ************* HIDDEN PLAYER HANDELING ****************
     // ******************************************************
 
     // Player in hide mode
     private Set<Player> hiddenPlayer = new HashSet<Player>();
 
     public void hidePlayer(Player player) {
         // send fake packet
         FakeQuitEvent fakeQuitEvent = new FakeQuitEvent(player, ChatColor.YELLOW + player.getName() + " left the game.");
         Bukkit.getPluginManager().callEvent(fakeQuitEvent);
         Bukkit.broadcastMessage(fakeQuitEvent.getQuitMessage());
 
         hiddenPlayer.add(player);
 
         // Only ops can see the hidden player
         Player[] onlinePlayer = Bukkit.getOnlinePlayers();
         for (Player other : onlinePlayer) {
             if (!other.isOp())
                 other.hidePlayer(player);
         }
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(player);
         mPlayer.setBoolean("adminstuff.hide", true);
     }
 
     // logged in player don't allow to see them
     public void updateHidePlayer(Player loggedIn) {
         if (loggedIn.isOp())
             return;
         for (Player hidden : hiddenPlayer)
             loggedIn.hidePlayer(hidden);
     }
 
     public void showPlayer(Player player) {
         // send fake packet
         FakeJoinEvent fakeJoinEvent = new FakeJoinEvent(player, ChatColor.YELLOW + player.getName() + " joined the game.");
         Bukkit.getPluginManager().callEvent(fakeJoinEvent);
         Bukkit.broadcastMessage(fakeJoinEvent.getJoinMessage());
 
         hiddenPlayer.remove(player);
 
         // Make player visible to everyone
         Player[] onlinePlayer = Bukkit.getOnlinePlayers();
         for (Player other : onlinePlayer)
             other.showPlayer(player);
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(player);
         mPlayer.setBoolean("adminstuff.hide", false);
     }
     public boolean isHidden(Player player) {
         return hiddenPlayer.contains(player);
     }
 }
