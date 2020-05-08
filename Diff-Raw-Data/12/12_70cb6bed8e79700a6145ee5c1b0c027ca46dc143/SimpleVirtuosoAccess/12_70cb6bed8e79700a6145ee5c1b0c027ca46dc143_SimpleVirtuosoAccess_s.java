 /**
  * 
  */
 package cz.cuni.mff.odcleanstore.engine.core;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Encapsulates jdbc connections and single threaded basic data operations on Virtuoso database.
  * 
  * @author Petr Jerman (petr.jerman@centrum.cz)
  * 
  */
 public class SimpleVirtuosoAccess {
 
 	private static boolean _isDriverInitialized = false;
 
 	/**
 	 * Create a new connections to the local database on 1111 port with dba credentials.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	public static SimpleVirtuosoAccess CreateDefaultDbaConnection() throws ClassNotFoundException, SQLException {
 
 		return new SimpleVirtuosoAccess("jdbc:virtuoso://localhost:1111", "dba", "dba");
 	}
 
 	private Connection _con;
 
 	/**
 	 * Create a new connections to the database.
 	 * 
 	 * @param connectionString
 	 * @param user
 	 * @param password
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	public SimpleVirtuosoAccess(String connectionString, String user, String password) throws ClassNotFoundException,
 			SQLException {
 		if (!_isDriverInitialized) {
 			Class.forName("virtuoso.jdbc3.Driver");
 			_isDriverInitialized = true;
 		}
 
 		_con = DriverManager.getConnection(connectionString, user, password);
 
 		adjustTransactionLevel("1", false);
 	}
 
 	/**
 	 * Adjust transaction level in Virtuoso and jdbc
 	 * 
 	 * @param virtusoLogEnableValue
 	 *            - 0 disable log, 1 enable transaction level log, 3 enable statement level log
 	 * @param autoCommitValue
 	 * 
 	 * @throws SQLException
 	 */
 	private void adjustTransactionLevel(String virtusoLogEnableValue, boolean autoCommitValue) throws SQLException {
 
 		CallableStatement cst = _con.prepareCall(String.format("log_enable(%s)", virtusoLogEnableValue));
 		cst.execute();
 		_con.setAutoCommit(autoCommitValue);
 	}
 
 	/**
 	 * Close connection
 	 * 
 	 */
 	public void close() {
 		if (_con != null) {
 			try {
 				_con.close();
 				_con = null;
 			} catch (SQLException e) {
 			}
 		}
 	}
 
 	/**
 	 * Commit changes to the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void commit() throws SQLException {
 		_con.commit();
 	}
 
 	/**
 	 * Delete graph from the database.
 	 * 
 	 * @param graphName
 	 * 
 	 * @throws SQLException
 	 */
 	public void deleteGraphs(String graphName) throws SQLException {
 		String statement = String.format("SPARQL CLEAR GRAPH %s", graphName);
 		executeStatement(statement);
 	}
 
 	/**
 	 * Execute Sql and Sparql statement.
 	 * 
 	 * @param statement
 	 * 
 	 * @throws SQLException
 	 */
 	private void executeStatement(String statement) throws SQLException {
 		Statement stmt = _con.createStatement();
 		stmt.execute(statement);
 	}
 
 	/**
 	 * Overridden finalize method close connection.
 	 */
 	@Override
 	protected void finalize() {
 		try {
 			close();
 			super.finalize();
 		} catch (Throwable e) {
 		}
 	}
 
 	/**
 	 * Insert quad to the database.
 	 * 
 	 * @param subject
 	 * @param predicate
 	 * @param object
 	 * @param graph
 	 * 
 	 * @throws SQLException
 	 */
 	public void insertQuad(String subject, String predicate, String object, String graph) throws SQLException {
 		String statement = String.format("SPARQL INSERT INTO GRAPH %s { %s %s %s }", graph, subject, predicate, object);
 		executeStatement(statement);
 	}
 
 	/**
 	 * Load rdfxml file.
 	 * 
	 * @param graph
 	 * 
 	 * @throws SQLException
 	 */
 	public void loadRdfXmlFile(String fileName, String baseURI, String graphName) throws SQLException {
 		// TODO not tested
 		String statement = String.format("SPARQL DB.DBA.RDF_LOAD_RDFXML(file_to_string_output('%s'), '%s', '%s')",
 				fileName, baseURI, graphName);
 		executeStatement(statement);
 	}
 
 	/**
 	 * Revert changes to the last commit.
 	 * 
 	 * @throws SQLException
 	 */
 	public void revert() throws SQLException {
 		_con.rollback();
 	}
 }
