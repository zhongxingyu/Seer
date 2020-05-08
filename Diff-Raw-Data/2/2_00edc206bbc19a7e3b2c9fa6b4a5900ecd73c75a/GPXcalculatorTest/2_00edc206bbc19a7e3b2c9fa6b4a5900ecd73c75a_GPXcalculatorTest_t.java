 package edu.upenn.cis350.gpx;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.junit.Test;
 
 public class GPXcalculatorTest {
 	
 	/**
 	 * "Normal" case tests.
 	 */
 	@Test
 	public void testTwoGPXtrkptsOneGPXtrkseg() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(80.0, 100.0, date);
		GPXtrkpt trkpt2 = new GPXtrkpt(80.0, 101.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a within bounds latitude and longitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 1);
 	}
 	
 	@Test
 	public void testThreeGPXtrkptsOneGPXtrkseg() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(80.0, 100.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(80.0, 101.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(81.0, 101.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with within bounds latitude and longitude track points", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 2);
 	}
 	
 	@Test
 	public void testTwoGPXtrksegs() {
 		Date date = new Date();
 		
 		// create first GPXtrkseg
 		GPXtrkpt trkpt1 = new GPXtrkpt(80.0, 100.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(80.0, 101.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(81.0, 101.0, date);
 		ArrayList<GPXtrkpt> trkpts1 = new ArrayList<GPXtrkpt>();
 		trkpts1.add(trkpt1);
 		trkpts1.add(trkpt2);
 		trkpts1.add(trkpt3);
 		GPXtrkseg trkseg1 = new GPXtrkseg(trkpts1);
 		
 		// create second GPXtrkseg
 		GPXtrkpt trkpt4 = new GPXtrkpt(80.0, 100.0, date);
 		GPXtrkpt trkpt5 = new GPXtrkpt(81.0, 100.0, date);
 		GPXtrkpt trkpt6 = new GPXtrkpt(81.0, 101.0, date);
 		ArrayList<GPXtrkpt> trkpts2 = new ArrayList<GPXtrkpt>();
 		trkpts2.add(trkpt4);
 		trkpts2.add(trkpt5);
 		trkpts2.add(trkpt6);
 		GPXtrkseg trkseg2 = new GPXtrkseg(trkpts2);
 		
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg1);
 		trksegs.add(trkseg2);
 		GPXtrk track = new GPXtrk("Track with 2 segments with within bounds latitude and longitude track point2", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 4);
 	}
 	
 	/**
 	 * If the GPXtrk object is null,
 	 * the method should return -1
 	 */
 	@Test
 	public void testNullGPXtrk() {
 		assertTrue(GPXcalculator.calculateDistanceTraveled(null) == -1);
 	}
 	
 	/**
 	 * If the GPXtrk contains no GPXtrkseg objects,
 	 * the method should return -1.
 	 */
 	@Test
 	public void testEmptyGPXtrk() {
 		GPXtrk track = new GPXtrk("Empty Track", new ArrayList<GPXtrkseg>());
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == -1);
 	}
 	
 	/**
 	 * If any GPXtrkseg in the GPXtrk is null,
 	 * the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testNullGPXtrkseg() {
 		// create non null GPXtrkseg
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(80.0, 100.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(80.0, 101.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(81.0, 101.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(null); // bad GPXtrkseg
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a single null segment", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 *  If a GPXtrkseg contains no GPXtrkpt objects,
 	 *  the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testOneEmptyGPXtrkseg() {
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts); // empty GPXtrkseg
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with no track points", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * If a GPXtrkseg contains only one GPXtrkpt,
 	 * the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testOneGPXtrkptInGPXtrkseg() {
 		Date date = new Date();
 		GPXtrkpt trkpt = new GPXtrkpt(1.0, 1.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts); // GPXtrkseg with one GPXtrkpt
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a single track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * If any GPXtrkpt in a GPXtrkseg is null,
 	 * the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testNullGPXtrkptInGPXtrkseg() {
 		Date date = new Date();
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		GPXtrkpt trkpt1 = new GPXtrkpt(80.0, 100.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(80.0, 101.0, date);
 		trkpts.add(null); // bad GPXtrkpt
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a null track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 *  If any GPXtrkpt in a GPXtrkseg has a latitude that is greater than 90,
 	 *  the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testGPXtrkptLatitudeGreaterThanNinety() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(100.0, 1.0, date); // bad point
 		GPXtrkpt trkpt2 = new GPXtrkpt(10.0, 1.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(11.0, 1.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a > 90 latitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * Boundary case for above case. This should compute normally.
 	 */
 	@Test
 	public void testGPXtrkptLatitudeEqualsNinety() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(90.0, 1.0, date); // OK point
 		GPXtrkpt trkpt2 = new GPXtrkpt(89.0, 1.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(89.0, 0.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a = 90 latitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 2);
 	}
 	
 	/**
 	 *  If any GPXtrkpt in a GPXtrkseg has a latitude that is less than -90,
 	 *  the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testOneGPXtrkptLatitudeLessThanNegativeNinety() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(-100.0, 1.0, date); // bad point
 		GPXtrkpt trkpt2 = new GPXtrkpt(80, 0.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(89.0, 0.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a < -90 latitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * Boundary case for above case. This should compute normally.
 	 */
 	@Test
 	public void testOneGPXtrkptLatitudeEqualsNegativeNinety() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(-90.0, 1.0, date); // OK point
 		GPXtrkpt trkpt2 = new GPXtrkpt(-89.0, 1.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(-89.0, 0.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a = -90 latitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 2);
 	}
 	
 	/**
 	 * If any GPXtrkpt in a GPXtrkseg has a longitude that is greater than 180,
 	 * the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testOneGPXtrkptLongitudeGreaterThanOneEighty() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(1.0, 200.0, date); // bad point
 		GPXtrkpt trkpt2 = new GPXtrkpt(1.0, 100.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(0.0, 100.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a > 180 longitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * Boundary case for above case. This should compute normally.
 	 */
 	@Test
 	public void testOneGPXtrkptLongitudeEqualsOneEighty() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(1.0, 180.0, date); // OK point
 		GPXtrkpt trkpt2 = new GPXtrkpt(1.0, 179.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(0.0, 179.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a = 180 longitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 2);
 	}
 	
 	/**
 	 * If any GPXtrkpt in a GPXtrkseg has a longitude that is less than -180,
 	 * the distance traveled for that GPXtrkseg should be considered 0.
 	 */
 	@Test
 	public void testOneGPXtrkptLongitudeLessThanNegativeOneEighty() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(1.0, -200.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(1.0, 100.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(0.0, 100.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a < -180 longitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 0);
 	}
 	
 	/**
 	 * Boundary case for above case. This should compute normally.
 	 */
 	public void testOneGPXtrkptLongitudeEqualsNegativeOneEighty() {
 		Date date = new Date();
 		GPXtrkpt trkpt1 = new GPXtrkpt(1.0, -180.0, date);
 		GPXtrkpt trkpt2 = new GPXtrkpt(1.0, -179.0, date);
 		GPXtrkpt trkpt3 = new GPXtrkpt(0.0, -179.0, date);
 		ArrayList<GPXtrkpt> trkpts = new ArrayList<GPXtrkpt>();
 		trkpts.add(trkpt1);
 		trkpts.add(trkpt2);
 		trkpts.add(trkpt3);
 		GPXtrkseg trkseg = new GPXtrkseg(trkpts);
 		ArrayList<GPXtrkseg> trksegs = new ArrayList<GPXtrkseg>();
 		trksegs.add(trkseg);
 		GPXtrk track = new GPXtrk("Track with a segment with a = -180 longitude track point", trksegs);
 		assertTrue(GPXcalculator.calculateDistanceTraveled(track) == 2);
 	}
 	
 }
