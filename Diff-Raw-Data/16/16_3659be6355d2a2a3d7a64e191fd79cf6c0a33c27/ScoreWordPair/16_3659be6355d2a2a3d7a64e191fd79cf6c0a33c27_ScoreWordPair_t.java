 import java.lang.*;
 
 
 public class ScoreWordPair implements Comparable<ScoreWordPair> {
    public final Integer score;
    public final String word;
    
    public ScoreWordPair() {
       score = -1;
       word = "";
       System.err.println("Default constructing a ScoreWordPair :(");
    }
    
    public ScoreWordPair(Integer score, String word) {
       this.score = score;
       this.word = word;
    }
    
    /**
    * Natural sort order is descending by score, then ascending alphabetically.
     * This -returns 0 if scores AND words are equal,
     *      -returns -1 if this.score < o.score 
     *              OR if scores are equal, and this.word is BEFORE o.word in
     *                    the alphabet
     *      -returns 1 if this.score > o.score 
     *              OR if scores are equal, and this.word is AFTER o.word in
     *                    the alphabet
     * It should never be the case that words are the same and scores are
     *    unequal, so for the sake of simplicity, we're not checking for that
     */
    @Override
    public int compareTo( ScoreWordPair o ) {
       int retVal;
       int scoreCompare = this.getScore().compareTo( o.getScore() );
       if( scoreCompare == 0 ) {
          int wordCompare = this.getWord().compareTo( o.getWord() );
          retVal = wordCompare;
       } else {
         retVal = -1* scoreCompare;
       }
       return retVal;
    }
    
    
     public String toString(){ 
            return "" + score + ", " + word ; 
     }
 
     public Integer getScore() {
     	return score;
     }
 
     public String getWord() {
     	return word;
     }
     
    /*
    // modified from http://stackoverflow.com/a/677248/1572766
    public boolean equals(Object other) {
     	if (other instanceof ScoreWordPair) {
     		ScoreWordPair otherPair = (ScoreWordPair) other;
     		return 
     		((  this.score == otherPair.score ||
     			( this.score != null && otherPair.score != null &&
     			  this.score.equals(otherPair.score))) &&
     		 (	this.word == otherPair.word ||
     			( this.word != null && otherPair.word != null &&
     			  this.word.equals(otherPair.word))) );
     	}
 
     	return false;
     }
    public int hashCode() {
     	int hashFirst = score != null ? score.hashCode() : 0;
     	int hashSecond = word != null ? word.hashCode() : 0;
 
     	return (hashFirst + hashSecond) * hashSecond + hashFirst;
     }
    */
    
 
 }
