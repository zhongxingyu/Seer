 package Models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import Util.Logger;
 import Util.MySQLConnection;
 
 /**
  * Model - Car Type
  * As data-representation of car-types in the database, this class provides 
  * several methods for dealing with car-types, i.e. creating car-types, listing 
  * car-types, updating and deleting car-types.
  *
  * <code>
  * 	CarType carType = new CarType();
  *	// Create a new car-type (Van)
  *	int van = carType.create("Van");
  *	// Delete a car-type
  *	carType.delete(van);
  * </code>
  */
 public class CarType extends Model {
 	
 	/**
 	 * Creates a new car-type in the database, with the name provided. Names are 
 	 * unique, so if a car-type with the provided name already exists in the 
 	 * database, -1 will be returned. Otherwise the ID-number of the new 
 	 * car-type is returned.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * 			key		=> description
 	 * 			name	=> The title of the car-type
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		return create (createVars.get("name").toString());
 	}
 	
 	/**
 	 * Creates a new car-type in the database, with the name provided. Names are 
 	 * unique, so if a car-type with the provided name already exists in the 
 	 * database, -1 will be returned. Otherwise the ID-number of the new 
 	 * car-type is returned.
 	 * 
 	 * @param typeName The title of the car-type.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (String typeName) {
 		if (typeName == null || typeName.length() <= 0)
 			throw new NullPointerException();
 		
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "INSERT INTO CarType " +
 			   			   "SET title = '" + typeName + "'";
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return -1;
 			result.next();
 			int newId = result.getInt(1);
 			if (newId > 0) {
 				return newId;
 			}
 		} catch (Exception e) {
 			Logger.write("Couldn't insert row to database: " + e.getMessage());
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * Reads the car-type with the specified ID-number, and returns a Map 
 	 * containing the data about that car-type. If no car-type exists with 
 	 * that ID-number, null will be returned.
 	 * 
 	 * @param id The id of the car-type to read.
 	 * @return Map containing data on success; null on failure.
 	 * 			key 			=> description:
 	 * 			id 				=> The ID of the car-type
 	 * 			name			=> The title of the car-type
 	 */
 	public Map<String, Object> read (int id) {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT typeId, title " +
 						   "FROM CarType " +
 						   "WHERE typeId = '" + id + "' " +
 						   "LIMIT 1";
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			result.next();
 			
 			Map<String, Object> returnMap = new HashMap<String, Object>();
 			returnMap.put("id", 			result.getInt("typeId"));
 			returnMap.put("name", 			result.getString("title"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Reads the car-type with the specified title, and returns a Map 
 	 * containing the data about that car-type. If no car-type exists with 
 	 * that title, null will be returned.
 	 * 
 	 * @param title The title of the car-type to read.
 	 * @return Map containing data on success; null on failure.
 	 * 			key 			=> description:
 	 * 			id 				=> The ID of the car-type
 	 * 			name			=> The title of the car-type
 	 */
 	public Map<String, Object> read (String title) {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT typeId, title " +
 						   "FROM CarType " +
 						   "WHERE title = '" + title + "' " +
 						   "LIMIT 1";
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			result.next();
 			
 			Map<String, Object> returnMap = new HashMap<String, Object>();
 			returnMap.put("id", 			result.getInt("typeId"));
 			returnMap.put("name", 			result.getString("title"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * TODO: Future release: Implement this
 	 * Updates the car-type with the provided ID-number. The fields to be updated, 
 	 * are the keys in the map, and the new data is the values in the map.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, Map<String, Object> updateVars) { return false; }
 	
 	/**
 	 * Deletes the car-type with the provided ID-number. If the deletion fails, 
 	 * false is returned. Otherwise true is returned. Please note: If no car-type 
 	 * is found with the provided ID, true will still be returned, as an entry 
 	 * with that ID isn't in the database after this method-call.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	public boolean delete (int id) {
 		if (id <= 0)
 			throw new NullPointerException();
 		
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "DELETE FROM CarType " +
 						   "WHERE typeId = " + id;
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
 	 * Counts the amount of existing car-types in the database, and returns that 
 	 * amount.
 	 * 
 	 * @return The amount of entries in the data-source.
 	 */
 	public int amountOfEntries () {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT count(*) AS entryAmount " +
 						   "FROM CarType";
 			ResultSet result = conn.query(query);
 			
 			if (result != null) {
 				result.next();
 				return result.getInt(1);
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from database: " + e.getMessage());
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * TODO: Implement this
 	 * Returns a List of all car-types in the database. Every car-type is 
 	 * represented with a Map<String, Object> containing the data about that car-type.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list () {
 List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
 		
 		try {
 			String query =	"SELECT typeId, title " +
 							"FROM CarType " +
 							"ORDER BY title ASC ";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			Map<String, Object> curr;
 			while (result.next()) {
 				curr = new HashMap<String, Object>();
 				curr.put("id", 				result.getInt	("typeId"));
 				curr.put("title", 			result.getString("title"));
 				
 				list.add(curr);
 			}
 		} catch (SQLException e) {
 			Logger.write("Failed to list items from database: " + e.getMessage());
 		}
 		
 		return list;
 	}
 }
