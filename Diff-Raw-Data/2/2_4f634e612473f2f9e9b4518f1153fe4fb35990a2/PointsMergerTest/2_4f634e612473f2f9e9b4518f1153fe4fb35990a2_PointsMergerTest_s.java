 package com.oschrenk.gis.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.oschrenk.gis.geometry.api.Bearing;
 import com.oschrenk.gis.geometry.api.Distance;
 import com.oschrenk.gis.geometry.core.HaversineDistance;
 import com.oschrenk.gis.geometry.core.OrthodromeBearing;
 import com.oschrenk.gis.geometry.core.Point;
 import com.oschrenk.gis.geometry.ref.Spheres;
 
 public class PointsMergerTest {
 
 	private static final double DROP_BEARING = 5.0;
 	private static final double DROP_DISTANCE = 50.0;
 
 	private Distance distance = new HaversineDistance(Spheres.EARTH);
 	private Bearing bearing = new OrthodromeBearing();
 
 	private PointsMerger pointsMerger;
 	private List<Point> points;
 
 	@Before
 	public void setup() {
 		pointsMerger = new PointsMerger(distance, DROP_DISTANCE, bearing, DROP_BEARING);
 		points = new LinkedList<Point>();
 	}
 
 	@Test
	public void testWçithOnePoint() {
 		points.add(new Point(10, 10));
 
 		assertEquals(points, pointsMerger.build(points));
 	}
 
 	@Test
 	public void testWithTwoPoints() {
 		points.add(new Point(10, 10));
 		points.add(new Point(10, 20));
 
 		assertEquals(points, pointsMerger.build(points));
 	}
 
 	@Test
 	public void testWithDroppablePoint() {
 		Point a = new Point(10.0001, 10.0);
 		Point b = new Point(10.0002, 10.0);
 		Point c = new Point(10.1, 10.0);
 
 		points.add(a);
 		points.add(b);
 		points.add(c);
 
 		List<Point> mergedPoints = pointsMerger.build(points);
 		assertTrue(mergedPoints.contains(a));
 		assertTrue(mergedPoints.contains(c));
 	}
 
 }
