 package org.vikenpedia.fellesprosjekt.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
import org.vikenpedia.fellesprosjekt.shared.models.Calendar;
import org.vikenpedia.fellesprosjekt.shared.models.Meeting;
import org.vikenpedia.fellesprosjekt.shared.models.MeetingParticipant;
import org.vikenpedia.fellesprosjekt.shared.models.ReplyStatus;
import org.vikenpedia.fellesprosjekt.shared.models.Reservation;
 
 public class Converter {
 	
 	public boolean convertLogin(ResultSet rs) throws SQLException {
 		return (getResultSetSize(rs) > 0);
 	}
 	
 	public Calendar[] convertSelectUserCalendars(ResultSet rs) throws SQLException {
 		int rsSize = getResultSetSize(rs);
 		Calendar[] arr = new Calendar[rsSize];
 		while (rs.next()) {
 			int calId = rs.getInt(1);
 			Calendar tempCal = new Calendar(calId);
 			arr[rs.getRow()-1] = tempCal;
 		}
 		return arr;
 	}
 	
 	public Meeting[] convertSelectMeetingsInInterval(ResultSet rs) throws SQLException {
 		int rsSize = getResultSetSize(rs);
 		Meeting[] arr = new Meeting[rsSize];
 		while(rs.next()) {
 			int meetingId = rs.getInt(1);
 			Timestamp startTime = rs.getTimestamp(2);
 			Timestamp endTime = rs.getTimestamp(3);
 			String description = rs.getString(4);
 			String place = rs.getString(5);
 			int chairmanId = rs.getInt(6);
 			Meeting tempMeet = new Meeting(meetingId, startTime, endTime, description, place, chairmanId);
 			arr[rs.getRow()-1] = tempMeet;
 		}
 		return arr;
 	}
 	
 	public Reservation[] convertSelectReservationsInInterval(ResultSet rs) throws SQLException {
 		int rsSize = getResultSetSize(rs);
 		Reservation[] arr = new Reservation[rsSize];
 		while(rs.next()) {
 			int meetingId = rs.getInt(1);
 			int roomId = rs.getInt(2);
 			Timestamp startTime = rs.getTimestamp(3);
 			Timestamp endTime = rs.getTimestamp(4);
 			Reservation tempRes = new Reservation(meetingId, roomId, startTime,endTime);
 			arr[rs.getRow()-1] = tempRes;
 		}
 		return arr;
 	}
 	
 	public MeetingParticipant[] covertSelectUserHasMeeting(ResultSet rs) throws SQLException {
 		int rsSize = getResultSetSize(rs);
 		MeetingParticipant[] arr = new MeetingParticipant[rsSize];
 		while(rs.next()) {
 			int meetingId = rs.getInt(1);
 			int userId = rs.getInt(2);
 			int alarmBeforeMinutes = rs.getInt(3);
 			ReplyStatus replyStatus = getReplyStatus(rs.getString(4));
 			MeetingParticipant tempMeetPart = new MeetingParticipant(meetingId, userId, alarmBeforeMinutes, replyStatus);
 			arr[rs.getRow()-1] = tempMeetPart;
 		}
 		return arr;
 	}
 	
 	
 	
 	private int getResultSetSize(ResultSet rs) throws SQLException {
 		rs.last();
 		int size = rs.getRow();
 		rs.beforeFirst();
 		return size;
 	}
 	
 	private ReplyStatus getReplyStatus(String status) {
 		if (status.equals("WAITING"))
 			return ReplyStatus.WAITING;
 		else if (status.equals("NO"))
 			return ReplyStatus.NO;
 		else
 			return ReplyStatus.YES;
 		//vet at denne koden kanskje ikke er optimal, bare aa endre hvis dere har bedre implementasjoner
 	}
 }
