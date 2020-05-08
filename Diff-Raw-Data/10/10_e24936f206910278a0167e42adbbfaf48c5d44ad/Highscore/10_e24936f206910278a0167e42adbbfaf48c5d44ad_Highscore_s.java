 package ultraextreme.view;
 
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  *
  */
 public class Highscore implements Comparable<Highscore> {
 
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
		return this.getScore()-another.getScore();
 	}
 	
 	public String toString()
 	{
 		return "Highscore[name:" + name + ", score:" + score + "]";
 		
 	}
 }
