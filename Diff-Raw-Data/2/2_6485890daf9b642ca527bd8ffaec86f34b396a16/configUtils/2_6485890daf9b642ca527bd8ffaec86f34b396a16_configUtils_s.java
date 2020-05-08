 package com.zolli.rodolffoutilsreloaded.utils;
 
 import java.util.Set;
 
 import org.bukkit.Location;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class configUtils {
 	
 	public String[] returnArray = new String[2];
 	private rodolffoUtilsReloaded plugin;
 	public configUtils(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	public void setLocation(Location loc, String type, String name) {
 		
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		String world = loc.getWorld().getName().toString();
 		
 		plugin.button.set("specialbutton." + name + ".type", type);
 		plugin.button.set("specialbutton." + name + ".world", world);
 		plugin.button.set("specialbutton." + name + ".x", x);
 		plugin.button.set("specialbutton." + name + ".y", y);
 		plugin.button.set("specialbutton." + name + ".z", z);
 		
 	}
 	
 	public String[] scanButton(Location loc) {
 		
 		Set<String> names = plugin.button.getConfigurationSection("specialbutton").getKeys(false);
 		String WorldButton = loc.getWorld().getName().toString();
 		double xButton = loc.getX();
 		double yButton = loc.getY();
 		double zButton = loc.getZ();
 		
 		for(String s : names) {
 			
 			String WorldConfig = plugin.button.getString("specialbutton." + s + ".world");
 			double xConfig = plugin.button.getDouble("specialbutton." + s + ".x");
 			double yConfig = plugin.button.getDouble("specialbutton." + s + ".y");
 			double zConfig = plugin.button.getDouble("specialbutton." + s + ".z");
 			
 			if(WorldButton.equalsIgnoreCase(WorldConfig) && xButton == xConfig && yButton == yConfig && zButton == zConfig) {
 				
 				returnArray[0] = plugin.button.getString("specialbutton." + s + ".type");
				returnArray[1] = plugin.button.getString("specialbutton." + s + ".value", null);
 				
 				return returnArray;
 				
 				
 			} 
 			
 		}
 		
 		return null;
 		
 	}
 	
 }
