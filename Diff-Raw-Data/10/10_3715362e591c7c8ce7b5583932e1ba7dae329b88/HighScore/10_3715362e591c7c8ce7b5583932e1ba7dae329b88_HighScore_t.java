 package pl.edu.agh.to1.dice.logic;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 public class HighScore {
 	private static final int MAX_ENTRIES = 5;
 	private static HighScore instance;
 	
 	private HighScore() {}
 	
 	public static HighScore getInstance() {
 		if(instance == null) {
 			instance = new HighScore();
 		}
 		return instance;
 	}
 
 
 	public void update(List<Player> players, ScoreTable table, int scoresPerCategory) throws IOException {
 		TreeMap<Integer, String> scoreMap = new TreeMap<Integer, String>();
 		loadHighScores(scoreMap, scoresPerCategory);
 		getCurrentScores(scoreMap, players, table);
 		saveHighScores(scoreMap, scoresPerCategory);
 	}
 	
 	private void saveHighScores(TreeMap<Integer, String> scoreMap, int scoresPerCategory) throws IOException {
 		String filename = getHighScoreFileName(scoresPerCategory);	
 		FileWriter writer = new FileWriter(filename);
 		int mapSize = scoreMap.size();
 		for(int entry=0; entry < Math.min(MAX_ENTRIES, mapSize); entry++) {
 			Entry<Integer, String> mapEntry = scoreMap.pollLastEntry();
			writer.write(mapEntry.getKey() + " " + mapEntry.getValue() + "\n");
 		}
 		writer.close();
 	}
 
 
 	private void getCurrentScores(Map<Integer, String> scoreMap, List<Player> players, ScoreTable table) {
 		for(Player p : players) {
 			scoreMap.put(table.computeTotalScore(p), p.getName());
 		}
 	}
 	
 	private void loadHighScores(Map<Integer, String> scores, int scoresPerCategory) throws IOException {
 		String filename = getHighScoreFileName(scoresPerCategory);
 		File highScoreFile = new File(filename);
 		if(!highScoreFile.exists()) {
 			highScoreFile.createNewFile();
 		}
 		Scanner scanner = new Scanner( new File(filename) );
 		
 		while(scanner.hasNext()) {
 			int score = scanner.nextInt();
 			String name = scanner.next();	
 			scores.put(score, name);
 		}
 		
 		scanner.close();
 	}
 	
 	private String getHighScoreFileName(int scoresPerCategory) {
 		return "highscores_" + scoresPerCategory + ".txt";
 	}
 
 }
