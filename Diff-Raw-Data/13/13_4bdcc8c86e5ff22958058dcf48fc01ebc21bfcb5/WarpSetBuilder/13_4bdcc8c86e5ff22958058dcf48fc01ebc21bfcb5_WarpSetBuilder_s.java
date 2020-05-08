 package com.vartala.soulofw0lf.rpgapi.warpsapi;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by: soulofw0lf
  * Date: 6/24/13
  * Time: 8:06 PM
  * <p/>
  * This file is part of the Rpg Suite Created by Soulofw0lf and Linksy.
  * <p/>
  * The Rpg Suite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * The Rpg Suite is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with The Rpg Suite Plugin you have downloaded.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class WarpSetBuilder {
     /**
      * Make Warp sets from yml
      *
      */
     public static void BuildSets() {
         YamlConfiguration warpConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgWarps/RpgWarps.yml"));
        if (warpConfig.get("Warp Sets") != null) {
             for (String warpSet : warpConfig.getConfigurationSection("Warp Sets").getKeys(false)) {
                 WarpSets warpS = new WarpSets();
                 warpS.setSetName(warpSet);
                 List<RpgWarp> setWarps = new ArrayList<>();
                 warpS.setSetWarps(setWarps);
                 warpS.setWarpsRandom(warpConfig.getBoolean("Warp Sets." + warpSet + ".Is Random"));
                 warpS.setSetPermission(warpConfig.getString("Warp Sets." + warpSet + ".Permission Needed"));
                 RpgAPI.savedSets.put(warpSet, warpS);
            }
         }
     }
 
     /**
      * Save all warp sets to yml
      *
      */
     public static void SaveSets() {
         YamlConfiguration warpConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgWarps/RpgWarps.yml"));
         for (String setNames : RpgAPI.savedSets.keySet()) {
             WarpSets warpS = RpgAPI.savedSets.get(setNames);
             warpConfig.set("Warp Sets." + setNames + ".Is Random", warpS.getWarpsRandom());
             warpConfig.set("Warp Sets." + setNames + ".Permission Needed", warpS.getSetPermission());
         }
         try {
             warpConfig.save(new File("plugins/RpgWarps/RpgWarps.yml"));
         } catch (IOException e){
             System.out.print(e);
         }
     }
 }
