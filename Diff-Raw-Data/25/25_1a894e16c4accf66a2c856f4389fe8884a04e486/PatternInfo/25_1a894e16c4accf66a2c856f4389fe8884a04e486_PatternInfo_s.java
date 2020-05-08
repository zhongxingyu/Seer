 import java.util.List;
 
 /**
  * Note: this class has a natural ordering that is inconsistent with equals.
  */
 public class PatternInfo implements Comparable<PatternInfo>{
 	
 	private int startIndex;
 	private short length;
 	private int counter;
 	private List<short[]> move;
 	
 	public PatternInfo(int startIndex, short length, int counter, List<short[]> move) {
 		this.startIndex = startIndex;
 		this.length = length;
 		this.counter = counter;
 		this.move = move;
 	}
 	
 	public int getStartIndex() {
 		return this.startIndex;
 	}
 	public short getLength() {
 		return this.length;
 	}
 	public int getCounter() {
 		return this.counter;
 	}
 	public List<short[]> getMove() {
 		return this.move;
 	}
 	
 	public void augmentCounter() {
 		this.counter++;
 	}
 
 	
 	/**
 	 * Note: this class has a natural ordering that is inconsistent with equals.
 	 * 
 	 *@param o - the Object to be compared.
 	 *@return a negative integer, zero, or a positive integer as this object 
 	 *is less than, equal to, or greater than the specified object.
 	 */
 	@Override
 	public int compareTo(PatternInfo o) {
 		// first order by counter
 		if (this.counter < o.counter) {
 			return -1;
 		} else if (this.counter > o.counter) {
 			return 1;
 		}
 		// if counter is equal order by length
 		else  if (this.length<o.length){
 			return -1;
 		} else if (this.length>o.length){
 			return 1;
 		}
 		// if counter and length are equal - mark as same order
 		return 0;
 	}
 	
 	
 	/**
 	 * Returns true if the move stored in this Object is 
 	 * said to be the same as in the List<short[]>.
 	 * Attributes counter, length and startIndex are ignored.
 	 * 
 	 * @param moveTwo Move to be compared
 	 */
 	public boolean moveEquals(List<short[]> moveTwo) {
 		
 		return movesAreAlike(this.move, moveTwo);
 			
 	}
 	
 	
 	private boolean movesAreAlike(List<short[]> moveOne, List<short[]> moveTwo) {
 		if (moveOne.size() != moveTwo.size()){  // shouldn't occur anymore
 			return false;
 		}
 		
 		int minMatches = 20*3;
 		short[] arrayOne; short[] arrayTwo; int matches;
 		
 		for (int i = 0; i < moveOne.size(); i++) {
 			arrayOne = moveOne.get(i);
 			arrayTwo = moveTwo.get(i);
 			matches = 0;
 			for (int j = 0; j < arrayOne.length; j++) {
 				if (arrayOne[j]==arrayTwo[j]) { matches++; }
 			}
 			if (matches < minMatches) { minMatches = matches; }
 		}
 		if (minMatches >= 13*3) {  // seven means i.e.: both arms or both legs
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	
 	
 
 }
