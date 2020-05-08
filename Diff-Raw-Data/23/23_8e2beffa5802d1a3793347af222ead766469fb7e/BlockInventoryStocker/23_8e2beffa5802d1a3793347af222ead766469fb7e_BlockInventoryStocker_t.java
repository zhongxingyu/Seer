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
         super(i, j, Material.iron);
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
 		// Needs to call tile entity to determine correct facing and animation state?
 		switch (i)
 		{
 		case 0: // Bottom
 			return 16;
 		case 1: // Top
 			return 16;
 		case 2: // North/South
 			return 1;
 		case 3: // North/South
 			return 0;
 		default: // 4-5 East/West
 			return 19;
 		}
 	}
 
 	public int getBlockTextureFromSideAndMetadata(int i, int m)
 	{
 		int dir = m & 7;
 
 		if (dir == i)
 		{
 			// front face
 			return 1;
 		}
 		if (dir <= 1) // Up or down
 		{
 			if (i >= 4)
 			{
				return 16; // Side face
 			}
 			if (i >= 2)
 			{
				return 16; // Top/bottom face (as sides, so using 16 instead of 18 for now)
 			}
 			return 0; // Back face
 		}
 		if (i <= 1) 
 		{
			return 18; // Horizontal, top or bottom face
 		}
		if (dir <= 3)
 		{
			if (i == 4 || i == 5)
 			{
				return 16; // Side face
 			}
 		}
 		else
 		{
			if (i == 2 || i == 3)
 			{
				return 16; // Side face
 			}
 		}
 		return 0; // Back face
 	}
 
     private int determineOrientation(World world, int x, int y, int z, EntityPlayer player)
     {
         if (player.rotationPitch > 45D)
         {
             return 1;
         }
 
         if (player.rotationPitch < -45D)
         {
             return 0;
         }
 
         int dir = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
         return dir == 0 ? 2 : (dir == 1 ? 5 : (dir == 2 ? 3 : (dir == 3 ? 4 : 0)));
     }
 
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving)
     {
         int dir = determineOrientation(world, x, y, z, (EntityPlayer)par5EntityLiving);
         world.setBlockMetadataWithNotify(x, y, z, dir);
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityplayer)
     {
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
     
     public void onBlockPlaced(World world, int x, int y, int z, int facing)
     {
     	// TileEntity tile = world.getBlockTileEntity(x, y, z);
     };
     
 	public void onBlockRemoval(World world, int x, int y, int z)
 	{
 		Utils.preDestroyBlock(world, x, y, z);
 		super.onBlockRemoval(world, x, y, z);
 	}
 }
