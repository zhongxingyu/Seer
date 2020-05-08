 package utils;
 
 import hopfield.HopfieldNet;
 
 public class PatternUtils {
 
 	public static void invert(int[] pattern) {
 		for (int i = 0; i < pattern.length; i++) {
 			pattern[i] = (pattern[i] == HopfieldNet.STATE_NEGATIVE) ? 
 				HopfieldNet.STATE_POSITIVE : HopfieldNet.STATE_NEGATIVE;
 		}
 	}
 	
 	/**
 	 * Borra el 50% de los primeros valores del patron pasado por parametro
 	 */
 	public static void addNoise1(int[] pattern) {
 		for (int i = 0; i < pattern.length / 2; i++) {
 			pattern[i] = HopfieldNet.STATE_NEGATIVE;
 		}
 	}
 	
 
 	/**
 	 * Borra el 50% de los primeros valores del patron pasado por parametro
 	 */
 	public static void addNoiseH(int[] pattern) {
 		for (int i = 0; i < 64; i++) {
 			for (int j = 0; j < 64 / 2; j++) {				
 				pattern[i * 64 + j] = HopfieldNet.STATE_NEGATIVE;
 			}
 		}
 	}
 	
 	/**
 	 * Por cada bit, con probabilidad p lo invierte.
 	 */
 	public static void addNoise2(int[] pattern, float p) {
 		for (int i = 0; i < pattern.length; i++) {
 			if (Math.random() < p) {
 				pattern[i] = (pattern[i] == HopfieldNet.STATE_NEGATIVE) ? 
 					HopfieldNet.STATE_POSITIVE : HopfieldNet.STATE_NEGATIVE;
 			}
 		}
 	}
	
	public static float error(int[] p1, int[] p2) {
		int error = 0;
		for (int i = 0; i < p1.length; i++) {
			if (p1[i] != p2[i]) {
				error++;
			}
		}
		return (error * 100.0f) / p1.length;
	}
	
 }
