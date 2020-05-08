 package utilities;
 
 import static org.junit.Assert.*;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 
 import org.junit.Test;
 
 import utilities.helper_classes.ArimaaState;
 import utilities.helper_classes.GameInfo;
 import arimaa3.ArimaaMove;
 import arimaa3.GameState;
 import feature_extractor.FeatureExtractor;
 
 public class UtilitiesTest {
 
 	
 	@Test
 	public void testDB() {
 		// Test whether the result set is not empty.
 		ResultSet rs = MyDB.executeQuery("SELECT * FROM games limit 5"); 
 		try {
 			assertTrue(rs.next());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testGameData(){
 		GameData gd = new GameData(100, 0.7);
 		HashSet<GameInfo> trainSet = new HashSet<GameInfo>();
 		HashSet<GameInfo> testSet = new HashSet<GameInfo>();
 		while (gd.hasNextGame()){
 			GameInfo trainInfo = gd.getNextGame();
 			System.out.println(trainInfo.getBlackStartState());
 			trainSet.add(trainInfo);	
 		}
 		
 		gd.setMode(GameData.Mode.TEST);
 		while (gd.hasNextGame()){
 			testSet.add(gd.getNextGame());
 		}
 		
 		assertTrue(trainSet.size() == 70);
 		assertTrue(testSet.size() == 30);
 		trainSet.retainAll(testSet);
 		assertTrue(trainSet.size() == 0);
 	}
 
 	@Test
 	public void testGameParser() {
 		String whiteSS = "1w Re2", blackSS = "1b rd7";
 		String moveW1 = "2w Re2n Re3n Re4n", moveB1 = "\n2b rd7s rd6s rd5s";
 		String moveW2 = "\n3w Re5e";
		String emptyMove = "\n3b";
 		
		GameInfo gi = new GameInfo(whiteSS, blackSS, moveW1 + moveB1 + moveW2 + emptyMove);
 		GameParser gp = new GameParser(gi);
 		
 		assertTrue(gp.hasNextGameState());
 		
 		int loopCount = 0;
 		GameState prev = null;
		ArimaaState as = null; //init to silence compiler xD--this is initialized in the loop
 		while (gp.hasNextGameState()) {
			as = gp.getNextGameState();
 			assertTrue(as.getPrev() == prev);
 			
			System.out.println(as.getCurr().toBoardString());
 			
 			prev = as.getCurr();
 			loopCount++;
 		}
 		
		if (as != null)
			assertTrue(as.getNextMove().equals(new ArimaaMove(moveW2.substring(4)))); //test to ensure that this is the last move
		
 		assertTrue(gp.getNextGameState() == null);
 		assertTrue(loopCount == 3); //3 == numMoves
 	}
 
 }
