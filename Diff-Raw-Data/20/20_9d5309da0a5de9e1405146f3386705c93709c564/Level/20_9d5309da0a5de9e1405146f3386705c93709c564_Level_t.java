 package com.jeremyP.diseasedefense;
 
 import java.sql.Time;
 import java.util.Date;
 import java.util.Timer;
 
 public class Level {
 
 	private int currentLevel;	/* Holds the current playing level */
 	
 	//All the level information
 	private long startTime;
 	private static final int LEVEL_TIME = 15;
 	private static final int MAX_LEVEL = 5;
 	private int cumScore;
 	private int levelScore;
 	
 	/**
 	 * Plain constructor that starts you off with score of 0 at LEVEL one
 	 */
 	public Level(){
 		currentLevel = 1;
 		cumScore = 0;
 		levelScore = 0;
 		startTime = new Date().getTime();
 	}
 	
 	/**
 	 * Calls addScore(1) - adds one point
 	 * @return if 1, your just went to the next level.
 	 */
 	public void addScore(){
 		this.addScore(1);
 	}
 	
 	/**
 	 * Adds more points to total score
 	 * @param points - the amount of points being added
 	 * @return if 1, your just went to the next level.
 	 */
 	public void addScore(int points){
 		levelScore += points;
 		cumScore += points;
 	}
 	
 	/**
 	 * Gives the levelScores
 	 * @return levelScore
 	 */
 	public int getLevelScore(){
 		return levelScore;
 	}
 	
 	/**
 	 * Gives the cumScores
 	 * @return cumScore
 	 */
 	public int getCumScore(){
 		return cumScore;
 	}
 	
 	/**
 	 * Gives the current level
 	 * @return current level
 	 */
 	public int whatLevel(){
 		return currentLevel;
 	}
 	
 	public boolean isLevelEnd(){
 		return isLevelEnd(true);
 	}
 	
 	/**
 	 * If the pause was paused, the timer in this class will still count.
 	 * This allows you to add time to start time, so technically, you won't "lose"
 	 * any time
 	 * @param pausedTime -  amount of time paused
 	 */
 	public void addPausedTime(long pausedTime){
 		startTime -= pausedTime;
 	}
 	
 	/**
 	 * Returns whether or not the level is complete
 	 * @param clearScore - true if you want the level score to be reset
 	 * NOTE: This is a method called resetScore() that resets the level score
 	 * @return true if level done
 	 */
 	public boolean isLevelEnd(boolean clearScore){
 		
		if(timeLeft() < 1){
 			currentLevel++;
 			if(clearScore)
 				levelScore = 0;
 			
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	/**
 	 * Return the amount of time that has passed in the current level
 	 * @return
 	 */
 	public long getTimePassed(){
 		
 		long nowTime = new Date().getTime();
 		System.out.println("nowTime: " + nowTime);
 		System.out.println("currentTime: " + startTime);
 
 		return (nowTime - startTime) / 1000;
 	}	
 	
 	/**
 	 * Reset/clear the level score and start the time over.
 	 */
 	public void resetScore(){
 		levelScore = 0;
 		startTime = new Date().getTime();
 	}
 	
 	/**
 	 * Returns whether the last level has been beaten or not
 	 * @return whether the game has been beaten
 	 */
 	public boolean isEnd(){
 		return currentLevel > MAX_LEVEL;
 	}
 	
 	public long timeLeft(){
 		return LEVEL_TIME - getTimePassed();
 	}
 }
