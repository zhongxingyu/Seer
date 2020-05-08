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
 		this.setCreativeTab(ModMain.tabMoP_WIP);
 		this.setStepSound(Block.blockClay.stepSound);
 	}
 
 	@Override
 	public void registerIcons(IconRegister reg)
 	{
		this.blockIcon = reg.registerIcon(ModInfo.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName().substring(5));
 	}
     /**
      * Returns the ID of the items to drop on destruction.
      */
 	public int idDropped(int par1, Random par2Random, int par3)
     {
         return ModInfo.oyster.itemID;
     }
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return 4;
     }
     
 }
