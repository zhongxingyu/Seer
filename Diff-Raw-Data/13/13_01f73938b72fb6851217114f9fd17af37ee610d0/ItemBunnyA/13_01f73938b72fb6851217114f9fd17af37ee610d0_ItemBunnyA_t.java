 package com.mrgreaper.twisted.items;
 
 import com.mrgreaper.twisted.TwistedMod;
 import com.mrgreaper.twisted.client.sounds.Sounds;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemBunnyA extends Item {
 
 	public ItemBunnyA(int id) {
 		super(id);
 		setCreativeTab(TwistedMod.tabTwisted);
 		setMaxStackSize(1);
 		setUnlocalizedName(ItemInfo.BUNNYA_UNLOCALIZED_NAME);
 	}
 	
 	@Override
 	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
		//int playerX;
		int playerX = (int) Minecraft.getMinecraft().thePlayer.lastTickPosX;
		int playerY = (int) Minecraft.getMinecraft().thePlayer.lastTickPosY;
		int playerZ = (int) Minecraft.getMinecraft().thePlayer.lastTickPosZ;
		System.out.println(playerX + " " + playerY + " " + playerZ);
		//EntityPlayerMP entityplayermpx = playerNetServerHandler.managedPosX;
		//		 EntityPlayerMP locx = EntityPlayerMP.managedPosX;
//		EntityPlayerMP playermp = (EntityPlayerMP)entityplayer;
 		if (!player.worldObj.isRemote){
			Sounds.CREATED_MONSTER.play (playerX ,playerY, playerZ, 3, 1);
 		}else{
 			Minecraft.getMinecraft().thePlayer.addChatMessage("Dont let The evil vile creature go free!");
 		}
 	}
 	
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	//here is where we add the textures etc, has to be client side ofcourse
 	public void registerIcons(IconRegister register) {
 		itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BUNNYA_ICON);
 		
 	}
 
 	
 	
 
 }
