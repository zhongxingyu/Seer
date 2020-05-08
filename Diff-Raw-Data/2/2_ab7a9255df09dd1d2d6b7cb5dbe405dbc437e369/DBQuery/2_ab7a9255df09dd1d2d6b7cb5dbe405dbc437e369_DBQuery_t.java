 package org.vikenpedia.fellesprosjekt.server;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import org.vikenpedia.fellesprosjekt.shared.models.FpCalendar;
 import org.vikenpedia.fellesprosjekt.shared.models.Meeting;
 import org.vikenpedia.fellesprosjekt.shared.models.MeetingParticipant;
 import org.vikenpedia.fellesprosjekt.shared.models.ReplyStatus;
 import org.vikenpedia.fellesprosjekt.shared.models.Reservation;
 import org.vikenpedia.fellesprosjekt.shared.models.Room;
 import org.vikenpedia.fellesprosjekt.shared.models.User;
 
 public class DBQuery {
     private DBC dbc;
     private Converter conv;
 
     public DBQuery() throws FileNotFoundException, IOException, ClassNotFoundException,
             SQLException {
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
     
     private synchronized ResultSet syncExecUpdateKeyed(PreparedStatement pstmt) throws SQLException {
         pstmt.executeUpdate();
         return pstmt.getGeneratedKeys();
     }
 
     public boolean createMeeting(Meeting meeting, int calendar_id, int chairman_id) {
         String place = meeting.getPlace();
         String descr = meeting.getDescription();
         Timestamp startTime = meeting.getStartTime();
         Timestamp endTime = meeting.getEndTime();
 
         String query = "INSERT INTO meeting(start_time, end_time, description, place, chairman_id)"
                 + " VALUES (?, ?, ?, ?, ?);";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.prepareKeyedStatement(query);
             pstmt.setTimestamp(1, startTime);
             pstmt.setTimestamp(2, endTime);
             pstmt.setString(3, descr);
             pstmt.setString(4, place);
             pstmt.setInt(5, chairman_id);
             ResultSet rs = syncExecUpdateKeyed(pstmt);
             int lastId = conv.convertGetLastMeetingId(rs);
             pstmt.close();
             createCalendarHasMeetingEntry(lastId, calendar_id);
             createUserHasMeetingEntry(calendar_id, lastId, ReplyStatus.YES);
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.createMeeting: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.createMeeting: " + e.getMessage());
             }
         }
         return false;
     }
 
     public boolean createReservationEntry(int meeting_id, int room_id) {
     	String query = "INSERT INTO reservation"
     			+ " SELECT ?, ?, m.start_time, m.end_time FROM meeting AS m"
     			+ " WHERE m.meeting_id = ?;";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             pstmt.setInt(2, room_id);
             pstmt.setInt(3, meeting_id);
             syncExecUpdate(pstmt);
             pstmt.close();
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.createReservationEntry: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.createReservationEntry: " + e.getMessage());
             }
         }
         return false;
     }
 
     public boolean createUserHasMeetingEntry(int calendar_id, int meeting_id, ReplyStatus replyStatus) {
         String query = "INSERT INTO user_has_meeting(user_id, meeting_id, reply_status)"
                 + " (SELECT user_id, ?, ? FROM calendar WHERE calendar_id = ?);";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             pstmt.setString(2, replyStatus.toString());
             pstmt.setInt(3, calendar_id);
             System.out.println(pstmt);
             syncExecUpdate(pstmt);
             pstmt.close();
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.createUserHasMeetingEntry: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.createUserHasMeetingEntry: " + e.getMessage());
             }
         }
         return false;
     }
 
     public boolean createCalendarHasMeetingEntry(int meeting_id, int calendar_id) {
         String query = "INSERT INTO calendar_has_meeting (meeting_id, calendar_id) VALUES (?, ?);";
         PreparedStatement pstmt = null;
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             pstmt.setInt(2, calendar_id);
             syncExecUpdate(pstmt);
             pstmt.close();
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.createCalendarHasMeetingEntry: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.createCalendarHasMeetingEntry: " + e.getMessage());
             }
         }
         return false;
     }
  
     public User login(String username, String password) {
         String query = "SELECT user_id, name, username FROM user "
                 + " WHERE username = ? and password = ?;";
         ResultSet rs = null;
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setString(1, username);
             pstmt.setString(2, password);
             rs = syncExec(pstmt);
             User user = conv.convertLogin(rs);
             pstmt.close();
             return user;
         } catch (SQLException e) {
             System.out.println("DBQuery.login: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.login: " + e.getMessage());
             }
         }
         return null;
     }
 
     /*
      * public Meeting[] getMeetingsInInterval(int calendar_id, Timestamp
      * startTime, Timestamp endTime) { String query =
      * "SELECT m.meeting_id, m.start_time, m.end_time, " +
      * "m.description, m.place, m.chairman_id " +
      * "FROM calendar_has_meeting as chm " +
      * "JOIN meeting as m ON (chm.meeting_id = m.meeting_id) " +
      * "WHERE chm.calendar_id = ? AND m.start_time >= ? AND m.end_time <= ?;";
      * PreparedStatement pstmt = null; ResultSet rs = null; Meeting[] meetings =
      * null;
      * 
      * try { pstmt = dbc.preparedStatement(query); pstmt.setInt(1, calendar_id);
      * pstmt.setTimestamp(2, startTime); pstmt.setTimestamp(3, endTime); rs =
      * syncExec(pstmt); meetings = conv.convertSelectMeetingsInInterval(rs);
      * pstmt.close(); return meetings; } catch (SQLException e) {
      * System.out.println("DBQuery.selectMeetingsInInterval: " +
      * e.getMessage()); } finally { try { pstmt.close(); } catch (SQLException
      * e) { System.out.println("DBQuery.selectMeetingsInInterval: " +
      * e.getMessage()); } } return null; }
      */
 
     public Meeting[] getMeetingsInWeek(int calendar_id, int weekNum, int userId) {
        String query = "SELECT DISTINCT m.meeting_id, m.start_time, m.end_time, "
                 + "m.description, m.place, m.chairman_id, r.name " + "FROM calendar_has_meeting as chm "
                 + "JOIN meeting as m ON (chm.meeting_id = m.meeting_id) "
                 + "JOIN user_has_meeting as uhm ON (uhm.meeting_id = m.meeting_id) "
                 + "LEFT JOIN reservation AS rs ON rs.meeting_id = m.meeting_id "
                 + "LEFT JOIN room AS r ON rs.room_id = r.room_id "
                 + "WHERE chm.calendar_id = ? AND WEEKOFYEAR(m.start_time) = ? AND uhm.reply_status ='yes';";
 
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Meeting[] meetings = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, calendar_id);
             pstmt.setInt(2, weekNum);
             rs = syncExec(pstmt);
             meetings = conv.convertSelectMeetingsInInterval(rs, userId);
             pstmt.close();
             return meetings;
         } catch (SQLException e) {
             System.out.println("DBQuery.getMeetingsInWeek: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getMeetingsInWeek: " + e.getMessage());
             }
         }
         return null;
     }
 
     public Meeting[] getUnrepliedMeetings(int userId) {
         String query = "SELECT m.meeting_id, m.start_time, m.end_time, "
                 + "m.description, m.place, m.chairman_id " + "FROM user_has_meeting as uhm "
                 + "JOIN meeting as m ON (uhm.meeting_id = m.meeting_id) "
                 + " WHERE uhm.user_id = ? AND uhm.reply_status = ?;";
 
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Meeting[] meetings = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, userId);
             pstmt.setString(2, "waiting"); // denne maa kanskje fikses slik at
                                            // man sjekker paa enum og ikke
                                            // string
             rs = syncExec(pstmt);
             meetings = conv.convertSelectMeetingsInInterval(rs);
             pstmt.close();
             return meetings;
         } catch (SQLException e) {
             System.out.println("DBQuery.selectMeetingsInInterval: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.selectMeetingsInInterval: " + e.getMessage());
             }
         }
         return null;
     }
 
     public Reservation[] getReservationsInInterval(int room_id, Timestamp startTime,
             Timestamp endTime) {
         String query = "SELECT meeting_id, room_id, start_time, end_time "
                 + "FROM reservation as r "
                 + "WHERE room_id = ? AND r.start_time >= ? AND r.end_time <= ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Reservation[] reservations = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, room_id);
             pstmt.setTimestamp(2, startTime);
             pstmt.setTimestamp(3, endTime);
             rs = syncExec(pstmt);
             reservations = conv.convertSelectReservationsInInterval(rs);
             pstmt.close();
             return reservations;
         } catch (SQLException e) {
             System.out.println("DBQuery.getReservationsInInterval: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getReservationsInInterval: " + e.getMessage());
             }
         }
         return null;
     }
 
     public FpCalendar[] getUserCalendars(int user_id) {
         String query = "SELECT calendar_id FROM calendar " + " WHERE user_id = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         FpCalendar[] calendars = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, user_id);
             rs = syncExec(pstmt);
             calendars = conv.convertSelectUserCalendars(rs);
             pstmt.close();
             return calendars;
         } catch (SQLException e) {
             System.out.println("DBQuery.getUserCalendars: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getUserCalendars: " + e.getMessage());
             }
         }
         return null;
     }
 
     public boolean deleteMeetingChairman(int meeting_id) {
         String query = "DELETE FROM meeting WHERE meeting_id = ?;";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             syncExecUpdate(pstmt);
             pstmt.close();
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.deleteMeetingMeetingChairman: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.deleteMeetingMeetingChairman: " + e.getMessage());
             }
         }
         return false;
     }
 
     public MeetingParticipant[] getMeetingParticipants(int meeting_id) {
         String query = " SELECT user_id, meeting_id, alarm_minutes_before, "
                 + "reply_status FROM user_has_meeting WHERE meeting_id = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         MeetingParticipant[] meetingParticipantArr = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             rs = syncExec(pstmt);
             meetingParticipantArr = conv.convertSelectUserHasMeeting(rs);
             pstmt.close();
             return meetingParticipantArr;
         } catch (SQLException e) {
             System.out.println("DBQuery.getMeetingParticipants: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getMeetingParticipants: " + e.getMessage());
             }
         }
         return null;
     }
 
     /*
      * public MeetingParticipant[] getMeetingParticipants(int meeting_id) {
      * String query = "SELECT user_id, meeting_id, alarm_minutes_before, " +
      * "reply_status FROM user_has_meeting WHERE meeting_id = ?;";
      * PreparedStatement pstmt = null; ResultSet rs = null; MeetingParticipant[]
      * meetingParticipantArr = null;
      * 
      * try { pstmt = dbc.preparedStatement(query); pstmt.setInt(1, meeting_id);
      * rs = syncExec(pstmt); meetingParticipantArr =
      * conv.convertSelectUserHasMeeting(rs); pstmt.close(); return
      * meetingParticipantArr; } catch (SQLException e) {
      * System.out.println("DBQuery.getMeetingParticipants: " + e.getMessage());
      * } finally { try { pstmt.close(); } catch (SQLException e) {
      * System.out.println("DBQuery.getMeetingParticipants: " + e.getMessage());
      * } } return null; }
      */
 
     /*
      * public boolean deleteMeeting(int user_id, int calendar_id, int
      * meeting_id) { return deleteUserHasMeetingEntry(user_id, meeting_id) &&
      * deleteCalendarHasMeetingEntry(calendar_id, meeting_id); }
      * 
      * private boolean deleteUserHasMeetingEntry(int user_id, int meeting_id) {
      * String query =
      * "DELETE FROM user_has_meeting WHERE user_id = ? AND meeting_id = ?;";
      * PreparedStatement pstmt = null;
      * 
      * try { pstmt = dbc.preparedStatement(query); pstmt.setInt(1, user_id);
      * pstmt.setInt(2, meeting_id); syncExecUpdate(pstmt); pstmt.close();
      * 
      * return true; } catch (SQLException e) {
      * System.out.println("DBQuery.deleteUserHasMeetingEntry: " +
      * e.getMessage()); } finally { try { pstmt.close(); } catch (SQLException
      * e) { System.out.println("DBQuery.deleteUserHasMeetingEntry: " +
      * e.getMessage()); } } return false; }
      */
     public Room[] getFreeRooms(int meeting_id) {
         String query = "SELECT ro.room_ID, ro.capacity, ro.name"
                 + " FROM room AS ro"
                 + " WHERE ro.room_id NOT IN"
                 + " (SELECT res.room_id "
                 + " FROM reservation AS res, room AS ro, meeting AS m"
                 + " WHERE m.start_time > res.end_time AND m.end_time < res.start_time AND m.meeting_id = ?);";
         ResultSet rs = null;
         Room[] freeRooms = null;
         PreparedStatement pstmt = null;
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             rs = syncExec(pstmt);
             freeRooms = conv.convertGetFreeRooms(rs);
             pstmt.close();
             return freeRooms;
         } catch (SQLException e) {
             System.out.println("DBQuery.getFreeRooms: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getFreeRooms: " + e.getMessage());
             }
         }
         return null;
     }
 
     /*
      * private boolean deleteCalendarHasMeetingEntry(int calendar_id, int
      * meeting_id) { String query = "DELETE FROM calendar_has_meeting " +
      * "WHERE calendar = ? AND meeting_id = ?;"; PreparedStatement pstmt = null;
      * 
      * try { pstmt = dbc.preparedStatement(query); pstmt.setInt(1, calendar_id);
      * pstmt.setInt(2, meeting_id); syncExecUpdate(pstmt); pstmt.close();
      * 
      * return true; } catch (SQLException e) {
      * System.out.println("DBQuery.deleteCalendarHasMeetingEntry: " +
      * e.getMessage()); } finally { try { pstmt.close(); } catch (SQLException
      * e) { System.out.println("DBQuery.deleteCalendarHasMeetingEntry: " +
      * e.getMessage()); } } return false; }
      */
     public boolean saveMeeting(Meeting meeting) {
         String query = "UPDATE meeting SET start_time = ?, end_time = ?, "
                 + "description = ?, place = ? WHERE meeting_id = ?;";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setTimestamp(1, meeting.getStartTime());
             pstmt.setTimestamp(2, meeting.getEndTime());
             pstmt.setString(3, meeting.getDescription());
             pstmt.setString(4, meeting.getPlace());
             pstmt.setInt(5, meeting.getId());
             syncExecUpdate(pstmt);
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.saveMeeting: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.saveMeeting: " + e.getMessage());
             }
         }
         return false;
     }
 
     public boolean saveMeetingParticipant(MeetingParticipant meetingPar) {
         String query = "UPDATE user_has_meeting SET alarm_minutes_before = ?, reply_status = ?"
                 + " WHERE user_id = ? AND meeting_id = ?;";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meetingPar.getAlarmBeforeMinutes());
             pstmt.setString(2, meetingPar.getReplyStatus().toString());
             pstmt.setInt(3, meetingPar.getUserId());
             pstmt.setInt(4, meetingPar.getMeetingId());
             syncExecUpdate(pstmt);
 
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.saveMeetingParticipant: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.saveMeetingParticipant: " + e.getMessage());
             }
         }
         return false;
     }
 
     public boolean executeDBScript(String filePath) {
         try {
             FileReader fr = new FileReader(filePath);
             BufferedReader in = new BufferedReader(fr);
             StringBuffer sb = new StringBuffer();
             String str = null;
 
             while ((str = in.readLine()) != null) {
                 sb.append(str + " "); // Mellomrom p� slutten s�rger for at to
                                       // linjer som ikke avsluttes med ";"
                                       // behandles separat
             }
             in.close();
             String[] inst = sb.toString().split(";");
 
             for (int i = 0; i < inst.length; i++) {
                 if (!inst[i].trim().equals("")) {
                     PreparedStatement pstmt = dbc.preparedStatement(inst[i]);
                     syncExecUpdate(pstmt);
                 }
             }
 
             return true;
         } catch (IOException e) {
             System.out.println("DBQuery.executeDBScript: " + e.getMessage());
         } catch (SQLException e) {
             System.out.println("DBQuery.executeDBScript: " + e.getMessage());
         }
         return false;
     }
 
     public MeetingParticipant getMeetingParticipant(int meeting_id, int user_id) {
         String query = "SELECT user_id, meeting_id, alarm_minutes_before, reply_status"
                 + " FROM user_has_meeting" + " WHERE meeting_id = ? AND user_id = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             pstmt.setInt(2, user_id);
             rs = syncExec(pstmt);
             MeetingParticipant meetPar = conv.convertSelectUserHasMeeting(rs)[0];
             pstmt.close();
             return meetPar;
         } catch (SQLException e) {
             System.out.println("DBQuery.getMeetingParticipant: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getMeetingParticipant: " + e.getMessage());
             }
         }
         return null;
     }
 
     public int getChairmanId(int meeting_id) {
         String query = "SELECT chairman_id" + " FROM meeting" + " WHERE meeting_id = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         int chairmanId = -1;
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meeting_id);
             rs = syncExec(pstmt);
             chairmanId = conv.convertGetChairmanId(rs);
             pstmt.close();
         } catch (SQLException e) {
             System.out.println("DBQuery.getChairmanId: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getChairmanId: " + e.getMessage());
             }
         }
         return chairmanId;
     }
 
     public boolean userOwnsCalendar(int userId, int calendarId) {
         String query = "SELECT user_Id" + " FROM calendar AS cal"
                 + " WHERE cal.calendar_id = ? AND cal.user_ID = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Boolean resultat = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, calendarId);
             pstmt.setInt(2, userId);
             rs = syncExec(pstmt);
             resultat = conv.isEmptySet(rs);
             pstmt.close();
             return resultat;
         } catch (SQLException e) {
             System.out.println("DBQuery.userOwnsCalendar: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.userOwnsCalendar: " + e.getMessage());
             }
         }
         return false;
     }
 
     public MeetingParticipant[] getInvited(int meetingId) {
         String query = "SELECT uhm.user_id, uhm.meeting_id, uhm.reply_status, u.username"
                 + " FROM user_has_meeting AS uhm, user AS u"
                 + " WHERE uhm.meeting_id = ? AND uhm.user_id = u.user_id;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         MeetingParticipant[] participants = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meetingId);
             rs = syncExec(pstmt);
             participants = conv.convertGetInvited(rs);
             pstmt.close();
             return participants;
         } catch (SQLException e) {
             System.out.println("DBQuery.getInvited: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getInvited: " + e.getMessage());
             }
         }
         return null;
     }
 
     public FpCalendar[] getNotInvited(int meetingId) {
         String query = "SELECT calendar.calendar_id, calendar.name, user.name" + " FROM calendar"
         		+ " JOIN user ON user.user_id = calendar.user_id"
                 + " WHERE user.user_id NOT IN" + "(SELECT user_id"
                 + " FROM user_has_meeting" + " WHERE meeting_id = ?);";
 
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         FpCalendar[] nonParticipants = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setInt(1, meetingId);
             rs = syncExec(pstmt);
             nonParticipants = conv.convertGetNotInvited(rs);
             pstmt.close();
             return nonParticipants;
         } catch (SQLException e) {
             System.out.println("DBQuery.getNotInvited: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getNotInvited: " + e.getMessage());
             }
         }
         return null;
     }
 
     public int getUserByName(String username) {
         String query = "SELECT u.user_id" + " FROM user AS u" + " WHERE u.username = ?;";
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         int userId;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setString(1, username);
             rs = syncExec(pstmt);
             userId = conv.convertGetUserByName(rs);
             pstmt.close();
             return userId;
         } catch (SQLException e) {
             System.out.println("DBQuery.getUserByName: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.getUserByName: " + e.getMessage());
             }
         }
         return -1;
     }
     
     public boolean setReply(int meetingId, int userId, ReplyStatus replyStatus) {
         String query = "UPDATE user_has_meeting"
         		+ " SET reply_status = ?"
                 + " WHERE user_id = ? AND meeting_id = ?;";
         PreparedStatement pstmt = null;
 
         try {
             pstmt = dbc.preparedStatement(query);
             pstmt.setString(1, replyStatus.toString());
             pstmt.setInt(2, userId);
             pstmt.setInt(3, meetingId);
             syncExecUpdate(pstmt);
             pstmt.close();
             return true;
         } catch (SQLException e) {
             System.out.println("DBQuery.setReply: " + e.getMessage());
         } finally {
             try {
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("DBQuery.setReply: " + e.getMessage());
             }
         }
         return false;
     }
 
 }
