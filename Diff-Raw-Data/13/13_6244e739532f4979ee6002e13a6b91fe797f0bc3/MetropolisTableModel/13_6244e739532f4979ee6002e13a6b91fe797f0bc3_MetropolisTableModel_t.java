 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.swing.table.AbstractTableModel;
 
 import com.mysql.jdbc.ResultSetImpl;
 
 public class MetropolisTableModel extends AbstractTableModel {
 		private static final long serialVersionUID = 1L;
 		
 		public static final int METROPOLIS = 0;
 		public static final int CONTINENT = 1;
 		public static final int POPULATION = 2;
 		public static final String[] COLUMNS = {"Metropolis", "Continent", "Population"};
 		
 		private ResultSet rs;
 		private Connection conn;
 		
 		/**
 		 * Default constructor for MetropolisTableModel.
 		 * Calls super() and establishes a connection to the 
 		 * MySQL database.  If the connection fails, exits.
 		 */
 		public MetropolisTableModel() {
 			super();
 			emptyResultSet();
 			try{
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch(ClassNotFoundException e) {
 				System.err.println("JDBC Class not found");
 				System.err.println(e.getMessage());
 				System.exit(-1);
 			}
 			try {
 				conn = DriverManager.getConnection(
 						"jdbc:mysql://" + SERVER, ACCOUNT, PASSWORD);
 			} catch (SQLException e) {
 				System.err.println("Could not get a connection from MySQL.");
 				System.exit(-1);
 			}
 		}
 
 		/**
 		 * Returns the number of columns in the model.
 		 *  A JTable uses this method to determine how many columns it 
 		 *  should create and display by default.
 		 *  @return the number of columns in the model
 		 */
 		public int getColumnCount() { 
 			return COLUMNS.length;
 		}
 		
 		/**
 		 * Returns a default name for the column using spreadsheet conventions: 
 		 * A, B, C, ... Z, AA, AB, etc. If column cannot be found, returns an empty string.
 		 * @param column the column being queried
 		 * @return a string containing the default name of column
 		 */
 		@Override
 		public String getColumnName(int column) {
 			return COLUMNS[column];
 		}
 
 		/**
 		 * Returns the number of rows in the model. A JTable uses this method to determine how many rows it should display. 
 		 * This method should be quick, as it is called frequently during rendering.
 		 * @return the number of rows in the model
 		 */
 		public int getRowCount() {
 			try {
 				rs.last();
 				return rs.getRow();
 			} catch(SQLException e) {
 				return 0;
 			}
 		}
 
 		/**
 		 * Returns the value for the cell at columnIndex and rowIndex.
 		 * @param rowIndex the row whose value is to be queried
 		 * @param columnIndex the column whose value is to be queried
 		 * @return the value Object at the given cell
 		 */
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			try{
 				rs.first();
 				while(rs.getRow() < rowIndex + 1) { 
 					rs.next();
 				}
 				switch(columnIndex) {
 				case METROPOLIS:
 					return rs.getString("metropolis");
 				case CONTINENT:
 					return rs.getString("continent");
 				case POPULATION:
 					return rs.getInt("population");
 				default:
 					return "";
 				}
 			} catch (SQLException e) {
 				return "";
 			}		
 		}
 		
 		/**
 		 * Add a metropolis to the database with the given arguments.
 		 * @param metropolis Name of the metropolis. If blank, enters a null value.
 		 * @param continent Name of the continent. If blank, enters a null value.
 		 * @param population Population. If blank or not a valid integer, enters a null value.
 		 */
 		public void add(String metropolis, String continent, String population) {
 			try{
 				Integer.parseInt(population);
 			} catch (NumberFormatException e) {
 				population = "";
 			}
 			String insert = getAddQuery(metropolis, continent, population);
 			String select = getSearchQuery(metropolis, continent, population,
 					PopulationSearchOptions.EQUAL, MatchSearchOptions.EXACT); 
 			try {
 				executeInsert(insert);
 				rs = executeSelect(select);
 				fireTableDataChanged();
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 				emptyResultSet();
 			}
 		}
 		
 		/**
 		 * Search the database with the given arguments.
 		 * @param metropolis Name of the metropolis. If blank, enters a null value.
 		 * @param continent Name of the continent. If blank, enters a null value.
 		 * @param population Population. If blank or not a valid integer, enters a null value.
 		 * @param pso Set how to compare database entries to the entered population.
 		 * @param mso Set whether to require matches to be exact matches.
 		 */
 		public void search(String metropolis, String continent, String population,
 						   PopulationSearchOptions pso, MatchSearchOptions mso) {
 			try{ 
 				Integer.parseInt(population);
 			} catch (NumberFormatException e) {
 				population = "";
 			}
 			try {
 				String select = getSearchQuery(metropolis, continent,
 											   population, pso, mso);
 
 				rs = executeSelect(select);
 				fireTableDataChanged();
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 				emptyResultSet();
 			}
 		}	
 		
 		/**
 		 * Convert parameters into a valid SQL INSERT statement.
 		 * @param metropolis Name of the metropolis. If blank, enters a null value.
 		 * @param continent Name of the continent. If blank, enters a null value.
 		 * @param population Population. If blank or not a valid integer, enters a null value.
 		 * @return INSERT query.
 		 */
 		private static String getAddQuery(String metropolis, String continent, String population) {
 			metropolis = sanitizeSQL(metropolis);
 			continent = sanitizeSQL(continent);
 			population = sanitizeSQL(population);
 			
 			StringBuilder sb = new StringBuilder();
 			sb.append("INSERT INTO metropolises VALUES (");
 			metropolis = (metropolis.trim().length() == 0) ? "null" : ("\"" + metropolis + "\"");
 			sb.append(metropolis + ", ");
 			continent = (continent.trim().length() == 0) ? "null" : ("\"" + continent + "\"");
 			sb.append(continent + ", ");
 			population = (population.trim().length() == 0) ? "null" : ("\"" + population + "\"");
 			sb.append(population + ");");		
 			return sb.toString();
 		}
 		
 		/**
 		 * Convert parameters into a valid SQL SELECT statement.
 		 * @param metropolis Name of the metropolis. If blank, enters a null value.
 		 * @param continent Name of the continent. If blank, enters a null value.
 		 * @param population Population. If blank or not a valid integer, enters a null value.
 		 * @param pso Set how to compare database entries to the entered population.
 		 * @param mso Set whether to require matches to be exact matches.
 		 * @return SELECT query.
 		 */
 		private static String getSearchQuery(String metropolis, String continent, String population,
 									  PopulationSearchOptions pso, MatchSearchOptions exact) {
 			metropolis = sanitizeSQL(metropolis);
 			continent = sanitizeSQL(continent);
 			population = sanitizeSQL(population);
 			
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM metropolises");
 			Integer count = 0;
 			if(metropolis.trim().length() != 0) {
 				sb.append((count == 0) ? " WHERE " : " AND ");
 				count++;
 				if(exact == MatchSearchOptions.EXACT) { sb.append("metropolis = \"" + metropolis + "\""); }
 				else {      sb.append("metropolis LIKE \"%" + metropolis + "%\""); }	
 			}
 			if(continent.trim().length() != 0) {
 				sb.append((count == 0) ? " WHERE " : " AND ");
 				count++;
 				if(exact == MatchSearchOptions.EXACT) { sb.append("continent = \"" + continent + "\""); }
 				else {      sb.append("continent LIKE \"%" + continent + "%\""); }
 			}
 			if(population.trim().length() != 0) {
 				sb.append((count == 0) ? " WHERE " : " AND ");
 				count++;
 				if(pso == PopulationSearchOptions.EQUAL) { sb.append("population = "); }
 				else if(pso == PopulationSearchOptions.SMALLER){ sb.append("population < "); }
 				else if(pso == PopulationSearchOptions.LARGER) { sb.append("population > "); }
 				sb.append(population);
 			}
 			sb.append(";");			
 			return sb.toString();
 		}
 		
 		/**
 		 * Sanitize string parameters for use in SQL queries by removing 
 		 * the characters \ " ' % _ ; . ( ) [ ]
 		 * @param s String to sanitize.
 		 * @return Cleaned version of string.
 		 */
 		public static String sanitizeSQL(String s) {
 			return s.replaceAll("[\\\\\"\'%_;\\.\\(\\)\\[\\]]", "");
 		}
 
 		private static final String SERVER = "mysql-user.stanford.edu";
 		private static final String ACCOUNT = "ccs108samath";
 		private static final String PASSWORD = "fahmohpa";
 		private static final String DATABASE = "c_cs108_samath";
 		
 		/**
 		 * Get a Statement from the connection and execute the given query.
 		 * @param query SELECT statement. Should not update the database.
 		 * @return ResultSet from java#sql#Statement#executeQuery.
 		 * @throws SQLException
 		 */
 		private ResultSet executeSelect(String query) throws SQLException {
 			Statement stmt = conn.createStatement();
 			stmt.executeQuery("USE " + DATABASE);
 			ResultSet set = stmt.executeQuery(query);
 			return set;
 		}
 		
 		/**
 		 * Get a Statement from the connection and execute the given query.
 		 * @param query INSERT statement.
 		 * @throws SQLException
 		 */
 		private void executeInsert(String query) throws SQLException {
 			Statement stmt = conn.createStatement();
 			stmt.executeQuery("USE " + DATABASE);
 			stmt.executeUpdate(query);
 		}
 		
 		/**
 		 * Empty Table Model by replacing local ResultSet with new empty set.
 		 * Used for initialization and handling malformed queries.
 		 */
 		private void emptyResultSet() {
 			rs = new ResultSetImpl(0, 0, null, null);
 		}
 		
 		public enum PopulationSearchOptions { 
 			EQUAL(0), LARGER(1), SMALLER(2);
 			private int index;
 			private PopulationSearchOptions(int i) { index = i; }
 			public int index() { return index; }
 		}
 		public enum MatchSearchOptions {
 			EXACT(0), PARTIAL(1);
 			private int index;
 			private MatchSearchOptions(int i) { index = i; }
 			public int index() { return index; }
 		}
 
 }
