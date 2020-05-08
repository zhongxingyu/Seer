 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.*;
 
 import java.util.Random;
 
 public abstract class BlockRedstoneFlipFlop extends Block {
     public BlockRedstoneFlipFlop(int id) {
         super(id, Block.stairSingle.getBlockTextureFromSideAndMetadata(1, 0), Material.circuits);
         setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
     }
 
     private static boolean getStateFromMetadata(int metadata) {
         return ((metadata & 0x8) >> 3) == 1;
     }
 
     private static int getOrientationFromMetadata(int metadata) {
         return metadata & 0x3;
     }
 
     private static int setStateInMetadata(int metadata, boolean state) {
         return ((metadata & 0x7) | ((state ? 1 : 0) << 3) & 0x8);
     }
 
     private static int setOrientationInMetadata(int metadata, int orientation) {
         return ((metadata & 0xC) | (orientation & 0x3));
     }
 
     public static boolean getState(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getStateFromMetadata(metadata);
     }
 
     public static int getOrientation(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getOrientationFromMetadata(metadata);
     }
 
     protected static void setState(World world, int x, int y, int z, boolean state) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setStateInMetadata(oldMetadata, state);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     private static void setOrientation(World world, int x, int y, int z, int orientation) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setOrientationInMetadata(oldMetadata, orientation);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public int tickRate() {
         return 2;
     }
 
     @Override
     public boolean canPlaceBlockAt(World world, int x, int y, int z) {
         return world.isBlockOpaqueCube(x, y - 1, z) &&
                 super.canPlaceBlockAt(world, x, y, z);
     }
 
     @Override
     public boolean canBlockStay(World world, int x, int y, int z) {
         return world.isBlockOpaqueCube(x, y - 1, z) &&
                 super.canBlockStay(world, x, y, z);
     }
 
     @Override
     public abstract void onNeighborBlockChange(World world, int x, int y, int z, int direction);
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
         world.notifyBlocksOfNeighborChange(x + 1, y, z, blockID);
         world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
     }
 
     @Override
     public void onBlockRemoval(World world, int x, int y, int z) {
         world.notifyBlocksOfNeighborChange(x + 1, x, z, blockID);
         world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
     }
 
     @Override
     public void updateTick(World world, int x, int y, int z, Random random) {
         setState(world, x, y, z, !getState(world, x, y, z));
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer activator) {
         world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
 
         return true;
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving creator) {
         int orientation = ((MathHelper.floor_double((double)((creator.rotationYaw * 4F) / 360F) + 0.5D) & 0x3) + 2) % 4;
         setOrientation(world, x, y, z, orientation);
     }
 
     @Override
     public abstract int idDropped(int i, Random random);
 
     @Override
     public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int direction) {
         return isPoweringTo(world, x, y, z, direction);
     }
 
     @Override
     public abstract boolean isPoweringTo(IBlockAccess iBlockAccess, int x, int y, int z, int direction);
 
     @Override
     public boolean canProvidePower() {
         return true;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public int getRenderType() {
         return mod_redstoneExtended.getInstance().renderBlockRedstoneFlipFlop;
     }
 
     @Override
     public abstract int getBlockTextureFromSide(int side);
 
     protected boolean isInputTopBeingPowered(World world, int x, int y, int z) {
         switch (getOrientation(world, x, y, z)) {
             case 0:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2);
             case 2:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3);
             case 3:
                 return world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4);
             case 1:
                 return world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5);
             default:
                 return false;
         }
     }
 
     protected boolean isInputLeftBeingPowered(World world, int x, int y, int z) {
         switch (getOrientation(world, x, y, z)) {
             case 0:
                 return world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4);
             case 2:
                 return world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5);
             case 3:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3);
             case 1:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2);
             default:
                 return false;
         }
     }
 
     protected boolean isInputBottomBeingPowered(World world, int x, int y, int z) {
         switch (getOrientation(world, x, y, z)) {
             case 0:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3);
             case 2:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2);
             case 3:
                 return world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5);
             case 1:
                 return world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4);
             default:
                 return false;
         }
     }
 
     protected boolean isInputRightBeingPowered(World world, int x, int y, int z) {
         switch (getOrientation(world, x, y, z)) {
             case 0:
                 return world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5);
             case 2:
                 return world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4);
             case 3:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2);
             case 1:
                 return world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3);
             default:
                 return false;
         }
     }
 
     protected boolean isOutputBottom(int direction, int orientation) {
         return ((orientation == 0 && direction == 2) || (orientation == 1 && direction == 5) ||
                 (orientation == 2 && direction == 3) || (orientation == 3 && direction == 4));
     }
 
     protected boolean isOutputTop(int direction, int orientation) {
         return ((orientation == 0 && direction == 3) || (orientation == 1 && direction == 4) ||
                 (orientation == 2 && direction == 2) || (orientation == 3 && direction == 5));
     }
 
     protected boolean isOutputRight(int direction, int orientation) {
         return ((orientation == 0 && direction == 4) || (orientation == 1 && direction == 2) ||
                 (orientation == 2 && direction == 5) || (orientation == 3 && direction == 3));
     }
 }
