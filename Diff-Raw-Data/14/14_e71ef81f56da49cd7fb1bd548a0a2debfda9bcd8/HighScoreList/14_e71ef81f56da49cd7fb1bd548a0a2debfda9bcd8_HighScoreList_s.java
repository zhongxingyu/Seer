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
		//...
		if (highScores.size() <= MAX_SIZE && s.score > lowestScore)
 			highScores.add(s);
 		Collections.sort(highScores);
 		Collections.reverse(highScores);
 	}
 
	//returns this HighScoreList as a string for printing.
 	public String toString() {
 		String estr = "";
 		for (Score s : highScores) {
 			estr += s + "\n";
 		}
 		return estr;
 	}
 	
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
