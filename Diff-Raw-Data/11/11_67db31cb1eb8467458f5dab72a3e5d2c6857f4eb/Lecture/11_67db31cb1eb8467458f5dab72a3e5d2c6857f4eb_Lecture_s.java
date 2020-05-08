 package sql;
 
 import helper.MyBoolean;
 import helper.Settings;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
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
 public class Lecture extends Sql {
 
     public Lecture(DBAccess source) {
         super(source);
     }
 
     /**
      * Get data on a particular Lecture<p>
      * Fields:<p>
      * (0)ls_id (1)l_id (2)l_inidatetime (3)l_duration (4)l_iscancel
      * (5)l_description (6)l_modpass (7)l_userpass
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureInfo(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT lecture.* "
                 + "FROM lecture "
                 + "WHERE lecture.ls_id = '" + ls_id + "' "
                 + "AND lecture.l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Get data on lectures in a particular Lecture Schedule<p>
      * Fields:<p>
      * (0)ls_id (1)l_id (2)l_inidatetime (3)l_duration (4)l_iscancel
      * (5)l_description (6)l_modpass (7)l_userpass
      * @param result
      * @param l_id
      * @return
      */
     public boolean getLectureInfo(ArrayList<ArrayList<String>> result, String ls_id) {
         _sql = "SELECT lecture.* "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:<p>
      * (0)ls_id (1)c_id (2)sc_id (3)sc_semesterid (4)ls_inidatetime
      * (5)ls_spec (6)ls_duration
      * @param result
      * @return
      */
     public boolean getLectureScheduleInfo(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT * "
                 + "FROM lecture_schedule";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:<p>
      * (0)ls_id (1)c_id (2)sc_id (3)sc_semesterid (4)ls_inidatetime
      * (5)ls_spec (6)ls_duration
      * @param result
      * @param ls_id
      * @return
      */
     public boolean getLectureScheduleInfo(ArrayList<ArrayList<String>> result, String ls_id) {
         _sql = "SELECT * "
                 + "FROM lecture_schedule "
                 + "WHERE ls_id = '" + ls_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)l_description
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureDescription(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_description "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)l_inidatetime
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureInitialDatetime(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_inidatetime "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)l_duration
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureDuration(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_duration "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     } 
     
     /**
      * (0)l_duration
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getProfessorCourse(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT c_id,sc_id,sc_semesterid "
                 + "FROM professor "
                 + "WHERE bu_id = '" + bu_id + "' ";              
         return _dbAccess.queryDB(result, _sql);
     } 
 
     /**
      * (0)bu_id (1)c_id (2)sc_id (3)sc_semesterid
      * @param result
      * @return
      */
     public boolean getAllProfessorCourse(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT bu_id,c_id,sc_id,sc_semesterid "
                 + "FROM professor ";            
         return _dbAccess.queryDB(result, _sql);
     } 
     
     /**
      * (0)bu_id 
      * @param result 
      * @param c_id
      * @param sc_id
      * @param sc_semesterid
      * @return
      */
     public boolean getLectureProfessor(ArrayList<ArrayList<String>> result,String c_id,String sc_id,String sc_semesterid) {
         _sql = "SELECT bu_id "
                 + "FROM professor "
         		+ "WHERE c_id = '" + c_id + "' "
         		+ "AND sc_id = '" + sc_id + "' "
         		+ "AND sc_semesterid = '" + sc_semesterid + "'";            
         return _dbAccess.queryDB(result, _sql);
     } 
 
     /**
      * (0)l_iscancel
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getIsLectureCancelled(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_iscancel "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)l_modpass
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureModPass(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_modpass "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)l_userpass
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureUserPass(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT l_userpass "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)sc_setting
      * @param result
      * @param c_id
      * @param sc_id
      * @param sc_semesterid
      * @return
      */
     public boolean getLectureSetting(HashMap<String, Integer> result, String c_id, String sc_id, String sc_semesterid) {
         _sql = "SELECT sc_setting "
                 + "FROM professor "
                 + "WHERE c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.section_setting[0], (value & (1<<6)) == 0 ? 0:1);
             result.put(Settings.section_setting[1], (value & (1<<5)) == 0 ? 0:1);
             result.put(Settings.section_setting[2], (value & (1<<4)) == 0 ? 0:1);
             result.put(Settings.section_setting[3], (value & (1<<3)) == 0 ? 0:1);
             result.put(Settings.section_setting[4], (value & (1<<2)) + (value & (1<<1)) + (value & 1));
         }
         return flag;
     }
         
     /**
      * Fields<p>
      * (0)lp_title (1)ls_id (2)l_id
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLecturePresentation(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT * "
                 + "FROM lecture_presentation "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_id (1)la_isattend (2)bu_nick
      * @param result
      * @param ls_id
      * @return
      */
     public boolean getLectureAttendance(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT la.bu_id, la.la_isattend, bu.bu_nick "
                 + "FROM lecture_attendance la "
                 + "JOIN bbb_user bu "
                 + "ON la.bu_id = bu.bu_id "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_id (1)la_isattend
      * @param result
      * @param ls_id
      * @param bu_id
      * @return
      */
     public boolean getLectureAttendance(ArrayList<ArrayList<String>> result, String ls_id, String l_id, String bu_id) {
         _sql = "SELECT bu_id, la_isattend "
                 + "FROM lecture_attendance "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = " + l_id + " "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_id (1)gl_ismod (2)bu_nick
      * @param result
      * @param ls_id
      * @param l_id
      * @return
      */
     public boolean getLectureGuest(ArrayList<ArrayList<String>> result, String ls_id, String l_id) {
         _sql = "SELECT gl.bu_id, gl.gl_ismod, bu.bu_id "
                 + "FROM guest_lecturer gl "
                 + "JOIN bbb_user bu "
                 + "ON gl.bu_id = bu.bu_id "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:<p>
      * (0)ls_id (1)l_id (2)l_inidatetime (3)l_duration (4)l_iscancel (5)l_description (6)l_modpass (7)l_userpass 
      * (8)c_id (9)sc_id (10)sc_semesterid
      * @param result
      * @param bu_id
      * @param professor
      * @param student
      * @return
      */
     public boolean getLecturesForUser(ArrayList<ArrayList<String>> result, String bu_id, boolean professor, boolean student) {
     	String _professor = "(SELECT lecture.*, lecture_schedule.c_id, lecture_schedule.sc_id, lecture_schedule.sc_semesterid " 
     			+ "FROM lecture "
     			+ "INNER JOIN lecture_schedule ON lecture.ls_id = lecture_schedule.ls_id "
     			+ "INNER JOIN professor ON lecture_schedule.c_id = professor.c_id AND lecture_schedule.sc_id = professor.sc_id AND lecture_schedule.sc_semesterid = professor.sc_semesterid "
     			+ "WHERE professor.bu_id = '" + bu_id +"') ";
     			
     	String _GuestProfessor = "(SELECT lecture.*, lecture_schedule.c_id, lecture_schedule.sc_id, lecture_schedule.sc_semesterid " 
                 + "FROM lecture "
                 + "INNER JOIN lecture_schedule ON lecture.ls_id = lecture_schedule.ls_id "
                 + "INNER JOIN guest_lecturer ON guest_lecturer.ls_id = lecture.ls_id AND lecture.l_id = guest_lecturer.l_id "
                 + "WHERE guest_lecturer.bu_id = '" + bu_id +"') ";
     	
     	String _student = "(SELECT lecture.*, lecture_schedule.c_id, lecture_schedule.sc_id, lecture_schedule.sc_semesterid " 
     			+ "FROM lecture "
     			+ "INNER JOIN lecture_schedule ON lecture.ls_id = lecture_schedule.ls_id "
     			+ "INNER JOIN student ON lecture_schedule.c_id = student.c_id AND lecture_schedule.sc_id = student.sc_id AND lecture_schedule.sc_semesterid = student.sc_semesterid "
     			+ "WHERE student.bu_id = '" + bu_id +"') ";
     	
     	if (professor && student) {
     		_sql = _professor + "UNION DISTINCT " + _GuestProfessor + "UNION DISTINCT " + _student;
     	} else if (professor) {
     		_sql = _professor + "UNION DISTINCT " + _GuestProfessor;
     	} else if (student) {
     		_sql = _student;
     	} else {
     		result.clear();
     		return true;
     	}
     	return (_dbAccess.queryDB(result, _sql));
     }
     
     /**
      * (0)bu_id (1)gl_ismod
      * @param result
      * @param ls_id
      * @param l_id
      * @param bu_id
      * @return
      */
     public boolean getLectureGuest(ArrayList<ArrayList<String>> result, String ls_id, String l_id, String bu_id) {
         _sql = "SELECT bu_id, gl_ismod "
                 + "FROM guest_lecturer "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     public boolean isLecture(MyBoolean bool, String ls_id, String l_id) {
         _sql = "SELECT l_id "
                 + "FROM lecture "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isLPresentation(MyBoolean bool, String lp_title, String ls_id, String l_id) {
         _sql = "SELECT 1 "
                 + "FROM lecture_presentation "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND lp_title = '" + lp_title + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean setLectureDescription(String ls_id, String l_id, String l_description) {
         _sql = "UPDATE lecture "
                 + "SET l_description = '" + l_description + "' "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean setLectureSetting(HashMap<String, Integer> map, String c_id, String sc_id, String sc_semesterid) {
         int value;
         value = (map.get(Settings.section_setting[0]) << 6)
                 + (map.get(Settings.section_setting[1]) << 5)
                 + (map.get(Settings.section_setting[2]) << 4)
                 + (map.get(Settings.section_setting[3]) << 3)
                 + (map.get(Settings.section_setting[4]));
         _sql = "UPDATE professor "
                 + "SET sc_setting = " + value + " "
                 + "WHERE c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setLectureIsCancel(String ls_id, String l_id, boolean l_iscancel) {
         int flag = (l_iscancel == true) ? 1 : 0;
         _sql = "UPDATE lecture "
                 + "SET l_iscancel = " + flag + " "
                 + "WHERE ls_id = '" + ls_id + "' "
                + "AND l_id = '" + l_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean setLectureAttendance(String bu_id, String ls_id, String l_id, boolean la_isattend) {
         int flag = (la_isattend == true) ? 1 : 0;
         _sql = "UPDATE lecture_attendance "
                 + "SET la_isattend = " + flag + " "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean setLectureGuestIsMod(String bu_id, String ls_id, String l_id, boolean gl_ismod) {
         int flag = (gl_ismod == true) ? 1 : 0;
         _sql = "UPDATE guest_lecturer "
                 + "SET gl_ismod = " + flag + " "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setLectureGuestIsMod(String bu_id, String ls_id, String l_id) {
         _sql = "UPDATE guest_lecturer "
                 + "SET gl_ismod = not gl_ismod "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setLecturePresentation(String lp_title, String ls_id, String l_id) {
         _sql = "UPDATE lecture_presentation "
                 + "SET lp_title = '" + lp_title + "' "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "WHERE l_id = '" + l_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current Lecture only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ls_id
      * @param l_id
      * @param l_duration
      * @return
      */
     public boolean updateLectureDuration(int num, String ls_id, String l_id, String l_duration) {
         _sql = "CALL sp_update_l_duration("
                 + num + ", '"
                 + ls_id + "', '"
                 + l_id + "', '"
                 + l_duration + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 
      * @param num (1, 2, 3)<p>
      * (1) change the current Lecture only<p>
        (2) change all sessions after and including the current one<p>
        (3) change all sessions not yet passed (reference to sysdate())
      * @param ls_id
      * @param l_id
      * @param time (format HH:MM:SS)
      * @return
      */
     public boolean updateLectureTime(int num, String ls_id, String l_id, String time) {
         _sql = "CALL sp_update_l_time("
                 + num + ", '"
                 + ls_id + "', '"
                 + l_id + "', '"
                 + time + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Edit a lecture schedule, delete all future lectures, create new ones<p>
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
      * @param ls_id
      * @param ls_inidatetime (format: 'YYYY-MM-DD HH:MM:SS')
      * @param ls_spec
      * @param l_description
      * @return
      */
     public boolean updateLectureSchedule(String ls_id, String ls_inidatetime, 
             String ls_spec, String l_description) {
         _sql = "CALL sp_edit_ls('"
                 + ls_id + "', '"
                 + ls_inidatetime + "', '"
                 + ls_spec + "', '"
                 + l_description + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean createLecturePresentation(String lp_title, String ls_id, String l_id) {
         _sql = "INSERT INTO lecture_presentation VALUES ('"
                 + lp_title + "', '" + ls_id + "', '" + l_id + "')";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean createLectureAttendance(String bu_id, String ls_id, String l_id, boolean la_isattend) {
         int flag = (la_isattend == true) ? 1 : 0;
         _sql = "INSERT INTO lecture_attendance VALUES ('"
                 + bu_id + "', '" + ls_id + "', '" + l_id + "', " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean createLectureGuest(String bu_id, String ls_id, String l_id, boolean gl_ismod) {
         int flag = (gl_ismod == true) ? 1 : 0;
         _sql = "INSERT INTO guest_lecturer VALUES ('"
                 + bu_id + "', '" + ls_id + "', '" + l_id + "', " + flag + ")";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Create lecture schedule as well as all lectures in this lecture schedule<p>
      * ls_spec Format: Type;Subtype;Repeat-or-EndDate;Interval;Weekstring-or-DayOfMonth<p>
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
      * @param ls_title
      * @param ls_inidatetime (format: 'YYYY-MM-DD HH:MM:SS')
      * @param ls_spec
      * @param ls_duration (in minutes, round to nearest integer)
      * @param l_description
      * @return
      */
     public boolean createLectureSchedule(String c_id, String sc_id, String sc_semesterid, 
             String ls_inidatetime, String ls_spec, String ls_duration, String l_description) {
         _sql = "CALL sp_create_ls('"
                 + c_id + "', '"
                 + sc_id + "', '"
                 + sc_semesterid + "', '"
                 + ls_inidatetime + "', '"
                 + ls_spec + "', '"
                 + ls_duration + "', '"
                 + l_description + "')";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeLectureAttendance(String bu_id, String ls_id, String l_id) {
         _sql = "DELETE FROM lecture_attendance "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeLectureGuest(String bu_id, String ls_id, String l_id) {
         _sql = "DELETE FROM guest_lecturer "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean removeLecturePresentation(String lp_title, String ls_id, String l_id) {
         _sql = "DELETE FROM lecture_presentation "
                 + "WHERE ls_id = '" + ls_id + "' "
                 + "AND l_id = '" + l_id + "' "
                 + "AND lp_title = '" + lp_title + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Only lectures yet to happen will be removed
      * lecture schedule only removed if there is no lecture associated with it
      * @param ls_id
      * @return
      */
     public boolean removeLectureSchedule(String ls_id) {
         _sql = "CALL sp_delete_ls('" + ls_id + "')";
         return _dbAccess.updateDB(_sql);
     }
 }
