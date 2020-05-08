 /*
  *  Copyright (C) 2013  emris
  *  https://github.com/emris/LeatherWaterSacTFC
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package emris.mods.LeatherWaterSacTFC;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import TFC.Core.Player.TFC_PlayerServer;
 import TFC.Food.FoodStatsTFC;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemLeatherWaterSac extends Item {
 
 	public static int itemID;
 	
 	public ItemLeatherWaterSac(int par1) {
 		super(par1);
 		
 		maxStackSize = 1;
 		setCreativeTab(CreativeTabs.tabMisc);
 		setMaxDamage(12);
 		hasSubtypes = false;
 		LanguageRegistry.addName(this, "Leather Water Sac");
 		itemID = 256 + par1;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
     public void updateIcons(IconRegister registerer)
     {
 		iconIndex = registerer.registerIcon("LeatherWaterSacTFC:LeatherWaterSac");
     }
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,	EntityPlayer par3EntityPlayer) {
 		if (par3EntityPlayer.capabilities.isCreativeMode) {
 			par1ItemStack.setItemDamage(0);
 		}
 		
 		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);
 		if (mop == null) {
 			if(par1ItemStack.getItemDamage() == par1ItemStack.getMaxDamage()){
 				if(par3EntityPlayer instanceof EntityPlayerMP) { par3EntityPlayer.sendChatToPlayer("Empty!"); }
 			} else {
 				par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 			}
 			return par1ItemStack;
 		} else {
 			if(mop.typeOfHit == EnumMovingObjectType.TILE) {
 				int x = mop.blockX;
 				int y = mop.blockY;
 				int z = mop.blockZ;
 				
 				if(!par2World.canMineBlock(par3EntityPlayer, x,  y,  z)) {
 					return par1ItemStack;
 				}
 				
 				if(!par3EntityPlayer.canPlayerEdit(x, y, z, mop.sideHit, par1ItemStack)) {
 					return par1ItemStack;
 				}
 				
 				if(par2World.getBlockMaterial(x, y, z) == Material.water) {
 					if(par1ItemStack.getItemDamage() > 0) {
 						if (!par3EntityPlayer.capabilities.isCreativeMode) {
 							par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() - 1);
 						}
 					} else {
 						par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 					}
 				} else {
 					if(par1ItemStack.getItemDamage() == par1ItemStack.getMaxDamage()){
 						if(par3EntityPlayer instanceof EntityPlayerMP) { par3EntityPlayer.sendChatToPlayer("Empty!"); }
 					} else {
 						par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 					}
 				}
 			}
 			return par1ItemStack;
 		}
 	}
 
 	@Override
 	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
 		return EnumAction.drink;
 	}
 
 	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
         return 32;
     }
 
 	@Override
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
 		if(!par2World.isRemote) {
 			if(par1ItemStack.getItemDamage() != par1ItemStack.getMaxDamage() || par3EntityPlayer.capabilities.isCreativeMode) {
 				if(par3EntityPlayer instanceof EntityPlayerMP) {
 					EntityPlayerMP p = (EntityPlayerMP)par3EntityPlayer;
 					FoodStatsTFC fs = TFC_PlayerServer.getFromEntityPlayer(par3EntityPlayer).getFoodStatsTFC();
					if (fs.getMaxWater(p) - 100 > fs.waterLevel) {
 						float nwl = fs.getMaxWater(p);
 						int rw = (int)nwl / 6;
 						fs.restoreWater(p, rw);
 						if (!par3EntityPlayer.capabilities.isCreativeMode) {
 							par1ItemStack.setItemDamage(par1ItemStack.getItemDamage() + 1);
 						}
 					} else {
 						par2World.playSoundAtEntity(par3EntityPlayer, "random.burp", 0.5F, par2World.rand.nextFloat() * 0.1F + 0.9F);
 						par3EntityPlayer.sendChatToPlayer("I'm full!");
 					}
 				}
 			}
 		}
 		return par1ItemStack;
 	}
 
 	@SideOnly(Side.CLIENT)
     public boolean requiresMultipleRenderPasses() {
         return true;
     }
 	
 }
