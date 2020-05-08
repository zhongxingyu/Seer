 package books;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class BookSearch {
 
 	Connection conn;
 	Statement stat;
 	ResultSet rs;
 
 	/**
 	 * Creates a BookSearch object that is connected to an SQL database containing Book, Author, 
 	 * and Authored tables
 	 * @param databaseLocation Location of database to be used
 	 * @throws ClassNotFoundException Cannot use JDBC
 	 * @throws SQLException Error connecting to database
 	 */
 	public BookSearch(String databaseLocation) throws ClassNotFoundException, SQLException	{
 			connect(databaseLocation);
 	}
 	
 	/**
 	 * Connects BookSearch object to an SQL database
 	 * @param databaseLocation Location of database to which BookSearch object should connect
 	 * @throws ClassNotFoundException Cannot use JDBC
 	 * @throws SQLException Error connecting to database
 	 */
 	public void connect(String databaseLocation) throws ClassNotFoundException, SQLException {
 		Class.forName("org.sqlite.JDBC");
 		conn =
 				DriverManager.getConnection("jdbc:sqlite:" + databaseLocation);
 		stat = conn.createStatement();
 		stat.execute("PRAGMA foreign_keys = ON;");
 	}
 
 	/**
 	 * Ensures that the input date is valid
 	 * @param dayOfMonth
 	 * @param month
 	 * @param year
 	 * @return true if date is valid, false if invalid
 	 */
 	public boolean checkDate(int dayOfMonth, int month, int year) {
 		
 		// Calendar starts date and month at 0
 		int calMonth = month-1;
 		
 		Calendar inputCal = Calendar.getInstance();
 		inputCal.set(year, calMonth, 1);	
 		Calendar cal = Calendar.getInstance();
 
 		// Can't have a future date
 		if ( !(year <= cal.get(Calendar.YEAR)) )
 			return false;
 		
 		// Month must be valid
 		if ( !(calMonth >= inputCal.getActualMinimum(Calendar.MONTH) 
 				&& calMonth <= inputCal.getActualMaximum(Calendar.MONTH)) )
 			return false;
 		
 		// Day must be valid
 		if ( !(dayOfMonth >= inputCal.getActualMinimum(Calendar.DAY_OF_MONTH) 
 				&& dayOfMonth <= inputCal.getActualMaximum(Calendar.DAY_OF_MONTH)) )
 			return false;
 		
 		return true;
 
 	}
 
 	/**
 	 * Adds a book to the database to which the BookSearch object is connected
 	 * @param title Title of book
 	 * @param pDayOfMonth Publish date
 	 * @param pMonth Publish month
 	 * @param pYear Publish year
 	 * @throws SQLException Database has too many Books. Auto-increment has reached 
 	 * the highest possible integer.
 	 */
 	public void addBook(String title, int pDayOfMonth, int pMonth, int pYear) throws SQLException {
 		stat.execute("INSERT INTO BOOK VALUES ('" + title +	"', " + pDayOfMonth + ", " 
 				+ pMonth + ", " + pYear + ", NULL);");
 	}
 
 	/**
 	 * Adds an Author to the database with the following parameters as values for its attributes
 	 * @param fName Author's first name
 	 * @param mName Author's middle name
 	 * @param lName Author's last name
 	 * @param bDay Author's birth day of month
 	 * @param bMonth Author's birth month
 	 * @param bYear Author's birth year
 	 * @throws SQLException Database has too many Authors. Autoincrement has reached the highest 
 	 * possible integer.
 	 */
 	public void addAuthor(String fName, String mName, String lName, int bDay, int bMonth, int bYear) throws SQLException{
 		stat.execute("INSERT INTO AUTHOR VALUES ('" + fName +
 				"', '" + mName + "', '" + lName + "', " + bDay + ", " + bMonth + ", " +
 				bYear + ", NULL);");
 	}
 
 	/**
 	 * Search for authors that meet a set of criteria. To not specify a field, use null
 	 * @param firstName
 	 * @param middleName
 	 * @param lastName
 	 * @param birthDay
 	 * @param birthMonth
 	 * @param birthYear
 	 * @param authorID
 	 * @return
 	 * @throws SQLException
 	 */
 	public ArrayList<Author> searchAuthor(String firstName, String middleName, String lastName, String birthDay,
 			String birthMonth, String birthYear, String authorID) throws SQLException {
 		
 		ArrayList<Author> authors = new ArrayList<Author>();
 		String[] fields = {firstName, middleName, lastName, birthDay, birthMonth, birthYear, authorID};
 		String query = "SELECT * FROM AUTHOR";
 		for (int i = 0 ; i < fields.length ; i++)	{
 			if (fields[i] != null)	{
 				query += " WHERE";
 				break;
 			}
 		}
 
 		boolean andNeeded = false;
 		if (fields[0] != null)	{
 			query += (andNeeded) ? " AND fNamee='"+fields[0]+"'" : " fName='"+fields[0]+"'";
 			andNeeded = true;
 		}
 		if (fields[1] != null)	{
 			query += (andNeeded) ? " AND mName='"+fields[1]+"'" : " mName='"+fields[1]+"'";
 			andNeeded = true;
 		}
 		if (fields[2] != null)	{
 			query += (andNeeded) ? " AND lName='"+fields[2]+"'" : " lName='"+fields[2]+"'";
 			andNeeded = true;
 		}
 		if (fields[3] != null)	{
 			query += (andNeeded) ? " AND bDay="+fields[3] : " bDay="+fields[3];
 			andNeeded = true;
 		}
 		if (fields[4] != null)	{
 			query += (andNeeded) ? " AND bMonth="+fields[4] : " bMonth="+fields[4];
 			andNeeded = true;
 		}
 		if (fields[5] != null)	{
 			query += (andNeeded) ? " AND bYear="+fields[4] : " bYear="+fields[4];
 			andNeeded = true;
 		}
 		if (fields[6] != null)	{
			query += (andNeeded) ? " AND authorID="+fields[4] : " authorID="+fields[4];
 			andNeeded = true;
 		}
 		ResultSet rs = stat.executeQuery(query);
 		while (rs.next()) authors.add(new Author(rs));
 
 		return authors;
 	}
 
 	/**	 
 	 * Searches for all books that meet a set of criteria. To leave a field empty, pass null
 	 * @param title
 	 * @param pDay
 	 * @param pMonth
 	 * @param pYear
 	 * @param bookID
 	 * @return
 	 * @throws SQLException
 	 */
 	public ArrayList<Book> searchBook(String title, String pDay, String pMonth, String pYear, 
 			String bookID) throws SQLException {
 		
 		String[] fields = {title, pDay, pMonth, pYear, bookID};
 		ArrayList<Book> books = new ArrayList<Book>();
 		String query = "SELECT * FROM BOOK";
 		for (int i = 0 ; i < fields.length ; i++)	{
 			if (fields[i] != null)	{
 				query += " WHERE";
 				break;
 			}
 		}
 		boolean andNeeded = false;
 		if (fields[0] != null)	{
 			query += (andNeeded) ? " AND title='"+fields[0]+"'" : " title='"+fields[0]+"'";
 			andNeeded = true;
 		}
 		if (fields[1] != null)	{
 			query += (andNeeded) ? " AND pDay="+fields[1] : " pDay="+fields[1];
 			andNeeded = true;
 		}
 		if (fields[2] != null)	{
 			query += (andNeeded) ? " AND pMonth="+fields[2] : " pMonth="+fields[2];
 			andNeeded = true;
 		}
 		if (fields[3] != null)	{
 			query += (andNeeded) ? " AND pYear="+fields[3] : " pYear="+fields[3];
 			andNeeded = true;
 		}
 		if (fields[4] != null)	{
 			query += (andNeeded) ? " AND bookID="+fields[4] : " bookID="+fields[4];
 			andNeeded = true;
 		}
 		ResultSet rs = stat.executeQuery(query);
 		while (rs.next()) books.add(new Book(rs));
 		return books;
 	}
 
 	//Returns all of the authors for a book given the book's ID
 	public ArrayList<Author> getBookAuthors(int bookID){
 		ArrayList<Author> authors = new ArrayList<Author>();
 		
 		return authors;
 	}
 
 	//Returns all of the books an author wrote given the author's ID
 	public ArrayList<Book> getAuthorsBooks(int authorID){
 		ArrayList<Book> books = new ArrayList<Book>();
 
 		return books;
 	}
 
 	//Removes a book from the database, also removes all AUTHORED tuples containing the bookID
 	public void removeBook(int bookID) {
 		try {
 		rs = stat.executeQuery("DELETE FROM BOOK WHERE bookID = '" + bookID + "'");
 		} catch (Exception err){
 
 		}
 	}
 
 
 }
