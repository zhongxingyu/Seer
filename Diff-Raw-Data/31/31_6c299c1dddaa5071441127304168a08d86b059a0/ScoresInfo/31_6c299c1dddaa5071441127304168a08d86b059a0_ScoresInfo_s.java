 package Scores;
 
 public class ScoresInfo implements java.io.Serializable {
 	
 	public static final int DEFAULT_ARRAY_SIZE = 20;
 	private Score[] scoreList;
 	private int lastIndex;  // index of the last entry in the list 
 	
 	public ScoresInfo() {
 		scoreList = new Score[DEFAULT_ARRAY_SIZE];
 		lastIndex = -1;
 	}
 	
 	/**
 	 * Add a score to the scores list
 	 * @param newScore score to add
 	 * @return 'true' is the score is a new record
 	 *         'false' otherwise
 	 */
 	public boolean addEntry(Score newScore) {
 		lastIndex++; //update the index of the last entry in the list
 		//System.out.println("lastIndex: "+lastIndex);
 		// check if list's size needs to be increase
 		if (isFull())
 			doubleArraySize();
 		
 		boolean isHighest = false;
 		boolean done = false;
 		int currentIndex = lastIndex - 1;
 		
 		while(!done && currentIndex >= 0) {
 			//System.out.println("curentIndex: " + currentIndex);
 			if (scoreList[currentIndex].compareTo(newScore) < 0)
 				scoreList[currentIndex + 1] = scoreList[currentIndex];
 			else {
 				scoreList[currentIndex + 1] = newScore;
 				done = true;
 			}
 			currentIndex--;
 		}
 		
 		// the new score has not been added to the list: it's a new record!
 		if (!done) {
 			scoreList[0] = newScore;
 			isHighest = true;
 		}
 		
 		return isHighest;
 	}
 	
 	/**
 	 * Get the score object that holds the greatest amount of point
 	 * @return a Score object that holds the best score
 	 */
 	public Score getBestScoreObject() {
 		return scoreList[0];
 	}
 	
 	/**
 	 * Get the best score
 	 * @return an integer equals to the best score
 	 *         or equals to -1 if the score list is empty
 	 */
 	public int getBestScore() {
 		if (scoreList[0] != null)
 			return scoreList[0].getScore();
 		else
 			return -1;
 	}
 	
 	/**
 	 * Get the player that did the best score
 	 * @return the name of the best player
 	 *         or 'null' if the score list is empty
 	 */
 	public String getBestPlayer() {
 		if (scoreList[0] != null)
 			return scoreList[0].getPlayerName();
 		else 
 			return null;
 	}
 	
 	/**
 	 * Double the size of the array that stores the score
 	 * (needed if this array is full)
 	 */
 	public void doubleArraySize() {
 		Score[] oldList = scoreList;
 		int oldSize = scoreList.length;
 		// create a new array twice long than the old one
 		scoreList = new Score[2 * oldSize];
 		// copy all items of the old array in the new one
 		for (int i = 0; i < oldSize; i++)
 			scoreList[i] = oldList[i];
 	}
 	
 	/**
 	 * Check if the array is full based on lastIndex value
 	 * that equals the size of the array minus one.
 	 * @return
 	 */
 	public boolean isFull() {
 		return lastIndex == scoreList.length - 1;
 	}
 	
 	/**
 	 * For debugging
 	 */
 	public String toString() {
 		String result = "";
 		
 		for (int i = 0; i <= lastIndex; i++) {
 			result += (i+1) + " - " + scoreList[i] + "\n";
 		}
 		
 		return result;
 	}
 
 }
