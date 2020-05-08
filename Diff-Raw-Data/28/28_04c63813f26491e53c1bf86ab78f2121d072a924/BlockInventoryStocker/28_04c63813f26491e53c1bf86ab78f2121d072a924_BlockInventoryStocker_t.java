 package kaijin.InventoryStocker;
 
 import net.minecraft.src.forge.*;
 import java.util.*;
 import net.minecraft.src.*;
 import net.minecraft.src.mod_InventoryStocker.*;
 import kaijin.InventoryStocker.*;
 
 public class BlockInventoryStocker extends BlockContainer implements ITextureProvider
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
         return "/kaijin/InventoryStocker/terrain.png";
     }
 
     public int getBlockTextureFromSide(int i)
     {
         switch (i)
         {
             case 0: // Bottom
                 return 0;
 
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
 
     public int getBlockTextureFromSideAndMetadata(int i, int m)
     {
         int dir = m & 7;
         int side = Utils.lookupRotatedSide(i, dir);
         // Sides (0-5) are: Front, Back, Top, Bottom, Left, Right
 
         switch (side)
         {
             case 0: // Front
                 return 1;
 
             case 1: // Back
                 return 0;
 
             case 2:
             case 3:
                 if (dir < 2)
                 {
                     return 16;
                 }
 
                 return 18;
 
             case 4:
             case 5:
             default:
                 return 16;
         }
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
         // Prevent GUI popup and handle block rotation
         if (entityplayer.isSneaking())
         {
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
 
             return false;
         }
 
         if (!Utils.isClient(world))
         {
             entityplayer.openGui(mod_InventoryStocker.instance, 1, world, x, y, z);
         }
 
         return true;
     }
 
     @Override
     public TileEntity getBlockEntity()
     {
         return new TileEntityInventoryStocker();
     }
 
     @Override
     public boolean canProvidePower()
     {
        return true; // Will appear to connect to RedPower wires and such.
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
         Utils.preDestroyBlock(world, x, y, z);
         super.onBlockRemoval(world, x, y, z);
     }
 }
