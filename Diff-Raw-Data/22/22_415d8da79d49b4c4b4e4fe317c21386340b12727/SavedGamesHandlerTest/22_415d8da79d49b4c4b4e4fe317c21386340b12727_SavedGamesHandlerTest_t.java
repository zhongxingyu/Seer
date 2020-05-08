 package ch.unibe.scg.team3.wordfinder.test;
 
 import java.io.IOException;
 
 import com.parse.Parse;
 
 import android.test.AndroidTestCase;
 
 import ch.unibe.scg.team3.game.SavedGame;
 import ch.unibe.scg.team3.localDatabase.DataHandler;
 import ch.unibe.scg.team3.localDatabase.SavedGamesHandler;
 import ch.unibe.scg.team3.wordfinder.R;
 
 public class SavedGamesHandlerTest extends AndroidTestCase implements
 		IDataHandlerTest {
 
 	protected String defaultBoard = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
 	protected DataHandler wordlistHandler;
 	protected SavedGamesHandler savedGamesHandler;
 
 	public void testGetTaggedGames() {
 		SavedGame saved = new SavedGame();
 		SavedGame saved2 = new SavedGame();
 		saved = initSavedGame(saved, "Test0");
 		saved2 = initSavedGame(saved2, "Test0");
 		savedGamesHandler.saveGame(saved);
 		savedGamesHandler.tagSavedGame("Test1", saved.getId());
 		savedGamesHandler.saveGame(saved2);
 		savedGamesHandler.tagSavedGame("Test2", saved2.getId());
 		assertEquals(savedGamesHandler.getTaggedGames().size(), 2);
 	}
 
 	public void testGetSavedGameAndSaveGame() {
 		SavedGame saved = new SavedGame();
 		SavedGame saved2 = new SavedGame();
 		SavedGame saved3 = new SavedGame();
 		saved = initSavedGame(saved, "Test1");
 		long temp = savedGamesHandler.saveGame(saved);
 		//saved2 = savedGamesHandler.getSavedGame(saved2.getId());
 		saved3 = savedGamesHandler.getSavedGame(temp);
 
 //		assertEquals(saved2.getName(), "Test1");
 //		assertEquals(saved2.getStringBoard(), defaultBoard);
 //		assertEquals(saved2.isPersonal(), true);
 //		assertEquals(saved2.getScore(), 914356341);
 //		assertEquals(saved2.getRemainingTime(), 1111);
 //		assertEquals(saved2.getNumberOfAttempts(), 1332434);
 //		assertEquals(saved2.getWordlistId(), 1);
 
 		assertEquals(saved3.getName(), "Test1");
 		assertEquals(saved3.getStringBoard(), defaultBoard);
 		assertEquals(saved3.isPersonal(), true);
 		assertEquals(saved3.getScore(), 914356341);
 		assertEquals(saved3.getRemainingTime(), 1111);
 		assertEquals(saved3.getNumberOfAttempts(), 1332434);
 		assertEquals(saved3.getWordlistId(), 1);
 	}
 
 	public void testGameInDatabase() {
 		SavedGame saved = new SavedGame();
 		saved = initSavedGame(saved, "Test1");
 		long temp = savedGamesHandler.saveGame(saved);
		assertTrue(savedGamesHandler.isGameInDatabase(temp));
		//assertTrue(savedGamesHandler.isGameInDatabase(temp));
 	}
 
 	public void testRemove() {
 		SavedGame saved = new SavedGame();
 		SavedGame saved2 = new SavedGame();
 		saved = initSavedGame(saved, "Test1");
 		saved2 = initSavedGame(saved2, "Test2");
 		long id = savedGamesHandler.saveGame(saved);
 		long temp = savedGamesHandler.saveGame(saved2);
		assertTrue(savedGamesHandler.isGameInDatabase(id));
		//assertTrue(savedGamesHandler.isGameInDatabase(temp));
 		assertTrue(savedGamesHandler.removeGame(saved));
 		assertTrue(savedGamesHandler.removeGame(temp));
		assertFalse(savedGamesHandler.isGameInDatabase(id));
 		assertFalse(savedGamesHandler.removeGame(saved));
		//assertFalse(savedGamesHandler.isGameInDatabase(temp));
 		assertFalse(savedGamesHandler.removeGame(temp));
 	}
 	
 
 	private SavedGame initSavedGame(SavedGame saved, String name) {
 		String board = defaultBoard;
 		saved.setName(name);
 		saved.setStringBoard(board);
 		saved.setDate("now");
 		saved.setPersonal(true);
 		saved.setScore(914356341);
 		saved.setRemainingTime(1111);
 		saved.setTimesPlayed(0);
 		saved.setAttempts(1332434);
 		saved.setWordlistId(1);
 		return saved;
 	}
 	
 	public void setUp() {
 		//wordlistHandler = new WordlistHandler(mContext.getApplicationContext());
 		savedGamesHandler = new SavedGamesHandler(
 				mContext.getApplicationContext());
 		try {
 			savedGamesHandler.copyDB();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		android.preference.PreferenceManager.setDefaultValues(
 				mContext.getApplicationContext(), R.xml.preferences, false);
 		Parse.initialize(mContext, "ORYli0X0QqbH3Oefe0wvI2TsYa4d4Kw7sYKZFYuK",
 						"FYUWqwq1E0VlFkVUXs6Fus1OZUN6CfqJo81EPbTJ");
 		
 		
 	
 	}
 
 
 	
 }
