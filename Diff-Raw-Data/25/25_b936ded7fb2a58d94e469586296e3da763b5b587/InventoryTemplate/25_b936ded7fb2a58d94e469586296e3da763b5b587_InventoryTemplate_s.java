 package net.evtr.hungergames;
 
 import java.util.Vector;
 
 import org.bukkit.inventory.ItemStack;
 
 public class InventoryTemplate 
 {
 	public static int nextID = 1;
	public int minRange, maxRange;
 	public Vector<ItemStack> items;
 	public int id;
 	
 	public InventoryTemplate() 
 	{ 
 		items = new Vector<ItemStack>();
 	}
 	
 	public InventoryTemplate(ItemStack[] items, int minRange, int maxRange) 
 	{
 		this.items = new Vector<ItemStack>();
 		this.minRange = minRange;
 		this.maxRange = maxRange;
 		
 		for ( int i = 0; i < Math.min(items.length, 27); i++ ) 
 		{
 			this.items.add(items[i]);
 		}
 		id = ++nextID;
 	}
 }
