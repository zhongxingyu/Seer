 package silentAbyss.block;
 
 import java.util.List;
 
 import silentAbyss.item.AbyssGem;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class BrickSlab extends BlockHalfSlab {
 
 	public BrickSlab(int par1, boolean par2) {
 		
 		super(par1, par2, Material.rock);
 		this.setHardness(3.0f);
 		this.setResistance(10.0f);
 		this.setStepSound(Block.soundStoneFootstep);
 		this.setCreativeTab(CreativeTabs.tabBlock);
 		Block.useNeighborBrightness[par1] = true;
 	}
 
 	@Override
 	public String getFullSlabName(int i) {
 
 		return null;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public Icon getIcon(int side, int meta) {
 		
 		return Brick.icons[meta & 7];
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs tab, List subItems) {
 		
 		for (int i = 0; i < Brick.icons.length; ++i)  {
 			subItems.add(new ItemStack(this, 1, i));
 		}
 	}
 	
 	@Override
 	public int damageDropped(int par1) {
 		
 		return par1;
 	}
 	
 	public static void addRecipes() {
 		
 		for (int i = 0; i < Brick.icons.length; ++i) {
 			GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.brickSlab, 6, i), "sss",
 					's', new ItemStack(ModBlocks.brick, 1, i));
 		}
 	}
 }
