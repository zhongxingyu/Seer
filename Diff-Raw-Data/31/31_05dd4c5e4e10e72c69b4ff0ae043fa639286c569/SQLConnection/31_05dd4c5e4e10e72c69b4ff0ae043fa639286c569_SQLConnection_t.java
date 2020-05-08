 package taberystwyth.db;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 
 import javax.swing.JOptionPane;
 
 import org.ibex.nestedvm.UnixRuntime.CygdriveFS;
 
 import taberystwyth.view.OverviewFrame;
 
 public class SQLConnection {
 
 	private static SQLConnection instance = new SQLConnection();
 
 	public static SQLConnection getInstance() {
 		return instance;
 	}
 
 	File dbfile;
 	
 	private Connection conn;
 
 	private SQLConnection() {		
 		/*
 		 * Ensure that the SQL driver is pulled through the class loader
 		 */
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			panic(e, "Unable to load the database driver."
 					+ "Perhaps your computer architecture is not supported?");
 		}
 
 		/*
 		 * Load the database FIXME: the user should be consulted about which one
 		 */
 		setDatabase(new File("taberystwyth.tab"));
 		
 
 		/*
 		 * If the tables don't already exist, load them.
 		 */
 		@SuppressWarnings("serial")
 		HashSet<String> expected = new HashSet<String>() {
 			{
				add("JUDGE");
 				add("LOCATION");
 				add("PANEL");
 				add("RESULTS");
 				add("ROOM");
 				add("SPEAKER");
 				add("SPEAKER_POINTS");
 				add("TEAM");
 				add("SQLITE_AUTOINDEX_PANEL_1"); // automatic
				add("SQLITE_AUTOINDEX_LOCATION_1");
				add("SQLITE_AUTOINDEX_RESULTS_1");
				add("SQLITE_SEQUENCE");
 			}
 		};
 		HashSet<String> actual = new HashSet<String>();
 
 		/*
 		 * See what tables we currently have
 		 */
 		ResultSet rs = null;
 		try {
 			rs = conn.getMetaData().getTables(null, null, null, null);
 			while (rs.next()) {
				System.out.println("SQLConnection(): Found table - " + 
						rs.getString("TABLE_NAME"));
 				actual.add(rs.getString("TABLE_NAME"));
 			}
 		} catch (SQLException e) {
 			panic(e, "Unable to check what tables are already in the database");
 		}
 
 		/*
 		 * If we don't have the tables, we need to create them, using data/* .
 		 */
 		if (!actual.equals(expected)) {
 			String[] sqlFiles = { "location", "panel", "results", "room",
					"speaker", "speaker_points", "team", "judge" };
 			for (String s : sqlFiles) {
 				evaluateSQLFile("data/" + s + ".sql");
 			}
 		}
 
 		try {
 			rs.close();
 		} catch (SQLException e) {
 			panic(e, "Unable to close a resultset.");
 		}
 	}
 
 	/**
 	 * Evaluates a given SQL file against the given connection.
 	 * 
 	 * @param conn
 	 *            connection to SQL database
 	 * @param filePath
 	 *            path of SQL file
 	 * @throws IOException
 	 *             if the file is not found
 	 * @throws SQLException
 	 *             if there is some problem with the SQL server
 	 */
 	private synchronized void evaluateSQLFile(String filePath) {
		System.out.println("SQLConnection.evaluateSQLFile: evaluating - " + filePath);
 		char[] cbuf = new char[2000];
 		try {
 			new BufferedReader(new FileReader(new File(filePath))).read(cbuf);
 			conn.createStatement().execute(new String(cbuf));
 		} catch (Exception e) {
 			panic(e,
 					"Unable to evaluate this file:\n"
 							+ new File(filePath).getAbsolutePath());
 		}
 	}
 
 	/**
 	 * There are so many instances where an unfixable situation arises in this
 	 * file that I have defined this shorthand.
 	 * 
 	 * @param e
 	 *            an exception
 	 * @param reason
 	 *            a Very Informative Description of the problem
 	 */
 	public synchronized void panic(Exception e, String reason) {
 		JOptionPane.showMessageDialog(OverviewFrame.getInstance(), reason);
 		e.printStackTrace();
 		System.exit(255);
 	}
 
 	/**
 	 * Execute an SQL statement against the database
 	 * 
 	 * @param statement
 	 *            statement
 	 * @return success or failure
 	 */
 	public synchronized boolean execute(String statement) {
 		boolean returnValue = false;
 		try { 	
 			/*
 			 * Order seems to be important here because the semantics of sqlite are
 			 * quite odd.  Closing the connection is required in order to flush updates
 			 * to the database file
 			 */
 			Statement stmt = conn.createStatement();
 			returnValue = stmt.execute(statement);
 			stmt.close();
 			conn.close();
 			conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ dbfile.getAbsolutePath()); 
 		} catch (SQLException e) {
 			panic(e, "Unable to execute this statement against the database:\n"
 					+ statement);
 		}
 		return returnValue;
 	}
 
 	/**
 	 * Execute an SQL query against the database
 	 * 
 	 * @param query
 	 *            query
 	 * @return resultset
 	 */
 	public synchronized ResultSet executeQuery(String query) {
 		ResultSet returnValue = null;
 		try {
 			Statement stmt = conn.createStatement();
 			returnValue = stmt.executeQuery(query);
 		} catch (SQLException e) {
 			panic(e, "Unable to execute this query against the database:\n"
 					+ query);
 		}
 		return returnValue;
 	}
 
 	private synchronized void setDatabase(File file) {
 		try {
 			dbfile = file;
 			conn = DriverManager.getConnection("jdbc:sqlite:"
 					+ file.getAbsolutePath());
 		} catch (SQLException e) {
 			panic(e, "Unable to load the following file:\n" + file);
 		}
 	}
 
 }
