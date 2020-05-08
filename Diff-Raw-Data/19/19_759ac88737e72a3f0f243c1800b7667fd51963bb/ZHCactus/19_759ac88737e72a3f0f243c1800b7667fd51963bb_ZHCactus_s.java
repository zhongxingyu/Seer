 package zh.usefulthings.crops;
 
 import java.util.List;
 import java.util.Random;
 
 import zh.usefulthings.UsefulThings;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCactus;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Direction;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class ZHCactus extends BlockCactus
 {
 
 	private String _unlocalizedName;
 	
 	@SideOnly(Side.CLIENT)
 	private Icon zhCactusFlower;
 	
 	public ZHCactus(int par1) 
 	{
 		super(par1);
 	}
 	
 	//Turns into cactus when dropped
	public int idDropped()
 	{
 		return Block.cactus.blockID;
 	}
 	
 	public int idPicked(World world, int x, int y, int z)
 	{
 		return Block.cactus.blockID;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister)
 	{
 		super.registerIcons(iconRegister);
 		zhCactusFlower = iconRegister.registerIcon("UsefulThings:cactusFlower");
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta)
 	{
 		if(side == 1)
 			return zhCactusFlower;
 		else
 			return super.getIcon(side, meta);
 	}
 	
 	@Override
 	public void updateTick(World world, int x, int y, int z, Random rand)
     {
 		boolean generated = false;
 		
 		if(rand.nextInt(20) < world.getBlockMetadata(x, y, z) + 5)
 		{
 			//Generate the fruit - loop through all four sides
 	        for (int i = 0; i < 4 && !generated; i++)
 	        {
 	        	if (world.isAirBlock(x + Direction.offsetX[Direction.rotateOpposite[i]], y, z + Direction.offsetZ[Direction.rotateOpposite[i]]))
 	        	{
 	        		if(rand.nextInt(4) >= 1)
 	        		{
 	        			world.setBlock(x + Direction.offsetX[Direction.rotateOpposite[i]], y, z + Direction.offsetZ[Direction.rotateOpposite[i]],UsefulThings.cactusFruitBlock.blockID);
 	        			world.setBlockMetadataWithNotify(x + Direction.offsetX[Direction.rotateOpposite[i]], y, z + Direction.offsetZ[Direction.rotateOpposite[i]], 0 << 2 | i, 2);
 	        			world.setBlockMetadataWithNotify(x, y, z, 0, 2);
 	        			generated = true;
 	        		}
 	        	}
 	        }
 		}
 		else if (world.getBlockMetadata(x, y, z) < 16)
 		{
 			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x,y,z) + 1,2);
 		}
 			
 	}
 	
 	@Override
 	public Block setUnlocalizedName(String par1Str)
     {
 		this._unlocalizedName = par1Str;
 		return super.setUnlocalizedName(par1Str);
     }
 	
 	@Override
 	public boolean canBlockStay(World world, int x, int y, int z)
     {
         if (world.getBlockMaterial(x - 1, y, z).isSolid())
         {
             return false;
         }
         else if (world.getBlockMaterial(x + 1, y, z).isSolid())
         {
             return false;
         }
         else if (world.getBlockMaterial(x, y, z - 1).isSolid())
         {
             return false;
         }
         else if (world.getBlockMaterial(x, y, z + 1).isSolid())
         {
             return false;
         }
         else
         {
         	for(int i = 1; i <= 3; i++)
         	{
         		if(world.getBlockId(x, y - i, z) != Block.cactus.blockID)
         			return false;
         	}
             
         	return true;
         }
     }
 
 }
