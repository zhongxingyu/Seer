import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
public class LeagueScoreEngineTest {
 	public static final int PLAYER_BASIC_SCORE = 1000;
 	private LeagueScoreEngine scoreEngine;
 	private Game game;
 	
 	@Before
 	public void setUp() throws Exception {
 		scoreEngine = new LeagueScoreEngine();
 		game = createBasicGame();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testEvaluateResultReturnNullWhenNoResultIsProviden() { 
 		Game game = scoreEngine.evaluateGame(null);
 		
 		assertEquals(null, game);
 	}
 	
 	@Test
 	public void testEvaluateResultRetunAMatchScore() {
 		Game theGame = scoreEngine.evaluateGame(game);
 		
 		assertTrue(theGame instanceof Game);
 	}
 	
 	@Test
 	public void testEvaluateResultReturnSamePlayersIntoTheScore() { 
 		Game theGame = scoreEngine.evaluateGame(game);
 		
 		assertTrue(theGame.local instanceof Player);
 		assertTrue(theGame.visitor instanceof Player);
 	}
 	
 	@Test
 	public void testEvaluateResultBasicPointUpdate() {
 		game.golsLocal = 2;
 		game.golsVisitor = 0;
 		
 		scoreEngine.evaluateGame(game);
 		
 		assertTrue(game.local.score > PLAYER_BASIC_SCORE);
 		assertTrue(game.visitor.score < PLAYER_BASIC_SCORE);
 	}
 	
 	private Game createBasicGame() {
 		game = new Game();
 		game.local = new Player(PLAYER_BASIC_SCORE);
 		game.visitor = new Player(PLAYER_BASIC_SCORE);
 		game.golsLocal = 0;
 		game.golsVisitor = 2;
 				
 		return game;
 	}
 }
