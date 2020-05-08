 package bu.edu.cs673.edukid.db;
 
 import java.util.List;
 
 import android.test.AndroidTestCase;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.model.Word;
 import bu.edu.cs673.edukid.db.model.category.CategoryType;
 
 public class DatabaseTest extends AndroidTestCase {
 
 	private Database database;
 
 	public void setUp() throws Exception {
 		database = new Database(getContext());
 		database.createNewDatabase();
 
 		// Add custom word to letter K
 		Word kWord1 = new Word();
 		kWord1.setWord("K_word_1");
 		database.addWord(11, kWord1);
 	}
 
 	public void testGetCategories() {
 		CategoryType[] categories = database.getCategories();
 		assertTrue(categories.length == 4);
 		assertTrue(categories[0].getCategoryName().equals("Alphabet"));
 		assertTrue(categories[1].getCategoryName().equals("Numbers"));
 		assertTrue(categories[2].getCategoryName().equals("Shapes"));
 		assertTrue(categories[3].getCategoryName().equals("Colors"));
 	}
 
 	public void testGetLetters() {
 		// TODO
 	}
 
 	public void testEditLetter() {
 		// TODO
 	}
 
 	public void testGetWords() {
 		List<Word> kWords = database.getWords(DatabaseHelper
 				.generateWordsSelection(11));
 		System.out.println("get size: " + kWords.size());
 		assertTrue(kWords.size() == 1);
 	}
 
 	public void testAddWords() {
 		List<Word> kWords = database.getWords(DatabaseHelper
 				.generateWordsSelection(11));
 		assertTrue(kWords.size() == 1);
 
 		// Check K_word_1
 		Word word = kWords.get(0);
 		assertTrue(word.getWord().equals("K_word_1"));
 		assertTrue(word.isChecked() == true);
 
 		// Add custom word to letter K
 		Word kWord2 = new Word();
 		kWord2.setWord("K_word_2");
 		database.addWord(11, kWord2);
 
 		kWords = database.getWords(DatabaseHelper.generateWordsSelection(11));
 		assertTrue(kWords.size() == 2);
 
 		// Check K_word_2
 		word = kWords.get(1);
 		assertTrue(word.getWord().equals("K_word_2"));
 		assertTrue(word.isChecked() == true);
 	}
 
 	public void testUpdateWord() {
 		// Check update on K_word_1 to word_1
		Word word = kwords.get(0);
 		word.setWord("word_1");
 		database.updateWord(11, 0, word);
 		assertTrue(word.getWord().equals("word_1"));
		
 	}
 
 	public void testDeleteWord() {
		// check delete on 11,1 K_word_2
		Word word = kword.get(1);
		database.deleteWord(11, 1);
 		List<Word> kWords = database.getWords(DatabaseHelper
 				.generateWordsSelection(11));
 		assertTrue(kWords.size() == 1);
 	}
 
 	public void testGetDefaultWordMapping() {
 		// TODO
 	}
 
 	public void testAddDefaultWordMapping() {
 		// TODO
 	}
 
 	public void testUpdateDefaultWordMapping() {
 		// TODO
 	}
 
 	public void testGetUserAccounts() {
 		// TODO
 	}
 
 	public void testAddUserAccount() {
 		// TODO
 	}
 
 	public void testEditUserAccount() {
 		// TODO
 	}
 
 	public void testGetNums() {
 		// TODO
 	}
 
 	public void testAddNums() {
 		// TODO
 	}
 
 	public void testGetNumbers() {
 		// TODO
 	}
 
 	public void testAddNumbers() {
 		// TODO
 	}
 
 	public void testGetNumTypes() {
 		// TODO
 	}
 
 	public void testAddNumType() {
 		// TODO
 	}
 
 	public void testGetColors() {
 		// TODO
 	}
 
 	public void testAddColor() {
 		// TODO
 	}
 
 	public void testGetShapes() {
 		// TODO
 	}
 
 	public void testAddShape() {
 		// TODO
 	}
 
 	public void testGetTimer() {
 		// TODO
 	}
 
 	public void testAddTimer() {
 		// TODO
 	}
 
 	public void updateTimer() {
 		// TODO
 	}
 }
