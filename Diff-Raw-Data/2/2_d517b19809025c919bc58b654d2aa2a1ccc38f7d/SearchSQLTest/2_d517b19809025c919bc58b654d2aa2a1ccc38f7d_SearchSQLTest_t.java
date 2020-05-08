 package com.chalmers.schmaps;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import com.google.android.maps.OverlayItem;
 
 import android.content.Context;
 
 public class SearchSQLTest {
 	GoogleMapActivity goolie = new GoogleMapActivity();
	SearchSQL tester = new SearchSQL(null);
 	String theTestValue = new String("runan"); //"runan" exists within the database.
 	
 	@Test
 	public void testExists() {
 		tester.openRead();
 		assertTrue(tester.exists(theTestValue));
 		
 	}
 
 	@Test
 	public void testGetLat() {
 		tester.openRead();
 		assertEquals(57689329, tester.getLat(theTestValue));
 	}
 
 	@Test
 	public void testGetLong() {
 		tester.openRead();
 		assertEquals(11973824, tester.getLong(theTestValue));
 	}
 
 	@Test
 	public void testGetAddress() {
 		tester.openRead();
 		assertEquals("Sven Hultins Gata 2", tester.getAddress(theTestValue));
 	}
 
 	@Test
 	public void testGetLevel() {
 		tester.openRead();
 		assertEquals("Plan 2", tester.getLevel(theTestValue));
 	}
 
 	@Test
 	public void testGetLocations() {
 		tester.openRead();
 		assertEquals(2, tester.getLocations("Microwave").size());
 	}
 
 }
