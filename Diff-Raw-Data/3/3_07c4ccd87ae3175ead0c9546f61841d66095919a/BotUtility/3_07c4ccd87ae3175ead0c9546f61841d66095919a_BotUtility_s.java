 package com.jaxelson;
 
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import robocode.AdvancedRobot;
 import robocode.RobocodeFileOutputStream;
 
 public class BotUtility {	
 	public static final int botWidth = 36;
 
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
 	
 	public static void drawCircle(Graphics2D g, ExtendedPoint2D location, Double radius) {
     	final double x = location.getX() - radius;
         final double y = location.getY() - radius;
 		final double diameter = radius*2;
 		Shape circle = new Ellipse2D.Double(x, y, diameter, diameter);
         g.draw(circle);
     }
 	
 	public static String fixName(String name) {
 		// Hack because of asterisk error
 		String fixedName = name.replace(" (", "* (");
 		return fixedName;
 	}
 
 	/**
 	 * Projects from a location a distance at an angle
 	 * @param sourceLocation location to start projection at
 	 * @param angle the angle to project at
 	 * @param length the length to project out to
 	 * @return the point projected out to
 	 */
 	public static ExtendedPoint2D project(ExtendedPoint2D sourceLocation,
 			double angle, double length) {
 		return new ExtendedPoint2D(sourceLocation.x + Math.sin(angle) * length,
 				sourceLocation.y + Math.cos(angle) * length);
 	}
 
 	public static double bulletVelocity(double power) {
 	    return (20.0 - (3.0*power));
 	}
 
 	public static double maxEscapeAngle(double velocity) {
 	    return Math.asin(8.0/velocity);
 	}
 
 	public static double limit(double min, double value, double max) {
 	    return Math.max(min, Math.min(value, max));
 	}
 
 	public static double absoluteBearing(ExtendedPoint2D source, ExtendedPoint2D target) {
 	    return Math.atan2(target.x - source.x, target.y - source.y);
 	}
 	
 	
 	public Object readCompressedObject(String filename, AdvancedRobot robot)
 	{
 		try
 		{
 			ZipInputStream zipin = new ZipInputStream(new
 			FileInputStream(robot.getDataFile(filename + ".zip")));
 			zipin.getNextEntry();
 			ObjectInputStream in = new ObjectInputStream(zipin);
 			Object obj = in.readObject();
 			in.close();
 			return obj;
 		}
 		catch (FileNotFoundException e)
 		{
 			System.out.println("File not found!");
 		}
 		catch (IOException e)
 		{
 			System.out.println("I/O Exception");
 		}
 		catch (ClassNotFoundException e)
 		{
 			System.out.println("Class not found! :-(");
 			e.printStackTrace();
 		}
 		return null;    //could not get the object
 	}
 	
 	/**
 	 * Write an object to disk
 	 * @param obj to write to disk
 	 * @param filename to write object to
 	 */
 	public static void writeObject(Serializable obj, String filename, AdvancedRobot robot)
 	{
 		System.out.println("Writing to disk!");
 		try
 		{
 			ZipOutputStream zipout = new ZipOutputStream(
 	                    new RobocodeFileOutputStream(robot.getDataFile(filename + ".zip")));
 			zipout.putNextEntry(new ZipEntry(filename));
 			ObjectOutputStream out = new ObjectOutputStream(zipout);
 			out.writeObject(obj);
 			out.flush();
 			zipout.closeEntry();
 			out.close();
 		}
 		catch (IOException e)
 		{
 			System.out.println("Error writing Object:" + e);
 		}
 	}
 }
 
