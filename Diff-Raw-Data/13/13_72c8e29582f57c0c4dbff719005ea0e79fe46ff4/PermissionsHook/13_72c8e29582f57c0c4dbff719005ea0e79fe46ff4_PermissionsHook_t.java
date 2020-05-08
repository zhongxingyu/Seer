 /*
  * PermissionsHook.java
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
 
 package com.wolvencraft.promote.hooks;
 
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.promote.Promote;
 import com.wolvencraft.promote.util.Message;
 
 public class PermissionsHook {
     
     /**
      * Returns the rank that the specified player has
      * @param player Player to check
      * @return <b>String</b> name of the rank
      */
     public static String getRank(Player player) {
         return Promote.getInstance().getPermissions().getPlayerGroups(player.getWorld(), player.getPlayerListName())[0];
     }
     
     /**
      * Sets the rank of the specified player to the desired rank
      * @param player Player to set the rank to
      * @param rank Rank to set
      * @return <b>true</b> if the rank has been set, <b>false</b> otherwise
      */
     public static boolean setRank(Player player, String rank) {
         if(!rankExists(rank)) {
             Message.log("The rank '" + rank + "' specified in the configuration does not exist");
             return false;
         }
         Permission permissions = Promote.getInstance().getPermissions();
         if(permissions.playerInGroup(player, rank)) return false;
         String currentGroup = getRank(player);
        if(permissions.playerRemoveGroup(player, currentGroup)
                && permissions.playerAddGroup(player, rank)) return true;
         return false;
     }
     
     /**
      * Checks if the rank even exists in the permissions plugin
      * @param rank Rank to check
      * @return <b>true</b> if the rank exists, <b>false</b> otherwise
      */
     public static boolean rankExists(String rank) {
         for(String group : Promote.getInstance().getPermissions().getGroups()) {
             if(group.equalsIgnoreCase(rank)) return true;
         }
         return false;
     }
 }
