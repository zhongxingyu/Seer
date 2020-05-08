 package game.highscore;
 
 import java.io.Serializable;
 
 public class Score implements Serializable {
 	private int score;
	private String name;
 
 	public int getScore() {
 		return this.score;
 	}
 
	public String getName() {
		return this.name;
 	}
 
	public Score(String name, int score) {
 		this.score = score;
		this.name = name;
 	}
 }
