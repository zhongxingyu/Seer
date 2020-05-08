 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 /**
  * Low-level Database access class
  * @author isaac
  */
 public class Database {
 	//Reference to singleton
 	private static Database singleton;
 	private static boolean IS_CONNECTED = true;
 	//Database connection and default statement
 	private Connection db;
 	private Statement stmt;
 	
     /**
 	 * Private constructor for the singleton
 	 * - Initializes database connection
 	 * - Creates database if it doesn't exist
 	 * @author isaac
 	 */
 	private Database() throws Exception{
 		//Load the database driver
 		try{
             Class.forName("org.sqlite.JDBC");
         } catch(ClassNotFoundException e) {
             System.out.println("Could not load database driver!");
 			throw e;
         }
 		//Open a database connection
 		try{
 			db = DriverManager.getConnection("jdbc:sqlite:HyPeerWeb.sqlite");
 		} catch(Exception e){
 			System.out.println("Could not connect to the database!");
 			throw e;
 		}
 		//Setup the database, if not already there
 		try{
 			String db_setup = 
 				"BEGIN;"+
 				"create table if not exists `Nodes` (`WebId` integer primary key, `Height` integer, `Fold` integer, `SurrogateFold` integer, `InverseSurrogateFold` integer);"+
 				"create table if not exists `Neighbors` (`WebId` integer, `Neighbor` integer);"+
 				"create table if not exists `SurrogateNeighbors` (`WebId` integer, `SurrogateNeighbor` integer);"+
 				"create index if not exists `Idx_Neighbors` on `Neighbors` (`WebId`);"+
 				"create index if not exists `Idx_SurrogateNeighbors` on `SurrogateNeighbors` (`WebId`);"+
 				"create index if not exists `Idx_InverseSurrogateNeighbors` on `SurrogateNeighbors` (`SurrogateNeighbor`);"+
 				"COMMIT;";
 			stmt = db.createStatement();
 			stmt.setQueryTimeout(5);
 			stmt.executeUpdate(db_setup);			
 		} catch (SQLException e) {
 			System.out.println("Could not create the database!");
 			throw e;
 		}
 	}
 	/**
 	 * Gets the singleton instance of the Database class
 	 * @return singleton Database reference
 	 * @author isaac
 	 */
 	public static Database getInstance() throws Exception{
 		//First time connect
 		if (IS_CONNECTED && singleton == null){
 			try{
 				singleton = new Database();
 			} catch(Exception e){
 				IS_CONNECTED = false;
 				throw e;
 			}
 		}
 		//Failed connection
 		if (!IS_CONNECTED)
 			throw new Exception("Failed to connect to database");
 		//Successful connection available
 		return singleton;
 	}
 	/**
 	 * Removes all data from the database, leaving the structure intact.
 	 * @author john
 	 */
 	public void clearDB(){
 		sqlUpdate("delete from `Nodes`");
 		sqlUpdate("delete from `Neighbors`");
 		sqlUpdate("delete from `SurrogateNeighbors`");
 	}
 	
 	///SQL OPERATIONS
 	/**
 	 * Run an SQL update statement
 	 * @param sql the sql statement
 	 * @return true if the operation was successful
 	 * @author isaac
 	 */
 	private boolean sqlUpdate(String sql){
 		try{
 			stmt.executeUpdate(sql);
 		} catch (Exception e){
 			System.out.println("SQL Error: "+e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	/**
 	 * Run an SQL query statement
 	 * @param sql the sql query
 	 * @return the ResultSet of the query, if successful
 	 * @throws Exception the error, if the operation failed
 	 * @author isaac
 	 */
 	private ResultSet sqlQuery(String sql) throws Exception{
 		return stmt.executeQuery(sql);
 	}
 			
 	///FULL NODE OPERATIONS
 	/**
 	 * Add a node to the database
 	 * @param node the node to add
 	 * @return true if the node was successfully added
 	 * @author guy
 	 */
 	public boolean addNode(Node node){
             String update;
             update = "INSERT INTO Nodes VALUES(" + 
                 node.getWebID() + ", " + 
                 node.getHeight() + ", " + 
                 node.getFold() + ", " + 
                 node.getSurrogateFold() + ", " +
                 node.getInverseSurrogateFold() + ");";
             
             if(!sqlUpdate(update))
 		return false;
             
             ArrayList<Integer> list;
             int webID = node.getWebID();
             
             list = node.getNeighbors();
             for(int i:list)
                 if(!addNeighbor(webID, i))
                     return false;
             
             list = node.getSurrogateNeighbors();
             for(int i:list)
                 if(!addSurrogateNeighbor(webID, i))
                     return false;
             
             list = node.getInverseSurrogateNeighbors();
             for(int i:list)
                 if(!addSurrogateNeighbor(i, webID))
                     return false;
             
             return true;
 	}
 	/**
 	 * Add a node to the database
 	 * @param webid the node's WebID
 	 * @param height the node's height
 	 * @param fold the WebID of the node's Fold 
 	 * @param sfold the WebID of the surrogate
 	 * @return true if the node was successfully added
 	 * @author brian
 	 */
 	public boolean addNode(int webid, int height, int foldid, int sfoldid){
 		//may want to create a new node and send it to addNode(Node) ???
 		//set inverse surrogate fold of "sfold" to "webid"
 		return false;
 	}
 	/**
 	 * Removes a node from the database
 	 * @param webid the WebID of the node to remove
 	 * @return true if the node was successfully removed
 	 * @author brian
 	 */
 	public boolean removeNode(int webid){
 		return false;
 	}
 	
 	///NODE ATTRIBUTES
 	/**
 	 * Set a node's height
 	 * @param webid the WebId of the node to modify
 	 * @param height the node's height
 	 * @return true if the operation was successful
 	 * @author isaac
 	 */
 	public boolean setHeight(int webid, int height){
 		return setColumn(webid, "Height", height);
 	}
 	/**
 	 * Set the Fold node of another node
 	 * @param webid the WebId of the node to modify
 	 * @param foldid the WebId of the node's Fold
 	 * @return true if the operation was successful
 	 * @author isaac
 	 */
 	public boolean setFold(int webid, int foldid){
 		return setColumn(webid, "Fold", foldid);
 	}
 	/**
 	 * Set the Surrogate Fold node of another node
 	 * @param webid the WebId of the node to modify
 	 * @param sfoldid the WebId of the node's surrogate fold
 	 * @return true if the operation was successful
 	 * @author isaac
 	 */
 	public boolean setSurrogateFold(int webid, int sfoldid){
 		try{
 			db.setAutoCommit(false);
 			if (!setColumn(webid, "SurrogateFold", sfoldid) ||
 				!setColumn(sfoldid, "InverseSurrogateFold", webid))
 				throw new Exception("Failed to set fold columns");
 			db.commit();
 		} catch(Exception e){
 			try {
 				System.out.println("SQL Error: "+e.getMessage());
 				db.rollback();
 			} catch (SQLException ex) {
 				System.out.println("SQL Error: Could not rollback changes");
 				System.out.println(ex.getMessage());
 			}
 			return false;
 		} finally{
 			try {
 				db.setAutoCommit(true);
 			} catch (SQLException ex) {
 				System.out.println("Error: Could not enable auto-commit");
 				return false;
 			}
 		}
 		return true;
 	}
 	/**
 	 * Get a node's height
 	 * @param webid the WebId of the node to access
 	 * @return the node's height
 	 * @throws Exception throws exception if retrieval fails
 	 * @author isaac
 	 */
 	public int getHeight(int webid) throws Exception{
 		return getColumn(webid, "Height");
 	};
 	/**
 	 * Get the Fold node of another node
 	 * @param webid the WebId of the node to access
 	 * @return the WebId of the node's Fold
 	 * @throws Exception throws exception if retrieval fails
 	 * @author isaac
 	 */
 	public int getFold(int webid) throws Exception{
 		return getColumn(webid, "Fold");
 	};
 	/**
 	 * Get the Surrogate Fold node of another node
 	 * @param webid the WebId of the node to access
 	 * @return the WebId of the node's Surrogate Fold
 	 * @throws Exception throws exception if retrieval fails
 	 * @author isaac
 	 */
 	public int getSurrogateFold(int webid) throws Exception{
 		return getColumn(webid, "SurrogateFold");
 	};
 	/**
 	 * Gets the Inverse Surrogate Fold of another node
 	 * @param webid the WebId of the node to access
 	 * @return the WebId of the node's Inverse Surrogate Fold
 	 * @throws Exception throws exception if retrieval fails
 	 * @author isaac
 	 */
 	public int getInverseSurrogateFold(int webid) throws Exception{
 		return getColumn(webid, "InverseSurrogateFold");
 	};
 	
 	//private methods for getting/setting columns
 	private boolean setColumn(int webid, String colname, int value){
 		return sqlUpdate("update `Nodes` set `"+colname+"` = '"+value+"' where `WebId` = '"+webid+"'");
 	}
 	private int getColumn(int webid, String colname) throws Exception{
 		ResultSet res = sqlQuery("select `"+colname+"` as col_value from `Nodes` where `WebId` = '"+webid+"'");
 		return res.getInt("col_value");
 	}
 	
 	///NODE NEIGHBORS
 	/**
 	 * Add neighbor node to a node
 	 * @param webid the WebId of the parent node
 	 * @param neighbor WebId of the new neighbor
 	 * @return true if neighbor was successfully added
 	 * @author josh
 	 */
 	public boolean addNeighbor(int webid, int neighbor){
 		return false;
 	}
 	/**
 	 * Removes neighbor node from a node
 	 * @param webid the WebId of the parent node
 	 * @param neighbor WebId of the neighbor to remove
 	 * @return true if neighbor was successfully removed
 	 * @author josh
 	 */
 	public boolean removeNeighbor(int webid, int neighbor){
 		return false;
 	}
 	/**
 	 * Retrieves a list of a node's neighbors
 	 * @param webid the WebId of the node to access
 	 * @return list of WebId's of the node's neighbors
 	 * @throws Exception throws exception if there was an error in retrieval
 	 * @author josh
 	 */
 	public ArrayList<Integer> getNeighbors(int webid) throws Exception{
 		throw new Exception("Failed to retrieve data");
 	}
 	/**
 	 * Add a surrogate neighbor to a node
 	 * @param webid the WebId of the parent node
 	 * @param neighbor the WebId of the neighbor node to add
 	 * @return true, if the neighbor was successfully added
 	 * @author john
 	 */
 	public boolean addSurrogateNeighbor(int webid, int neighbor){
 		return sqlUpdate("INSERT INTO SurrogateNeighbors VALUES(" + webid + "," + neighbor + ")");
 	}
 	/**
 	 * Remove a surrogate neighbor from a node
 	 * @param webid the WebId of the parent node
 	 * @param neighbor the WebId of the neighbor node to remove
 	 * @return true, if the neighbor was successfully removed
 	 * @author john
 	 */
 	public boolean removeSurrogateNeighbor(int webid, int neighbor){
 		return sqlUpdate("DELETE FROM SurrogateNeighbors WHERE WebID=" + webid +
 				" AND SurrogateNeighbor=" + neighbor);
 	}
 	/**
 	 * Retrieves a list of surrogate neighbors
 	 * @param webid the WebId of the original node
 	 * @return a list of WebId's of the node's 
 	 * @throws Exception throws exception if there was an error in retrieval
 	 * @author john
 	 */
 	public ArrayList<Integer> getSurrogateNeighbors(int webid) throws Exception{
 		ArrayList<Integer> data = new ArrayList<>();
		ResultSet results = sqlQuery("SELECT * FROM SurrogateNeighbors WHERE WebID=" + webid);
		while (!results.isLast())
		{
			data.add(results.getInt("SurrogateNeighbor"));
			results.next();
		}
 		return data;
 	}
 	/**
 	 * Retrieves a list of inverse surrogate neighbors
 	 * @param webid the WebId of the surrogate neighbor
 	 * @return a list of WebId's that are the surrogate neighbor's inverse
 	 * @throws Exception throws exception if there was an error in retrieval
 	 * @author john
 	 */
 	public ArrayList<Integer> getInverseSurrogateNeighbors(int webid) throws Exception{
 		ArrayList<Integer> data = new ArrayList<>();
		ResultSet results = sqlQuery("SELECT * FROM SurrogateNeighbors "
				+ "WHERE SurrogateNeighbor=" + webid);
		while (!results.isLast())
		{
			data.add(results.getInt("WebID"));
			results.next();
		}
 		return data;
 	}
 }
