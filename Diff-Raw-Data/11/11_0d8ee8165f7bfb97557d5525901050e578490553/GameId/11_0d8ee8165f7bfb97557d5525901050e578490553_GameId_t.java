 package it.chalmers.tendu.gamemodel;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public enum GameId {
 
 
	NUMBER_GAME(/*Difficulty.ONE, Difficulty.TWO, Difficulty.THREE, Difficulty.FOUR, Difficulty.FIVE*/), SHAPE_GAME(Difficulty.ONE, Difficulty.TWO, Difficulty.THREE, Difficulty.FOUR, Difficulty.FIVE);
 
 
 
 	/**
 	 * Levels that
 	 */
 	private List<Difficulty> acceptedDifficulties = new ArrayList<Difficulty>();
 
 	private GameId(Difficulty... difficulty) {
 		for (Difficulty d : difficulty) {
 			acceptedDifficulties.add(d);
 		}
 
 	}
 
 	/**
 	 * @param level
 	 * @return a list of GameIds that accepts the given level value
 	 */
 	public static List<GameId> getGameIdsFor(Difficulty difficulty) {
 		List<GameId> validGames = new ArrayList<GameId>();
 		for (GameId g : values()) {
 			if (g.acceptedDifficulties.contains(difficulty))
 				validGames.add(g);
 		}
 		return validGames;
 	}
 
 }
