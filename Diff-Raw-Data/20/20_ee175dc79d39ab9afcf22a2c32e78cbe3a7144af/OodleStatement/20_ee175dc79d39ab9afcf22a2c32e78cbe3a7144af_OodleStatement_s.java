 package oodles.DBC;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import oodles.RMICommon.ColumnSchema;
 
 /**
  * 
  * 
  * <strong>JDBC DISCLAIMER:</strong>
  * 
  * <p>
  * The packages java.sql and javax.sql are collectively known as 
  * "JDBC"
  * </p>
  * 
  * <p>
  * JDBC's interfaces include many, many methods which provide lots of
  * wonderfully intricate functionality which have been deemed totally
  * unecessary for OodleDBC. These methods will throw SQLExceptions when
  * invoked, and have been marked as "Unsupported"
  * </p>
  * 
  * @author mitch
  *
  */
 public class OodleStatement implements Statement {
 	
 
 	/** The connection object that created this OodleStatement instance */
 	protected OodleConnection parentConnection;
 	
 	/** Caches the ResultSet from the last query we executed, or is null if the last query didn't have a ResultSet */ 
 	protected ResultSet currentResultSet; 
 
 	/** A pattern that can be used to split a comma seperated list while stripping whitespace around the comma-seperated tokens. */
 	protected static final Pattern COMMA_DELIMITER_PATTERN =
 		Pattern.compile("\\s*,\\s*");
 	
 	/** Matches a comma seprated list of columns names */
 	protected static final Pattern COLUMN_LIST =
 		Pattern.compile("\\w+(?:\\s*,\\s*\\w+)*");
 	
 	/**
 	 * <p>
 	 * 	Matches am UPDATE query's assignment clause
 	 * </p>
 	 * 
 	 * <strong>Groups</strong>
 	 * <ol>
 	 * 	<li>Column Name: The name of the column to assign to</li>
 	 * 	<li>Expression: The expression to asign to that column</li>
 	 * </ol>	 
 	 * 
 	 */
 	protected static final Pattern COLUMN_ASSIGNMENT_CLAUSE_PATTERN =
 		Pattern.compile("(\\w+)\\s*=\\s*(.*)");
 	
 	/** The pattern for a column schema */
 	protected static final Pattern COLUMN_SCHEMA_PATTERN = 
 		Pattern.compile("(\\w+)\\s+(\\w+)");
 	
 	
 	/** The pattern for a USE query */
 	protected static final Pattern USE_QUERY_PATTERN = 
 		Pattern.compile("use\\s+(\\w+)");
 	
 	
 	/** 
 	 * <p>
 	 * The pattern for an ALTER TABLE query 
 	 * </p>
 	 * 
 	 * <strong>Groups</strong>
 	 * <ol>
 	 * 	<li>Table Name: The name of the table to alter</li>
 	 * 	<li>New Column Name: the name of the new column</li>
 	 * 	<li>New Column Type: the type of the new column</li>
 	 * </ol>
 	 * 
 	 */
 	protected static final Pattern ALTER_QUERY_PATTERN = 
 		Pattern.compile("alter\\s+table\\s+(\\w+)\\s+add\\s+column\\s+(\\w+)\\s+(\\w+)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	/**
 	 * <p>
 	 * 	Matches CREATE queries
 	 * </p>
 	 * 
 	 * <strong>Groups</strong>
 	 * <ol>
 	 * 	<li>Database mode: "database" (case insensitive) if this is a CREATE DATABASE query, or null otherwise.</li>
 	 * 	<li>Table mode: "table" (case insenstive) if this is a CREATE TABLE query, or null otherwise.</li>
 	 * 	<li>Create clause: the parameters 
 	 * 
 	 */
 	protected static final Pattern CREATE_QUERY_PATTERN = 
		Pattern.compile("create\\s+(database)|(table)\\s+(.*)",
 		Pattern.CASE_INSENSITIVE);
 	
 	/**
 	 * <p>
 	 * 	Matches SELECT queries
 	 * </p>
 	 * 
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Columns to Select: "*" or comma delimited list of column names</li>
 	 *  <li>Table Name: The table name to select from</li>
 	 *  <li>Where Clause: The where clause </li>
 	 * </ol>
 	 */
 	protected static final Pattern SELECT_QUERY_PATTERN = 
		Pattern.compile("select\\s+(\\*|(?:\\w+(?:\\s*,\\s*\\w+)*))\\s+from\\s+(\\w+)\\s+where\\s+(.*)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	/**
 	 * <p>
 	 * 	Matches DELETE queries.
 	 * </p>
 	 * 
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Table Name: the table to delete from</li>
 	 * 	<li>Where Clause: the conditions on which to delete</li>
 	 * </ol>
 	 */
 	protected static final Pattern DELETE_QUERY_PATTERN = 
 		Pattern.compile("delete\\s+from\\s+(\\w+)\\s+where\\s+(.*)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	/**
 	 * <p>
 	 * 	Matches UPDATE queries
 	 * </p>
 	 * 
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Table Name: the table to update</li>
 	 *  <li>Set Clause: the changes to make to the table</li>
 	 * 	<li>Where Clause: the conditions on which to make the changes</li>
 	 * </ol>
 	 */
 	protected static final Pattern UPDATE_QUERY_PATTERN = 
 		Pattern.compile("update\\s+(\\w+)\\s+set\\s+(.*?)\\s+where\\s+(.*)",
 		Pattern.CASE_INSENSITIVE);
 	
 
 	/**
 	 * <p>
 	 * 	Matches DESCRIBE queries
 	 * </p>	
 	 * 
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Table Name: the table to describe</li>
 	 * </ol>
 	 */
 	protected static final Pattern DESCRIBE_QUERY_PATTERN = 
 		Pattern.compile("describe\\s+(\\w+)",
 		Pattern.CASE_INSENSITIVE);
 	
 
 	/**
 	 * <p>
 	 * 	Matches insert queries
 	 * </p>
 	 * 
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Table Name: the table to describe</li>
 	 * 	<li>Column List: A comma seperated list of columns to be changed</li>
 	 * 	<li>Value List: A comma seperated list of values to assign to the columns</li>
 	 * </ol>
 	 */
 	protected static final Pattern INSERT_QUERY_PATTERN = 
 		Pattern.compile("insert\\s+into\\s+(\\w+)\\s+\\((.*?)\\)\\s+values\\s+\\((.*)\\)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	/**
 	 * <p>Matches DROP queries</p>
 	 *  
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Target type: "table" or "database" (case insensitive), indicating what type of entity should be dropped</li>
 	 * 	<li>Target name: The name of the table or database to drop.</li>
 	 * </ol>
 	 */
 	protected static final Pattern DROP_QUERY_PATTERN = 
 		Pattern.compile("drop\\s+(table|database)\\s+(\\w+)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	
 	/**
 	 * <p>Matches SHOW queries</p>
 	 *  
 	 * <strong>Groups:</strong>
 	 * <ol>
 	 * 	<li>Target type: "tables" or "databases" (case insensitive), indicating what type of entity should be shown</li>
 	 * </ol>
 	 */
 	protected static final Pattern SHOW_QUERY_PATTERN = 
 		Pattern.compile("show\\s+(tables|databases)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	
 	/** The pattern for the table definition clause of a CREATE TABLE query */
 	protected static final Pattern TABLE_DEFINITION_CLAUSE_PATTERN = 
 		Pattern.compile("(\\w+)\\s+(.*)",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 	/** The pattern for the table schema clause of a CREATE TABLE query */
 	protected static final Pattern TABLE_SCHEMA_CLAUSE_PATTERN = 
 		Pattern.compile("(\\w+\\s+\\w+)(,\\s+\\w+\\s+\\w+)*",
 		Pattern.CASE_INSENSITIVE);
 	
 	
 
 	/**
 	 * Constructs an OodleStatement for a particular connection object.
 	 * 
 	 * @param connection The connection object to bind this OodleStatement to.
 	 * 
 	 * @throws IllegalArgumentException if the connection is null.
 	 */
 	public OodleStatement(OodleConnection connection) throws IllegalArgumentException {
 		
 		/* Make sure we have an actual connection */
 		
 		if (connection == null) {
 			throw new IllegalArgumentException("Connection cannot be null. (Read: go fuck yourself.)");
 		}
 		
 		this.parentConnection = connection; 
 
 	}
 	
 	
 	/**
 	 * Returns the ResultSet from the last query, or null if the last
 	 * query didn't return a result set.
 	 * 
 	 * @returns ...
 	 */
 	public ResultSet getResultSet() throws SQLException {
 		return currentResultSet;
 	}
 	
 	
 	/**
 	 * Performs a query on the database.
 	 */
 	public boolean execute(String query) throws SQLException {
 		
 		/* Clear out the current result set. */
 		
 		currentResultSet = null;
 		
 		/* Peek at the query and hand it off to a helper */
 		
 		try {
 		
 			if ( USE_QUERY_PATTERN.matcher(query).matches() ) {
 	
 				executeUseQuery(query);
 				
 			} else if ( ALTER_QUERY_PATTERN.matcher(query).matches() ) {
 				
 				executeAlterQuery(query);
 				
 			} else if ( CREATE_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeCreateQuery(query);
 				
 			} else if ( SELECT_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeSelectQuery(query);
 				
 			} else if ( DELETE_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeDeleteQuery(query);
 				
 			} else if ( UPDATE_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeUpdateQuery(query);
 			
 			} else if ( DESCRIBE_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeDescribeQuery(query);
 			
 			} else if ( INSERT_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeInsertQuery(query);
 			
 			} else if ( DROP_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeDropQuery(query);
 				
 			} else if ( SHOW_QUERY_PATTERN.matcher(query).matches() ) {	
 				
 				executeShowQuery(query);
 				
 			} else {
 				
 				throw new SQLException("Query was malformed.");
 				
 			}
 		
 		} catch (RemoteException e ) {
 		
 			/* If a RemoteException is thrown, RMI had problems and couldn't invoke the remote method */
 			
 			throw new SQLException("Error communicating with database server.");
 			
 		}
 		
 		return true;
 		
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Changes the database
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 */
 	protected void executeUseQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher m = USE_QUERY_PATTERN.matcher(query);
 		
 		/* Make sure we match */
 		
 		if (m.matches() == false) {
 			throw new SQLException("USE query was malformed.");
 		}
 		
 		/* Set the parent connection to use the new database */
 		
 		String databaseName = m.group(1);
 		
 		parentConnection.useDatabase(databaseName);
 		
 	}
 	
 	/**
 	 * Alter query
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 */
 	protected void executeAlterQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher m = ALTER_QUERY_PATTERN.matcher(query);
 		
 		/* Make sure we match */
 		
 		if (m.matches() == false) {
 			throw new SQLException("ALTER query was malformed.");
 		}
 		
 		/* "Alter" the table */
 		
 		String tableName = m.group(1);
 		ColumnSchema columnSchema = new ColumnSchema(m.group(2), m.group(3));
 		
 		parentConnection.getDatabase().alterTable(tableName, columnSchema);
 		
 	}
 	
 	
 	/**
 	 * <p>
 	 * 	Executes a CREATE query.
 	 * </p>
 	 * 
 	 * @param query the query.
 	 * @throws SQLException if there is a problem parsing the query.
 	 */
 	protected void executeCreateQuery(String query) throws SQLException, RemoteException {
 		
 		
 		Matcher m = CREATE_QUERY_PATTERN.matcher(query);
 		
 		/* Make sure we match */
 		
 		if (m.matches() == false) {
 			throw new SQLException("CREATE query was malformed.");
 		}
 		
 		
 		
 		if (m.group(1) != null) {
 			
 			/* If it's a CREATE DATABASE query */
 			
 			String databaseName = m.group(3);
 			
 			/*  make sure the database name was ok */
 			
 			if ( Pattern.matches("\\w+", databaseName) == false ) {
 				throw new SQLException("CREATE DATABASE query was malformed.");
 			}
 			
 			/* Create the database */
 			
 			parentConnection.getDatabaseServer().createDatabase(databaseName);
 			
 		} else {
 			
 			/* If it's a CREATE TABLE query */
 			
 			String tableDefinitionClause = m.group(3);
 			
 			
 			/* Make sure we've got a table name and a list of column schema */
 			
 			Matcher tableDefinitionMatcher = TABLE_DEFINITION_CLAUSE_PATTERN.matcher(tableDefinitionClause);
 			
 			if ( tableDefinitionMatcher.matches() == false ) {
 				throw new SQLException("CREATE TABLE query was malformed.");
 			}
 			
 			
 			/* Parse the table definition */
 			
 			String tableName = tableDefinitionMatcher.group(1);
 			String tableSchemaClause = tableDefinitionMatcher.group(2);
 
 			
 			/* A comma delimited list of column definitions: NAME TYPE, NAME TYPE ... */
 			
 			if ( TABLE_SCHEMA_CLAUSE_PATTERN.matcher( tableSchemaClause ).matches() ) {
 				
 				/* Split into column schemas */
 				
 				String[] columnDefinitions = tableSchemaClause.split(",\\s+");
 				
 				ArrayList<ColumnSchema> columnSchemaList = new ArrayList<ColumnSchema>(columnDefinitions.length);
 				
 
 				/* Iterate over the schema and add them to the list */
 				
 				for (String columnSchema : columnDefinitions) {
 					columnSchemaList.add(parseColumnSchema(columnSchema));
 				}
 				
 				
 				/* Create the table */
 				
 				parentConnection.getDatabase().createTable(tableName, columnSchemaList);
 				
 				
 			} else {
 				
 				/* The table definition was malformed */
 				
 				throw new SQLException("CREATE TABLE query table definition was malformed.");
 			}
 			
 		}
 		
 		
 	}
 	
 	/**
 	 * A helper method that constructs a ColumnSchema object from a column schema.
 	 * 
 	 * @param columnSchema the column schema string.
 	 * 
 	 * @return A new column schema object from the column schema string
 	 * 
 	 * @throws SQLException if there was an error 
 	 */
 	protected static ColumnSchema parseColumnSchema(String columnSchema) throws SQLException, RemoteException {
 		
 		Matcher columnSchemaMatcher = COLUMN_SCHEMA_PATTERN.matcher(columnSchema);
 		
 		if (columnSchemaMatcher.matches()) {
 			
 			String name = columnSchemaMatcher.group(1);
 			String dataType = columnSchemaMatcher.group(2);
 			
 			return new ColumnSchema(name, dataType);
 			
 		} else {
 			throw new SQLException("Column definition was malformed.");
 		}
 		
 		
 	}
 	
 	/**
 	 * Execute a DELETE query on the database.
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 * @throws RemoteException
 	 */
 	protected void executeDeleteQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher deleteQueryMatcher = DELETE_QUERY_PATTERN.matcher(query);
 		
 		if (deleteQueryMatcher.matches()) {
 			
 			String tableName = deleteQueryMatcher.group(1);
 			String whereClause = deleteQueryMatcher.group(2);
 			
 			parentConnection.getDatabase().delete(tableName, whereClause);
 			
 		} else {
 			throw new SQLException("DELETE query was malformed.");
 		}
 		
 	}
 	
 	/**
 	 * Executes a DESCRIBE query on the database
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 * @throws RemoteException
 	 */
 	protected void executeDescribeQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher describeQueryMatcher = DESCRIBE_QUERY_PATTERN.matcher(query);
 		
 		if (describeQueryMatcher.matches()) {
 			
 			String tableName = describeQueryMatcher.group(1);
 			
 			currentResultSet = parentConnection.getDatabase().describeTable(tableName);
 			
 		} else {
 			throw new SQLException("DESCRIBE query was malformed.");
 		}
 		
 	}
 	
 	/**
 	 * Executes DROP TABLE and DROP DATABASE queries on the server.
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 * @throws RemoteException
 	 */
 	protected void executeDropQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher dropQueryMatcher = DROP_QUERY_PATTERN.matcher(query);
 		
 		if (dropQueryMatcher.matches()) {
 			
 			String targetType = dropQueryMatcher.group(1).toLowerCase();
 			String targetName = dropQueryMatcher.group(2);
 			
			if (targetType == "table") {
 				
 				/* We've got a DROP TABLE query */
 				
 				parentConnection.getDatabase().dropTable(targetName);
 				
			} else if (targetType == "database") {
 				
 				/* We've got a DROP DATABASE query */
 				
 				parentConnection.getDatabaseServer().dropDatabase(targetName);
 				
 			} else {
 				
 				/* We've got something else ... let the world know. */
 				
 				throw new SQLException("Unknown target type for DROP query.");
 			}
 			
 			
 		} else {
 			throw new SQLException("DESCRIBE query was malformed.");
 		}
 		
 	}
 	
 	
 	/**
 	 * Execute an INSERT query on the database
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 * @throws RemoteException
 	 */
 	protected void executeInsertQuery(String query) throws SQLException, RemoteException {
 	
 		Matcher insertQueryMatcher = INSERT_QUERY_PATTERN.matcher(query);
 		
 		if (insertQueryMatcher.matches()) {
 			
 			String tableName = insertQueryMatcher.group(1);
 			String columnList = insertQueryMatcher.group(2);
 			String valueList = insertQueryMatcher.group(3);
 			
 			
 			/* Collect the columns */
 			
 			// TODO: Check to make sure the columnList is valid.
 			
 			ArrayList<String> columnNameCollection = new ArrayList<String>();
 			
 			for (String columnName : columnList.split( COMMA_DELIMITER_PATTERN.pattern() )) {
 				columnNameCollection.add(columnName);
 			}
 			
 			
 			/* Collect the values */
 			
 			// TODO: Check to make sure the value list is valid.
 			
 			ArrayList<String> valueCollection = new ArrayList<String>();
 			
 			for (String value : valueList.split( COMMA_DELIMITER_PATTERN.pattern() )) {
				columnNameCollection.add(value);
 			}
 			
 			parentConnection.getDatabase().insert(tableName, columnNameCollection, valueCollection);
 			
 		} else {
 			throw new SQLException("INSERT query was malformed.");
 		}
 		
 		
 	}
 	
 	protected void executeSelectQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher selectQueryMatcher = SELECT_QUERY_PATTERN.matcher(query);
 		
 		if (selectQueryMatcher.matches()) {
 			
 			String columnList = selectQueryMatcher.group(1);
 			String tableName = selectQueryMatcher.group(2);
 			String whereClause = selectQueryMatcher.group(3);
 			
 			
 			/* Collect columns */
 			
 			// TODO: check to make sure column list is valid.
 			
 			ArrayList<String> columnNameCollection = new ArrayList<String>();
 			
 			for (String columnName : columnList.split( COMMA_DELIMITER_PATTERN.pattern() )) {
 				columnNameCollection.add(columnName);
 			}
 			
 			currentResultSet = parentConnection.getDatabase().select(tableName, columnNameCollection, whereClause);
 			
 		} else {
 			throw new SQLException("SELECT query was malformed.");
 		}
 	}
 	
 	/**
 	 * Execute a 
 	 * @param query
 	 * @throws SQLException
 	 * @throws RemoteException
 	 */
 	protected void executeShowQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher showQueryMatcher = SHOW_QUERY_PATTERN.matcher(query);
 		
 		if (showQueryMatcher.matches()) {
 			
 			String targetType = showQueryMatcher.group(1).toLowerCase();
 			
 			
			if (targetType == "tables") {
 				
 				/* Execute a SHOW TABLES query */
 				
 				currentResultSet = parentConnection.getDatabase().showTables();
 				
			} else if (targetType == "databases") {
 				
 				/* Execute a SHOW DATABASES query */
 				
 				currentResultSet = parentConnection.getDatabaseServer().showDatabases();
 				
 			} else {
 				throw new SQLException("Unknown SHOW query target.");
 			}
 			
 			
 		} else {
 			throw new SQLException("SHOW query was malformed");
 		}
 	}
 	
 
 	protected void executeUpdateQuery(String query) throws SQLException, RemoteException {
 		
 		Matcher updateQueryMatcher = UPDATE_QUERY_PATTERN.matcher(query);
 		
 		if (updateQueryMatcher.matches()) {
 			
 			String tableName = updateQueryMatcher.group(1);
 			String setClause = updateQueryMatcher.group(2);
 			String whereClause = updateQueryMatcher.group(3);
 			
 			
 			/* Parse SET clause */
 			
 			String[] assignmentList = COMMA_DELIMITER_PATTERN.split(setClause);
 
 			/* Parse assignment list */
 			
 			ArrayList<String> columnNames = new ArrayList<String>(assignmentList.length);
 			ArrayList<String> columnValues = new ArrayList<String>(assignmentList.length);
 			
 			for (String assignmentClause : assignmentList) {
 				
 				/* Parse each column assignment clause */
 				
 				Matcher assignmentClauseMatcher = COLUMN_ASSIGNMENT_CLAUSE_PATTERN.matcher(assignmentClause);
 
 				if (assignmentClauseMatcher.matches()) {
 					
 					/* It looks valid */
 					
 					String columnName = assignmentClauseMatcher.group(1);
 					String columnValue = assignmentClauseMatcher.group(2);
 					
 					columnNames.add(columnName);
 					columnValues.add(columnValue);
 					
 				} else {
 					
 					/* oops :P */
 					
 					throw new SQLException("Column assignment clause in UPDATE query was malformed");
 					
 				}
 				
 			}
 			
 			/* Pass the changes along */
 			
 			parentConnection.getDatabase().update(tableName, columnNames, columnValues, whereClause);
 			
 			
 		} else {
 			throw new SQLException("UPDATE query was malformed.");
 		}
 		
 	}
 	
 	
 	/**
 	 * Returns the connection that this OodleStatement belongs to.
 	 * 
 	 * @return The parent connection.
 	 */
 	public Connection getConnection() throws SQLException {
 		return parentConnection;
 	}
 	
 	
 	
 	/*
 	 * And now to make JDBC happy...
 	 */
 
 	public void addBatch(String arg0) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void cancel() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void clearBatch() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void clearWarnings() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void close() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 
 	public boolean execute(String arg0, int arg1) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public boolean execute(String sql, String[] columnNames)
 			throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int[] executeBatch() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public ResultSet executeQuery(String sql) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int executeUpdate(String sql) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int executeUpdate(String sql, int autoGeneratedKeys)
 			throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int executeUpdate(String sql, int[] columnIndexes)
 			throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int executeUpdate(String sql, String[] columnNames)
 			throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getFetchDirection() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getFetchSize() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public ResultSet getGeneratedKeys() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getMaxFieldSize() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getMaxRows() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public boolean getMoreResults() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public boolean getMoreResults(int current) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getQueryTimeout() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getResultSetConcurrency() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getResultSetHoldability() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getResultSetType() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public int getUpdateCount() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public SQLWarning getWarnings() throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setCursorName(String name) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setEscapeProcessing(boolean enable) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setFetchDirection(int direction) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setFetchSize(int rows) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setMaxFieldSize(int max) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setMaxRows(int max) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 	public void setQueryTimeout(int seconds) throws SQLException {
 		throw new SQLException("Unsupported Operation.");
 	}
 
 }
