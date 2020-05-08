 import java.io.*;
 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 
 public class HighScores {
 	private static final String HIGH_SCORE_FILE = "data/scores.txt";
 	private static final int MAX_SCORES = 10;
 	private static final String[] DUMMY_NAMES = {
 		"John",
 		"Josh",
 		"Larry",
 		"Bob",
 		"133t",
 		"George",
 		"Chunk",
 		"Cloud",
 		"Gary",
 		"Cthulu"
 	};
 
 	public static ArrayList<HighScore> score_list;
 
 
 	/* Open HIGH_SCORE_FILE and read the scores, names into an array */
 	public HighScores() {
 		String line;
 		String[] parts;
 		int score;
 
 		score_list = new ArrayList<HighScore>(MAX_SCORES);
 
 		try {
 			BufferedReader file = new BufferedReader(new FileReader(HIGH_SCORE_FILE));
 
 			while((line = file.readLine()) != null) {
 				parts = line.split("\t", 2);
 
 				score = Integer.parseInt(parts[0]);
 
 				score_list.add(new HighScore(score, parts[1]));
 			}
 
 			file.close();
 		} catch (IOException e) {
 			System.err.println("No high score file found, creating new high scores...");
			//TODO prepopulate high score with dummy scores
			// logic seems sound, but score_list does not load values
			int dummy_score = 10;
			for(int i=0; i<score_list.size(); i++){
				score_list.add(new HighScore(dummy_score*10, DUMMY_NAMES[i]));
				dummy_score *=10;
 			}
 		}
 	}
 	
 	public boolean isNewHighScore(int score) {
 		for(int i = score_list.size() - 1; i >= 0; i--) {
 			if (score < score_list.get(i).score) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public void displayScoreDialog() {
 		new HighScoreDialog(this);
 	}
 
 	/*
 	 * Open HIGH_SCORE_FILE for writing (not appending), and write the high scores to the file in order 
 	 */
 	public void writeScoreFile() {
 		try{
 			FileWriter write = new FileWriter(HIGH_SCORE_FILE);
 			PrintWriter out = new PrintWriter(write);
 
 			for (int i = 0; i < score_list.size() && i < 10; i++){
 				out.println(score_list.get(i));
 			}
 			out.close();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This prints out the current high scores, it's mostly for debugging
 	 */
 	public void printScores() {
 		System.out.println("High Scores:");
 		for(int i=0; i<score_list.size();i++){
 			System.out.println(score_list.get(i));
 		}
 	}
 	
 	
 	public void add(String name, int score) {
 		score_list.add(new HighScore(score, name));
 		int min_element; // marks last element to compare
 		int index;       // index of an element to compare
 		int temp_score;        // used to swap scores
 		String temp_name;      // used to swap names
 		
 		for(min_element = score_list.size() - 1; min_element >= 0; min_element --) {
 			for(index = 0; index <= min_element - 1; index++){
 				if(score_list.get(index).score < score_list.get(index + 1).score) {
 					temp_score = score_list.get(index).score;
 					score_list.get(index).score = score_list.get(index + 1).score;
 					score_list.get(index + 1).score = temp_score;
 					
 					temp_name = score_list.get(index).name;
 					score_list.get(index).name = score_list.get(index + 1).name;
 					score_list.get(index+1).name = temp_name;
 				}
 			}
 		}
 	}
 
 	public void newScore(int score) {
 		if (this.isNewHighScore(score)) {
 			String name =  JOptionPane.showInputDialog("New high score, enter your name");
 
 			this.add(name, score);
 			writeScoreFile();
 		}
 	}
 
 	// If you have trouble w/ HighScoresDialog - change this to public static class ...
 	public static class HighScore {
 		public String name;
 		public int score;
 
 		public HighScore(int score, String name) {
 			this.name = name;
 			this.score = score;
 		}
 
 		public String toString() {
 			return score + "\t" + name;
 		}
 	}
 }
