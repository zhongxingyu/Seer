 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.*;
 
 import java.util.Random;
 
 public class BlockRedstoneRandom extends BlockRedstoneFlipFlop {
     private static final int textureInputs = ModLoader.addOverride("/terrain.png", "/redstoneExtended/flipFlops/Random/inputs.png");
     private static final int textureOutput = ModLoader.addOverride("/terrain.png", "/redstoneExtended/flipFlops/Random/output.png");
 
     private static final Random random = new Random();
 
     public BlockRedstoneRandom(int id) {
         super(id);
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int direction) {
         if (!canBlockStay(world, x, y, z)) {
             dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
             world.setBlockWithNotify(x, y, z, 0);
         }
 
         boolean inputClockBeingPowered = isInputLeftBeingPowered(world, x, y, z);
 
         if (inputClockBeingPowered && !getLastClockState(world, x, y, z)) {
             if (random.nextBoolean() != getState(world, x, y, z))
                 world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
         }
 
         setLastClockState(world, x, y, z, inputClockBeingPowered);
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer activator) {
         if (random.nextBoolean() != getState(world, x, y, z))
             world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
 
         return true;
     }
 
     @Override
     public int idDropped(int i, Random random) {
        return mod_redstoneExtended.getInstance().itemRedstoneJKFlipFlop.shiftedIndex;
     }
 
     @Override
     public boolean isPoweringTo(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         boolean state = getState(iBlockAccess, x, y, z);
         int orientation = getOrientation(iBlockAccess, x, y, z);
         return (state && isOutputRight(direction, orientation));
     }
 
     @Override
     public int getBlockTextureFromSide(int side) {
         switch (side) {
             case 6:
                 return textureInputs;
             case 7:
                 return textureOutput;
             case 8:
                 return mod_redstoneExtended.getInstance().emptyTexture;
             default:
                 return Block.stairSingle.getBlockTextureFromSideAndMetadata(side, 0);
         }
     }
 
     private static boolean getLastClockStateFromMetadata(int metadata) {
         return ((metadata & 0x4) >> 2) == 1;
     }
 
     private static int setLastClockStateInMetadata(int metadata, boolean lastClockState) {
         return ((metadata & 0xB) | (((lastClockState ? 1 : 0) << 2) & 0x4));
     }
 
     private static boolean getLastClockState(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getLastClockStateFromMetadata(metadata);
     }
 
     private static void setLastClockState(World world, int x, int y, int z, boolean lastClockState) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setLastClockStateInMetadata(oldMetadata, lastClockState);
         world.setBlockMetadata(x, y, z, newMetadata);
     }
 }
