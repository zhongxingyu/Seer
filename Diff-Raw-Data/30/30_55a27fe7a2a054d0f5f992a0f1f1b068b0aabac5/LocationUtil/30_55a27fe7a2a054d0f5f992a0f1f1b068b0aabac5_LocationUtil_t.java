 package carnero.netmap.common;
 
 import android.util.Log;
 import carnero.netmap.model.XY;
 import com.google.android.gms.maps.model.LatLng;
 import java.util.ArrayList;
 
 public class LocationUtil {
 
 	/**
 	 * Get x,y index of hexagon that contains given position
 	 *
 	 * @param position
 	 * @return
 	 */
 	public static XY getSectorXY(LatLng position) {
 		final XY xy = new XY();
 
 		xy.y = (int) Math.floor(position.latitude / (Constants.SECTOR_HEIGHT - Constants.SECTOR_HEIGHT_CROP));
 
 		if ((xy.y % 2) == 0) {
			xy.x = (int) Math.floor((position.longitude - (Constants.SECTOR_WIDTH / 2)) / Constants.SECTOR_WIDTH);
 		} else {
 			xy.x = (int) Math.floor(position.longitude / Constants.SECTOR_WIDTH);
 		}
 
 		final LatLng center = getSectorCenter(xy);
 		final ArrayList<LatLng> hex = getSectorHexagon(center);
 		final ArrayList<LatLng> sqr = getSectorSquare(center);
 
 		if (inTriangle(position, hex.get(0), hex.get(1), sqr.get(0))) {
 			xy.y ++;
 		} else if (inTriangle(position, hex.get(2), hex.get(3), sqr.get(1))) {
 			xy.y --;
 		} else if (inTriangle(position, hex.get(3), hex.get(4), sqr.get(2))) {
 			xy.y --;
 		} else if (inTriangle(position, hex.get(5), hex.get(0), sqr.get(3))) {
 			xy.y ++;
 		}
 
 		return xy;
 	}
 
 	/**
 	 * Counts center coordinates of hexagon defined by x,y index
 	 *
 	 * @param xy
 	 * @return
 	 */
 	public static LatLng getSectorCenter(XY xy) {
 		return getSectorCenter(xy.x, xy.y);
 	}
 
 	/**
 	 * Counts center coordinates of hexagon defined by x,y index
 	 *
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public static LatLng getSectorCenter(int x, int y) {
 		final boolean odd = ((y % 2) == 0);
 
 		double centerLat = (y * (Constants.SECTOR_HEIGHT - Constants.SECTOR_HEIGHT_CROP)) + (Constants.SECTOR_HEIGHT / 2);
 		double centerLon = (x * Constants.SECTOR_WIDTH);
 		if (odd) {
 			centerLon += Constants.SECTOR_WIDTH;
 		} else {
 			centerLon += (Constants.SECTOR_WIDTH / 2);
 		}
 
 		return new LatLng(centerLat, centerLon);
 	}
 
 	/**
 	 * Returns array of corners of hexagon defined by its center
 	 *
 	 * @param center
 	 * @return
 	 */
 	public static ArrayList<LatLng> getSectorHexagon(LatLng center) {
 		final ArrayList<LatLng> corners = new ArrayList<LatLng>();
 
 		for (int i = 1; i <=6; i ++) {
 			corners.add(getHexagonCorner(center, i));
 		}
 
 		return corners;
 	}
 
 	/**
 	 * Returns array of corners of hexagon defined by its center
 	 *
 	 * @param center
 	 * @return
 	 */
 	public static ArrayList<LatLng> getSectorSquare(LatLng center) {
 		final ArrayList<LatLng> corners = new ArrayList<LatLng>();
 
 		for (int i = 1; i <=4; i ++) {
 			corners.add(getSquareCorner(center, i));
 		}
 
 		return corners;
 	}
 
 	/**
 	 * Returns coordinates of points 1 to 6 of hexagon defined by its center
 	 *
 	 * @param center
 	 * @param cnt
 	 * @return
 	 */
 	public static LatLng getHexagonCorner(LatLng center, int cnt) {
 		cnt = cnt -1; // 0..5
 
 		double lat;
 		double lon;
 
 		switch (cnt) {
 			case 0:
 				lat = center.latitude + (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude;
 				return new LatLng(lat, lon);
 			case 1:
 				lat = center.latitude + Constants.SECTOR_HEIGHT_CROP;
 				lon = center.longitude + (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 2:
 				lat = center.latitude - Constants.SECTOR_HEIGHT_CROP;
 				lon = center.longitude + (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 3:
 				lat = center.latitude - (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude;
 				return new LatLng(lat, lon);
 			case 4:
 				lat = center.latitude - Constants.SECTOR_HEIGHT_CROP;
 				lon = center.longitude - (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 5:
 				lat = center.latitude + Constants.SECTOR_HEIGHT_CROP;
 				lon = center.longitude - (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * Returns coordinates of points 1 to 4 of square defined by its center
 	 *
 	 * @param center
 	 * @param cnt
 	 * @return
 	 */
 	public static LatLng getSquareCorner(LatLng center, int cnt) {
 		cnt = cnt -1; // 0..3
 
 		double lat;
 		double lon;
 
 		switch (cnt) {
 			case 0:
 				lat = center.latitude + (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude + (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 1:
 				lat = center.latitude - (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude + (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 2:
 				lat = center.latitude - (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude - (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			case 3:
 				lat = center.latitude + (Constants.SECTOR_HEIGHT / 2.0);
 				lon = center.longitude - (Constants.SECTOR_WIDTH / 2.0);
 				return new LatLng(lat, lon);
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * Check if point is inside of triangle
 	 * source: http://stackoverflow.com/questions/2464902/determine-if-a-point-is-inside-a-triangle-formed-by-3-points-with-given-latitude
 	 *
 	 * @param point
 	 * @param x1
 	 * @param x2
 	 * @param x3
 	 * @return
 	 */
 	public static boolean inTriangle(LatLng point, LatLng x1, LatLng x2, LatLng x3) {
 		double o1 = getOrientationResult(x1.longitude, x1.latitude, x2.longitude, x2.latitude, point.longitude, point.latitude);
 		double o2 = getOrientationResult(x2.longitude, x2.latitude, x3.longitude, x3.latitude, point.longitude, point.latitude);
 		double o3 = getOrientationResult(x3.longitude, x3.latitude, x1.longitude, x1.latitude, point.longitude, point.latitude);
 
 		return (o1 == o2) && (o2 == o3);
 	}
 
 	private static int getOrientationResult(double x1, double y1, double x2, double y2, double px, double py) {
 		final double orientation = ((x2 - x1) * (py - y1)) - ((px - x1) * (y2 - y1));
 
 		if (orientation > 0) {
 			return 1;
 		} else if (orientation < 0) {
 			return -1;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Get heading from one location to another
 	 *
 	 * @param location1
 	 * @param location2
 	 * @return
 	 */
 	public static Double getHeading(LatLng location1, LatLng location2) {
 		double result = new Double(0);
 
 		double lat1 = location1.latitude;
 		double lon1 = location1.longitude;
 		double lat2 = location2.latitude;
 		double lon2 = location2.longitude;
 
 		int ilat1 = (int) Math.round(0.5 + lat1 * 360000);
 		int ilon1 = (int) Math.round(0.5 + lon1 * 360000);
 		int ilat2 = (int) Math.round(0.5 + lat2 * 360000);
 		int ilon2 = (int) Math.round(0.5 + lon2 * 360000);
 
 		lat1 *= Constants.DEG_TO_RAD;
 		lon1 *= Constants.DEG_TO_RAD;
 		lat2 *= Constants.DEG_TO_RAD;
 		lon2 *= Constants.DEG_TO_RAD;
 
 		if (ilat1 == ilat2 && ilon1 == ilon2) {
 			return new Double(result);
 		} else if (ilat1 == ilat2) {
 			if (ilon1 > ilon2) {
 				result = new Double(270);
 			} else {
 				result = new Double(90);
 			}
 		} else if (ilon1 == ilon2) {
 			if (ilat1 > ilat2) {
 				result = new Double(180);
 			}
 		} else {
 			Double c = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2) * Math.cos(lat1) * Math.cos(lon2 - lon1));
 			Double a = Math.asin(Math.cos(lat2) * Math.sin(lon2 - lon1) / Math.sin(c));
 			result = new Double(a * Constants.RAD_TO_DEG);
 
 			if (ilat2 > ilat1 && ilon2 > ilon1) {
 				// result don't need change
 			} else if (ilat2 < ilat1 && ilon2 < ilon1) {
 				result = 180f - result;
 			} else if (ilat2 < ilat1 && ilon2 > ilon1) {
 				result = 180f - result;
 			} else if (ilat2 > ilat1 && ilon2 < ilon1) {
 				result += 360f;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Computes distance between two locations in metres
 	 *
 	 * @param location1
 	 * @param location2
 	 * @return
 	 */
 	public static double getDistance(LatLng location1, LatLng location2) {
 		double lat1 = location1.latitude;
 		double lon1 = location1.longitude;
 		double lat2 = location2.latitude;
 		double lon2 = location2.longitude;
 
 		lat1 *= Constants.DEG_TO_RAD;
 		lon1 *= Constants.DEG_TO_RAD;
 		lat2 *= Constants.DEG_TO_RAD;
 		lon2 *= Constants.DEG_TO_RAD;
 
 		final double d = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2);
 		final double distance = Constants.EARTH_D * Math.acos(d); // distance in km
 
 		if (Double.isNaN(distance) == false) {
 			return distance * 1000d;
 		} else {
 			return 0d;
 		}
 	}
 
 	/**
 	 * Get point at given direction and distance
 	 *
 	 * @param location
 	 * @param bearing
 	 * @param distance in metres
 	 * @return
 	 */
 	public static LatLng getPointInDirection(LatLng location, double bearing, double distance) {
 		final double rlat1 = location.latitude * Constants.DEG_TO_RAD;
 		final double rlon1 = location.longitude * Constants.DEG_TO_RAD;
 		final double rbearing = bearing * Constants.DEG_TO_RAD;
 		final double rdistance = (distance / 1000d) / Constants.EARTH_D;
 
 		final double rlat = Math.asin(Math.sin(rlat1) * Math.cos(rdistance) + Math.cos(rlat1) * Math.sin(rdistance) * Math.cos(rbearing));
 		final double rlon = rlon1 + Math.atan2(Math.sin(rbearing) * Math.sin(rdistance) * Math.cos(rlat1), Math.cos(rdistance) - Math.sin(rlat1) * Math.sin(rlat));
 
 		return new LatLng(rlat * Constants.RAD_TO_DEG, rlon * Constants.RAD_TO_DEG);
 	}
 }
