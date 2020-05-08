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
    private Icon[] iconArray;
    private Icon[] iconArray1;
 	
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
        int l = par2 & 15;
        return (par1 == 1 || par1 == 0) ? this.iconArray1[l] : this.iconArray[l];

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
        this.iconArray1 = new Icon[16];
         
 	    for (int i = 0; i < this.iconArray.length; ++i)        {
            this.iconArray[i] = par1IconRegister.registerIcon("Gwycraft:bookcase_" + i);
            this.iconArray1[i] = par1IconRegister.registerIcon("Gwycraft:plank_" + i);
 		}
 	}
 
 }
