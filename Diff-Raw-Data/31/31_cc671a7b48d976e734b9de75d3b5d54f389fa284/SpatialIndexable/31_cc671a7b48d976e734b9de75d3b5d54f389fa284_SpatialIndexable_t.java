 package net.novacodex.hibernate.search.spatial;
 
 public interface SpatialIndexable {
 	/**
	 * @return the latitude in degrees
 	 */
 	double getLatitude();
 
 	/**
	 * @return the longitude in degrees
 	 */
 	double getLongitude();
 }
