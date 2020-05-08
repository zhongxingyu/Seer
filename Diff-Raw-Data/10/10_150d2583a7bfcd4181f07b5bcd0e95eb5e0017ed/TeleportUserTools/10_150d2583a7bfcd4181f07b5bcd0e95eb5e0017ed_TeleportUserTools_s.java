 package com.randrdevelopment.propertygroup.regions;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 public class TeleportUserTools {
 	public static Location getCenterProperty(Location startPoint, int width, int length){
 		int middlewidth = width / 2;
 		int middlelength = length / 2;
 		
 		int x = (int) (startPoint.getX() + middlewidth);
 		int z = (int) (startPoint.getZ() + middlelength);
 		
 		startPoint.setX(x);
 		startPoint.setZ(z);
 		
 		return startPoint;
 	}
 	
 	public static int getYLocation(Location startPoint) {
 		int y = 255;
 		
 		World world = startPoint.getWorld();
		
		for(int i=0; i<=255; i++) {
			int block = world.getBlockTypeIdAt((int) startPoint.getX(), y, (int) startPoint.getY());
 			
 			if (block != 0) {
 				break;
 			}
			
			y--;
 		}
 		y++;
 		return y;
 	}
 }
