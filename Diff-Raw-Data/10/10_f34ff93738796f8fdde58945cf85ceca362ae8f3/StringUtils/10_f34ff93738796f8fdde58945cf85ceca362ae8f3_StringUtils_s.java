 package me.meta1203.plugins.satellitetrade;
 
import java.util.ListIterator;

 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class StringUtils {
 	public static String serializeInv(Inventory inv) {
 		short num = 1;
 		String ret = "";
		ListIterator<ItemStack> iter = inv.iterator();
 		
		while (iter.hasNext()) {
 			if (num % 4 == 0) {
 				ret += "\n";
 			}
			ret += iter.next().getAmount() + "x " + iter.next().getType().toString() + " | ";
 		}
 		
 		return ret;
 	}
 }
