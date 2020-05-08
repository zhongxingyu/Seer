 package com.oschrenk.gis.geometry.util;
 
 import java.awt.Shape;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Path2D;
 import java.util.List;
 
 import com.oschrenk.gis.geometry.api.Bearing;
 import com.oschrenk.gis.geometry.api.Coordinate;
 import com.oschrenk.gis.geometry.api.Destination;
 import com.oschrenk.gis.geometry.core.EuclideanBearing;
 import com.oschrenk.gis.geometry.core.EuclideanDestination;
 import com.oschrenk.gis.geometry.ref.Spheres;
 
 /**
 Given* Builds an {@link Area} that is a epsilon 
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class EpsilonPolylineBuilder {
 
 	/** The bearing. */
 	private final Bearing bearing;
 
 	/** The destination. */
 	private final Destination destination;
 
 	/** The epsilon in meter. */
 	private final double epsilonInMeter;
 
 	/** The coordinates. */
 	private final List<Coordinate> coordinates;
 
 	/** The area. */
 	private Area area;
 
 	/** The epsilon in degree. */
 	private double epsilonInDegree;
 
 	/**
 	 * Instantiates a new epsilon polyline builder.
 	 * 
 	 * @param coordinates
 	 *            the coordinates
 	 * @param epsilonInMeter
 	 *            the epsilon in meter
 	 */
 	public EpsilonPolylineBuilder(List<Coordinate> coordinates, double epsilonInMeter) {
 		super();
 		this.coordinates = coordinates;
 		this.destination = new EuclideanDestination(Spheres.EARTH);
 		this.bearing = new EuclideanBearing();
 		this.epsilonInMeter = epsilonInMeter;
 		epsilonInDegree = destination.getDestination(0, 0, 0, 1).getLatitude() * epsilonInMeter;
 
 	}
 
 	/**
 	 * Builds the.
 	 * 
 	 * @return the shape
 	 */
 	public Shape build() {
 
 		int size = coordinates.size();
 
 		if (size <= 1)
 			throw new IllegalArgumentException("Too few coordinates.");
 
 		area = new Area();
 
 		Coordinate startRight;
 		Coordinate startLeft;
 		Coordinate endRight = null;
 		Coordinate endLeft = null;
 		Coordinate start;
 		Coordinate end = null;
 		double b;
 
 		for (int i = 0; i < size - 1; i++) {
 			start = coordinates.get(i);
 			end = coordinates.get(i + 1);
 
 			Ellipse2D.Double circle = getCircle(start);
 			area.add(new Area(circle));
 
 			Path2D.Double rectangle = new Path2D.Double();
 			b = getBearing(start, end);
 			startRight = destination.getDestination(start.getLatitude(), start.getLongitude(), (b + 90) % 360, epsilonInMeter);
 			startLeft = destination.getDestination(start.getLatitude(), start.getLongitude(), (b - 90) % 360, epsilonInMeter);
 			endRight = destination.getDestination(end.getLatitude(), end.getLongitude(), (b + 90) % 360, epsilonInMeter);
 			endLeft = destination.getDestination(end.getLatitude(), end.getLongitude(), (b - 90) % 360, epsilonInMeter);
 			rectangle.moveTo(endLeft.getLatitude(), endLeft.getLongitude());
 			rectangle.lineTo(startLeft.getLatitude(), startLeft.getLongitude());
 			rectangle.lineTo(startRight.getLatitude(), startRight.getLongitude());
 			rectangle.lineTo(endRight.getLatitude(), endRight.getLongitude());
 			rectangle.closePath();
 			area.add(new Area(rectangle));
 
 		}
 
 		// close with the circle arround last point
 		area.add(new Area(getCircle(end)));
 
 		return area;
 
 	}
 
 	/**
 	 * Gets the circle.
 	 * 
 	 * @param point
 	 *            the point
 	 * @return the circle
 	 */
 	private Ellipse2D.Double getCircle(Coordinate point) {
 		Ellipse2D.Double circle = new Ellipse2D.Double(point.getLatitude() - epsilonInDegree, point.getLongitude() - epsilonInDegree,
 				2 * epsilonInDegree,
 				2 * epsilonInDegree);
 		return circle;
 	}
 
 	/**
 	 * Gets the bearing.
 	 * 
 	 * @param start
 	 *            the start
 	 * @param end
 	 *            the end
 	 * @return the bearing
 	 */
 	private double getBearing(Coordinate start, Coordinate end) {
 		double b = bearing.getBearing(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
 		return b;
 	}
 }
