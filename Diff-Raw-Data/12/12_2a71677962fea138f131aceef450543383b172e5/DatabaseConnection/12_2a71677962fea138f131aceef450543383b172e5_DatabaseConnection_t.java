 package de.reneruck.tcd.ipp.datamodel.database;
 
 import java.net.ConnectException;
 import java.sql.ResultSet;
 import java.util.Date;
 
 import de.reneruck.tcd.ipp.datamodel.Airport;
 import de.reneruck.tcd.ipp.datamodel.Booking;
 
 public interface DatabaseConnection {
 
 	/**
 	 * Checks if a specific booking exists.
 	 * 
 	 * @param booking_id
 	 *            the id of the desired booking
 	 * @return true if a booking with the given booking_id exists
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract boolean bookingExists(long booking_id) throws ConnectException;
 
 	/**
 	 * Returns the count of bookings already made for the given flight
 	 * 
 	 * @param flightId
 	 *            the flightId(DB id) of the desired flight
 	 * @return the count of bookings for the given flight
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract int getBookingCountForFlight(int flightId) throws ConnectException;
 
 	/**
 	 * Looks for a flight at the given {@link Date} +/- 1 hour, from the given
 	 * {@link Airport}
 	 * 
 	 * @param date
 	 *            the date to look for a flight
 	 * @param from
 	 *            the airport to fly from
 	 * @return the id of the flight if found one, otherwise 0 will be returned.
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract int getFlightForDate(Date date, Airport from) throws ConnectException;
 
 	/**
 	 * Inserts a new booking into the database.
 	 * 
 	 * @param booking
 	 *            the {@link Booking} Object to be inserted.
 	 * @param flightId
 	 *            the flight to make this booking for
 	 * @return the database id of the booking if successful or 0 if not.
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract int makeABooking(Booking booking, long flightId) throws ConnectException;
 
 	/**
 	 * Executes the given insert statement and returns all generated ID's that
 	 * were made with this statement.
 	 * 
 	 * @param sql
 	 *            insert statement
 	 * @return The {@link ResultSet} of generated ID's or null if none have been
	 *         generated or an error occurred.
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract ResultSet executeInsert(String sql) throws ConnectException;
 
 	/**
 	 * Executes a given SQL statement without any check before.<br>
 	 * Only useful for inserts. Will not return anything from the database.
 	 * 
 	 * @param sqlStatement
 	 *            the statement to execute
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract void executeSql(String sqlStatement) throws ConnectException;
 
 	/**
 	 * Executes the given update statement and returns all generated ID's that
 	 * were made with this statement.
 	 * 
 	 * @param sql
 	 *            update statement
 	 * @return The {@link ResultSet} of generated ID's or null if none have been
	 *         generated or an error occurred.
 	 * @throws ConnectException
 	 *             can occur if no connection to the database is available
 	 */
 	public abstract ResultSet executeUpdate(String sql) throws ConnectException;
 	
 	/**
 	 * Tries to close the {@link DatabaseConnection}
 	 */
 	public abstract void close();
 
 	/**
 	 * Gets the overall count of bookings in the system.
	 * 
	 * @return a number between 0 and MAX_INT.
 	 * 
 	 * @throws ConnectException
 	 */
 	public abstract int getBookingsCount() throws ConnectException;
 
 }
