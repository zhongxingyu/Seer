 package highscore;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import interfaces.IHighscore;
 import interfaces.IHighscoreEntry;
 
 public class DummyHighscore implements IHighscore {

	List<IHighscoreEntry> scores = new LinkedList<IHighscoreEntry>();
 	
 	public DummyHighscore() {
		scores.add(new HighscoreEntry("Matze", 40));
		scores.add(new HighscoreEntry("Max", 50));
		scores.add(new HighscoreEntry("Tilo", 52));
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
