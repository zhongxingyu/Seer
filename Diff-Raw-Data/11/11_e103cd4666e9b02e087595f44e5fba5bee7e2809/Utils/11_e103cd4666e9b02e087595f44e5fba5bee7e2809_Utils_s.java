 /*
  * Copyright (C) 2012 mewin <mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mewin.WGBlockRestricter;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 /**
  *
  * @author mewin <mewin001@hotmail.de>
  */
 public final class Utils {
     
     public static boolean blockAllowedAtLocation(WorldGuardPlugin wgp, Material blockType, Location loc) {
         RegionManager rm = wgp.getRegionManager(loc.getWorld());
         if (rm == null) {
             return true;
         }
         ApplicableRegionSet regions = rm.getApplicableRegions(loc);
         Iterator<ProtectedRegion> itr = regions.iterator();
         Map<ProtectedRegion, Boolean> regionsToCheck = new HashMap<ProtectedRegion, Boolean>();
         Set<ProtectedRegion> ignoredRegions = new HashSet<ProtectedRegion>();
         
         while(itr.hasNext()) {
             ProtectedRegion region = itr.next();
             
             if (ignoredRegions.contains(region)) {
                 continue;
             }
             
             Object allowed = blockAllowedInRegion(region, blockType);
             
             if (allowed != null) {
                 ProtectedRegion parent = region.getParent();
                 
                 while(parent != null) {
                     ignoredRegions.add(parent);
                     
                     parent = parent.getParent();
                 }
                 
                 regionsToCheck.put(region, (Boolean) allowed);
             }
         }
         
         if (regionsToCheck.size() >= 1) {
             Iterator<Entry<ProtectedRegion, Boolean>> itr2 = regionsToCheck.entrySet().iterator();
             
             while(itr2.hasNext()) {
                 Entry<ProtectedRegion, Boolean> entry = itr2.next();
                 
                 ProtectedRegion region = entry.getKey();
                 boolean value = entry.getValue();
                 
                 if (ignoredRegions.contains(region)) {
                     continue;
                 }
                 
                 if (value) { // allow > deny
                     return true;
                 }
             }
             
             return false;
         } else {
             Object allowed = blockAllowedInRegion(rm.getRegion("__global__"), blockType);
             
             if (allowed != null) {
                 return (Boolean) allowed;
             } else {
                 return true;
             }
         }
     }
     
     public static Object blockAllowedInRegion(ProtectedRegion region, Material blockType) {
         if (region == null)
         {
             return null;
         }
         ArrayList<BlockMaterial> bmList = castMaterial(blockType);
         
         HashSet<BlockMaterial> allowedBlocks = (HashSet<BlockMaterial>) region.getFlag(WGBlockRestricterPlugin.ALLOW_BLOCK_FLAG);
         HashSet<BlockMaterial> blockedBlocks = (HashSet<BlockMaterial>) region.getFlag(WGBlockRestricterPlugin.DENY_BLOCK_FLAG);
         
         boolean denied = false;
         
         for (BlockMaterial bm : bmList)
         {
             if (allowedBlocks != null && (allowedBlocks.contains(bm) || allowedBlocks.contains(BlockMaterial.ANY))) {
                 return true;
             }
             else if(blockedBlocks != null && (blockedBlocks.contains(bm) || blockedBlocks.contains(BlockMaterial.ANY))) {
                 denied = true;
             }
             else if (isTreefarm(region)) {
                 denied = true;
             }
         }
         
         if (!denied)
         {
             return null;
         }
         else
         {
             return false;
         }
     }
     
     public static boolean hasWGTFF()
     {
         return Bukkit.getServer().getPluginManager().getPlugin("WGTreeFarmFlag") != null;
     }
     
     public static boolean isTreefarm(ProtectedRegion region) {
         if (!hasWGTFF())
         {
             return false;
         }
         try {
             StateFlag treeFarmFlag = de.bangl.wgtff.listeners.PlayerListener.FLAG_TREEFARM;
             
             return region.getFlag(treeFarmFlag) == State.DENY;
         } catch(Error ex) {
             return false;
         }
     }
     
     public static ArrayList<BlockMaterial> castMaterial(Material material) {
         try {
             if (BlockMaterial.ByMaterial.byMaterial.containsKey(material))
             {
                 return BlockMaterial.ByMaterial.byMaterial.get(material);
             }
             else
             {
                Bukkit.getLogger().log(Level.WARNING, "Invalid material: ", material.name());
                 return new ArrayList<BlockMaterial>();
             }
         }
         catch(IllegalArgumentException ex)
         {
            Bukkit.getLogger().log(Level.WARNING, "Invalid material: ", material.name());
             return new ArrayList<BlockMaterial>();
         }
     }
 }
