 package db;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.LinkedList;
 import java.util.List;
 
 import data.DeadlineNotification;
 import data.ExRules;
 import data.ModManual;
 import data.Modification;
 import data.ModificationNotification;
 import data.Module;
 import data.Notification;
 import data.Subject;
 
 public class DBNotification extends DBManager {
 
 	/**
 	 * Save a notification to the database
 	 * 
 	 * @param notif
 	 */
 	public static void saveNotification(Notification notif) {
 		Connection con = null;
 
 		// checks the instance of the Notification
 		if (notif instanceof ModificationNotification) {
 			ModificationNotification modNotif = (ModificationNotification) notif;
 			String update = "";
 
 			// checks the instance of the editable
 			if (modNotif.getModification().getBefore() instanceof ExRules) {
 				ExRules edit = (ExRules) modNotif.getModification().getBefore();
 				update = "INSERT INTO notification(RecipientEmail, SenderEmail, Timestamp, Message, Action, Status, isReadSender, isReadRecipient, exRulesTitle) Values('"
 						+ notif.getRecipientEmail()
 						+ "', '"
 						+ notif.getSenderEmail()
 						+ "', '"
 						+ (Timestamp) notif.getTimeStamp()
 						+ "', '"
 						+ notif.getMessage()
 						+ "', '"
 						+ notif.getAction()
 						+ "', '"
 						+ notif.getStatus()
 						+ "', "
 						+ false
 						+ ", "
 						+ false
 						+ ", '"
 						+ edit.getExRulesTitle() + "')";
 			} else if (modNotif.getModification().getBefore() instanceof ModManual) {
 				ModManual edit = (ModManual) modNotif.getModification()
 						.getBefore();
 				update = "INSERT INTO notification(RecipientEmail, SenderEmail, Timestamp, Message, Action, Status, isReadSender, isReadRecipient, ExRulesTitle, ModManTitle) Values('"
 						+ notif.getRecipientEmail()
 						+ "', '"
 						+ notif.getSenderEmail()
 						+ "', '"
 						+ (Timestamp) notif.getTimeStamp()
 						+ "', '"
 						+ notif.getMessage()
 						+ "', '"
 						+ notif.getAction()
 						+ "', '"
 						+ notif.getStatus()
 						+ "', "
 						+ false
 						+ ", "
 						+ false
 						+ ", '"
 						+ edit.getExRulesTitle()
 						+ "', '"
 						+ edit.getModManTitle() + "')";
 			} else if (modNotif.getModification().getBefore() instanceof Module) {
 				Module edit = (Module) modNotif.getModification().getBefore();
 				update = "INSERT INTO notification(RecipientEmail, SenderEmail, Timestamp, Message, Action, Status, isReadSender, isReadRecipient, modTitle) Values('"
 						+ notif.getRecipientEmail()
 						+ "', '"
 						+ notif.getSenderEmail()
 						+ "', '"
 						+ (Timestamp) notif.getTimeStamp()
 						+ "', '"
 						+ notif.getMessage()
 						+ "', '"
 						+ notif.getAction()
 						+ "', '"
 						+ notif.getStatus()
 						+ "', "
 						+ false
 						+ ", "
 						+ false
 						+ ", '"
 						+ edit.getModTitle() + "')";
 			} else if (modNotif.getModification().getBefore() instanceof Subject) {
 				Subject edit = (Subject) modNotif.getModification().getBefore();
 				update = "INSERT INTO notification(RecipientEmail, SenderEmail, Timestamp, Message, Action, Status, isReadSender, isReadRecipient, ModTitle, SubTitle) Values('"
 						+ notif.getRecipientEmail()
 						+ "', '"
 						+ notif.getSenderEmail()
 						+ "', '"
 						+ (Timestamp) notif.getTimeStamp()
 						+ "', '"
 						+ notif.getMessage()
 						+ "', '"
 						+ notif.getAction()
 						+ "', '"
 						+ notif.getStatus()
 						+ "', "
 						+ false
 						+ ", "
 						+ false
 						+ ", '"
 						+ edit.getModTitle()
 						+ "', '"
 						+ edit.getSubTitle()
 						+ "')";
 			}
 			try {
 				con = openConnection();
 				Statement stmt = con.createStatement();
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
 		} else if (notif instanceof DeadlineNotification) {
 			DeadlineNotification deadNotif = (DeadlineNotification) notif;
 			try {
 				con = openConnection();
 				Statement stmt = con.createStatement();
 				String update = "INSERT INTO notification(RecipientEmail, SenderEmail, Timestamp, Message, Action, Status, isReadSender, isReadRecipient, modManTitle, deadline) Values('"
 						+ notif.getRecipientEmail()
 						+ "', '"
 						+ notif.getSenderEmail()
 						+ "', '"
 						+ (Timestamp) notif.getTimeStamp()
 						+ "', '"
 						+ notif.getMessage()
 						+ "', '"
 						+ notif.getAction()
 						+ "', '"
 						+ "deadline"
 						+ "', "
 						+ false
 						+ ", "
 						+ false
 						+ ", '"
 						+ deadNotif.getModManTitle()
 						+ "', '"
 						+ (Timestamp) deadNotif.getDeadline() + "')";
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
 		} else if (notif instanceof Notification) {
 			try {
 				con = openConnection();
 				Statement stmt = con.createStatement();
 				String update = "INSERT INTO notification VALUES('"
 						+ notif.getRecipientEmail() + "', '"
 						+ notif.getSenderEmail() + "', '"
 						+ (Timestamp) notif.getTimeStamp() + "', '"
 						+ notif.getMessage() + "', '" + notif.getAction()
 						+ "', '" + notif.getStatus() + "', " + 0 + ")";
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
 	}
 
 	/**
 	 * Delete a notification based on it's unique recipientEmail,
 	 * senderEmail,timeStamp
 	 * 
 	 * @param notif
 	 */
 	public static void deleteNotification(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "DELETE FROM notification WHERE senderEmail = '"
 					+ notif.getSenderEmail() + "' AND " + "timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp() + "';";
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
 	 * deletes one deadline notification specified by the sender and recipient
 	 * 
 	 * @param notif
 	 */
 	public static void deleteDeadlineNotification(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "DELETE FROM notification WHERE senderEmail = '"
 					+ notif.getSenderEmail() + "' AND " + "timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp()
 					+ "' AND recipientEmail ='" + notif.getRecipientEmail()
 					+ "'";
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
 	 * Update a specific notification to isReadSender = true
 	 * 
 	 * @param notif
 	 * 
 	 */
 	public static void updateNotificationIsReadSender(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "UPDATE notification SET isReadSender = " + true
 					+ " WHERE senderEmail = '" + notif.getSenderEmail()
 					+ "' AND " + " timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp() + "';";
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
 	 * Update a specific notification to isReadRecipient = true
 	 * 
 	 * @param notif
 	 * 
 	 */
 	public static void updateNotificationIsReadRecipient(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "UPDATE notification SET isReadRecipient = " + true
 					+ " WHERE recipientEmail = '" + notif.getRecipientEmail()
 					+ "' AND " + "senderEmail = '" + notif.getSenderEmail()
 					+ "' AND " + " timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp() + "';";
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
 	 * Update a specific notifications status to declined and sets isReadSender to false
 	 * 
 	 * @param Notification
 	 * @return boolean
 	 */
 	public static boolean declineNotification(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "UPDATE notification SET status = 'declined', isReadSender = false "
 					+ "WHERE senderEmail = '"
 					+ notif.getSenderEmail()
 					+ "' AND "
 					+ " timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp() + "'";
 			con.setAutoCommit(false);
 			System.out.println(update);
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
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Update a specific notifications status to accepted and sets isReadSender to false
 	 * 
 	 * @param Notification
 	 * @return boolean
 	 */
 	public static boolean acceptNotification(Notification notif) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
			String update = "UPDATE notification SET status = 'accepted', isReadSender = false  "
 					+ "WHERE "
 					+ "senderEmail = '"
 					+ notif.getSenderEmail()
 					+ "' AND "
 					+ " timeStamp = '"
 					+ (Timestamp) notif.getTimeStamp() + "'";
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
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * loads a notification based on the specific recipientEmail, senderEmail
 	 * and timeStamp
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
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat,
 						isReadS, isReadR));
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
 	 * 
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
 					+ email + "' OR " + "senderEmail = '" + email + "'";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat,
 						isReadS, isReadR));
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
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 
 				notif.add(new Notification(recEm, senEm, timS, mess, act, stat,
 						isReadS, isReadR));
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
 	 * loads all modNotifs
 	 * 
 	 * @return
 	 */
 	public static List<Notification> loadModificationNotificationModEx(
 			String currentUser) {
 		List<Notification> notif = new LinkedList<Notification>();
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification WHERE senderEmail= '"
 					+ currentUser + "' OR recipientEmail='" + currentUser
 					+ "' AND senderEmail= '"
 					+ DBUser.loadUsersEmailByRole("Dekan").get(0) + "'";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 				String exRules = rs.getString("exRulesTitle");
 				String modMan = rs.getString("modManTitle");
 				String mod = rs.getString("modTitle");
 				String sub = rs.getString("subTitle");
 				Timestamp deadline = rs.getTimestamp("deadline");
 
 				if (deadline != null && modMan != null) {
 					notif.add(new DeadlineNotification(recEm, senEm, timS,
 							mess, act, stat, isReadS, isReadR, deadline, modMan));
 				} else if (exRules != null) {
 					if (modMan != null) {
 						// TODO add version for ModMan or not ;)
 					}
 					// TODO add version for ExRule or not ;)
 				} else if (mod != null) {
 					if (sub != null) {
 						if (notif.size() != 0) {
 							boolean isIn = false;
 							for (int i = 0; i < notif.size(); i++) {
 								if (notif.get(i).getTimeStamp().equals(timS)) {
 									isIn = true;
 									break;
 								}
 							}
 							if (!isIn) {
 								Subject subject = DBSubject
 										.loadSubjectMaxVersionModNotif(sub, mod);
 								Subject subBefore = DBSubject.loadSubject(
 										subject.getVersion() - 1, sub, mod);
 								if (subBefore == null) {
 									subBefore = new Subject(0,
 											"Vorgängerversion nicht verfügbar.", "",
 											"Da das Fach neu angelegt wurde, ist keine Anzeige möglich.", "", 0, false);
 								}
 								notif.add(new ModificationNotification(recEm,
 										senEm, timS, mess, act, stat, isReadS, isReadR,
 										new Modification(subBefore, subject)));
 							}
 						} else {
 							Subject subject = DBSubject
 									.loadSubjectMaxVersionModNotif(sub, mod);
 							Subject subBefore = DBSubject.loadSubject(
 									subject.getVersion() - 1, sub, mod);
 							if (subBefore == null) {
 								subBefore = new Subject(0,
 										"Vorgängerversion nicht verfügbar.", "", "Da das Fach neu angelegt wurde, ist keine Anzeige möglich.",
 										"", 0, false);
 							}
 							notif.add(new ModificationNotification(recEm,
 									senEm, timS, mess, act, stat, isReadS, isReadR,
 									new Modification(subBefore, subject)));
 						}
 					}
 					// TODO add version for Module or not ;)
 				}
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
 	 * load all modNotifs where status is "queued"
 	 * 
 	 * @param currentUser
 	 * 
 	 * @return
 	 */
 	public static List<ModificationNotification> loadModificationNotificationRedakteur(
 			String currentUser) {
 		List<ModificationNotification> notif = new LinkedList<ModificationNotification>();
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM notification where status='queued' AND recipientEmail = '"
 					+ currentUser + "'";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 				String exRules = rs.getString("exRulesTitle");
 				String modMan = rs.getString("modManTitle");
 				String mod = rs.getString("modTitle");
 				String sub = rs.getString("subTitle");
 
 				if (exRules != null) {
 					if (modMan != null) {
 						// TODO add version for ModMan or not ;)
 					}
 					// TODO add version for ExRule or not ;)
 				} else if (mod != null) {
 					if (sub != null) {
 						Subject subject = DBSubject
 								.loadSubjectMaxVersionForRedakNotif(sub, mod);
 						Subject subBefore = DBSubject.loadSubject(
 								subject.getVersion() - 1, sub, mod);
 						if (subBefore == null) {
 							subBefore = new Subject(0,
 									"Vorgängerversion nicht verfügbar.", "", "Da das Fach neu angelegt wurde, ist keine Anzeige möglich.", "",
 									0, true);
 						}
 						if (subject != null) {
 							notif.add(new ModificationNotification(recEm,
 									senEm, timS, mess, act, stat, isReadS, isReadR,
 									new Modification(subBefore, subject)));
 						}
 					}
 					// TODO add version for Module or not ;)
 				}
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
 					+ "'"
 					+ "' AND "
 					+ "timeStamp = "
 					+ (Timestamp) timeStamp + ";";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				String recEm = rs.getString("recipientEmail");
 				String senEm = rs.getString("senderEmail");
 				Timestamp timS = rs.getTimestamp("timeStamp");
 				String mess = rs.getString("message");
 				String act = rs.getString("action");
 				String stat = rs.getString("status");
 				boolean isReadS = rs.getBoolean("isReadSender");
 				boolean isReadR = rs.getBoolean("isReadRecipient");
 
 				notif = new Notification(recEm, senEm, timS, mess, act, stat,
 						isReadS, isReadR);
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
 	 * DELETE all notifications from one user with specified email
 	 * 
 	 * @param email
 	 */
 	public static void deleteNotificationFromUser(String email) {
 		Connection con = null;
 		try {
 			con = openConnection();
 			Statement stmt = con.createStatement();
 			String update = "DELETE FROM notification WHERE recipientEmail = '"
 					+ email + "' OR " + "senderEmail = '" + email + "';";
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
 
 }
