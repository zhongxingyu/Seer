 /*
  * This file is part of JBSim.
  * 
  * JBSim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * JBSim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with JBSim.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.icx.sim;
 
 import java.util.*;
 import java.awt.geom.*;
 
 /**
  * A class representing a digital sensor (physical push button) on the robot.
  */
 public class ButtonSensor extends Sensor {
 	public static final String NAME = "Collision Touch Sensor";
 
 	// Gets the sensor's name.
 	public static String getSensorName() {
 		return NAME;
 	}
 
 	// The 2D geometry of the bumper.
 	private Area geometry;
 
 	/**
 	 * Creates a button sensor of the given value type.
 	 * 
 	 * @param location the button sensor's location on the robot, or null if not physical
 	 */
 	public ButtonSensor(Location location) {
 		super(location);
 		geometry = null;
 	}
 
 	public String getName() {
 		return NAME;
 	}
 
 	/**
 	 * Creates a physical button sensor with non-default geometry. The geometry is to
 	 *  be in device coords, so no need for location.
 	 * 
 	 * @param geometry the sensor geometry
 	 */
 	public ButtonSensor(Area geometry) {
 		this(new Location(0, 0));
 		this.geometry = geometry;
 	}
 
	// Collision checks all objects (and bots)
 	protected int realValue(Environment env) {
 		Location loc = getRealLocation();
 		if (loc == null) return createDigitalValue(false);
 		boolean hit;
 		if (geometry == null) {
 			// check against center
 			Point2D point = new Point2D.Double(loc.getX(), loc.getY());
 			hit = collisionCheck(point, env.getObjects());
 			if (!hit) hit = collisionCheck(point, env.getRobots());
 		} else {
 			Area transform = new Area(geometry);
 			SimObject.transformArea(transform, loc);
 			// check against geometry
			hit = collisionCheck(transform, env.getObjects());
			if (!hit) hit = collisionCheck(transform, env.getRobots());
 		}
 		return createDigitalValue(hit);
 	}
 
 	// Checks all objects in the list for collisions with the given point.
 	protected boolean collisionCheck(Point2D point, Collection<? extends SimObject> toCheck) {
 		for (SimObject obj : toCheck)
			if (obj != getParentRobot() && obj.getTransformedCollision().contains(point))
				return true;
 		return false;
 	}
 
	// Checks all objects in the list for collisions with the given shape (area).
 	protected boolean collisionCheck(Area geom, Collection<? extends SimObject> toCheck) {
 		for (SimObject obj : toCheck) {
			// stupid bug
			if (obj == getParentRobot()) continue;
 			Area search = new Area(geom);
			// do it right
			search.intersect(obj.getTransformedCollision());
 			if (!search.isEmpty()) return true;
 		}
 		return false;
 	}
 }
