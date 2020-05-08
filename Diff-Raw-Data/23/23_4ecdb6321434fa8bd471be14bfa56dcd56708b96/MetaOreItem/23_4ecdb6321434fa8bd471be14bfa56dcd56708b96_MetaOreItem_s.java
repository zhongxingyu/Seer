 package com.metatechcraft.block;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.MathHelper;
 
 public class MetaOreItem extends ItemBlock {
 
 	public MetaOreItem(int par1) {
 		super(par1);
 		setHasSubtypes(true);
 	}
 	
 	@Override
 	public String getItemDisplayName(ItemStack itemStack) {
 		return MetaOreBlock.getItemDisplayName(itemStack);
 	}
 	
 	@Override
 	public int getMetadata(int damageValue) {
 		return damageValue;
 	}
 	@Override
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int id, CreativeTabs creativeTab, List list) {
 		for (int meta = 0; meta < (MetaOreBlock.ORE_NUMBER + 1); meta++) {
 			list.add(new ItemStack(id, 1, meta));
 		}
 	}
 }
