 /* This file is part of FearTheReaper.
 
     FearTheReaper is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FearTheReaper is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with FearTheReaper.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.herocraftonline.fearthereaper.utils;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.fearthereaper.FearTheReaper;
 
 public class GraveyardUtils {
 
     public static String makeString(String[] input) {
         String pointname = "";
         for (int i = 1; i < input.length; i++) pointname = pointname + input[i] + " ";
         pointname = pointname.substring(0, pointname.length() - 1);
 
         return pointname;
     }
 
     public static boolean hasBed(Player player) {
         Location bedlocation = player.getBedSpawnLocation();
         if ((bedlocation != null) && (bedlocation.getBlock().getType() == Material.BED_BLOCK)) {
             return true;
         }
         return false;
     }
 
     public static boolean useBed(Player player) {
         if (FearTheReaper.isBedsEnabled()) {
             if ((hasBed(player)) && (player.hasPermission("graveyard.respawn.bed"))) {
                 return true;
             }
         }
         return false;
     }
    
    public static String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-fk0-9])", "\u00A7$1");
    }
 }
