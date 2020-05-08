 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.*;
 
 import java.util.Random;
 
 public class BlockRedstoneHardenedTorch extends BlockTorch {
     private boolean torchActive;
 
     public BlockRedstoneHardenedTorch(int id, int textureIndex, boolean isActive) {
         super(id, textureIndex);
         torchActive = isActive;
         setTickOnLoad(true);
     }
 
     @Override
     public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
         if (side == 1) {
             return Block.redstoneWire.getBlockTextureFromSideAndMetadata(side, metadata);
         } else {
             return super.getBlockTextureFromSideAndMetadata(side, metadata);
         }
     }
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
         if (getOrientation(world, x, y, z) == 0) {
             if (world.isBlockOpaqueCube(x - 1, y, z)) {
                 setOrientation(world, x, y, z, 1);
             } else if (world.isBlockOpaqueCube(x + 1, y, z)) {
                 setOrientation(world, x, y, z, 2);
             } else if (world.isBlockOpaqueCube(x, y, z - 1)) {
                 setOrientation(world, x, y, z, 3);
             } else if (world.isBlockOpaqueCube(x, y, z + 1)) {
                 setOrientation(world, x, y, z, 4);
             } else if (world.isBlockOpaqueCube(x, y - 1, z)) {
                 setOrientation(world, x, y, z, 5);
             }
             dropTorchIfCantStay(world, x, y, z);
         }
         if (torchActive) {
             world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
             world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
             world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
             world.notifyBlocksOfNeighborChange(x + 1, y, z, blockID);
             world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
             world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
         }
     }
 
     @Override
     public void onBlockRemoval(World world, int x, int y, int z) {
         if (torchActive) {
             world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
             world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
             world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
             world.notifyBlocksOfNeighborChange(x + 1, y, z, blockID);
             world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
             world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
         }
     }
 
     @Override
     public boolean isPoweringTo(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         if (!torchActive) {
             return false;
         }
         int orientation = getOrientation(iBlockAccess, x, y, z);
         return !(orientation == 5 && direction == 1) && !(orientation == 3 && direction == 3) && !(orientation == 4 && direction == 2) &&
                 !(orientation == 1 && direction == 5) && (orientation != 2 || direction != 4);
     }
 
     private boolean isBeingPowered(World world, int x, int y, int z) {
         int orientation = getOrientation(world, x, y, z);
         return (orientation == 5 && world.isBlockIndirectlyProvidingPowerTo(x, y - 1, z, 0)) || (orientation == 3 && world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2)) ||
                 (orientation == 4 && world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3)) || (orientation == 1 && world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4)) ||
                 (orientation == 2 && world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5));
     }
 
     @Override
     public void updateTick(World world, int x, int y, int z, Random random) {
         boolean powered = isBeingPowered(world, x, y, z);
         if (torchActive) {
             if (powered)
                 world.setBlockAndMetadataWithNotify(x, y, z, mod_redstoneExtended.getInstance().blockRedstoneHardenedTorchIdle.blockID, world.getBlockMetadata(x, y, z));
         } else if (!powered) {
             world.setBlockAndMetadataWithNotify(x, y, z, mod_redstoneExtended.getInstance().blockRedstoneHardenedTorchActive.blockID, world.getBlockMetadata(x, y, z));
         }
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int direction) {
         if (dropTorchIfCantStay(world, x, y, z)) {
             int orientation = getOrientation(world, x, y, z);
             boolean toBeRemoved = false;
             if (!world.isBlockOpaqueCube(x - 1, y, z) && orientation == 1) {
                 toBeRemoved = true;
             }
             if (!world.isBlockOpaqueCube(x + 1, y, z) && orientation == 2) {
                 toBeRemoved = true;
             }
             if (!world.isBlockOpaqueCube(x, y, z - 1) && orientation == 3) {
                 toBeRemoved = true;
             }
             if (!world.isBlockOpaqueCube(x, y, z + 1) && orientation == 4) {
                 toBeRemoved = true;
             }
             if (!world.isBlockOpaqueCube(x, y - 1, z) && orientation == 5) {
                 toBeRemoved = true;
             }
             if (toBeRemoved) {
                dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
                 world.setBlockWithNotify(x, y, z, 0);
             }
         }
         world.scheduleBlockUpdate(x, y, z, blockID, getType(world, x, y, z) ? 1 : 2);
     }
 
     @Override
     public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int direction) {
         return direction == 0 && isPoweringTo(world, x, y, z, direction);
     }
 
     @Override
     public int idDropped(int i, Random random) {
         return mod_redstoneExtended.getInstance().blockRedstoneHardenedTorchActive.blockID;
     }
 
     @Override
     protected int damageDropped(int i) {
         return ((i & 0x8) >> 3);
     }
 
     @Override
     public int getRenderType() {
         return mod_redstoneExtended.getInstance().renderBlockTorchExtended;
     }
 
     @Override
     public void randomDisplayTick(World world, int x, int y, int z, Random random) {
         if (!torchActive) {
             return;
         }
         int orientation = getOrientation(world, x, y, z);
         double positionX = (double) ((float) x + 0.5F) + (double) (random.nextFloat() - 0.5F) * 0.20000000000000001D;
         double positionY = (double) ((float) y + 0.7F) + (double) (random.nextFloat() - 0.5F) * 0.20000000000000001D;
         double positionZ = (double) ((float) z + 0.5F) + (double) (random.nextFloat() - 0.5F) * 0.20000000000000001D;
         double offsetYWhenOnBlockSide = 0.2199999988079071D;
         double offsetXZWhenOnBlockSide = 0.27000001072883606D;
         if (orientation == 1) {
             world.spawnParticle("reddust", positionX - offsetXZWhenOnBlockSide, positionY + offsetYWhenOnBlockSide, positionZ, 0.0D, 0.0D, 0.0D);
         } else if (orientation == 2) {
             world.spawnParticle("reddust", positionX + offsetXZWhenOnBlockSide, positionY + offsetYWhenOnBlockSide, positionZ, 0.0D, 0.0D, 0.0D);
         } else if (orientation == 3) {
             world.spawnParticle("reddust", positionX, positionY + offsetYWhenOnBlockSide, positionZ - offsetXZWhenOnBlockSide, 0.0D, 0.0D, 0.0D);
         } else if (orientation == 4) {
             world.spawnParticle("reddust", positionX, positionY + offsetYWhenOnBlockSide, positionZ + offsetXZWhenOnBlockSide, 0.0D, 0.0D, 0.0D);
         } else {
             world.spawnParticle("reddust", positionX, positionY, positionZ, 0.0D, 0.0D, 0.0D);
         }
     }
 
     @Override
     public void onBlockPlaced(World world, int x, int y, int z, int direction) {
         int orientation = getOrientation(world, x, y, z);
         if (direction == 1 && world.isBlockOpaqueCube(x, y - 1, z)) {
             orientation = 5;
         }
         if (direction == 2 && world.isBlockOpaqueCube(x, y, z + 1)) {
             orientation = 4;
         }
         if (direction == 3 && world.isBlockOpaqueCube(x, y, z - 1)) {
             orientation = 3;
         }
         if (direction == 4 && world.isBlockOpaqueCube(x + 1, y, z)) {
             orientation = 2;
         }
         if (direction == 5 && world.isBlockOpaqueCube(x - 1, y, z)) {
             orientation = 1;
         }
         setOrientation(world, x, y, z, orientation);
     }
 
     @Override
     public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3D vec3d, Vec3D vec3d1) {
         int orientation = getOrientation(world, x, y, z);
         float f = 0.15F;
         if (orientation == 1) {
             setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
         } else if (orientation == 2) {
             setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
         } else if (orientation == 3) {
             setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
         } else if (orientation == 4) {
             setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
         } else {
             float f1 = 0.1F;
             setBlockBounds(0.5F - f1, 0.0F, 0.5F - f1, 0.5F + f1, 0.6F, 0.5F + f1);
         }
         return super.collisionRayTrace(world, x, y, z, vec3d, vec3d1);
     }
 
     private boolean dropTorchIfCantStay(World world, int x, int y, int z) {
         if (!canPlaceBlockAt(world, x, y, z)) {
             dropBlockAsItem(world, x, y, z, getType(world, x, y, z) ? 1 : 0);
             world.setBlockWithNotify(x, y, z, 0);
             return false;
         } else {
             return true;
         }
     }
 
     private int getOrientation(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return (metadata & 0x7);
     }
 
     private void setOrientation(World world, int x, int y, int z, int orientation) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = ((oldMetadata & 0x8) | (orientation & 0x7));
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     private boolean getType(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return ((metadata & 0x8) >> 3) == 1;
     }
 }
