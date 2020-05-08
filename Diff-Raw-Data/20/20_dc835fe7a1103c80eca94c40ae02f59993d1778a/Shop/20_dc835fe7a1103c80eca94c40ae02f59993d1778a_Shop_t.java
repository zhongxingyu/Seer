 package qs.swornshop;
 
 import java.util.HashMap;
 
 import org.bukkit.inventory.ItemStack;
 
 public class Shop {
 	public Shop() { }
 	public String owner;
 	public HashMap<ItemStack, ShopEntry> inventory = new HashMap<ItemStack, ShopEntry>();
	
 }
