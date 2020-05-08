 package MysticWorld.Items;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 
 import org.lwjgl.input.Mouse;
 
 import MysticWorld.MysticWorld;
 import MysticWorld.Entity.EntityChargeAir;
 
 public class ItemStaffAir extends ItemStaff {
 	
 	public ItemStaffAir(int id) 
 	{
 		super(id);
 	}
 	
 	@Override
 	public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) 
 	{
 		if (itemStack.stackTagCompound == null)
     		itemStack.setTagCompound(new NBTTagCompound());
 
 		EntityPlayer player = (EntityPlayer)entity;
 		ItemStack currentItem = player.inventory.getCurrentItem();	
 		
 		itemStack.stackTagCompound.setInteger("maxChargeTime", 3 * 20);
 						
 		if (!world.isRemote)
 		{
 			if (currentItem != null)
 			{
 				if (currentItem.itemID == itemStack.itemID)
 				{
 					player.fallDistance = 0.0F;
 					MysticWorld.proxy.airFeetFX(world, (player.posX - 0.5D) + rand.nextDouble(), player.posY + (player.height/itemStack.stackTagCompound.getInteger("maxChargeTime") * itemStack.stackTagCompound.getInteger("chargeTime")), (player.posZ - 0.5D) + rand.nextDouble(), 1.0f + ((float)(itemStack.stackTagCompound.getInteger("chargeTime")) * (5.0f * (1 / (float)itemStack.stackTagCompound.getInteger("maxChargeTime")))));
 				
 					if (!Mouse.isButtonDown(1))
 					{
 						if (itemStack.stackTagCompound.getInteger("chargeTime") > 0)
 						{
 							world.playAuxSFXAtEntity((EntityPlayer)null, 1009, (int)player.posX, (int)player.posY, (int)player.posZ, 0);
 						
 							if (!world.isRemote)
 							{
 								world.spawnEntityInWorld(new EntityChargeAir(world, player, 1.0f + ((float)(itemStack.stackTagCompound.getInteger("chargeTime")) * (5.0f * (1 / (float)itemStack.stackTagCompound.getInteger("maxChargeTime")))), (float)(itemStack.stackTagCompound.getInteger("chargeTime")) * (2.5D * (1.0D / (double)itemStack.stackTagCompound.getInteger("maxChargeTime")))));
 					        
 								itemStack.damageItem(1, player);
 							}					  
 						}
 					}
 					
					if (Mouse.isButtonDown(1))
 					{
 						if (itemStack.stackTagCompound.getBoolean("charging") == true)
 						{
 							itemStack.stackTagCompound.setInteger("chargeTime", itemStack.stackTagCompound.getInteger("chargeTime") + 1);
 						}
 					}
 					else
 					{
 						itemStack.stackTagCompound.setInteger("chargeTime", 0);
 						itemStack.stackTagCompound.setBoolean("charging", false);
 					}	
 				}	
 			}
 		}
 		
 		if (itemStack.stackTagCompound.getInteger("chargeTime") > itemStack.stackTagCompound.getInteger("maxChargeTime"))
 		{
 			itemStack.stackTagCompound.setInteger("chargeTime", itemStack.stackTagCompound.getInteger("maxChargeTime"));
 		}
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
 	{	
 		if (itemStack.stackTagCompound == null)
     		itemStack.setTagCompound(new NBTTagCompound());
 
 		if (itemStack.stackTagCompound.getBoolean("charging") == false)
 		{
 			itemStack.stackTagCompound.setBoolean("charging", true);
 		}
 		
 		return itemStack;
 	}
 	
 }
