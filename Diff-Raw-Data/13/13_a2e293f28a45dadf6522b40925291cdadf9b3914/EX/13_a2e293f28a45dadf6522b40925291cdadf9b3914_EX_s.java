 package com.revised.CallOfDutyMC;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class EX {
 	/*
 	 * Implement  Buying of Classes
 	 */
 	
 	public static void buyClassA(Player player){
		player.setExp(-10);
 		
 	}
 	
 	public static void buyClassB(Player player){
		player.setExp(-20);
 	
 	}
 	
 	public static void nuke(World world, Location location){
 		world.createExplosion(location, 60);
 	}
 }
