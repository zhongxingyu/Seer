 package utilities;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import utilities.helper_classes.GameInfo;
 
 public class GameData {
 	private ResultSet filteredGames; 
	private double firstTestGame;
 	private Mode myMode;
 	
	private final int RATING_THRESHOLD = 2100;
 	// For reference, there are 279K total games, and 5K where both players are rated > 2100. 
 	
 	private static String myQuery = "SELECT w_state, b_state, movelist FROM games WHERE id in " +
 			"(SELECT id FROM games WHERE " +
 			"white_rating >= %d AND black_rating >= %d ORDER BY RAND() ) LIMIT %d;"; 
 	
 	public static enum Mode {
 		TRAIN, TEST;
 	}
 	
 	public GameData(int numGames, double trainFraction){
		this.firstTestGame = trainFraction * numGames + 1;
 		setMode(Mode.TRAIN);
 		
 		filteredGames = MyDB.executeQuery(String.format(myQuery, RATING_THRESHOLD, RATING_THRESHOLD, numGames));
 		try {
 			filteredGames.next();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public boolean hasNextGame(){
 		try {
 			if (myMode == Mode.TRAIN && filteredGames.getRow() >= firstTestGame)
 				return false;
 			else if (myMode == Mode.TEST && filteredGames.isAfterLast())
 				return false;
 			else return true;
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public GameInfo getNextGame(){
 		try {
 			
 			GameInfo ret = new GameInfo(filteredGames.getString("w_state"),
 					filteredGames.getString("b_state"),
 					filteredGames.getString("movelist"));
 			
 			filteredGames.next();
 			return ret;
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	public void setMode(Mode myMode){
 		this.myMode = myMode; 
 	}
 }
