 package com.untamedears.xppylons.listener;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 
 import com.untamedears.xppylons.rtree.AABB;
 
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
 import org.bukkit.event.entity.EntityExplodeEvent;
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
         Block block = e.getBlock();
         World world = block.getWorld();
         int x = block.getX();
         int y = block.getY() + 1;
         int z = block.getZ();
         
         for (Pylon pylon : possiblyDamagedPylons) {
             if (pylon.getX() == x && pylon.getY() == y && pylon.getZ() == z) {
                 // Is the glow block
                 plugin.deactivatePylon(pylon, world);
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkSensitiveBlock(List<Pylon> possiblyDamagedPylons, BlockEvent e, AABB checkZone) {
         boolean result = false;
         for (Pylon pylon : possiblyDamagedPylons) {
             if (checkZone.contains(pylon.getX(), (pylon.getY() - 1), pylon.getZ())) {
                 // Is the glow block
                 plugin.deactivatePylon(pylon, e.getBlock().getWorld());
                 result = true;
             }
         }
         return result;
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent e) {
         try {
             final Block block = e.getBlock();
             final World world = block.getWorld();
             final PylonSet pylons = plugin.getPylons(world);
             if (pylons != null) {
                 final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(block.getX(), block.getY(), block.getZ());
                 
                 if (!possiblyDamagedPylons.isEmpty()) {
                     if (checkSensitiveBlock(possiblyDamagedPylons, e)) {
                         e.setCancelled(true);
                         return;
                     }
                     
                     scheduleStructureCheck(world, possiblyDamagedPylons);
                 }
             }
         } catch (RuntimeException ex) {
             plugin.severe("Error with block break event");
             ex.printStackTrace();
             throw ex;
         }
     }
     
     @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
     public void explosion(EntityExplodeEvent eee) {
         try {
            World world = eee.getEntity().getWorld();
            PylonSet pylons = plugin.getPylons(world);
            if (pylons == null) {
                return;
            }
            
             AABB explosionZone = null;
             for (Block block : eee.blockList()) {
                 AABB blockZone = new AABB();
                 int x = block.getX();
                 int y = block.getY();
                 int z = block.getZ();
                 blockZone.setMinCorner(x, y, z);
                 blockZone.setMaxCorner(x, y, z);
                 if (explosionZone == null) {
                     explosionZone = blockZone;
                 } else {
                     explosionZone = explosionZone.merge(blockZone);
                 }
             }
             
             if (explosionZone != null) {
                 final List<Pylon> possiblyDamagedPylons = plugin.getPylons(world).pylonsAround(explosionZone);
                 if (!possiblyDamagedPylons.isEmpty()) {
                     Set<Block> cancelBlocks = new HashSet<Block>();
                     Set<Pylon> cancelPylons = new HashSet<Pylon>();
                     
                     for (Pylon pylon : possiblyDamagedPylons) {
                         int pyX = pylon.getX();
                         int pyY = pylon.getY() - 1;
                         int pyZ = pylon.getZ();
                         for (Block block : eee.blockList()) {
                             if (block.getX() == pyX && block.getY() == pyY && block.getZ() == pyZ) {
                                 // Is the glow block
                                 plugin.deactivatePylon(pylon, world);
                                 cancelBlocks.add(block);
                                 cancelPylons.add(pylon);
                             }
                         }
                     }
                     
                     eee.blockList().removeAll(cancelBlocks);
                     possiblyDamagedPylons.removeAll(cancelPylons);
                     
                     scheduleStructureCheck(world, possiblyDamagedPylons);
                 }
             }
         } catch (RuntimeException ex) {
            plugin.severe("Error with explosion event");
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
             
             final List<Pylon> possiblyDamagedPylons = plugin.getPylons(moved.getWorld()).pylonsAround(moved.getX(), moved.getY(), moved.getZ());
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
             final Block block = bbe.getBlock();
             final World world = block.getWorld();
             final PylonSet pylons = plugin.getPylons(world);
             final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(block.getX(), block.getY(), block.getZ());
             
             if (!possiblyDamagedPylons.isEmpty()) {
                 if (checkSensitiveBlock(possiblyDamagedPylons, bbe)) {
                     bbe.setCancelled(true);
                     return;
                 }
                 
                 scheduleStructureCheck(world, possiblyDamagedPylons);
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
         }, 2L);
     }
     
 }
