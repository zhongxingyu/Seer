 package testpackage.shared.ship;
 
 /**
  * class for storing game rules
  */
 public class Rules {
 	public static final int standardSpeerfeuerTime = 5000;
 
	public static final boolean enableShotsPerShip = true;
	public static final boolean defaultReichweiteVonSchiffenEnabled = true;
 	/**
 	 * Necessary for test compliance
 	 */
 	public static int defaultWidth = 10;
 	/**
 	 * Necessary for test compliance
 	 */
 	public static int defaultHeight = 10;
 	/**
 	 * Necessary for test compliance
 	 */
 	public static boolean defaultAllowMultipleShotsPerTurn = false;
 	
 	/**
 	 * default ship counts. first: ship length; second: ship count
 	 * these values are necessary for test compliance
 	 */
 	public static int[][] ships = {
 		{2,4,6}, // 4 schnellbote mit range 6
 		{3,3,8}, // TODO dynamisch
 		{4,2,10},
 		{5,1,20}
 	};
 	
 	/**
 	 * number of shots per each part of not-sunken ship part. value necessary for test compliance
 	 */
 	public static int shotsPerShipPart = 5;
 	
 }
