 package com.oschrenk.humangeo.cs.trans;
 
 import com.oschrenk.humangeo.api.CoordinateTransformation;
 import com.oschrenk.humangeo.core.Spheroid;
 import com.oschrenk.humangeo.core.Spheroids;
 import com.oschrenk.humangeo.cs.Cartesian3dCoordinate;
 import com.oschrenk.humangeo.cs.Geographic2dCoordinate;
 
 /**
  * Every point that is expressed in ellipsoidal coordinates can be expressed as
  * an x y z (Cartesian) coordinate. Cartesian coordinates simplify many
  * mathematical calculations. The origin is usually the center of mass of the
  * earth, a point close to the Earth's center of figure.
  * 
  * <p>
  * With the origin at the center of the ellipsoid, the conventional setup is the
  * expected right-hand:
  * <ul>
  * <li>Z-axis along the axis of the ellipsoid, positive northward</li>
  * <li>X- and Y-axis in the plane of the equator, X-axis positive toward 0
  * degrees longitude and Y-axis positive toward 90 degrees east longitude</li>
  * </ul>
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class Geographic2dToCartesian3dDirectCoordinateTransformation implements
 		CoordinateTransformation<Geographic2dCoordinate, Cartesian3dCoordinate> {
 
 	private final Spheroid spheroid;
 	private final double e2;
 
 	public Geographic2dToCartesian3dDirectCoordinateTransformation(
 			final Spheroid spheroid) {
 		this.spheroid = spheroid;
 		e2 = Spheroids.eccentricitySquared(spheroid);
 	}
 
 	/*
 	 * @see com.oschrenk.humangeo.api.Transformation#transform(java.lang.Object)
 	 */
 	@Override
 	public Cartesian3dCoordinate transform(final Geographic2dCoordinate from) {
 		final double nu = getNu(spheroid.getSemiMajorAxis(), e2,
 				from.getLatitude());
 		final double cosPhi = Math.cos(Math.toRadians(from.getLatitude()));
		final double sinPhi = Math.sin(Math.toRadians(from.getLatitude()));
 		final double cosLamda = Math.cos(Math.toRadians(from.getLongitude()));
 		final double sinLamda = Math.sin(Math.toRadians(from.getLongitude()));
 		// final double h = 0;
		// (nu + h) cosPhi cosLamda
 		final double x = nu * cosPhi * cosLamda;
 		final double y = nu * cosPhi * sinLamda;
		final double z = (nu * (1 - e2)) * sinPhi;
 
 		return new Cartesian3dCoordinate(x, y, z);
 	}
 
 	private static final double getNu(final double semiMajorAxis,
 			final double eccentricitySquared, final double phiInDegree) {
 		final double sinPhi = Math.sin(Math.toRadians(phiInDegree));
 		final double denomSquared = 1 - eccentricitySquared * sinPhi * sinPhi;
 
 		return semiMajorAxis / Math.sqrt(denomSquared);
 	}
 }
