 package skittles.g4FatKid;
 
 import java.util.ArrayList;
 
 public class EatStrategy {
 	
 	private int[] aintInHand;
 	private int intColorNum;
 	private int intLastEatIndex;
 
 	PreferredColors prefs;
 
 	public EatStrategy(int[] inHand, int intColorNum, PreferredColors prefs) {	
 		
 		aintInHand = new int[intColorNum];
 		for (int j = 0; j < intColorNum; j++) {
 			this.aintInHand[j] = inHand[j];
 		}
 		this.intColorNum = intColorNum;
 		intLastEatIndex = 0;
 		this.prefs = prefs;
 	}
 
 	public void updatePrefs(PreferredColors prefs){
 		this.prefs = prefs;
 	}
 
 	/*
 	 * returns an array of length numberOfColors
 	 * array[x] = number of color x to eat
 	 */
 	public int[] eatNow(int[] inHand) {
 		
 		for (int j = 0; j < intColorNum; j++) {
 			this.aintInHand[j] = inHand[j];
 		}
 		int[] whatToEatNow = new int[intColorNum];
 		int min = Integer.MAX_VALUE;
 		int minIndex = -1;
 
 		// Rounds to taste each of the skittles to check if we like them
 		// if some preferences are still unknown...
 		if (!prefs.allPreferencesKnown(aintInHand)) {
 			// find color with smallest amount from the colors we still don't know
 			for (int j = 0; j < intColorNum; j++) {
 				// only if taste of color j is unknown
 				if (prefs.getRankOfColor(j) == -1) {
 					if (aintInHand[j] < min && aintInHand[j] > 0) {
 						min = aintInHand[j];
 						minIndex = j;
 					}
 				}
 			}
 			// after for loop, minIndex should be the index of the smallest non-zero color
 			intLastEatIndex = minIndex;
 			// eat one of this min color
			whatToEatNow[intLastEatIndex] = 1; //bug here
 			return whatToEatNow;
 		}
 		
 		// else, all preferences are known, and we can move to phase 2:
 		// this phase goes until only one color is left
 		else {
 			// check number of colors remaining in our hand
 			int colorCount = 0;
 			for (int i = 0; i < intColorNum; i++) {
 				if (aintInHand[i] > 0) colorCount++;
 			}
 			
 			int colorsToHoard = 2; // this is the number of colors to hoard
 			// if we only colors we are hoarding left in our hand, eat all of them
 			if (colorCount <= colorsToHoard ) {
 				for (int i = 0; i < intColorNum; i++) {
 					if (aintInHand[i] != 0) {
 						whatToEatNow[i] = aintInHand[i];
 						return whatToEatNow;
 					}
 				}
 			}
 			// else, there are more than one color left in our hand, so we eat one of the colors ranked median or lower
 			else {
 				int medianRank = prefs.getMedian();
 				for (int i = medianRank; i < intColorNum; i++) {
 					if (aintInHand[i] > 0) {
 						whatToEatNow[i] = 1;
 						return whatToEatNow;
 					}
 				}
 				// if we get here, all colors below median have been traded/eaten
 				// so, eat one of color with the least amount in our hand
 				for (int i = 0; i < intColorNum; i++) {
 					if (aintInHand[i] < min && aintInHand[i] > 0) {
 						min = aintInHand[i];
 						minIndex = i;
 					}
 				}
 				whatToEatNow[minIndex] = 1;
 				return whatToEatNow;
 			}
 		}
 		return whatToEatNow;
 	}
 	
 }
