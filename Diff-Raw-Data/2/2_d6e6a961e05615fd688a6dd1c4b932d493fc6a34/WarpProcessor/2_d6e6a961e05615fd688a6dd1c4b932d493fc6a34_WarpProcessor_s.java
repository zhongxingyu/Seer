 package com.vartala.soulofw0lf.rpgapi.warpsapi;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created by: soulofw0lf
  * Date: 6/25/13
  * Time: 1:32 PM
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
 public class WarpProcessor {
     public static void WarpHandler(String p, RpgWarp rpgWarp){
         WarpSets wSet = RpgAPI.savedSets.get(rpgWarp.getWarpSet());
          RpgWarp thisWarp = rpgWarp;
         Integer i = 0;
         if (wSet.getWarpsRandom()){
             List<RpgWarp> newList = wSet.getSetWarps();
             Boolean warpPerms = false;
             while (warpPerms = false){
                 thisWarp = newList.get( (int)(Math.random() * newList.size() ) );
                 warpPerms = WarpRequirements(Bukkit.getPlayer(p), thisWarp);
             }
         }
         Double X = thisWarp.getWarpX();
         Double Y = thisWarp.getWarpY();
         Double Z = thisWarp.getWarpZ();
         String world = thisWarp.getWorldName();
         Float yaw = thisWarp.getWarpYaw();
         Float pitch = thisWarp.getWarpPitch();
         Location l = new Location(Bukkit.getWorld(world), X, Y, Z, yaw, pitch);
         if (thisWarp.getVariance()){
             Integer variance = thisWarp.getWarpVariance();
             l.setX(X-variance+((Math.random() * ((variance * 2)+1))));
             l.setZ(Z-variance+((Math.random() * ((variance * 2)+1))));
             Boolean aboveGround = false;
             while (aboveGround == false){
                 Block b = l.getBlock();
                if (b.getType() != Material.AIR){
                     l.setY(l.getY()+3);
                 } else {
                     aboveGround = true;
                 }
             }
         }
         Player pl = Bukkit.getPlayer(p);
         pl.teleport(l);
         for ( WarpBehavior behavior : thisWarp.getWarpBehaviors()){
             behavior.onWarp(p);
         }
     }
 
     public static Boolean WarpRequirements(Player p, RpgWarp rWarp){
         Boolean requirements = true;
         if (rWarp.getLevelNeeded()){
             if (p.getLevel() < rWarp.getWarpLevel()){
                 requirements = false;
             }
 
         }
         if (rWarp.getSinglePerm()){
             if (!(p.hasPermission(rWarp.getPermNeeded()))){
                 requirements = false;
             }
         }
         return requirements;
     }
 }
