 /*******************************************************************************
  * Copyright (c) 2012 Mast3rPlan.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     Mast3rPlan
  ******************************************************************************/
 package net.brokenpineapple.robotcraft.world.items;
 
 import java.util.List;
 
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagString;
 import net.minecraft.src.StatCollector;
 import net.minecraft.src.World;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class TheCoreItem extends Item {
 
 	public TheCoreItem() {
 		super(3702);
 		super.setItemName("theCore");
 		super.setIconIndex(1);
 		super.setTabToDisplayOn(CreativeTabs.tabMaterials);
 		super.setMaxStackSize(1);
 
 		LanguageRegistry.addName(this, "The Core");
 	}
 
 	@Override
 	public String getTextureFile() {
 		return "/net/brokenpineapple/robotcraft/sprites/misc.png";
 	}
 
 	@Override
 	public boolean getShareTag() {
 		return true;
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
 		if(!world.isRemote) {
 			/* func_77983_a = setTag */
 			itemStack.func_77983_a("owner", new NBTTagString(null, player.username));
 		}
 
 		return itemStack;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void addInformation(ItemStack itemStack, List lines)
 	{
 		if (itemStack.hasTagCompound())
 		{
 			NBTTagCompound tags = itemStack.getTagCompound();
 			NBTTagString ownerTag = (NBTTagString)tags.getTag("owner");
 
 			if (ownerTag != null)
 			{
				lines.add("\u00a77" + "Owned by " + ownerTag.data);
 			}
 		}
 	}
 
 
 
 }
