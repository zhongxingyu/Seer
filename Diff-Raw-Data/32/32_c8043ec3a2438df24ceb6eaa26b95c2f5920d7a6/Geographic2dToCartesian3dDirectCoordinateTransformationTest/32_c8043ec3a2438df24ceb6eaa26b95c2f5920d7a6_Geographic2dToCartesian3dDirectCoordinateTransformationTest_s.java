 package com.oschrenk.humangeo.cs.trans;
 
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
		System.out.println(paris);
 
 		final Cartesian3dCoordinate cartesianParis = transformationForward
 				.transform(paris);
 
		System.out.println(cartesianParis);
 
		final CoordinateTransformation<Cartesian3dCoordinate, Geographic2dCoordinate> transformationReverse = new Cartesian3dToGeographic2dTomsCoordinateTransformation(
				Ellipsoids.WGS_84);
 
		final Geographic2dCoordinate reverseParis = transformationReverse
				.transform(cartesianParis);
		System.out.println(reverseParis);
 	}
 }
