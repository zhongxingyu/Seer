 package com.mewin.WGBlockRestricter;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 /**
  *
  * @author  mewin
  */
 public final class Utils {
     
     public static boolean blockAllowedAtLocation(WorldGuardPlugin wgp, Material blockType, Location loc) {
         RegionManager rm = wgp.getRegionManager(loc.getWorld());
         if (rm == null) {
             return true;
         }
         ApplicableRegionSet regions = rm.getApplicableRegions(loc);
         Iterator<ProtectedRegion> itr = regions.iterator();
         Map<ProtectedRegion, Boolean> regionsToCheck = new HashMap<>();
         Set<ProtectedRegion> ignoredRegions = new HashSet<>();
         
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
                 
                 regionsToCheck.put(region, (boolean) allowed);
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
                 return (boolean) allowed;
             } else {
                 return true;
             }
         }
     }
     
     public static Object blockAllowedInRegion(ProtectedRegion region, Material blockType) {
         BlockMaterial bm = castMaterial(blockType);
         
         HashSet<BlockMaterial> allowedBlocks = (HashSet<BlockMaterial>) region.getFlag(WGBlockRestricterPlugin.ALLOW_BLOCK_FLAG);
         HashSet<BlockMaterial> blockedBlocks = (HashSet<BlockMaterial>) region.getFlag(WGBlockRestricterPlugin.DENY_BLOCK_FLAG);
         
         if (allowedBlocks != null && (allowedBlocks.contains(bm) || allowedBlocks.contains(BlockMaterial.ANY))) {
             return true;
         }
         else if(blockedBlocks != null && (blockedBlocks.contains(bm) || blockedBlocks.contains(BlockMaterial.ANY))) {
             return false;
         }
         else if (isTreefarm(region)) {
             return false;
         } else {
             return null;
         }
     }
     
     public static boolean isTreefarm(ProtectedRegion region) {
         try {
             StateFlag treeFarmFlag = de.bangl.wgtff.listeners.PlayerListener.FLAG_TREEFARM;
             
             return region.getFlag(treeFarmFlag) == State.DENY;
        } catch(Exception ex) {
             return false;
         }
     }
     
     public static BlockMaterial castMaterial(Material material) {
         try {
             return BlockMaterial.valueOf(material.name());
         } catch(IllegalArgumentException ex) {
             return null;
         }
     }
 }
