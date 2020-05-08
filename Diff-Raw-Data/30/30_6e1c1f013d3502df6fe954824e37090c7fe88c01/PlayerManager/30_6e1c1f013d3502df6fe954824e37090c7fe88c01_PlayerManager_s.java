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
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import de.minestar.core.units.MinestarPlayer;
 
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
            if (leftClick)
                 corners[0] = target.getLocation();
            else
                 corners[1] = target.getLocation();
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
            prefix = "[AFK] " + displayName;
         if (slapped != null && slapped)
             prefix += "was fished ";
         displayName = prefix + displayName;
         mPlayer.setNickName(displayName);
     }
 }
