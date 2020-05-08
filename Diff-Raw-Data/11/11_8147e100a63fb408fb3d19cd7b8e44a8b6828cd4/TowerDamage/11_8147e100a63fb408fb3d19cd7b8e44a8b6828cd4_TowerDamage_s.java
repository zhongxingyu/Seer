 package com.untamedears.xppylons.listener;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 
 import rtree.AABB;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.Listener;
 import org.bukkit.Location;
 import org.bukkit.event.*;
 import org.bukkit.event.block.BlockEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.block.BlockFace;
 
 import com.untamedears.xppylons.XpPylons;
 import com.untamedears.xppylons.PylonSet;
 import com.untamedears.xppylons.Pylon;
 import com.untamedears.xppylons.EnergyField;
 
 public class TowerDamage implements Listener {
     private XpPylons plugin;
     private Set<Integer> diviningBlocks;
     
     public TowerDamage(XpPylons plugin) {
         this.plugin = plugin;
     }
     
     private boolean checkSensitiveBlock(List<Pylon> possiblyDamagedPylons, BlockEvent e) {
         int x = e.getBlock().getX();
         int y = e.getBlock().getY();
         int z = e.getBlock().getZ();
         
         for (Pylon pylon : possiblyDamagedPylons) {
             if (pylon.getX() == x && pylon.getY() == y + 1 && pylon.getZ() == z) {
                 // Is the glow block
                 plugin.deactivatePylon(pylon, e.getBlock().getWorld());
                 return true;
             }
         }
         return false;
     }
     
     private boolean checkSensitiveBlock(List<Pylon> possiblyDamagedPylons, BlockEvent e, AABB checkZone) {
         for (Pylon pylon : possiblyDamagedPylons) {
             if (checkZone.contains(pylon.getX(), (pylon.getY() - 1), pylon.getZ())) {
                 // Is the glow block
                 plugin.deactivatePylon(pylon, e.getBlock().getWorld());
                 return true;
             }
         }
         return false;
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent e) {
         try {
             final World world = e.getBlock().getWorld();
             final PylonSet pylons = plugin.getPylons(world);
             final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
             
             if (!possiblyDamagedPylons.isEmpty()) {
                 if (checkSensitiveBlock(possiblyDamagedPylons, e)) {
                     e.setCancelled(true);
                     return;
                 }
                 
                 scheduleStructureCheck(e.getBlock().getWorld(), possiblyDamagedPylons);
             }
         } catch (RuntimeException ex) {
             plugin.severe("Error with block break event");
             ex.printStackTrace();
             throw ex;
         }
     }
     
     
     @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
     public void pistonExtend(BlockPistonExtendEvent bpee) {
         try {
             if (bpee.getLength() > 0) {
                 AABB firstBlockZone = new AABB();
                 Block firstBlock = bpee.getBlock().getRelative(bpee.getDirection(), 1);
                 firstBlockZone.setMinCorner(firstBlock.getX(), firstBlock.getY(), firstBlock.getZ());
                 firstBlockZone.setMaxCorner(firstBlock.getX(), firstBlock.getY(), firstBlock.getZ());
                 
                 Block lastBlock = bpee.getBlock().getRelative(bpee.getDirection(), 1 + bpee.getLength());
                 AABB lastBlockZone = new AABB();
                 lastBlockZone.setMinCorner(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ());
                 lastBlockZone.setMaxCorner(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ());
                 
                 AABB movementZone = firstBlockZone.merge(lastBlockZone);
                 
                 final List<Pylon> possiblyDamagedPylons = plugin.getPylons(firstBlock.getWorld()).pylonsAround(movementZone);
                 if (!possiblyDamagedPylons.isEmpty()) {
                     if (checkSensitiveBlock(possiblyDamagedPylons, bpee, movementZone)) {
                         bpee.setCancelled(true);
                         return;
                     }
                     
                     scheduleStructureCheck(bpee.getBlock().getWorld(), possiblyDamagedPylons);
                 }
             }
         } catch (RuntimeException ex) {
             plugin.severe("Error with piston extend event");
             ex.printStackTrace();
             throw ex;
         }
     }
     
     @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
     public void pistonRetract(BlockPistonRetractEvent bpre) {
         try {
             BlockFace direction = bpre.getDirection();
             
             // the block that the piston moved
             Block moved = bpre.getBlock().getRelative(direction, 2);
             
            final List<Pylon> possiblyDamagedPylons = plugin.getPylons(bpre.getBlock().getWorld()).pylonsAround(bpre.getBlock().getX(), bpre.getBlock().getY(), bpre.getBlock().getZ());
             if (!possiblyDamagedPylons.isEmpty()) {
                 if (checkSensitiveBlock(possiblyDamagedPylons, bpre)) {
                     bpre.setCancelled(true);
                     return;
                 }
                 
                 scheduleStructureCheck(bpre.getBlock().getWorld(), possiblyDamagedPylons);
             }
         } catch (RuntimeException ex) {
             plugin.severe("Error with piston retract event");
             ex.printStackTrace();
             throw ex;
         }
     }
     
     @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
     public void blockBurn(BlockBurnEvent bbe) {
         try {
             final World world = bbe.getBlock().getWorld();
             final PylonSet pylons = plugin.getPylons(world);
             final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(bbe.getBlock().getX(), bbe.getBlock().getY(), bbe.getBlock().getZ());
             
             if (!possiblyDamagedPylons.isEmpty()) {
                 if (checkSensitiveBlock(possiblyDamagedPylons, bbe)) {
                     bbe.setCancelled(true);
                     return;
                 }
                 
                 scheduleStructureCheck(bbe.getBlock().getWorld(), possiblyDamagedPylons);
             }
         } catch (RuntimeException ex) {
             plugin.severe("Error with block burn event");
             ex.printStackTrace();
             throw ex;
         }
     }
     
     private void scheduleStructureCheck(final World world, final List<Pylon> possiblyDamagedPylons) {
         final XpPylons plugin = this.plugin;
         
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override 
             public void run() {
                 for (Pylon pylon : possiblyDamagedPylons) {
                     if (!plugin.getPylonPattern().checkStructure(world, pylon)) {
                         plugin.info("Pylon at " + Integer.toString(pylon.getX()) + ", " + Integer.toString(pylon.getY()) + ", " + Integer.toString(pylon.getZ()) + " damaged, deactivating");
                         plugin.deactivatePylon(pylon, world);
                     }
                 }
             }
        }, 1L);
     }
     
 }
