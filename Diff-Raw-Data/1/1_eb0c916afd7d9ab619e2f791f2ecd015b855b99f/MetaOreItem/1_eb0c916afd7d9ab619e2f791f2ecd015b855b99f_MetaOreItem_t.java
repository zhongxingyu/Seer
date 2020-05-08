 package com.metatechcraft.block;
 
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
 	
 }
