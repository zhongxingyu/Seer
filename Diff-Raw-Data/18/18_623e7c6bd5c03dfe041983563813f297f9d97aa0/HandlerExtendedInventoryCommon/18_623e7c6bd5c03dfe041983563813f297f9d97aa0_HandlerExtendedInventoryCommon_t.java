 package loecraftpack.ponies.inventory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import loecraftpack.common.items.ItemAccessory;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 
 public class HandlerExtendedInventoryCommon
 {
 	
 	public static InventoryCustom getInventory(EntityPlayer player, InventoryId id)
 	{
 		if(player.worldObj.isRemote)
 		{
 			return HandlerExtendedInventoryClient.getInventory(player, id);
 		}
 		else
 		{
 			return HandlerExtendedInventoryServer.getInventory(player, id);
 		}
 	}
 	
 	/**
 	 * get the inventory position of any existing Accessories
 	 */
 	public static List<Integer> getAccessorySlotIds(EntityPlayer player, InventoryCustom inv)
 	{
 		List<Integer> accessorySlotIds = new ArrayList<Integer>();
 		ItemStack targetItem;
 		for(int i=0; i<inv.getSizeInventory(); i++)
 		{
 			targetItem = inv.getStackInSlot(i);
 			if (targetItem != null && (targetItem.getItem() instanceof ItemAccessory))
 				accessorySlotIds.add(i);
 		}
 		if (accessorySlotIds.size()>0)
 			return accessorySlotIds;
 		return null;
 	}
 }
