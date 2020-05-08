 package com.oschrenk.humangeo.cs.trans;
 
import static org.junit.Assert.*;

 import org.junit.Test;
 
 import com.oschrenk.humangeo.api.CoordinateTransformation;
 import com.oschrenk.humangeo.cs.Cartesian3dCoordinate;
 import com.oschrenk.humangeo.cs.Geographic2dCoordinate;
 import com.oschrenk.humangeo.ref.Ellipsoids;
 
 /**
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class Geographic2dToCartesian3dDirectCoordinateTransformationTest {
 
 	@Test
 	public void test() {
 		final CoordinateTransformation<Geographic2dCoordinate, Cartesian3dCoordinate> transformationForward = new Geographic2dToCartesian3dDirectCoordinateTransformation(
 				Ellipsoids.WGS_84);
 
 		// Paris
 		final double latitude = 48 + 50 / 60.0;
 		final double longitude = 2 + 20 / 60.0;
 
 		final Geographic2dCoordinate paris = new Geographic2dCoordinate(
 				latitude, longitude);
 
 		final Cartesian3dCoordinate cartesianParis = transformationForward
 				.transform(paris);
 
		final double x = 4202917.917;
		final double y = 171255.782;
		final double z = 4778378.571;
 
		assertEquals(x, cartesianParis.getX(), 0.001);
		assertEquals(y, cartesianParis.getY(), 0.001);
		assertEquals(z, cartesianParis.getZ(), 0.001);
 
 	}
 }
