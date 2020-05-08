 package com.example.transitlogger.tests;
 
import com.example.transitlogger.model.Distance;

 import junit.framework.TestCase;
 
 public class DistanceTest extends TestCase {
 
 	private Distance d;
 	
 	public void setUp() {
 		d = new Distance(1.0); // 1 km
 	}
 	
 	public void testDistance() {
 		assertEquals(d.getKilometers(), 1.0, .001);
 		assertEquals(d.getMiles(), 1.0/Distance.KM_PER_MI, .001);
 		d.setKilometers(2.0);
 		assertEquals(d.getKilometers(), 2.0, .001);
 	}
 
 }
