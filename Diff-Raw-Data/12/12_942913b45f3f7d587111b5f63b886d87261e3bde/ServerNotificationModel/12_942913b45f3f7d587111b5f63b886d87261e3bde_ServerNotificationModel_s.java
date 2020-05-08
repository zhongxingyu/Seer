 package server.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import server.DBConnection;
 import server.ServerMain;
 import client.model.MeetingModel;
 import client.model.NotificationModel;
 import client.model.NotificationType;
 import client.model.TransferableModel;
 import client.model.UserModel;
 
 public class ServerNotificationModel extends NotificationModel 
 	implements IDBStorableModel {
 
 	/**
 	 * Construct a new Notification
 	 * 
 	 * @param type
 	 * @param given_to
 	 * @param regards_meeting
 	 */
 	public ServerNotificationModel(NotificationType type, UserModel given_to, 
 			MeetingModel regards_meeting) {
 		super(type, given_to, regards_meeting);
 	}
 	
 	/**
 	 * Construct a new Notification
 	 * 
 	 * @param type
 	 * @param given_to
 	 * @param regards_meeting
 	 * @param regards_user
 	 */
 	public ServerNotificationModel(NotificationType type, UserModel given_to,
 			MeetingModel regards_meeting, UserModel regards_user) {
 		super(type, given_to, regards_meeting, regards_user);
 	}
 	
 	/**
 	 * Construct NotificationModel from reader
 	 * 
 	 * @param reader
 	 * @param modelBuff
 	 * @throws IOException
 	 */
 	public ServerNotificationModel(BufferedReader reader) throws IOException {
 		super(reader);
 	}
 	
 	/**
 	 * Construct notification from sql result set
 	 * 
 	 * @param rs
 	 * @throws SQLException
 	 */
 	public ServerNotificationModel(ResultSet rs, DBConnection db) throws SQLException {
 		id = rs.getInt("id");
 		time = Calendar.getInstance();
		time.setTime(rs.getDate("time"));
 		type = NotificationType.valueOf(rs.getString("type"));
 		read = rs.getBoolean("read");
 		given_to = ServerUserModel.findByUsername(rs.getString("given_to"), db);
 		if(rs.getInt("regards_appointment") != 0) {
 			regards_meeting = ServerMeetingModel.findById(rs.getInt("regards_appointment"), db);
 		}
 		if(rs.getString("regards_user") != null) {
 			regards_user = ServerUserModel.findByUsername(rs.getString("regards_user"), db);
 		}
 	}
 	
 	/**
 	 * Store notification
 	 * 
 	 * This will also broadcast the notification to the recipient if he's online
 	 */
 	@Override
 	public void store(DBConnection db) {
 		
 		try {
 			// Insert notification
 			if(getId() == -1) {
 				Statement st = db.createStatement();
 				st.executeUpdate(String.format("INSERT INTO notification(`type`, `time`, " +
 						"`given_to`, `read`, `regards_appointment`, `regards_user`) " +
 						"VALUES ('%s', '%s', '%s', %s, %s, %s)",
 						getType(), DBConnection.getFormattedDate(getTime()), 
 						getGivenTo().getUsername(),	(isRead() ? "TRUE" : "FALSE"), 
 						(getRegardsMeeting() != null ? getRegardsMeeting().getId() : "NULL"),
 						(getRegardsUser() != null ? "'"+getRegardsUser().getUsername()+"'" : "NULL")
 					), Statement.RETURN_GENERATED_KEYS);
 				
 				ResultSet rs = st.getGeneratedKeys();
 				rs.next();
 				id = rs.getInt(1);
 				rs.close();
 				
 				// Send the notification to the user right away
 				ServerMain.ccl.broadcastModel(this, getGivenTo().getUsername());
 			} else {
 				// Update notification, only read may be changed
 				db.preformUpdate(String.format(
 					"UPDATE notification SET read = %d WHERE id = %d",
 					isRead(), getId()
 				));
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
