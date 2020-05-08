 package org.vikenpedia.fellesprosjekt.server;
 
import java.io.FileNotFoundException;
import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import org.vikenpedia.fellesprosjekt.shared.models.Calendar;
 import org.vikenpedia.fellesprosjekt.shared.models.Meeting;
 import org.vikenpedia.fellesprosjekt.shared.models.MeetingParticipant;
 import org.vikenpedia.fellesprosjekt.shared.models.ReplyStatus;
 import org.vikenpedia.fellesprosjekt.shared.models.Reservation;
 
 public class DBQuery {
 	private DBC dbc;
 	private Converter conv;
 
	public DBQuery() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		dbc = new DBC("src/org/vikenpedia/fellesprosjekt/server/properties.properties");
 		conv = new Converter();
 		dbc.initialize();
 	}
 
 	public void close() throws SQLException {
 		dbc.close();
 	}
 
 	private synchronized ResultSet syncExec(PreparedStatement pstmt) throws SQLException {
 		return pstmt.executeQuery();
 	}
 
 	private synchronized void syncExecUpdate(PreparedStatement pstmt) throws SQLException {
 		pstmt.executeUpdate();
 	}
 
 	public boolean login(String username, String password) throws SQLException {
 		String query = "SELECT user_id, name" + 
 				"FROM user" + 
 				"WHERE username = ? and password = ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setString(1, username);
 		pstmt.setString(2, password);
 
 		ResultSet rs = syncExec(pstmt);
 		boolean loginSuccess = conv.convertLogin(rs);
 		pstmt.close();
 
 		return loginSuccess;
 	}
 	
 	public Meeting[] selectMeetingsInInterval(int calendar_id, Timestamp startTime, Timestamp endTime) throws SQLException {
 		String query = "SELECT meeting_id, start_time, end_time, description, place, chairman_id" +
 				"FROM calendar_has_meeting as chm" +
 				"JOIN meeting as m ON (chm.meeting_id = m.meeting_id)" +
 				"WHERE chm.calander_id = ? AND m.start_time >= ? AND m.end_time <= ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, calendar_id);
 		pstmt.setTimestamp(2, startTime);
 		pstmt.setTimestamp(3, endTime);
 
 		ResultSet rs = syncExec(pstmt);
 		Meeting[] meetings = conv.convertSelectMeetingsInInterval(rs);
 		pstmt.close();
 
 		return meetings;
 	}
 
 	public Reservation[] selectReservationsInInterval(int room_id, Timestamp startTime, Timestamp endTime) throws SQLException {
 		String query = "SELECT meeting_id, room_id, start_time, end_time" +
 				"FROM reservation as r" +
 				"WHERE room_id = ? AND r.start_time >= ? AND r.end_time <= ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);  
 		pstmt.setInt(1, room_id);
 		pstmt.setTimestamp(2, startTime);
 		pstmt.setTimestamp(3, endTime);
 
 		ResultSet rs = syncExec(pstmt);
 		Reservation[] reservations = conv.convertSelectReservationsInInterval(rs);
 		pstmt.close();
 
 		return reservations;
 	}
 
 	public Calendar[] selectUserCalendars(int user_id) throws SQLException {
 		String query = "SELECT calendar_id" + 
 				"FROM user_has_calendar" +
 				"WHERE user_has_calendar.user_id = ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, user_id);
 
 		ResultSet rs = syncExec(pstmt);
 		Calendar[] calendars = conv.convertSelectUserCalendars(rs);
 		pstmt.close();
 
 		return calendars;
 	}
 	
 	public MeetingParticipant[] selectUserHasMeeting(int meeting_id) throws SQLException {
 		String query = "SELECT user_id, meeting_id, alarm_minutes_before, reply_status" + 
 				"FROM user_has_meeting" + "WHERE meeting_id = ?";
 		
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, meeting_id);
 
 		ResultSet rs = syncExec(pstmt);
 		MeetingParticipant[] meetingParticipantArr = conv.convertSelectUserHasMeeting(rs);
 		pstmt.close();
 
 		return meetingParticipantArr;
 	}
 
 	public void createMeeting(int chairman_id, int calendar_id, int meeting_id, Timestamp startTime, Timestamp endTime,
 			String description, String place, int alarmMinutesBefore) throws SQLException {
 		String query = "INSERT INTO meeting" +
 				"VALUES (?, ?, ?, ?, ?, ?)";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, meeting_id);
 		pstmt.setTimestamp(2, startTime);
 		pstmt.setTimestamp(3, endTime);
 		pstmt.setString(4, description);
 		pstmt.setString(5, place);
 		pstmt.setInt(6, chairman_id);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 
 		createCalendarHasMeetingEntry(meeting_id, calendar_id);
 		createUserHasMeetingEntry(chairman_id, meeting_id, alarmMinutesBefore, ReplyStatus.YES); // Chairman should automatically accept
 	}
 
 	private void createCalendarHasMeetingEntry(int meeting_id, int calendar_id) throws SQLException {
 		String query = "INSERT INTO calendar_has_meeting" +
 				"VALUES (?, ?)";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, meeting_id);
 		pstmt.setInt(2, calendar_id);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 
 	public void createUserHasMeetingEntry(int user_id, int meeting_id, int alarmMinutesBefore, ReplyStatus replyStatus) throws SQLException {
 		String query = "INSERT INTO user_has_meeting" +
 				"VALUES (?, ?, ?, ?)";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, user_id);
 		pstmt.setInt(2, meeting_id);
 		pstmt.setInt(3, alarmMinutesBefore);
 		pstmt.setString(4, replyStatus.toString());
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 
 	public void deleteMeeting(int user_id, int calendar_id, int meeting_id) throws SQLException {
 		deleteUserHasMeetingEntry(user_id, meeting_id);
 		deleteCalendarHasMeetingEntry(calendar_id, meeting_id);
 	}
 
 	private void deleteUserHasMeetingEntry(int user_id, int meeting_id) throws SQLException {
 		String query = "DELETE FROM user_has_meeting" + 
 				"WHERE user_id = ? AND meeting_id = ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, user_id);
 		pstmt.setInt(2, meeting_id);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 
 	private void deleteCalendarHasMeetingEntry(int calendar_id, int meeting_id) throws SQLException {
 		String query = "DELETE FROM calendar_has_meeting" +
 				"WHERE calendar = ? AND meeting_id = ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, calendar_id);
 		pstmt.setInt(2, meeting_id);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 
 
 	public void createReservationEntry(int meeting_id, int room_id, Timestamp startTime, Timestamp endTime) throws SQLException {
 		String query = "INSERT INTO reservation" +
 				"VALUES (?, ?, ?, ?)";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setInt(1, meeting_id);
 		pstmt.setInt(2, room_id);
 		pstmt.setTimestamp(3, startTime);
 		pstmt.setTimestamp(4, endTime);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 
 
 	public void updateReplyStatus(int user_id, int meeting_id, ReplyStatus replyStatus) throws SQLException {
 		String query = "UPDATE user_has_meeting" + 
 				"SET reply_status = ?" +
 				"WHERE user_id = ? AND meeting_id = ?";
 
 		PreparedStatement pstmt = dbc.preparedStatement(query);
 		pstmt.setString(1, replyStatus.toString());
 		pstmt.setInt(2, user_id);
 		pstmt.setInt(3, meeting_id);
 		syncExecUpdate(pstmt);
 		pstmt.close();
 	}
 }
