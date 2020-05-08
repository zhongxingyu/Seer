 package dk.slashwin.openframe;
 
 import dk.slashwin.openframe.common.CoordTuple;
 import dk.slashwin.openframe.lib.BlockIDs;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Facing;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 import java.util.*;
 
 /**
  * User: DelusionalLogic
  * Date: 20-04-13
  * Time: 14:21
  */
 public class BlockMotor extends BlockContainer
 {
     Icon sideIcon;
 
     TileMotor tileMotor;
 
     public BlockMotor(int id)
     {
         super(id, Material.iron);
 
         setCreativeTab(CreativeTabs.tabRedstone);
         setUnlocalizedName("Frame Motor");
     }
 
     @Override
     public boolean isOpaqueCube()
     {
         return true;
     }
 
     @Override
     public int getRenderType()
     {
         return -1;
     }
 
     @Override
     public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
     {
         return true;
     }
 
     @Override
     public TileEntity createNewTileEntity(World world)
     {
         return new TileMotor();
     }
 
     @Override
     public void registerIcons(IconRegister iconRegister)
     {
         blockIcon = iconRegister.registerIcon("openframe:motorTop");
         sideIcon = iconRegister.registerIcon("openframe:motorSide");
     }
 
     @Override
     public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
     {
         TileMotor tileMotor = (TileMotor)world.getBlockTileEntity(x, y, z);
         int topSide = tileMotor.getTopSide();
         return topSide > 5 ? this.sideIcon : (topSide == side ? blockIcon : (topSide == Facing.oppositeSide[topSide] ? this.sideIcon : this.sideIcon));
     }
 
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float relX, float relY, float relZ)
     {
         if (world.isRemote)
             return true;
 
         TileMotor tileMotor = (TileMotor)world.getBlockTileEntity(x, y, z);
 
         if(tileMotor.getTopSide() == side)
             tileMotor.setOrientation(tileMotor.getOrientation() + 1);
         else
             tileMotor.setTopSide(side);
 
         world.markBlockForUpdate(x, y, z);
         return true;
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
     {
         if(!world.isRemote)
         {
             if(world.isBlockIndirectlyGettingPowered(x, y, z))
             {
                 world.setWorldTime(500);
                 tryMove(world, x, y, z);
             }
         }
     }
 
     private boolean tryMove(World world, int x, int y, int z)
     {
         TileMotor tileMotor = (TileMotor)world.getBlockTileEntity(x, y, z);
         ForgeDirection topSide = ForgeDirection.getOrientation(tileMotor.getTopSide());
 
         int blockX = x + topSide.offsetX;
         int blockY = y + topSide.offsetY;
         int blockZ = z + topSide.offsetZ;
 
         ForgeDirection pushDirection = tileMotor.getPushDirection();
 
         Comparator<CoordTuple> comparator = null;
         if(pushDirection.offsetX != 0)
             comparator = new xComparator(pushDirection.offsetX < 0);
         else if(pushDirection.offsetY != 0)
             comparator = new yComparator(pushDirection.offsetY < 0);
         else if(pushDirection.offsetZ != 0)
             comparator = new zComparator(pushDirection.offsetZ < 0);
 
         ArrayList<CoordTuple> blocksToMove = new ArrayList<CoordTuple>(getBlocksToMove(world, blockX, blockY, blockZ));
         blocksToMove.remove(new CoordTuple(x, y, z));
         Collections.sort(blocksToMove, comparator);
 
         boolean canMove = true;
         for(CoordTuple coordTuple : blocksToMove)
         {
             if(blocksToMove.contains(new CoordTuple(coordTuple.x + pushDirection.offsetX, coordTuple.y + pushDirection.offsetY, coordTuple.z + pushDirection.offsetZ))) continue;
             if(!canPushBlock(world, coordTuple.x, coordTuple.y, coordTuple.z, pushDirection))
                 canMove = false;
         }
 
         if(!canMove)
             return false;
 
         for(CoordTuple coordTuple : blocksToMove)
         {
             if(!tryMoveBlock(world, coordTuple.x, coordTuple.y, coordTuple.z, pushDirection))
                 continue;
         }
 
         return true;
     }
 
     private LinkedHashSet<CoordTuple> getBlocksToMove(World world, int blockX, int blockY, int blockZ)
     {
         TileEntity tileEntity = world.getBlockTileEntity(blockX, blockY, blockZ);
 
         LinkedHashSet<CoordTuple> blocks = new LinkedHashSet<CoordTuple>();
         blocks.add(new CoordTuple(blockX, blockY, blockZ));
 
         if(tileEntity != null)
         {
             if(tileEntity instanceof TileFrame)
             {
                 blocks = ((TileFrame) tileEntity).getNeighbors(blocks);
             }
         }
         return blocks;
     }
 
     private boolean canPushBlock(World world, int blockX, int blockY, int blockZ, ForgeDirection pushDirection)
     {
         int newX = blockX + pushDirection.offsetX;
         int newY = blockY + pushDirection.offsetY;
         int newZ = blockZ + pushDirection.offsetZ;
 
         if(world.getBlockId(newX, newY, newZ) != 0)
             return false;
 
         return true;
     }
 
     private boolean tryMoveBlock(World world, int blockX, int blockY, int blockZ, ForgeDirection pushDirection)
     {
         int newX = blockX + pushDirection.offsetX;
         int newY = blockY + pushDirection.offsetY;
         int newZ = blockZ + pushDirection.offsetZ;
 
         TileEntity oldTileEntity = world.getBlockTileEntity(blockX, blockY, blockZ);
         NBTTagCompound nbtTag = new NBTTagCompound();
 
         if(oldTileEntity != null)
         {
             if(oldTileEntity instanceof TileMovingBlock)
                 return false;
 
             oldTileEntity.writeToNBT(nbtTag);
             world.removeBlockTileEntity(blockX, blockY, blockZ);
         }
 
         int oldBlockID = world.getBlockId(blockX, blockY, blockZ);
         int oldMetadata = world.getBlockMetadata(blockX, blockY, blockZ);
 
         if(oldBlockID == 0)
             return false;
 
         if(!canPushBlock(world, blockX, blockY, blockZ, pushDirection))
             return false;
 
        world.setBlock(blockX, blockY, blockZ, 0);
         world.setBlock(newX, newY, newZ, BlockIDs.movingID);
         TileEntity tileEntity = world.getBlockTileEntity(newX, newY, newZ);
         if(tileEntity instanceof TileMovingBlock)
         {
             if(nbtTag != null)
                 ((TileMovingBlock) tileEntity).setNBTData(nbtTag);
             ((TileMovingBlock) tileEntity).setBlockData(oldBlockID, oldMetadata);
             ((TileMovingBlock) tileEntity).setMoveDirection(pushDirection);
         }
         return true;
     }
 }
