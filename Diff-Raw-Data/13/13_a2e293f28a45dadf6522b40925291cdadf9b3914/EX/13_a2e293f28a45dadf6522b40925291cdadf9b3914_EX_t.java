 package com.revised.CallOfDutyMC;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class EX {
 	/*
 	 * Implement  Buying of Classes
 	 */
 	
 	public static void buyClassA(Player player){
		float sub = player.getExp() - 10;
		player.setExp(sub);
 		
 	}
 	
 	public static void buyClassB(Player player){
		float sub = player.getExp() - 20;
		player.setExp(sub);
 	
 	}
 	
	public static void buyClassC(Player player){
		float sub = player.getExp() - 15;
		player.setExp(sub);
	}
	
 	public static void nuke(World world, Location location){
 		world.createExplosion(location, 60);
 	}
 }
