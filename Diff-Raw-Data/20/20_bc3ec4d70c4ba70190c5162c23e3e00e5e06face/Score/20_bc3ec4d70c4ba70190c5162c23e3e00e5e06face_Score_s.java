 package game.highscore;
 
 import java.io.Serializable;
 
 public class Score implements Serializable {
 	private int score;
	private String naam;
 
 	public int getScore() {
 		return this.score;
 	}
 
	public String getNaam() {
		return this.naam;
 	}
 
	public Score(String naam, int score) {
 		this.score = score;
		this.naam = naam;
 	}
 }
