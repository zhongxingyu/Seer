 package com.norcode.bukkit.buildinabox;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.server.v1_5_R3.Packet61WorldEvent;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.norcode.bukkit.buildinabox.util.CuboidClipboard;
 import com.norcode.bukkit.buildinabox.util.RandomFireworksGenerator;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.blocks.BaseBlock;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 
 public abstract class BuildingPlanTask implements Runnable {
     private static Random random = new Random();
     BuildInABox plugin;
     public BukkitWorld bukkitWorld;
     public BuildChest buildChest;
     public BlockFace buildDirection = BlockFace.UP;
     public LinkedList<BlockUpdate> finalPoints = new LinkedList<BlockUpdate>();
     public LinkedList<BlockUpdate> points = new LinkedList<BlockUpdate>();
     public boolean shuffle;
     public int blocksPerTick;
     public CuboidClipboard clipboard;
     private boolean onFinalPass = false;
     private BlockVector origin;
     private int currentY;
     protected boolean cancelled;
     private List<Player> nearbyPlayers = new ArrayList<Player>();
     public static final HashSet<Integer> postLayerBlockIds = new HashSet<Integer>();
     public static final HashSet<Integer> postBuildBlockIds = new HashSet<Integer>();
     static {
         // These are placed after each vertical layer is complete.
         postLayerBlockIds.add(Material.BED_BLOCK.getId());
         postLayerBlockIds.add(Material.COCOA.getId());
         postLayerBlockIds.add(Material.TORCH.getId());
         postLayerBlockIds.add(Material.REDSTONE_TORCH_ON.getId());
         postLayerBlockIds.add(Material.REDSTONE_TORCH_OFF.getId());
         postLayerBlockIds.add(Material.WALL_SIGN.getId());
         postLayerBlockIds.add(Material.WOOD_BUTTON.getId());
         postLayerBlockIds.add(Material.STONE_BUTTON.getId());
         postLayerBlockIds.add(Material.TRAP_DOOR.getId());
         postLayerBlockIds.add(Material.TRIPWIRE_HOOK.getId());
         // These are placed VERY last.
         postBuildBlockIds.add(Material.VINE.getId());
         postBuildBlockIds.add(Material.WOODEN_DOOR.getId());
         postBuildBlockIds.add(Material.IRON_DOOR.getId());
         postBuildBlockIds.add(Material.LEVER.getId());
         
     }
 
     public BuildingPlanTask(CuboidClipboard clipboard, BuildChest buildChest, BlockFace buildDirection, int blocksPerTick, boolean shuffle) {
         this.plugin = BuildInABox.getInstance();
         this.bukkitWorld = new BukkitWorld(buildChest.getLocation().getWorld());
         this.clipboard = clipboard;
         this.buildChest = buildChest;
         this.buildDirection = buildDirection;
         this.blocksPerTick = blocksPerTick;
         this.shuffle = shuffle;
         Location cl = buildChest.getLocation();
         Vector off = clipboard.getOffset();
         origin = new BlockVector(cl.getBlockX() + off.getBlockX(), cl.getBlockY() + off.getBlockY(), cl.getBlockZ() + off.getBlockZ());
     }
 
     public boolean getMorePoints() {
         if (onFinalPass) {
             return false;
         } else {
             currentY += buildDirection.getModY();
             if (currentY < 0 || currentY >= clipboard.getSize().getBlockY()) {
                 onFinalPass = true;
                 if (finalPoints.isEmpty()) {
                     return false;
                 }
                 points = finalPoints;
                 return true;
             }
             BlockVector v;
             for (int x=0;x<clipboard.getSize().getBlockX();x++) {
                 for (int z=0;z<clipboard.getSize().getBlockZ();z++) {
                     v = new BlockVector(x, currentY, z);
                     points.add(new BlockUpdate(v, clipboard.getPoint(v), true));
                 }
             }
             if (shuffle) {
                 Collections.shuffle(points);
             }
             return true;
         }
     }
 
     public Location getWorldLocationFor(BlockVector v) {
         return new Location(bukkitWorld.getWorld(), 
                 origin.getBlockX() + v.getBlockX(), 
                 origin.getBlockY() + v.getBlockY(), 
                 origin.getBlockZ() + v.getBlockZ());
     }
 
     public void start() {
         currentY = buildDirection.equals(BlockFace.UP) ? 0 : clipboard.getSize().getBlockY()-1;
         BlockVector v;
         for (int x=0;x<clipboard.getSize().getBlockX();x++) {
             for (int z=0;z<clipboard.getSize().getBlockZ();z++) {
                 v = new BlockVector(x, currentY, z);
                 points.add(new BlockUpdate(v, clipboard.getPoint(v), true));
             }
         }
         if (shuffle) {
             Collections.shuffle(points);
         }
         plugin.getServer().getScheduler().runTaskLater(plugin, this, 1);
      // Collect the nearby players for sending fake block breaks etc.
         nearbyPlayers.clear();
         double maxDistance = 16 + (Math.max(clipboard.getSize().getBlockX(), clipboard.getSize().getBlockZ())/2);
         Location center = new Location(
                 bukkitWorld.getWorld(), 
                 origin.getX() + clipboard.getSize().getBlockX()/2,
                 origin.getY() + clipboard.getSize().getBlockY()/2,
                 origin.getZ() + clipboard.getSize().getBlockZ()/2);
         for (Player p: bukkitWorld.getWorld().getPlayers()) {
             if (p.getLocation().distance(center) < maxDistance) {
                 nearbyPlayers.add(p);
             }
             nearbyPlayers.add(p);
         }
     }
 
     public void run() {
         boolean finished = false;
         int bpt = blocksPerTick;
         BlockProcessResult result;
         BlockUpdate update;
         onRunStart();
         while (bpt > 0 && !cancelled) {
             if (points.isEmpty()) {
                 if (!getMorePoints()) {
                     finished = true;
                     break;
                 }
             }
             update = points.removeFirst();
             result = processBlockUpdate(update);
             switch (result) {
             case PROCESSED:
                 bpt--;
                 break;
             case QUEUE_AFTER_LAYER:
                 update.setCanQueue(false);
                 points.addLast(update);
                 break;
             case QUEUE_FINAL:
                 update.setCanQueue(false);
                 finalPoints.addLast(update);
                 break;
             case DISCARD:
                 break;
             }
         }
         onRunEnd();
         if (cancelled) {
             onComplete();
         } else if (!finished) {
             // reschedule
             plugin.getServer().getScheduler().runTaskLater(plugin, this, 1);
         } else {
             onComplete();
         }
     }
 
     public abstract void onComplete();
 
     public void onRunStart() {}
     public void onRunEnd() {}
     
     public abstract BlockProcessResult processBlockUpdate(BlockUpdate update);
 
     public void copyFromClipboard(BaseBlock bb, Location wc, Player attributeToPlayer) {
         // Send a BlockPlace event for loggers to rollback maybe.
         BlockState bs = wc.getBlock().getState();
         BlockPlaceEvent bpe = new BlockPlaceEvent(wc.getBlock(), bs, 
                 wc.getBlock().getRelative(BlockFace.DOWN), null, attributeToPlayer, true);
         plugin.getServer().getPluginManager().callEvent(bpe);
         bs.setTypeId(bb.getType());
         bs.setRawData((byte) bb.getData());
        bs.update(true, false);
         bukkitWorld.copyToWorld(new BlockVector(wc.getBlockX(), wc.getBlockY(), wc.getBlockZ()), bb);
     }
 
     public void sendAnimationPacket(int x, int y, int z, int type) {
         Packet61WorldEvent packet = new Packet61WorldEvent(2001, x, y, z, type, false);
         for (Player p: nearbyPlayers) {
             if (p.isOnline()) {
                 ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
             }
         }
     }
 
     protected void launchFireworks(int fireworksLevel) {
         
         final int y = clipboard.getSize().getBlockY() + origin.getBlockY();
         final int x = clipboard.getSize().getBlockX();
         final int z = clipboard.getSize().getBlockZ();
         final Location worldCursor = new Location(bukkitWorld.getWorld(), origin.getBlockX(), y, origin.getBlockZ());
         for (int i=0;i<fireworksLevel;i++) {
             BuildInABox.getInstance().getServer().getScheduler().runTaskLater(BuildInABox.getInstance(), new Runnable() {
                 public void run() {
                     Firework fw;
                     for (int j=0;j<Math.max(x*z,20);j++) {
                         worldCursor.setX(random.nextInt(x)+origin.getBlockX());
                         worldCursor.setZ(random.nextInt(z)+origin.getBlockZ());
                         fw = (Firework) worldCursor.getWorld().spawnEntity(worldCursor, EntityType.FIREWORK);
                         RandomFireworksGenerator.assignRandomFireworkMeta(fw);
                     }
                 }
             }, i*10);
         }
     }
 
     public static enum BlockProcessResult {
         QUEUE_AFTER_LAYER,
         QUEUE_FINAL,
         DISCARD,
         PROCESSED
     }
 
     public class BlockUpdate {
         BlockVector pos;
         BaseBlock block;
         boolean canQueue;
         public BlockUpdate(BlockVector pos, BaseBlock block, boolean canQueue) {
             this.pos = pos;
             this.block = block;
             this.canQueue = canQueue;
         }
         public BlockVector getPos() {
             return pos;
         }
         public void setPos(BlockVector pos) {
             this.pos = pos;
         }
         public BaseBlock getBlock() {
             return block;
         }
         public void setBlock(BaseBlock block) {
             this.block = block;
         }
         public boolean isCanQueue() {
             return canQueue;
         }
         public void setCanQueue(boolean canQueue) {
             this.canQueue = canQueue;
         }
     }
 }
