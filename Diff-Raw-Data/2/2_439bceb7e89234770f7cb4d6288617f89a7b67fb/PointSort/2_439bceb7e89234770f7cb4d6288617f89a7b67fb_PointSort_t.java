package org.infinity.bot.api.utils;
 
 import java.awt.Point;
 import java.util.Arrays;
 import java.util.Comparator;
 
 public class PointSort {
 
 	/**
 	 * Sort points lexicographically (Sort by x and in case of a tie, sort by y)
 	 * @param points Array of points to sort
 	 * @return Array of sorted points
 	 */
 	public static Point[] sortLexi(final Point[] points) {		
 		Arrays.sort(points, PointSort.sortLexi);
 		return points;
 	}
 
 	/**
 	 * Sort points by x value
 	 * @param points Array of points to sort
 	 * @param ascending true if ascending; otherwise false
 	 * @return Array of sorted points
 	 */
 	public static Point[] sortX(final Point[] points, final boolean ascending) {		
 		if (ascending)
 			Arrays.sort(points, PointSort.sortX);
 		else
 			Arrays.sort(points, PointSort.sortXDesc);
 		return points;
 	}
 
 	/**
 	 * Sort points by y value
 	 * @param points Array of points to sort
 	 * @param ascending true if ascending; otherwise false
 	 * @return Array of sorted points
 	 */
 	public static Point[] sortY(final Point[] points, final boolean ascending) {		
 		if (ascending)
 			Arrays.sort(points, PointSort.sortY);
 		else
 			Arrays.sort(points, PointSort.sortYDesc);
 		return points;
 	}
 
 	private static final Comparator<Point> sortY = new Comparator<Point>() {
 		@Override
 		public int compare(Point a, Point b) {
 			if (a.y < b.y) {
 				return -1;
 			}
 			if (a.y > b.y) {
 				return 1;
 			}
 			return 0;
 		}
 	};
 
 	private static final Comparator<Point> sortX = new Comparator<Point>() {
 		@Override
 		public int compare(Point a, Point b) {
 			if (a.x < b.x) {
 				return -1;
 			}
 			if (a.x > b.x) {
 				return 1;
 			}
 			return 0;
 		}
 	};
 
 	private static final Comparator<Point> sortYDesc = new Comparator<Point>() {
 		@Override
 		public int compare(Point a, Point b) {
 			if (a.y > b.y) {
 				return -1;
 			}
 			if (a.y < b.y) {
 				return 1;
 			}
 			return 0;
 		}
 	};
 
 	private static final Comparator<Point> sortXDesc = new Comparator<Point>() {
 		@Override
 		public int compare(Point a, Point b) {
 			if (a.x > b.x) {
 				return -1;
 			}
 			if (a.x < b.x) {
 				return 1;
 			}
 			return 0;
 		}
 	};
 
 	private static final Comparator<Point> sortLexi = new Comparator<Point>() {
 		@Override
 		public int compare(Point a, Point b) {
 			if (a.x < b.x) {
 				return -1;
 			}
 			else if (a.x > b.x) {
 				return 1;
 			}
 			else {
 				if (a.y < b.y) {
 					return -1;
 				}
 				else {
 					return 1;
 				}
 			}
 		}
 	};
 }
