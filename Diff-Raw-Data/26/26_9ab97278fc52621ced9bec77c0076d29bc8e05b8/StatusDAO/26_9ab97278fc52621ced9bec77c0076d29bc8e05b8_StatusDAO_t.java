 package ru.allgage.geofriend.server;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Status data access object.
  */
 public class StatusDAO {
 	private Connection connection;
 
 	/**
 	 * Create data access object instance.
 	 *
 	 * @param connection server connection.
 	 */
 	public StatusDAO(Connection connection) {
 		this.connection = connection;
 	}
 
 	/**
 	 * Creates the status in the database.
 	 *
 	 * @param user      user.
 	 * @param latitude  latitude.
 	 * @param longitude longitude.
 	 * @param text      message text.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public boolean create(User user, double latitude, double longitude, String text) throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"INSERT INTO statuses (user_id, time, status, lat, lng) VALUES (?, ?, ?, ?, ?)")) {
 			statement.setInt(1, user.getId());
 			statement.setTimestamp(2, SQLHelper.convertDate(new Date()));
 			statement.setString(3, text);
 			statement.setDouble(4, latitude);
 			statement.setDouble(5, longitude);
 
 			return statement.executeUpdate() > 0;
 		}
 	}
 
 	/**
 	 * Returns actual statuses of all users.
 	 *
 	 * @return list with statuses.
 	 * @throws SQLException thrown on query fail.
 	 */
 	public List<Status> getStatuses() throws SQLException {
 		try (PreparedStatement statement = connection.prepareStatement(
 				"SELECT " +
 						"users.id AS user_id, " +
 						"users.login AS login, " +
 						"users.email AS email, " +
 						"statuses.id AS status_id, " +
 						"statuses.time AS time, " +
 						"statuses.lat AS lat, " +
 						"statuses.lng AS lng, " +
 						"statuses.status AS text " +
 						"FROM statuses " +
 						"JOIN users ON (statuses.user_id = users.id)")) {
 			// TODO: GROUP BY clause for status filtering.
 			try (ResultSet resultSet = statement.executeQuery()) {
 				List<Status> result = new ArrayList<>();
 				while (resultSet.next()) {
 					int userId = resultSet.getInt("user_id");
 					String login = resultSet.getString("login");
 					String email = resultSet.getString("email");
 					int statusId = resultSet.getInt("status_id");
 					Timestamp dateTime = resultSet.getTimestamp("time");
 					double latitude = resultSet.getDouble("lat");
 					double longitude = resultSet.getDouble("lng");
 					String text = resultSet.getString("text");
 
 					User user = new User(userId, login, email);
 					Status status = new Status(statusId, user, dateTime, latitude, longitude, text);
 
 					result.add(status);
 				}
 
 				return result;
 			}
 		}
 	}
 
     public List<Status> getOnlineStatuses() throws SQLException {
         try (PreparedStatement statement = connection.prepareStatement(
                 "SELECT " +
                         "users.id AS user_id, " +
                         "users.login AS login, " +
                         "users.email AS email, " +
                         "statuses.id AS status_id, " +
                        "statuses.time AS time, " +
                         "statuses.lat AS lat, " +
                         "statuses.lng AS lng, " +
                         "statuses.status AS text " +
                         "FROM statuses " +
                         "JOIN users ON (statuses.user_id = users.id) " +
                        "WHERE statuses.time = (SELECT " +
                        "MAX(s2.time) " +
                        "FROM statuses AS s2 " +
                        "WHERE s2.user_id = statuses.user_id) AND users.isonline = 1 " +
                        "GROUP BY statuses.user_id" )) {
             try (ResultSet resultSet = statement.executeQuery()) {
                 List<Status> result = new ArrayList<>();
                 while (resultSet.next()) {
                     int userId = resultSet.getInt("user_id");
                     String login = resultSet.getString("login");
                     String email = resultSet.getString("email");
                     int statusId = resultSet.getInt("status_id");
                     Timestamp dateTime = resultSet.getTimestamp("time");
                     double latitude = resultSet.getDouble("lat");
                     double longitude = resultSet.getDouble("lng");
                     String text = resultSet.getString("text");
 
                     User user = new User(userId, login, email);
                     Status status = new Status(statusId, user, dateTime, latitude, longitude, text);
 
                     result.add(status);
                 }
 
                 return result;
             }
         }
 
     }
 }
