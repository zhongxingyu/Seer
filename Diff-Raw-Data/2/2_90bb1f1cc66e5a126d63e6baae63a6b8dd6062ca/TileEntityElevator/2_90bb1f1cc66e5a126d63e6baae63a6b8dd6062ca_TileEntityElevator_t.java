 package jk_5.nailed.blocks.tileentity;
 
 import com.google.common.base.Preconditions;
 import jk_5.nailed.blocks.NailedBlocks;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.common.ForgeDirection;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class TileEntityElevator extends NailedTileEntity {
 
     private static final int MAX_DISTANCE = 32;
     private static final int MAX_PASSABLE_BLOCKS = 32;
 
     private boolean canTeleportPlayer(int x, int y, int z) {
         int blockId = worldObj.getBlockId(x, y, z);
         Block block = Block.blocksList[blockId];
         if (block == null || block.isAirBlock(worldObj, x, y, z)) return true;
 
         final AxisAlignedBB aabb = block.getCollisionBoundingBoxFromPool(worldObj, x, y, z);
         return aabb == null || aabb.getAverageEdgeLength() < 0.7;
     }
 
     private static boolean isPassable(int blockId) {
         return !Block.isNormalCube(blockId);
     }
 
     private int findLevel(ForgeDirection direction) {
         Preconditions.checkArgument(direction == ForgeDirection.UP || direction == ForgeDirection.DOWN, "Must be either up or down");
 
         int blocksInTheWay = 0;
         final int delta = direction.offsetY;
         for (int i = 0, y = yCoord; i < MAX_DISTANCE; i++) {
             y += delta;
             if (!worldObj.blockExists(xCoord, y, zCoord)) break;
             if (worldObj.isAirBlock(xCoord, y, zCoord)) continue;
 
             int blockId = worldObj.getBlockId(xCoord, y, zCoord);
             int meta = worldObj.getBlockMetadata(xCoord, y, zCoord);
 
             if (blockId == NailedBlocks.stat.blockID && meta == 2) {
                 TileEntity otherBlock = worldObj.getBlockTileEntity(xCoord, y, zCoord);
                 if (otherBlock instanceof TileEntityElevator) {
                     if (canTeleportPlayer(xCoord, y + 1, zCoord) && canTeleportPlayer(xCoord, y + 2, zCoord)) return y;
                 }
             }
 
             if (!isPassable(blockId) && (++blocksInTheWay > MAX_PASSABLE_BLOCKS)) break;
         }
 
         return -1;
     }
 
     private void activate(EntityPlayer player, ForgeDirection dir) {
         int level = findLevel(dir);
         if (level >= 0) {
             player.setPositionAndUpdate(xCoord + 0.5, level + 1.1, zCoord + 0.5);
            //worldObj.playSoundAtEntity(player, "nailed:teleport", 1F, 1F);
         }
     }
 
     public void onMovementEvent(byte b, EntityPlayer player){
         switch(b){
             case 0:
                 activate(player, ForgeDirection.UP);
                 break;
             case 1:
                 activate(player, ForgeDirection.DOWN);
                 break;
         }
     }
 }
