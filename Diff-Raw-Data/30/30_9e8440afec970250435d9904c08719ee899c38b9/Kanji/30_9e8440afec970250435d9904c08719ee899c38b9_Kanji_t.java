 package vocab;
 import java.io.Serializable;
 
 
 /**
  * Represents a kanji or kanji compound
  */
 public class Kanji extends RightWrong implements Serializable {
 
 	private static final long serialVersionUID = 2216391087212502892L;
 	
 	/** The kanji. */
 	public String kanji;
 	
 	/** The reading. */
 	public String reading;
 	
 	/** The translation. */
 	public String translation;
 	
 	/** The number of correct guesses */
 	public int right;
 	
 	/** The number of wrong guesses */
 	public int wrong;
 	
 	/**
 	 * Instantiates a new kanji.
 	 *
 	 * @param kanji the kanji
 	 */
 	public Kanji(String kanji){
 		this.kanji = kanji;
 		reading = "";
 		translation = "";
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString(){
 		return kanji;
 	}
 
 	/**
 	 * Check whether a guess is correct or not
 	 *
 	 * @param text the guess
 	 * @return true, if the answer is correct
 	 */
 	public boolean check(String text) {
 		if (text.equals(reading)){
 			return true;
 		}else{
 			return text.equals(translation);
 		}
 	}
 }
