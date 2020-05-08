 package lib.PatPeter.SQLibrary;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 public class H2 extends DatabaseHandler {
 	public String location;
 	public String name;
	private File sqlFile;
 
 	public H2(Logger log, String prefix, String name, String location) {
 		super(log, prefix, "[H2] ");
 		this.name = name;
 		this.location = location;
 		File folder = new File(this.location);
 		if (this.name.contains("/") ||
 				this.name.contains("\\") ||
				this.name.endsWith(".h2")) {
			this.writeError("The database name can not contain: /, \\, or .h2", true);
 		}
 		if (!folder.exists()) {
 			folder.mkdir();
 		}
 
		sqlFile = new File(folder.getAbsolutePath() + File.separator + name + ".h2");
 	}
 
 	@Override
 	protected boolean initialize() {
 		try {
 			Class.forName("org.h2.Driver");
 			return true;
 		} catch (ClassNotFoundException e) {
 			this.writeError("You need the H2 library " + e, true);
 			return false;
 		}
 	}
 
 	@Override
 	public Connection open() {
 		if (initialize()) {
 			try {
 				return DriverManager.getConnection("jdbc:h2:file:" +
						sqlFile.getAbsolutePath()+";MODE=MYSQL;IGNORECASE=TRUE;AUTO_SERVER=TRUE");
 			} catch (SQLException e) {
 				this.writeError("SQLite exception on initialize " + e, true);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void close() {
 		Connection connection = this.open();
 		if (connection != null){
 			try {
 				connection.close();
 			} catch (SQLException ex) {
 				this.writeError("Error on Connection close: " + ex, true);
 			}
 		}
 	}
 
 	@Override
 	public Connection getConnection() {
 		if (this.connection == null)
 			return open();
 		return this.connection;
 	}
 
 	@Override
 	public boolean checkConnection() {
 		Connection connection = this.open();
 		if (connection != null)
 			return true;
 		return false;
 	}
 
 	@Override
 	public ResultSet query(String query) {
 		Connection connection = null;
 		Statement statement = null;
 		ResultSet result = null/*new JdbcRowSetImpl()*/;
 		try {
 			connection = open();
 			statement = connection.createStatement();
 			result = statement.executeQuery("SELECT CURTIME()");
 
 			switch (this.getStatement(query)) {
 			case SELECT:
 				result = statement.executeQuery(query);
 				return result;
 
 			default:
 				statement.executeUpdate(query);
 				return result;
 			}
 		} catch (SQLException ex) {
 			if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
 				return retryResult(query);
 				//this.writeError("",false);
 			} else {
 				this.writeError("Error at SQL Query: " + ex.getMessage(), false);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public PreparedStatement prepare(String query) {
 		Connection connection = null;
 		PreparedStatement ps = null;
 		try
 		{
 			connection = open();
 			ps = connection.prepareStatement(query);
 			return ps;
 		} catch(SQLException e) {
 			if(!e.toString().contains("not return ResultSet"))
 				this.writeError("Error in SQL prepare() query: " + e.getMessage(), false);
 		}
 		return ps;
 	}
 
 	@Override
 	public boolean createTable(String query) {
 		Statement statement = null;
 		try {
 			this.connection = this.open();
 			if (query.equals("") || query == null) {
 				this.writeError("SQL query empty: createTable(" + query + ")", true);
 				return false;
 			}
 
 			statement = connection.createStatement();
 			statement.execute(query);
 			return true;
 		} catch (SQLException e) {
 			this.writeError(e.getMessage(), true);
 			return false;
 		} catch (Exception e) {
 			this.writeError(e.getMessage(), true);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean checkTable(String table) {
 		try {
 			Connection connection = open();
 			//this.connection = this.open();
 			Statement statement = connection.createStatement();
 
 			ResultSet result = statement.executeQuery("SELECT * FROM " + table);
 
 			if (result == null)
 				return false;
 			if (result != null)
 				return true;
 		} catch (SQLException e) {
 			if (e.getMessage().contains("exist")) {
 				return false;
 			} else {
 				this.writeError("Error in SQL query: " + e.getMessage(), false);
 			}
 		}
 
 
 		if (query("SELECT * FROM " + table) == null) return true;
 		return false;
 	}
 
 	@Override
 	public boolean wipeTable(String table) {
 		//Connection connection = null;
 		Statement statement = null;
 		String query = null;
 		try {
 			if (!this.checkTable(table)) {
 				this.writeError("Error wiping table: \"" + table + "\" does not exist.", true);
 				return false;
 			}
 			//connection = open();
 			this.connection = this.open();
 			statement = this.connection.createStatement();
 			query = "DELETE FROM " + table + ";";
 			statement.executeUpdate(query);
 
 			return true;
 		} catch (SQLException ex) {
 			if (!(ex.getMessage().toLowerCase().contains("locking") ||
 					ex.getMessage().toLowerCase().contains("locked")) &&
 					!ex.toString().contains("not return ResultSet"))
 						this.writeError("Error at SQL Wipe Table Query: " + ex, false);
 				return false;
 		}
 	}
 
 	/*
 	 * <b>retry</b><br>
 	 * <br>
 	 * Retries.
 	 * <br>
 	 * <br>
 	 * @param query The SQL query.
 	 */
 	public void retry(String query) {
 		boolean passed = false;
 		Connection connection = open();
 		Statement statement = null;
 
 		while (!passed) {
 			try {
 				statement = connection.createStatement();
 				statement.executeQuery(query);
 				passed = true;
 			} catch (SQLException ex) {
 				if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked") ) {
 					passed = false;
 				} else {
 					this.writeError("Error at SQL Query: " + ex.getMessage(), false);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Retries a result.
 	 * 
 	 * @param query The SQL query to retry.
 	 * @return The SQL query result.
 	 */
 	public ResultSet retryResult(String query) {
 		boolean passed = false;
 		Connection connection = open();
 		Statement statement = null;
 		ResultSet result = null;
 
 		while (!passed) {
 			try {
 				statement = connection.createStatement();
 				result = statement.executeQuery(query);
 				passed = true;
 				return result;
 			} catch (SQLException ex) {
 				if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
 					passed = false;
 				} else {
 					this.writeError("Error at SQL Query: " + ex.getMessage(), false);
 				}
 			}
 		}
 
 		return null;
 	}
 
 }
