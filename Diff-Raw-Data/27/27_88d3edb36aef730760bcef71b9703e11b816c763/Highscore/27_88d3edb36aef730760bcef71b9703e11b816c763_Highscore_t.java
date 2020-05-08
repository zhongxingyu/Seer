 package ultraextreme.view;
 
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  *
  */
 public class Highscore implements Comparable<Highscore> {
 
	public static final Highscore EMPTY_HIGHSCORE = new Highscore("Empty", 0);
	
 	private final String name;
 	private final int score;
 	
 	public Highscore(String name, int score)
 	{
 		this.name = name;
 		this.score = score;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public int getScore()
 	{
 		return score;
 	}
 
 	@Override
 	public int compareTo(Highscore another) {
 		return another.getScore()-this.getScore();
 	}
 	
 	public String toString()
 	{
 		return "Highscore[name:" + name + ", score:" + score + "]";
 		
 	}
 }
