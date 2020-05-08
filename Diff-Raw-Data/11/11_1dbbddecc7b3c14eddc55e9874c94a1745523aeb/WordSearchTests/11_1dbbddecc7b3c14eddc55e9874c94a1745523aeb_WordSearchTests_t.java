 package Test;
 
 import static org.junit.Assert.*;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JCheckBox;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import WordSearch.Board;
 import WordSearch.Cell;
 import WordSearch.Game;
 import WordSearch.SplashScreen;
 import WordSearch.WordBank;
 
 
 /**
  * @author Naomi Plasterer, Brandon Bosso, Jason Steinberg, Austin Diviness
  */
 
 public class WordSearchTests {
 	
 	private static Game testGame;
 	
 	/**
 	 * Creates a test word search for testing.
 	 */
 	@BeforeClass
 	public static void setUp() {
 		testGame = new Game();
 		testGame.loadConfigFile("Cats.txt");
 	}
 
 	/** 
 	 * Loads words and then tests if words are loaded correctly in the game.
 	 * Will fail if file loaded does not load certain words in the file.
 	 */
 	@Test
 	public void testLoadingWords() {
 		ArrayList<String> words = new ArrayList<String>();
 		words = testGame.getListOfLinesFromFile("SecretFiles", "Cats.txt");
 		assertEquals(15, words.size());
 		assertTrue(words.contains("Calico"));
 		assertTrue(words.contains("Siamese"));
 		assertTrue(words.contains("Bengal"));
 		assertTrue(words.contains("Toyger"));
 		
 		words = testGame.getListOfLinesFromFile("SecretFiles", "Automobiles.txt");
 		assertEquals(11, words.size());
 		assertTrue(words.contains("Dodge"));
 		assertTrue(words.contains("Mitsubishi"));
 		assertTrue(words.contains("Hummer"));
 		assertTrue(words.contains("Jeep"));
 	}
 	
 	/**
 	 * Loads file names and then tests if file names are loaded correctly.
 	 * Will fail if certain file is not loaded.
 	 */
 	@Test
 	public void testLoadFileNames() {
 		ArrayList<String> files = new ArrayList<String>();
 		files = testGame.getListOfFiles("SecretFiles", ".txt", true);
 		assertEquals(2, files.size());
 		assertTrue(files.contains("Cats"));
 		assertTrue(files.contains("Automobiles"));
 	}
 
 	/**
 	 * Creates a SplashScreen and then tests to make sure certain categories are present.
 	 */
 	@Test
 	public void testSplashScreen() {
 		ArrayList<String> lists = new ArrayList<String>();
 		lists.add("Automobiles");
 		lists.add("Cats");
 		SplashScreen splashy = new SplashScreen(lists, testGame);
 		splashy.setCategoriesText("Cats");
 		splashy.click();
 		testGame.setCategory(splashy.getCategory());
 		Assert.assertEquals("Cats", testGame.getCategory());
 		
 		splashy.setCategoriesText("Automobiles");
 		splashy.click();
 		testGame.setCategory(splashy.getCategory());
 		Assert.assertEquals("Automobiles", testGame.getCategory());
 	}
 	
 	/**
 	 * Test if two selected points are valid.
 	 * Will fail if selected points are not in the same column, row, or diagonal.
 	 */
 	@Test
 	public void testValidSelection() {
 		//test same column
 		assertTrue(testGame.checkValidSelection(new Point(1,1), new Point(1,15)));
 		
 		//test same row
 		assertTrue(testGame.checkValidSelection(new Point(1,1), new Point(15,1)));
 		
 		//test diagonal
 		assertTrue(testGame.checkValidSelection(new Point(1,1), new Point(5,5)));
 		assertTrue(testGame.checkValidSelection(new Point(5,1), new Point(1,5)));
 		
 		//test invalid selections
 		assertFalse(testGame.checkValidSelection(new Point(5,5), new Point(1,12)));
 		assertFalse(testGame.checkValidSelection(new Point(7,8), new Point(3,2)));
 		assertFalse(testGame.checkValidSelection(new Point(5,5), new Point(6,9)));
 	}
 	
 	/**
 	 * Tests to see if word selected is correct
 	 * Will fail if a word selected is not a valid word in file.
 	 */
 	@Test
 	public void testValidWord() {
		testGame.setWordBank(new WordBank(testGame.getListOfLinesFromFile("SecretFiles", "Cats.txt")));
 		assertTrue(testGame.checkValidWord("Calico"));
 		assertTrue(testGame.checkValidWord("Bengal"));
 		assertTrue(testGame.checkValidWord("Highlander"));
 		
 		assertFalse(testGame.checkValidWord("Dodge"));
 		assertFalse(testGame.checkValidWord("Husky"));
 		assertFalse(testGame.checkValidWord("Mitsubishi"));
 	}
 		
 	/**
 	 * Test that correct characters are in the correct cells on the board.
 	 * Will fail if the characters are located in incorrect cells.
 	 */
 	@Test
 	public void testCellCharacters() {
 		Board testBoard = new Board();
 		String inserted = "Kohlrabi";
 		Map<Character, Boolean> found = new HashMap<Character, Boolean>();
 		testBoard.insertWord(inserted);
 		
 		for(int i = 0; i < inserted.length(); i++) {
 			found.put(inserted.charAt(i), false);
 		}
 		
 		for(int i = 0; i < testBoard.getRows(); i++) {
 			for(int j = 0; j < testBoard.getColumns(); j++) {
 				Cell c = testBoard.getCellAt(i, j);
 				if(c.getLetter() != ' ') {
 					found.put(c.getLetter(), true);
 				}
 			}
 		}
 		
 		for(Boolean b : found.values()) {
 			assertTrue(b);
 		}
 	}
 	
 	/**
 	 * Tests if game can finish when there are zero words left to find.
 	 * Will fail if zero words left does not finish game.
 	 */
 	@Test
 	public void testGameEnds() {
 		testGame.setWordsLeft(0);
 		Assert.assertTrue(testGame.checkIfDone());
 	}
 	
 	/**
 	 * Tests that correct words are in word bank and can be selected.
 	 * Will fail if selected word is not in word bank.
 	 */
 	@Test
 	public void testWordBank() {
 		WordBank bank = testGame.getWordBank();
 		bank.checkBox("Calico");
 		bank.checkBox("Siamese");
 		bank.checkBox("Bengal");
 		Assert.assertEquals(testGame.getWords().size(), bank.getWordBank().size());
 		
 		
 		assertTrue(bank.getWordBank().get("Calico").isSelected());
 		assertTrue(bank.getWordBank().get("Siamese").isSelected());
 		assertTrue(bank.getWordBank().get("Bengal").isSelected());
 		assertFalse(bank.getWordBank().get("Ocelot").isSelected());
 		assertFalse(bank.getWordBank().get("Persian").isSelected());
 	}
 	
 	/**
 	 * Tests if word can be highlighted between two points.
 	 * Will fail if the cells between the two points are not highlighted.
 	 */
 	@Test
 	public void testHighlightingWords() {
 		Point start = new Point(0,0);
 		Point end = new Point(3,3);
 		testGame.highlightWords(start, end);
 		for(int i = 0; i < 4; i++) {
 			Cell c = testGame.getBoard().getCellAt(i, i);
 			assertTrue(c.isHighlighted());
 		}
 	}
 }
