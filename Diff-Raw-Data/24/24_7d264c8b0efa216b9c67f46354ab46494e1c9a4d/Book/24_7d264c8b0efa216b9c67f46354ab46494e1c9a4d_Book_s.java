 package tables;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import users.Conn;
 
 public class Book implements Table {
 
 
 	//todo: handling not null?
 	private String callNumber;
 	private String isbn;
 	private String title;
 	private String mainAuthor;
 	private String publisher;
 	private int year;
 	private ArrayList<String> subjects;	
 	private ArrayList<String> authors;
 	private Connection c;
 	private Statement stmt;
 
 
 	//empty constructor for Book
 	//should connection singleton be "referenced" as part of the class or after each new object is instantiated. It would 
 	//refer to the same object right?
 	//why 2dArray of strings?
 	public Book(){
 
 	};
 
 	/**
 	 * Construct a Book object given all it's fields. 
 	 * @param cN
 	 * @param isBun
 	 * @param titleArg
 	 * @param mainAuthorArg
 	 * @param pub
 	 * @param yr
 	 * @param authrs
 	 * @param subjectsArg
 	 */
 	public Book(String cN, String isBun, String titleArg, String mainAuthorArg, 
 			String pub, int yr, ArrayList<String> authrs, ArrayList<String> subjectsArg){
 		callNumber = cN;
 		isbn = isBun;
 		title= titleArg;
 		mainAuthor = mainAuthorArg;
 		publisher = pub;
 		year = yr;
 		authors = authrs;
 		subjects = subjectsArg;
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 *
 	 * return a 2d array of strings, each row represents a tuple in the database
 	 * @throws SQLException 
 	 */
 	@Override
 	public String[][] display() throws SQLException {
 
 		//thanks Mitch! v   
 		ArrayList<String[]> borrowingGrowable = new ArrayList<String[]>();
 
 		PreparedStatement ps = c.prepareStatement("SELECT * FROM Book");
 		ResultSet rs = ps.executeQuery();
 
 		ResultSetMetaData md = rs.getMetaData();
 		int numFields = md.getColumnCount();
 		String[] columnNames = new String[numFields];
 		for (int i = 0; i < numFields; i++)
 		{
 			columnNames[i] = md.getColumnName(i + 1);
 		}
 		borrowingGrowable.add(columnNames);
 
 		//		int max = Math.max(authors.size(), subjects.size());
 		//		String[][] stringTable = new String[8][max];
 		//		stringTable[0][0] = callNumber;
 		//		stringTable[1][0] = isbn;
 		//		stringTable[2][0] = title;
 		//		stringTable[3][0] = mainAuthor;
 		//		stringTable[4][0] = publisher;
 		//		stringTable[5][0] = String.valueOf(year);
 		//		//		for(int i =0; i< authors.size()-1; i++){
 		//		//			stringTable[6][i] = authors.get(i);
 		//		//		}
 		//		//		for(int i =0; i< subjects.size()-1; i++){
 		//		//			stringTable[7][i] = subjects.get(i);
 		//		//		}
 		//		return stringTable;
 
 		while (rs.next())
 		{
 			String[] row = new String[numFields];
 			// row will contain a tuple from the database
 			int fieldIndex = 0;
 
 			// these fields are marked not null in database
 			//callNumber
 			row[fieldIndex] = rs.getString(fieldIndex + 1);
 			fieldIndex++;
 			//isbn
 			row[fieldIndex] = rs.getString(fieldIndex + 1);
 			fieldIndex++;
 
 			//possibly null
 			//title
 			String tempVal = rs.getString(fieldIndex + 1);
 			if(rs.wasNull()) row[fieldIndex] = "null";
 			else row[fieldIndex] = tempVal;
 			fieldIndex++;
 			//mainAuthor
 			tempVal = rs.getString(fieldIndex + 1);
 			if(rs.wasNull()) row[fieldIndex] = "null";
 			else row[fieldIndex] = tempVal;
 			fieldIndex++;
 
 			//publisher
 			tempVal = rs.getString(fieldIndex + 1);
 			if(rs.wasNull()) row[fieldIndex] = "null";
 			else row[fieldIndex] = tempVal;
 			fieldIndex++;
 			//year
 			int temp = rs.getInt(fieldIndex + 1);
 			if(rs.wasNull()) row[fieldIndex]= "null";
 			else row[fieldIndex] = Integer.toString(temp);
 
 
 			borrowingGrowable.add(row);
 		} 
 		rs.close();
 		int numRows = borrowingGrowable.size();
 		String[][] borrowing = new String[numRows][];
 		for (int i = 0; i < numRows; i++)
 		{
 			borrowing[i] = borrowingGrowable.get(i);
 		}
 		return borrowing;
 	}
 
 	/**
 	 * PRE: Assume the tuple exists in the database
 	 * Updates the tuple associated with this object.
 	 */
 	@Override
 	public void update() throws SQLException {
 
 		stmt = c.createStatement();
 
 		//update the corresponding book tuple in the Book Table according to this objects key(callnum)
 		int rows = stmt.executeUpdate( "UPDATE Book SET callNumber = " +callNumber+",isbn = " +isbn+"," +
 				"title = " +title+",mainAuthor = " +mainAuthor+",publisher = " +publisher+",year = " +year+"," +
 				" WHERE callNumber = " +callNumber ) ;
 	}
 
 
 	/**
 	 * Pre: Assume the tuple exists in the database
 	 * deletes this object from the database
 	 * Note: checks the key of Book - callNumber
 	 */
 	@Override
 	public boolean delete() throws SQLException {
 		Statement stmt = c.createStatement();
 		stmt.execute("DELETE FROM Book WHERE callNumber = "+ callNumber);
 		return true;
 	}
 
 	//	CREATE TABLE Book
 	//	(callNumber VARCHAR(32) NOT NULL,
 	//	isbn CHAR(13) NOT NULL UNIQUE,
 	//	title VARCHAR(32),
 	//	mainAuthor VARCHAR(32),
 	//	publisher VARCHAR(32),
 	//	year INT,
 	//	PRIMARY KEY(callNumber));
 
 	@Override
 	/**
 	 * PRE: Assume there is a table in the database called Book
 	 * This method inserts the object Book and it's variables into the database
 	 */
 	public boolean insert() throws SQLException{
 
 		String sql = "INSERT INTO Book VALUES ('"
 				+callNumber 
 				+"','"
 				+isbn
 				+"','"
 				+title
 				+"','"
 				+mainAuthor
 				+"','"
 				+publisher
 				+"',"
 				+year
 				+")";
 
 
 		Statement stmt = c.createStatement();
 		int rowCount1 = stmt.executeUpdate(sql); 
 
 		for(int i=0;i<subjects.size();i++){
 		HasSubject hS = new HasSubject(subjects.get(i),callNumber);
 			i++;
 		}
 		for(int i=0;i<authors.size();i++){
 			HasAuthor hA = new HasAuthor(authors.get(i),callNumber);
 			i++;
 		}
 		return true;
 
 	}
 
 
 
 
 
 
 	@Override
 	public Collection<Table> getAll() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * return the Book tuple corresponding to the callNumber field in this book object updated with the DB values
 	 * @throws SQLException 
 	 */
 	@Override
 	public Book get() throws SQLException {
 		Statement stmt1 = Conn.getInstance().getConnection().createStatement();
		ResultSet rs = stmt1.executeQuery("SELECT * FROM Book WHERE callNumber = "+callNumber);
 
 		if(rs!=null && rs.next()){
 			callNumber = rs.getString(1);
 			isbn = rs.getString(2); 
 			title = rs.getString(3);
 			mainAuthor = rs.getString(4);
 			publisher = rs.getString(5);
 			year = rs.getInt(6);
 
 			//how to handle authors/subjects?
 			//c.close();
 			return this;
 		}
 
 		return this;
 	}
 
 	/**
 	 * @return the callNumber
 	 */
 	public String getCallNumber() {
 		return callNumber;
 	}
 
 	/**
 	 * @param callNumber the callNumber to set
 	 */
 	public void setCallNumber(String callNumber) {
 		this.callNumber = callNumber;
 	}
 
 	/**
 	 * @return the isbn
 	 */
 	public String getIsbn() {
 		return isbn;
 	}
 
 	/**
 	 * @param isbn the isbn to set
 	 */
 	public void setIsbn(String isbn) {
 		this.isbn = isbn;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the mainAuthor
 	 */
 	public String getMainAuthor() {
 		return mainAuthor;
 	}
 
 	/**
 	 * @param mainAuthor the mainAuthor to set
 	 */
 	public void setMainAuthor(String mainAuthor) {
 		this.mainAuthor = mainAuthor;
 	}
 
 	/**
 	 * @return the publisher
 	 */
 	public String getPublisher() {
 		return publisher;
 	}
 
 	/**
 	 * @param publisher the publisher to set
 	 */
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	/**
 	 * @return the year
 	 */
 	public Integer getYear() {
 		return year;
 	}
 
 	/**
 	 * @param year the year to set
 	 */
 	public void setYear(Integer year) {
 		this.year = year;
 	}
 
 	/**
 	 * @return the authors
 	 */
 	public Collection<String> getAuthors() {
 		return authors;
 	}
 
 	/**
 	 * @param authors the authors to set
 	 */
 	public void setAuthors(ArrayList<String> authors) {
 		this.authors = authors;
 	}
 
 	/**
 	 * @return the subjects
 	 */
 	public Collection<String> getSubjects() {
 		return subjects;
 	}
 
 	/**
 	 * @param subjects the subjects to set
 	 */
 	public void setSubjects(ArrayList<String> subjects) {
 		this.subjects = subjects;
 	}
 
 	public Connection getC() {
 		return c;
 	}
 
 	public void setC(Connection c) {
 		this.c = c;
 	}
 
 }
