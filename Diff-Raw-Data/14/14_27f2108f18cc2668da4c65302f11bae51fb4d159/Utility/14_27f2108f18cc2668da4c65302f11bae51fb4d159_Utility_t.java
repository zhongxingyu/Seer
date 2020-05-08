 package net.adbenson.utility;
 
 import java.awt.Color;
import java.awt.Graphics2D;
 
 public class Utility {
 	
 	public static Color setAlpha(Color c, double alpha) {
 		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
 	}
 	
 	public static double oppositeAngle(double angle) {
		return (angle - Math.PI) % (Math.PI * 2);
	}
	
	public static void drawCrosshairs(Graphics2D g, Vector center, int start, int end) {
		int x = center.intX();
		int y = center.intY();
		
		g.drawLine(x-start, y, x-end, y);
		g.drawLine(x, y-start, x, y-end);
		g.drawLine(x+start, y, x+end, y);
		g.drawLine(x, y+start, x, y+end);
 	}
 
 }
