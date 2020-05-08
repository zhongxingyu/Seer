 package edu.upenn.cis573;
 
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class GPXcalculatorTest {
 
 	private GPXobject obj;
 	private GPXcalculator calc = new GPXcalculator();
 
 	@Before
 	public void setUp() throws Exception {
 		GPXtrkpt pt0 = new GPXtrkpt(0, 0, 0, "2012-10-29T15:03:00Z");
 		GPXtrkpt pt1 = new GPXtrkpt(10, 10, 10, "2012-10-30T15:03:00Z");
 		GPXtrkpt pt2 = new GPXtrkpt(20, 20, 20, "2012-10-31T15:03:00Z");
 		GPXtrkpt pt3 = new GPXtrkpt(30, 30, 30, "2012-11-02T15:03:00Z");
 		
 		ArrayList pts0 = new ArrayList();
 		pts0.add(pt0);
 		pts0.add(pt1);
 		
 		ArrayList pts1 = new ArrayList();
 		pts1.add(pt2);
 		pts1.add(pt3);
 		
 		GPXtrkseg seg0 = new GPXtrkseg(pts0);
 		GPXtrkseg seg1 = new GPXtrkseg(pts1);
 		
 		ArrayList segs = new ArrayList();
 		segs.add(seg0);
 		segs.add(seg1);
 		
 		obj = new GPXobject("2012-11-05T15:03:00Z", "Test track", segs);
 	}
 
 	@Test
 	public void testElapsedTimeGPXtrk() {		
 		long time = calc.calculateElapsedTime(obj.trk());
 
 		// should be three days
 		long threeDays = 3 * 24 * 60 * 60 * 1000;
 		assertEquals(threeDays, time);
 	}
 
 	@Test
 	public void testElapsedTimeGPXtrkseg() {
 		
 		long time = calc.calculateElapsedTime(obj.trk().trkseg(0));
 		// should be one day
 		long oneDay = 24 * 60 * 60 * 1000;
 		assertEquals(oneDay, time);
 
 		time = calc.calculateElapsedTime(obj.trk().trkseg(1));
 		// should be two days
 		assertEquals(oneDay * 2, time);
 		
 	}
 
 	@Test
 	public void testDistanceTraveledGPXtrk() {
 		
 		double dist = calc.calculateDistanceTraveled(obj.trk());
 		
 		assertEquals(3067.685, dist, 0.01);
 	}
 
 	@Test
 	public void testDistanceTraveledGPXtrkseg() {
		double dist = calc.determineTotalDistanceCoveredBetweenPairsOfPointsInGPXTrackSegment(obj.trk().trkseg(0));
 		assertEquals(1568.552, dist, 0.01);
 		
		dist = calc.determineTotalDistanceCoveredBetweenPairsOfPointsInGPXTrackSegment(obj.trk().trkseg(1));
 		assertEquals(1499.132, dist, 0.01);
 	}
 	
 	@Test
 	public void testAverageSpeed() {
 		
 		double speed = calc.calculateAverageSpeed(obj.trk());
 		assertEquals(0.000011835, speed, 0.0000001);
 
 	}
 
 	@Test
 	public void testBearing() {
 
 		double bearing = calc.bearing(obj.trk());
 		assertEquals(40.89, bearing, 0.01);
 		
 	}
 	
 	@Test
 	public void testFastestSegment() {
 		
 		int fastest = calc.calculateFastestSegment(obj.trk());
		assertEquals(1, fastest);
 		
 	}
 
 
 }
