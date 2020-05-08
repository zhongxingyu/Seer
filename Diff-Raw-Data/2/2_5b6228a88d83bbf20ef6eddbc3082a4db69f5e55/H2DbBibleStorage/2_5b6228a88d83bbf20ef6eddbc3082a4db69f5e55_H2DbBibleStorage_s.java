 package com.github.mnicky.bible4j.storage;
 
 import hirondelle.date4j.DateTime;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.TimeZone;
 
 import com.github.mnicky.bible4j.data.BibleBook;
 import com.github.mnicky.bible4j.data.BibleVersion;
 import com.github.mnicky.bible4j.data.Bookmark;
 import com.github.mnicky.bible4j.data.DailyReading;
 import com.github.mnicky.bible4j.data.DictTerm;
 import com.github.mnicky.bible4j.data.Note;
 import com.github.mnicky.bible4j.data.Position;
 import com.github.mnicky.bible4j.data.Verse;
 import static com.github.mnicky.bible4j.storage.H2DbNaming.*;
 
 /**
  * {@link BibleStorage} backed by <a href="http://h2database.com">H2 database</a>.
  */
 public final class H2DbBibleStorage implements BibleStorage {
     
     //FIXME update SQL queries with BIBLE_NAME
 
     /**
      * Connection to H2 database.
      */
     private final Connection dbConnection;
 
     /**
      * Constructs new H2DbBibleStorage with specified connection to H2 database.
      * 
      * @param dbConnection
      *            conection to H2 database
      */
     public H2DbBibleStorage(Connection dbConnection) {
 	this.dbConnection = dbConnection;
     }
 
     /**
      * Closes this H2BibleStorage.
      * 
      * @throws BibleStorageException
      *             when H2DbBibleStorage can't be closed
      */
     @Override
     public void close() throws BibleStorageException {
 	try {
 	    this.dbConnection.close();
 	} catch (SQLException e) {
 	    throw new BibleStorageException("BibleStorage could not be closed", e);
 	}
     }
 
     // TODO needed?
     private int commitUpdate(PreparedStatement st) throws SQLException {
 
 	int rows = 0;
 	Connection con = st.getConnection();
 
 	try {
 	    con.setAutoCommit(false);
 	    rows = st.executeUpdate();
 	    con.commit();
 	} catch (SQLException e) {
 	    if (con != null)
 		con.rollback();
 	    throw e;
 	} finally {
 	    if (con != null)
 		con.setAutoCommit(true);
 	    st.close();
 	}
 
 	return rows;
     }
 
     // TODO needed?
     // TODO use CachedRowSet instead of ResultSet ?
     private ResultSet commitQuery(PreparedStatement st) throws SQLException {
 
 	ResultSet result = null;
 	Connection con = st.getConnection();
 
 	try {
 	    con.setAutoCommit(false);
 	    result = st.executeQuery();
 	    con.commit();
 	} catch (SQLException e) {
 	    if (con != null)
 		con.rollback();
 	    result = null;
 	    throw e;
 	} finally {
 	    if (con != null)
 		con.setAutoCommit(true);
 	}
 
 	return result;
     }
 
     private int[] commitBatch(Statement st) throws SQLException {
 
 	int[] rows = null;
 	Connection con = st.getConnection();
 
 	try {
 	    con.setAutoCommit(false);
 	    rows = st.executeBatch();
 	    con.commit();
 	} catch (SQLException e) {
 	    if (con != null)
 		con.rollback();
 	    throw e;
 	} finally {
 	    if (con != null)
 		con.setAutoCommit(true);
 	    st.close();
 	}
 
 	return rows;
     }
 
     @Override
     public int[] createStorage() throws BibleStorageException {
 
 	int[] columns;
 
 	try {
 	    Statement st = dbConnection.createStatement();
 
 	    // FIXME add CASCADE or RESTRICTED to foreign keys etc?
 	    // TODO convert more VARCHARs to V_IGNORECASE?
 	    // TODO add more UNIQUE constraints, CHECK etc... ?
 
 	    st.addBatch("CREATE ALIAS IF NOT EXISTS FT_INIT FOR \"org.h2.fulltext.FullText.init\";CALL FT_INIT();");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + VERSIONS + " ("
 		    + VERSION_ID + " INT IDENTITY NOT NULL,"
 		    + VERSION_ABBR + " VARCHAR_IGNORECASE(50) NOT NULL UNIQUE,"
 		    + VERSION_NAME + " VARCHAR_IGNORECASE(50) NOT NULL,"
 		    + VERSION_LANG + " VARCHAR(50) NOT NULL)");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + BOOKS + " ("
 		    + BOOK_ID + " INT IDENTITY NOT NULL,"
 		    + BOOK_NAME + " VARCHAR_IGNORECASE(50) NOT NULL UNIQUE,"
 		    + BOOK_DEUT + " BOOLEAN NOT NULL)");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + COORDS + " ("
 		    + COORD_ID + " INT IDENTITY NOT NULL,"
 		    + COORD_BOOK + " INT NOT NULL,"
 		    + COORD_CHAPT + " INT NOT NULL,"
 		    + COORD_VERSE + " INT NOT NULL,"
 		    + "CONSTRAINT `coords_unique` UNIQUE ( " + COORD_BOOK + ", " + COORD_CHAPT + ", " + COORD_VERSE + ")," 
 		    + "FOREIGN KEY (" + COORD_BOOK + ") REFERENCES " + BOOKS + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + VERSES + " ("
 		    + VERSE_ID + " INT IDENTITY NOT NULL,"
 		    + VERSE_TEXT + " VARCHAR(4096) NOT NULL,"
 		    + VERSE_VERSION + " INT NOT NULL,"
 		    + VERSE_COORD + " INT NOT NULL,"
 		    + "FOREIGN KEY (" + VERSE_VERSION + ") REFERENCES " + VERSIONS + ","
 		    + "FOREIGN KEY (" + VERSE_COORD + ") REFERENCES " + COORDS + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + NOTES + " ("
 		    + NOTE_ID + " INT IDENTITY NOT NULL,"
 		    + NOTE_TYPE + " VARCHAR(1) NOT NULL,"
 		    + NOTE_TEXT + " VARCHAR(500) NOT NULL,"
 		    + NOTE_COORD + " INT NOT NULL,"
 		    + "FOREIGN KEY (" + NOTE_COORD + ") REFERENCES " + COORDS + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + BKMARKS + " ("
 		    + BKMARK_ID + " INT IDENTITY NOT NULL,"
 		    + BKMARK_NAME + " VARCHAR(50) NOT NULL,"
 		    + BKMARK_VERSE + " INT NOT NULL,"
 		    + "FOREIGN KEY (" + BKMARK_VERSE + ") REFERENCES " + VERSES + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + RLISTS + " ("
 		    + RLIST_ID + " INT IDENTITY NOT NULL,"
 		    + RLIST_NAME + " VARCHAR(50) NOT NULL UNIQUE)");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + READS + " ("
 		    + READ_ID + " INT IDENTITY NOT NULL,"
 		    + READ_DATE + " DATE NOT NULL,"
 		    + READ_LIST + " INT NOT NULL,"
 		    + "FOREIGN KEY (" + READ_LIST + ") REFERENCES " + RLISTS + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + READxCOORDS + " ("
 		    + READxCOORD_ID + " INT IDENTITY NOT NULL,"
 		    + READxCOORD_COORD + " INT NOT NULL,"
 		    + READxCOORD_READ + " INT NOT NULL,"
 		    + "FOREIGN KEY (" + READxCOORD_COORD + ") REFERENCES " + COORDS + ","
 		    + "FOREIGN KEY (" + READxCOORD_READ + ") REFERENCES " + READS + ")");
 
 	    st.addBatch("CREATE TABLE IF NOT EXISTS " + TERMS + " ("
 		    + TERM_ID + " INT IDENTITY NOT NULL,"
 		    + TERM_NAME + " VARCHAR(50) NOT NULL UNIQUE,"
 		    + TERM_DEF + " VARCHAR(500) NOT NULL)");
 
 	    // TODO make constants from strings
 	    st.addBatch("CALL FT_CREATE_INDEX('PUBLIC', 'VERSES', 'TEXT');");
 
 	    columns = commitBatch(st);
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("BibleStorage could not be created", e);
 	}
 
 	return columns;
     }
 
     @Override
     public void insertVerse(Verse verse) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection
 		    .prepareStatement("INSERT INTO " + VERSES + " (" + VERSE_TEXT + ", " + VERSE_VERSION
 			    + ", " + VERSE_COORD + ") VALUES "
 			    + "( ?,"
 			    + "(SELECT DISTINCT " + VERSION_ID_F + " FROM " + VERSIONS
 					+ " WHERE " + VERSION_ABBR_F + " = ?),"
 			    + "(SELECT DISTINCT " + COORD_ID_F + " FROM " + COORDS
 					+ " WHERE " + COORD_CHAPT_F + " = ? AND "
 					+ COORD_BOOK_F + " = (SELECT DISTINCT " + BOOK_ID_F + " FROM " + BOOKS
 								+ " WHERE " + BOOK_NAME_F + " = ?) AND "
 								+ COORD_VERSE_F + " = ? ))");
 	    st.setString(1, verse.getText());
 	    st.setString(2, verse.getBibleVersion().getAbbr());
 	    st.setInt(3, verse.getPosition().getChapterNum());
 	    st.setString(4, verse.getPosition().getBook().getName());
 	    st.setInt(5, verse.getPosition().getVerseNum());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Verse could not be inserted", e);
 	}
 
     }
 
     @Override
     public void insertBibleBook(BibleBook book) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement("MERGE INTO " + BOOKS
 		    + "(" + BOOK_NAME + ", " + BOOK_DEUT + ") KEY (" + BOOK_NAME + ") VALUES ( ?, ?)");
 	    st.setString(1, book.getName());
 	    st.setBoolean(2, book.isDeutero());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bible book could not be inserted", e);
 	}
     }
 
     @Override
     public void insertPosition(Position position) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection
 		    .prepareStatement("MERGE INTO " + COORDS + "(" + COORD_BOOK + ", " + COORD_CHAPT + ", " + COORD_VERSE + ")"
 		                      + " KEY ( " + COORD_BOOK + ", " + COORD_CHAPT + ", " + COORD_VERSE + ")"
 		                      + " VALUES ((SELECT DISTINCT " + BOOK_ID_F + " FROM " + BOOKS + " WHERE " + BOOK_NAME_F
 		                      + " = ?), ?, ?)");
 	    st.setString(1, position.getBook().getName());
 	    st.setInt(2, position.getChapterNum());
 	    st.setInt(3, position.getVerseNum());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Position could not be inserted", e);
 	}
     }
 
     @Override
     public void insertBibleVersion(BibleVersion version) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement("MERGE INTO " + VERSIONS + " ("
 		    + VERSION_ABBR + ", " + VERSION_LANG + ", " + VERSION_NAME + ") KEY ( " + VERSION_ABBR + " ) VALUES ( ?, ?, ?)");
 	    st.setString(1, version.getAbbr());
 	    st.setString(2, version.getLanguage());
 	    st.setString(3, version.getName());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bible version could not be inserted", e);
 	}
     }
 
     @Override
     public Verse getVerse(Position position, BibleVersion version) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	Verse verse = null;
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 			    + VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", "
 			    + BOOK_NAME_F
 				      + "FROM " + VERSIONS
 				      + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 				      + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 				      + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 				      + "WHERE " + COORD_CHAPT_F + " = ? AND " + BOOK_NAME_F + " = ? AND "
 			    + VERSION_ABBR_F + " = ? AND " + COORD_VERSE_F + " = ? LIMIT 1");
 	    st.setInt(1, position.getChapterNum());
 	    st.setString(2, position.getBook().getName());
 	    st.setString(3, version.getAbbr());
 	    st.setInt(4, position.getVerseNum());
 	    rs = commitQuery(st);
 	    while (rs.next())
 		verse = new Verse(rs.getString(1), new Position(BibleBook.getBibleBookByName(rs
 			.getString(6)), rs.getInt(5), rs.getInt(4)), new BibleVersion(rs.getString(2), rs.getString(3)));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Verse could not be retrieved", e);
 	} finally {
 	    try {
 		if (rs != null)
 		    rs.close();
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	return verse;
     }
 
     @Override
     public List<Verse> getVerses(List<Position> positions, BibleVersion version) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Verse> verseList = new ArrayList<Verse>();
 
 	for (Position position : positions) {
 
 	    try {
 		st = dbConnection
 			.prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 				+ VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F
 					    + "FROM " + VERSIONS
 					    + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 					    + "WHERE " + COORD_CHAPT_F + " = ? AND " + BOOK_NAME_F + " = ? AND " + VERSION_ABBR_F
 					    + " = ? AND " + COORD_VERSE_F + " = ? LIMIT 1");
 		st.setInt(1, position.getChapterNum());
 		st.setString(2, position.getBook().getName());
 		st.setString(3, version.getAbbr());
 		st.setInt(4, position.getVerseNum());
 		rs = commitQuery(st);
 		while (rs.next())
 		    verseList.add(new Verse(rs.getString(1), new Position(BibleBook
 			    .getBibleBookByName(rs
 				    .getString(6)), rs.getInt(5), rs
 			    .getInt(4)), new BibleVersion(rs.getString(2), rs
 			    .getString(3))));
 
 	    } catch (SQLException e) {
 		throw new BibleStorageException("Verses could not be retrieved", e);
 	    } finally {
 		try {
 		    if (st != null)
 			st.close();
 		} catch (SQLException e) {
 		    e.printStackTrace();
 		}
 	    }
 	}
 
 	return verseList;
     }
     
     //chapter is represented by Position object with ignored verse number information
     @Override
     public List<Verse> getChapter(Position chapter, BibleVersion version) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Verse> verseList = new ArrayList<Verse>();
 
 	    try {
 		st = dbConnection
 			.prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 				+ VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F
 					    + "FROM " + VERSIONS
 					    + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 					    + "WHERE " + COORD_CHAPT_F + " = ? AND " + BOOK_NAME_F + " = ? AND " + VERSION_ABBR_F
 					    + " = ?");
 		st.setInt(1, chapter.getChapterNum());
 		st.setString(2, chapter.getBook().getName());
 		st.setString(3, version.getAbbr());
 		rs = commitQuery(st);
 		while (rs.next())
 		    verseList.add(new Verse(rs.getString(1), new Position(BibleBook
 			    .getBibleBookByName(rs
 				    .getString(6)), rs.getInt(5), rs
 			    .getInt(4)), new BibleVersion(rs.getString(2), rs
 			    .getString(3))));
 
 	    } catch (SQLException e) {
 		throw new BibleStorageException("Verses could not be retrieved", e);
 	    } finally {
 		try {
 		    if (st != null)
 			st.close();
 		} catch (SQLException e) {
 		    e.printStackTrace();
 		}
 	    }
 
 	return verseList;
     }
 
     @Override
     public List<Position> getChapterList(BibleVersion version) throws BibleStorageException {
 	PreparedStatement st = null;
 	List<Position> chapterList = new ArrayList<Position>();
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT DISTINCT " + BOOK_NAME_F + ", " + COORD_CHAPT_F
 				    + "FROM " + VERSIONS
 				    + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 				    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 				    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 				    + "WHERE " + VERSION_ABBR_F + " = ?");
 	    	st.setString(1, version.getAbbr());
 		
 		ResultSet rs = commitQuery(st);
 
 		while (rs.next())
 		    chapterList.add(new Position(BibleBook.getBibleBookByName(rs.getString(1)),
 		                                 rs.getInt(2), 0));
 
 		rs.close();
 
 	} catch (SQLException e) {
	    throw new BibleStorageException("CHapters could not be retrieved", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	
 	Collections.sort(chapterList);
 
 	return chapterList;
     }
 
     @Override
     public List<Verse> compareVerses(Position position, List<BibleVersion> versions) throws BibleStorageException {
 	PreparedStatement st = null;
 	List<Verse> verseList = new ArrayList<Verse>();
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 			    + VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F
 				    + "FROM " + VERSIONS
 				    + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 				    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 				    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 				    + "WHERE " + COORD_CHAPT_F + " = ? AND " + BOOK_NAME_F + " = ? AND "
 				    + VERSION_ABBR_F + " = ? AND " + COORD_VERSE_F + " = ? LIMIT 1");
 	    for (BibleVersion version : versions) {
 
 		st.setInt(1, position.getChapterNum());
 		st.setString(2, position.getBook().getName());
 		st.setString(3, version.getAbbr());
 		st.setInt(4, position.getVerseNum());
 
 		ResultSet rs = commitQuery(st);
 
 		while (rs.next())
 		    verseList.add(new Verse(rs.getString(1), new Position(BibleBook
 			    .getBibleBookByName(rs
 				    .getString(6)), rs.getInt(5), rs
 			    .getInt(4)), new BibleVersion(rs.getString(2), rs
 			    .getString(3))));
 
 		rs.close();
 
 	    }
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Verses could not be retrieved", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	return verseList;
     }
 
     @Override
     public List<Verse> compareVerses(List<Position> positions, List<BibleVersion> versions)
 	    throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Verse> verseList = new ArrayList<Verse>();
 
 	for (BibleVersion version : versions) {
 	    for (Position position : positions) {
 		try {
 		    st = dbConnection
 			    .prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 				    + VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F
 					    + "FROM " + VERSIONS
 					    + "INNER JOIN " + VERSES + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F + " "
 					    + "WHERE " + COORD_CHAPT_F + " = ? AND " + BOOK_NAME_F
 					    + " = ? AND " + VERSION_ABBR_F + " = ? AND " + COORD_VERSE_F
 					    + " = ? LIMIT 1");
 		    st.setInt(1, position.getChapterNum());
 		    st.setString(2, position.getBook().getName());
 		    st.setString(3, version.getAbbr());
 		    st.setInt(4, position.getVerseNum());
 		    rs = commitQuery(st);
 		    while (rs.next())
 			verseList.add(new Verse(rs.getString(1), new Position(BibleBook
 				.getBibleBookByName(rs
 					.getString(6)), rs.getInt(5), rs
 				.getInt(4)), new BibleVersion(rs.getString(2), rs
 				.getString(3))));
 
 		} catch (SQLException e) {
 		    throw new BibleStorageException("Verses could not be retrieved", e);
 		} finally {
 		    try {
 			if (st != null)
 			    st.close();
 		    } catch (SQLException e) {
 			e.printStackTrace();
 		    }
 		}
 	    }
 	}
 
 	return verseList;
     }
 
     @Override
     public void insertBookmark(Bookmark bookmark) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement(
 		    "INSERT INTO " + BKMARKS
 			    + "(" + BKMARK_VERSE + ", " + BKMARK_NAME + ") VALUES"
 			    + "((SELECT DISTINCT " + VERSE_ID_F + " FROM " + VERSES + " WHERE "
 					+ VERSE_VERSION_F + " = (SELECT DISTINCT " + VERSION_ID_F + " FROM " + VERSIONS + " WHERE "
 									+ VERSION_ABBR_F + " = ? AND "
 									+ VERSION_LANG_F + " = ?) AND "
 					+ VERSE_COORD_F + " = (SELECT DISTINCT " + COORD_ID_F + " FROM " + COORDS + " WHERE"
 									+ COORD_BOOK_F + " = (SELECT DISTINCT "
 											+ BOOK_ID_F + " FROM " + BOOKS + " WHERE "
 											+ BOOK_NAME_F + " = ?) AND "
 									+ COORD_CHAPT_F + " = ? AND "
 									+ COORD_VERSE_F + " = ?) AND "
 					+ VERSE_TEXT_F + " = ?)" + ", ?)");
 
 	    st.setString(1, bookmark.getVerse().getBibleVersion().getAbbr());
 	    st.setString(2, bookmark.getVerse().getBibleVersion().getLanguage());
 	    st.setString(3, bookmark.getVerse().getPosition().getBook().getName());
 	    st.setInt(4, bookmark.getVerse().getPosition().getChapterNum());
 	    st.setInt(5, bookmark.getVerse().getPosition().getVerseNum());
 	    st.setString(6, bookmark.getVerse().getText());
 	    st.setString(7, bookmark.getName());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bookmark could not be inserted", e);
 	}
 
     }
 
     @Override
     public List<Bookmark> getBookmarks() throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Bookmark> bookmarkList = new ArrayList<Bookmark>();
 
 	try {
 	    st = dbConnection
 			.prepareStatement("SELECT " + BKMARK_NAME_F + ", " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 				+ VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F + "FROM " + BKMARKS
 					    + "INNER JOIN " + VERSES + " ON " + BKMARK_VERSE_F + " = " + VERSE_ID_F + " "
 					    + "INNER JOIN " + VERSIONS + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F);
 	    rs = commitQuery(st);
 	    while (rs.next())
 		bookmarkList.add(new Bookmark(rs.getString(1), new Verse(rs.getString(2),
 									 new Position(BibleBook
 										 .getBibleBookByName(rs
 											 .getString(7)), rs
 										 .getInt(6), rs
 										 .getInt(5)),
 									 new BibleVersion(rs.getString(3), rs
 										 .getString(4)))));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bookmarks could not be retrieved", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	return bookmarkList;
     }
 
     @Override
     public List<Bookmark> getBookmarks(BibleVersion version) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Bookmark> bookmarkList = new ArrayList<Bookmark>();
 
 	try {
 	    st = dbConnection
 			.prepareStatement("SELECT " + BKMARK_NAME_F + ", " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 				+ VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F + "FROM " + BKMARKS
 					    + "INNER JOIN " + VERSES + " ON " + BKMARK_VERSE_F + " = " + VERSE_ID_F + " "
 					    + "INNER JOIN " + VERSIONS + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + "INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F
 					    + "WHERE " + VERSION_ABBR_F + " = ?");
 	    st.setString(1, version.getAbbr());
 	    rs = commitQuery(st);
 	    while (rs.next())
 		bookmarkList.add(new Bookmark(rs.getString(1), new Verse(rs.getString(2),
 									 new Position(BibleBook
 										 .getBibleBookByName(rs
 											 .getString(7)), rs
 										 .getInt(6), rs
 										 .getInt(5)),
 									 new BibleVersion(rs.getString(3), rs
 										 .getString(4)))));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bookmarks could not be retrieved", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	return bookmarkList;
     }
 
     @Override
     public void insertNote(Note note) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement(
 		    "INSERT INTO " + NOTES
 			    + "(" + NOTE_TYPE + ", " + NOTE_TEXT + ", " + NOTE_COORD + ") VALUES"
 			    + "(?, ?, (SELECT DISTINCT " + COORD_ID_F + " FROM " + COORDS + " WHERE"
 							+ COORD_BOOK_F + " = (SELECT DISTINCT "
 											+ BOOK_ID_F + " FROM " + BOOKS + " WHERE "
 											+ BOOK_NAME_F + " = ?) AND "
 							+ COORD_CHAPT_F + " = ? AND "
 							+ COORD_VERSE_F + " = ?))");
 
 	    st.setString(1, Character.toString(note.getType().getSpecifyingChar()));
 	    st.setString(2, note.getText());
 	    st.setString(3, note.getPosition().getBook().getName());
 	    st.setInt(4, note.getPosition().getChapterNum());
 	    st.setInt(5, note.getPosition().getVerseNum());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Note could not be inserted", e);
 	}
 
     }
 
     @Override
     public List<Note> getNotes(Position position) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Note> noteList = new ArrayList<Note>();
 
 	try {
 	    st = dbConnection
 			.prepareStatement("SELECT " + NOTE_TEXT_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", "
 					  + COORD_VERSE_F + ", " + NOTE_TYPE_F + "FROM " + NOTES
 				+ "INNER JOIN " + COORDS + " ON " + NOTE_COORD_F + " = " + COORD_ID_F + " "
 				+ "INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F
 				+ "WHERE " + BOOK_NAME_F + " = ? AND " + COORD_CHAPT_F + " = ? AND " + COORD_VERSE_F + "= ?");
 	    st.setString(1, position.getBook().getName());
 	    st.setInt(2, position.getChapterNum());
 	    st.setInt(3, position.getVerseNum());
 	    rs = commitQuery(st);
 	    while (rs.next())
 		noteList.add(new Note(rs.getString(1),
 					  new Position(BibleBook.getBibleBookByName(rs.getString(2)), rs
 						  .getInt(3), rs.getInt(4)), Note.getNoteTypeByChar(rs
 						  .getString(5).charAt(0))));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Bookmarks could not be retrieved", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	return noteList;
     }
 
     @Override
     public void insertDictTerm(DictTerm term) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement(
 		    "MERGE INTO " + TERMS
 			    + "(" + TERM_NAME + ", " + TERM_DEF + ") KEY (" + TERM_NAME + ") VALUES (?, ?)");
 	    st.setString(1, term.getName());
 	    st.setString(2, term.getDefinition());
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("DictTerm could not be inserted", e);
 	}
     }
 
     @Override
     public DictTerm getDictTerm(String name) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	DictTerm term = null;
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT " + TERM_NAME_F + ", " + TERM_DEF_F + "FROM " + TERMS + "WHERE " + TERM_NAME_F + " = ? LIMIT 1");
 	    st.setString(1, name);
 	    rs = commitQuery(st);
 	    while (rs.next())
 		term = new DictTerm(rs.getString(1), rs.getString(2));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("DictTerm could not be retrieved", e);
 	} finally {
 	    try {
 		if (rs != null)
 		    rs.close();
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	return term;
     }
 
     @Override
     public void insertReadingList(String name) throws BibleStorageException {
 	try {
 	    PreparedStatement st = dbConnection.prepareStatement(
 		    "MERGE INTO " + RLISTS + "(" + RLIST_NAME + ") KEY (" + RLIST_NAME + ") VALUES (?)");
 	    st.setString(1, name);
 	    commitUpdate(st);
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Reading list could not be inserted", e);
 	}
     }
 
     // TODO refactor this method
     @Override
     public void insertDailyReading(DailyReading reading) throws BibleStorageException {
 	PreparedStatement st = null;
 	try {
 	    dbConnection.setAutoCommit(false);
 
 	    // insert reading
 	    st = dbConnection.prepareStatement(
 		    "INSERT INTO " + READS
 			    + "(" + READ_DATE + ", " + READ_LIST + ") VALUES (?, "
 			    + "(SELECT DISTINCT " + RLIST_ID_F + " FROM " + RLISTS
 			    + "WHERE " + RLIST_NAME_F + " = ?))");
 	    st.setDate(1, new Date(reading.getDate().getMilliseconds(TimeZone.getDefault())));
 	    st.setString(2, reading.getReadingListName());
 	    st.executeUpdate();
 
 	    // insert verses of this reading
 	    int readingId = getThisReadingId(reading);
 
 	    st = dbConnection.prepareStatement("INSERT INTO " + READxCOORDS
 					+ "(" + READxCOORD_COORD + ", " + READxCOORD_READ + ") VALUES ("
 					+ "(SELECT DISTINCT " + COORD_ID_F + " FROM " + COORDS + " WHERE"
 								+ COORD_BOOK_F + " = (SELECT DISTINCT "
 											+ BOOK_ID_F + " FROM " + BOOKS + " WHERE "
 											+ BOOK_NAME_F + " = ?) AND "
 								+ COORD_CHAPT_F + " = ? AND "
 								+ COORD_VERSE_F + " = ?), ?)");
 
 	    for (Position p : reading.getPositions()) {
 		st.setString(1, p.getBook().name());
 		st.setInt(2, p.getChapterNum());
 		st.setInt(3, p.getVerseNum());
 		st.setInt(4, readingId);
 		st.addBatch();
 	    }
 
 	    st.executeBatch();
 	    dbConnection.commit();
 
 	} catch (SQLException e) {
 	    try {
 		dbConnection.rollback();
 	    } catch (SQLException e1) {
 		e1.printStackTrace();
 	    }
 	    throw new BibleStorageException("DailyReading could not be inserted", e);
 	} finally {
 	    try {
 		dbConnection.setAutoCommit(true);
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e2) {
 		e2.printStackTrace();
 	    }
 	}
     }
 
     private int getThisReadingId(DailyReading reading) throws BibleStorageException, SQLException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	int readingId = 0;
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT " + READ_ID_F + "FROM " + READS
 				      + "WHERE " + READ_DATE_F + " = ? AND "
 				      + READ_LIST_F + " = (SELECT DISTINCT "
 								+ RLIST_ID_F + " FROM " + RLISTS
 								+ " WHERE " + RLIST_NAME_F + " = ?) LIMIT 1");
 	    st.setDate(1, new Date(reading.getDate().getMilliseconds(TimeZone.getTimeZone("GMT"))));
 	    st.setString(2, reading.getReadingListName());
 	    rs = commitQuery(st);
 	    while (rs.next())
 		readingId = rs.getInt(1);
 
 	} finally {
 	    try {
 		if (rs != null)
 		    rs.close();
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	return readingId;
 
     }
 
     @Override
     public List<DailyReading> getDailyReadings(DateTime date) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<DailyReading> readings = new ArrayList<DailyReading>();
 
 	try {
 	    st = dbConnection
 		    .prepareStatement("SELECT " + RLIST_NAME_F + ", " + READ_DATE_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", "
 					+ COORD_VERSE_F
 				      + " FROM " + RLISTS
 				      + " INNER JOIN " + READS + " ON " + READ_LIST_F + " = " + RLIST_ID_F
 				      + " INNER JOIN " + READxCOORDS + " ON " + READxCOORD_READ_F + " = " + READ_ID_F
 				      + " INNER JOIN " + COORDS + " ON " + COORD_ID_F + " = " + READxCOORD_COORD_F
 				      + " INNER JOIN " + BOOKS + " ON " + BOOK_ID_F + " = " + COORD_BOOK_F
 				      + " WHERE " + READ_DATE_F + " = ? ORDER BY " + RLIST_NAME_F);
 
 	    st.setDate(1, new Date(date.getMilliseconds(TimeZone.getDefault())));
 	    rs = commitQuery(st);
 
 	    String lastReadingListName = "";
 	    DateTime lastRecDate = null;
 	    List<Position> positions = new ArrayList<Position>();
 
 	    while (rs.next()) {
 
 		String readingListName = rs.getString(1);
 		DateTime recDate = DateTime.forInstant(rs.getDate(2).getTime(), TimeZone.getDefault());
 		BibleBook book = BibleBook.getBibleBookByName(rs.getString(3));
 		int chapterNum = rs.getInt(4);
 		int verseNum = rs.getInt(5);
 
 		if (lastReadingListName.equals("") || lastReadingListName.equals(readingListName)) {
 		    positions.add(new Position(book, chapterNum, verseNum));
 		}
 		else {
 		    readings.add(new DailyReading(lastReadingListName, lastRecDate, positions));
 		    positions = new ArrayList<Position>();
 		    positions.add(new Position(book, chapterNum, verseNum));
 		}
 
 		lastReadingListName = readingListName;
 		lastRecDate = recDate;
 
 		if (rs.isLast()) {
 		    readings.add(new DailyReading(lastReadingListName, lastRecDate, positions));
 		}
 
 	    }
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("DailyReadings could not be retrieved", e);
 	} finally {
 	    try {
 		if (rs != null)
 		    rs.close();
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	return readings;
     }
 
     @Override
     public List<Verse> searchVersesForText(String text) throws BibleStorageException {
 	ResultSet rs = null;
 	PreparedStatement st = null;
 	List<Verse> verseList = new ArrayList<Verse>();
 
 	try {
 	    st = dbConnection
 			.prepareStatement("SELECT " + VERSE_TEXT_F + ", " + VERSION_ABBR_F + ", "
 					  + VERSION_LANG_F + ", " + COORD_VERSE_F + ", " + COORD_CHAPT_F + ", " + BOOK_NAME_F
 					    + " FROM FT_SEARCH_DATA(?, 0, 0) FT"
 					    + " INNER JOIN " + VERSES + " ON FT.TABLE = 'VERSES' AND " + VERSE_ID_F + " = FT.KEYS[0]"
 					    + " INNER JOIN " + VERSIONS + " ON " + VERSE_VERSION_F + " = " + VERSION_ID_F + " "
 					    + " INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F + " "
 					    + " INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F);
 	    st.setString(1, text);
 	    rs = commitQuery(st);
 	    while (rs.next())
 		verseList.add(new Verse(rs.getString(1), new Position(BibleBook
 			    .getBibleBookByName(rs
 				    .getString(6)), rs.getInt(5), rs
 			    .getInt(4)), new BibleVersion(rs.getString(2), rs
 			    .getString(3))));
 
 	} catch (SQLException e) {
 	    throw new BibleStorageException("Verses could not be searched", e);
 	} finally {
 	    try {
 		if (st != null)
 		    st.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 
 	return verseList;
     }
 
 }
