 package com.norcode.bukkit.buildinabox;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 
 import com.norcode.bukkit.buildinabox.util.RandomFireworksGenerator;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.CuboidClipboard;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.blocks.BaseBlock;
 
 public abstract class BuildingTask implements Runnable {
     public static final int TOP_DOWN = -1;
     public static final int BOTTOM_UP = 1;
    
     BlockVector cursor;
     CuboidClipboard clipboard;
     HashMap<BlockVector, BaseBlock> replacedBlocks = new HashMap<BlockVector, BaseBlock>();
     Location worldCursor;
     BlockVector origin;
     ArrayList<BlockVector> xzPoints;
     int ptr = -1;
     int buildDirection = 0;
     public BuildingTask(BuildChest buildChest, CuboidClipboard clipboard, int buildDirection) {
         this.buildDirection = buildDirection;
         this.clipboard = clipboard;
         this.cursor = new BlockVector(0, (buildDirection == TOP_DOWN ? this.clipboard.getSize().getBlockY()-1:0), 0);
         
         xzPoints = new ArrayList<BlockVector>(clipboard.getSize().getBlockX() * clipboard.getSize().getBlockZ());
         for (int x=0; x<this.clipboard.getSize().getBlockX();x++) {
             for (int z=0;z<this.clipboard.getSize().getBlockZ();z++) {
                 xzPoints.add(new BlockVector(x,0,z));
             }
         }
         if (shouldShuffle()) {
             Collections.shuffle(xzPoints);
         }
         Location cl = buildChest.getLocation();
         Vector off = clipboard.getOffset();
         origin = new BlockVector(cl.getBlockX() + off.getBlockX(), cl.getBlockY() + off.getBlockY(), cl.getBlockZ() + off.getBlockZ());
         worldCursor = new Location(buildChest.getBlock().getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
     }
 
     protected boolean shouldShuffle() {
         return false;
     }
 
     public abstract void run();
     boolean moveCursor() {
         int y = cursor.getBlockY();
         ptr++;
         if (ptr >= xzPoints.size()) {
             ptr = 0;
             y += buildDirection;
             switch (buildDirection) {
             case TOP_DOWN:
                 if (y < 0) return false;
                 break;
             case BOTTOM_UP:
                 if (y >= clipboard.getSize().getBlockY()) return false;
                 break;
             }
             if (shouldShuffle()) {
                 Collections.shuffle(xzPoints);
             }
         }
         BlockVector v = xzPoints.get(ptr);
         cursor = new BlockVector(v.getBlockX(),y,v.getBlockZ());
         worldCursor.setX(origin.getBlockX() + v.getBlockX());
         worldCursor.setY(origin.getBlockY() + y);
         worldCursor.setZ(origin.getBlockZ() + v.getBlockZ());
         return true;
     }
 
     protected void launchFireworks(int fireworksLevel) {
         for (int i=0;i<fireworksLevel;i++) {
             BuildInABox.getInstance().getServer().getScheduler().runTaskLater(BuildInABox.getInstance(), new Runnable() {
                 public void run() {
                     BlockVector vec;
                     Firework fw;
                     for (int ptc=0;ptc<20&&ptc<xzPoints.size();ptc++) {
                         vec = xzPoints.get(ptc);
                         worldCursor.setZ(origin.getBlockZ() + vec.getBlockZ());
                         worldCursor.setX(origin.getBlockX() + vec.getBlockX());
                         fw = (Firework) worldCursor.getWorld().spawnEntity(worldCursor, EntityType.FIREWORK);
                         RandomFireworksGenerator.assignRandomFireworkMeta(fw);
                     }
                 }
             }, i*10);
         }
     }
 }
