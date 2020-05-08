 /**
  * File: DatabaseConnection.java
  * Created: Aug 6, 2012
  */
 package quizsite.util;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * File: DBConnection.java
  * Author: Rege
  * Created: Jul 26, 2012
  * eg.
  * $ try {
  * $	DatabaseConnection db = new DatabaseConnection();
  * $ 	db.executeQuery();
  * $	db.close();
  * $ } catch (SQLException e) {}
  */
 public class DatabaseConnection {
 	private static Mode mode = Mode.PRODUCTION;
 
 	public enum Mode {
 		PRODUCTION ("ccs108rege", "ahdeetha", "c_cs108_rege", "mysql-user.stanford.edu"),
 		TEST ("ccs108makarst", "yexubohn", "c_cs108_makarst", "mysql-user.stanford.edu");
 
 		private final String account;
 		private final String password;
 		private final String database;
 		private final String server;
 		Mode(String account, String password, String database, String server) {
 			this.account = account;
 			this.password = password;
 			this.database = database;
 			this.server = server;
 		}
 		public String getAccount() {
 			return account;
 		}
 		public String getPassword() {
 			return password;
 		}
 		public String getDatabase() {
 			return database;
 		}
 		public String getServer() {
 			return server;
 		}
 	}
 
 
 	private Statement stmt;
 	private Connection conn;
 
 	/**
 	 * Private helper: Sets up connection to the database. Catches ClassNotFoundException and throws {@link SQLException}
 	 * @return statement variable to be used for executing queries.
 	 * */
 	private void setUpDBConnection() throws SQLException{
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection ( "jdbc:mysql://" + mode.getServer(), mode.getAccount(), mode.getPassword());
 			stmt = conn.createStatement();
 			stmt.executeQuery("USE " + mode.getDatabase());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Parses the ResultSet variable obtained from a SQL query and returns it as a ArrayList 
 	 * of rows [each row is a ArrayList of Strings]. Throws {@link SQLException}
 	 * @param	rs	{@link ResultSet} variable obtained from a SQL query
 	 * @return	a ArrayList of ArrayList of Strings containing all the rows contained in the parameter 
 	 * */
 	static List< List<String> > parseResultData( ResultSet rs ) throws SQLException {
 		// Fill in the data
 		List< List<String> > rows = new ArrayList< List<String> >();
 		List<String> newRow;
 		int nCol = rs.getMetaData().getColumnCount();
 		while(rs.next()) {
 			newRow = new ArrayList<String>();
 			for (int c = 0; c < nCol; c++) {
 				newRow.add(rs.getString(c + 1));			// Numbering is 1-indexed			
 			}
 			rows.add(newRow);
 		}
 		return rows;
 	}
 
 	/**
 	 * Used to execute raw SQL queries
 	 */
 	public ResultSet executeQuery(String sqlQuery) throws SQLException {
 		return stmt.executeQuery(sqlQuery);
 	}
 
 	/**
 	 * Execute updates on the table - INSERT, UPDATE, DELETE
 	 * */
 	public int executeUpdate(String sqlQuery) throws SQLException {
 		return stmt.executeUpdate(sqlQuery, Statement.RETURN_GENERATED_KEYS);
 	}
 
 	public int getGeneratedKey() throws SQLException {
 		ResultSet rs = stmt.getGeneratedKeys();
 		int key = -1;
 		if (rs.next()) {
 			key = rs.getInt(1);
 		} else {
 			throw new SQLException("ResultSet should've contained one key, did you call getGeneratedKey after running executeUpdate");
 		}
 		return key;
 	}
 
 	/*** STANDARD SQL QUERIES ***/
 
 	/**
 	 * Fetches all rows from given table as a ArrayList of ArrayList of strings
 	 * */
 	List< List<String> > fetchAllRows(String tableName) throws SQLException {
 		ResultSet rs = executeQuery("SELECT * FROM " + tableName);
 		return parseResultData(rs);
 	}
 
 	List<String> fetchRowById(String tableName, int id) throws SQLException{
 		String findQuery = "SELECT * FROM " + tableName + " WHERE id = '" + id + "'";
 		ResultSet rs = executeQuery(findQuery);
 		List<List<String> > res = parseResultData(rs);
 		switch (res.size()) {
 		case 0:
 			return null;
 		case 1:
 			return res.get(0);
 		default:
 			throw new SQLException("Uniqueness of id violated");
 		}
 	}
 
 	/** Runs a where SQL query for the AND of all conditions 
 	 * @throws SQLException */
 	List<List<String> > fetchRowsWhere(String tableName, String[][] conditions) throws SQLException {
 		String whereQuery = "SELECT * FROM " + tableName + " " + whereQuery(conditions);
 		return parseResultData(executeQuery(whereQuery));
 	}
 	
 	/** Use with caution. Runs a raw SQL query 
 	 * @throws SQLException */
 	List<List<String> > fetchRowsRawSQL(String tableName, String condition) throws SQLException {
 		String rawQuery = "SELECT * FROM " + tableName + " " + condition;
 		return parseResultData(executeQuery(rawQuery));
 	}
 
 	/**
 	 * Constructor
 	 * */
 	public DatabaseConnection() throws SQLException {
 		setUpDBConnection();
 	}
 
 	// This has been added for testing purposes. Call switchModeTo(Data..ion.TEST) in the setUp()
 	// for JUnit tests and switchModeTo(Data..ion.PRODUCTION) in the tearDown()
 	public static void switchModeTo(Mode newMode) {
 		mode = newMode;
 	}
 
 	// Closes the JDBC connection - call it once you're done with your db object
 	public void close() throws SQLException {
 		stmt.close();
 		conn.close();
 	}
 
 
 	// Creates the backing table if it doesn't exist
 	public static int createTableIfNotExists(PersistentModel pm) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + pm.getTableName() + 
 		"( id INTEGER AUTO_INCREMENT" + pm.getSchema() 
 		+ ForeignKey.serialize(pm.getForeignKeys()) 
 		+ ", PRIMARY KEY (id) ) ";
 		System.out.println(createTableQuery);
 		int result = db.executeUpdate(createTableQuery);
 		db.close();
 		return result;
 	}
 
 	// Drops the tables, if they exist - CAREFUL
 	public static void dropTablesIfExist(String...tableNames) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		for (String tableName : tableNames) {
 			String dropTableQuery = "DROP TABLE IF EXISTS " + tableName;
 			db.executeUpdate(dropTableQuery);
 		}
 		db.close();
 	}
 
 	// Check if table exists
 	public static boolean doesTableExist(String tableName) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		try {
 			String basicQuery = "SELECT * FROM " + tableName + " LIMIT 0";
 			db.executeQuery(basicQuery);
 			return true;
 		} catch (SQLException e) {
 			return false;
 		} finally {
 			db.close();
 		}
 	}
 
 	/*
 	 * Returns a string properly formatted for using inside an sql query
 	 * If the second param true, each word is surrounded with single quotes  
 	 * 
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" }) // To be as general as possible, we want this function to take List<Object>, but at
 	// the same time we don't want to change the return type of getColumnNames/Values
 	private String getFormattedStringFromList(List list, boolean isEscaped) {
 		StringBuilder sb = new StringBuilder();
 		for (Iterator<Object> itr = list.iterator(); itr.hasNext();) {
 			if (isEscaped) sb.append("'");
 			sb.append(itr.next());	
 			if (isEscaped) sb.append("'");
			sb.append(", "); // extra appending at the end of the loop needs to be removed when returning
 		}
		return sb.substring(0, sb.length() - 2)+" "; //Removes the trailing comma ', ' then add space
 	}
 
 	private String whereQuery(String[][] conditions) {
 		if (conditions.length > 0) {
 			String whereClause;
 			StringBuilder sb = new StringBuilder(" WHERE ");
 			for (String[] condition : conditions) {
 				whereClause = condition[0] + " " + condition[1] + " '" + condition[2] + "' AND ";
 				sb.append(whereClause);
 			}
 			String whereQuery = sb.toString();
 			return whereQuery.substring(0, whereQuery.length() - 4);
 		} else {
 			return "";
 		}
 	}
 
 	/* REST API */
 	/** Get a list of all rows */
 	public static List<List<String> > index(String tableName) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		List<List<String> > ret = db.fetchAllRows(tableName);
 		db.close();
 		return ret;
 	}
 
 	/** Runs an AND-ed WHERE query on given tableName
 	 * $ stuff = "something";
 	 * $ conditions = {{"id", ">", "23"},{"data", "=", stuff}} => 'WHERE id > '23' AND data = 'something'
 	 * */
 	public static List< List<String> > indexWhere(String tableName, String[][] conditions) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		List<List<String> > ret = db.fetchRowsWhere(tableName, conditions);
 		db.close();
 		return ret;
 	}
 	
 	/** Executes a raw SQL query on the given tableName
 	 * $ condition = "WHERE id = '23'"; tableName = Message.TABLE_NAME;
 	 * => Query run = "SELECT * FROM Message WHERE id = '23'"
 	 * @throws SQLException 
 	 * */
 	public static List<List<String> > indexQueryRaw(String tableName, String condition) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		List<List<String> > ret = db.fetchRowsRawSQL(tableName, condition);
 		db.close();
 		return ret;
 	}
 
 	// Create and save a new row - returns the auto-generated id of the new row
 	public static int create(PersistentModel pm) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 
 		String columnNames  = db.getFormattedStringFromList(pm.getColumnNames(), false);
 		String columnValues = db.getFormattedStringFromList(pm.getColumnValues(), true);
 
		String createQuery  = "INSERT INTO " + pm.getTableName() + " ( " + columnNames + 
		" ) VALUES (" + columnValues + ")";
 		db.executeUpdate(createQuery);
 		int id = db.getGeneratedKey();
 		db.close();
 		return id;
 	}
 
 	// Delete a row - ensure that the id is populated
 	public static int destroy(PersistentModel pm) throws SQLException {
 		String destroyQuery = "DELETE FROM " + pm.getTableName() + " WHERE id ='" + pm.getId() + "'";
 		DatabaseConnection db = new DatabaseConnection();
 		int res = db.executeUpdate(destroyQuery);
 		db.close();
 		return res;
 	}
 
 	// Get a specific row by it's id
 	public static List<String> get(String tableName, int id) throws SQLException {
 		DatabaseConnection db = new DatabaseConnection();
 		List<String> res = db.fetchRowById(tableName, id);
 		db.close();
 		return res;
 	}
 
 	/**
 	 * Update a row corresponding to a specific id
 	 * @param pm
 	 * @param status of the update operation [eg. 1 row affected]
 	 * @return An updated row
 	 * @throws SQLException
 	 */
 	public static int update(PersistentModel pm) throws SQLException {
 		List<String> columns = pm.getColumnNames();
 		List<Object> values  = pm.getColumnValues();
 
 		// construct an update string
 		StringBuilder updateStrBuilder = new StringBuilder();
 		for (int i = 0; i < columns.size(); i++) {
 			updateStrBuilder.append(columns.get(i)+"='"+values.get(i)+"',");
 		}
 		String updateStr = updateStrBuilder.toString();
 
 		String updQuery = "UPDATE " + pm.getTableName() + " SET " + updateStr.substring(0, updateStr.length() - 1) + " WHERE id = '" + pm.getId() + "'";
 		DatabaseConnection db = new DatabaseConnection();
 		int res = db.executeUpdate(updQuery);
 		db.close();
 		return res;
 	}
 }
