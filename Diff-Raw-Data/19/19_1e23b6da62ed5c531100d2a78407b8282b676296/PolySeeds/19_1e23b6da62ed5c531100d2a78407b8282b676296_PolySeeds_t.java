 /**
  * 
  */
 package dokutoku.lead.zotonic.crop.seed;
 
 import java.util.HashMap;
 
 import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import dokutoku.lead.zotonic.crop.EnumCropType;
 import dokutoku.lead.zotonic.crop.PolyCrop;
 import dokutoku.lead.zotonic.lib.Configs;
 import dokutoku.lead.zotonic.lib.Reference;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 
 /**
  * Codename: Lead Zotonic
  *
  * PolySeeds
  *
  * @author Atomfusion/DokuToku
  * @license MIT License (http://opensource.org/licenses/MIT)
  */
 public class PolySeeds extends ItemSeeds implements IPlantable {
 	
 	private String dropType;
 	private ItemStack smeltProduct;
 	private EnumCropType plantType;
 	private int blockType;
 	private int _id;
 	private int _blockType;
 
 	public PolySeeds(int par1, int par2, int par3, ItemStack smeltProduct, EnumCropType plantType) {
 		super(par1, par2, par3);
 		
 		this.blockType = par2;
 		this.smeltProduct = smeltProduct;
 		this.plantType = plantType;
 		this._id = par1;
 		this._blockType = par2;
 		
 		this.setCreativeTab(Configs.cTab);
 	}
 	
 	@Override
 	public void registerIcons(IconRegister iconRegister)
 	{
 		itemIcon = iconRegister.registerIcon(Reference.MOD_ID+":" + dropType.toLowerCase() + "_seed");
 	}
 	
 	public ItemStack getProduct() {
 		
 		return smeltProduct;
 		
 	}
 	
 	public EnumCropType getPolyType()
 	{
 		return plantType;
 	}
 	
 	
 	/**
 	 * @param string
 	 * @return
 	 */
 	public PolySeeds setType(String string) {
 		dropType = string;
 		return this;
 	}
 	
 	// A little unfortunate, the from-derived method leaves a lot to be desired.
 	// Here's a modification of it.
 	
 	@Override
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
 	{
		//boolean result = super.onItemUse(par1ItemStack, par2EntityPlayer, par3World, par4, par5, par6, par7, par8, par9, par10);
 		
 		if (par7 != 1)
         {
             return false;
         }
         else if (par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7, par1ItemStack) && par2EntityPlayer.canPlayerEdit(par4, par5 + 1, par6, par7, par1ItemStack))
         {
             int i1 = par3World.getBlockId(par4, par5, par6);
             Block soil = Block.blocksList[i1];
 
             if (soil != null && canSustainPlant(par3World, par4, par5, par6) && par3World.isAirBlock(par4, par5 + 1, par6))
             {
                 par3World.setBlock(par4, par5 + 1, par6, this.blockType);
                 --par1ItemStack.stackSize;
                 return true;
             }
             else
             {
                 return false;
             }
         }
         else
         {
             return false;
         }
 	}
 	
 	private boolean canSustainPlant(World world, int x, int y, int z)
 	{
 		return ((PolyCrop)Block.blocksList[this.blockType]).canThisPlantGrowOnThisBlockID(world.getBlockId(x, y, z));
 	}
 }
