 package highscore;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import interfaces.IHighscore;
 import interfaces.IHighscoreEntry;
 
 public class DummyHighscore implements IHighscore {
	private static int DUMMY_SCORE_1 = 40;
	private static int DUMMY_SCORE_2 = 50;
	private static int DUMMY_SCORE_3 = 52;
	private static String DUMMY_NAME_1 = "Matze";
	private static String DUMMY_NAME_2 = "Tilo";
	private static String DUMMY_NAME_3 = "Max";
	private List<IHighscoreEntry> scores = new LinkedList<IHighscoreEntry>();
 	
 	public DummyHighscore() {
		scores.add(new HighscoreEntry(DUMMY_NAME_1, DUMMY_SCORE_1));
		scores.add(new HighscoreEntry(DUMMY_NAME_2, DUMMY_SCORE_2));
		scores.add(new HighscoreEntry(DUMMY_NAME_3, DUMMY_SCORE_3));
 	}
 	
 	@Override
 	public void add(IHighscoreEntry entry) {
 		scores.add(entry);
 	}
 
 	@Override
 	public List<IHighscoreEntry> getAll() {
 		return scores;
 	}
 	
 	public String toString(){
 		StringBuilder builder = new StringBuilder("");
 		
 		for (IHighscoreEntry e : scores) {
 			builder.append(e.getPlayer());
 			builder.append(" : ");
 			builder.append(e.getScore());
 			builder.append("\n");
 		}		
 		return builder.toString();
 	}
 
 }
