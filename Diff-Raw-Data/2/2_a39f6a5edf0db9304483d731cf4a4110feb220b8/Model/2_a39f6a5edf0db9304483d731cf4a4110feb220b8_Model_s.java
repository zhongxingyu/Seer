 package Models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.List;
 
 import Util.Logger;
 import Util.MySQLConnection;
 
 /**
  * Model
  * Basic abstract super-class for models. With this certain 
  * basic methods are guaranteed in all models. This is based 
  * on the CRUD-model (Create, Read, Update, Delete).
  *
  */
 public abstract class Model {
 	
 	/**
 	 * Creates an entry in the database, with the data given in the Map. The 
 	 * ID of the new entry is returned on success; -1 on failure.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		try {
 			String setSQL = buildQuery(createVars);
 			setSQL = setSQL.substring(0, setSQL.lastIndexOf(','));
 			String query =	"INSERT INTO " + getClassName() + " " + 
 							"SET " + setSQL;
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result != null) {
 				result.next();
 				return result.getInt(1);
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't insert data to database: " + e.getMessage());
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * Reads and returns the data with the provided Id in a Map, with data-names 
 	 * as keys. If an entry with the provided ID cannot be found in the database, 
 	 * null will be returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 */
 	abstract public Map<String, Object> read (int id);
 	
 	/**
 	 * Updates the entry with the provided ID in the database. The data to be 
 	 * updated is the keys in the map, and the values are the new data. If the 
 	 * entry is successfully updated, true will be returned. If the update 
 	 * failed (eg. invalid ID), false will be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * @return true on success; false on failure.
 	 */
 	protected boolean update(int id, Map<String, Object> updateVars, String idColumn) {	
		String query =	"UPDATE " + getClassName() + 
 						"SET " + buildQuery(updateVars) + 
 						"WHERE " + idColumn + " = " + id;
 		MySQLConnection conn = MySQLConnection.getInstance();
 		ResultSet result = conn.query(query);
 		if (result != null) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Deletes the entry with the provided ID in the database. On success 
 	 * true will be returned. If the deletion failed, false will be returned. 
 	 * Please note: If no entry is found with the provided ID, true will still 
 	 * be returned, as an entry with that ID isn't in the database after this 
 	 * method-call.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	abstract public boolean delete (int id);
 	
 	/**
 	 * Deletes the entry with the provided ID in the database. On success 
 	 * true will be returned. If the deletion failed, false will be returned. 
 	 * Please note: If no entry is found with the provided ID, true will still 
 	 * be returned, as an entry with that ID isn't in the database after this 
 	 * method-call.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	protected boolean delete (int id, String idColumn) {
 		if (id <= 0 || idColumn == null)
 			throw new NullPointerException();
 		
 		try {			
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "DELETE FROM " + getClassName() + " " + 
 						   "WHERE " + idColumn + " = " + id;
 			ResultSet result = conn.query(query);
 			if (result != null) {
 				return true;
 			}
 		} catch (Exception e) {
 			Logger.write("Couldn't delete row from database: " + e.getMessage());
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Gives the amount of entries in the database, 
 	 * eg. the amount of customers in the database.
 	 * 
 	 * @return The amount of entries in the data-source.
 	 */
 	public int amountOfEntries () {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT count(*) AS entryAmount " +
 						   "FROM " + getClassName();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return 0;
 			result.next();
 			return result.getInt(1);
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from database: " + e.getMessage());
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * Lists the entries of the data-source.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	abstract public List<Map<String, Object>> list ();
 	
 	/**
 	 * Get the name of the current class. Used in SQL queries to manipulate 
 	 * data in the correct tables.
 	 * 
 	 * @return The name of the class.
 	 */
 	private String getClassName () {
 		String className = this.getClass().getName();
 		className = className.substring(className.lastIndexOf('.')+1, className.length());
 		
 		return className;
 	}
 	
 	/**
 	 * Takes a Map<String, Object> and returns a String ready for SQL queries 
 	 * (for INSERT and UPDATE).
 	 * 
 	 * @param vars A Map of values.
 	 * @return A string ready for SQL queries.
 	 */
 	private String buildQuery (Map<String, Object> vars) {
 		String query = "";
 		for (Map.Entry<String, Object> item : vars.entrySet()) {
 			query = query.concat(item.getKey() + " = '" + item.getValue() + "', ");
 		}
 		
 		return query;
 	}
 }
