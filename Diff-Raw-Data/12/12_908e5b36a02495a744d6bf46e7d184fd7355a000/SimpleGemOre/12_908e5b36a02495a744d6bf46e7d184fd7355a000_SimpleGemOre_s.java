 package SimpleOres.core.api;
 
 import java.util.Random;
 
 import SimpleOres.core.Items;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class SimpleGemOre extends Block
 {
 	private String modName;
 	private int itemIDToDrop;
 	
 	/**
 	 * This allows you to easily create a new ore block that drops an item. Only supports one item drop though.
 	 * 
 	 * @param id = ID of the block.
 	 * @param material = The material of the block. This sets what tools are effective against it, the sounds it makes, etc.
 	 */
 	public SimpleGemOre(int id, Material material, String mod, int itemDropID)
 	{
 		super(id, material);
 		this.modName = mod;
		this.itemIDToDrop = itemDropID;
 	}
 	
 	/**
 	 * Sets the texture for the block.
 	 */
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) 
 	{
 		 this.blockIcon = iconRegister.registerIcon(modName + ":" + (this.getUnlocalizedName().substring(5)));
 	}
 	
 	/**
 	 * Sets what item or block is dropped when the block is broken.
 	 * For all the ores except onyx, they will drop themselves. Onyx will drop the gem item.
 	 */
     public int idDropped(int i, Random random, int j)
     {
    	try
     	{
             return itemIDToDrop; 
     	}
    	catch(Exception e)
    	{
    		
    	}
    	return this.blockID;
     }
     
     /**
      * If the block drops something that is not itself (which is the case for onyx), then a random amount of XP is dropped.
      * This random amount is within the range of 16 to 33.
      */
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
         super.dropBlockAsItemWithChance(par1World, par2, par3, par4, par5, par6, par7);
 
         if (this.idDropped(par5, par1World.rand, par7) != this.blockID)
         {
         	int var8 = MathHelper.getRandomIntegerInRange(par1World.rand, 16, 33);
 
             this.dropXpOnBlockBreak(par1World, par2, par3, par4, var8);
         }
     }
     
     /**
      * Determines the quantity dropped when you use a Fortune pickaxe. Only applies to blocks that don't drop themselves (onyx ore).
      */
     public int quantityDroppedWithBonus(int par1, Random par2Random)
     {
         if (par1 > 0 && this.blockID != this.idDropped(0, par2Random, par1))
         {
             int var3 = par2Random.nextInt(par1 + 2) - 1;
 
             if (var3 < 0)
             {
                 var3 = 0;
             }
 
             return this.quantityDropped(par2Random) * (var3 + 1);
         }
         else
         {
             return this.quantityDropped(par2Random);
         }
     }
     
     /**
      * Sets whether or not Silk Touch has an effect on this block. Returns true so that Onyx Ore will drop itself rather than the gem item.
      */
     protected boolean canSilkHarvest()
     {
         return true;
     }
 
 }
