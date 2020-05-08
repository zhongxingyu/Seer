 package com.jaxelson;
 
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 
 
 public class BotUtility {	
 	/**
 	 * Returns the amount of damage inflicted by a bullet using the specified
 	 * power (not factoring in the life of the target).<br>
 	 * NOTE: A shotPower value greater than 3 will return an invalid result
 	 * @param shotPower A double specifying the power with which the bullet
 	 *                  was fired
 	 * @return A double specifying the amount of damage inflicted by the
 	 *         bullet
 	 */
 	public static Double calculateDamage(double shotPower) {
 		double damage = (shotPower * 4);
 		if (shotPower > 1) {
 			damage += ((shotPower - 1) * 2);
 		}
 		return damage;
 	}
 	
	public static void drawCircle(Graphics2D g, Point2D location, Integer radius) {
    	final double x = location.getX();
        final double y = location.getY();
		final int diameter = radius*2;
 		Shape circle = new Ellipse2D.Double(x, y, diameter, diameter);
         g.draw(circle);
     }
 }
 
