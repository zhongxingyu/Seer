 package com.oschrenk.humangeo.calc;
 
import static org.junit.Assert.assertEquals;

 import org.junit.Test;
 
 import com.oschrenk.humangeo.core.Segment;
 import com.oschrenk.humangeo.cs.Geographic2dCoordinate;
 import com.oschrenk.humangeo.ref.Spheres;
 
 /**
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class AlongTrackOrthodromeDestinationTest {
 
 	@Test
	public void testOnEquator() {
 		final Geographic2dCoordinate from = new Geographic2dCoordinate(0, 0);
 		final Geographic2dCoordinate to = new Geographic2dCoordinate(90, 0);
		final Geographic2dCoordinate point = new Geographic2dCoordinate(0, 45);
 		final Geographic2dCoordinate destination = new AlongTrackOrthodromeDestination(
 				Spheres.EARTH).destination(point,
 				new Segment<Geographic2dCoordinate>(from, to));
 
		assertEquals(new Geographic2dCoordinate(0, 0), destination);
 	}
	
 }
