 package awana.database;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import org.h2.jdbcx.JdbcDataSource;
 //TODO: rewrite wrapper using http://iciql.com/ to simplify database connections.
 
 /**
  *
  * @author Renlar <liddev.com>
  */
 public class DatabaseWrapper {
 
 	public static final String databaseLocked = "Awana Database can not start because the database is locked.\nEither the program is already running, or it did not shutdown properly last time it was run.";
 	public static final String altar = "ALTAR";
 	public static final String create = "CREATE ";
 	public static final String delete = "DELETE ";
 	public static final String from = "FROM ";
 	public static final String insert = "INSERT ";
 	public static final String into = "INTO ";
 	public static final String select = "SELECT ";
 	public static final String set = "SET ";
 	public static final String table = "TABLE ";
 	public static final String update = "UPDATE ";
 	public static final String values = "VALUES ";
 	public static final String where = "WHERE ";
 	public static final String bool = "BOOLEAN ";
 	public static final String date = "DATE ";
 	public static final String varchar100 = "VARCHAR(100) ";
 	public static final String fieldPrefix = "field_";
 	public static final String bookPrefix = "book_";
 	public static final String completedPostfix = "_completed";
 	public static final String datePostfix = "_date";
 	public static final String bigInt = "BIGINT";
 	public static final String storagePath = "AWANA.Data/";
 	public static final String databaseName = "Directory";
 	private String dataTable = "DIRECTORY";
 	private Connection h2DatabaseConnection;
 
 	public DatabaseWrapper() {
 		createDatabaseConnection();
 		updateDatabase();
 	}
 
 	public void deleteRecord(Record r) {
 		try {
 			runStatement(delete + from + dataTable + " " + where + "ID = \'" + r.getID() + "\';");
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public void deleteListing(Listing l) {
 		try {
 			runStatement(delete + from + dataTable + " " + where + "ID = '" + l.getID() + "';");
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public Record getRecord(int ID) {
 		ResultSet result;
 		Record s;
 		try {
 			result = executeQuery("SELECT * FROM " + dataTable + " WHERE ID = " + ID + ";");
 			result.first();
 			s = loadRecordData(result, ID);
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return null;
 		}
 		return s;
 	}
 
 	public void saveRecord(Record record) {
 		Field f;
 		StringBuilder builder = new StringBuilder(update + dataTable + " " + set);
 		for (int i = 0; i < record.getFieldListSize(); i++) {
 			f = record.getField(i);
 			builder.append("`").append(fieldPrefix).append(f.getName()).append("` = ");
 			if (f.getData() != null) {
 				builder.append("'").append(f.getData()).append("', ");
 			} else {
 				builder.append("null, ");
 			}
 		}
 		Book b;
 		for (int i = 0; i < record.getBookListSize(); i++) {
 			b = record.getBook(i);
 			builder.append(b.getSaveString()).append(", ");
 		}
 		builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(" ") + 1);
 		builder.append(" ").append(where).append("`ID` = ").append(record.getID()).append(";");
 		try {
 			runStatement(builder.toString());
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public Record newRecord() {
 		StringBuilder builder = new StringBuilder(insert + into + dataTable + " (`" + fieldPrefix + Record.getMasterField(0).getName() + "`) ");
 		builder.append(values).append(" (").append(Record.getMasterField(0).getData()).append(");");
 		int ID;
 		try {
 			ID = runStatement(builder.toString());
 			return new Record(ID);
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return null;
 		}
 	}
 
 	public DefaultListModel<Listing> getRecordListingsAsDefaultListModel() {
 		ResultSet resultSet;
 		DefaultListModel<Listing> listModel = new DefaultListModel<>();
 		Listing r;
 		try {
 			resultSet = executeQuery(select + "ID" + ", " + "`" + fieldPrefix + Record.getMasterField(0).getName()
 					+ "`" + ", " + "`" + fieldPrefix + Record.getMasterField(1).getName() + "`" + " " + from + dataTable + ";");
 			printResultSet(resultSet);
 			resultSet.beforeFirst();
 			while (resultSet.next()) {
 				r = new Listing(resultSet.getInt("ID"),
 						resultSet.getNString(fieldPrefix + Record.getMasterField(0).getName()),
 						resultSet.getNString(fieldPrefix + Record.getMasterField(1).getName()));
 				listModel.addElement(r);
 			}
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 		return listModel;
 	}
 
 	public void reconnectDatabase() {
 		if (h2DatabaseConnection != null) {
 			closeDatabase();
 		}
 		createDatabaseConnection();
 	}
 
 	private void createDatabaseConnection() {
 		JdbcDataSource dataSource = new JdbcDataSource();
 		dataSource.setURL("jdbc:h2:" + storagePath + databaseName + ";DB_CLOSE_ON_EXIT=FALSE");
 		dataSource.setUser("sa");
 		dataSource.setPassword("sa");
 		connectToDatabase(dataSource);
 	}
 
 	private void connectToDatabase(JdbcDataSource ds) {
 		try {
 			h2DatabaseConnection = ds.getConnection();
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			JOptionPane.showMessageDialog(new JFrame(), databaseLocked,
 					"Database Locked",
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(-2000);
 		}
 	}
 
 	public int runStatement(String sqlstmt) {
 		int ID = 0;
 
 		Logger.getLogger("global").log(Level.INFO, null, sqlstmt);
 		PreparedStatement stmt;
 		try {
 			stmt = h2DatabaseConnection.prepareStatement(sqlstmt, Statement.RETURN_GENERATED_KEYS);
 			stmt.executeUpdate();
 			ResultSet keys = stmt.getGeneratedKeys();
 			if (keys.next()) {
 				ID = keys.getInt(1);
 			}
 			stmt.close();
 		Logger.getLogger("global").log(Level.INFO, "Status: Success");
 			return ID;
 		} catch (SQLException ex) {
 		Logger.getLogger("global").log(Level.INFO, "Status: Failed");
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return ID;
 		}
 	}
 
 	public ResultSet executeQuery(String sqlstmt) throws Exception {
 		Logger.getLogger("global").log(Level.INFO, sqlstmt);
 		try {
 			ResultSet result = queryDatabase(sqlstmt);
 		Logger.getLogger("global").log(Level.INFO, "Status: Success");
 			return result;
 		} catch (Exception ex) {
 		Logger.getLogger("global").log(Level.INFO, "Status: Failed");
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			throw ex;
 		}
 	}
 
 	private ResultSet queryDatabase(String sqlstmt) throws Exception {
 		Statement select = h2DatabaseConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		ResultSet result = select.executeQuery(sqlstmt);
 		return result;
 	}
 
 	public void printResultSet(ResultSet result) {
 		try {
 			printQueryResults(result);
 			result.first();
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 	}
 
 	private void printQueryResults(ResultSet result) throws SQLException {
 		int rownum = 0;
 		ResultSetMetaData resultMetaData = result.getMetaData();
 		int numberOfColumns = resultMetaData.getColumnCount();
 		System.out.println("Got results:");
 		while (result.next()) { // process results one row at a time
 			System.out.print(" Row " + rownum + " | ");
 			for (int i = 1; i <= numberOfColumns; i++) {
 				System.out.print(resultMetaData.getColumnName(i) + " : " + result.getString(i));
 				if (i < numberOfColumns) {
 					System.out.print(", ");
 				}
 			}
 			System.out.println();
 			rownum++;
 		}
 	}
 
 	public void closeDatabase() {
 		try {
 			h2DatabaseConnection.close();
 			h2DatabaseConnection = null;
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 	}
 
 	private boolean isDatabaseGenerated() {
 		String statement = select + "1 " + from + dataTable + ";";
 		boolean empty = false;
 		try {
 			executeQuery(statement);
 		} catch (Exception ex) {
 			empty = true;
 		}
 		if (empty) {
 			return false;
 		}
 		return true;
 	}
 
 	private void updateDatabase() {
 		if (!tableExists(dataTable)) {
 			createTable();
 		}
 	}
 
 	//TODO: fix this method and add to updateDatabase() so database can be changed based on a easily editable yml file.
 	private void updateTable() {
 		Field f;
 		String name, storageType, defaultValue, sectionName;
 		ResultSet result = getColumnProperties(dataTable);
 		for (int i = 0; i < Record.getMasterFieldListSize(); i++) {
 			f = Record.getMasterField(i);
 			name = f.getName();
 			storageType = f.getStorageType();
 			defaultValue = f.getData();
 			if (!(resultSetContainsNameDataTypeAndDefaultValue(fieldPrefix + name, storageType, defaultValue, result) >= 0)) {
 				addColumn(name, storageType, defaultValue);
 			}
 		}
 		for (int i = 0; i < Book.bookNames.length; i++) {
 			name = Book.bookNames[i];
 			if (!(resultSetContainsNameAndDataType(bookPrefix + name + completedPostfix, bool, result) >= 0)) {
 				addColumn(bookPrefix + name + completedPostfix, bool, "FALSE");
 			}
 			if (!(resultSetContainsNameAndDataType(bookPrefix + name + datePostfix, varchar100, result) >= 0)) {
 				addColumn(bookPrefix + name + datePostfix, varchar100, "FALSE");
 			}
 			for (int j = 0; j < Book.bookSections[i].length; j++) {
 				sectionName = Book.bookSections[i][j];
 				if (!(resultSetContainsNameAndDataType(bookPrefix + name + "_" + sectionName + completedPostfix, bool, result) >= 0)) {
 					addColumn(bookPrefix + name + "_" + sectionName + completedPostfix, bool, "FALSE");
 				}
 				if (!(resultSetContainsNameAndDataType(bookPrefix + name + "_" + sectionName + datePostfix, varchar100, result) >= 0)) {
 					addColumn(bookPrefix + name + "_" + sectionName + datePostfix, varchar100, "FALSE");
 				}
 			}
 		}
 	}
 
 	private void addColumn(String columnName, String columnType, String defaultValue) {
 		String query = altar + table + "\"" + dataTable + "\" ADD " + columnName + " " + columnType + " DEFAULT \"" + defaultValue + "\";";
 		runStatement(query);
 	}
 
 	private void createTable() {
 		Field f;
 		StringBuilder builder = new StringBuilder(create + table + dataTable + " ( ");
 		builder.append("`ID` IDENTITY(1,1)");
 		for (int i = 0; i < Record.getMasterFieldListSize(); i++) {
 			f = Record.getMasterField(i);
 			builder.append(", `").append(fieldPrefix).append(f.getName()).append("` ");
 			builder.append(f.getStorageType());
 			if (f.getData() != null) {
 				builder.append(" DEFAULT ").append("'").append(f.getData()).append("'");
 			} else {
 				builder.append(" DEFAULT ").append(f.getData());
 			}
 		}
 		for (int i = 0; i < Book.bookNames.length; i++) {
 			builder.append(", `").append(bookPrefix).append(Book.bookNames[i]);
 			builder.append(completedPostfix).append("` ");
 			builder.append(bool);
 			builder.append(" DEFAULT ").append("FALSE");
 			builder.append(", `").append(bookPrefix).append(Book.bookNames[i]);
 			builder.append(datePostfix).append("` ");
 			builder.append(varchar100);
 			builder.append(" DEFAULT ").append("NULL");
 			for (int j = 0; j < Book.bookSections[i].length; j++) {
 				builder.append(", `").append(bookPrefix).append(Book.bookNames[i]).append("_");
 				builder.append(Book.bookSections[i][j]).append(completedPostfix).append("` ");
 				builder.append(bool);
 				builder.append(" DEFAULT ").append("FALSE");
 				builder.append(", `").append(bookPrefix).append(Book.bookNames[i]).append("_");
 				builder.append(Book.bookSections[i][j]).append(datePostfix).append("` ");
 				builder.append(varchar100);
 				builder.append(" DEFAULT ").append("NULL");
 			}
 		}
 		builder.append(", PRIMARY KEY (ID) );");
 		runStatement(builder.toString());
 
 	}
 
 	public boolean tableExists(String table) {
 		ResultSet s;
 		try {
 			s = executeQuery("select * from information_schema.tables where table_name = '" + dataTable + "'");
			if (s.first()) {
				return true;
			}
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 		return false;
 	}
 
 	public ResultSet getColumnProperties(String table) {
 		try {
 			printResultSet(h2DatabaseConnection.getMetaData().getColumns(null, null, dataTable, null));
 			ResultSet r = h2DatabaseConnection.getMetaData().getColumns(null, null, dataTable, null);
 			return r;
 		} catch (Exception ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return null;
 		}
 	}
 
 	public String setDefaultValue(String fieldName) {
 		throw new UnsupportedOperationException();
 	}
 
 	public String getDefaultValue(int rowNumber, ResultSet results) {
 		String s;
 		try {
 			results.absolute(rowNumber);
 			s = results.getString(13);
 			return s;
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return null;
 		}
 	}
 
 	public int resultSetContainsNameDataTypeAndDefaultValue(String fieldName, String fieldDataType, String defaultValue, ResultSet results) {
 		boolean contains = false;
 		int containsInRow = resultSetContainsNameAndDataType(fieldName, fieldDataType, results);
 		if (containsInRow >= 0) {
 			try {
 				results.absolute(containsInRow);
 				contains = defaultValue.equals(results.getString("COLUMN_DEFAULT"));
 				if (contains) {
 					return containsInRow;
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			}
 		}
 		return -1;
 	}
 
 	public int resultSetContainsNameAndDataType(String fieldName, String fieldDataType, ResultSet results) {
 		boolean contains = false;
 		int containsInRow = resultSetContainsName(fieldName, results);
 		if (containsInRow >= 0) {
 			try {
 				results.absolute(containsInRow);
 				contains = fieldDataType.equals(results.getString("TYPE_NAME"));
 				if (contains) {
 					return containsInRow;
 				}
 			} catch (SQLException ex) {
 				Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			}
 		}
 		return -1;
 	}
 
 	public int resultSetContainsName(String fieldName, ResultSet results) {
 		int contains = -1;
 		try {
 			int i = 0;
 			results.beforeFirst();
 			while (results.next()) {
 				if (fieldName.equals(results.getString("COLUMN_NAME"))) {
 					return results.getRow();
 				}
 			}
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 		}
 		return contains;
 	}
 
 	private Record loadRecordData(ResultSet result, int ID) {
 		Record s = new Record(ID);
 		ArrayList<Field> fields = new ArrayList<>();
 		Field master;
 		Field f;
 		try {
 			result.first();
 			for (int i = 0; i < Record.getMasterFieldListSize(); i++) {
 				master = Record.getMasterField(i);
 				f = new Field(master.getName(), result.getString(fieldPrefix + master.getName()), master.getType(), master.getDisplayLength());
 				fields.add(f);
 			}
 			result.first();
 		} catch (SQLException ex) {
 			Logger.getLogger("global").log(Level.SEVERE, null, ex);
 			return null;
 		}
 		s.setFieldList(fields);
 		s.setBookList(loadBookDataAsArrayList(result));
 		return s;
 	}
 
 	private ArrayList<Book> loadBookDataAsArrayList(ResultSet result) {
 		ArrayList<Book> bookList = new ArrayList<>();
 		ArrayList<Section> sections;
 		Section section;
 		Book book;
 		boolean isCompleted;
 		String date;
 
 		for (int i = 0; i < Book.bookNames.length; i++) {
 			sections = new ArrayList<>();
 			for (int j = 0; j < Book.bookSections[i].length; j++) {
 				try {
 					isCompleted = result.getBoolean(bookPrefix + Book.bookNames[i] + "_" + Book.bookSections[i][j] + completedPostfix);
 					date = result.getString(bookPrefix + Book.bookNames[i] + "_" + Book.bookSections[i][j] + datePostfix);
 					section = new Section(Book.bookSections[i][j], isCompleted, date);
 					sections.add(section);
 				} catch (SQLException ex) {
 					Logger.getLogger("global").log(Level.SEVERE, null, ex);
 					return null;
 				}
 			}
 			try {
 				isCompleted = result.getBoolean(bookPrefix + Book.bookNames[i] + completedPostfix);
 				date = result.getString(bookPrefix + Book.bookNames[i] + datePostfix);
 			} catch (SQLException ex) {
 				Logger.getLogger("global").log(Level.SEVERE, null, ex);
 				return null;
 			}
 			book = new Book(Book.bookNames[i], sections, isCompleted, date);
 			bookList.add(book);
 		}
 		return bookList;
 	}
 }
 /*
 
 	public void createTable(String collumnName) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void addCollumn(String collumnName) {
 		throw new UnsupportedOperationException();
 	}
 	 public ArrayList<String> getStudentNames(){
 	 throw new UnsupportedOperationException();
 	 }
 	 public ArrayList<Student> getStudents(){
 	 throw new UnsupportedOperationException();
 	 }
 	 public void saveStudents(ArrayList<Student> students){
 	 throw new UnsupportedOperationException();
 	 }
 	 public Book getStudentBook(int ID, String bookName){//TODO: seperate main table into sections using book name prefixes.
 	 throw new UnsupportedOperationException();
 	 }
 	 public ArrayList<String> getCompletedBooks(String studentID){
 	 throw new UnsupportedOperationException();
 	 }
 
 	public ArrayList<String> getBookList() {
 		throw new UnsupportedOperationException();
 	}
 
 	public ArrayList<Record> getRecordsAsArrayList() {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}*/
