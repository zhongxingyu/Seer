 package com.mrgreaper.twisted.items;
 
 import com.mrgreaper.twisted.TwistedMod;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import universalelectricity.core.item.ItemElectric;
 
 public class ItemBunnyE extends ItemElectric{
 
 	public ItemBunnyE(int id) {
 		super(id);
 		setCreativeTab(TwistedMod.tabTwisted);
 		setMaxStackSize(1);
 		setUnlocalizedName(ItemInfo.BUNNYE_UNLOCALIZED_NAME);
		//setMaxDamage(1000);
 		setNoRepair();
 	}
 	//this.setElectricity(itemstack, 10000);
 
 /*	public void elecbunnyhelper(ItemStack itemstack, int charge,EntityPlayer entityplayer) {
 		itemstack = itemstack.setEnergyStored();
 		itemstack.setEnergyStored(charge);
 	entityplayer.inventory.addItemStackToInventory(itemstack);
 	};*/
 	
 	
 	@Override
 	public void onCreated(ItemStack itemStack, World world, EntityPlayer par3EntityPlayer)
 	{
 		this.setElectricity(itemStack, 1000000);
 		System.out.println("have i been called? ");
 	}
 	    
     @Override
 	@SideOnly(Side.CLIENT)
 	//here is where we add the textures etc, has to be client side ofcourse
 	public void registerIcons(IconRegister register) {
 	itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BUNNYE_ICON);
 	}
 
 
 	@Override
 	public float getMaxElectricityStored(ItemStack theItem) {
 	// TODO Auto-generated method stub
	return 1000000000;
 	}
 }
