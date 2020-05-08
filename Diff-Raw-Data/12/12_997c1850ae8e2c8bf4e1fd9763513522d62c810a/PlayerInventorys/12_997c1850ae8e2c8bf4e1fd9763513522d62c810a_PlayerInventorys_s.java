 package com.koponya.AuctionMaster;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerInventorys implements InventoryHolder{
 
 	private Inventory inv;
 	private static final Map<String, PlayerInventorys> inventorys = new HashMap<String, PlayerInventorys>();
 	
 	public static PlayerInventorys get(String name) {
 		PlayerInventorys ret = inventorys.get(name);
 		if(ret==null) {
 			ret = new PlayerInventorys();
 			inventorys.put(name, ret);
 		}
 		return ret;
 	}
 	
 	public PlayerInventorys() {
 		this.inv = Bukkit.createInventory(this, InventoryType.CHEST);
 	}
 	
 	public void setItemStack(ItemStack[] items) {
 		inv.setContents(items);
 	}
 	
 	@Override
 	public Inventory getInventory() {
 		return inv;
 	}
 
 	public static void saveAll(FileConfiguration data) {
 		Set<String> names = PlayerInventorys.inventorys.keySet();
 		for(String name : names) {
 			ConfigHelper.setInventory(PlayerInventorys.inventorys.get(name).getInventory(), "inventorys."+name, data);
 		}
 	}
 	
 	public static void loadAll(FileConfiguration data) {
 		ConfigurationSection sec = data.getConfigurationSection("inventorys");
		Set<String> names = sec.getKeys(true);
 		for(String name : names) {
 			PlayerInventorys pi = new PlayerInventorys();
 			pi.setItemStack(ConfigHelper.getInventory("inventorys."+name, data));
 			inventorys.put(name, pi);
 		}
 	}
 }
