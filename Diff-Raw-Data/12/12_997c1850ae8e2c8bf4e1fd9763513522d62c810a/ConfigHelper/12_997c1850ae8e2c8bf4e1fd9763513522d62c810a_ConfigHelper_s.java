 package com.koponya.AuctionMaster;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ConfigHelper {
 	private ConfigHelper() {}
 	
 	/*
 	 * Write a Location to a fileconfiguration object 
 	 */
 	public static void setLocation(Location loc, String node, FileConfiguration conf) {
 		conf.set(node+".world", loc.getWorld().getName());
 		conf.set(node+".x", loc.getX());
 		conf.set(node+".y", loc.getY());
 		conf.set(node+".z", loc.getZ());
 		conf.set(node+".yaw", loc.getYaw());
 		conf.set(node+".pitch", loc.getPitch());
 	}
 	
 	public static Location getLocation(String node, FileConfiguration conf)
 	{
 		return new Location(
 				Bukkit.getWorld(conf.getString(node+".world")),
 				conf.getDouble(node+".x"),
 				conf.getDouble(node+".y"),
 				conf.getDouble(node+".z"),
 				(float)conf.getDouble(node+".yaw"),
 				(float)conf.getDouble(node+".pitch")
 			);
 	}
 	
 	public static void setInventory(Inventory inv, String node, FileConfiguration conf) {
 		ItemStack[] items = inv.getContents();
		int j=0;
		for(ItemStack i : items) {
			conf.set(node+"."+j++, i);
 		}
 	}
 	
 	public static ItemStack[] getInventory(String node, FileConfiguration conf) {
 		ItemStack[] ret = new ItemStack[InventoryType.CHEST.getDefaultSize()];
 		for(int i=0;i<ret.length;i++) {
 			ret[i] = conf.getItemStack(node+"."+i);
 		}
 		return ret;
 	}
 }
