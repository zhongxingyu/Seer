 package com.asylumsw.bukkit.kits;
 
 import java.util.EnumMap;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.ItemStack;
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 /**
  *
  * @author jonathan
  */
 public class Package {
 	private String name;
 	private EnumMap<Material, Integer> items;
 	private int cooldown;
 
 	public Package() {
 		items = new EnumMap<Material, Integer>(Material.class);
 	}
 
 	public Package(String name, int cooldown) {
 		this.name = name;
 		this.cooldown = cooldown;
 		items = new EnumMap<Material, Integer>(Material.class);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	public int getNumItems() {
 		return items.size();
 	}
 	public int getCooldown() {
 		return cooldown;
 	}
 
 	public boolean addItem(Material item, int quantity) {
		if( 0 >= quantity ) return false;
		if( items.containsKey(item) ) {
			items.put(item, items.get(item)+quantity);
		}
 		return true;
 	}
 
 	public void spawnAt(Location loc) {
 		for( Map.Entry<Material,Integer> item : items.entrySet()) {
 			loc.getWorld().dropItem(loc, new ItemStack(item.getKey(), item.getValue()));
 		}
 	}
 
 	public String getChatMessage() {
		String message = ChatColor.LIGHT_PURPLE + name + ": ";
 		for( Map.Entry<Material,Integer> item : items.entrySet()) {
 			message += ChatColor.LIGHT_PURPLE + item.getKey().toString() + " "
 							+ ChatColor.GRAY + "("+item.getValue()+"), ";
 		}
 		return message;
 	}
 
 	@Override
 	public String toString() {
 		String line = String.format("%s:%d;", name, cooldown);
 		for( Map.Entry<Material,Integer> item : items.entrySet() ) {
 			line += String.format("%s:%d;", item.getKey().toString(), item.getValue());
 		}
 		return line;
 	}
 	
 
 }
