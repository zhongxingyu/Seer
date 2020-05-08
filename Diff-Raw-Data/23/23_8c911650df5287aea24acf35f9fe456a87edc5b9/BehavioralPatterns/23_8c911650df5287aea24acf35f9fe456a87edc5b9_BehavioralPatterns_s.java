 /**
  * 
  */
 package org.microworld.robots;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.microworld.utils.Point;
 
 /**
  * @author riccardo
  * 
  */
 public abstract class BehavioralPatterns {
 	public static final int STRAIGHT_LINE = 0;
 	public static final int ROUND = 1;
 	public static final int ROUTE = 2;
 	public static final int RIDER = 3;
 
	private static double mLatStr[] = {
 
 	};
	private static double mLngStr[] = {

 	};
 
 	private static double mLatRound[] = {
 
 	};
 
 	private static double mLngRound[] = {
 
 	};
 
 	// origin via Sernesi 1 Bolzano
 	// destination viale Druso 15 Bolzano
 	private static double mLatRoute[] = { 46.498810, 46.499030, 46.499620,
 			46.500060, 46.499190, 46.498290, 46.497880, 46.497140, 46.496430,
 			46.495700, 46.495220, 46.494860, 46.494692, 46.494640, 46.494660 };
 
 	private static double mLngRoute[] = { 11.351110, 11.350290, 11.349220,
 			11.348490, 11.347830, 11.347850, 11.348830, 11.349130, 11.349370,
 			11.349630, 11.349820, 11.348710, 11.347340, 11.345790, 11.344810 };
 
 	private static final double[] fixedLat = {
 
 	};
 
 	private static final double[] fixedLng = {
 
 	};
 
 	private static final int min(int a, int b) {
 		if (a < b)
 			return a;
 		return b;
 	}
 
 	private static final List<Point> getPath(double[] mLat, double[] mLng) {
 		int minSize = min(mLat.length, mLng.length);
 		List<Point> result = new ArrayList<Point>();
 		for (int i = 0; i < minSize; i++)
 			result.add(new Point(mLat[i], mLng[i]));
 		return result;
 	}
 
 	private static final List<Point> generateSinglePoint() {
 		int i = ((int) Math.random() * 1000)
 				% min(fixedLat.length, fixedLng.length);
 		List<Point> result = new ArrayList<Point>();
 		result.add(new Point(fixedLat[i], fixedLng[i]));
 		return result;
 	}
 
 	public static final List<Point> getBehavioralPattern(int id) {
 		switch (id) {
 		case STRAIGHT_LINE:
 			return getPath(mLatStr, mLngStr);
 		case ROUND:
 			return getPath(mLatRound, mLngRound);
 		case ROUTE:
 			return getPath(mLatRoute, mLngRoute);
 		case RIDER:
 			return generateSinglePoint();
 		}
 		return null;
 
 	}
 }
