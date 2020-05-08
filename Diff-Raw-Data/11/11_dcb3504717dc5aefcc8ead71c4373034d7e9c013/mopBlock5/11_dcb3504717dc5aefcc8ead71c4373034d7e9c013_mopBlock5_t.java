 package tcc.MotherOfPearl.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockClay;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import tcc.MotherOfPearl.ModInfo;
 import tcc.MotherOfPearl.ModMain;
 
 public class mopBlock5 extends Block {
 
 	public mopBlock5(int par1, Material par2Material) {
 		super(par1, par2Material);
 		// TODO Auto-generated constructor stub
 		this.setUnlocalizedName("clayOyster");
		this.setHardness(.5F);
 		this.setCreativeTab(ModMain.tabMoP);
 		this.setStepSound(Block.blockClay.stepSound);
 	}
 


 	@Override
 	public void registerIcons(IconRegister reg)
 	{
 		this.blockIcon = reg.registerIcon("minecraft:clay");
 	}
     /**
      * Returns the ID of the items to drop on destruction.
      */
 	
 	/*The website
 	 * http://www.minecraftforum.net/topic/1329625-help-making-a-block-drop-different-items-based-on-chance/
 	 * to help me with random chance at dropping clay or oysters.*/
 	public int idDropped(int par1, Random par2Random, int par3)
     {
 		int quickvar = par2Random.nextInt(25) + 1;
 		if(quickvar <= 1)
 		{
 			return ModInfo.oyster.itemID;
 		}
 		else
 		{
 			return Item.clay.itemID;
 		}
     }
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return 4;
     }
     
 }
