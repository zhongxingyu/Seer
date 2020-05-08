 package com.ecommuters;
 
 public class GPSManager {
 	int[] levels = new int[MAX_TIMER_INTERVALS + 2];// uno aggiuntivo per il
 													// tracking
 	// automatico, uno per il
 	// tracking manuale
 	int currentLevel = -1;
 	public final static int[] minDistanceMetres = { 20, 5, 5 };
 	public final static long[] minTimeSecs = { 20, 5, 5 };
 	public final static int[] minutesBeforeStart = { 10 };// minuti
 															// prima
 															// della
 															// partenza
 															// della
 															// traccia
 															// a
 															// partire
 															// dai
 															// quali
 															// attivo
 															// il
 															// GPS
 	public final static int[] minutesAfterStart = { 20 };// minuti
 															// dopo
 															// la
 															// partenza
 															// dopo
 															// i
 															// quali
 															// disattivo
 															// il
 															// GPS
 	public static final int MAX_TIMER_INTERVALS = minutesBeforeStart.length;
 	public static final int AUTOMATIC_TRACKING = MAX_TIMER_INTERVALS;
 	public static final int MANUAL_TRACKING = MAX_TIMER_INTERVALS + 1;
 
 	public boolean startGPS(int level) {
 		levels[level]++;

 		return updateCurrentLevel();
 	}
 
 	public boolean stopGPS(int level) {
 		if (level == MANUAL_TRACKING) {
 			for (int i = 0; i < levels.length; i++)
 				levels[i] = 0;
 			return updateCurrentLevel();
 		}
 		levels[level]--;
 		return updateCurrentLevel();
 	}
 
 	private boolean updateCurrentLevel() {
 		int l = activeLevel();
 		if (currentLevel != l) {
 			currentLevel = l;
 			return true;
 		}
 		return false;
 	}
 
 	private int activeLevel() {
 		for (int i = levels.length - 1; i >= 0; i--)
 			if (levels[i] > 0)
 				return i;
 		return -1;
 	}
 
 	public boolean requestingLocation() {
 		return currentLevel >= 0;
 	}
 
 	public int getMinDinstanceMeters() {
 		return minDistanceMetres[currentLevel];
 	}
 
 	public long getMinTimeSeconds() {
 		return minTimeSecs[currentLevel];
 	}
 
 	public boolean resetLevels() {
 		for (int i = 0; i < MAX_TIMER_INTERVALS; i++)
 			levels[i] = 0;
 		return updateCurrentLevel();
 	}
 
 }
