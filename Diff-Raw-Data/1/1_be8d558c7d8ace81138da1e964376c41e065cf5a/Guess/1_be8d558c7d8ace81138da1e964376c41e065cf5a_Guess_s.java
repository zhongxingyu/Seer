 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Guess extends Model {
 	
 	public double answer;
 	
 	@ManyToOne
 	public User player;
 	
 	@ManyToOne
 	public Problem problemAnswered;
 	
 	@ManyToOne
 	public GameInstance game;
 	
 	public Guess(User player, double answer, Problem problemAnswered) {
 		this.player = player;
 		this.answer = answer;
 		this.problemAnswered = problemAnswered;
 	}
 }
