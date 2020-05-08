 package com.kaijin.InventoryStocker;
 
 import net.minecraft.src.forge.*;
 import java.util.*;
 
 import com.kaijin.InventoryStocker.*;
 
 import net.minecraft.src.*;
 import net.minecraft.src.mod_InventoryStocker.*;
 
public class BlockInventoryStocker extends Block implements ITextureProvider, IConnectRedstone
 {
     public BlockInventoryStocker(int i, int j)
     {
         super(i, j, Material.ground);
     }
 
     public void addCreativeItems(ArrayList itemList)
     {
         itemList.add(new ItemStack(this));
     }
 
     public String getTextureFile()
     {
         return "/com/kaijin/InventoryStocker/textures/terrain.png";
     }
 
     public int getBlockTextureFromSide(int i)
     {
         switch (i)
         {
             case 0: // Bottom
                 return 16;
 
             case 1: // Top
                 return 0;
 
             case 2: // North
                 return 16;
 
             case 3: // South
                 return 16;
 
             default: // 4-5 West-East
                 return 16;
         }
     }
 
     public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int i)
     {
         int m = blocks.getBlockMetadata(x, y, z);
         int dir = m & 7;
         int side = Utils.lookupRotatedSide(i, dir);
         int powered = (m & 8) >> 3;
 
         TileEntity tile = blocks.getBlockTileEntity(x, y, z);
 
         // Sides (0-5) are: Front, Back, Top, Bottom, Left, Right
         if (side == 0) // Front
         {
             int time = (int)tile.worldObj.getWorldTime();
             return 2 + powered * (((time >> 2) & 3) + 1);
         }
         
         int open = tile instanceof TileEntityInventoryStocker ? (((TileEntityInventoryStocker)tile).doorOpenOnSide(i) ? 2 : 0) : 0;
         
         if (side == 1) // Back
         {
             return 32 + powered + open;
         }
 
         return 16 + powered + open; // Top, Bottom, Left, Right
     }
 
     private int determineOrientation(World world, int x, int y, int z, EntityPlayer player)
     {
         if (player.rotationPitch > 45D)
         {
             return 0;
         }
 
         if (player.rotationPitch < -45D)
         {
             return 1;
         }
 
         int dir = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
         return dir == 0 ? 3 : (dir == 1 ? 4 : (dir == 2 ? 2 : (dir == 3 ? 5 : 0)));
     }
 
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving)
     {
         int dir = determineOrientation(world, x, y, z, (EntityPlayer)par5EntityLiving);
         world.setBlockMetadataWithNotify(x, y, z, dir);
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityplayer)
     {
         // Prevent GUI pop-up and handle block rotation
         if (entityplayer.isSneaking())
         {
             // Rotate block if hand is empty
             if (entityplayer.getCurrentEquippedItem() == null)
             {
                 int i = world.getBlockMetadata(x, y, z);
                 int dir = i & 7; // Get orientation from first 3 bits of meta data
                 i ^= dir; // Clear those bits
                 ++dir; // Rotate
 
                 if (dir > 5)
                 {
                     dir = 0;    // Start over
                 }
 
                 i |= dir; // Write orientation back to meta data value
                 world.setBlockMetadataWithNotify(x, y, z, i); // And store it
                 world.markBlockNeedsUpdate(x, y, z);
             }
 
             // Block GUI popup when sneaking
             return false;
         }
 
         // Duplicate part of onNeighborBlockChange to ensure status is up-to-date before GUI opens
         TileEntityInventoryStocker tile = (TileEntityInventoryStocker)world.getBlockTileEntity(x, y, z);
         if (tile != null)
         {
             tile.onUpdate();
         }
 
         if (!CommonProxy.isClient(world))
         {
             entityplayer.openGui(mod_InventoryStocker.instance, 1, world, x, y, z);
         }
 
         return true;
     }
 
     @Override
     public TileEntity getTileEntity(int metadata)
     {
         return new TileEntityInventoryStocker();
     }
 
     @Override
     public boolean canProvidePower()
     {
        return false; // Old means of causing visual RedPower wire connections.
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int X, int Y, int Z, int direction)
    {
         return true; // Will appear to connect to RedPower wires and such.
        // Currently still causes redstone dust to appear to connect in some cases where it shouldn't; Not our fault.
     }
 
     @Override
     public boolean hasTileEntity(int metadata)
     {
         return true;
     }
 
     /**
      * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
      * their own) Args: x, y, z, neighbor blockID
      */
     public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
     {
         super.onNeighborBlockChange(world, x, y, z, blockID);
         TileEntityInventoryStocker tile = (TileEntityInventoryStocker)world.getBlockTileEntity(x, y, z);
         if (tile != null)
         {
             tile.onUpdate();
         }
     }
 
     public void onBlockPlaced(World world, int x, int y, int z, int facing)
     {
         // TileEntity tile = world.getBlockTileEntity(x, y, z);
     }
 
     public void onBlockRemoval(World world, int x, int y, int z)
     {
         preDestroyBlock(world, x, y, z);
         super.onBlockRemoval(world, x, y, z);
     }
     
     public static void dropItems(World world, ItemStack stack, int i, int j, int k)
     {
         float f1 = 0.7F;
         double d = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
         double d1 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
         double d2 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
         EntityItem entityitem = new EntityItem(world, (double) i + d,
                 (double) j + d1, (double) k + d2, stack);
         entityitem.delayBeforeCanPickup = 10;
         world.spawnEntityInWorld(entityitem);
     }
 
     public static void dropItems(World world, IInventory inventory, int i, int j, int k)
     {
         for (int l = 0; l < inventory.getSizeInventory(); ++l)
         {
             ItemStack items = inventory.getStackInSlot(l);
 
             if (items != null && items.stackSize > 0)
             {
                 dropItems(world, inventory.getStackInSlot(l).copy(), i, j, k);
             }
         }
     }
 
     public static void preDestroyBlock(World world, int i, int j, int k)
     {
         TileEntity tile = world.getBlockTileEntity(i, j, k);
 
         if (tile instanceof IInventory && !CommonProxy.isClient(world))
         {
             dropItems(world, (IInventory) tile, i, j, k);
         }
     }
 
 }
