 package com.github.rossrkk.utilities.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 import com.github.rossrkk.utilities.lib.Strings;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class CobbleHolder extends Item {
 
     public CobbleHolder(int id) {
         super(id);
         maxStackSize = 1;
         this.setUnlocalizedName(Strings.COBBLE_HOLDER_NAME);
         this.setCreativeTab(CreativeTabs.tabTools);
         this.setNoRepair();
         this.setMaxDamage(2304);
         this.setNoRepair();
     }
 
     public ItemStack onItemRightClick(ItemStack itemStack, World world,
             EntityPlayer player) {
         
         //the inventory being searched
         ItemStack[] inventory = player.inventory.mainInventory;
         
         // if cobble has been absorbed
         boolean cobbleAbsorbed = false;
         
         if (!player.isSneaking() && (itemStack.getItemDamage() < getMaxDamage())) {
             // loop through each stack in the inventory and check if it's
             // cobble
             for (int i = 1; i < 36; i++) {
             	try{
             		
 	                if(inventory[i].itemID == 4) {
 	                    // add the size of the itemstack to cobbleheld
 	                    itemStack.setItemDamage(itemStack.getItemDamage() + inventory[i].stackSize);
 	                }
             	} catch (NullPointerException e){/*Swallowed*/}
             	
             }
             // clear the inventory of cobble
             player.inventory.clearInventory(4, -1);
         } else {
         	
         	//loop while there are items remaining to give
         	//while (itemStack.getItemDamage() > 0){
         	
         		//if there is more than a stack to give, give a stack
             	if (itemStack.getItemDamage() >= 64){
                 	if (player.inventory.addItemStackToInventory(new ItemStack(Block.cobblestone, 64))) {
 	                	//remove a stack from the total left to give
 	                	this.setDamage(itemStack, getDamage(itemStack) - 64);
 	                	}
             	} else {
             		//if there is less than a stack to give give what's left
             		if (player.inventory.addItemStackToInventory(new ItemStack(Block.cobblestone, getMaxDamage() - itemStack.getItemDamage()))) {
            			this.setDamage(itemStack, 0);
             		}
             	}
         	//}
         }
         return itemStack;
     }
     
     @Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) {
 		itemIcon = register.registerIcon(Strings.TEXTURE_LOCATION + ":" + Strings.COBBLE_HOLDER_NAME);
 	}
 
 }
