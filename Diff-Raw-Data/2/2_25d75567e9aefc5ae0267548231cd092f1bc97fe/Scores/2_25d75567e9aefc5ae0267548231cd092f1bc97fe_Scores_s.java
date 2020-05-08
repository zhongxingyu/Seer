 package edu.upenn.cis350.mosstalkwords;
 
 import java.io.Serializable;
 
 import android.content.Context;
 import android.database.Cursor;
 
 public class Scores{
 
 	private int total_score;
 	private int [] high_scores;
 	private int highest_streak;
 
 	public ScoresDbAdapter scoresDb;
 	/**
 	 * Initialize a new Scores object for keeping track of a user's scores.
 	 */
 	public Scores(Context ctx) {
 		total_score = 0;
 		high_scores = new int[6];
 		scoresDb = new ScoresDbAdapter(ctx);
 		scoresDb.open();
 	}
 
 
 	/**
 	 * Returns the total score (accumulated over all of the 
 	 * games the user has played)
 	 * @return the total score
 	 */
 	public int getTotalScore() {
 		int totalScore = scoresDb.getScore("totalscore");
 		if(totalScore != -1)
 		{
 			total_score = totalScore;
 			return total_score;
 		}
 		else
 		{
 			scoresDb.addScore("totalscore", total_score);
 			return 0;
 		}
 	}
 
 
 	/**
 	 * Set the total score
 	 * @param val the new value 
 	 * @return true if success, false if val is < 0
 	 */
 	public boolean setTotalScore(int val) {
 		if(val > 0) {
 			total_score = val;
 			
 			int result = scoresDb.getScore("totalscore");
 			if(result != -1)
 			{
 				scoresDb.updateScore("totalscore", total_score);
 				return true;
 			}
 			else
 			{
 				scoresDb.addScore("totalscore", total_score);
 				return true;
 			}
 		}
 		else {
 			return false;
 		}
 	}
 	
 
 
 	/**
 	 * Increment the total score by the amount val
 	 * @param val  the amount to increment the total score by
 	 * @return true if success, false if val < 0
 	 */
 	public boolean incTotalScore(int val) {
 		if(val > 0) {
 			total_score += val;
 			setTotalScore(total_score);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 
 	/**
 	 * Given a stimulus set, returns the high score for that set
 	 * @param set the name of the stimulus set
 	 * @return the high score, or -1 if set was not a valid name
 	 */
 	public int getHighScore(String set) {
 		
 		int setHighScore = scoresDb.getScore(set+"score");
 		if(setHighScore != -1)
 		{
 			return setHighScore;
 		}
 		else
 		{
 			scoresDb.addScore(set+"score", 0);
 			return 0;
 		}
 	}
 
 
 	/**
 	 * Sets the high score of a stimulus set.
 	 * @param set  The stimulus set to set
 	 * @param val  The value of the new high score
 	 * @return true if success, false if val is negative or if 
 	 * the set does not exist
 	 */
 	public boolean setHighScore(String set, int val) {
 
 		int setHighScore = scoresDb.getScore(set+"score");
 		if(setHighScore != -1)
 		{
 			scoresDb.updateScore(set+"score", val);
 		}
 		else
 		{
 			scoresDb.addScore(set+"score", val);
 		}
 		
 		return true;
 	}
 
 	public void setNumCompleted(String set, int val) {
 		
 		int currentNumCompleted = scoresDb.getScore(set+"completed");
 		if(currentNumCompleted != -1)
 		{
 			scoresDb.updateScore(set+"completed", val);
 		}
 		else
 		{
 			scoresDb.addScore(set+"completed", val);
 		}
 		
 	}
 	
 	public int getNumCompleted(String set) {
 		
 		int currentNumCompleted = scoresDb.getScore(set+"completed");
 		if(currentNumCompleted != -1)
 		{
 			return currentNumCompleted;
 		}
 		else
 		{
 			scoresDb.addScore(set+"completed", 0);
 			return 0;
 		}
 	}
 
 
 	/**
 	 * Returns the highest streak (over all of the 
 	 * games the user has played)
 	 * @return the highest streak of correct answers
 	 */
 	public int getHighestStreak() {
 		
 		int streak = scoresDb.getScore("higheststreak");
 		if(streak != -1)
 		{
 			highest_streak = streak;
 			return highest_streak;
 		}
 		else
 		{
 			scoresDb.addScore("higheststreak", 0);
 			return 0;
 		}
 		
 	}
 
 
 	/**
 	 * Set the highest streak of correct answers
 	 * @param val the new value 
 	 * @return true if success, false if val is < 0
 	 */
 	public boolean setHighestStreak(int val) {
 		
 		if(val > 0) {
 			highest_streak = val;
			int score = scoresDb.getScore("higestscore");
 			if(score != -1)
 			{
 				scoresDb.updateScore("higheststreak", highest_streak);
 				return true;
 			}
 			else
 			{
 				scoresDb.addScore("higheststreak", highest_streak);
 				return true;
 			}
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public void closeDb() {
 		scoresDb.close();
 	}
 
 
 }
 
