 package skittles.g4FatKid;
 
 public class PreferredColors {
 
 	// ranks[0] = the highest ranked color, ranks[1] is second, etc.
 	private int[] ranks;
 	private int numColors;
 	private int median;
 	private double[] tasteArray;
 
 	public PreferredColors(int numColors) {
 		ranks = new int[numColors];
 		tasteArray = new double[numColors];
 		this.numColors = numColors;
 		for (int i = 0; i < numColors; i++) {
 			ranks[i] = -1;
 			tasteArray[i] = -2.0;
 		}
 	}
 	
 	/*
 	 * takes an array as input.  The array is the number of skittles for each color in our hand.
 	 * returns true if all preferences are known for aintInHand, false otherwise
	 * ranks[i] == -1
 	 */
 	public boolean allPreferencesKnown(int[] aintInHand) {
 		for (int i = 0; i < numColors; i++) {
			if (aintInHand[i] != 0 && !doesRankExist(i)){ 
				return false;
			}
 		}
 		return true;
 	}
 
 	public void updateTastes(double[] adblTastes) {
 		for (int j = 0; j < numColors; j++) {
 			this.tasteArray[j] = adblTastes[j];
 		}
 	}
 
 	/*
 	 * returns the taste of a specified color
 	 * returns -2 if color taste is unknown
 	 */
 	public double returnTaste(int index) {
 		return this.tasteArray[index];
 	}
 
 	public void rerank(double[] adblTastes) {
 
 		updateTastes(adblTastes);
 		boolean[] ranked = new boolean[numColors];
 		for (int i = 0; i < numColors; i++) {
 			ranked[i] = false;
 		}
 
 		for (int rank = 0; rank < numColors; rank++) {
 			int indexOfBest = -1;
 			double best = 0;
 			for (int i = 0; i < numColors; i++) {
 				if (adblTastes[i] > best && ranked[i] != true) {
 					best = adblTastes[i];
 					indexOfBest = i;
 				}
 			}
 			ranks[rank] = indexOfBest;
 			if (indexOfBest != -1)
 				ranked[indexOfBest] = true;
 		}
 
 		int medRank = numColors / 2; // medRank is the middle rank
 		while (medRank < 0 && adblTastes[ranks[medRank]] < 0.0) {
 			medRank--;
 		}
 		this.median = medRank; // median is the median value, or the smallest
 								// non-zero number if median is negative
 
 	}
 
 	public int getHighestRankedColor() {
 		return ranks[0];
 	}
 
 	/*
 	 * returns the color with the lowest rank
 	 * if all colors are unknown, returns -1
 	 */
 	public int getLowestRankedColor() {
 		if (ranks[0] == -1) return -1;
 		for (int i = 1; i < numColors; i++) {
 			if (ranks[i] == -1) return i - 1;
 		}
 		return ranks[numColors-1];
 	}
 
 	/*
 	 * takes input of rank, returns the color with this rank
 	 */
 	public int getColorAtRank(int rank) {
 		return ranks[rank];
 	}
 	
 	/*
 	 * takes input of color, returns the rank of this color
 	 * if color is unknown, returns -1
 	 * if color is unranked, returns -1
 	 */
 	public int getRankOfColor(int color) {
 		for (int i = 0; i < numColors; i++) {
 			if (ranks[i] == color) return i;
 		}
 		return -1;
 	}
 
 	public void printRanks() {
 		System.out.println("Personal preferences for each color (0th rank is best):");
 		for (int i = 0; i < numColors; i++) {
 			System.out.print("Rank " + i + ": Color ");
 			if (ranks[i] == -1)
 				System.out.println("unknown");
 			else
 				System.out.println(ranks[i]);
 		}
 		System.out.println();
 	}
 	
 	public void printTastes() {
 		System.out.println("Taste values for each color:");
 		for (int i = 0; i < numColors; i++) {
 			System.out.print("Color " + i + " happiness value = ");
 			if (tasteArray[i] == -2.0)
 				System.out.println("unknown");
 			else
 				System.out.println(tasteArray[i]);
 		}
 		System.out.println();
 	}
 	
 	/*
 	 * goodOffer evaluates offers Returns true if offer is "good," false
 	 * otherwise
 	 */
 	public boolean checkIfGoodOffer(int[] skittlesToLose, int[] skittlesToGain) {
 
 		// basic good offer algorithm
 		// takes offers that satisfy
 		// "nothing gained worst than median, nothing lost better than median"
 		boolean toBeGained = false;
 		boolean toBeLost = false;
 		for (int i = 0; i < numColors; i++) {
 			for (int j = 0; j < median; j++) {
 				if (skittlesToGain[i] != 0 && i == ranks[j]) {
 					toBeGained = true;
 					break;
 				}
 			}
 			if (toBeGained)
 				break;
 		}
 		for (int i = 0; i < numColors; i++) {
 			for (int j = numColors - 1; j > median; j--) {
 				if (skittlesToLose[i] != 0 && (i == ranks[j]||ranks[j]==-1)) {
 					toBeLost = true;
 					break;
 				}
 			}
 			if (toBeLost)
 				break;
 		}
 		if (toBeGained && toBeLost)
 			return true;
 		else
 			return false;
 	}
 
 	/*
 	 * returns the color value with median rank
 	 */
 	public int getMedianElement(){
 		return ranks[median];
 	}
 	
 	/*
 	 * returns the rank of the media 
 	 */
 	public int getMedian() {
 		return median;
 	}
	
	
	public boolean doesRankExist(int colorNumber){
		for(int i=0; i < numColors ;i++){
			if(ranks[i]== colorNumber)
				return true;
		}
		return false;
	}
 }
