 package ctrl;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.LinkedList;
 import java.util.List;
 
 import data.Notification;
 
 public class DBNotification extends DBManager {
 	/**
 	 * Save a notification to the database
 	 * 
 	 * @param notif
 	 */
 	public static void saveNotification(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "INSERT INTO notification VALUES('"
 					+ notif.getRecipientEmail() + "', '"
 					+ notif.getSenderEmail() + "', current_timestamp" + ", '"
 					+ notif.getMessage() + "', '" + notif.getAction() + "', '"
 					+ notif.getStatus() + "')";
 			System.out.println(update);
 			con.setAutoCommit(false);
 			stmt.executeUpdate(update);
 			try {
 				con.commit();
 			} catch (SQLException exc) {
 				con.rollback(); // bei Fehlschlag Rollback der Transaktion
 				System.out
 						.println("COMMIT fehlgeschlagen - Rollback durchgefuehrt");
 			} finally {
 				closeQuietly(stmt);
 				closeQuietly(con); // Abbau Verbindung zur Datenbank
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Delete a notification based on it's unique recipientEmail,
 	 * senderEmail,timeStamp
 	 * 
 	 * @param sub
 	 */
 	public static void deleteSubject(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "DELETE FROM notification WHERE recipientEmail = '"
 					+ notif.getRecipientEmail() + "' AND " + "senderEmail = '"
 					+ notif.getSenderEmail() + "' AND " + "timeStamp = "
 					+ notif.getTimeStamp();
 			con.setAutoCommit(false);
 			stmt.executeUpdate(update);
 			try {
 				con.commit();
 			} catch (SQLException exc) {
 				con.rollback(); // bei Fehlschlag Rollback der Transaktion
 				System.out.println("COMMIT fehlgeschlagen - "
 						+ "Rollback durchgefuehrt");
 			} finally {
 				closeQuietly(stmt);
 				closeQuietly(con); // Abbau Verbindung zur Datenbank
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Update a specific subject (identified by the version, subTitle and
 	 * modTitle) with the data from the subject object.
 	 * 
 	 * @param notif
 	 * @param recipientEmail
 	 * @param senderEmail
 	 * @param timeStamp
 	 */
 	public static void updateNotification(Notification notif,
 			String recipientEmail, String senderEmail, Timestamp timeStamp) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "UPDATE notification SET recipientEmail = '"
 					+ notif.getRecipientEmail() + "', senderEmail = '"
 					+ notif.getSenderEmail() + "', timeStamp = '"
 					+ notif.getTimeStamp() + "', message = '"
 					+ notif.getMessage() + "', action = '" + notif.getAction()
 					+ "', " + "status= '" + notif.getStatus() + "' ,'"
 					+ "WHERE recipientEmail = '" + recipientEmail + "' AND "
 					+ "senderEmail = '" + senderEmail + "' AND "
 					+ " timeStamp = " + timeStamp;
 			con.setAutoCommit(false);
 			stmt.executeUpdate(update);
 			try {
 				con.commit();
 			} catch (SQLException exc) {
 				con.rollback(); // bei Fehlschlag Rollback der Transaktion
 				System.out.println("COMMIT fehlgeschlagen - "
 						+ "Rollback durchgefuehrt");
 			} finally {
 				closeQuietly(stmt);
 				closeQuietly(con); // Abbau Verbindung zur Datenbank
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
	 * loads a subject based on the specific recipientEmail, senderEmail and
 	 * timeStamp
 	 * 
 	 * @param recipientEmail
 	 * @param senderEmail
 	 * @param timeStamp
 	 * @return notif
 	 */
 	public static List<Notification> loadNotification(String recipientEmail,
 			String senderEmail) {
 		List<Notification> notif = new LinkedList<Notification>();
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification WHERE recipientEmail = '"
 					+ recipientEmail
 					+ "' AND "
 					+ "senderEmail = '"
 					+ senderEmail + "'";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat));
 			}
 			closeQuietly(rs);
 			closeQuietly(stmt);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeQuietly(con);
 		}
 		return notif;
 	}
 	/**
 	 * Loads all notification that were sent or recieved by one user.
 	 * @param email
 	 * @return
 	 */
 	public static List<Notification> loadNotification(String email) {
 		List<Notification> notif = new LinkedList<Notification>();
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification WHERE recipientEmail = '"
 					+ email
 					+ "' OR "
 					+ "senderEmail = '"
 					+ email + "'";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat));
 			}
 			closeQuietly(rs);
 			closeQuietly(stmt);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeQuietly(con);
 		}
 		return notif;
 	}
 	
 	public static List<Notification> loadNotification() {
 		List<Notification> notif = new LinkedList<Notification>();
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat));
 			}
 			closeQuietly(rs);
 			closeQuietly(stmt);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeQuietly(con);
 		}
 		return notif;
 	}
 
 	public static Notification loadNotification(String recipientEmail,
 			String senderEmail, Timestamp timeStamp) {
 		Notification notif = null;
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification WHERE recipientEmail = '"
 					+ recipientEmail
 					+ "' AND "
 					+ "senderEmail = '"
					+ senderEmail
					+ "' AND "
					+ "timeStamp = "
					+ timeStamp
 					+ ";";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 
 				notif = new Notification(recEm, senEm, timS, mess, act, stat);
 			}
 			closeQuietly(rs);
 			closeQuietly(stmt);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeQuietly(con);
 		}
 		return notif;
 	}
 
 	public static void main(String[] args) {
 		Notification notif = new Notification("test3", "test4", new Timestamp(
 				System.currentTimeMillis()), "Edited: Modulverantwortlicher",
 				"edit", "ausstehend");
 		saveNotification(notif);
 		System.out.println("saved");
 		List<Notification> notifs = loadNotification("test3", "test4");
 
 		System.out.println(notifs.get(0).getTimeStamp());
 		System.out.println(notifs.get(0).getMessage());
 
 	}
 
 }
