 package sql;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import helper.MyBoolean;
 import helper.Settings;
 import db.DBAccess;
 
 /**
  * For the parameters, please use DB column names
  * Example: setNickName(String bu_id, String bu_nick) 
  *          bu_id and bu_nick are both DB column names
  * <p>
  * SQL Method prefix Legend:<p>
  * 1. (get): simple query<p>
  * 2. (is): query that use SELECT 1 to check existence<p>
  * 3. (default): UPDATE statement that set targeted data back to default values<p>
  * 4. (set): normal UPDATE statement, single column<p>
  * 5. (setMul): UPDATE statement, multi column<p>
  * 6. (update): UPDATE multiple tables using MySQL Stored Procedure (SP) or complex SQL statements
  *    if the method needs to be changed, edit would like be done in SQL script: bbb_db_init.sql<p>
  * 7. (create): INSERT INTO<p>
  * 8. (remove): DELETE<p>
  * @author Kelan (Bo) Li
  *
  */
 public class Meeting extends Sql {
 
     public Meeting(DBAccess source) {
         super(source);
     }
 
     /**
      * Get data on a particular Meeting<p>
      * Fields:<p>
      * (0)ms_id (1)m_id (2)m_inidatetime (3)m_duration (4)m_iscancel (5)m_description (6)m_modpass (7)m_userpass
      * (8)m_setting(meeting)
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingInfo(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT meeting.* "
                 + "FROM meeting "
                 + "WHERE meeting.ms_id = '" + ms_id + "' "
                 + "AND meeting.m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Get data on meetings in a particular Meeting Schedule<p>
      * Fields:<p>
      * (0)ms_id (1)m_id (2)m_inidatetime (3)m_duration (4)m_iscancel (5)m_description (6)m_modpass (7)m_userpass
      * (8)m_setting(meeting) 
      * @param result
      * @return
      */
     public boolean getMeetingInfo(ArrayList<ArrayList<String>> result, String ms_id) {
         _sql = "SELECT meeting.* "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)ms_title (2)ms_inidatetime (3)ms_spec (4)ms_duration (5)bu_id
      * @param result
      * @return
      */
     public boolean getMeetingScheduleInfo(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT * "
                 + "FROM meeting_schedule";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)ms_title (2)ms_inidatetime (3)ms_spec (4)ms_duration (5)bu_id 
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingScheduleInfo(ArrayList<ArrayList<String>> result, String ms_id) {
         _sql = "SELECT * "
                 + "FROM meeting_schedule "
                 + "WHERE ms_id = '" + ms_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)ms_title (2)ms_inidatetime (3)ms_spec (4)ms_duration (5)bu_id 
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingCreators(ArrayList<ArrayList<String>> result, String ms_id) {
         _sql = "SELECT bu_id "
                 + "FROM meeting_schedule "
                 + "WHERE ms_id = '" + ms_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields:<p>
      * (0)ms_id (1)m_id (2)m_inidatetime (3)m_duration (4)m_iscancel (5)m_description (6)m_modpass (7)m_userpass
      * (8)m_setting(meeting) (9)ms_title
      * @param result
      * @param bu_id
      * @param created
      * @param attended
      * @return
      */
     public boolean getMeetingsForUser(ArrayList<ArrayList<String>> result, String bu_id, boolean creator, boolean attendee) {
         String _creator = "(SELECT meeting.*, meeting_schedule.ms_title " 
                 + "FROM meeting "
                 + "INNER JOIN meeting_schedule ON meeting.ms_id = meeting_schedule.ms_id "
                 + "WHERE meeting_schedule.bu_id = '" + bu_id +"') ";
                 
         String _attendee = "(SELECT meeting.*, meeting_schedule.ms_title "
                 + "FROM meeting "
                 + "INNER JOIN meeting_schedule ON meeting.ms_id = meeting_schedule.ms_id "
                 + "INNER JOIN meeting_attendee ON meeting_schedule.ms_id = meeting_attendee.ms_id "
                 + "WHERE meeting_attendee.bu_id = '" + bu_id + "') "
                 + "UNION DISTINCT "
                 + "(SELECT meeting.*, meeting_schedule.ms_title "
                 + "FROM meeting "
                 + "INNER JOIN meeting_guest ON meeting.m_id = meeting_guest.m_id AND meeting.ms_id = meeting_guest.ms_id "
                 + "INNER JOIN meeting_schedule ON meeting.ms_id = meeting_schedule.ms_id "
                 + "WHERE meeting_guest.bu_id = '" + bu_id + "')";
         
         if (creator && attendee) {
             _sql = _creator + "UNION DISTINCT " + _attendee;
         } else if (creator) {
             _sql = _creator;
         } else if (attendee) {
             _sql = _attendee;
         } else {
             result.clear();
             return true;
         }
         return (_dbAccess.queryDB(result, _sql));// && _dbAccess.queryDB(result, _sql2) && _dbAccess.queryDB(result, _sql3);
     }
     
     /**
      * (0)m_description
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingDescription(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_description "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_inidatetime
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingInitialDatetime(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_inidatetime "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_duration
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingDuration(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_duration "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     } 
     
     /**
      * (0)m_iscancel
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getIsMeetingCancelled(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_iscancel "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_modpass
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingModPass(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_modpass "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)m_userpass
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingUserPass(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT m_userpass "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * Fields<p>
      * (0)mp_title (1)ls_id (2)l_id
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingPresentation(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT * "
                 + "FROM meeting_presentation "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ms_id (2)ma_ismod (3)bu_nick
      * @param result
      * @return
      */
     public boolean getMeetingAttendee(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT ma.*, bu.bu_nick "
                 + "FROM meeting_attendee ma "
                 + "JOIN bbb_user bu "
                 + "ON ma.bu_id = bu.bu_id";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ms_id (2)ma_ismod (3)bu_nick
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingAttendee(ArrayList<ArrayList<String>> result, String ms_id) {
         _sql = "SELECT ma.*, bu.bu_nick "
                 + "FROM meeting_attendee ma "
                 + "JOIN bbb_user bu "
                 + "ON ma.bu_id = bu.bu_id "
                 + "WHERE ma.ms_id = '" + ms_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ms_id (2)ma_ismod (3)bu_nick
      * @param result
      * @param ms_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingAttendee(ArrayList<ArrayList<String>> result, String ms_id, String bu_id) {
         _sql = "SELECT ma.*, bu.bu_nick "
                 + "FROM meeting_attendee ma "
                 + "JOIN bbb_user bu "
                 + "WHERE ma.bu_id = bu.bu_id "
                 + "AND ma.ms_id = '" + ms_id + "' "
                 + "AND ma.bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mac_isattend (2)bu_nick
      * @param result
      * @param ms_id
      * @return
      */
     public boolean getMeetingAttendance(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT ma.bu_id, ma.mac_isattend, bu.bu_nick "
                 + "FROM meeting_attendance ma "
                 + "JOIN bbb_user bu "
                 + "ON bu.bu_id = ma.bu_id "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)ms_id (2)m_id (1)mac_isattend
      * @param result
      * @param ms_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingAttendance(ArrayList<ArrayList<String>> result, String ms_id, String m_id, String bu_id) {
         _sql = "SELECT * "
                 + "FROM meeting_attendance "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mg_ismod (2)bu_nick
      * @param result
      * @param ms_id
      * @param m_id
      * @return
      */
     public boolean getMeetingGuest(ArrayList<ArrayList<String>> result, String ms_id, String m_id) {
         _sql = "SELECT mg.bu_id, mg.mg_ismod, bu.bu_id "
                 + "FROM meeting_guest mg "
                 + "JOIN bbb_user bu "
                 + "ON bu.bu_id = mg.bu_id "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)bu_id (1)mg_ismod
      * @param result
      * @param ms_id
      * @param m_id
      * @param bu_id
      * @return
      */
     public boolean getMeetingGuest(ArrayList<ArrayList<String>> result, String ms_id, String m_id, String bu_id) {
         _sql = "SELECT bu_id, mg_ismod "
                 + "FROM meeting_guest "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     public boolean getMeetingSetting(HashMap<String, Integer> result, String ms_id, String m_id) {
         _sql = "SELECT m_setting "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.meeting_setting[0], (value & (1<<6)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[1], (value & (1<<5)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[2], (value & (1<<4)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[3], (value & (1<<3)) == 0 ? 0:1);
             result.put(Settings.meeting_setting[4], (value & (1<<2)) + (value & (1<<1)) + (value & 1));
         }
         return flag;
     }
     
     public boolean isMeeting(MyBoolean bool, String ms_id, String m_id) {
         _sql = "SELECT m_id "
                 + "FROM meeting "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isMPresentation(MyBoolean bool, String mp_title, String ms_id, String m_id) {
         _sql = "SELECT 1 "
                 + "FROM meeting_presentation "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND mp_title = '" + mp_title + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean defaultMeetingSetting(String ms_id, String m_id) {
         _sql = "UPDATE meeting as a "
                 + "CROSS JOIN (SELECT key_value FROM bbb_admin WHERE key_name='default_meeting') as b "
                 + "SET a.m_setting = b.key_value "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.updateDB(_sql);   
     }
     
     public boolean setMeetingSetting(HashMap<String, Integer> map, String ms_id, String m_id) {
         int value;
         value = (map.get(Settings.meeting_setting[0]) << 6)
                 + (map.get(Settings.meeting_setting[1]) << 5)
                 + (map.get(Settings.meeting_setting[2]) << 4)
                 + (map.get(Settings.meeting_setting[3]) << 3)
                 + (map.get(Settings.meeting_setting[4]));
         _sql = "UPDATE meeting "
                 + "SET m_setting = " + value + " "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingDescription(String ms_id, String m_id, String m_description) {
         _sql = "UPDATE meeting "
                 + "SET m_description = '" + m_description + "' "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "'";
         return _dbAccess.updateDB(_sql);
     }
       
     public boolean setMeetingIsCancel(String ms_id, String m_id, boolean m_iscancel) {
         int flag = (m_iscancel == true) ? 1 : 0;
         _sql = "UPDATE meeting "
                 + "SET m_iscancel = " + flag + " "
                 + "WHERE ms_id = '" + ms_id + "' "
                + "AND m_id = '" + m_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingAttendance(String bu_id, String ms_id, String m_id, boolean mac_isattend) {
         int flag = (mac_isattend == true) ? 1 : 0;
         _sql = "UPDATE meeting_attendance "
                 + "SET mac_isattend = " + flag + " "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingGuestIsMod(String bu_id, String ms_id, String m_id, boolean mg_ismod) {
         int flag = (mg_ismod == true) ? 1 : 0;
         _sql = "UPDATE meeting_guest "
                 + "SET mg_ismod = " + flag + " "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingGuestIsMod(String bu_id, String ms_id, String m_id) {
         _sql = "UPDATE meeting_guest "
                 + "SET mg_ismod = NOT mg_ismod "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingAttendeeIsMod(String bu_id, String ms_id, boolean ma_ismod) {
         int flag = (ma_ismod == true) ? 1 : 0;
         _sql = "UPDATE meeting_attendee "
                 + "SET ma_ismod = " + flag + " "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingAttendeeIsMod(String bu_id, String ms_id) {
         _sql = "UPDATE meeting_attendee "
                 + "SET ma_ismod = not ma_ismod "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setMeetingPresentation(String mp_title, String ms_id, String m_id) {
         _sql = "UPDATE meeting_presentation "
                 + "SET mp_title = '" + mp_title + "' "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "WHERE m_id = '" + m_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current meeting only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ms_id
      * @param m_id
      * @param m_duration
      * @return
      */
     public boolean updateMeetingDuration(int num, String ms_id, String m_id, String m_duration) {
         _sql = "CALL sp_update_m_duration("
                 + num + ", '"
                 + ms_id + "', '"
                 + m_id + "', '"
                 + m_duration + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Edit a meeting schedule, delete all future meetings, create new ones<p>
      * ms_spec Format: Type;Subtype;Repeat-or-EndDate;Interval;Weekstring-or-DayOfMonth<p>
      * Type 1: Single occurrence, no Subtype<p>
      *      Example: 1<p>
      * Type 2: Daily<p> 
      *      2 Subtypes:<p>
      *          (1) repeat for a number of times<p>
      *          (2) repeat until a certain date<p>
      *      Example: 2;1;6;2<p>
      *               repeat 6 times with interval of 2 days<p> 
      *               2;2;2013-10-01;7<p>
      *               repeat until end date reached with interval of 7 days<p>
      * Type 3: Weekly<p>
      *      3 Subtypes:<p>
      *          (1) repeat for a number of times<p>
      *          (2) repeat for a number of weeks<p>
      *          (3) repeat until end date is reached<p>
      *      Weekstring: 0011000 Sun|Mon|Tues|Wed|Thurs|Fri|Sat<p>
      *      Example: 3;1;6;1;0111110<p>
      *               3;2;6;2;0110010<p>
      *               3;3;2013-11-11;3;0110010<p>
      * Type 4: Monthly<p>
      *      2 Subtypes:<p>
      *          (1) repeat on same day each month
      *              (date will auto change to the last day of month for shortened month)
      *          (2) repeat the first occurrence of day-of-week in a month
      *      Example: 4;1;7;1;31<p>
      *               4;2;5;1;3<p>
      *               repeat on the first Tuesday of each month for 5 month, repeat every month
      * @param ms_id
      * @param ms_inidatetime (format: 'YYYY-MM-DD HH:MM:SS')
      * @param ms_spec
      * @param m_description
      * @return
      */
     public boolean updateMeetingSchedule(String ms_id, String ms_inidatetime, 
             String ms_spec, String m_description) {
         _sql = "CALL sp_edit_ms('"
                 + ms_id + "', '"
                 + ms_inidatetime + "', '"
                 + ms_spec + "', '"
                 + m_description + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current meeting only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ms_id
      * @param m_id
      * @param time (format HH:MM:SS)
      * @return
      */
     public boolean updateMeetingTime(int num, String ms_id, String m_id, String time) {
         _sql = "CALL sp_update_m_time("
                 + num + ", '"
                 + ms_id + "', '"
                 + m_id + "', '"
                 + time + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingPresentation(String mp_title, String ms_id, String m_id) {
         _sql = "INSERT INTO meeting_presentation VALUES ('"
                 + mp_title + "', '" + ms_id + "', '" + m_id + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingAttendee(String bu_id, String ms_id, boolean ma_ismod) {
         int flag = (ma_ismod == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_attendee VALUES ('"
                 + bu_id + "', '" + ms_id + "', " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingAttendance(String bu_id, String ms_id, String m_id, boolean mac_attend) {
         int flag = (mac_attend == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_attendance VALUES ('"
                 + bu_id + "', '" + ms_id + "', '" + m_id + "', " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createMeetingGuest(String bu_id, String ms_id, String m_id, boolean mg_ismod) {
         int flag = (mg_ismod == true) ? 1 : 0;
         _sql = "INSERT INTO meeting_guest VALUES ('"
                 + bu_id + "', '" + ms_id + "', '" + m_id + "', " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Create meeting schedule as well as all meetings in this meeting schedule<p>
      * ms_spec Format: Type;Subtype;Repeat-or-EndDate;Interval;Weekstring-or-DayOfMonth<p>
      * Type 1: Single occurrence, no Subtype<p>
      *      Example: 1<p>
      * Type 2: Daily<p> 
      *      2 Subtypes:<p>
      *          (1) repeat for a number of times<p>
      *          (2) repeat until a certain date<p>
      *      Example: 2;1;6;2<p>
      *               repeat 6 times with interval of 2 days<p> 
      *               2;2;2013-10-01;7<p>
      *               repeat until end date reached with interval of 7 days<p>
      * Type 3: Weekly<p>
      *      3 Subtypes:<p>
      *          (1) repeat for a number of times<p>
      *          (2) repeat for a number of weeks<p>
      *          (3) repeat until end date is reached<p>
      *      Weekstring: 0011000 Sun|Mon|Tues|Wed|Thurs|Fri|Sat<p>
      *      Example: 3;1;6;1;0111110<p>
      *               3;2;6;2;0110010<p>
      *               3;3;2013-11-11;3;0110010<p>
      * Type 4: Monthly<p>
      *      2 Subtypes:<p>
      *          (1) repeat on same day each month
      *              (date will auto change to the last day of month for shortened month)
      *          (2) repeat the first occurrence of day-of-week in a month
      *      Example: 4;1;7;1;31<p>
      *               4;2;5;1;3<p>
      *               repeat on the first Tuesday of each month for 5 month, repeat every month
      * @param ms_title
      * @param ms_inidatetime (format: 'YYYY-MM-DD HH:MM:SS')
      * @param ms_spec
      * @param ms_duration (in minutes, round to nearest integer)
      * @param m_description
      * @param bu_id
      * @return
      */
     public boolean createMeetingSchedule(String ms_title, String ms_inidatetime, 
             String ms_spec, String ms_duration, String m_description, String bu_id) {
         _sql = "CALL sp_create_ms('"
                 + ms_title + "', '"
                 + ms_inidatetime + "', '"
                 + ms_spec + "', '"
                 + ms_duration + "', '"
                 + m_description + "', '"
                 + bu_id + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingAttendance(String bu_id, String ms_id, String m_id) {
         _sql = "DELETE FROM meeting_attendance "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingAttendee(String bu_id, String ms_id) {
         _sql = "DELETE FROM meeting_attendee "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingGuest(String bu_id, String ms_id, String m_id) {
         _sql = "DELETE FROM meeting_guest "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeMeetingPresentation(String mp_title, String ms_id, String m_id) {
         _sql = "DELETE FROM meeting_presentation "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND mp_title = '" + mp_title + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Only meetings yet to happen will be removed
      * meeting schedule only removed if there is no meeting associated with it
      * @param ms_id
      * @return
      */
     public boolean removeMeetingSchedule(String ms_id) {
         _sql = "CALL sp_delete_ms('" + ms_id + "')";
         return _dbAccess.updateDB(_sql);
     }
 }
