 package gwydion0917.gwycraft;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBookshelf;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import gwydion0917.gwycraft.DyedPlank;
 
 public class DyedBookcase extends BlockBookshelf {
 
 	@SideOnly(Side.CLIENT)
//	private Icon[][] iconBuffer;
	private Icon[] iconArray;
 	
 	public DyedBookcase(int id) {
 		super(id);
 		setUnlocalizedName("dyedBookcase");
 		setCreativeTab(CreativeTabs.tabBlock);
 		setHardness(1.5F);
 		setStepSound(Block.wood.stepSound);
 
 	}
 
 	@Override
     public Icon getBlockTextureFromSideAndMetadata(int par1, int par2) 
 	{
 //       return this.iconArray[par2 % this.iconArray.length];
        return par1 != 1 && par1 != 0 ? super.getBlockTextureFromSideAndMetadata(par1, par2) : Block.planks.getBlockTextureFromSide(par1);
    }
 	
 	@Override
 	public int damageDropped(int metadata) {
 		return metadata;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs tab, List subItems) {
 		for (int i = 0; i < 16; i++) {
 			subItems.add(new ItemStack(this, 1, i));
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 
 	
 	public void registerIcons(IconRegister par1IconRegister) 
 	{
 	    this.iconArray = new Icon[16];
         
 	    for (int i = 0; i < this.iconArray.length; ++i)        {
	        this.iconArray[i] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);

	    //	    this.iconBuffer = new Icon[16][6];
	    
//		for (int i = 0; i < this.iconBuffer.length; ++i) 		{
//		    this.iconBuffer[i][0] = par1IconRegister.registerIcon("Gwycraft:plank_" + i);
//            this.iconBuffer[i][1] = par1IconRegister.registerIcon("Gwycraft:plank_" + i);
//            this.iconBuffer[i][2] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);
//            this.iconBuffer[i][3] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);
//            this.iconBuffer[i][4] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);
//            this.iconBuffer[i][5] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);
 		}
 	}
 
 }
