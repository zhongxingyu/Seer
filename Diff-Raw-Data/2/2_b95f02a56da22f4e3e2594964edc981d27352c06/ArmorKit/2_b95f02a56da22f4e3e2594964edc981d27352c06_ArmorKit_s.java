 package com.ttaylorr.uhc.pvp;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class ArmorKit {
 
 	static ItemStack[] armor;
 	static ItemStack[] items;
 	static ArrayList<Location> coordinates = new ArrayList<Location>();
 	
 	static {
 		
 		armor = new ItemStack[4];
 		items = new ItemStack[4];
 
 		armor[3] = new ItemStack(Material.IRON_HELMET, 1);
 		armor[2] = new ItemStack(Material.IRON_CHESTPLATE, 1);
 		armor[1] = new ItemStack(Material.IRON_LEGGINGS, 1);
 		armor[0] = new ItemStack(Material.IRON_BOOTS, 1);
 
 		for(ItemStack stack : armor) {
			stack.addEnchantment(Enchantment.PROTECTION_ALL,1);
 			stack.addUnsafeEnchantment(Enchantment.DURABILITY,10);
 		}
 
 		ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);
 		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
 		sword.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
 		items[0] = sword;
 		
 		ItemStack bow = new ItemStack(Material.BOW, 1);
 		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
 		bow.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
 		items[1] = bow;
 		
 		items[2] = new ItemStack(Material.ARROW,1);
 		items[3] = new ItemStack(Material.ENDER_PEARL,1);
 		
 		World spawnWorld = Bukkit.getServer().getWorld("spawn");
 
 		coordinates.add(new Location(spawnWorld, -106.5, 96, 40.5));
 		coordinates.add(new Location(spawnWorld, -84.5, 90, 80.5));
 		coordinates.add(new Location(spawnWorld, -41.5, 87, 93.5));
 		coordinates.add(new Location(spawnWorld, -12.5, 82, 104.5));
 		coordinates.add(new Location(spawnWorld, 13.5, 95, 120.5));
 		coordinates.add(new Location(spawnWorld, 20.5, 78, 101.5));
 		coordinates.add(new Location(spawnWorld, 41.5, 91, 114.5));
 		coordinates.add(new Location(spawnWorld, 42.5, 77, 92.5));
 		coordinates.add(new Location(spawnWorld, 33.5, 85, 75.5));
 		coordinates.add(new Location(spawnWorld, 68.5, 80, 58.5));
 		coordinates.add(new Location(spawnWorld, 76.5, 78, 18.5));
 		coordinates.add(new Location(spawnWorld, 72.5, 78, 6.5));
 		coordinates.add(new Location(spawnWorld, 78.5, 78, -20.5));
 		coordinates.add(new Location(spawnWorld, 52.5, 82, -33.5));
 		coordinates.add(new Location(spawnWorld, 40.5, 86, -64.5));
 		coordinates.add(new Location(spawnWorld, 66.5, 80, -77.5));
 		coordinates.add(new Location(spawnWorld, 44.5, 76, -87.5));
 		coordinates.add(new Location(spawnWorld, 5.5, 74, -94.5));
 		coordinates.add(new Location(spawnWorld, -17.5, 73, -92.5));
 		coordinates.add(new Location(spawnWorld, -38.5, 81, -82.5));
 		coordinates.add(new Location(spawnWorld, -62.5, 79, -106.5));
 		coordinates.add(new Location(spawnWorld, -82.5, 76, -71.5));
 		coordinates.add(new Location(spawnWorld, -90.5, 76, -51.5));
 		coordinates.add(new Location(spawnWorld, -101.5, 86, 15.5));
 		coordinates.add(new Location(spawnWorld, -80.5, 89, 19.5));
 
 	}
 
 	public void applyKit(Player p) {
 		p.getInventory().setArmorContents(armor);
 	
 		p.getInventory().addItem(items);
 	}
 
 	public Location getRandomLocation() {
 		Random r = new Random();
 		
 		return coordinates.get(r.nextInt(coordinates.size()));
 	}
 }
