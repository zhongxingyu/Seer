 package database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import node.Node;
 import node.SimplifiedNodeDomain;
 
 /**
  * Singleton that creates easy access data in the connected database.
  * @author Craig Jacobson
  */
 public class HyPeerWebDatabase {
 
 	public static final String DATABASE_DIRECTORY = "db/";
 	public static final String DEFAULT_DATABASE_NAME = "HyPeerWeb.db";
 	private static final String SQLITE_DRIVER = "org.sqlite.JDBC";
 	private static final String SQLITE_DRIVER_URL_PREFIX = "jdbc:sqlite";
 	private static final String[] tableNames = {"nodes","neighbors","up_pointers","down_pointers"};
 	
 	private static HyPeerWebDatabase singleton = null;
 	
 	private Connection connection = null;
 	
 	public Connection getConnection(){
 		assert connection != null;
 		return this.connection;
 	}
 	
 	/**
 	 * Creates a HyPeerWebDatabase that connects to a sqlite database 
 	 * to load or save information.
 	 * @param dbName Valid database name.
 	 * @Precondition dbName is valid and located in the DATABASE_DIRECTORY
 	 * @Postcondition The database is connected and <code>connection</code> is not null.
 	 * Additionally, dbName is created if it did not exist 
 	 * and any necessary tables are created.
 	 * @author Craig Jacobson
 	 */
 	private HyPeerWebDatabase(String dbName){
 		try{
 			Class.forName(SQLITE_DRIVER);
 			String url = SQLITE_DRIVER_URL_PREFIX + ":" + DATABASE_DIRECTORY + dbName;
 			connection = DriverManager.getConnection(url);
 			connection.setAutoCommit(true);
 			
 			initHyPeerWebTables();
 		}
 		catch(ClassNotFoundException e){
 			System.out.println(e);
 		}
 		catch(SQLException e){
 			System.out.println(e);
 		}
 	}
 	
 	private static final String CREATE_TABLE_NODES = "CREATE TABLE IF NOT EXISTS " + tableNames[0] + "("+
 														"web_id INTEGER UNIQUE NOT NULL,"+
 														"height INTEGER NOT NULL,"+
 														"fold INTEGER,"+
 														"surrogate_fold INTEGER,"+
 														"inverse_surrogate_fold INTEGER,"+
 														"state INTEGER);";
 	
 	private static final String CREATE_TABLE_NEIGHBORS = "CREATE TABLE IF NOT EXISTS " + tableNames[1] + "("+
 															"node INTEGER NOT NULL,"+
 															"neighbor INTEGER NOT NULL,"+
 															"UNIQUE (node, neighbor));";
 	
 	private static final String CREATE_TABLE_UP_POINTERS = "CREATE TABLE IF NOT EXISTS " + tableNames[2] + "("+
 															"node INTEGER NOT NULL,"+
 															"edge_node INTEGER NOT NULL,"+
 															"UNIQUE (node, edge_node));";
 	
 	private static final String CREATE_TABLE_DOWN_POINTERS = "CREATE TABLE IF NOT EXISTS " + tableNames[3] + "("+
 																"node INTEGER NOT NULL,"+
 																"surrogate_neighbor INTEGER NOT NULL,"+
 																"UNIQUE (node, surrogate_neighbor));";
 	
 	/**
 	 * Ensures that the database has the proper tables.
 	 * @param con A valid non-null Connection object.
 	 * @Precondition <code>con</code> is non-null
 	 * @Postcondition The database connection <code>con</code>  contains the necessary tables.
 	 * @author Craig Jacobson
 	 */
 	private void initHyPeerWebTables(){
 		
 		try{
 			Statement createTable = getConnection().createStatement();
 			createTable.executeUpdate(CREATE_TABLE_NODES);
 			createTable.executeUpdate(CREATE_TABLE_NEIGHBORS);
 			createTable.executeUpdate(CREATE_TABLE_UP_POINTERS);
 			createTable.executeUpdate(CREATE_TABLE_DOWN_POINTERS);
 			createTable.close();
 		}
 		catch(SQLException e){
 			System.out.println(e);
 		}
 	}
 	
 	/**
 	 * Creates or loads the default HyPeerWebDatabase.
 	 */
 	public static void initHyPeerWebDatabase(){
 		initHyPeerWebDatabase(null);
 	}
 	
 	/**
 	 * Creates or loads a HyPeerWebDatabase. 
 	 * Should be one of the first things called when creating a HyPeerWeb.
 	 * @param dbName The name of the database. Must be valid, null, or empty.
 	 * @Precondition dbName = null OR |dbName| = 0 OR dbName is valid
 	 * @Postcondition 
 	 * IF dbName == null OR |dbName| = 0
 	 * new HyPeerWebDatabase(DEFAULT_DATABASE_NAME)
 	 * ELSE
 	 * new HyPeerWebDatabase(dbName)
 	 * @author Craig Jacobson
 	 */
 	public static void initHyPeerWebDatabase(String dbName){
 		if(dbName == null || dbName.length() == 0){
 			File databaseDirectory = new File(DATABASE_DIRECTORY);
 			databaseDirectory.mkdirs();
 			
 			singleton = new HyPeerWebDatabase(DEFAULT_DATABASE_NAME);
 		}
 		else {
 			singleton = new HyPeerWebDatabase(dbName);
 		}
 	}
 	
 	/**
 	 * Gets the single HyPeerWebDatabase.
 	 * @return The HyPeerWebDatabase singleton.
 	 * @Precondition <code>initHyPeerWebDatabase()</code> must have been called previously to ensure
 	 * that result != null
 	 * @Postcondition result = singleton
 	 * @author Craig Jacobson
 	 */
 	public static HyPeerWebDatabase getSingleton(){
 		if(singleton == null){
 			initHyPeerWebDatabase("");
 		}
 			
 		return singleton;
 	}
 	
 	/**
 	 * Creates a SimplifiedNodeDomain representing the node with indicated webId.
 	 * The information is retrieved from the database.
 	 * @param webId The webId of the node whose information will be retrieved.
 	 * @return The SimplifiedNodeDomain corresponding to the webId.
 	 * @Precondition There exists a node in the database with the given webId.
 	 * @Postcondition result contains the webId, neighbors, upPointers, 
 	 * downPointers, fold, surrogateFold, and inverse surrogate fold and state of the 
 	 * indicated node.
 	 * @author Jason Robertson
 	 */
 	public SimplifiedNodeDomain getNode(int webId){
 		assert webId >= 0;
 
 		SimplifiedNodeDomain result = null;
 		
 		try {
 			
 			Statement stmt = getSingleton().getConnection().createStatement();
 			String sql = "SELECT * from nodes WHERE web_id = " + webId;
 			ResultSet rs = stmt.executeQuery(sql);
 
 			int web_id;
 			int height;
 			int fold;
 			int surrogate_fold;
 			int inverse_surrogate_fold;
 			int state;
 
 			if( ! rs.next()) {
 				return null;
 			}
 			
 			web_id = rs.getInt(1);
 			height = rs.getInt(2);
 			fold = rs.getInt(3);
 			surrogate_fold = rs.getInt(4);
 			inverse_surrogate_fold = rs.getInt(5);
 			state = rs.getInt(6);
 
 			HashSet<Integer> neighbors = findNearbyFriends(webId, tableNames[1]);
 			HashSet<Integer> upPointers = findNearbyFriends(webId, tableNames[2]); 
 			HashSet<Integer> downPointers = findNearbyFriends(webId, tableNames[3]); 
 
 			result = new SimplifiedNodeDomain(web_id, height, neighbors,
 					upPointers, downPointers, fold, surrogate_fold, inverse_surrogate_fold, state);
 
 		}
 		catch(SQLException e) {
 			System.out.println(e);
 		}
 
 		return result;
 	}
 	
 	/**
 	 * This method retrieves neighboring information of a given node.
 	 * @param webId The web id of the node the resulting data belongs to.
 	 * @param table The name of the table being searched. (neighbors, up_pointers, or down_pointers)
 	 * @return A set of web id values of the nearby friends
 	 * @author Jason Robertson
 	 */
 	private HashSet<Integer> findNearbyFriends(int webId, String table) {
 		HashSet<Integer> result = new HashSet<Integer>();
 
 		try {
 			Statement stmt = getSingleton().getConnection().createStatement();
 			String sql = "SELECT * from " + table + " WHERE node = " + webId;
 			ResultSet rs = stmt.executeQuery(sql);
 
 			while(rs.next()) {
 				result.add(rs.getInt(2));
 			}
 		}
 		catch (SQLException e) {
 			System.out.println(e);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns a list of all the web ids currently stored in the database.
 	 * @author Jason Robertson
 	 */
 	public List<Integer> getAllWebIds(){
 
 		List<Integer> result = new ArrayList<Integer>();
 		
 		try {
 
 			Statement stmt = getSingleton().getConnection().createStatement();
 			String sql = "SELECT * from nodes;";
 			ResultSet rs = stmt.executeQuery(sql);
 
 			while(rs.next()) {
 				result.add(rs.getInt(1));
 			}
 		}
 		catch(SQLException e) {
 			System.out.println(e);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Stores the node and all of that nodes references.
 	 * @param node - The node being stored 
 	 * @author Jason Robertson
 	 */
 	public void storeNode(Node node){
 
 		try {
 			
 			String sql_delete = "DELETE FROM " + tableNames[0] + " WHERE web_id = " + node.getWebIdValue();
 			String sql_upPtrs = "DELETE FROM " + tableNames[1] + " WHERE node = " + node.getWebIdValue();
 			String sql_downPtrs = "DELETE FROM " + tableNames[2] + " WHERE node = " + node.getWebIdValue();
 			String sql_neighbors = "DELETE FROM " + tableNames[3] + " WHERE node = " + node.getWebIdValue();
 			
 			Statement stmt = getSingleton().getConnection().createStatement();
 			
 			stmt.execute(sql_delete);
 			stmt.execute(sql_upPtrs);
 			stmt.execute(sql_downPtrs);
 			stmt.execute(sql_neighbors);					
 			
 			// Store General Info
 
 			String sql = String.format("INSERT INTO " + tableNames[0] +
 					"(web_id, height, fold, surrogate_fold, inverse_surrogate_fold, state) " +
 					"VALUES ('%d', '%d', '%d', '%d', '%d', '%d');",
 					node.getWebIdValue(),
 					node.getWebIdHeight(),
 					node.getFold().getWebIdValue(),
 					node.getSurrogateFold().getWebIdValue(),
 					node.getInverseSurrogateFold().getWebIdValue(),
 					node.getState().STATE_ID
 					);
 
 			stmt.executeUpdate(sql);
 
 			// Store Neighbors
 
 			for(Node neighbor : node.getNeighbors().values()){
 				sql = String.format("INSERT INTO " + tableNames[1] +
 						"(node, neighbor) " +
 						"VALUES ('%d', '%d');",
 						node.getWebIdValue(), neighbor.getWebIdValue());
 
 				stmt.executeUpdate(sql);
 			}
 
 			// Store Up Pointers
 
 			for(Node up : node.getUpPointers().values()){
 				sql = String.format("INSERT INTO " + tableNames[2] +
 						"(node, edge_node) " +
 						"VALUES ('%d', '%d');",
 						node.getWebIdValue(), up.getWebIdValue());
 
 				stmt.executeUpdate(sql);
 			}
 
 			// Store Down Pointers
 
 			for(Node down : node.getDownPointers().values()){
 				sql = String.format("INSERT INTO " + tableNames[3] +
 						"(node, surrogate_neighbor) " +
 						"VALUES ('%d', '%d');",
 						node.getWebIdValue(), down.getWebIdValue());
 
 				stmt.executeUpdate(sql);
 			}
 
 			stmt.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Removes all the data in the tables from the database. (The schema
 	 * skeleton is left behind, ready to start adding things to it again)
 	 * @author Jason Robertson
 	 */
 	public static void clear(){
 
 		try {
 			Statement stmt = getSingleton().getConnection().createStatement();
 			for(String table : tableNames){
 
 				String sql = "delete from " + table + ";";
 				stmt.executeUpdate(sql);
 			}
 			stmt.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 }
