 package util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import menu.Highscore;
 
 public class Config {
 	private static final int MAX_SCORES = 10;
 
 	public static void createConfig() {
 		String home = System.getProperty("user.home");
 		try {
			createConfigFolder();
 			createFile(home + "/.config/tuxbounce/highscores");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
	
	private static void createConfigFolder() {
		String home = System.getProperty("user.home");
		File f = new File(home + "/.config/tuxbounce/");
		f.mkdirs();
	}
 
 	private static void createFile(String filename) throws IOException {
		File f = new File(filename);
		if (!f.exists()) {
			f.createNewFile();
 		}
 	}
 
 	public static boolean checkIsHighscore(int score) {
 		String home = System.getProperty("user.home");
 		int i = 0;
 		try {
 			Scanner s = new Scanner(new File(home
 					+ "/.config/tuxbounce/highscores"));
 			for (; s.hasNext(); i++) {
 				s.next();
 				if (s.nextInt() < score) {
 					return true;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return i < MAX_SCORES;
 	}
 
 	public static List<Highscore> readHighscores() {
 		List<Highscore> highscores = new ArrayList<Highscore>();
 		try {
 			String home = System.getProperty("user.home");
 			Scanner s = new Scanner(new File(home
 					+ "/.config/tuxbounce/highscores"));
 			while (s.hasNext()) {
 				String name = s.next();
 				int score = s.nextInt();
 				highscores.add(new Highscore(name, score));
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return highscores;
 	}
 
 	public static void addNewHighScore(Highscore score) {
 		List<Highscore> highscores = readHighscores();
 		List<Highscore> newScores = new ArrayList<Highscore>();
 		if (highscores.size() == 0) {
 			newScores.add(score);
 		} else {
 			boolean added = false;
 			for (int i = 0; i < highscores.size(); i++) {
 				Highscore hs = highscores.get(i);
 				if (!added && hs.getScore() < score.getScore()) {
 					newScores.add(score);
 					added = true;
 				}
 				newScores.add(hs);
 			}
 			if (!added && highscores.size() < MAX_SCORES - 1) {
 				newScores.add(score);
 			}
 		}
 		writeHighscores(newScores);
 	}
 
 	private static void writeHighscores(List<Highscore> highscores) {
 		try {
 			String home = System.getProperty("user.home");
 			String filename = home + "/.config/tuxbounce/highscores";
 			Writer out = new BufferedWriter(new FileWriter(filename));
 			for (int i = 0; i < highscores.size() && i < MAX_SCORES; i++) {
 				Highscore hs = highscores.get(i);
 				out.write(hs.getName() + " " + hs.getScore() + "\n");
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
