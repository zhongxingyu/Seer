 package edu.hm.androidsweeper.persistence;
 
 import android.test.AndroidTestCase;
 import edu.hm.androidsweeper.difficulties.EasyDifficulty;
 import edu.hm.androidsweeper.difficulties.IDifficulty;
 import edu.hm.androidsweeper.gamelogic.Game;
 import edu.hm.androidsweeper.gamelogic.GameState;
 
 public class GamePersistenceManagerTest extends AndroidTestCase {
 
 	private Game game;
 	private IDifficulty difficulty;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		difficulty = new EasyDifficulty();
 		game = new Game(difficulty);
 		game.init();
 	}
 
	public void testSaveAndLoadGame(){
		saveGame();
		loadGame();
	}
 	
	private void saveGame() {
 		if(GamePersistenceManager.saveGameAvailable(getContext())) {
 			GamePersistenceManager.deleteSaveGame(getContext());
 		}
 		assertFalse(GamePersistenceManager.saveGameAvailable(getContext()));
 		
 		GamePersistenceManager.saveGame(getContext(), game);	
 		assertTrue(GamePersistenceManager.saveGameAvailable(getContext()));
 	}
 	
	private void loadGame() {
 		if(!GamePersistenceManager.saveGameAvailable(getContext())) {
 			GamePersistenceManager.saveGame(getContext(), game);
 		}
 		Game loadedGame = GamePersistenceManager.loadGame(getContext());
 		assertNotNull(loadedGame);
 		loadedGame.setState(GameState.STARTING);
 		
 		GameLoaderTest.compareGames(game, loadedGame);
 	}
 	
 }
