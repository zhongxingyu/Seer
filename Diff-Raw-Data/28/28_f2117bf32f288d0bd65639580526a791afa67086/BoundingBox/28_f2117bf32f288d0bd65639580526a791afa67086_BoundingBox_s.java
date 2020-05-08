 package com.winterwell.jgeoplanet;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A region of the Earth's surface defined by four corners and some
  * great circles.
  * 
  * @author Joe Halliwell <joe@winterwell.com>
  */
 public class BoundingBox {
 
 	final Location northEast;
 	final Location southWest;
 	
 	public BoundingBox(Location northEast, Location southWest) {
 		this.northEast = northEast;
 		this.southWest = southWest;
 	}
 	
 	BoundingBox(JSONObject bbox) throws JSONException {
 		this.southWest = new Location(bbox.getJSONObject("southWest"));
 		this.northEast = new Location(bbox.getJSONObject("northEast"));
 	}
 	
 	public Location getNorthEast() {
 		return northEast;
 	}
 	
 	public Location getSouthWest() {
 		return southWest;
 	}
 	
 	/**
 	 * Determine whether the specified location is contained within this bounding
 	 * box.
 	 * @param location the location to test
 	 * @return true if the location is within this bounding box. False otherwise.
 	 */
 	public boolean contains(Location location) {
 		return (location.latitiude <= northEast.latitiude
 				&& location.latitiude >= southWest.latitiude
 				&& location.longitude <= northEast.longitude
 				&& location.longitude >= southWest.longitude);
 	}
 	
 	/**
 	 * Determine whether the specified bounding box is completely contained 
 	 * within this one.
 	 * @param other the bounding box to test
 	 * @return true if the other bounding box is completely contained within this one. False otherwise.
 	 */
 	public boolean contains(BoundingBox other) {
 		return (contains(other.southWest) && contains(other.northEast));
 	}
 	
 	public boolean intersects(BoundingBox other) {
 		return (contains(other.northEast)
 				|| contains(other.southWest)
				|| other.contains(this.northEast)
				|| other.contains(this.southWest));
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((northEast == null) ? 0 : northEast.hashCode());
 		result = prime * result
 				+ ((southWest == null) ? 0 : southWest.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BoundingBox other = (BoundingBox) obj;
 		if (northEast == null) {
 			if (other.northEast != null)
 				return false;
 		} else if (!northEast.equals(other.northEast))
 			return false;
 		if (southWest == null) {
 			if (other.southWest != null)
 				return false;
 		} else if (!southWest.equals(other.southWest))
 			return false;
 		return true;
 	}
 	
 }
