 package chalmers.TDA367.B17.gamemodes;
 
 import java.util.*;
 
 import chalmers.TDA367.B17.model.Player;
 
 
 
 public abstract class ScoreBasedGame extends GameConditions{
 private static int DEFAULT_SCORE_LIMIT = 8;	
 protected int scoreLimit;
 
 	public ScoreBasedGame(){
 		this.scoreLimit = DEFAULT_SCORE_LIMIT;
 	}
 	
 	
 	@Override
 	public void endRound(){
 		if(!roundEnded){
 			super.endRound();
 			//Winning by score
 			for(Player p: players){
 				if(p.getScore() >= scoreLimit){
 					setGameOver(true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Increments every player's score.
 	 */
 	public void incrementAllPlayerScores(){
 		for(Player p : getPlayerList()){
 			if(p.isActive())
 				incrementPlayerScore(p);
 		}
 	}
 	
 	public void incrementPlayerScore(Player p){
 		p.setScore(p.getScore()+1);
 	}
 
 	@Override
 	public void gameOver(){
 		super.gameOver();
 		for(Player p : players){
 			System.out.println(p.getName() + "'s score: " + p.getScore());
 		}
 	}
 	
 	@Override
 	public List<Player> getWinningPlayers(){
 		return getHighestScoringPlayers();
 	}
 	
 	/**
 	 * Return the score of a player at an index.
 	 * @param index The index of the player.
 	 * @return The score of the player at index.
 	 */
 	public int getPlayerScoreAtIndex(int index){
 		return getPlayerList().get(index).getScore();
 	}
 
 	/**
 	 * Set the score-limit.
 	 * @param scoreLimit The new score-limit
 	 */
 	public void setScoreLimit(int scoreLimit) {
 		this.scoreLimit = scoreLimit;
 	}
 	
 	/**
 	 * Returns the player with the highest score.
 	 * return Player with the highest score
 	 */
 	public List<Player> getHighestScoringPlayers(){
		List<Player> winningPlayers = new ArrayList<Player>();
		if(getPlayerList().size() > 0){
			winningPlayers.add(getPlayerList().get(0));
			for(int i = 1; i < players.size(); i++){
				if(getPlayerScoreAtIndex(i) > winningPlayers.get(0).getScore()){
					winningPlayers.clear();
					winningPlayers.add(players.get(i));
				}else if(getPlayerScoreAtIndex(i) == winningPlayers.get(0).getScore()){
					winningPlayers.add(players.get(i));
				}
 			}
 		}
 
		return winningPlayers;
 	}
 
 	
 	/**
 	 * Return the score-limit.
 	 * @return The score-limit
 	 */
 	public int getScoreLimit() {
 		return scoreLimit;
 	}
 }
