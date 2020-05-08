 //BEGIN FILE StatisticsImp.java
 package model;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 //BEGIN CLASS StatisticsImp
 public class StatisticsImp implements Statistics, Cloneable{
 	
 	private Map<String, List<Integer>> sessionStatistics;
 	
 	/**
 	 * Generates a new Statistics class gives session data for a game going on
 	 */
 	public StatisticsImp(){
 		//setSessionStatistics(sessionStatistics);
		this.sessionStatistics = new HashMap<String, List<Integer>>();
 	}
 
	public StatisticsImp(Map<String, List<Integer>> stats){
		this.sessionStatistics = stats;
	}
 	@Override
 	/**
 	 * Returns an immutable copy of this Statistics for use by views to update their model.
 	 * @return An immutable copy of this Statistics
 	 */
 	public Statistics getState() {
 		// TODO Auto-generated method stub
 		return this.clone();
 	}
 
 	@Override
 	/**
 	 * Returns the number of wins for a given player.
 	 * @param playerName The name of the player to query
 	 * @return The number of wins
 	 */
 	public int getWins(String playerName) {
 		// TODO Auto-generated method stub
 		return sessionStatistics.get(playerName).get(0);
 	}
 
 	@Override
 	/**
 	 * Returns the number of losses for a given player.
 	 * @param playerName The name of the player to query.
 	 * @return The number of losses.
 	 */
 	public int getLosses(String playerName) {
 		// TODO Auto-generated method stub
 		return sessionStatistics.get(playerName).get(1);
 	}
 
 	@Override
 	/**
 	 * Returns the number of ties for a given player.
 	 * @param playerName The name of the player to query.
 	 * @return The number of ties.
 	 */
 	public int getTies(String playerName) {
 		return sessionStatistics.get(playerName).get(2);
 	}
 
 	@Override
 	/**
 	 * Returns the total number of games played by a given player
 	 * @param playerName The name of the player to query.
 	 * @return The total number of games.
 	 */
 	public int getGameCount(String playerName) {
 		// TODO Auto-generated method stub
 		
 		return sessionStatistics.get(playerName).get(3);
 	}
 
 	@Override
 	/**
 	 * Adds a win for the given player's record.
 	 * @param playerName The player to assign a win to.
 	 */
 	public void addWin(String playerName) {
 		// TODO Auto-generated method stub
 		List<Integer> stats = sessionStatistics.get(playerName);
 		stats.add(0, stats.get(0) + 1);
 		sessionStatistics.put(playerName,  stats);
 	}
 
 	@Override
 	/**
 	 * Adds a loss for the given player's record.
 	 * @param playerName The player to assign a loss to.
 	 */
 	public void addLoss(String playerName) {
 		// TODO Auto-generated method stub
 		List<Integer> stats = this.sessionStatistics.get(playerName);
 		stats.add(1, stats.get(1) + 1);
 		this.sessionStatistics.put(playerName,  stats);
 	}
 
 	@Override
 	/**
 	 * Adds a tie for the given player's record.
 	 * @param playerName The player to assign a tie to.
 	 */
 	public void addTie(String playerName) {
 		// TODO Auto-generated method stub
 		
 		List<Integer> stats = this.sessionStatistics.get(playerName);
 		stats.add(2, stats.get(2) + 1);
 		
 		this.sessionStatistics.put(playerName, stats);
 	}
 
 	public Map<String, List<Integer>> getSessionStatistics() {
 		return this.sessionStatistics;
 	}
 
 	public void setSessionStatistics(Map<String, List<Integer>> sessionStatistics) {
 		this.sessionStatistics = sessionStatistics;
 	}
 	
 	public Statistics clone(){
 		
		Statistics c = new StatisticsImp(sessionStatistics);
		
		return c;
 		
 	}
 	
 	public void addPlayer(String playerName){
 		List<Integer> stats = new ArrayList<Integer>();
 		for(int i = 0; i < stats.size(); i++){
 			stats.add(i, (Integer) 0);
 		}
 		sessionStatistics.put(playerName, stats);
 	}
 	
 	
 }
 //END CLASS StatisticsImp
