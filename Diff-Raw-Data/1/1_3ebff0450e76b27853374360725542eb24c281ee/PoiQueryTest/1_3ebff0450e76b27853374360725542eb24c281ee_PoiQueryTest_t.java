 package com.nearme;
 
 import static org.junit.Assert.*;
 
 /**
  * Unit test for the PoiQuery constructor
  */
 
 import org.junit.Test;
 
 public class PoiQueryTest {
 
 	@Test
 	public void testValidPoiQueryConstruction() {
 		PoiQuery pq = new PoiQuery("/101.123245/-56.12/10");
 		assertEquals(101.123245, pq.getLatitude(), 0);
 		assertEquals(-56.12, pq.getLongitude(), 0);
 		assertEquals(10, pq.getRadius());
 		assertEquals(0, pq.getTypes().size());
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testNewPoiFailsBadLatitude() {
 		new PoiQuery("/fred/-56.12/10");		
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testNewPoiFailsBadLongitude() {
 		new PoiQuery("/101.123245/something/10");
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testNewPoiFailsNullRadius() {
 		new PoiQuery("/101.123245/");
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testNewPoiFailMissingParameter() {
 		new PoiQuery("/101.123245/-56.12");
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testNewPoiFailsBadRadius() {
 		new PoiQuery("/101.123245/-56.12/fred");
 	}
 
 	@Test
 	public void testWithOneType() {
 		PoiQuery pq = new PoiQuery("/101.123245/-56.12/10?t=1");
 		assertEquals(101.123245, pq.getLatitude(), 0);
 		assertEquals(-56.12, pq.getLongitude(), 0);
 		assertEquals(10, pq.getRadius());
 		assertEquals(1, pq.getTypes().size());
 		assertEquals(1, ((Integer) pq.getTypes().get(0)).intValue());
 	}
 
 	@Test
 	public void testWithTwoTypes() {
 		PoiQuery pq = new PoiQuery("/101.123245/-56.12/10?t=1,2");
 		assertEquals(101.123245, pq.getLatitude(), 0);
 		assertEquals(-56.12, pq.getLongitude(), 0);
 		assertEquals(10, pq.getRadius());
 		assertEquals(2, pq.getTypes().size());
 		assertEquals(1, ((Integer) pq.getTypes().get(0)).intValue());
 		assertEquals(2, ((Integer) pq.getTypes().get(1)).intValue());
 	}
 
 	
 
 }
