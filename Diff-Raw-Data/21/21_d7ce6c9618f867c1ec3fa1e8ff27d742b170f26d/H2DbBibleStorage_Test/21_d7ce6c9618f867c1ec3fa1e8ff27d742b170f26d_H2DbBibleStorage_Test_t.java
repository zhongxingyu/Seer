 package com.github.mnicky.bible4j.storage;
 
 import hirondelle.date4j.DateTime;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.github.mnicky.bible4j.data.BibleBook;
 import com.github.mnicky.bible4j.data.BibleVersion;
 import com.github.mnicky.bible4j.data.Bookmark;
 import com.github.mnicky.bible4j.data.DailyReading;
 import com.github.mnicky.bible4j.data.DictTerm;
 import com.github.mnicky.bible4j.data.Note;
 import com.github.mnicky.bible4j.data.Position;
 import com.github.mnicky.bible4j.data.Verse;
 import com.github.mnicky.bible4j.data.Note.NoteType;
 
 import static com.github.mnicky.bible4j.storage.H2DbNaming.*;
 
 public final class H2DbBibleStorage_Test {
 
     private Connection conn;
     private H2DbBibleStorage bible;
 
     @BeforeMethod
     public void setUpTest() {
 	try {
 	    conn = DriverManager.getConnection("jdbc:h2:mem:test", "test", "");
 
 	    // for debugging purposes:
 	    // conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:test", "test", "");
 
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	bible = new H2DbBibleStorage(conn);
 
     }
 
     @AfterMethod
     public void tearDownTest() {
 	try {
 	    conn.close();
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
     }
 
     @Test
     public void shouldCreateBibleStorage() {
 
 	// expected numbers of column updates
 	// see Statement.executeBatch() javadoc for more info
 	int[] exp = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 
 	int[] columns = null;
 
 	try {
 	    columns = bible.createStorage();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertTrue(Arrays.equals(columns, exp));
 
     }
 
     @Test
     public void shouldCloseBibleStorage() {
 	try {
 	    bible.close();
 	    Assert.assertTrue(conn.isClosed());
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
     }
 
     @Test
     public void shouldInsertBibleBook() {
 
 	Object[] exp = { "baruch", true };
 	Object[] actual = new Object[2];
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleBook(BibleBook.BARUCH);
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BOOK_NAME_F + ", " + BOOK_DEUT_F + " FROM " + BOOKS + " WHERE " + BOOK_NAME_F + " = 'baruch' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getBoolean(2);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void shouldInsertPosition() {
 
 	Object[] exp = { "john", 3, 16 };
 	Object[] actual = new Object[3];
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 3, 16));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", " + COORD_VERSE_F
 			    + " FROM " + COORDS + ", " + BOOKS + " WHERE " + BOOK_ID_F + " = " + COORD_BOOK_F+ " LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getInt(2);
 		actual[i++] = rs.getInt(3);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void shouldInsertBibleVersion() {
 
 	Object[] exp = { "Douay-Rheims", "en" };
 	Object[] actual = new Object[2];
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("Douay-Rheims", "en"));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + VERSION_ABBR_F + ", " + VERSION_LANG_F + " FROM " + VERSIONS
 			    + " WHERE " + VERSION_ABBR_F + " = 'Douay-Rheims' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void insertVerseShouldInsertVerse() {
 
 	Object[] exp = { "There was a man sent from God, whose name was John.", "john", 1, 6,
 		"English Standard Version", };
 	Object[] actual = new Object[5];
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("English Standard Version", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 	    bible.insertVerse(new Verse("There was a man sent from God, whose name was John.",
 					new Position(
 						     BibleBook.JOHN, 1, 6),
 					new BibleVersion("English Standard Version", "en")));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + VERSE_TEXT_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", "
 		                  	+ COORD_VERSE_F + ", " + VERSION_ABBR_F + " FROM " + VERSIONS
 			    + " INNER JOIN " + VERSES + " ON " + VERSION_ID_F + " = " + VERSE_VERSION_F
 			    + " INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F
 			    + " INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F
 			    + " WHERE " + VERSE_TEXT_F + " = 'There was a man sent from God, whose name was John.' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 		actual[i++] = rs.getInt(3);
 		actual[i++] = rs.getInt(4);
 		actual[i++] = rs.getString(5);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
 
     @Test
     public void testGetVerseWithOneVerse() {
 	Verse exp = new Verse("test text", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV", "en"));
 	Verse retrieved = null;
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertVerse(new Verse("test text", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV", "en")));
 
 	    retrieved = bible.getVerse(new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getVersesShouldRetrieveListOfAllRequestedVerses() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV", "en")));
 
 	    List<Position> positions = new ArrayList<Position>();
 	    positions.add(new Position(BibleBook.ACTS, 1, 2));
 	    positions.add(new Position(BibleBook.ACTS, 1, 3));
 	    positions.add(new Position(BibleBook.ACTS, 1, 4));
 
 	    retrieved = bible.getVerses(positions, new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
     
     @Test
     public void getChapterShouldReturnAllVersesFromSpecifiedChapter() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    //given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 2));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("test text4", new Position(BibleBook.ACTS, 2, 2), new BibleVersion("KJV", "en")));
 
 	    Position chapter = new Position(BibleBook.ACTS, 1, 0);
 
 	    //when
 	    retrieved = bible.getChapter(chapter, new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
     
     @Test
     public void getChapterListShouldReturnAllChaptersInSpecifiedBibleVersion() {
 	List<Position> exp = new ArrayList<Position>();
	exp.add(new Position(BibleBook.LUKE, 4, 0));
 	exp.add(new Position(BibleBook.ACTS, 1, 0));
 	exp.add(new Position(BibleBook.ACTS, 2, 0));
 	exp.add(new Position(BibleBook.ACTS, 4, 0));
 
 	List<Position> retrieved = null;
 
 	try {
 	    //given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("NIV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertBibleBook(BibleBook.JOB);
 	    bible.insertBibleBook(BibleBook.LUKE);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 1));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 3, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 4, 4));
 	    bible.insertPosition(new Position(BibleBook.JOB, 4, 4));
	    bible.insertPosition(new Position(BibleBook.LUKE, 4, 2));
 	    bible.insertPosition(new Position(BibleBook.LUKE, 4, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 1), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 2, 2), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 3, 3), new BibleVersion("NIV", "en")));
 	    bible.insertVerse(new Verse("test text4", new Position(BibleBook.ACTS, 4, 4), new BibleVersion("KJV", "en")));
	    bible.insertVerse(new Verse("test text5", new Position(BibleBook.LUKE, 4, 2), new BibleVersion("KJV", "en")));
	    bible.insertVerse(new Verse("test text6", new Position(BibleBook.LUKE, 4, 4), new BibleVersion("KJV", "en")));
 
 	    //when
 	    retrieved = bible.getChapterList(new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void compareVersesForOnePositionShouldRetrieveListOfAllRequestedVerses() {
 
 	int numOfVersionsToTest = 9;
 
 	List<Verse> exp = new ArrayList<Verse>();
 
 	for (int i = 0; i < numOfVersionsToTest; i++)
 	    exp.add(new Verse("x2test text" + (i + 1), new Position(BibleBook.ACTS, 1, 2),
 			      new BibleVersion(
 					       "KJV" + (i + 1), "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.createStorage();
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		bible.insertBibleVersion(new BibleVersion("KJV" + (i + 1), "en"));
 
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		bible.insertVerse(new Verse("x2test text" + (i + 1), new Position(BibleBook.ACTS, 1, 2),
 					    new BibleVersion("KJV" + (i + 1), "en")));
 
 	    List<BibleVersion> versions = new ArrayList<BibleVersion>();
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		versions.add(new BibleVersion("KJV" + (i + 1), "en"));
 
 	    retrieved = bible.compareVerses(new Position(BibleBook.ACTS, 1, 2), versions);
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void compareVersesForMorePositionsShouldRetrieveListOfAllRequestedVerses() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV1", "en")));
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV1", "en")));
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV1", "en")));
 
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV2", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV2", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV2", "en")));
 
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("KJV3", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("KJV3", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("KJV3", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV1", "en"));
 	    bible.insertBibleVersion(new BibleVersion("KJV2", "en"));
 	    bible.insertBibleVersion(new BibleVersion("KJV3", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV1", "en")));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "KJV1", "en")));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV1", "en")));
 
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV2", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "KJV2", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV2", "en")));
 
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV3", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "KJV3", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV3", "en")));
 
 	    List<BibleVersion> versions = new ArrayList<BibleVersion>();
 	    versions.add(new BibleVersion("KJV1", "en"));
 	    versions.add(new BibleVersion("KJV2", "en"));
 	    versions.add(new BibleVersion("KJV3", "en"));
 	    List<Position> positions = new ArrayList<Position>();
 	    positions.add(new Position(BibleBook.ACTS, 1, 2));
 	    positions.add(new Position(BibleBook.ACTS, 1, 3));
 	    positions.add(new Position(BibleBook.ACTS, 1, 4));
 
 	    retrieved = bible.compareVerses(positions, versions);
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
 
     private void insertSimulatedBibles(BibleStorage bible) throws BibleStorageException {
 
 	int BIBLE_VERSIONS = 61;
 	int CHAPTERS_IN_BOOK = 7;
 	int VERSES_IN_CHAPTER = 11;
 
 	BibleVersion[] bibles = new BibleVersion[BIBLE_VERSIONS];
 	for (int i = 0; i < BIBLE_VERSIONS; i++) {
 	    bibles[i] = new BibleVersion("Bible version " + i, "lang " + i);
 	    bible.insertBibleVersion(bibles[i]);
 	}
 
 	for (BibleBook book : BibleBook.values()) {
 	    System.out.println(book.getName());
 	    bible.insertBibleBook(book);
 
 	    for (int chpt = 0; chpt < CHAPTERS_IN_BOOK; chpt++)
 
 		for (int vrs = 0; vrs < VERSES_IN_CHAPTER; vrs++) {
 
 		    Position pos = new Position(book, chpt, vrs);
 		    bible.insertPosition(pos);
 
 		    for (BibleVersion version : bibles) {
 			bible.insertVerse(new Verse("this is the Bible verse text of position"
 				+ pos.toString()
 				+ "and version " + version.toString(), pos, version));
 		    }
 
 		}
 
 	}
 
     }
 
     @Test
     public void insertBookmarkShouldInsertBookmark() {
 
 	Object[] exp = { "joel", "But this is that which was spoken by the prophet Joel;" };
 	Object[] actual = new Object[2];
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 16));
 	    bible.insertVerse(new Verse("But this is that which was spoken by the prophet Joel;",
 					new Position(
 						     BibleBook.ACTS, 2, 16),
 					new BibleVersion("KJV", "en")));
 
 	    // when
 	    bible.insertBookmark(new Bookmark(
 					      "joel",
 					      new Verse(
 							"But this is that which was spoken by the prophet Joel;",
 							new Position(
 								     BibleBook.ACTS, 2, 16),
 							new BibleVersion("KJV", "en"))));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BKMARK_NAME_F + ", " + VERSE_TEXT_F + " FROM " + BKMARKS
 			    + " INNER JOIN " + VERSES + " ON " + VERSE_ID_F + " = " + BKMARK_VERSE_F
 			    + " WHERE " + BKMARK_NAME_F + " = 'joel' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void getBookmarksShouldRetrieveAllBookmarks() {
 	List<Bookmark> exp = new ArrayList<Bookmark>();
 	exp.add(new Bookmark("bkmark1", new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 						  new BibleVersion("KJV", "en"))));
 	exp.add(new Bookmark("bkmark2", new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 						  new BibleVersion("KJV", "en"))));
 	exp.add(new Bookmark("bkmark3", new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 						  new BibleVersion("KJV", "en"))));
 
 	List<Bookmark> retrieved = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV", "en")));
 
 	    bible.insertBookmark(new Bookmark("bkmark1", new Verse("test text1", new Position(BibleBook.ACTS,
 											      1, 2),
 								   new BibleVersion("KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark2", new Verse("test text2", new Position(BibleBook.ACTS,
 											      1, 3),
 								   new BibleVersion("KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark3", new Verse("test text3", new Position(BibleBook.ACTS,
 											      1, 4),
 								   new BibleVersion("KJV", "en"))));
 
 	    // when
 	    retrieved = bible.getBookmarks();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getBookmarksShouldRetrieveAllBookmarksForSpecifiedBibleVersion() {
 	List<Bookmark> exp = new ArrayList<Bookmark>();
 	exp.add(new Bookmark("bkmark1", new Verse("x1test text1", new Position(BibleBook.ACTS, 1, 2),
 						  new BibleVersion("KJV", "en"))));
 	exp.add(new Bookmark("bkmark3", new Verse("x1test text3", new Position(BibleBook.ACTS, 1, 4),
 						  new BibleVersion("KJV", "en"))));
 
 	List<Bookmark> retrieved = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("ROH", "sk"));
 	    bible.insertBibleVersion(new BibleVersion("NIV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("ECAV", "sk"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertVerse(new Verse("x1test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("x1test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion(
 							 "NIV", "en")));
 	    bible.insertVerse(new Verse("x1test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "KJV", "en")));
 	    bible.insertVerse(new Verse("x1test text4", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "ROH", "sk")));
 	    bible.insertVerse(new Verse("x1test text5", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion(
 							 "ECAV", "sk")));
 
 	    bible.insertBookmark(new Bookmark("bkmark1", new Verse("x1test text1", new Position(BibleBook.ACTS,
 												1, 2),
 								   new BibleVersion("KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark2", new Verse("x1test text2", new Position(BibleBook.ACTS,
 												1, 3),
 								   new BibleVersion("NIV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark3", new Verse("x1test text3", new Position(BibleBook.ACTS,
 												1, 4),
 								   new BibleVersion("KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark4", new Verse("x1test text4", new Position(BibleBook.ACTS,
 												1, 4),
 								   new BibleVersion("ROH", "sk"))));
 	    bible.insertBookmark(new Bookmark("bkmark5", new Verse("x1test text5", new Position(BibleBook.ACTS,
 												1, 4),
 								   new BibleVersion("ECAV", "sk"))));
 
 	    // when
 	    retrieved = bible.getBookmarks(new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void insertNoteShouldInsertNote() {
 	Object[] exp = { "note text", "acts", 2, 16, "u" };
 	Object[] actual = new Object[5];
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 16));
 
 	    // when
 	    bible.insertNote(new Note("note text", new Position(BibleBook.ACTS, 2, 16), NoteType.USER_NOTE));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + NOTE_TEXT_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", " + COORD_VERSE_F + ", " + NOTE_TYPE_F
 			    + " FROM " + NOTES
 			    + " INNER JOIN " + COORDS + " ON " + COORD_ID_F + " = " + NOTE_COORD_F
 			    + " INNER JOIN " + BOOKS + " ON " + BOOK_ID_F + " = " + COORD_BOOK_F
 			    + " WHERE " + NOTE_TEXT_F + " = 'note text' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 		actual[i++] = rs.getInt(3);
 		actual[i++] = rs.getInt(4);
 		actual[i++] = rs.getString(5);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
 
     @Test
     public void getNotesShouldRetrieveAllNotesForSpecifiedPosition() {
 	List<Note> exp = new ArrayList<Note>();
 	exp.add(new Note("note text2", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	exp.add(new Note("note text3", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	exp.add(new Note("note text4", new Position(BibleBook.ACTS, 1, 4), NoteType.COMMENTARY));
 
 	List<Note> retrieved = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertNote(new Note("note text1", new Position(BibleBook.ACTS, 1, 2), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text2", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text3", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text4", new Position(BibleBook.ACTS, 1, 4), NoteType.COMMENTARY));
 
 	    // when
 	    retrieved = bible.getNotes(new Position(BibleBook.ACTS, 1, 4));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void insertDictTermShouldInsertDictTerm() {
 	Object[] exp = { "term number one", "term text" };
 	Object[] actual = new Object[2];
 
 	try {
 	    // given
 	    bible.createStorage();
 
 	    // when
 	    bible.insertDictTerm(new DictTerm("term number one", "term text"));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + TERM_NAME_F + ", " + TERM_DEF_F + " FROM " + TERMS
 				  + " WHERE " + TERM_NAME_F + " = 'term number one' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
 
     @Test
     public void getDictTermShouldRetrieveRequestedTerm() {
 	DictTerm exp = new DictTerm("term number one", "term text");
 	DictTerm retrieved = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertDictTerm(new DictTerm("term number one", "term text"));
 	    bible.insertDictTerm(new DictTerm("term number two", "term text"));
 	    bible.insertDictTerm(new DictTerm("term number three", "term text"));
 
 	    // when
 	    retrieved = bible.getDictTerm("term number one");
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void insertReadingListShouldInsertReadingList() {
 	String exp = "reading list";
 	String actual = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 
 	    // when
 	    bible.insertReadingList("reading list");
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + RLIST_NAME_F + " FROM " + RLISTS + " WHERE " + RLIST_NAME_F + " = 'reading list' LIMIT 1");
 
 	    while (rs.next()) {
 		actual = rs.getString(1);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertEquals(actual, exp);
     }
 
     @Test
     public void insertDailyReadingShouldInsertDailyReading() {
 
 	Object[] exp = { "verse text 1", "verse text 2" };
 	Object[] actual = new Object[2];
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 7));
 	    bible.insertReadingList("reading list");
 	    bible.insertVerse(new Verse("verse text 1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("verse text 2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("KJV", "en")));
 
 	    List<Position> positions = new ArrayList<Position>();
 	    positions.add(new Position(BibleBook.JOHN, 1, 6));
 	    positions.add(new Position(BibleBook.JOHN, 1, 7));
 
 	    // when
 	    bible.insertDailyReading(new DailyReading("reading list", new DateTime("2011-07-12"), positions));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + VERSE_TEXT_F
 			    + " FROM " + VERSES
 			    + " INNER JOIN " + COORDS + " ON " + COORD_ID_F + " = " + VERSE_COORD_F
 			    + " INNER JOIN " + READxCOORDS + " ON " + READxCOORD_COORD_F + " = " + COORD_ID_F
 			    + " INNER JOIN " + READS + " ON " + READ_ID_F + " = " + READxCOORD_READ_F
 			    + " WHERE " + READ_DATE_F + " = " + "'2011-07-12'");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 	    }
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
 
     @Test
     public void getDailyReadingsShouldRetrieveAllDailyReadingsForSpecifiedDate() {
 	List<DailyReading> exp = new ArrayList<DailyReading>();
 
 	List<Position> positions1 = new ArrayList<Position>();
 	positions1.add(new Position(BibleBook.JOHN, 1, 1));
 	positions1.add(new Position(BibleBook.JOHN, 1, 2));
 
 	List<Position> positions2 = new ArrayList<Position>();
 	positions2.add(new Position(BibleBook.JOHN, 1, 5));
 	positions2.add(new Position(BibleBook.JOHN, 1, 6));
 
 	List<Position> positions3 = new ArrayList<Position>();
 	positions3.add(new Position(BibleBook.JOHN, 1, 5));
 	positions3.add(new Position(BibleBook.JOHN, 1, 6));
 
 	exp.add(new DailyReading("reading list1", new DateTime(2011, 7, 12, 0, 0, 0, 0), positions1));
 	exp.add(new DailyReading("reading list3", new DateTime(2011, 7, 12, 0, 0, 0, 0), positions3));
 
 	List<DailyReading> retrieved = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 1));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 5));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 
 	    bible.insertReadingList("reading list1");
 	    bible.insertReadingList("reading list2");
 	    bible.insertReadingList("reading list3");
 
 	    bible.insertDailyReading(new DailyReading("reading list1", new DateTime("2011-07-12"), positions1));
 	    bible.insertDailyReading(new DailyReading("reading list2", new DateTime("2011-08-12"), positions2));
 	    bible.insertDailyReading(new DailyReading("reading list3", new DateTime("2011-07-12"), positions3));
 
 	    // when
 	    retrieved = bible.getDailyReadings(new DateTime("2011-07-12"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void searchVersesForTextShouldReturnAllVersesFound() {
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("search2 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("KJV", "en")));
 	exp.add(new Verse("search2 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("KJV", "en")));
 	List<Verse> actual = null;
 
 	try {
 	    // given
 	    bible.createStorage();
 	    bible.insertBibleVersion(new BibleVersion("KJV", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 7));
 	    bible.insertVerse(new Verse("search1 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("search1 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("search2 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("search2 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("search3 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("KJV", "en")));
 	    bible.insertVerse(new Verse("search3 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("KJV", "en")));
 
 	    // when
 	    actual = bible.searchVersesForText("search2");
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(actual, exp);
     }
 
 }
