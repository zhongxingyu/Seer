 package net.novacodex.hibernate.search.spatial;
 
 public class Point implements SpatialIndexable {
 
 	private final double latitude;
 	private final double longitude;
 
	public Point( Point p ) {
		this( p.latitude, p.longitude );
	}

 	/**
	 * @param latitude in radians
	 * @param longitude in radians
 	 */
 	public Point( double latitude, double longitude ) {
 
 		//Normalize in [-180;180]
 		this.longitude = ( ( longitude + ( GeometricConstants.LONGITUDE_DEGREE_RANGE / 2 ) ) % GeometricConstants.LONGITUDE_DEGREE_RANGE ) - ( GeometricConstants.LONGITUDE_DEGREE_RANGE / 2 );
 
		if ( latitude > GeometricConstants.LONGITUDE_DEGREE_MAX || latitude < GeometricConstants.LONGITUDE_DEGREE_MIN ) {
 			throw new IllegalArgumentException( "Illegal latitude value for Point creation" );
 		}
 
 		this.latitude = latitude;
 	}
 
 	public Point computeDestination( double distance, double heading ) {
 		double startPointLatitude = this.getLatitude();
 		double startPointLongitude = this.getLongitude();
 		double headingRadian = Math.toRadians( heading );
 		double startPointLatitudeRadian = Math.toRadians( startPointLatitude );
 		double startPointLongitudeRadian = Math.toRadians( startPointLongitude );
 
 		// Haversine formula (http://www.movable-type.co.uk/scripts/latlong.html)
 		double destinationLatitudeRadian = Math.asin( Math.sin( startPointLatitudeRadian ) * Math.cos( distance / GeometricConstants.EARTH_MEAN_RADIUS_KM ) + Math.cos( startPointLatitudeRadian ) * Math.sin( distance / GeometricConstants.EARTH_MEAN_RADIUS_KM ) * Math.cos( headingRadian ) );
 
 		double destinationLongitudeRadian = startPointLongitudeRadian + Math.atan2( Math.sin( headingRadian ) * Math.sin( distance / GeometricConstants.EARTH_MEAN_RADIUS_KM ) * Math.cos( startPointLatitudeRadian ), Math.cos( distance / GeometricConstants.EARTH_MEAN_RADIUS_KM ) - Math.sin( startPointLatitudeRadian ) * Math.sin( destinationLatitudeRadian ) );
 
 		return new Point( Math.toDegrees( destinationLatitudeRadian ), Math.toDegrees( destinationLongitudeRadian ) );
 	}
 
 	public double distanceToPoint( Point point ) {
 		double point1LatitudeRadian = Math.toRadians( this.getLatitude() );
 		double point1LongitudeRadian = Math.toRadians( this.getLongitude() );
 		double point2LatitudeRadian = Math.toRadians( point.getLatitude() );
 		double point2LongitudeRadian = Math.toRadians( point.getLongitude() );
 
 		// Haversine formula (http://www.movable-type.co.uk/scripts/latlong.html)
 		return Math.acos( Math.sin( point1LatitudeRadian ) * Math.sin( point2LatitudeRadian ) + Math.cos( point1LatitudeRadian ) * Math.cos( point2LatitudeRadian ) * Math.cos( point2LongitudeRadian - point1LongitudeRadian ) ) * GeometricConstants.EARTH_MEAN_RADIUS_KM;
 	}
 
 	@Override
 	public double getLatitude() {
 		return latitude;
 	}
 
 	@Override
 	public double getLongitude() {
 		return longitude;
 	}
 }
