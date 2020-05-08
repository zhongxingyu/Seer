 package net.novacodex.hibernate.search.spatial;
 
 public interface SpatialIndexable {
 	/**
	 * @return the latitude in radians
 	 */
 	double getLatitude();
 
 	/**
	 * @return the longitude in radians
 	 */
 	double getLongitude();
 }
