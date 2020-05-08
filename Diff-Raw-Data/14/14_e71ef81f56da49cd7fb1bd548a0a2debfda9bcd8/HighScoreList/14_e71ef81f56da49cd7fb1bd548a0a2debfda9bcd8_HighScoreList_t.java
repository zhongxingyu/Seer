 package javagame;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class HighScoreList implements Serializable {
 
 	public static final long serialVersionUID = 42L;
 	public static final int MAX_SIZE = 20;
 	private ArrayList<Score> highScores;
 	
 	public HighScoreList() {
 		highScores = generateDefaultScores();
 	}
 	
 	public void add(Score s) {
 		Collections.sort(highScores);
 		int lowestScore = highScores.get(0).score;
 		//and then we compare lowest score to s and see
 		//if s needs to be added to the list
		if (s.score > lowestScore) {
			if (highScores.size() >= MAX_SIZE)
				highScores.remove(0);
 			highScores.add(s);
		}
 		Collections.sort(highScores);
 		Collections.reverse(highScores);
 	}
 
	// Returns this HighScoreList in a String representation.
 	public String toString() {
 		String estr = "";
 		for (Score s : highScores) {
 			estr += s + "\n";
 		}
 		return estr;
 	}
 	
	/** Populates this HighScoreList with default scores. You know,
	 *  so the player can compete against zany names.
	 *  @return the initialized list of Score objects */
 	private ArrayList<Score> generateDefaultScores() {
 		ArrayList<Score> scores = new ArrayList<Score>();
 		String[] names = {"Polypyrgonices", "Malaclypse", "Brobdingnag", "Zaphod Beeblebrox",
 				"Willie Stroker", "Ben Dover", "Hugh Jass", "Harold A. Ballitch", "Kevin C.",
 				"Ashley B."};
 		for (int i = 0; i < 10; i++) {
 			Score s = new Score(names[i], 5000 * i + 1000);
 			scores.add(s);
 		}
 		return scores;
 	}
 }
