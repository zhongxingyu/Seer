 package server.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import server.DBConnection;
 import server.ServerMain;
 import client.model.InvitationModel;
 import client.model.MeetingModel;
 import client.model.NotificationType;
 import client.model.TransferableModel;
 import client.model.UserModel;
 
 /**
  * Server Side MeetingModel with methods for reading and writing to server
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class ServerMeetingModel extends MeetingModel implements IDBStorableModel {
 
 	protected String ownerId; 
 		
 	/**
 	 * Construct Meeting model from ResultSet
 	 * 
 	 * @param rs
 	 */
 	public ServerMeetingModel(ResultSet rs, DBConnection db) throws SQLException {		
 		super();
 		
 		id = rs.getInt("id");
 		setName(rs.getString("title"));
 		setDescription(rs.getString("description"));
 		ownerId = rs.getString("owner");
 		setActive(rs.getBoolean("active"));
 		setLocation(rs.getString("location"));
 		
 		// Pull in reserved room
 		setRoom(ServerMeetingRoomModel.findReservedRoom(id, db));
 		
 		Calendar fromTime = Calendar.getInstance();
		fromTime.setTime(rs.getTimestamp("start_date"));
 		setTimeFrom(fromTime);
 		Calendar toTime = Calendar.getInstance();
		toTime.setTime(rs.getTimestamp("start_date"));
 		setTimeTo(toTime);
 		
 		// Set invitations to null forcing a re-fetch on next getInvitations()
 		invitations = null;
 	}
 	
 
 	/**
 	 * Construct MeetingModel from stream
 	 * 
 	 * @param reader
 	 * @param modelBuff
 	 * @throws IOException
 	 */
 	public ServerMeetingModel(BufferedReader reader) throws IOException {
 		super(reader);
 	}
 
 	/**
 	 * Get user model
 	 * 
 	 * Server side this may not yet be loaded, do JIT loading
 	 */
 	@Override
 	public UserModel getOwner() {
 		if(owner == null) {
 			owner = ServerUserModel.findByUsername(ownerId, ServerMain.dbConnection);
 		}
 		return owner;
 	}
 	
 	/**
 	 * Get meeting attenddes
 	 * 
 	 * Server side this may not yet be loaded, do JIT loading
 	 */
 	@Override
 	public ArrayList<InvitationModel> getInvitations() {
 		if(invitations == null) {
 			invitations = ServerInvitationModel.findByMeeting(this, ServerMain.dbConnection);
 		}
 		return invitations;
 	}
 	
 	/**
 	 * Store model to database
 	 */
 	@Override
 	public void store(DBConnection db) {
 		try {
 			if(id == -1) {
 				// Insert
 				Statement st = ServerMain.dbConnection.createStatement();
 
 				st.executeUpdate(String.format(
 						"INSERT INTO appointment(title, start_date, end_date, description, owner, location)"
 						+" VALUES('%s', '%s', '%s', '%s', '%s', '%s')",						
 						getName(), DBConnection.getFormattedDate(getTimeFrom()), 
 						DBConnection.getFormattedDate(getTimeTo()),
 						getDescription(), getOwner().getUsername(), getLocation()),
 						Statement.RETURN_GENERATED_KEYS);
 				
 				ResultSet rs = st.getGeneratedKeys();
 				rs.next();
 				id = rs.getInt(1);
 				rs.close();
 				
 				// Register a room reservations
 				if(getRoom() != null) {
 					st.executeUpdate(String.format(
 						"INSERT INTO meeting_room_booking (meeting_room_number, appointment_id)" +
 						"VALUES(%s, %d);", getRoom().getRoomNumber(), getId()
 					));
 				}				
 				st.close();				
 			} else {				
 				// Use old meeting as a reference
 				ServerMeetingModel old = ServerMeetingModel.findById(getId(), db);
 				
 				// Handle invitations, first remove any users that we've removed from a meeting
 				for(InvitationModel i : old.getInvitations()) {
 					if(getInvitation(i.getUser()) == null) {
 						((ServerInvitationModel)i).delete(db);						
 					}
 				}
 				
 				// Then add all new invitations
 				for(InvitationModel i : invitations) {
 					if(old.getInvitation(i.getUser()) == null) {
 						((ServerInvitationModel)i).store(db);
 					}
 				}
 				
 				// Update the actual meeting model
 				db.preformUpdate(String.format(
 						"UPDATE appointment SET title='%s', start_date='%s', end_date='%s', " +
 						"description='%s', owner='%s', location='%s' WHERE id=%d",			
 						getName(), DBConnection.getFormattedDate(getTimeFrom()), 
 						DBConnection.getFormattedDate(getTimeTo()),	getDescription(), 
 						getOwner().getUsername(), getLocation(), getId()));
 
 				// Reset invitations if needed
 				boolean resetInv = !old.getTimeFrom().equals(getTimeFrom()) 
 						|| !old.getTimeTo().equals(getTimeTo()) 
 						|| !old.getLocation().equals(getLocation())
 						|| ((old.getRoom() != null) && !old.getRoom().equals(getRoom()));
 				
 				if(resetInv) {
 					resetInvitations();
 				}
 				
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Reset all invitaions sent for this meeting
 	 * 
 	 */
 	private void resetInvitations() {
 		// Set all invitaions to INVITED, send a notification to every user
 	}
 	
 	/**
 	 * Delete meeting
 	 */
 	public void delete(DBConnection db) {
 		for(InvitationModel i : getInvitations()) {
 			ServerNotificationModel n = new ServerNotificationModel(
 					NotificationType.A_CANCELED, i.getUser(), this, getOwner());
 			n.store(db);
 		}
 		try {
 			db.preformUpdate(String.format("UPDATE appointment SET active=0 WHERE id=%d", getId()));
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	/**
 	 * Search database for meetings concerning all the given users, within the
 	 * given time period
 	 * 
 	 * @param users
 	 * @param startDate
 	 * @param endDate
 	 * @param db
 	 * @return
 	 */
 	public static ArrayList<ServerMeetingModel> searchByUsernamesAndPeriod(
 			String[] users, Calendar startDate, Calendar endDate, DBConnection db) {
 		
 		if(users.length == 0) {
 			throw new IllegalArgumentException("Need atleast one user");
 		}		
 		ArrayList<ServerMeetingModel> ret = new ArrayList<ServerMeetingModel>();
 		try {
 			StringBuilder userIn = new StringBuilder();			
 			for(String u : users) {
 				userIn.append(",'"+u+"'");
 			}
 			String userList = userIn.toString().substring(1);
 			
 			ResultSet rs = db.preformQuery(
 					"SELECT DISTINCT a.* FROM appointment as a " +
 					"LEFT JOIN user_appointment as ap ON a.id = ap.appointment_id " +
 					"WHERE active=1 " +
 					"AND start_date >= '"+DBConnection.getFormattedDate(startDate)+"' " +
 					"AND start_date < '"+DBConnection.getFormattedDate(endDate)+"' " +
 					"AND (a.owner IN (" + userList + ") OR ap.username IN ("+userList+"))");
 			while (rs.next()) {
 				ret.add(new ServerMeetingModel(rs, db));
 			}
 			rs.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Find a single model based on a id number
 	 * 
 	 * @param id
 	 * @param db
 	 * @return
 	 */
 	public static ServerMeetingModel findById(int id, DBConnection db) {
 		try {
 			ResultSet rs = db.preformQuery(
 					"SELECT * FROM appointment AS a " +
 					"LEFT JOIN user_appointment AS ap ON a.id = ap.appointment_id " +
 					"WHERE a.id = " + id + ";");
 			
 			if(rs.next()) {
 				return new ServerMeetingModel(rs, db);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
