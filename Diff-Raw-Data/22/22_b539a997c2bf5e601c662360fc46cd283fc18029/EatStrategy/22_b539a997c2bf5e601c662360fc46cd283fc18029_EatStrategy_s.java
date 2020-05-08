 package skittles.g4FatKid;
 
 import java.util.ArrayList;
 
 class EatStrategy{
 	
 	private int[] aintInHand;
 	private int intColorNum;
 	private int intLastEatIndex;
 	ArrayList<Integer> colorTasted;
 	int rounds = 0;
 
 	PreferredColors prefs;
 	double initSkittleNum = 0;
 	double skittleNum = 0;
 
 	public EatStrategy(int[] inHand, int intColorNum, PreferredColors prefs) {	
 		
 		aintInHand = new int[intColorNum];
 		for (int j = 0; j < intColorNum; j++) {
 			this.aintInHand[j] = inHand[j];
 		}
 		this.intColorNum = intColorNum;
 		
 		intLastEatIndex = 0;
 
 		this.prefs = prefs;
 		for (int j = 0; j < aintInHand.length; j++)
 			initSkittleNum += aintInHand[j];
 		skittleNum = initSkittleNum;
 		colorTasted = new ArrayList<Integer>();
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
 		//int minIndex = -1;
 
 		// Rounds to taste each of the skittles to check if we like them
 		// if some preferences are still unknown...
 		int j;
		if (!prefs.allPreferencesKnown() && rounds<aintInHand.length-1) {
 			// find color with smallest amount from the colors we still don't know
 			for (j = 0; j < aintInHand.length; j++) {
 				// only if taste of color j is unknown
				if(colorTasted.isEmpty() | !colorTasted.contains(j) && aintInHand[j] !=0){
 					intLastEatIndex = j;
 					whatToEatNow[intLastEatIndex] = 1;
 					colorTasted.add(j);
 					break;
 				}
				else if(aintInHand[j] == 0 && j==aintInHand.length-1)
					for(int l=0;l<aintInHand.length;l++)
						if(aintInHand[l] != 0){
 							intLastEatIndex = l;
 							whatToEatNow[l] = 1;
 							break;
							}	
		}
 			rounds++;
	}
 			// after for loop, minIndex should be the index of the smallest non-zero color
 		//	intLastEatIndex = minIndex;
 			// eat one of this min color
 		//	whatToEatNow[intLastEatIndex] = 1;
 			//skittleNum--;
 		// else, all preferences are known, and we can move to phase 2: eat the lower colors one by one
 		// this phase goes until only one color is left
 		// TODO change to a better criteria for ending phase 2
 		
 		else if (rounds <= 2*initSkittleNum) {
 			int k = aintInHand.length-1;
 			intLastEatIndex = prefs.getLowestRankedColor();
 			while(aintInHand[intLastEatIndex] == 0) {
 				intLastEatIndex = prefs.getColorAtRank(k);
 				if(intLastEatIndex == -1)
 					intLastEatIndex = 0;
 				k--;
 			}	
 			whatToEatNow[intLastEatIndex] = 1;
 			//whatToEatNow[1] = 1;
 			rounds++;
 		}
 		else {
 			// after we're finished hoarding just eat all of one color together
 			for (int l = 0; l < aintInHand.length; l++) {
 				if (aintInHand[l] != 0)
 					min = l;
 			}
 			intLastEatIndex = min;
 			whatToEatNow[intLastEatIndex] =  aintInHand[intLastEatIndex];
 			rounds++;
 		}
 		return whatToEatNow;
 	}
 }
 
 /*if (prefs.returnTaste(j) == -2.0) {
 if (aintInHand[j] < min && aintInHand[j] > 0) {
 	min = aintInHand[j];
 	minIndex = j;
 }*/
