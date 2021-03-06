 package ch.hsr.geohash;
 
 public class BoundingBox {
 	private WGS84Point upperLeft;
 	private WGS84Point lowerRight;
 
 	public BoundingBox(WGS84Point upperLeft, WGS84Point lowerRight) {
 		this.upperLeft = upperLeft;
 		this.lowerRight = lowerRight;
 	}
 
 	public BoundingBox(BoundingBox other) {
 		upperLeft = new WGS84Point(other.upperLeft);
 		lowerRight = new WGS84Point(other.lowerRight);
 	}
 
 	public WGS84Point getUpperLeft() {
 		return upperLeft;
 	}
 
 	public WGS84Point getLowerRight() {
 		return lowerRight;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj instanceof BoundingBox) {
 			BoundingBox o = (BoundingBox) obj;
 			return upperLeft.equals(o.upperLeft) && lowerRight.equals(o.lowerRight);
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return 31 * (713 + upperLeft.hashCode()) + lowerRight.hashCode();
 	}
 
 	public double area() {
 		return (lowerRight.latitude - upperLeft.latitude) * (lowerRight.longitude - lowerRight.longitude);
 	}
 
 	public boolean contains(WGS84Point point) {
		return point.latitude >= upperLeft.latitude && point.latitude <= upperLeft.latitude
				&& point.longitude >= lowerRight.longitude && point.longitude <= lowerRight.longitude;
 	}
 
 	@Override
 	public String toString() {
 		return upperLeft + " -> " + lowerRight;
 	}
 
 	public WGS84Point[] getFourBoundingBoxPoints() {
 		WGS84Point upperRight = new WGS84Point(upperLeft.latitude, lowerRight.longitude);
 		WGS84Point lowerLeft = new WGS84Point(lowerRight.latitude, upperLeft.longitude);
 		return new WGS84Point[] { upperLeft, upperRight, lowerLeft, lowerRight };
 	}
 
 	public WGS84Point getCenterPoint() {
 		double centerLatitude = (upperLeft.latitude + lowerRight.latitude) / 2;
 		double centerLongitude = (upperLeft.longitude + lowerRight.longitude) / 2;
 		return new WGS84Point(centerLatitude, centerLongitude);
 	}
 }
