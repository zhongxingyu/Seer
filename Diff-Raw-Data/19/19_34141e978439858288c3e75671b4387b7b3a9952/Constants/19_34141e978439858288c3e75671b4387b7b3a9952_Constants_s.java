 package main;
 /**
  * A collection of system-wide static constants.
  * NOTE: Possible error concerning all UPDATE strings. May be missing sufficient
  * information to identify proper table entry. Consider adding "WHERE ID = ::id"
  * to identify.
  * @author sgeir Davidsson
  *
  */
 public class Constants {
 	
 	// Database
 	// NOTE: Same as from Pizzabox project, no other way to implement.
 	public static final String DB_DRIVER = "org.apache.derby.jdbc.ClientDriver";
 	public static final String DATABASE_INIT_URL = "jdbc:derby:ITP_database;create=true";
 	public static final String DATABASE_SHUTDOWN_URL ="jdbc:derby:ITP_database;shutdown=true";
 	
 	//AuthorMapper
 	public static final String AUTHOR_RETRIEVE = "SELECT * FROM AUTHOR WHERE AUTHORID = ::id";
 	public static final String AUTHOR_RETRIEVE_BY_FIRSTNAME = "SELECT AUTHORID FROM AUTHOR WHERE FIRSTNAME = '::firstname'";
 	public static final String AUTHOR_RETRIEVE_BY_LASTNAME = "SELECT AUTHORID FROM AUTHOR WHERE LASTNAME = '::last'";
 	public static final String AUTHOR_UPDATE = "UPDATE AUTHOR SET AUTHORID = ::id, FIRSTNAME = '::firstname', LASTNAME = '::lastname'";
 	public static final String AUTHOR_INSERT = "INSERT INTO AUTHOR VALUES (DEFAULT, '::firstname', '::lastname')";
 	public static final String AUTHOR_DELETE = "DELETE FROM AUTHOR WHERE AUTHORID = ::id";
 	
 	//PublisherMapper
 	public static final String PUBLISHER_RETRIEVE = "SELECT * FROM PUBLISHER WHERE PUBLISHERID = ::id";
 	public static final String PUBLISHER_RETRIEVE_BY_NAME = "SELECT PUBLISHERID FROM PUBLISHER WHERE NAME = '::name'"; 
 	public static final String PUBLISHER_UPDATE = "UPDATE PUBLISHER SET PUBLISHERID = ::id, NAME = '::name'";
 	public static final String PUBLISHER_INSERT = "INSERT INTO PUBLISHER VALUES (DEFAULT, '::name')";
 	public static final String PUBLISHER_DELETE = "DELETE FROM PUBLISHER WHERE PUBLISHERID = ::id";
 	
 	//GenreMapper
 	public static final String GENRE_RETRIEVE = "SELECT * FROM GENRE WHERE GENREID = ::id";
 	public static final String GENRE_RETRIEVE_BY_NAME = "SELECT GENREID FROM GENRE WHERE NAME = '::name'";
 	public static final String GENRE_UPDATE = "UPDATE GENRE SET GENREID = ::id, NAME = '::name'";
 	public static final String GENRE_INSERT = "INSERT INTO GENRE VALUES (DEFAULT, '::name')";
 	public static final String GENRE_DELETE = "DELETE FROM GENRE WHERE GENREID = ::id";
 	
 	//FormatMapper
 	public static final String FORMAT_RETRIEVE = "SELECT * FROM FORMAT WHERE FORMATID = ::id";
 	public static final String FORMAT_RETRIEVE_BY_NAME = "SELECT FORMATID FROM FORMAT WHERE NAME = '::name'";
 	public static final String FORMAT_UPDATE = "UPDATE FORMAT SET NAME = '::name'";
 	public static final String FORMAT_INSERT = "INSERT INTO FORMAT VALUES (DEFAULT, '::name')";
 	public static final String FORMAT_DELETE = "DELETE FROM FORMAT WHERE FORMATID = ::id";
 	
 	//BookMapper
 	public static final String BOOK_RETRIEVE = "SELECT * FROM BOOK WHERE ISBN = ::id";
 	public static final String BOOK_UPDATE = "UPDATE BOOK SET TITLE = '::title', AUTHORID = ::authorID, PUBLISHERID = ::publisherID, GENREID = ::genreID, FORMATID = ::formatID, PRICE = ::price";
	public static final String BOOK_INSERT = "INSERT INTO BOOK VALUES (::ISBN, '::title', ::authorID, ::publisherID, ::genreID, ::formatID, ::price)";
 	public static final String BOOK_DELETE = "DELETE FROM BOOK WHERE ISBN = ::id";
 	public static final String BOOK_SELL = "UPDATE BOOK SET QUANTITY = ::quantity WHERE ISBN = ::isbn";
 	public static final String SALES_RECORD = "INSERT INTO SALES VALUES (::ISBN, ::date, ::price)";
 	
 	//LoginMapper
 	public static final String LOGIN_CHECK = "SELECT * FROM LOGIN WHERE NAME = '::username' AND PASSWORD = '::password'";
 	
 	//Author
 	public static final String AUTHOR_ID = "authorID";
 	public static final String AUTHOR_FIRSTNAME = "firstname";
 	public static final String AUTHOR_LASTNAME = "lastname";
 	
 	//Publisher
 	public static final String PUBLISHER_ID = "publisherID";
 	public static final String PUBLISHER_NAME = "name";
 	
 	//Genre
 	public static final String GENRE_ID = "genreID";
 	public static final String GENRE_NAME = "name";
 	
 	//Format
 	public static final String FORMAT_ID = "formatID";
 	public static final String FORMAT_NAME = "name";
 	
 	//Book
 	public static final String BOOK_ID = "ISBN";
 	public static final String BOOK_AUTHOR = "authorID";
 	public static final String BOOK_PUBLISHER = "publisherID";
 	public static final String BOOK_GENRE = "genreID";
 	public static final String BOOK_FORMAT = "formatID";
 	public static final String BOOK_TITLE = "title";
 	public static final String BOOK_PRICE = "price";
 	
  	
 	
 }
