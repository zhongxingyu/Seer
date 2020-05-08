 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 package uw.cse403.nonogramfun.enums;
 
 
 /**
  * Difficulty represents difficulty of a NonoPuzzle. The difficulty is classified
  * according to dimension (# rows, # columns) of the puzzle. There are 4 difficulty
  * levels: Easy, Medium, Hard and Insane. The dimensions that doesn't fit in the 4
  * difficulty levels are classified as undefined.
  */
 public enum Difficulty {
 	
	EASY(5, 5, "Easy"), MEDIUM(10, 10, "Medium"), HARD(14, 14, "Hard"), 
 	INSANE(50, 100, "Insane"), UNDEFINED(5, 100, "Undefined"), INVALID(5, 100, "Invalid");
 
 	private final int minDim;  // Minimum dimension of this difficulty
 	private final int maxDim;  // Maximum dimension of this difficulty
 	private final String name; // Name of this difficulty
 
 
 	// Private constructor
 	private Difficulty(int minDim, int maxDim, String name) {
 		this.minDim = minDim;
 		this.maxDim = maxDim;
 		this.name = name;
 	}
 
 	/**
 	 * @param numRows Number of rows of a NonoPuzzle.
 	 * @param numCols Number of columns of a NonoPuzzle.
 	 * @return The difficulty of puzzle with given dimensions.
 	 */
 	public static Difficulty getDifficulty(int numRows, int numCols) {
 		for(Difficulty d : Difficulty.values()) {
 			if(d.isInRange(numRows, numCols)) {
 				return d;
 			}
 		}
 	    return Difficulty.UNDEFINED;
 	}
 
 	/**
 	 * @param numRows Number of rows of a NonoPuzzle.
 	 * @param numCols Number of columns of a NonoPuzzle.
 	 * @return A boolean that tells if given dimension can be classified into this difficulty.
 	 */
 	public boolean isInRange(int numRows, int numCols) {
 		int min = Math.min(numRows, numCols);
 		int max = Math.max(numRows, numCols);
 
 		if(this == Difficulty.INVALID) {
 			return min < getMinDimension() || getMaxDimension() <= max;
 		}else if(this != Difficulty.UNDEFINED) {
 			return getMinDimension() <= min && max < getMaxDimension();
 		}else{
 			return (getMinDimension() <= min && max < getMaxDimension()) &&
 				   !Difficulty.EASY.isInRange(numRows, numCols) && !Difficulty.MEDIUM.isInRange(numRows, numCols) &&
 				   !Difficulty.HARD.isInRange(numRows, numCols) && !Difficulty.INSANE.isInRange(numRows, numCols);
 		}
 	}
 
 	/**
 	 * @return The minimum possible dimension of this difficulty
 	 */
 	public int getMinDimension() {
 		return minDim;
 	}
 
 	/**
 	 * @return The maximum possible dimension of this difficulty
 	 */
 	public int getMaxDimension() {
 		return maxDim;
 	}
 
 	/**
 	 * Returns string representation of this difficulty
 	 */
 	public String toString() {
 		return name;
 	}
 }
