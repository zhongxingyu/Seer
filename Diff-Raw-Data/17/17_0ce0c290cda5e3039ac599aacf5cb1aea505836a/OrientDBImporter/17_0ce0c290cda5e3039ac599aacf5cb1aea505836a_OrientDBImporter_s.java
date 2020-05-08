 package de.uni.leipzig.IR15.Importer;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
 import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 
 import de.uni.leipzig.IR15.Connectors.MySQLConnector;
 import de.uni.leipzig.IR15.Support.Configuration;
 
 public class OrientDBImporter extends Importer {
 
 	private static enum RelTypes {
 		CO_S, CO_N
 	}
 
	private static Connection mySQL;
 	private static OGraphDatabase orientdb;
 
 	public OGraphDatabase getDB() {
 		return orientdb;
 	}
 
 	public OGraphDatabase onlyLoadDB() {
 		graphConfiguration = Configuration.getInstance("orientdb");
 		// specify that the filesystem is used
 		String url2db = "local:"
 				+ graphConfiguration.getPropertyAsString("location");
 		// create an OrientDB-Database
 		orientdb = new OGraphDatabase(url2db);
 		// connect to it
 		orientdb.open("admin", "admin");
 
 		return orientdb;
 	}
 
 	@Override
 	public void setUp() {
 		// load properties
 		mySQLConfiguration = Configuration.getInstance("mysql");
 		graphConfiguration = Configuration.getInstance("orientdb");
 
 		// delete existing db
 		reset();
 
		mySQL = MySQLConnector.getConnection();
 
 		// specify that the filesystem is used
 		String url2db = "local:"
 				+ graphConfiguration.getPropertyAsString("location");
 		// create an OrientDB-Database
 		ODatabaseDocumentTx db = new ODatabaseDocumentTx(url2db).create();
 		// Java-Object for DB
 		orientdb = new OGraphDatabase(url2db);
 		// connect to it
 		orientdb.open("admin", "admin");
 
 		// make sure that DB is closed properly
 		registerShutdownHook(orientdb);
 
 		// @TODO
 		// create an index on the nodes
 		// nodeIndex = neo4j.index().forNodes("words");
 	}
 
 	@Override
 	public void tearDown() {
 		// shutdown the connections
 		orientdb.close();
 		try {
 			mySQLConnection.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		reset();
 	}
 
 	/**
 	 * @param args
 	 */
 	public void importData() {
 		// transfer the data from mysql to orientdb
		transferData(orientdb, mySQL);
 	}
 
 	private void transferData(OGraphDatabase neo4j, Connection mySQL) {
 		importWords(mySQL, orientdb);
 		importCooccurrences(mySQL, neo4j, RelTypes.CO_N);
 		importCooccurrences(mySQL, neo4j, RelTypes.CO_S);
 	}
 
 	private void importWords(Connection mySQL, OGraphDatabase orientdb) {
 		String query = "SELECT * FROM words";
 
 		Integer count = getRowCount(mySQL, "words");
 		log.info("Importing " + count + " words ");
 
 		try {
 			Statement st = mySQL.createStatement();
 			st.setFetchSize(Integer.MIN_VALUE);
 			ResultSet rs = st.executeQuery(query);
 			// v-type
 			while (rs.next()) {
 				String word = rs.getString("word");
 				Integer word_id = rs.getInt("w_id");
 
 				ODocument vertex = orientdb.createVertex();
 				vertex.field("w_id", word_id);
 				vertex.field("word", word);
 				vertex.save(); // make it persistent
 
 				// @TODO: DIRTY!!! Is this procedure really safe for finding
 				// vertices by w_id?
 				orientdb.setRoot(word_id.toString(), vertex);
 
 			}
 		} catch (SQLException ex) {
 			System.err.println(ex.getMessage());
 		}
 	}
 
 	private void importCooccurrences(Connection mySQL, OGraphDatabase orientdb,
 			RelTypes relType) {
 		String table = relType.toString().toLowerCase();
 		Integer count = getRowCount(mySQL, table);
 
 		orientdb.createEdgeType(table);
 		log.info("Importing " + count + " cooccurrences (" + table + ")");
 
 		String query = "SELECT * FROM " + table;
 
 		try {
 			Statement st = mySQL.createStatement();
 			st.setFetchSize(Integer.MIN_VALUE);
 			ResultSet rs = st.executeQuery(query);
 
 			while (rs.next()) {
 				Integer w1_id = rs.getInt("w1_id");
 				Integer w2_id = rs.getInt("w2_id");
 				Integer sig = rs.getInt("sig");
 				Integer freq = rs.getInt("freq");
 
 				// @TODO Dirty, maybe better solution for bigger Graphs
 				ODocument source = orientdb.getRoot(w1_id.toString());
 				ODocument target = orientdb.getRoot(w2_id.toString());
 
 				ODocument edge = orientdb.createEdge(source, target, table); // table
 																				// =
 																				// co_n
 																				// or
 																				// co_s
 				edge.field("freq", freq);
 				edge.field("sig", sig);
 				edge.save();
 			}
 
 		} catch (SQLException ex) {
 			System.err.println(ex.getMessage());
 		}
 	}
 
 	private void registerShutdownHook(final OGraphDatabase graphDb) {
 		// Registers a shutdown hook for the OrientDB instance so that it
 		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
 		// running example before it's completed)
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				graphDb.close();
 			}
 		});
 	}
 
 	private Integer getRowCount(Connection sqlConnection, String table) {
 		String query = "SELECT COUNT(*) FROM " + table;
 		Integer count = null;
 		try {
 			Statement st = sqlConnection.createStatement();
 			ResultSet rs = st.executeQuery(query);
 
 			while (rs.next()) {
 				count = Integer.valueOf(rs.getInt("COUNT(*)"));
 			}
 		} catch (SQLException ex) {
 			System.err.println(ex.getMessage());
 		}
 		return count;
 	}
 
 	@Override
 	public String getName() {
 		return "orientdb";
 	}
 }
