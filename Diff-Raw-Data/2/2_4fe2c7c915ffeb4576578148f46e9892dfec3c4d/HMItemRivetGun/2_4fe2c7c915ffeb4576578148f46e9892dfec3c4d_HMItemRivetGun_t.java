 
 package hawksmachinery.items;
 
 import hawksmachinery.HawksMachinery;
 import hawksmachinery.api.HMRepairInterfaces.IHMRepairable;
 import hawksmachinery.api.HMRepairInterfaces.IHMRivet;
 import universalelectricity.prefab.ItemElectric;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EnumAction;
 import net.minecraft.src.EnumMovingObjectType;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MovingObjectPosition;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMItemRivetGun extends ItemElectric
 {
 	public static HawksMachinery BASEMOD;
 	
 	public HMItemRivetGun(int id)
 	{
 		super(id);
 		setTextureFile(BASEMOD.ITEM_TEXTURE_FILE);
 		setItemName("rivetGun");
		setIconIndex(29);
 		
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
 	{
 		if (player.isSneaking() && (item.getItemDamage() < (this.getMaxDamage() - 1) || !item.isItemDamaged()))
 		{
 			player.setItemInUse(item, getMaxItemUseDuration(item));
 		}
 		
 		return item;
 	}
 	
 	@Override
 	public ItemStack onFoodEaten(ItemStack item, World world, EntityPlayer player)
 	{
 		MovingObjectPosition locatedBlock = getMovingObjectPositionFromPlayer(world, player, true);
 		
 		if (locatedBlock != null)
 		{
 			if (locatedBlock.typeOfHit == EnumMovingObjectType.TILE);
 			{
 				TileEntity foundBlock = world.getBlockTileEntity(locatedBlock.blockX, locatedBlock.blockY, locatedBlock.blockZ);
 				
 				if (foundBlock instanceof IHMRepairable)
 				{
 					if (((IHMRepairable)foundBlock).getMaxHP() > 0)
 					{
 						for (int counter = 0; counter <= 8; ++counter)
 						{
 							if (player.inventory.mainInventory[counter] != null)
 							{
 								if (player.inventory.mainInventory[counter].getItem() instanceof IHMRivet)
 								{
 									int potentialRepairAmount = ((IHMRivet)player.inventory.mainInventory[counter].getItem()).getRepairAmount(player.inventory.mainInventory[counter]);
 									
 									if (potentialRepairAmount > 0)
 									{
 										if (((IHMRepairable)foundBlock).attemptToRepair(potentialRepairAmount))
 										{
 											--player.inventory.mainInventory[counter].stackSize;
 											if (player.inventory.mainInventory[counter].stackSize == 0)
 											{
 												player.inventory.mainInventory[counter] = null;
 											}
 											
 											player.swingItem();
 											this.onUse(1, item);
 											return item;
 										}
 										
 									}
 									
 								}
 								
 							}
 							
 						}
 						
 					}
 					else
 					{
 						throw new RuntimeException("Hawk's Machinery: Maximum machine HP must be >0!");
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		return item;
 	}
 	
 	@Override
 	public EnumAction getItemUseAction(ItemStack item)
 	{
 		return EnumAction.bow;
 	}
 	
 	@Override
 	public int getMaxItemUseDuration(ItemStack item)
 	{
 		return 25;
 	}
 	
 	@Override
 	public double getMaxJoules()
 	{
 		return 300;
 	}
 	
 	@Override
 	public double getVoltage()
 	{
 		return 120;
 	}
 	
 	@Override
 	public boolean isFull3D()
 	{
 		return true;
 	}
 	
 }
