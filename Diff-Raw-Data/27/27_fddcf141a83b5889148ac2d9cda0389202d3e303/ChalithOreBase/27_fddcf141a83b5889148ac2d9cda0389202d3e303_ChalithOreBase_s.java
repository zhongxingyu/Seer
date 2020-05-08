 package tektor.minecraft.chalith.blocks;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class ChalithOreBase extends BlockOre{
 
	@SideOnly(Side.CLIENT)
 	private Icon[] icon = new Icon[2];
 	public ChalithOreBase(int id) {
 		super(id);
 		setHardness(3.2F);
 		setStepSound(Block.soundStoneFootstep);
 		setUnlocalizedName("Ore");
 		setCreativeTab(CreativeTabs.tabBlock);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		icon[0] = par1IconRegister.registerIcon("chalith:lorynOre");
 		icon[1] = par1IconRegister.registerIcon("chalith:sorfynOre");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int par1,
 	           int par2)
 	{
 		return icon[par2];
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs tab, List subItems) {
 
 		subItems.add(new ItemStack(this, 1, 0));
 		subItems.add(new ItemStack(this, 1, 1));
 	}
 	
 	@Override
 	public int damageDropped(int par1)
 	{
 		 return par1;
 	}
 
 }
