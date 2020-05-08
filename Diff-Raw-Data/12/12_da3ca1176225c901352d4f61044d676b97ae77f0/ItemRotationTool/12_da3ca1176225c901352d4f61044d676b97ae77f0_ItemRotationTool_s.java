 package modJam;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemEnchantedBook;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.world.World;
 
 public class ItemRotationTool extends Item{
 
 	public ItemRotationTool(int par1) {
 		super(par1);
 		this.setMaxDamage(250);
 	}
 	
 	@Override
 	public int getItemStackLimit(){
 		return 1;
 	}
 	
     public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2)
     {
     	ItemEnchantedBook handlerItem = null;
     	if(itemstack2.itemID == Item.enchantedBook.itemID){
     		handlerItem = (ItemEnchantedBook)Item.itemsList[itemstack2.itemID];
     		NBTTagList nbttaglist = handlerItem.func_92110_g(itemstack2);
 
             if (nbttaglist != null)
             {
                 if (nbttaglist.tagCount() == 1){
                     short short1 = ((NBTTagCompound)nbttaglist.tagAt(0)).getShort("id");
                     if (short1 == 34){
                         return true;
                     }
                 }
             }
     	}
         return false;
     }
	
     /**
      * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
      * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
      */
 	@Override
     public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
     {
     	int currentBlock = par3World.getBlockId(par4, par5, par6);
     	boolean wasSuccessful = false;
 		if (currentBlock == ModJam.woodChairIDs[0] || currentBlock == ModJam.woodChairIDs[1] || currentBlock == ModJam.woodChairIDs[2]
 		|| currentBlock == ModJam.woodChairIDs[3] || currentBlock == ModJam.stoneChairIDs[0] || currentBlock == ModJam.stoneChairIDs[1] || currentBlock == ModJam.stoneChairIDs[2]
 		|| currentBlock == ModJam.stoneChairIDs[3]) {
 			wasSuccessful = BlockChair.handleRotation(par3World, par4, par5, par6);
     	}
 		if(wasSuccessful){
 			par1ItemStack.damageItem(2, par2EntityPlayer);
 			return true;
 		}
         return false;
     }
 	
     @Override
     /**
      * returns the action that specifies what animation to play when the items is being used
      */
     public EnumAction getItemUseAction(ItemStack par1ItemStack)
     {
         return EnumAction.block;
     }
     
 	@Override
 	public void updateIcons(IconRegister par1IconRegister){
 		this.iconIndex = par1IconRegister.registerIcon("AwesomeMod:fuj1n.AwesomeMod.rotationTool");
 	}
 
 }
