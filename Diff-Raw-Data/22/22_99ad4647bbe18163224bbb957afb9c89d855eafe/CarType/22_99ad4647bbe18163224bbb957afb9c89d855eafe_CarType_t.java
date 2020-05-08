 package Models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import Util.Logger;
 import Util.MySQLConnection;
 
 /**
  * Model - Car Type
  *
  */
 public class CarType extends Model {
 	
 	/**
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		return create (createVars.get("name").toString());
 	}
 	
 	/**
 	 * TODO: Edit this text
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param createVars Map containing data to be stored.
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
 			result.next();
 			return result.getInt(1);
 		} catch (Exception e) {
 			Logger.write("Couldn't insert row to database: " + e.getMessage());
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * Reads and returns the data with the provided Id in 
 	 * a Map, with data-names as keys. If an entry with 
 	 * the provided ID cannot be found in the data-source, 
 	 * null will be returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 */
 	public Map<String, Object> read (int id) {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT Car.carId, Car.carType, Car.licensePlate, Car.title, CarType.title " +
 						   "FROM Car " +
 						   "WHERE carId = " + id + " " +
 						   "CarType.typeId = Car.carType " + 
 						   "LIMIT 1";
 			ResultSet result = conn.query(query);
 			
 			Map<String, Object> returnMap = new TreeMap<String, Object>();
 			returnMap.put("id", 			result.getInt("Car.carId"));
 			returnMap.put("name", 			result.getInt("Car.title"));
 			returnMap.put("typeId", 		result.getInt("Car.carType"));
 			returnMap.put("typeName", 		result.getInt("CarType.title"));
 			returnMap.put("licensePlate", 	result.getInt("Car.licensePlate"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * TODO: Edit this text
 	 * Reads and returns the data with the provided Id in 
 	 * a Map, with data-names as keys. If an entry with 
 	 * the provided ID cannot be found in the data-source, 
 	 * null will be returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 */
 	public Map<String, Object> read (String title) {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT Car.carId, Car.carType, Car.licensePlate, Car.title, CarType.title " +
 						   "FROM Car " +
 						   "WHERE Car.title = '" + title + "' " +
 						   "CarType.typeId = Car.carType " + 
 						   "LIMIT 1";
 			ResultSet result = conn.query(query);
 			
 			Map<String, Object> returnMap = new TreeMap<String, Object>();
 			returnMap.put("id", 			result.getInt("Car.carId"));
 			returnMap.put("name", 			result.getInt("Car.title"));
 			returnMap.put("typeId", 		result.getInt("Car.carType"));
 			returnMap.put("typeName", 		result.getInt("CarType.title"));
 			returnMap.put("licensePlate", 	result.getInt("Car.licensePlate"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Updates the entry with the provided ID in the data-
 	 * source. The data to be updated is the keys in the map, 
 	 * and the values are the new data. If then entry is 
 	 * successfully updated, true will be returned. If the 
 	 * update failed (invalid ID or similar), false will 
 	 * be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, Map<String, Object> updateVars) { return false; }
 	
 	/**
 	 * Deletes the entry with the provided ID in the data-
 	 * source. On success true will be returned. If the 
 	 * deletion failed (invalid ID or similar), false 
 	 * will be returned.
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
			result.next();
			if (result != null) {
				return true;
			}
		} catch (Exception e) {
			Logger.write("Couldn't delete row from database: " + e.getMessage());
		}
		
		return false;
	}
 	
 	/**
 	 * Gives the amount of entries in the data-source, 
 	 * i.e. the amount of customers in the database.
 	 * 
 	 * @return The amount of entries in the data-source.
 	 */
 	public int amountOfEntries () {
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = "SELECT count(*) AS entryAmount " +
 						   "FROM CarType";
 			ResultSet result = conn.query(query);
 			
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
 	public List list () { return null; }
 }
