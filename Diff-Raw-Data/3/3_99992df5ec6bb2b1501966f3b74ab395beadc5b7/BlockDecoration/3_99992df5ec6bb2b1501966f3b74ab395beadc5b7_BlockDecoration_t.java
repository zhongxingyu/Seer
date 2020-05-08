 package mods.themike.modjam.blocks;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
import mods.themike.modjam.ModJam;
 import mods.themike.modjam.utils.MultiBlockUtils;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class BlockDecoration extends Block {
 	
 	private static Icon[] subIcons = new Icon[MultiBlockUtils.getArray().length];
 
 	public BlockDecoration(int par1) {
 		super(par1, Material.rock);
		this.setCreativeTab(ModJam.tab);
 	}
 	
 	@Override
 	public Icon getBlockTextureFromSideAndMetadata(int side, int metadata) {
 		return subIcons[metadata];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg) {
 		for(int par1 = 0; par1 < MultiBlockUtils.getArray().length; par1++) {
 			subIcons[par1] = reg.registerIcon("mikejam:" + MultiBlockUtils.getArray()[par1]);
 		}
 	}
 	
 	@Override
 	public int damageDropped(int meta) {
 		return meta;
 	}
 	
 	@Override
 	public void getSubBlocks(int ID, CreativeTabs tab, List subItems) {
 		for(int par1 = 0; par1 < MultiBlockUtils.getArray().length; par1++) {
 			subItems.add(new ItemStack(this, 1, par1));
 		}
 	}
 
 }
