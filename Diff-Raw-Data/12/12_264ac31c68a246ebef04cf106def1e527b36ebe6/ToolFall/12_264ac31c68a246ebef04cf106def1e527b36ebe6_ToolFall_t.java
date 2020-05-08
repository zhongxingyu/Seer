 package spia1001.InvFall;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class ToolFall 
 {
 	Player player;
 	PlayerInventory inv;
 	public ToolFall(Player p,PlayerManager playerManager)
 	{
 		player = p;
 		inv = p.getInventory();
 		ItemStack heldItem = inv.getItemInHand(); 
 		if(isTool(heldItem))
 			if(readyFall(heldItem))
 				new ItemFall(player,playerManager.freeFallEnabled(player),1);
 	}
 	private boolean readyFall(ItemStack stack)
 	{
 		if(stack.getDurability() == stack.getType().getMaxDurability())
 			return true;
 		return false;
 	}
 	private boolean isTool(ItemStack stack)
 	{
 		Material type = stack.getType();
 		if(!type.isBlock())
 		{
 			int id = type.getId();
 			if(inRange(id,256,259) || inRange(id,267,279))
 				return true;
			if(inRange(id,283,286) || inRange(id,290,294))
 				return true;
 		}
 		return false;
 	}
 	private boolean inRange(int num,int min,int max)
 	{
 		if(min <= num && num <= max)
 			return true;
 		return false;
 	}
 }
