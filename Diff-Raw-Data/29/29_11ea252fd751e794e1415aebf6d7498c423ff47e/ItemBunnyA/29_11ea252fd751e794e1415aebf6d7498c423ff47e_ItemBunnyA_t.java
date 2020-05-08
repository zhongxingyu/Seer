 package com.mrgreaper.twisted.items;
 
import java.util.Random;

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
 		int playerX = (int) Minecraft.getMinecraft().thePlayer.lastTickPosX;
 		int playerY = (int) Minecraft.getMinecraft().thePlayer.lastTickPosY;
 		int playerZ = (int) Minecraft.getMinecraft().thePlayer.lastTickPosZ;
 		System.out.println(playerX + " " + playerY + " " + playerZ);
 		if (!player.worldObj.isRemote){
			Random randomGenerator = new Random(); //test of random
			int randomInt = randomGenerator.nextInt(3);
			System.out.println("the random number was "+randomInt);
			if (randomInt == 0){
			Sounds.CREATED_MONSTER.play (playerX ,playerY, playerZ, 150, 1);
			}
			if (randomInt == 1){
				Sounds.BUNNY_RELEASE.play (playerX ,playerY, playerZ, 150, 1);
				}
			if (randomInt == 2){
				Sounds.BUNNY_LITTLE.play (playerX ,playerY, playerZ, 150, 1);
				}
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
