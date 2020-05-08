 package com.oschrenk.gis.geometry.core;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.oschrenk.gis.geometry.api.Destination;
 import com.oschrenk.gis.geometry.ref.Spheres;
 
 public class EuclideanDestinationTest {
 
 	private Destination d;
 
 	@Before
 	public void setup() {
 		d = new EuclideanDestination(Spheres.EARTH);
 	}
 
 	@Test
 	public void testPositiveVertical() {
 		Point a = new Point(0, 0);
 		double bearing = 0;
		double distance = 100;
 
		assertEquals(10, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLatitude(), 0.000001);
 		assertEquals(0, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLongitude(), 0.000001);
 	}
 
 	@Test
 	public void testNegativeVertical() {
 		Point a = new Point(0, 0);
 		double bearing = 180;
 		double distance = 10;
 
		assertEquals(-10, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLatitude(), 0.000001);
 		assertEquals(0, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLongitude(), 0.000001);
 	}
 
 	@Test
 	public void testPositiveHorizontal() {
 		Point a = new Point(0, 0);
 		double bearing = 90;
 		double distance = 10;
 
 		assertEquals(0, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLatitude(), 0.000001);
		assertEquals(10, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLongitude(), 0.000001);
 	}
 
 	@Test
 	public void testNegativeHorizontal() {
 		Point a = new Point(0, 0);
 		double bearing = 270;
 		double distance = 10;
 
 		assertEquals(0, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLatitude(), 0.000001);
		assertEquals(-10, d.getDestination(a.getLatitude(), a.getLongitude(), bearing, distance).getLongitude(), 0.000001);
 	}
 
 }
