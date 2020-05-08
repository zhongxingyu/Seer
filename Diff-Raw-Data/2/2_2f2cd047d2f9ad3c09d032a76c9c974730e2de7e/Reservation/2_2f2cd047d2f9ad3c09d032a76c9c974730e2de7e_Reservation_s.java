 package Models;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import Util.Logger;
 import Util.MySQLConnection;
 
 /**
  * Model - Reservation
  *
  */
 public class Reservation extends Model {
 	
 	/**
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * 			key			=> description
 	 * 			customer	=> The ID of the customer to book a car.
 	 * 			carType		=> The ID of the car-type to be booked.
 	 * 			startDate	=> The start date of the booking.
 	 * 			endDate		=> The end date of the booking.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		int customer = Integer.parseInt(createVars.get("customer").toString());
 		int carType = Integer.parseInt(createVars.get("carType").toString());
 		Date startDate = (Date) createVars.get("startDate");
 		Date endDate = (Date) createVars.get("endDate");
 		
 		return create (customer, carType, startDate, endDate);
 	}
 	
 	/**
 	 * Creates an entry in the particular data-source, with 
 	 * the data given in the Map. The ID of the new entry 
 	 * is returned on success.
 	 * 
 	 * @param customer The ID of the customer to book a car.
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking (format: YYYY-MM-DD).
 	 * @param endDate The end date of the booking (format: YYYY-MM-DD).
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (int customerId, int carType, Date startDate, Date endDate) {
 		if (customerId <= 0 || carType <= 0 || startDate == null || endDate == null)
 			throw new NullPointerException();
 		
 		try {
 			int freeCar = findFreeCar(carType, startDate, endDate); // Find available car
 			Customer Customer = new Customer(); // Verify customer-ID
 			if (freeCar <= 0 || Customer.read(customerId) == null)
 				return -1;
 			
 			String query =	"INSERT INTO Reservation " +
 							"SET " +
 							"customerId =  " + customerId 	+ ", " +
 							"carId 		=  " + freeCar 		+ ", " +
 							"startDate 	= '" + startDate 	+ "', " +
 							"endDate 	= '" + endDate 		+ "'";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result != null) {
 				result.next();
 				return result.getInt(1);
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't book car: " + e.getMessage());
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
 	 * 			key			=> Description
 	 * 			id			=> The ID of the reservation.
 	 * 			car			=> The ID of the car booked.
 	 * 			customer	=> The ID of the customer booking the car.
 	 * 			startDate	=> Start date of reservation (YYYY-MM-DD).
 	 * 			endDate		=> End date of reservation (YYYY-MM-DD).
 	 */
 	public Map<String, Object> read (int id) {
 		if (id <= 0)
 			throw new NullPointerException();
 		
 		try {
 			String query =	"SELECT reservationId, carId, customerId, startDate, endDate " +
 							"FROM Reservation " +
 							"WHERE reservationId = " + id + " " +
 							"LIMIT 1";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			
 			result.next();
 			
 			Map<String, Object> returnMap = new HashMap<String, Object>();
 			returnMap.put("id", 			result.getInt	("reservationId"));
 			returnMap.put("car", 			result.getInt	("carId"));
 			returnMap.put("customer",		result.getInt	("customerId"));
 			returnMap.put("startDate",		result.getDate	("startDate"));
 			returnMap.put("endDate",		result.getDate	("endDate"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * TODO: Implement this
 	 * Updates the entry with the provided ID in the data-
 	 * source. The data to be updated is the keys in the map, 
 	 * and the values are the new data. If then entry is 
 	 * successfully updated, true will be returned. If the 
 	 * update failed (invalid ID or similar), false will 
 	 * be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * 			key			=> description
 	 * 			customer	=> The ID of the customer to book a car.
 	 * 			carType		=> The ID of the car-type to be booked.
 	 * 			startType	=> The start date of the booking.
 	 * 			endDate		=> The end date of the booking.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update (int id, Map<String, Object> updateVars) { return false; }
 	
 	/**
 	 * TODO: Implement this
 	 * Updates the entry with the provided ID in the data-
 	 * source. The data to be updated is the keys in the map, 
 	 * and the values are the new data. If then entry is 
 	 * successfully updated, true will be returned. If the 
 	 * update failed (invalid ID or similar), false will 
 	 * be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param customer The ID of the customer to book a car.
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking.
 	 * @param endDate The end date of the booking.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update (int id, int customer, int carType, Date startDate, Date endDate) { return false; }
 	
 	/**
 	 * TODO: Implement this
 	 * Deletes the entry with the provided ID in the data-
 	 * source. On success true will be returned. If the 
 	 * deletion failed (invalid ID or similar), false 
 	 * will be returned.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	public boolean delete (int id) { return false; }
 	
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
 						   "FROM Reservation";
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
 	 * TODO: Implement this
 	 * Lists the entries of the data-source.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list () { return null; }
 	
 	/**
 	 * TODO: Implement this
 	 * TODO: More list()-methods
 	 * Lists the entries of the data-source, from the user 
 	 * with the provided ID.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (int id) { return null; }
 	
 	/**
 	 * Finds a free car in the specified period, with the specified car-type. 
 	 * Car ID is returned if car is found; Otherwise -1 is returned.
 	 * 
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking.
 	 * @param endDate The end date of the booking.
 	 * @return Car ID on success; -1 on failure.
 	 */
 	public int findFreeCar (int carType, Date startDate, Date endDate) {
 		if (carType <= 0 || startDate == null || endDate == null)
 			throw new NullPointerException();
 		
 		try {
 			String query =	
 				"SELECT carId " + 
 				"FROM Car " + 
 				"WHERE carType = " + carType + " " + 
 				"AND NOT EXISTS ( " + 
 					"SELECT reservationId " + 
 					"FROM Reservation " + 
					"WHERE Reservation.carId = car.carId " +
 					   "AND (('"+startDate+"' 	>= startDate && '"+startDate+"' <= endDate) " + 
 					     "OR ('"+endDate+"' 	>= startDate && '"+endDate+"' 	<= endDate) " + 
 					     "OR ('"+startDate+"' 	<= startDate && '"+endDate+"' 	>= endDate)) " + 
 				")";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result != null) {
 				result.next();
 				return result.getInt(1);
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't find free car: " + e.getMessage());
 		}
 		
 		return -1;
 	}
 }
