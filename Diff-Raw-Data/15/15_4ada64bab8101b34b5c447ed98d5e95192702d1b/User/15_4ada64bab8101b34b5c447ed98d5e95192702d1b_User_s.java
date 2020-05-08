 //User.java
 package sql;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import helper.*;
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
 public class User extends Sql {
 
     public User(DBAccess source) {
         super(source);
     }
     
     /** 
      * the following are query (SELECT) methods
      * that begin with 'get'
      */
 
     /**
      * Fields:
      * (0)nu_hash
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getHash(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT non_ldap_user.nu_hash "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:
      * (0)nu_salt
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getSalt(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT non_ldap_user.nu_salt "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)nu_salt (1)nu_hash
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getSaltAndHash(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT non_ldap_user.nu_salt, non_ldap_user.nu_hash "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Getting all data of all bbb_users<p>
      * Fields:<p>
      * (0)bu_id (1)bu_nick (2)bu_isbanned (3)bu_isactive (4)bu_comment (5)bu_lastlogin (6)bu_isldap (7)bu_issuper
      * (8)ur_id (9)bu_setting (10)bbb_user.m_setting (11)nu_name (12)nu_lastname (13)nu_email (14)nu_createtime
      * (15)pr_name (16)ur_rolemask 
      * @param result
      * @return
      */
     public boolean getUserInfo(ArrayList<ArrayList<String>> result) {
         _sql = "SELECT bbb_user.*, non_ldap_user.nu_name, non_ldap_user.nu_lastname, "
                 + "non_ldap_user.nu_email, non_ldap_user.nu_createtime, user_role.pr_name, user_role.ur_rolemask "
                 + "FROM bbb_user "
                 + "LEFT OUTER JOIN non_ldap_user "
                 + "ON bbb_user.bu_id = non_ldap_user.bu_id "
                 + "INNER JOIN user_role "
                 + "ON bbb_user.ur_id = user_role.ur_id "
                 + "ORDER BY bbb_user.bu_id";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Getting all data of all bbb_users limited by page<p>
      * Fields:<p>
      * (0)bu_id (1)bu_nick (2)bu_isbanned (3)bu_isactive (4)bu_comment (5)bu_lastlogin (6)bu_isldap (7)bu_issuper
      * (8)ur_id (9)bu_setting (10)bbb_user.m_setting (11)nu_name (12)nu_lastname (13)nu_email (14)nu_createtime
      * (15)pr_name (16)ur_rolemask 
      * @param result
      * @param page first page is 1
      * @param size
      * @return
      */
     public boolean getUserInfo(ArrayList<ArrayList<String>> result, int page, int size) {
         int start = (page-1)*size;
         _sql = "SELECT bbb_user.*, non_ldap_user.nu_name, non_ldap_user.nu_lastname, "
                 + "non_ldap_user.nu_email, non_ldap_user.nu_createtime, user_role.pr_name, user_role.ur_rolemask "
                 + "FROM bbb_user "
                 + "LEFT OUTER JOIN non_ldap_user "
                 + "ON bbb_user.bu_id = non_ldap_user.bu_id "
                 + "INNER JOIN user_role "
                 + "ON bbb_user.ur_id = user_role.ur_id "
                 + "ORDER BY bbb_user.bu_id "
                 + "LIMIT " + start + "," + size;
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Getting all data related to a bbb_user<p>
      * Fields:<p>
      * (0)bu_id (1)bu_nick (2)bu_isbanned (3)bu_isactive (4)bu_comment (5)bu_lastlogin (6)bu_isldap (7)bu_issuper
      * (8)ur_id (9)bu_setting (10)bbb_user.m_setting (11)nu_name (12)nu_lastname (13)nu_email (14)nu_createtime
      * (15)pr_name (16)ur_rolemask 
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getUserInfo(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bbb_user.*, non_ldap_user.nu_name, non_ldap_user.nu_lastname, "
                 + "non_ldap_user.nu_email, non_ldap_user.nu_createtime, user_role.pr_name, user_role.ur_rolemask "
                 + "FROM bbb_user "
                 + "LEFT OUTER JOIN non_ldap_user "
                 + "ON bbb_user.bu_id = non_ldap_user.bu_id "
                 + "INNER JOIN user_role "
                 + "ON bbb_user.ur_id = user_role.ur_id "
                 + "WHERE bbb_user.bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:
      * (0)nu_name
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getName(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT nu_name "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:
      * (0)nu_lastname
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getLastName(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT nu_lastname "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:
      * (0)bu_nick
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getNickName(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_nick "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * Fields:
      * (0)ur_id (1)pr_name
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getRoleInfo(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT user_role.ur_id, user_role.pr_name "
                 + "FROM user_role "
                 + "INNER JOIN bbb_user "
                 + "ON user_role.ur_id = bbb_user.ur_id "
                 + "WHERE bbb_user.bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
     
     /**
      * (0)d_code, (1)d_name
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getDepartment(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT department.d_code, department.d_name "
                 + "FROM department "
                 + "INNER JOIN user_department "
                 + "ON department.d_code = user_department.d_code "
                 + "WHERE user_department.bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_lastlogin
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getLastLogin(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_lastlogin "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_isactive
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getIsActive(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_isactive "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)s_isbanned
      * @param result
      * @param bu_id
      * @param c_id
      * @param sc_id
      * @param sc_semesterid
      * @return
      */
     public boolean getIsBannedFromSection(ArrayList<ArrayList<String>> result, 
             String bu_id, String c_id, String sc_id, String sc_semesterid) {
         _sql = "SELECT s_isbanned "
                 + "FROM student "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_isbanned
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getIsBannedFromSystem(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_isbanned "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_comment
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getComment(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_comment "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * (0)bu_issuper
      * @param result
      * @param bu_id
      * @return
      */
     public boolean getIsSuperAdmin(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_issuper "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.queryDB(result, _sql);
     }
 
     /**
      * get setting methods 
      */
     
     public boolean getUsersLike(ArrayList<ArrayList<String>> result, String bu_id) {
         _sql = "SELECT bu_id "
                 + "FROM bbb_user "
                 + "WHERE bu_id LIKE '" + bu_id + "%' ";
         return _dbAccess.queryDB(result, _sql);
     }
     
     public boolean getUserSetting(HashMap<String, Integer> result, String bu_id) {
         _sql = "SELECT bu_setting "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.bu_setting[0], (value & (1<<2)) == 0 ? 0:1);
             result.put(Settings.bu_setting[1], (value & (1<<1)) == 0 ? 0:1);
             result.put(Settings.bu_setting[2], (value & 1) == 0 ? 0:1);
         }
         return flag;
     }
     
     public boolean getDefaultUserSetting(HashMap<String, Integer> result) {
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
         		+ "WHERE key_name = 'default_user_hr'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.bu_setting[0], (value & (1<<2)) == 0 ? 0:1);
             result.put(Settings.bu_setting[1], (value & (1<<1)) == 0 ? 0:1);
             result.put(Settings.bu_setting[2], (value & 1) == 0 ? 0:1);
         }
         return flag;
     }
 
     public boolean getUserMeetingSetting(HashMap<String, Integer> result, String bu_id) {
         _sql = "SELECT m_setting "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "'";
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
 
     public boolean getDefaultMeetingSetting(HashMap<String, Integer> result) {
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
                 + "WHERE key_name = 'default_meeting_hr'";
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
     
     public boolean getSectionSetting(HashMap<String, Integer> result, 
             String bu_id, String c_id, String sc_id, String sc_semesterid) {
         _sql = "SELECT sc_setting "
                 + "FROM professor "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
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
     
     public boolean getDefaultSectionSetting(HashMap<String, Integer> result) {
         _sql = "SELECT key_value "
                 + "FROM bbb_admin "
                 + "WHERE key_name = 'default_class_hr'";
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
     
     public boolean getUserRoleSetting(HashMap<String, Integer> result, int ur_id) {
         _sql = "SELECT ur_rolemask "
                 + "FROM user_role "
                 + "WHERE ur_id = " + ur_id;
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.ur_rolemask[0], (value & (1<<1)) == 0 ? 0:1);
             result.put(Settings.ur_rolemask[1], (value & 1) == 0 ? 0:1);
             result.put(Settings.ur_rolemask[2], (value & 1) == 0 ? 0:1);
         }
         return flag;
     }
     
     public boolean getPredefinedUserRoleSetting(HashMap<String, Integer> result, String pr_name) {
         _sql = "SELECT pr_defaultmask "
                 + "FROM predefined_role "
                 + "WHERE pr_name = '" + pr_name + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             int value = Integer.valueOf(tempResult.get(0).get(0)).intValue();
             result.clear();
             result.put(Settings.ur_rolemask[0], (value & (1<<1)) == 0 ? 0:1);
             result.put(Settings.ur_rolemask[1], (value & 1) == 0 ? 0:1);
          //   result.put(Settings.ur_rolemask[0], (value & (1<<2)) == 0 ? 0:1);
          //   result.put(Settings.ur_rolemask[1], (value & (1<<1)) == 0 ? 0:1);
          //   result.put(Settings.ur_rolemask[2], (value & 1) == 0 ? 0:1);
         }
         return flag;
     }
     
     /**
      * the following queries used to test the exist of a value in a table
      * these methods begin with 'is'
      * WARNING: the return boolean value is the first parameter (boolean result)
      * NOT the method return boolean value, which is used to determine the success/failure of SQL execution
      */
     
 
     public boolean isMeetingCreator(MyBoolean bool, String ms_id, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM meeting_schedule "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND bu_id = '" + bu_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     } 
     
     public boolean isMeetingAttendee(MyBoolean bool, String ms_id, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM meeting_attendee "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isMeetingGuest(MyBoolean bool, String ms_id, String m_id, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM meeting_guest "
                 + "WHERE ms_id = '" + ms_id + "' "
                 + "AND m_id = '" + m_id + "' "
                 + "AND bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isTeaching(MyBoolean bool, String ls_id, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM professor p "
                 + "JOIN lecture_schedule ls "
                 + "ON p.c_id = ls.c_id "
                 + "AND p.sc_id = ls.sc_id "
                 + "AND p.sc_semesterid = ls.sc_semesterid "
                 + "WHERE ls.ls_id = '" + ls_id + "'"
                 + "AND p.bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isGuestTeaching(MyBoolean bool, String ls_id, String l_id, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM guest_lecturer gl "
                 + "WHERE gl.bu_id = '" + bu_id + "' "
                 + "AND gl.ls_id = '" + ls_id + "' "
                 + "AND gl.l_id = '" + l_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isLectureStudent(MyBoolean bool, String ls_id, String bu_id) {
         _sql = "SELECT s.bu_id "
                 + "FROM student s "
                 + "JOIN lecture_schedule ls "
                 + "ON s.c_id = ls.c_id "
                 + "AND s.sc_id = ls.sc_id "
                 + "AND s.sc_semesterid = ls.sc_semesterid "
                 + "WHERE ls.ls_id = '" + ls_id + "'"
                 + "AND s.bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isLectureStudent(MyBoolean bool, String c_id,  String sc_id, String sc_semesterid,String bu_id) {
         _sql = "SELECT * "
                 + "FROM student "
                 + "WHERE c_id = '" + c_id + "'"
                 + " AND sc_id = '" + sc_id + "'"
                 + " AND sc_semesterid = '" + sc_semesterid + "'"
                 + " AND bu_id = '" + bu_id + "'";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     
     public boolean isProfessor(MyBoolean bool, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM professor "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isDepartmentUser(MyBoolean bool, String bu_id, String d_code) {
         _sql = "SELECT 1 "
                 + "FROM user_department "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND d_code = '" + d_code + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isDepartmentAdmin(MyBoolean bool, String bu_id) {
         _sql = "SELECT ud_isadmin "
                 + "FROM user_department "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND ud_isadmin = 1 "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isDepartmentAdmin(MyBoolean bool, String bu_id, String d_code) {
         _sql = "SELECT ud_isadmin "
                 + "FROM user_department "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND d_code = '" + d_code + "' "
                 + "AND ud_isadmin = 1 "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isUser(MyBoolean bool, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM bbb_user "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     public boolean isnonLDAP(MyBoolean bool, String bu_id) {
         _sql = "SELECT 1 "
                 + "FROM non_ldap_user "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "LIMIT 1";
         ArrayList<ArrayList<String>> tempResult = new ArrayList<ArrayList<String>>();
         boolean flag =_dbAccess.queryDB(tempResult, _sql);
         if (flag) {
             bool.set_value(tempResult.isEmpty() ? false : true);
         }
         return flag;
     }
     
     /**
      * the following are UPDATE methods that begin with 'default'
      */
     
     public boolean defaultUserSetting(String bu_id) {
         _sql = "UPDATE bbb_user as a "
                 + "CROSS JOIN (SELECT key_value FROM bbb_admin WHERE key_name='default_user') as b "
                 + "SET a.bu_setting = b.key_value "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);   
     }
 
     public boolean defaultUserMeetingSetting(String bu_id) {
         _sql = "UPDATE bbb_user as a "
                 + "CROSS JOIN (SELECT key_value FROM bbb_admin WHERE key_name='default_meeting') as b "
                 + "SET a.m_setting = b.key_value "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);   
     }
 
     public boolean defaultSectionSetting(String bu_id, String c_id, String sc_id, String sc_semesterid) {
         _sql = "UPDATE professor as a "
                 + "CROSS JOIN (SELECT key_value FROM bbb_admin WHERE key_name='default_class') as b "
                 + "SET a.sc_setting = b.key_value "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.updateDB(_sql);   
     }
     
     public boolean defaultUserRoleSetting(int ur_id) {
         _sql = "UPDATE user_role as a "
                 + "CROSS JOIN (SELECT * FROM predefined_role) as b "
                 + "SET a.ur_rolemask = b.pr_defaultmask "
                 + "WHERE a.ur_id = " + ur_id + " "
                 + "AND a.pr_name = b.pr_name";
         return _dbAccess.updateDB(_sql);   
     }
     
 
     /**
      * the following are UPDATE methods that begin with 'set'
      */
     
     public boolean setUserSetting(HashMap<String, Integer> map, String bu_id) {
         int value;
         value = (map.get(Settings.bu_setting[0]) << 2)
                 + (map.get(Settings.bu_setting[1]) << 1)
                 + (map.get(Settings.bu_setting[2]));
         _sql = "UPDATE bbb_user "
                 + "SET bu_setting = " + value + " "
                 + "WHERE bu_id='" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean setUserMeetingSetting(HashMap<String, Integer> map, String bu_id) {
         int value;
         value = (map.get(Settings.meeting_setting[0]) << 6)
                 + (map.get(Settings.meeting_setting[1]) << 5)
                 + (map.get(Settings.meeting_setting[2]) << 4)
                 + (map.get(Settings.meeting_setting[3]) << 3)
                 + (map.get(Settings.meeting_setting[4]));
         _sql = "UPDATE bbb_user "
                 + "SET m_setting = " + value + " "
                 + "WHERE bu_id='" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 
     public boolean setSectionSetting(HashMap<String, Integer> map, 
             String bu_id, String c_id, String sc_id, String sc_semesterid) {
         int value;
         value = (map.get(Settings.section_setting[0]) << 6)
                 + (map.get(Settings.section_setting[1]) << 5)
                 + (map.get(Settings.section_setting[2]) << 4)
                 + (map.get(Settings.section_setting[3]) << 3)
                 + (map.get(Settings.section_setting[4]));
         _sql = "UPDATE professor "
                 + "SET sc_setting = " + value + " "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setUserRoleSetting(HashMap<String, Integer> map, int ur_id) {
         int value;
         value = (map.get(Settings.ur_rolemask[0]) << 1)
                 + (map.get(Settings.ur_rolemask[1]));
         _sql = "UPDATE user_role "
                 + "SET ur_rolemask = " + value + " "
                 + "WHERE ur_id=" + ur_id;
         return _dbAccess.updateDB(_sql);
     }
 
     /** 
      * the following are UPDATE SQL methods
      */
 
     public boolean setSalt(String bu_id, String nu_salt) {
         _sql = "UPDATE non_ldap_user "
                 + "SET nu_salt = '" + nu_salt + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setHash(String bu_id, String nu_hash) {
         _sql = "UPDATE non_ldap_user "
                 + "SET nu_hash = '" + nu_hash + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setName(String bu_id, String nu_name) {
         _sql = "UPDATE non_ldap_user "
                 + "SET nu_name = '" +  nu_name + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setEmail(String bu_id, String nu_email) {
         _sql = "UPDATE non_ldap_user "
                 + "SET nu_email = '" +  nu_email + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setLastName(String bu_id, String nu_lastname) {
         _sql = "UPDATE non_ldap_user "
                 + "SET nu_lastname = '" +  nu_lastname + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setNickName(String bu_id, String bu_nick) {
         _sql = "UPDATE bbb_user "
                 + "SET bu_nick = '" +  bu_nick + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setRoleName(String pr_name_old, String pr_name_new) {
         _sql = "UPDATE predefined_role "
                 + "SET pr_name = '" +  pr_name_new + "' "
                 + "WHERE pr_name = '" + pr_name_old + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * Uses the current system time when method called
      * @param bu_id
      * @return
      */
     public boolean setLastLogin(String bu_id) {
         _sql = "UPDATE bbb_user "
                 + "SET bu_lastlogin = SYSDATE() "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setActive(String bu_id, boolean bu_isactive) {
         int flag = (bu_isactive == true) ? 1 : 0;
         _sql = "UPDATE bbb_user "
                 + "SET bu_isactive = " + flag + " "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setBannedFromSection(
             String bu_id, String c_id, String sc_id, String sc_semesterid, boolean s_isbanned) {
         int flag = (s_isbanned == true) ? 1 : 0;
         _sql = "UPDATE student "
                 + "set s_isbanned = " + flag + " "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setBannedFromSection(
             String bu_id, String c_id, String sc_id, String sc_semesterid) {
         _sql = "UPDATE student "
                 + "set s_isbanned = not s_isbanned "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND c_id = '" + c_id + "' "
                 + "AND sc_id = '" + sc_id + "' "
                 + "AND sc_semesterid = '" + sc_semesterid + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setBannedFromLecture(String bu_id, String ls_id) {
         _sql = "UPDATE student s "
                 + "JOIN lecture_schedule ls "
                 + "ON s.c_id = ls.c_id "
                 + "AND s.sc_id = ls.sc_id "
                 + "AND s.sc_semesterid = ls.sc_semesterid "
                 + "SET s.s_isbanned = not s.s_isbanned "
                 + "WHERE s.bu_id = '" + bu_id + "' "
                 + "AND ls.ls_id = '" + ls_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setBannedFromSystem(String bu_id, boolean bu_isbanned) {
         int flag = (bu_isbanned == true) ? 1 : 0;
         _sql = "UPDATE bbb_user "
                 + "set bu_isbanned = " + flag + " "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setComment(String bu_id, String bu_comment) {
         _sql = "UPDATE bbb_user "
                 + "SET bu_comment = '" + bu_comment + "' "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setDepartmentAdmin(
             String bu_id, String d_code, boolean ud_isadmin) {
         int flag = (ud_isadmin == true) ? 1 : 0;
         _sql = "UPDATE user_department "
                 + "set ud_isadmin = " + flag + " "
                 + "WHERE bu_id = '" + bu_id + "' "
                 + "AND d_code = '" + d_code + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setSuperAdmin(String bu_id, boolean bu_issuper) {
         int flag = (bu_issuper == true) ? 1 : 0;
         _sql = "UPDATE bbb_user "
                 + "set bu_issuper = " + flag + " "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     public boolean setUserRole(String bu_id, int ur_id) {
         _sql = "UPDATE bbb_user "
                 + "set ur_id = " + ur_id + " "
                 + "WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * the following are methods dealing with create or remove user
      */
     
     /**
      * This creates a general LDAP user
      * to create a non-ldap user call this method first (1st part)
      * then call method createNonLdapUser (2nd part)
      * @param bu_id
      * @param bu_comment
      * @param ur_id
      * @return
      */
     public boolean createUser(String bu_id, String nick, String bu_comment, boolean bu_isldap, int ur_id) {
         _sql = "INSERT INTO bbb_user " 
                 + "VALUES ('" + bu_id + "', '" + nick + "', 0, 1, '"
                 + bu_comment + "', SYSDATE(), " + bu_isldap + ", 0, " + ur_id + ", "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_user'), "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_meeting'))";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * This creates a employee LDAP user
      * to create a non-ldap user call this method first (1st part)
      * then call method createNonLdapUser (2nd part)
      * @param bu_id
      * @param bu_comment
      * @param bu_isldap
      * @return
      */
     public boolean createEmployeeUser(String bu_id, String bu_comment, boolean bu_isldap) {
         _sql = "INSERT INTO bbb_user " 
                 + "VALUES ('" + bu_id + "', '" + bu_id + "', 0, 1, '"
                 + bu_comment + "', null, " + bu_isldap + ", 0, "
                 + "(SELECT ur_id FROM user_role WHERE pr_name = 'employee'), "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_user'), "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_meeting'))";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * This creates a employee LDAP user
      * to create a non-ldap user call this method first (1st part)
      * then call method createNonLdapUser (2nd part)
      * @param bu_id
      * @param bu_comment
      * @param bu_isldap
      * @return
      */
     public boolean createStudentUser(String bu_id, String bu_comment, boolean bu_isldap) {
         _sql = "INSERT INTO bbb_user " 
                 + "VALUES ('" + bu_id + "', '" + bu_id + "', 0, 1, '"
                 + bu_comment + "', null, " + bu_isldap + ", 0, "
                 + "(SELECT ur_id FROM user_role WHERE pr_name = 'student'), "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_user'), "
                 + "(SELECT key_value FROM bbb_admin WHERE key_name='default_meeting'))";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * 2nd part in creating non-ldap user
      * @param bu_id
      * @param nu_name
      * @param nu_lastname
      * @param nu_salt
      * @param nu_hash
      * @param nu_email
      * @return
      */
     public boolean createNonLdapUser(String bu_id, 
             String nu_name, String nu_lastname, String nu_salt, String nu_hash, String nu_email) {
         _sql = "INSERT INTO non_ldap_user "
                 + "VALUES ('" + bu_id + "', '" + nu_name + "', '" + nu_lastname + "', '"
                 + nu_salt + "', '" + nu_hash + "', '" + nu_email + "', SYSDATE())";
         return _dbAccess.updateDB(_sql);
     }
     
     /**
      * WARNING: remove user from database<p>
      * Fairly safe, would gave error if there are child rows dependent on 
      * this bu_id
      * @param bu_id
      * @return
      * */
     public boolean removeUser(String bu_id) {
         _sql = "DELETE FROM bbb_user WHERE bu_id = '" + bu_id + "'";
         return _dbAccess.updateDB(_sql);
     }
 }
