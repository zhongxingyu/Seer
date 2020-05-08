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
  * TODO: Write class description
  * TODO: Rewrite javadoc
  * Model - Customer
  *
  */
 public class Customer extends Model {
 	
 	/**
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * 			key				=> description
 	 * 			name			=> The full name of the customer.
 	 * 			phone			=> The customer's phone-number.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		String name = createVars.get("name").toString();
 		int phone = Integer.parseInt(createVars.get("phone").toString());
 		return create(name, phone);
 	}
 	
 	/**
 	 * TODO: Edit this text
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param name The full name of the customer.
 	 * @param phone The customer's phone-number.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (String name, int phone) {
 		if (name == null || name.length() <= 0 || phone <= 0)
 				throw new NullPointerException();
 			
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			String query = 	"INSERT INTO Customer " +
 							"SET name = '" + name + "', " + 
 							"phone = '" + phone + "'";
 			ResultSet result = conn.query(query);
			if (result == null)
				return null;
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
 	 * Creates a new customer in the database. This method differs from create(), 
 	 * as this returns the ID of an existing customer in the database, if 
 	 * another customer with the given phone-number is found.
 	 * 
 	 * @param name The name of the customer (is overlooked if a customer with 
 	 * 			   the same phone-number is found in the database).
 	 * @param phone The phone-number of the customer. This is also used to 
 	 * 				look through the database, in order to find an existing 
 	 * 				customer with the same phone-number.
 	 * @return The ID number of the new or existing customer.
 	 */
 	public int createIfNew (String name, int phone) {
 		Map<String, Object> existingCustomer = read (phone, true);
 		if (existingCustomer != null)
 			return Integer.parseInt(existingCustomer.get("id").toString());
 		
 		return create (name, phone);
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
 	public Map<String, Object> read (int id) {
 		return read (id, false);
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
 	public Map<String, Object> read (int phoneId, boolean phone) {
 		if (phoneId <= 0)
 			throw new NullPointerException();
 		
 		String query = "";
 		if (!phone) {
 			query = "SELECT customerId, name, phone " +
 					"FROM Customer " +
 					"WHERE customerId = " + phoneId + " " + 
 					"LIMIT 1";
 		} else {
 			query = "SELECT customerId, name, phone " +
 					"FROM Customer " +
 					"WHERE phone = " + phoneId + " " + 
 					"LIMIT 1";
 		}
 		
 		try {
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			
 			result.next();
 			
 			Map<String, Object> returnMap = new HashMap<String, Object>();
 			returnMap.put("id", 			result.getInt	("customerId"));
 			returnMap.put("name", 			result.getString("name"));
 			returnMap.put("phone", 			result.getInt	("phone"));
 			
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
 	 * 			key		=> description
 	 * 			name	=> The updated name
 	 * 			phone	=> The new phone-number
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, Map<String, Object> updateVars) {
 		String newName = updateVars.get("name").toString();
 		int newPhone = Integer.parseInt(updateVars.get("phone").toString());
 		return update (id, newName, newPhone);
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
 	 * @param newName The updated name.
 	 * @param newPhone The updated phone-number.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, String newName, int newPhone) {
 		if (id <= 0 || newName == null || newPhone <= 0)
 			throw new NullPointerException();
 		
 		try {
 			String query =	"UPDATE Customer " +
 							"SET name = '" + newName + "', " +
 							"phone = '" + newPhone + "' " +
 							"WHERE customerId = " + id;
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			result.next();
 			if (result != null) {
 				return true;
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't update row: " + e.getMessage());
 		}
 		
 		return false;
 	}
 	
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
 			String query = "DELETE FROM Customer " +
 						   "WHERE customerId = " + id;
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
 						   "FROM Customer";
 			ResultSet result = conn.query(query);
 			
 			result.next();
 			return result.getInt(1);
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from database: " + e.getMessage());
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * Lists the customers from the database.
 	 * 
 	 * @param sortColumn The column to sort by.
 	 * @param sortOrder The sorting direction (ASC for ascending; DESC for descending).
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String sortColumn, String sortOrder) {
 		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
 		
 		try {
 			String query =	"SELECT customerId, name, phone " +
 							"FROM Customer " +
 							"ORDER BY " + sortColumn + " " + sortOrder;
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			Map<String, Object> curr = new HashMap<String, Object>();
 			while (result.next()) {
 				curr.put("id", result.getString("customerId"));
 				curr.put("name", result.getString("name"));
 				curr.put("phone", result.getInt("phone"));
 				
 				list.add(curr);
 			}
 		} catch (SQLException e) {
 			Logger.write("Failed to list items from database: " + e.getMessage());
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Lists the customers from the database.
 	 * 
 	 * @param sortColumn The column to sort by.
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String sortColumn) { return list(sortColumn, "ASC"); }
 	
 	/**
 	 * Lists the customers from the database.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list () { return list("name", "ASC"); }
 }
