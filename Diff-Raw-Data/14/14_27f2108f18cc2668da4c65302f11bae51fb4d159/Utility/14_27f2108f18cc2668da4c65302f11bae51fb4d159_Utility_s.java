 package net.adbenson.utility;
 
 import java.awt.Color;
 
 public class Utility {
 	
 	public static Color setAlpha(Color c, double alpha) {
 		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
 	}
 	
 	public static double oppositeAngle(double angle) {
		return (angle - Math.PI) % Math.PI * 2;
 	}
 
 }
