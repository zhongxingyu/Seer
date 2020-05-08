 package aor.SimplePlugin;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class RunnableShootArrow implements Runnable {

 	public Player m_player;

 	public RunnableShootArrow (Player player)
 	{
 		m_player = player;
 	}

 	public void run()
 	{
		if (m_player.getInventory().contains(Material.ARROW))
		{
			m_player.shootArrow();
			removeFromInventory(m_player.getInventory(), new ItemStack(Material.ARROW, 1));
		}
		else
		{
			m_player.sendMessage("Error: You no longer have enough arrows to cast this spell.");
		}

 	}

 	public void removeFromInventory(Inventory inventory, ItemStack item) { // Removes an itemstack from the inventory. Use this for quantities of items. Based on code by nisovin.
 		int amt = item.getAmount();
 		ItemStack[] items = inventory.getContents();
 		for (int i = 0; i < items.length; i++) {
 			if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
 				if (items[i].getAmount() > amt) {
 					items[i].setAmount(items[i].getAmount() - amt);
 					break;
 				} else if (items[i].getAmount() == amt) {
 					items[i] = null;
 					break;
 				} else {
 					amt -= items[i].getAmount();
 					items[i] = null;
 				}
 			}
 		}
 		inventory.setContents(items);
 	}
 }
