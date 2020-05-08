 package edu.uci.lighthouse.model.util;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Properties;
 import java.util.TimeZone;
 
 /**
  * Utility class for database tasks.
  * 
  * @author tproenca
  * 
  * TODO: Unit testing for the all methods.
  * 
  */
 public class DatabaseUtility {
 
 	private static TimeZone serverTimeZone;
 	
 	/**
 	 * Returns a <code>Date</code> object adjusted for the given timezone.
 	 * 
 	 * @param dt The date to be adjusted.
 	 * @param tz The desired timezone.
 	 * @return
 	 */
 	public static Date getAdjustedDateTime(Date dt, TimeZone tz) {
 		Date result = null;
 		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
 				DateFormat.FULL);
 		df.setTimeZone(tz);
 		try {
 			result = df.parse(df.format(dt));
 		} catch (ParseException e) {
 			// The exception will never be thrown, since we are parsing the same
 			// pattern set in the DateFormat object.
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the Server timezone.
 	 * 
 	 * @param dbSettings
 	 *            The database connection settings.
 	 * @return A <code>TimeZone</code> object.
 	 * @throws SQLException
 	 */
 	public static TimeZone getServerTimezone(Properties dbSettings)
 			throws SQLException {
 		if (serverTimeZone == null) {
 			String url = dbSettings.getProperty("hibernate.connection.url");
 			String username = dbSettings
 					.getProperty("hibernate.connection.username");
 			String password = dbSettings
 					.getProperty("hibernate.connection.password");
 
 			Connection conn = DriverManager.getConnection(url, username,
 					password);
 
 			ResultSet rs = conn
 					.createStatement()
 					.executeQuery(
 							"select timestampdiff(HOUR,utc_timestamp(), current_timestamp());");
 			String timediff = "";
 			if (rs.next()) {
 				timediff = rs.getString(1);
 			}
 			conn.close();
 
 			serverTimeZone = TimeZone.getTimeZone("GMT" + timediff);
 		}
 		return serverTimeZone;
 	}
 
 	/**
 	 * Returns whether is possible to connect to the database server for a given
 	 * connection settings.
 	 * 
 	 * @param dbSettings
 	 *            The datatabase connection settings
 	 * @return <code>true</code> if is possible to connect to the database
 	 *         server and <code>false</code> otherwise.
 	 */
	public static boolean canConnect(Properties dbSettings) {
 		String url = dbSettings.getProperty("hibernate.connection.url");
 		String username = dbSettings
 				.getProperty("hibernate.connection.username");
 		String password = dbSettings
 				.getProperty("hibernate.connection.password");
		try {
 			Connection conn = DriverManager.getConnection(url, username,
 					password);
 			conn.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
 	}
 }
