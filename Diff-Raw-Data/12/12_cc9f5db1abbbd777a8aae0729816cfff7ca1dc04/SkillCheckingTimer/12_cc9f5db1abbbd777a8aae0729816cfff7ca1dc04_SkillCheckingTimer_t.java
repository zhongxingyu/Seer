 package Model.Timers;
 
 import Model.Obstacles.Obstacle;
 import Model.*;
 
 public class SkillCheckingTimer {
 
 	private int ESColInterval;
 	private long ESColStartTime = 0;
 	private long ESColElapsedTime = 0;
	private String playerName = "empty";
 	private Obstacle obstacle = null;
 	
 	
 	public SkillCheckingTimer(int interval, String playerName, Skill skill){
 		ESColInterval = interval;
 		this.playerName = playerName;
 		
 		resetESColTimer();
 	}
 	
 	public SkillCheckingTimer(int interval, Obstacle obstacle, Skill skill){
 		ESColInterval = interval;
 		this.obstacle = obstacle;
 		
 		resetESColTimer();
 	}
 	
 	// Getters
 	public String getPlayerName(){
 		return playerName;
 	}
 	public Obstacle getObstacle(){
 		return obstacle;
 	}
 	public int getESColInterval(){
 		return ESColInterval;
 	}
 	
 	// Setters
 	public void resetESColTimer(){
 		ESColStartTime = System.currentTimeMillis();
 		ESColElapsedTime = 0;
 	}
 	public long checkESColTimer(){
 		ESColElapsedTime = System.currentTimeMillis() - ESColStartTime;
 		if(ESColElapsedTime > ESColInterval){
 			ESColElapsedTime = ESColInterval;
 		}
 		return ESColElapsedTime;
 	}
 }
