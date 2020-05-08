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
  * Model - Customer.
  * As data-representation of customers in the database, this class provides 
  * several methods for dealing with customers, i.e. creating customers, listing 
  * customers, updating and deleting customer.
  * 
  * <code>
  * 	Customer customer = new Customer();
  * 	// Create a new customer, with the name "Ole Hansen" and phone-number 43569874.
  * 	int newCustomer = customer.create("Ole Hansen", 43569874);
  * 	// Update a customer. Set the name to "Ole S. Hansen" and phone-number to 47172817.
  * 	customer.update(newCustomer, "Ole S. Hansen", 47172817);
  * 	// Delete a customer.
  * 	customer.delete(newCustomer);
  * </code>
  */
 public class Customer extends Model {
 	
 	/**
 	 * Creates a new customer in the database, using the name and the phone 
 	 * provided. Phone-numbers are unique, so creation of a new customer will 
 	 * fail if a customer with the provided phone-number already exists in the 
 	 * database. In that case -1 will be returned. Otherwise the ID-number 
 	 * of the new customer is returned.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * 			key				=> description
 	 * 			name			=> The full name of the customer.
 	 * 			phone			=> The customer's phone-number.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {		
 		if (createVars.get("name").toString() == null || 
 			createVars.get("name").toString().length() <= 0 || 
 			Integer.parseInt(createVars.get("phone").toString()) <= 0)
 				throw new NullPointerException();
 		
 		return super.create(createVars);
 	}
 	
 	/**
 	 * Creates a new customer in the database, using the name and the phone 
 	 * provided. Phone-numbers are unique, so creation of a new customer will 
 	 * fail if a customer with the provided phone-number already exists in the 
 	 * database. In that case -1 will be returned. Otherwise the ID-number 
 	 * of the new customer is returned.
 	 * 
 	 * @param name The full name of the customer.
 	 * @param phone The customer's phone-number.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (String name, int phone) {
 		Map<String, Object> createVars = new HashMap<String, Object>();
 		createVars.put("name", name);
 		createVars.put("phone", phone);
 		return this.create(createVars);
 	}
 	
 	/**
 	 * Creates a new customer in the database, using the name and the phone 
 	 * provided. Phone-numbers are unique, so you can't create a new customer 
 	 * if the provided phone-number already exists in the database. In that 
 	 * case the ID of the user with the phone-number is returned. Otherwise 
 	 * the ID of the new customer created is returned.
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
 	 * Reads the customer with the provided ID, and returns a 
 	 * Map containing the data about that customer. If no customer is found, 
 	 * null is returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 * 			key			=> description
 	 * 			id			=> The ID-number of the customer.
 	 * 			name		=> The name of the customer.
 	 * 			phone		=> The phone-number of the customer.
 	 */
 	public Map<String, Object> read (int id) {
 		return read (id, false);
 	}
 	
 	/**
 	 * Reads the customer with the provided ID or phone-number, and returns a 
 	 * Map containing the data about that customer. If second parameter 
 	 * is true, the number will be handled as a phone-number. Otherwise it 
 	 * will be handled like an ID-number. If no customer is found, null is 
 	 * returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 * 			key			=> description
 	 * 			id			=> The ID-number of the customer.
 	 * 			name		=> The name of the customer.
 	 * 			phone		=> The phone-number of the customer.
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
 	 * Updates the customer with the provided ID-number. The fields to be updated, 
 	 * are the keys in the map, and the new data is the values in the map. Both 
 	 * "name" and "phone" are expected when updated.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * 			key		=> description
 	 * 			name	=> The updated name
 	 * 			phone	=> The new phone-number
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, Map<String, Object> updateVars) {
 		if (id <= 0)
 			throw new NullPointerException();
 		
 		return super.update(id, updateVars, "customerId");
 	}
 	
 	/**
 	 * Updates the customer with the provided ID-number. Both "name" and "phone" 
 	 * are expected when updating.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param newName The updated name.
 	 * @param newPhone The updated phone-number.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update(int id, String newName, int newPhone) {
 		Map<String, Object> update = new HashMap<String, Object>();
 		update.put("name", newName);
 		update.put("phone", newPhone);
 		
 		return this.update(id, update);
 	}
 	
 	/**
 	 * Deletes the customer with the provided ID-number. If the deletion fails, 
 	 * false is returned. Otherwise true is returned. Please note: If no customer 
 	 * is found with the provided ID, true will still be returned, as an entry 
 	 * with that ID isn't in the database after this method-call.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	public boolean delete (int id) {
 		return super.delete(id, "customerId");
 	}
 	
 	/**
 	 * Lists the customers from the database, matching the name and phone-number 
 	 * provided. Also works on partial inputs.
 	 * 
 	 * @param name The name to search for.
 	 * @param phone The phone-number to search for.
 	 * @return A list with all matching entries in the database.
 	 */
 	public List<Map<String, Object>> search (String name, int phone) {
 		return search (name, phone, "name", "ASC");
 	}
 	
 	/**
 	 * Lists the customers from the database, matching the name and phone-number 
 	 * provided. Also works on partial inputs.
 	 * 
 	 * @param name The name to search for.
 	 * @param phone The phone-number to search for.
 	 * @param orderColumn The column to order by.
 	 * @param orderDirection The ordering direction (ASC for ascending; DESC for descending).
 	 * @return A list with all matching entries in the database.
 	 */
 	public List<Map<String, Object>> search (String name, int phone, String orderColumn, String orderDirection) {
 		String phoneSQL	= phone == 0	? "" : phone + "";
 		String nameSQL	= name 	== null	? "" : name;
 		
 		if (orderColumn == null)
 			orderColumn = "name";
 		if (orderDirection == null || 
 		   (!orderDirection.toUpperCase().equals("ASC") && 
 			!orderDirection.toUpperCase().equals("DESC")))
 				orderDirection = "ASC";
 		
 		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
 		
 		try {
 			String query =	"SELECT customerId, name, phone " +
 							"FROM Customer " +
 							"WHERE name LIKE '%" + nameSQL + "%' " +
 							"AND phone LIKE '%" + phoneSQL + "%' " +
 							"ORDER BY " + orderColumn + " " + orderDirection;
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			Map<String, Object> curr;
 			while (result.next()) {
 				curr = new HashMap<String, Object>();
				curr.put("id", result.getInt("customerId"));
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
 	 * Lists the customers from the database, ordered by the column provided.
 	 * 
 	 * @param orderColumn The column to order by.
 	 * @param orderDirection The ordering direction (ASC for ascending; DESC for descending).
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String orderColumn, String orderDirection) {
 		return search(null, 0, orderColumn, orderDirection);
 	}
 	
 	/**
 	 * Lists the customers from the database, ordered by the provided column.
 	 * 
 	 * @param orderColumn The column to order by.
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String orderColumn) { return list(orderColumn, "ASC"); }
 	
 	/**
 	 * Lists the customers from the database in alphabetical order.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list () { return list("name", "ASC"); }
 }
