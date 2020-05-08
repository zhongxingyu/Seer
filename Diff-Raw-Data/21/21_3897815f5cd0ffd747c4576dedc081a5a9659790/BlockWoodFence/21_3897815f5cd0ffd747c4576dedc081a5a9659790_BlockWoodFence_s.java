 package net.mcft.copy.vanilladj.block;
 
 import java.util.List;
 
 import net.mcft.copy.vanilladj.misc.WoodUtils;
 import net.minecraft.block.BlockFence;
 import net.minecraft.block.BlockWood;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockWoodFence extends BlockFence {
 	
 	private Icon[] icons;
 	
 	public BlockWoodFence(int id) {
 		super(id, null, Material.wood);
 		setHardness(2.0F);
 		setResistance(5.0F);
 		setStepSound(soundWoodFootstep);
 		setUnlocalizedName("fence");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		icons = WoodUtils.registerIcons(iconRegister);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
 		return icons[Math.min(meta, icons.length - 1)];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int id, CreativeTabs tabs, List list) {
 		for (int i = 0; i < BlockWood.woodType.length; i++)
 			list.add(new ItemStack(id, 1, i));
 	}
 	
 }
