 package vocab;
 import java.io.Serializable;
 
 
 /**
  * Represents a word and its various translations
  */
public class Word extends RightWrong implements Serializable {
 	
 	private static final long serialVersionUID = 393701205076555364L;
 	private String word;
 	private String romaji;
 	private String kana;
 	private String kanji;
 	public int right;
 	public int wrong;
 	
 	/** The grammatical class of the word */
 	public String type;
 	
 	/**
 	 * Instantiates a new word.
 	 *
 	 * @param word the word in English
 	 * @param romaji the romaji writing
 	 * @param kana the kana writing
 	 * @param kanji the kanji writing
 	 */
 	public Word(String word, String romaji, String kana, String kanji){
 		this.word = word;
 		this.romaji = romaji;
 		this.kana = kana;
 		this.kanji = kanji;
 		type = "noun";
 		right = 0;
 		wrong = 0;
 	}
 	
 	/**
 	 * returns the English word
 	 */
 	public String toString(){
 		return word;
 	}
 	
 	/**
 	 * Sets the romaji.
 	 *
 	 * @param romaji the new romaji
 	 */
 	public void setRomaji(String romaji) {
 		this.romaji = romaji;
 	}
 
 	/**
 	 * Sets the kana.
 	 *
 	 * @param kana the new kana
 	 */
 	public void setKana(String kana) {
 		this.kana = kana;
 	}
 
 	/**
 	 * Sets the kanji.
 	 *
 	 * @param kanji the new kanji
 	 */
 	public void setKanji(String kanji) {
 		this.kanji = kanji;
 	}
 
 	/**
 	 * Check if an attempt from the user of entering the word was corrent
 	 * Note! does not update the 'wrong' and 'right' variables
 	 *
 	 * @param attempt the attempted string
 	 * @return true, if correct
 	 */
 	public boolean check(String attempt, String wordtype, boolean testtype){
 		if (testtype){
 			if (!wordtype.equals(type)){
 				return false;
 			}
 		}
 		if (attempt != null){
 			if (attempt == "") {
 				return false;
 			}
 			if (attempt.equals(romaji) || attempt.equals(kana) || attempt.equals(kanji)){
 				return true;
 			}else{
 				return false;
 			}
 		}else{
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the romaji.
 	 *
 	 * @return the romaji
 	 */
 	public String getRomaji(){
 		return romaji;
 	}
 
 	/**
 	 * Gets the kana.
 	 *
 	 * @return the kana
 	 */
 	public String getKana(){
 		return kana;
 	}
 
 	/**
 	 * Gets the kanji.
 	 *
 	 * @return the kanji
 	 */
 	public String getKanji(){
 		return kanji;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
	public int compareTo(Word otherword) {
 		return (right - wrong) - (otherword.right - otherword.wrong);
 	}
 }
