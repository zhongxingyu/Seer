 package com.jaxelson;
 
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 
 import robocode.AdvancedRobot;
 import robocode.util.Utils;
 
 public class ExtendedPoint2D extends Point2D.Double implements Serializable{
 
 	private static final long serialVersionUID = 74324L;
 	public ExtendedPoint2D(double x, double y) {
 		super(x, y);
 	}
 	
 	public ExtendedPoint2D(Point2D location, double angle, double dist) {
 		double enemyX = location.getX() + Math.sin(angle) * dist;
 		double enemyY = location.getY() + Math.cos(angle) * dist;
 		x = enemyX;
 		y = enemyY;
 	}
 	
 	public void setVectorLocation(double angle, double dist) {
 		double enemyX = x + Math.sin(angle) * dist;
 		double enemyY = y + Math.cos(angle) * dist;
 		x = enemyX;
 		y = enemyY;
 	}
 	
 	/**
 	 * Get bearing from this point to given location
 	 * @param location to get bearing to
 	 * @param robot used to calculate heading
 	 * @return bearing to point from robot
 	 */
 	public double bearingTo(Point2D location, AdvancedRobot robot) {
 		double absoluteAngle = angleTo(location);
 		double bearing = absoluteAngle - robot.getHeadingRadians();
 
		return (bearing > 0) ? bearing - Math.PI : bearing + Math.PI;
 	}
 	
 	/**
 	 * Gives absolute angle from this point to given location
 	 * @param location to get angle to
 	 * @return absolute angle
 	 */
 	public double angleTo(Point2D location) {
 		ExtendedPoint2D loc = (ExtendedPoint2D) location;
		return Utils.normalAbsoluteAngle(Math.atan2(loc.x - x,
				loc.y - y));
 	}
 	
 	public double angleTo(double x, double y) {
 		return angleTo(new Point2D.Double(x, y));
 	}
 
 }
