 package zh.usefulthings.food;
 
 import scala.Console;
 import zh.usefulthings.CommonProxy;
 import zh.usefulthings.UsefulThings;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockLog;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ZHFood extends ItemFood 
 {
 
 	private String _unlocalizedName;
 	
 	public ZHFood(int id, int fullness, float saturation, boolean wolfFood) 
 	{
 		super(id,fullness,saturation,wolfFood);
 	}
 	
 	@Override
 	public void registerIcons(IconRegister iconRegister)
 	{
 	         itemIcon = iconRegister.registerIcon("UsefulThings:" + this._unlocalizedName);
 	}
 	
 	@Override
 	public Item setUnlocalizedName(String par1Str)
     {
 		this._unlocalizedName = par1Str;
 		return super.setUnlocalizedName(par1Str);
 			
     }
 	
 	@Override
 	//No idea what par8, par9, or par10 are!
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float par8, float par9, float par10)
     {
 		//Handles placing of cactus berries
 		if (itemStack.itemID == UsefulThings.cactusFruit.itemID)
 		{
 			int y1 = world.getBlockId(x, y, z);
 			int y2 = world.getBlockId(x, y-1, z);
 			int y3 = world.getBlockId(x, y-2, z);
 			int y4 = world.getBlockId(x, y-3, z);
 			boolean y5 = world.isAirBlock(x, y+1, z);
 		
 			//Turn the top most block of a 4-block tall cactus into a fruiting cactus block
			if (y1 == Block.cactus.blockID && y2 == Block.cactus.blockID && y3 == Block.cactus.blockID && y4 == Block.cactus.blockID && y5)
 			{
 				world.setBlock(x, y, z, UsefulThings.zhCactus.blockID,0,2);
 				
 				if (!entityPlayer.capabilities.isCreativeMode)
 		        {
 		            --itemStack.stackSize;
 		        }
 			}
 			//Cactus berry pods can only be placed on the top most block of a four-tall cactus
 			else if (y1 == UsefulThings.zhCactus.blockID)
 			{
 				//side?
 			    if (side == 0) //top?
 			    {
 			        return false;
 			    }
 			
 			    if (side == 1) //bottom?
 			    {
 			        return false;
 			    }
 			
 			    if (side == 2) //east?
 			    {
 			        --z;
 			    }
 			
 			    if (side == 3) //west?
 			    {
 			        ++z;
 			    }
 			
 			    if (side == 4) //north?
 			    {
 			        --x;
 			    }
 			
 			    if (side == 5) //south?
 			    {
 			        ++x;
 			    }
 			
 			    if (world.isAirBlock(x, y, z))
 			    {
 			    	
 			        int k1 = Block.blocksList[UsefulThings.cactusFruitBlock.blockID].onBlockPlaced(world, x, y, z, side, par8, par9, par10, 0);
 			        world.setBlock(x, y, z, UsefulThings.cactusFruitBlock.blockID, k1, 2);
 			
 			        if (!entityPlayer.capabilities.isCreativeMode)
 			        {
 			            --itemStack.stackSize;
 			        }
 			    }
 			
 			    return true;
 			}
 		}
 		
 		return false;
     }
 	
 }
