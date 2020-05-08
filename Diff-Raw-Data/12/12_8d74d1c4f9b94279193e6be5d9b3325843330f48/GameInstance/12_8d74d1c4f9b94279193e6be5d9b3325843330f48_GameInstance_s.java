 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 import play.db.jpa.Model;
 
 @Entity
 public class GameInstance extends Model {
 
 	@OneToMany(mappedBy="game", cascade=CascadeType.ALL)
 	public List<User> players;
 	
 	@OneToMany(mappedBy="game", cascade=CascadeType.ALL)
 	public List<Problem> problems;
 	
 	public int round;
 	public int problemsAtOnce;
 	public int maxPlayers;
 	public int pointsToWin;
 	public int totalRounds;
 	
 	public boolean started;
 	public boolean ended;
 	
 	public boolean inRound;
 	
 	public GameInstance() {
 		this.round = 0;
 		this.started = false;
 		this.ended = false;
 		this.inRound = false;
 		this.problemsAtOnce = 1;
 		this.maxPlayers = 2;
 		this.pointsToWin = 5;
 		this.totalRounds = 1;
 		this.players = new ArrayList<User>();
 		this.problems = new ArrayList<Problem>();
 	}
 	
 	public void start() {
 		started = true;
 		newRound();
 	}
 	public void newRound() {
 		inRound = false;
 		for(User u: players) {
 			u.score = 0;
 			u.ready = false;
 		}
 		round++;
 		for(Problem p: problems) {
			p.delete(); // TODO not sure if i can actually do this w/o concurrency issues
 		}
 		problems.clear();
		save();
 		if(round>totalRounds) {
 			end();
 		}
 	}
 	public void end() {
 		ended = true;
 	}
 	public void startRound() {
 		inRound = true;
 		for(int i=0; i<problemsAtOnce; i++) 
 			newProblem(i);
 		save();
 	}
 	public Problem newProblem(int position) {
 		Problem pr = new Problem(this, position).save();
 		return pr;
 	}
 }
