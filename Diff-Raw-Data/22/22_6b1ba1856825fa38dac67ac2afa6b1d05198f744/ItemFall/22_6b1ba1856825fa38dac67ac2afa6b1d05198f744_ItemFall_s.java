 package spia1001.InvFall;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /*
 InvFall Plugin
 
 @author Chris Lloyd (SPIA1001)
 */
 
 public class ItemFall
 {
 	Player player;
 	PlayerInventory inv;
 	int itemCountForFall;
 	
 	ItemFall(Player p,boolean freefall, int i)
 	{
 		init(p,i,freefall,p.getInventory().getHeldItemSlot());
 	}
 	ItemFall(Player p,boolean freefall, int i,int slotId)
 	{
 		init(p,i,freefall,slotId);
 	}
 	void init(Player p,int i,boolean freefall,int slotId)
 	{
 		player = p;
 		inv = player.getInventory();
 		itemCountForFall = i;
 		if(freefall)
 			clumpItems(slotId,1);
 		columnFall(slotId);
 	}
 	@SuppressWarnings("deprecation")
 	private void columnFall(int column)
 	{
 		int height = columnHeight(column);
 		if(slotEmpty(column))
 		{
 			for(int row = 1; row <= height; row++)
 			{
 				int slot = getSlot(row,column);
 				int dest = getSlot(row-1,column);
 				if(!slotEmpty(slot) && slotEmpty(dest))
 					dropItem(slot,dest);
 			}
 		}
 		player.updateInventory();
 	}
 	private void dropItem(int startSlot,int destSlot)
 	{
		int type = inv.getItem(startSlot).getTypeId();
		int amount = inv.getItem(startSlot).getAmount();
		inv.setItem(destSlot, new ItemStack(type,amount));
 		inv.clear(startSlot);
 	}
 	private int columnHeight(int column)
 	{
 		int height = 0;
 		for(int i = 0; i < 4; i++)
 			if(!slotEmpty(getSlot(i,column)))
 					height++;
 		return height;
 	}
 	private void clumpItems(int column,int start)
 	{
 		int space = 0;
 		for(int i = start; i < 4; i++)
 		{
 			int slot = getSlot(i,column);
 			if(slotEmpty(slot))
 				space++;
 			else
 			{
 				if(space != 0)
 				{
 					int dest = getSlot(i - space,column);
 					dropItem(slot,dest);
 				}
 				clumpItems(column,i - space + 1);
 			}
 		}
 	}
 	private int getSlot(int row,int column)
 	{
 		int col[] = {0,27,18,9};
 		return col[row] + column; 
 	}
 	private boolean slotEmpty(int slot)
 	{
 		if(slot < 9)
 			return hotSlotEmpty(slot);
 		if(inv.getItem(slot).getType() == Material.AIR)
 			return true;
 		return false;
 	}
 	private boolean hotSlotEmpty(int slot)
 	{
 		if(inv.getItem(slot).getAmount() == itemCountForFall)
 			return true;
 		return false;
 	}
 }
