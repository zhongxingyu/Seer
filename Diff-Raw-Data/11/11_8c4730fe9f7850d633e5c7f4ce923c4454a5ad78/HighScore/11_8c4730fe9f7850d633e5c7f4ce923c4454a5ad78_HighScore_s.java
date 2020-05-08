 package tetrix.core;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 

 /**
  * 
  * A High Score class with a list of maximum 10 players. It compares the score of the
  * current player with the score in the list to see if the player should be added in the 
  * high score or not.  
  * @author Andreas Karlberg
  *
  */
 
 public class HighScore implements IHighScore {
 	private static int maxPlayers;
 	private static HighScore instance;
 	private static boolean reachedHighscore;
 
 	public HighScore() throws FileNotFoundException {
 	}
 
 	public static synchronized HighScore instance() throws FileNotFoundException {
 		if(instance == null) {
 			instance = new HighScore();
 			maxPlayers = 10;
 			reachedHighscore = false;
 		}
 		return instance;
 	}
 	
 	@Override
 	public List<Entry> getHighScore() throws FileNotFoundException{
 		FileReader f = new FileReader("highscore/highscore.dat");
 		List<String> rows = f.getRows();
 
 		List<Entry> l = new ArrayList<Entry>();
 		for (String row : rows){
 			String[] np = row.split(":");
 			String name = np[0];
 			String points = np[1];
 			Entry e = new Entry(name, Integer.valueOf(points));
 			l.add(e);
 		}
 		return l;
 	}
 
 	@Override
 	public void setHighScore(String playerName, int score) throws IOException {
 		List<Entry> ls = this.getHighScore();
 		Entry e = ls.get(ls.size()-1);
 
 		if (e.getPoints()< score){
 			ls.remove(e);
 			ls.add(new Entry(playerName, score));
 			reachedHighscore = true;
 		} else {
 			reachedHighscore = false;
 		}
 		
 		Collections.sort(ls);
 		List<String> ss = new ArrayList<String>();
 		for(Entry e1:ls){
 			String row = e1.getName() + ":" + e1.getPoints(); 
 			ss.add(row);
 			for (int i=0; i<maxPlayers; i++){
 				if(ss.size() > maxPlayers){
 					ss.remove(maxPlayers);
 				}
 			}
 		}
 		
 		FileReader f = new FileReader("highscore/highscore.dat");
 		f.writeRows(ss);
 	}
	
<<<<<<< HEAD
	public static boolean writtenToHighscore() {
		return reachedHighscore;
=======
 
 	public static boolean writtenToHighscore() {
 		return reachedHighscore;
 	}
 
 	public String getPlayerName() throws FileNotFoundException{
 		FileReader p = new FileReader("highscore/playerName.dat");
 		return p.getRow();
 
 	}
>>>>>>> 5ce2e2907545218579a8469f91c9f78d7d4f600d

	}
 }
